package com.example.project.raft.tasks;

import com.example.project.raft.HeartbeatCallback;
import com.example.project.raft.RaftImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

/**
 * @author revanth on 4/4/22
 */
public class HeartbeatTask extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatTask.class);

    private final HeartbeatCallback heartbeatCallback;

    public HeartbeatTask(HeartbeatCallback heartbeatCallback) {
        this.heartbeatCallback = heartbeatCallback;
    }

    @Override
    public void run() {

        try {
            heartbeatCallback.sendHeartbeat();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
