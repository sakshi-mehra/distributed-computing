package com.example.project.raft.communication;

/**
 * @author revanth on 4/4/22
 */
public class Configs {

    public static final int PORT = 5555;
    public static final int MSG_BUFFER_LENGTH = 256;
    public static final String GROUP_NAME = "230.0.0.0";
    public static final int NODE_COUNT = 5;
    public static final int MAJORITY_COUNT = Math.floorDiv(NODE_COUNT, 2);
    public static final long HEARTBEAT_TIMEOUT = 10000;
}
