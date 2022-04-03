package com.example.project.raft.tasks;

import com.example.project.raft.model.Message;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Queue;

/**
 * @author revanth on 4/2/22
 */
public class MessageReceiverTask implements Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageReceiverTask.class);

    private final DatagramSocket socket;
    private byte[] buf = new byte[256];
    private boolean stop = false;
    private final Queue<Message> messageQueue;
    private final Gson gson;
    private final int port = 5555;

    public MessageReceiverTask(Queue<Message> messageQueue) throws SocketException {
        socket = new DatagramSocket(port);
        this.messageQueue = messageQueue;
        gson = new Gson();
    }

    @Override
    public void run() {

        while (!stop) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            try {
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                LOGGER.info(received);
                messageQueue.add(gson.fromJson(received, Message.class));
            } catch (IOException | JsonSyntaxException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void stop() {
        stop = true;
        socket.close();
    }
}

