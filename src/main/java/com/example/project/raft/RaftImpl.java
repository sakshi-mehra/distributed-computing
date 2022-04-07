package com.example.project.raft;

import com.example.project.entity.RaftPersistenceInfo;
import com.example.project.raft.communication.Configs;
import com.example.project.raft.communication.Sender;
import com.example.project.raft.model.Message;
import com.example.project.raft.model.NodeState;
import com.example.project.raft.model.RequestType;
import com.example.project.raft.tasks.*;
import com.example.project.service.Impl.RaftPersistenceService;
import com.example.project.utils.Utils;
import com.google.gson.Gson;
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

    // TODO: There are cases to handle for message queue. It is not thread safe and not size bounded.
    private final Queue<Message> messageQueue;
    private final TaskManger msgProcessorTaskManager;
    private final TaskManger receiverTaskManager;
    private final Timer electionTimer;
    private final Timer heartbeatTimer;
    private ElectionTask electionTask;
    private HeartbeatTask heartbeatTask;

    private final Sender sender;
    private final Gson gson;

    private final String myName;
    private NodeState nodeState;
    private long currentTerm = 0;
    private String votedFor = null;
    private int voteCount;
    private String leaderNode = null;

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
        gson = new Gson();

        myName = Utils.getHostName();
        nodeState = NodeState.FOLLOWER;
        voteCount = 0;
    }

    public void scheduleElection() {
        long electionTimeout = Utils.getRandom(15000, 30000);
        electionTask = new ElectionTask(electionTimeout, this);
        electionTimer.schedule(electionTask, electionTask.getTimeout());
        LOGGER.info(String.format("Election scheduled by %s in %d ms", myName, electionTimeout));
    }

    public void resetElectionTimer() {
        LOGGER.info(String.format("Resetting election timeout by %s set for %d", myName, electionTask.getTimeout()));
        electionTask.cancel();
        scheduleElection();
    }

    @Override
    public void onVotingRequestReceived(String candidateName, Message message) {
        long requestTerm = message.getTerm();

        LOGGER.info("Vote requested by " + candidateName + " for term : " + requestTerm);

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

        long requestTerm = message.getTerm();

        if (nodeState == NodeState.FOLLOWER) {
            resetElectionTimer();
            leaderNode = message.getSenderName();
        } else if (nodeState == NodeState.CANDIDATE) {

            // Received a heart beat from leader
            if (requestTerm >= currentTerm) {
                nodeState = NodeState.FOLLOWER;
                leaderNode = message.getSenderName();
                resetElectionTimer();
            }
        } else {
            // If leader
        }
    }

    @Override
    public void sendHeartbeat() {
        LOGGER.info("Send Heartbeat");
        sendHeartbeatRPC();
        heartbeatTask = new HeartbeatTask(this);
        heartbeatTimer.schedule(heartbeatTask, Configs.HEARTBEAT_TIMEOUT);
    }

    @Override
    public void startElection() {
        nodeState = NodeState.CANDIDATE;
        currentTerm++;
        votedFor = myName;
        updatePersistenceInfo(currentTerm, votedFor);
        voteCount = 0;
        LOGGER.info(String.format("%s became candidate", myName));
        sendVoteRequestRPC();
        scheduleElection();
    }

    @Override
    public void onVoteReceived() {

        if (nodeState == NodeState.LEADER)
            return;

        voteCount++;

        if (voteCount >= Configs.MAJORITY_COUNT ) {
            nodeState = NodeState.LEADER;
            leaderNode = myName;
            voteCount = 0;
            LOGGER.info(myName + " Became Leader");
            LOGGER.info(String.format("Stopping election timer since %s became leader", myName));
            electionTimer.cancel();
            sendHeartbeat();
        }
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

        receiverTaskManager.execute();
        msgProcessorTaskManager.execute();
        scheduleElection();
    }

    @Override
    public void processMessage(Message message) {

        long requestTerm = message.getTerm();

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
                LOGGER.error(leaderNode);
                break;
            case TIMEOUT:
                resetElectionTimer();
                startElection();
                break;
            case APPEND_RPC:
                if (requestTerm > currentTerm && nodeState == NodeState.LEADER) {
                    LOGGER.info("Downgrade");
                    nodeState = NodeState.FOLLOWER;
                    heartbeatTimer.cancel();
                    currentTerm = requestTerm;
                    return;
                }
                onAppendRPCReceived(message);
                break;
            case VOTE_ACK:
                if (requestTerm > currentTerm && nodeState == NodeState.LEADER) {
                    LOGGER.info("Downgrade");
                    nodeState = NodeState.FOLLOWER;
                    currentTerm = requestTerm;
                    heartbeatTimer.cancel();
                    return;
                }
                onVoteReceived();
                break;
            case VOTE_REQUEST:
                if (requestTerm > currentTerm && nodeState == NodeState.LEADER) {
                    LOGGER.info("Downgrade");
                    nodeState = NodeState.FOLLOWER;
                    currentTerm = requestTerm;
                    heartbeatTimer.cancel();
                    return;
                }
                onVotingRequestReceived(message.getSenderName(), message);
                break;

        }
    }

    public void shutdown() {
        receiverTaskManager.cancel();
        msgProcessorTaskManager.cancel();
        electionTimer.cancel();
        heartbeatTimer.cancel();
    }

    public void sendHeartbeatRPC() {
        Message message = new Message();
        message.setRequestType(RequestType.APPEND_RPC);
        message.setSenderName(myName);
        message.setTerm(currentTerm);
        try {
            sender.multicast(gson.toJson(message));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void sendVoteResponseACK(String candidateName) {
        Message message = new Message();
        message.setSenderName(myName);
        message.setRequestType(RequestType.VOTE_ACK);
        message.setTerm(currentTerm);
        try {
            sender.uniCast(candidateName, gson.toJson(message));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void sendVoteRequestRPC() {
        Message message = new Message();
        message.setSenderName(myName);
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
        scheduleElection();
        heartbeatTimer.cancel();
    }
}
