package com.example.project.raft.communication;

import com.example.project.raft.model.RequestType;

/**
 * @author revanth on 4/21/22
 */
public interface Message {

    RequestType getRequestType();

    long getTerm();

    String getSenderName();
}
