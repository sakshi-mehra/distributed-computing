package com.example.project.raft.tasks;

/**
 * @author revanth on 4/4/22
 */
public interface ReceiveCallback {
    void receive(String message);
}
