package com.example.project.service;

/**
 * @author revanth on 4/21/22
 */
public interface ILogReplicationService {

    long getLastLogIndex();

    long getLastLogTerm();
}
