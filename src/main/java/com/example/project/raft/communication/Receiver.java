package com.example.project.raft.communication;

import com.example.project.raft.tasks.MessageReceiverTask;
import com.example.project.raft.tasks.ReceiveCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * @author revanth on 4/4/22
 */
public class Receiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageReceiverTask.class);

    private final MulticastSocket socket;
    private final InetAddress group;
    private byte[] buf = new byte[Configs.MSG_BUFFER_LENGTH];
    private final ReceiveCallback receiveCallback;

    public Receiver(ReceiveCallback receiveCallback) throws IOException {
        socket = new MulticastSocket(Configs.PORT);
        group = InetAddress.getByName(Configs.GROUP_NAME);
        socket.joinGroup(group);
        this.receiveCallback = receiveCallback;
    }

    public void receive() throws IOException {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String received = new String(packet.getData(), 0, packet.getLength());
        LOGGER.info(received);
        receiveCallback.receive(received);
    }

    public void stop() throws IOException {
        socket.leaveGroup(group);
        socket.close();
    }

}
