package com.example.project.raft;

import com.example.project.entity.Log;
import com.example.project.entity.RaftPersistenceInfo;
import com.example.project.entity.User;
import com.example.project.raft.communication.Configs;
import com.example.project.raft.communication.Message;
import com.example.project.raft.communication.Sender;
import com.example.project.raft.model.*;
import com.example.project.raft.server.RaftContext;
import com.example.project.raft.server.RaftElection;
import com.example.project.raft.server.ServerState;
import com.example.project.raft.tasks.*;
import com.example.project.service.Impl.LogReplicationService;
import com.example.project.service.Impl.RaftPersistenceService;
import com.example.project.service.Impl.UserService;
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

    @Autowired
    UserService userService;

    // TODO: There are cases to handle for message queue. It is not thread safe and not size bounded.
    private final Queue<Message> messageQueue;
    private final TaskManger msgProcessorTaskManager;
    private final TaskManger receiverTaskManager;
    private Timer electionTimer;
    private final Timer heartbeatTimer;
    private ElectionTask electionTask;
    private HeartbeatTask heartbeatTask;

    private final Sender sender;
    private final Sender sender2;
    private final Gson gson;

    private final RaftElection raftElection;
    private final RaftContext raftContext;
    private final ServerState serverState;
    private final Object lock = new Object();

    public RaftImpl() throws IOException {
        LOGGER.info("Creating RAFT instance");

        messageQueue = new LinkedList<>();
        Task msgProcessorTask = new MessageProcessorTask(messageQueue, this);
        Task msgReceiverTask = new MessageReceiverTask(messageQueue, Configs.GROUP_NAME, Configs.PORT);
        msgProcessorTaskManager = new TaskManger(msgProcessorTask, "MsgProcessorTask");
        receiverTaskManager = new TaskManger(msgReceiverTask, "MsgReceiverTask");
        electionTimer = new Timer("Election Timer 1");
        heartbeatTimer = new Timer("Heartbeat timer");

        serverState = new ServerState(Utils.getHostName(), Configs.NODE_COUNT);
        raftContext = new RaftContext(this, messageQueue, msgProcessorTaskManager,
                receiverTaskManager, electionTimer, heartbeatTimer, electionTask, heartbeatTask, serverState);
        raftElection = new RaftElection(raftContext);
        sender = new Sender(Configs.GROUP_NAME, Configs.PORT);
        sender2 = new Sender(Configs.GROUP_NAME2, Configs.PORT1);

        gson = new GsonBuilder().setExclusionStrategies(new AnnotationExclusionStrategy()).create();
    }

    public synchronized void scheduleElection() {
        long electionTimeout = Utils.getRandom(Configs.ELECTION_TIMEOUT_MIN, Configs.ELECTION_TIMEOUT_MAX);
        electionTask = new ElectionTask(electionTimeout, this);
        try {
            electionTimer.schedule(electionTask, electionTask.getTimeout());
        } catch (IllegalStateException e) {
            electionTimer = new Timer("Election timer 2");
            electionTimer.schedule(electionTask, electionTask.getTimeout());
        }

        LOGGER.info(String.format("Election scheduled by %s in next %d ms", serverState.getServerName(),
                electionTimeout));
    }

    public synchronized void resetElectionTimer() {
        LOGGER.info(String.format("Resetting election timeout by %s set for %d ms", serverState.getServerName(),
                electionTask.getTimeout()));
        synchronized (lock) {
            if (electionTask != null)
                electionTask.cancel();
        }

        scheduleElection();
    }

    @Override
    public void onVotingRequestReceived(RequestVoteMessage requestVoteMessage) {
        String candidateName = requestVoteMessage.getSenderName();
        long requestTerm = requestVoteMessage.getTerm();
        String senderName = requestVoteMessage.getSenderName();

        // Ignore self request
        if (senderName.equals(serverState.getServerName()))
            return;

        // Ignore if our term is greater than candidate term
        if (requestTerm < serverState.getCurrentTerm())
            return;

        long myLastLogIndex = logReplicationService.getLastLogIndex();
        long myLastLogTerm = logReplicationService.getLastLogTerm();
        long requestLastLogIndex = requestVoteMessage.getLastLogIndex();
        long requestLastLogTerm = requestVoteMessage.getLastLogTerm();

        // Check whose log is up-to-date
        if (myLastLogTerm == requestLastLogTerm && requestLastLogIndex < myLastLogIndex) {
            LOGGER.info("Logger1 not up to date");
            return;
        } else if (myLastLogTerm > requestLastLogTerm) {
            LOGGER.info("Logger2 not up to date" + myLastLogTerm + " " + requestLastLogTerm);
            return;
        }

        if (serverState.getCurrentTerm() < requestTerm) {
            LOGGER.info("Voted for " + candidateName + " for term : " + requestTerm);
            serverState.setCurrentTerm(requestTerm);
            serverState.setVotedFor(candidateName);
            updatePersistenceInfo(serverState.getCurrentTerm(), serverState.getVotedFor(),
                    serverState.getLastApplied());
            sendVoteResponseACK(candidateName);
        }
    }

    public void checkAndAppendLogs(List<Log> logs, AppendEntriesMessage appendEntriesMessage) {

        if (logs.size() != 0) {
            if (appendEntriesMessage.getPrevLogIndex() == logReplicationService.getLastLogIndex() &&
                appendEntriesMessage.getPrevLogTerm() == logReplicationService.getLastLogTerm()) {
                LOGGER.info("Need to write the logs into the database");
                logReplicationService.saveAll(logs);
            }
        }
    }

    @Override
    public synchronized void onAppendEntriesReceived(AppendEntriesMessage appendEntriesMessage) {
        String senderName = appendEntriesMessage.getSenderName();
        long requestTerm = appendEntriesMessage.getTerm();
        // Ignore self heartbeat
        if (senderName.equals(serverState.getServerName()))
            return;

        // Respond false if current term is greater than request term
        if (serverState.getCurrentTerm() > requestTerm) {
            sendAppendEntriesResponseMessage(senderName, false);
            return;
        }

        if (appendEntriesMessage.getPrevLogIndex() > logReplicationService.getLastLogIndex()) {
            sendAppendEntriesResponseMessage(senderName, false);
            return;
        }

        // If the term does not match for leader's previous log index and our previous log index
        if (appendEntriesMessage.getPrevLogTerm() !=
                logReplicationService.getLogTermByIndex(appendEntriesMessage.getPrevLogIndex())) {
            serverState.setCurrentTerm(requestTerm);
            sendAppendEntriesResponseMessage(senderName, false);
            return;
        }

        String entries = appendEntriesMessage.getEntries();

        List<Log> logs = gson.fromJson(entries,  new TypeToken<List<Log>>(){}.getType());

        LOGGER.info(String.format("Append RPC obtained from %s for term %s and my term %d and state : %s, " +
                        "commit Index : %d, Last Log index : %d",
                senderName,
                requestTerm, serverState.getCurrentTerm(), serverState.getNodeState(), serverState.getCommitIndex()
                        , logReplicationService.getLastLogIndex())
                );

        if (serverState.getNodeState() == NodeState.FOLLOWER) {
            resetElectionTimer();
            serverState.setLeaderNode(appendEntriesMessage.getSenderName());
            checkAndAppendLogs(logs, appendEntriesMessage);
        } else if (serverState.getNodeState() == NodeState.CANDIDATE) {

            // Received a heart beat from leader and his term is at least my term then withdraw
            if (requestTerm >= serverState.getCurrentTerm()) {
                serverState.setNodeState(NodeState.FOLLOWER);
                serverState.setLeaderNode(appendEntriesMessage.getSenderName());
                resetElectionTimer();
                checkAndAppendLogs(logs, appendEntriesMessage);
            }
        } else {
            // If leader

            if (requestTerm == serverState.getCurrentTerm()) {
                LOGGER.info("**********Cannot have 2 leaders for the same term, so downgrade");
                convertToFollower();
            }
        }

        serverState.setCommitIndex(Math.min(appendEntriesMessage.getLeaderCommit(), logReplicationService.getLastLogIndex()));
        applyLogToStateMachine();
        sendAppendEntriesResponseMessage(senderName, true);
    }

    @Override
    public void sendHeartbeat() {
        LOGGER.info(String.format("Send Heartbeat : %s ", serverState.getServerName()));
        sendAppendEntriesMessage();
        synchronized (lock) {
            heartbeatTask = new HeartbeatTask(this);
            heartbeatTimer.schedule(heartbeatTask, Configs.HEARTBEAT_TIMEOUT);
        }
    }

    @Override
    public synchronized void startElection() {
        LOGGER.info(String.format("Election timer timed out for %s", serverState.getServerName()));
        serverState.setNodeState(NodeState.CANDIDATE);
        serverState.incrementCurrTerm();
        serverState.setVotedFor(serverState.getServerName());
        updatePersistenceInfo(serverState.getCurrentTerm(), serverState.getVotedFor(), serverState.getLastApplied());
        serverState.setVoteCount(1);
        LOGGER.info(String.format("%s became candidate. Vote count : %d, Current Term : %s",
                serverState.getServerName(), serverState.getVoteCount(), serverState.getCurrentTerm()));
        sendVoteRequestRPC();
        scheduleElection();
    }

    @Override
    public void onVoteReceived(Message message) {
        BaseMessage baseMessage = (BaseMessage) message;
        String senderName = baseMessage.getSenderName();

        if (serverState.getNodeState() == NodeState.LEADER || serverState.getNodeState() == NodeState.FOLLOWER) {
            LOGGER.info(String.format("Current node state is %s so ignoring vote from %s",
                    serverState.getNodeState(), senderName));
            return;
        }

        long requestTerm = baseMessage.getTerm();

        if (requestTerm < serverState.getCurrentTerm()) {
            LOGGER.info(String.format("Request term is less than current. reqTerm : %d, curTerm: %d. " +
                            "So ignoring vote from %s",
                    requestTerm, serverState.getCurrentTerm(), senderName));
            return;
        }

        serverState.incrementVoteCount();

        LOGGER.info(String.format("Used the vote from %s, Vote Count : %d", baseMessage.getSenderName(),
                serverState.getVoteCount()));

        if (serverState.getVoteCount() >= Configs.MAJORITY_COUNT) {
            serverState.setNodeState(NodeState.LEADER);
            serverState.setLeaderNode(serverState.getServerName());
            LOGGER.info(String.format("I(%s) became Leader. Vote count : %d, Current Term : %d",
                    serverState.getServerName(), serverState.getVoteCount(), serverState.getCurrentTerm()));

            serverState.resetVoteCount();
            LOGGER.info(String.format("Stopping election timer since %s became leader", serverState.getServerName()));
            electionTimer.cancel();
            sendHeartbeat();
            for (int i = 0; i < Configs.NODE_COUNT; i++) {
                serverState.setNextIndex(i, logReplicationService.getLastLogIndex() + 1);
                serverState.setMatchIndex(i, 0L);
            }
        }
    }

    @Override
    public void onClientRequest(Message message) {
        if (serverState.getNodeState() != NodeState.LEADER) {
            sendLeaderInfo(message);
            return;
        }
        store(message);
        sendAppendEntriesMessage();
    }

    @Override
    public void onAppendEntriesResponseReceived(AppendEntriesResponse appendEntriesResponse) {

        String senderName = appendEntriesResponse.getSenderName();
        long responseTerm = appendEntriesResponse.getTerm();
        if (responseTerm > serverState.getCurrentTerm()) {
            serverState.setCurrentTerm(responseTerm);
            convertToFollower();
            updatePersistenceInfo(responseTerm, null, serverState.getLastApplied());
            return;
        }

        Integer senderId = Integer.parseInt(String.valueOf(senderName.charAt(senderName.length() - 1))) - 1;

        if (!appendEntriesResponse.isSuccess()) {
            if (serverState.getNextIndex()[senderId] > 1)
                serverState.decrementNextIndex(senderId);
        } else {
            serverState.setMatchIndex(senderId, appendEntriesResponse.getMatchIndex());
            serverState.setNextIndex(senderId, serverState.getMatchIndex()[senderId] + 1);
            for (Long i = logReplicationService.getLastLogIndex(); i > serverState.getCommitIndex(); i--) {
                int count = 0;
                for (int j = 0; j < Configs.NODE_COUNT; j++) {
                    if ((serverState.getMatchIndex()[j]).equals(i)) {
                        count++;
                    }
                }

                if (count >= Configs.MAJORITY_COUNT) {
                    serverState.setCommitIndex(i);
                    applyLogToStateMachine();
                    break;
                }
            }
        }
    }

    public void applyLogToStateMachine() {
        if (serverState.getCommitIndex() > serverState.getLastApplied()) {
            LOGGER.info("Applying to state machine" + serverState.getLastApplied());

            Log log = logReplicationService.getLogByIndex(serverState.getLastApplied() + 1);
            LOGGER.info("Applying log " + log.toString());

            if (log.getEntryKey().equals("UM")) {
                LOGGER.info("User manager query");
                User user = gson.fromJson(log.getEntryValue(), User.class);
                userService.addUser(user);
            }
            serverState.setLastApplied(serverState.getLastApplied() + 1);
            updatePersistenceInfo(serverState.getCurrentTerm(), serverState.getVotedFor(),
                    serverState.getLastApplied() + 1);
        }
    }

    public boolean checkForDowngrade(Message message) {
        long requestTerm = message.getTerm();

        if (requestTerm > serverState.getCurrentTerm() && serverState.getNodeState() == NodeState.LEADER) {
            LOGGER.info("Downgrade to follower");
            convertToFollower();
            serverState.setCurrentTerm(requestTerm);
            updatePersistenceInfo(requestTerm, null, serverState.getLastApplied());
            return true;
        }

        return false;
    }

    public void init() {

        List<RaftPersistenceInfo> raftPersistenceInfoList = raftPersistenceService.getAllInfo();

        if(raftPersistenceInfoList.size() == 0){
            serverState.setCurrentTerm(0);
            serverState.setVotedFor(null);
            serverState.setLastApplied(0);
        } else {
            serverState.setCurrentTerm(raftPersistenceInfoList.get(0).getTerm());
            serverState.setVotedFor(raftPersistenceInfoList.get(0).getVotedFor());
            serverState.setLastApplied(raftPersistenceInfoList.get(0).getLastApplied());
        }
        LOGGER.info(String.valueOf(serverState.getCurrentTerm()));
        LOGGER.info(serverState.getVotedFor());
        LOGGER.info("Last Applied :" + serverState.getLastApplied());

        for (int i = 0; i < Configs.NODE_COUNT; i++) {
            serverState.setNextIndex(i, logReplicationService.getLastLogIndex() + 1);
            serverState.setMatchIndex(i, 0L);
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
                sendLeaderInfo(message);
                break;
            case STORE:
                LOGGER.info("Store:" + message);
                onClientRequest(message);
                break;
            case RETRIEVE:
                retrieve(message);
                break;
            case TIMEOUT:
                resetElectionTimer();
                startElection();
                break;
            case APPEND_RPC:
                if (checkForDowngrade(message))
                    return;
                onAppendEntriesReceived((AppendEntriesMessage) message);
                break;
            case APPEND_RPC_RESPONSE:
                if (checkForDowngrade(message))
                    return;
                onAppendEntriesResponseReceived((AppendEntriesResponse) message);
            case VOTE_ACK:
                if (checkForDowngrade(message))
                    return;
                onVoteReceived(message);
                break;
            case VOTE_REQUEST:
                if (checkForDowngrade(message))
                    return;
                onVotingRequestReceived((RequestVoteMessage) message);
                break;
        }
    }

    public synchronized void shutdown() {
        receiverTaskManager.cancel();
        msgProcessorTaskManager.cancel();

        if (electionTask != null)
            electionTask.cancel();

        if (heartbeatTask != null)
            heartbeatTask.cancel();

        electionTimer.cancel();
        heartbeatTimer.cancel();
    }

    public void sendVoteResponseACK(String candidateName) {
        BaseMessage message = new BaseMessage();
        message.setSenderName(serverState.getServerName());
        message.setRequestType(RequestType.VOTE_ACK);
        message.setTerm(serverState.getCurrentTerm());
        try {
            sender.uniCast(candidateName, gson.toJson(message));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void sendAppendEntriesMessage() {

        for (int i = 0; i < Configs.NODE_COUNT; i++) {

            if (i == serverState.getServerId())
                continue;

            Long nextIndex = serverState.getNextIndex(i);

            if (nextIndex < 1) {
                nextIndex = 1L;
            }

            AppendEntriesMessage appendEntriesMessage = new AppendEntriesMessage();
            appendEntriesMessage.setSenderName(serverState.getServerName());
            appendEntriesMessage.setTerm(serverState.getCurrentTerm());
            appendEntriesMessage.setLeaderNode(serverState.getServerName());
            appendEntriesMessage.setPrevLogIndex(nextIndex - 1);
            appendEntriesMessage.setPrevLogTerm(logReplicationService.getLogTermByIndex(nextIndex - 1));
            appendEntriesMessage.setLeaderCommit(serverState.getCommitIndex());
            appendEntriesMessage.setRequestType(RequestType.APPEND_RPC);

            List<Log> logs = logReplicationService.getAllLogsGreaterThanEqual(nextIndex);

            if (logs.size() > 1) {
                logs.subList(1, logs.size()).clear();
            }

            appendEntriesMessage.setEntries(
                    gson.toJsonTree(logs, new TypeToken<List<Log>>(){}.getType()).getAsJsonArray().toString());

            try {
                String dest = "Node" + (i + 1);
                sender.uniCast(dest, gson.toJson(appendEntriesMessage));
            } catch (IOException e) {
                LOGGER.error("Failed to send Append RPC to Node" + (i +1));
            }
        }
    }

    public void sendAppendEntriesResponseMessage(String destServerName, boolean isSuccess) {
        AppendEntriesResponse appendEntriesResponse = new AppendEntriesResponse();
        appendEntriesResponse.setSenderName(serverState.getServerName());
        appendEntriesResponse.setRequestType(RequestType.APPEND_RPC_RESPONSE);
        appendEntriesResponse.setSuccess(isSuccess);
        appendEntriesResponse.setTerm(serverState.getCurrentTerm());

        if (isSuccess) {
            appendEntriesResponse.setMatchIndex(logReplicationService.getLastLogIndex());
        } else {
            appendEntriesResponse.setMatchIndex(0L);
        }

        try {
            sender.uniCast(destServerName, gson.toJson(appendEntriesResponse));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void sendLeaderInfo(Message requestMessage) {
        BaseMessage message = new BaseMessage();
        message.setSenderName(serverState.getServerName());
        message.setRequestType(RequestType.LEADER_INFO);
        message.setTerm(serverState.getCurrentTerm());
        message.setKey("LEADER");
        message.setValue(serverState.getLeaderNode());

        if (requestMessage.getSenderName().equals("Controller")) {
            try {
                sender.uniCast("Controller", gson.toJson(message));
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } else {
            LOGGER.info("Sender Name:" + requestMessage.getSenderName() + "Json :" + gson.toJson(message));
            try {
                sender2.uniCast(requestMessage.getSenderName(), gson.toJson(message));
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public void sendVoteRequestRPC() {
        RequestVoteMessage message = new RequestVoteMessage();
        message.setSenderName(serverState.getServerName());
        message.setTerm(serverState.getCurrentTerm());
        message.setRequestType(RequestType.VOTE_REQUEST);
        message.setLastLogIndex(logReplicationService.getLastLogIndex());
        message.setLastLogTerm(logReplicationService.getLastLogTerm());

        try {
            sender.multicast(gson.toJson(message));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void  updatePersistenceInfo(long term, String votedFor, long lastApplied) {
        RaftPersistenceInfo raftPersistenceInfo = new RaftPersistenceInfo();
        raftPersistenceInfo.setId(1);
        raftPersistenceInfo.setTerm(term);
        raftPersistenceInfo.setVotedFor(votedFor);
        raftPersistenceInfo.setLastApplied(lastApplied);
        raftPersistenceService.update(raftPersistenceInfo);
    }

    public void convertToFollower() {

        if (serverState.getNodeState() == NodeState.FOLLOWER)
            return;

        serverState.setNodeState(NodeState.FOLLOWER);
        synchronized (lock) {
            if (heartbeatTask != null)
                heartbeatTask.cancel();
        }
        scheduleElection();
    }

    public void store(Message message) {
        LOGGER.info("Store");
        Log log = new Log();
        BaseMessage baseMessage = (BaseMessage) message;
        log.setTerm(serverState.getCurrentTerm());
        log.setEntryKey(baseMessage.getKey());
        log.setEntryValue(baseMessage.getValue());
        logReplicationService.save(log);
    }

    public void retrieve(Message message) {
        if (serverState.getNodeState() != NodeState.LEADER) {
            sendLeaderInfo(message);
            return;
        }

        List<Log> logs = logReplicationService.getAllLogs();
        JsonArray jsonLogsArray = gson.toJsonTree(logs, new TypeToken<List<Log>>(){}.getType()).getAsJsonArray();
        BaseMessage baseMessage = new BaseMessage();
        baseMessage.setSenderName(serverState.getServerName());
        baseMessage.setRequestType(RequestType.RETRIEVE);
        baseMessage.setKey("COMMITED_LOGS");
        baseMessage.setValue(jsonLogsArray.toString());
        LOGGER.error(jsonLogsArray.toString());
        LOGGER.error(gson.toJson(baseMessage));
        try {
            sender.uniCast("Controller", gson.toJson(baseMessage));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
