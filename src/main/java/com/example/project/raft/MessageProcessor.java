package com.example.project.raft;

import com.example.project.raft.model.Message;

/**
 * @author revanth on 4/3/22
 */
public interface MessageProcessor {

    void processMessage(Message message);
}
