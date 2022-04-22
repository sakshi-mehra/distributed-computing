package com.example.project.service.Impl;

import com.example.project.dao.LogDao;
import com.example.project.entity.Log;
import com.example.project.service.ILogReplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author revanth on 4/21/22
 */
@Service
public class LogReplicationService implements ILogReplicationService {

    @Autowired
    private LogDao logDao;

    @Override
    public long getLastLogIndex() {
        return logDao.count();
    }

    @Override
    public long getLastLogTerm() {

        if (logDao.count() == 0)
            return logDao.count();

        Log log = logDao.getById(logDao.count());
        return log.getTerm();
    }
}
