package com.example.project.raft;

import com.example.project.entity.Log;
import com.example.project.entity.RaftPersistenceInfo;
import com.example.project.raft.communication.Configs;
import com.example.project.raft.communication.Message;
import com.example.project.raft.communication.Sender;
import com.example.project.raft.model.*;
import com.example.project.raft.tasks.*;
import com.example.project.service.Impl.LogReplicationService;
import com.example.project.service.Impl.RaftPersistenceService;
import com.example.project.utils.AnnotationExclusionStrategy;
import com.example.project.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * @author revanth on 4/3/22
 */
@Component
public class RaftImpl implements RAFT, MessageProcessor, ElectionCallback, HeartbeatCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaftImpl.class);

    @Autowired
    private RaftPersistenceService raftPersistenceService;

    @Autowired
    private LogReplicationService logReplicationService;

    // TODO: There are cases to handle for message queue. It is not thread safe and not size bounded.
    private final Queue<Message> messageQueue;
    private final TaskManger msgProcessorTaskManager;
    private final TaskManger receiverTaskManager;
    private Timer electionTimer;
    private final Timer heartbeatTimer;
    private ElectionTask electionTask;
    private HeartbeatTask heartbeatTask;

    private final Sender sender;
    private final Gson gson;

    private final String serverName;
    private NodeState nodeState;
    private long currentTerm = 0;
    private String votedFor = null;
    private int voteCount;
    private int successAppendCount = 0;
    private String leaderNode = null;
    // index of the highest log entry known to be committed
    private long commitIndex = 0;
    // index of the highest log entry applied to state machine
    private long lastApplied = 0;
    private final Object lock = new Object();

    // for each server, index of the next log entry to send to that server
    private Long[] nextIndex;
    // for each server, index of the highest log entry known to be replicated on server
    private Long[] matchIndex;

    public RaftImpl() throws IOException {
        LOGGER.info("Creating RAFT instance");

        messageQueue = new LinkedList<>();
        Task msgProcessorTask = new MessageProcessorTask(messageQueue, this);
        Task msgReceiverTask = new MessageReceiverTask(messageQueue);
        msgProcessorTaskManager = new TaskManger(msgProcessorTask, "MsgProcessorTask");
        receiverTaskManager = new TaskManger(msgReceiverTask, "MsgReceiverTask");
        electionTimer = new Timer();
        heartbeatTimer = new Timer();

        sender = new Sender();
        gson = new GsonBuilder().setExclusionStrategies(new AnnotationExclusionStrategy()).create();

        serverName = Utils.getHostName();
        nodeState = NodeState.FOLLOWER;
        voteCount = 0;

        nextIndex = new Long[Configs.NODE_COUNT];
        matchIndex = new Long[Configs.NODE_COUNT];

    }

    public synchronized void scheduleElection() {
        long electionTimeout = Utils.getRandom(Configs.ELECTION_TIMEOUT_MIN, Configs.ELECTION_TIMEOUT_MAX);
        electionTask = new ElectionTask(electionTimeout, this);
        try {
            electionTimer.schedule(electionTask, electionTask.getTimeout());
        } catch (IllegalStateException e) {
            electionTimer = new Timer();
            electionTimer.schedule(electionTask, electionTask.getTimeout());
        }

        LOGGER.info(String.format("Election scheduled by %s in next %d ms", serverName, electionTimeout));
    }

    public void resetElectionTimer() {
        LOGGER.info(String.format("Resetting election timeout by %s set for %d ms", serverName,
                electionTask.getTimeout()));
        synchronized (lock) {
            if (electionTask != null)
                electionTask.cancel();
        }

        scheduleElection();
    }

    @Override
    public void onVotingRequestReceived(Message message) {
        String candidateName = message.getSenderName();
        RequestVoteMessage requestVoteMessage = (RequestVoteMessage) message;
        long requestTerm = requestVoteMessage.getTerm();
        String senderName = requestVoteMessage.getSenderName();

        // Ignore self request
        if (senderName.equals(serverName))
            return;

        // Ignore if our term is greater than candidate term
        if (requestTerm < currentTerm)
            return;

        long myLastLogIndex = logReplicationService.getLastLogIndex();
        long myLastLogTerm = logReplicationService.getLastLogTerm();
        long requestLastLogIndex = requestVoteMessage.getLastLogIndex();
        long requestLastLogTerm = requestVoteMessage.getLastLogTerm();

        // Check whose log is up-to-date
        if (myLastLogTerm == requestLastLogTerm && requestLastLogIndex < myLastLogIndex) {
            return;
        } else if (myLastLogTerm > requestLastLogTerm) {
            return;
        }

        if (currentTerm < requestTerm) {
            LOGGER.info("Voted for " + candidateName + " for term : " + requestTerm);
            currentTerm = requestTerm;
            votedFor = candidateName;
            updatePersistenceInfo(currentTerm, votedFor);
            sendVoteResponseACK(candidateName);
        }
    }

    @Override
    public void onAppendRPCReceived(Message message) {
        BaseMessage baseMessage = (BaseMessage) message;
        long requestTerm = baseMessage.getTerm();
        String senderName = baseMessage.getSenderName();

        LOGGER.info(String.format("Append RPC obtained from %s for term %s and my term %d and state : %s", senderName,
                requestTerm, currentTerm, nodeState));

        // Ignore self heartbeat
        if (senderName.equals(serverName))
            return;

        if (nodeState == NodeState.FOLLOWER) {
            resetElectionTimer();
            leaderNode = baseMessage.getSenderName();
        } else if (nodeState == NodeState.CANDIDATE) {

            // Received a heart beat from leader and his term is at least my term then withdraw
            if (requestTerm >= currentTerm) {
                nodeState = NodeState.FOLLOWER;
                leaderNode = baseMessage.getSenderName();
                resetElectionTimer();
            }
        } else {
            // If leader

            if (requestTerm == currentTerm) {
                LOGGER.info("**********Cannot have 2 leaders for the same term, so downgrade");
                convertToFollower();
            }
        }


    }

    @Override
    public void sendHeartbeat() {
        LOGGER.info(String.format("Send Heartbeat : %s ", serverName));
        sendHeartbeatRPC();
        synchronized (lock) {
            heartbeatTask = new HeartbeatTask(this);
            heartbeatTimer.schedule(heartbeatTask, Configs.HEARTBEAT_TIMEOUT);
        }
    }

    @Override
    public synchronized void startElection() {
        LOGGER.info(String.format("Election timer timed out for %s", serverName));
        nodeState = NodeState.CANDIDATE;
        currentTerm++;
        votedFor = serverName;
        updatePersistenceInfo(currentTerm, votedFor);
        voteCount = 1;
        LOGGER.info(String.format("%s became candidate. Vote count : %d, Current Term : %s",
                serverName, voteCount, currentTerm));
        sendVoteRequestRPC();
        scheduleElection();
    }

    @Override
    public void onVoteReceived(Message message) {
        BaseMessage baseMessage = (BaseMessage) message;
        String senderName = baseMessage.getSenderName();

        if (nodeState == NodeState.LEADER || nodeState == NodeState.FOLLOWER) {
            LOGGER.info(String.format("Current node state is %s so ignoring vote from %s", nodeState, senderName));
            return;
        }

        long requestTerm = baseMessage.getTerm();

        if (requestTerm < currentTerm) {
            LOGGER.info(String.format("Request term is less than current. reqTerm : %d, curTerm: %d. " +
                            "So ignoring vote from %s",
                    requestTerm, currentTerm, senderName));
            return;
        }

        voteCount++;
        LOGGER.info(String.format("Used the vote from %s, Vote Count : %d", baseMessage.getSenderName(), voteCount));

        if (voteCount >= Configs.MAJORITY_COUNT) {
            nodeState = NodeState.LEADER;
            leaderNode = serverName;
            LOGGER.info(String.format("I(%s) became Leader. Vote count : %d, Current Term : %d",
                    serverName, voteCount, currentTerm));
            voteCount = 0;
            LOGGER.info(String.format("Stopping election timer since %s became leader", serverName));
            electionTimer.cancel();
            sendHeartbeat();
        }
    }

    @Override
    public void onClientRequest(Message message) {
        if (nodeState != NodeState.LEADER) {
            sendLeaderInfo();
            return;
        }

        store(message);




    }

    public void applyLogToStateMachine(Message message) {
        if (commitIndex > lastApplied) {
            // execute apply
            //log[lastApplied] to state machine
        }
    }

    public boolean checkForDowngrade(Message message) {
        long requestTerm = message.getTerm();

        if (requestTerm > currentTerm && nodeState == NodeState.LEADER) {
            LOGGER.info("Downgrade to follower");
            convertToFollower();
            currentTerm = requestTerm;
            updatePersistenceInfo(currentTerm, null);
            return true;
        }

        return false;
    }

    public void init() {

        List<RaftPersistenceInfo> raftPersistenceInfoList = raftPersistenceService.getAllInfo();

        if(raftPersistenceInfoList.size() == 0){
            currentTerm = 0;
            votedFor = null;
        } else {
            currentTerm = raftPersistenceInfoList.get(0).getTerm();
            votedFor = raftPersistenceInfoList.get(0).getVotedFor();
        }
        LOGGER.info(String.valueOf(currentTerm));
        LOGGER.info(votedFor);

        for (int i = 0; i < nextIndex.length; i++) {
            nextIndex[i] = logReplicationService.getLastLogIndex() + 1;
            matchIndex[i] = 0L;
        }

        receiverTaskManager.execute();
        msgProcessorTaskManager.execute();
        scheduleElection();
    }

    @Override
    public void processMessage(Message message) {

        switch (message.getRequestType()) {
            case SHUTDOWN:
                LOGGER.info("Shutdown");
                shutdown();
                break;
            case CONVERT_FOLLOWER:
                LOGGER.info("Convert Follower");
                convertToFollower();
                break;
            case LEADER_INFO:
                LOGGER.info("Leader info");
                LOGGER.info(leaderNode);
                sendLeaderInfo();
                break;
            case STORE:
                LOGGER.info("Store:" + message);
                onClientRequest(message);
                break;
            case RETRIEVE:
                retrieve();
                break;
            case TIMEOUT:
                resetElectionTimer();
                startElection();
                break;
            case APPEND_RPC:
                if (checkForDowngrade(message))
                    return;
                onAppendRPCReceived(message);
                break;
            case VOTE_ACK:
                if (checkForDowngrade(message))
                    return;
                onVoteReceived(message);
                break;
            case VOTE_REQUEST:
                if (checkForDowngrade(message))
                    return;
                onVotingRequestReceived(message);
                break;
        }
    }

    public void shutdown() {
        receiverTaskManager.cancel();
        msgProcessorTaskManager.cancel();

        if (electionTask != null)
            electionTask.cancel();

        if (heartbeatTask != null)
            heartbeatTask.cancel();

        electionTimer.cancel();
        heartbeatTimer.cancel();
    }

    public void sendHeartbeatRPC() {
        BaseMessage message = new BaseMessage();
        message.setRequestType(RequestType.APPEND_RPC);
        message.setSenderName(serverName);
        message.setTerm(currentTerm);
        try {
            sender.multicast(gson.toJson(message));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void sendVoteResponseACK(String candidateName) {
        BaseMessage message = new BaseMessage();
        message.setSenderName(serverName);
        message.setRequestType(RequestType.VOTE_ACK);
        message.setTerm(currentTerm);
        try {
            sender.uniCast(candidateName, gson.toJson(message));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void sendAppendEntriesMessage() {

        Long lastLogId = logReplicationService.getLogCount();
        AppendEntriesMessage appendEntriesMessage = new AppendEntriesMessage();
        appendEntriesMessage.setSenderName(serverName);
        appendEntriesMessage.setTerm(currentTerm);
        appendEntriesMessage.setLeaderNode(serverName);
        appendEntriesMessage.setPrevLogIndex(logReplicationService.getLastLogIndex());
        appendEntriesMessage.setPrevLogTerm(logReplicationService.getLastLogTerm());
        appendEntriesMessage.setLeaderCommit(commitIndex);
        appendEntriesMessage.setEntries(gson.toJson(logReplicationService.getLogByIndex(lastLogId)));

        try {
            sender.multicast(gson.toJson(appendEntriesMessage));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    public void sendLeaderInfo() {
        BaseMessage message = new BaseMessage();
        message.setSenderName(serverName);
        message.setRequestType(RequestType.LEADER_INFO);
        message.setTerm(currentTerm);
        message.setKey("LEADER");
        message.setValue(leaderNode);
        try {
            sender.uniCast("Controller", gson.toJson(message));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void sendVoteRequestRPC() {
        BaseMessage message = new BaseMessage();
        message.setSenderName(serverName);
        message.setTerm(currentTerm);
        message.setRequestType(RequestType.VOTE_REQUEST);
        try {
            sender.multicast(gson.toJson(message));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void  updatePersistenceInfo(long term, String votedFor) {
        RaftPersistenceInfo raftPersistenceInfo = new RaftPersistenceInfo();
        raftPersistenceInfo.setId(1);
        raftPersistenceInfo.setTerm(term);
        raftPersistenceInfo.setVotedFor(votedFor);
        raftPersistenceService.update(raftPersistenceInfo);
    }

    public void convertToFollower() {

        if (nodeState == NodeState.FOLLOWER)
            return;

        nodeState = NodeState.FOLLOWER;
        synchronized (lock) {
            heartbeatTask.cancel();
        }
        scheduleElection();
    }

    public void store(Message message) {
        // Store in database
    }

    public void retrieve() {
        if (nodeState != NodeState.LEADER) {
            sendLeaderInfo();
            return;
        }

        // Retrieve
    }


}
