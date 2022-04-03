package com.example.project.raft.tasks;

import com.example.project.raft.model.Message;
import com.example.project.raft.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

/**
 * @author revanth on 4/2/22
 */
public class MessageProcessorTask implements Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessorTask.class);

    private boolean stop = false;

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
            LOGGER.info(String.valueOf(messageQueue.size()));
            while (!messageQueue.isEmpty()) {
                Message msg = messageQueue.poll();
                messageProcessor.processMessage(msg);
            }
           try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
               LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void stop() {
        stop = true;
    }
}
