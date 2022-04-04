package com.example.project.raft;

import com.example.project.raft.model.Message;
import com.example.project.raft.tasks.MessageProcessorTask;
import com.example.project.raft.tasks.MessageReceiverTask;
import com.example.project.raft.tasks.Task;
import com.example.project.raft.tasks.TaskManger;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author revanth on 4/3/22
 */
public class RaftImpl implements MessageProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaftImpl.class);

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
    }

    public void init() {
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
