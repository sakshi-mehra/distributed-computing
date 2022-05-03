package com.example.project.raft;

import com.example.project.raft.communication.Message;

/**
 * @author revanth on 4/7/22
 */
public interface RAFT {

    void onAppendRPCReceived(Message message);

    void onVotingRequestReceived(Message message);

    void onVoteReceived(Message message);

    void onClientRequest(Message message);
}
