package com.example.project.raft.server;

import com.example.project.raft.ElectionCallback;
import com.example.project.raft.RaftImpl;
import com.example.project.raft.communication.Configs;
import com.example.project.raft.model.NodeState;
import com.example.project.raft.tasks.ElectionTask;
import com.example.project.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;

/**
 * @author revanth on 5/4/22
 */
public class RaftElection implements ElectionCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaftElection.class);

    private final RaftContext raftContext;
    private final ServerState serverState;
    private final RaftImpl raft;

    public RaftElection(RaftContext raftContext) {
        this.raftContext = raftContext;
        this.serverState = raftContext.getServerState();
        this.raft = raftContext.getRaft();
    }

    public synchronized void scheduleElection() {
        long electionTimeout = Utils.getRandom(Configs.ELECTION_TIMEOUT_MIN, Configs.ELECTION_TIMEOUT_MAX);

        ElectionTask electionTask = new ElectionTask(electionTimeout, this);

        try {
            raftContext.getElectionTimer().schedule(electionTask, electionTask.getTimeout());
        } catch (IllegalStateException e) {
            Timer electionTimer = new Timer();
            electionTimer.schedule(electionTask, electionTask.getTimeout());
            raftContext.setElectionTimer(electionTimer);
        }
        raftContext.setElectionTask(electionTask);

        LOGGER.info(String.format("Election scheduled by %s in next %d ms", raftContext.getServerState().getServerName(),
                electionTimeout));
    }

    public void resetElectionTimer() {

        LOGGER.info(String.format("Resetting election timeout by %s set for %d ms", raftContext.getServerState().getServerName(),
                raftContext.getElectionTask().getTimeout()));

        synchronized (raftContext.getLock()) {
            if (raftContext.getElectionTask() != null)
                raftContext.getElectionTask().cancel();
        }

        scheduleElection();
    }

    @Override
    public void startElection() {
        LOGGER.info(String.format("Election timer timed out for %s", serverState.getServerName()));
        serverState.setNodeState(NodeState.CANDIDATE);
        serverState.incrementCurrTerm();
        serverState.setVotedFor(serverState.getServerName());
        serverState.setVoteCount(1);
        raft.updatePersistenceInfo(serverState.getCurrentTerm(), serverState.getVotedFor());

        LOGGER.info(String.format("%s became candidate. Vote count : %d, Current Term : %s",
                serverState.getServerName(), serverState.getVoteCount(), serverState.getCurrentTerm()));
        raft.sendVoteRequestRPC();
        scheduleElection();
    }
}
