package com.example.project.raft.server;

import com.example.project.raft.RaftImpl;
import com.example.project.raft.communication.Message;
import com.example.project.raft.tasks.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Queue;
import java.util.Timer;

/**
 * @author revanth on 5/4/22
 */
@Getter
@Setter
public class RaftContext {

    private final RaftImpl raft;
    private final Queue<Message> messageQueue;
    private final TaskManger msgProcessorTaskManager;
    private final TaskManger receiverTaskManager;
    private Timer electionTimer;
    private final Timer heartbeatTimer;
    private ElectionTask electionTask;
    private HeartbeatTask heartbeatTask;
    private final ServerState serverState;
    private final Object lock = new Object();

    public RaftContext(RaftImpl raft, Queue<Message> messageQueue, TaskManger msgProcessorTaskManager, TaskManger receiverTaskManager, Timer electionTimer, Timer heartbeatTimer, ElectionTask electionTask, HeartbeatTask heartbeatTask, ServerState serverState) {
        this.raft = raft;
        this.messageQueue = messageQueue;
        this.msgProcessorTaskManager = msgProcessorTaskManager;
        this.receiverTaskManager = receiverTaskManager;
        this.electionTimer = electionTimer;
        this.heartbeatTimer = heartbeatTimer;
        this.electionTask = electionTask;
        this.heartbeatTask = heartbeatTask;
        this.serverState = serverState;
    }
}
