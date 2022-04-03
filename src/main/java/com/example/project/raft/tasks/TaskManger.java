package com.example.project.raft.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author revanth on 4/2/22
 */
public class TaskManger implements TaskOperations {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskManger.class);

    public Task task;
    public Thread thread;

    public TaskManger(Task task, String taskName) {
        this.task = task;

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                task.run();
            }
        }, taskName);
    }

    @Override
    public void cancel() {
        task.stop();
        thread.interrupt();
    }

    @Override
    public void execute() {
        thread.start();
    }
}
