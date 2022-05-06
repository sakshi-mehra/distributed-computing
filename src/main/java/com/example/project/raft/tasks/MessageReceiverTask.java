package com.example.project.raft.tasks;

import com.example.project.raft.communication.Configs;
import com.example.project.raft.communication.Message;
import com.example.project.raft.communication.Receiver;
import com.example.project.raft.model.AppendEntriesMessage;
import com.example.project.raft.model.AppendEntriesResponse;
import com.example.project.raft.model.BaseMessage;
import com.example.project.raft.model.RequestVoteMessage;
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

    public MessageReceiverTask(Queue<Message> messageQueue, String host, int port) throws IOException {
        this.messageQueue = messageQueue;
        receiver = new Receiver(this, host, port);
        gson = new Gson();
    }

    @Override
    public void run() {

        while (!stop) {
            try {
                receiver.receive();
            } catch (Exception e) {
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
            BaseMessage baseMessage = gson.fromJson(message, BaseMessage.class);
            Message msg = baseMessage;

            switch (baseMessage.getRequestType()) {
                case VOTE_REQUEST:
                    msg = gson.fromJson(message, RequestVoteMessage.class);
                    break;
                case APPEND_RPC:
                    msg = gson.fromJson(message, AppendEntriesMessage.class);
                    break;
                case APPEND_RPC_RESPONSE:
                    msg = gson.fromJson(message, AppendEntriesResponse.class);
                    break;

            }

            messageQueue.add(msg);
        } catch (JsonSyntaxException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}

