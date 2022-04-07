package com.example.project.raft.tasks;

import com.example.project.raft.ElectionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

/**
 * @author revanth on 4/4/22
 */
public class ElectionTask extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElectionTask.class);

    private final long timeout;
    private final ElectionCallback electionCallback;

    public ElectionTask(long timeout, ElectionCallback election) {
        this.timeout = timeout;
        this.electionCallback = election;
    }

    public long getTimeout() {
        return timeout;
    }

    @Override
    public void run() {
        try {
            electionCallback.startElection();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

    }
}
