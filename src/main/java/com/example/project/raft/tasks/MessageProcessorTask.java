package com.example.project.raft.tasks;

import com.example.project.raft.communication.Message;
import com.example.project.raft.model.BaseMessage;
import com.example.project.raft.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

/**
 * @author revanth on 4/2/22
 */
public class MessageProcessorTask implements Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessorTask.class);

    private volatile boolean stop = false;

    private final Queue<Message> messageQueue;
    private final MessageProcessor messageProcessor;

    public MessageProcessorTask(Queue<Message> messageQueue, MessageProcessor messageProcessor) {
        this.messageQueue = messageQueue;
        this.messageProcessor = messageProcessor;
    }

    // TODO: Fix the busy waiting with wait and notify
    @Override
    public void run() {
        while(!stop) {
            //LOGGER.info(String.valueOf(messageQueue.size()));
            try {
                while (!messageQueue.isEmpty()) {
                    Message msg = messageQueue.poll();
                    if (msg != null)
                        messageProcessor.processMessage(msg);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                // Ignore any exception from the raft processor to continue the execution
            }
        }
    }

    @Override
    public void stop() {
        stop = true;
    }
}
