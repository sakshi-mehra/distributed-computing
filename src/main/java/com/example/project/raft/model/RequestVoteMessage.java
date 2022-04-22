package com.example.project.raft.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author revanth on 4/21/22
 */
@Getter
@Setter
public class RequestVoteMessage extends BaseMessage {

    long lastLogIndex;

    long lastLogTerm;
}
