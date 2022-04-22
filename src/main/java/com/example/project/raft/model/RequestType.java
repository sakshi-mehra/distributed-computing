package com.example.project.raft.model;

/**
 * @author revanth on 4/2/22
 */
public enum RequestType {

    CONVERT_FOLLOWER, TIMEOUT, SHUTDOWN, LEADER_INFO, VOTE_ACK, VOTE_REQUEST, APPEND_RPC, APPEND_RPC_RESPONSE;
}
