package com.example.project.raft;

import com.example.project.entity.RaftPersistenceInfo;
import com.example.project.raft.model.Message;
import com.example.project.raft.tasks.MessageProcessorTask;
import com.example.project.raft.tasks.MessageReceiverTask;
import com.example.project.raft.tasks.Task;
import com.example.project.raft.tasks.TaskManger;
import com.example.project.service.Impl.RaftPersistenceService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author revanth on 4/3/22
 */

@Component
public class RaftImpl implements MessageProcessor {

    @Autowired
    RaftPersistenceService raftService;

    private static final Logger LOGGER = LoggerFactory.getLogger(RaftImpl.class);
    private long curTerm = 0;
    private String votedFor = null;

    // TODO: There are cases to handle for message queue. It is not thread safe and not size bounded.
    private final Queue<Message> messageQueue;
    private final TaskManger msgProcessorTaskManager;
    private final TaskManger receiverTaskManager;
    private Gson gson;

    public RaftImpl() throws IOException {
        LOGGER.info("Creating RAFT instance");

        messageQueue = new LinkedList<>();
        Task msgProcessorTask = new MessageProcessorTask(messageQueue, this);
        Task msgReceiverTask = new MessageReceiverTask(messageQueue);
        msgProcessorTaskManager = new TaskManger(msgProcessorTask, "MsgProcessorTask");
        receiverTaskManager = new TaskManger(msgReceiverTask, "MsgReceiverTask");
        gson = new Gson();
    }

    public void shutdown() {
        receiverTaskManager.cancel();
        msgProcessorTaskManager.cancel();

        RaftPersistenceInfo raftPersistence = new RaftPersistenceInfo();
        raftPersistence.setCurrentTerm(2);
        raftPersistence.setVotedFor("Node 2");
        raftService.update(raftPersistence);
    }

    public void init() {

        List<RaftPersistenceInfo> raftPersistenceInfoList = raftService.getAllInfo();

        if(raftPersistenceInfoList.size() == 0){
            curTerm = 0;
            votedFor = null;
        } else {
            curTerm = raftPersistenceInfoList.get(0).getCurrentTerm();
            votedFor = raftPersistenceInfoList.get(0).getVotedFor();
        }
        LOGGER.info(String.valueOf(curTerm));
        LOGGER.info(votedFor);

        receiverTaskManager.execute();
        msgProcessorTaskManager.execute();
    }

    @Override
    public void processMessage(Message message) {
        switch (message.getRequestType()) {
            case SHUTDOWN:
                LOGGER.info("Shutdown");
                shutdown();
                break;
            case CONVERT_FOLLOWER:
                LOGGER.info("Convert Follower");
                break;
        }
    }
}
