package com.example.project.raft;

import com.example.project.raft.model.Message;

/**
 * @author revanth on 4/7/22
 */
public interface RAFT {

    void onAppendRPCReceived(Message message);

    void onVotingRequestReceived(String candidateName, Message message);

    void onVoteReceived(Message message);
}
