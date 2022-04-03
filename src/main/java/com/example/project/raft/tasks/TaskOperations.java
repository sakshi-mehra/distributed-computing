package com.example.project.raft.tasks;

/**
 * @author revanth on 4/2/22
 */
public interface TaskOperations {
    void cancel();
    void execute();
}
