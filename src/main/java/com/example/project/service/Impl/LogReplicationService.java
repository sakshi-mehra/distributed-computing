package com.example.project.service.Impl;

import com.example.project.dao.LogDao;
import com.example.project.entity.Log;
import com.example.project.service.ILogReplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author revanth on 4/21/22
 */
@Service
public class LogReplicationService implements ILogReplicationService {

    @Autowired
    private LogDao logDao;

    @Override
    public Long getLastLogIndex() {
        if (logDao.count() == 0)
            return 0L;

        return logDao.findTopByOrderByLogIdDesc().getLogId();
    }

    @Override
    public Long getLastLogTerm() {

        if (logDao.count() == 0)
            return 0L;

        return logDao.findTopByOrderByLogIdDesc().getTerm();
    }

    @Override
    public Long getLogCount() {
        return logDao.count();
    }

    @Override
    public void save(Log log) {
        logDao.save(log);
    }

    @Override
    public List<Log> getAllLogs() {
        return logDao.findAll();
    }

    @Override
    public Log getLogByIndex(Long id) {
        return logDao.getById(id);
    }

    @Override
    public Long getLogTermByIndex(Long id) {

        if (id == 0)
            return 0L;

        return getLogByIndex(id).getTerm();
    }

    @Override
    public List<Log> getAllLogsGreaterThanEqual(long logId) {
        return logDao.findByLogIdGreaterThanEqual(logId);
    }

    @Override
    public void saveAll(List<Log> logs) {
        logDao.saveAll(logs);
    }
}
