package com.example.project.raft.server;

import com.example.project.raft.communication.Configs;
import com.example.project.raft.model.NodeState;
import lombok.Getter;
import lombok.Setter;

/**
 * @author revanth on 5/4/22
 */
@Getter
@Setter
public class ServerState {

    private final String serverName;
    private NodeState nodeState;
    private long currentTerm = 0;
    private String votedFor = null;
    private int voteCount;
    private String leaderNode = null;

    // index of the highest log entry known to be committed
    private long commitIndex = 0;
    // index of the highest log entry applied to state machine
    private long lastApplied = 0;

    // for each server, index of the next log entry to send to that server
    private Long[] nextIndex;
    // for each server, index of the highest log entry known to be replicated on server
    private Long[] matchIndex;

    public ServerState(String serverName, int nodeCount) {
        this.serverName = serverName;

        nextIndex = new Long[Configs.NODE_COUNT];
        matchIndex = new Long[Configs.NODE_COUNT];

        this.nodeState = NodeState.FOLLOWER;
        this.voteCount = 0;
    }

    public void incrementCurrTerm() {
        currentTerm++;
    }

    public void incrementVoteCount() {
        voteCount++;
    }

    public void resetVoteCount() {
        voteCount = 0;
    }

    public void setNextIndex(int serverId, Long val) {
        nextIndex[serverId] = val;
    }

    public Long getNextIndex(int serverId) {
        return nextIndex[serverId];
    }

    public void incrementNextIndex(int serverId) {
        nextIndex[serverId]++;
    }

    public void decrementNextIndex(int serverId) {
        nextIndex[serverId]--;
    }

    public void setMatchIndex(int serverId, Long val) {
        matchIndex[serverId] = val;
    }

    public void incrementMatchIndex(int serverId) {
        matchIndex[serverId]++;
    }
}
