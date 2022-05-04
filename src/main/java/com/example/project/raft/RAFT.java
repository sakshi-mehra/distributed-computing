package com.example.project.raft;

import com.example.project.raft.communication.Message;
import com.example.project.raft.model.AppendEntriesMessage;
import com.example.project.raft.model.AppendEntriesResponse;
import com.example.project.raft.model.RequestVoteMessage;

/**
 * @author revanth on 4/7/22
 */
public interface RAFT {

    void onAppendEntriesReceived(AppendEntriesMessage appendEntriesMessage);

    void onVotingRequestReceived(RequestVoteMessage requestVoteMessage);

    void onVoteReceived(Message message);

    void onClientRequest(Message message);

    void onAppendEntriesResponseReceived(AppendEntriesResponse appendEntriesResponse);
}
