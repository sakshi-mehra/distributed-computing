package com.example.project.raft;

import com.example.project.raft.communication.Message;
import com.example.project.raft.model.BaseMessage;

/**
 * @author revanth on 4/3/22
 */
public interface MessageProcessor {

    void processMessage(Message message);
}
