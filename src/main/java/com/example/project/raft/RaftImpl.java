package com.example.project.raft;

import com.example.project.raft.model.Message;
import com.example.project.raft.tasks.MessageProcessorTask;
import com.example.project.raft.tasks.MessageReceiverTask;
import com.example.project.raft.tasks.TaskManger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
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

    public RaftImpl() throws SocketException {
        LOGGER.info("Creating RAFT instance");
        messageQueue = new LinkedList<>();
        msgProcessorTaskManager = new TaskManger(new MessageProcessorTask(messageQueue, this),
                MessageProcessorTask.class.getSimpleName());
        receiverTaskManager = new TaskManger(new MessageReceiverTask(messageQueue),
                MessageReceiverTask.class.getSimpleName());
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
