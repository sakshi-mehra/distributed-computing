package com.example.project.raft.communication;

/**
 * @author revanth on 4/4/22
 */
public class Configs {

    public static final int PORT = 5555;
    public static final int PORT1 = 5556;
    public static final int MSG_BUFFER_LENGTH = 100000;
    public static final String GROUP_NAME = "230.0.0.0";
    public static final String GROUP_NAME2 = "230.0.0.1";
    public static final int NODE_COUNT = 5;
    public static final int MAJORITY_COUNT = Math.floorDiv(NODE_COUNT, 2) + 1;
    public static final long HEARTBEAT_TIMEOUT = 500;
    public static final int ELECTION_TIMEOUT_MIN = 650;
    public static final int ELECTION_TIMEOUT_MAX = 1200;

}
