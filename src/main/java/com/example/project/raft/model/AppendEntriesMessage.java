package com.example.project.raft.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author revanth on 4/21/22
 */
@Setter
@Getter
public class AppendEntriesMessage extends BaseMessage {

    String leaderNode;

    long prevLogIndex;

    long prevLogTerm;

    long leaderCommit;

    String entries;
}
