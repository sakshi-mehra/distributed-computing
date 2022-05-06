package com.example.project.raft.communication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;

/**
 * @author revanth on 4/4/22
 */
public class Sender {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

    private final String hostName;
    private final int port;

    public Sender(String host, int port) {
        this.hostName = host;
        this.port = port;
    }

    public void multicast(String multicastMessage) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName(hostName);
        byte[] buf = multicastMessage.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port);
        socket.send(packet);
        socket.close();
    }

    public void uniCast(String dstServer, String uniCastMessage) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        byte[] buf = uniCastMessage.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(dstServer), port);
        socket.send(packet);
        socket.close();
    }
}
