package com.example.project.raft.tasks;

import com.example.project.raft.communication.Receiver;
import com.example.project.raft.model.Message;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Queue;

/**
 * @author revanth on 4/2/22
 */
public class MessageReceiverTask implements Task, ReceiveCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageReceiverTask.class);

    private volatile boolean stop = false;
    private final Queue<Message> messageQueue;
    private final Receiver receiver;
    private final Gson gson;

    public MessageReceiverTask(Queue<Message> messageQueue) throws IOException {
        this.messageQueue = messageQueue;
        receiver = new Receiver(this);
        gson = new Gson();
    }

    @Override
    public void run() {

        while (!stop) {
            try {
                receiver.receive();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void stop() {
        stop = true;
        try {
            receiver.stop();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void receive(String message) {
        try {
            messageQueue.add(gson.fromJson(message, Message.class));
        } catch (JsonSyntaxException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}

