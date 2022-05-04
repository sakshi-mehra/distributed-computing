package com.example.project.service;

import com.example.project.entity.Log;

import java.util.List;

/**
 * @author revanth on 4/21/22
 */
public interface ILogReplicationService {

    Long getLastLogIndex();

    Long getLastLogTerm();

    void save(Log log);

    void saveAll(List<Log> logs);

    List<Log> getAllLogs();

    Long getLogCount();

    Log getLogByIndex(Long id);

    Long getLogTermByIndex(Long id);

    List<Log> getAllLogsGreaterThanEqual(long logId);
}
