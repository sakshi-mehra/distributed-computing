package com.example.project.service.Impl;

import com.example.project.entity.RaftPersistenceInfo;
import com.example.project.service.IRaftPersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.project.dao.RaftPersistenceDao;

import java.util.List;


@Service
public class RaftPersistenceService implements IRaftPersistenceService {

    @Autowired
    private RaftPersistenceDao raftDao;

    @Override
    public List<RaftPersistenceInfo> getAllInfo() {
        return raftDao.findAll();
    }

    @Override
    public RaftPersistenceInfo getDataById(Long id) {
        return raftDao.getById(id);
    }

    @Override
    public void update(RaftPersistenceInfo raftPersistence) {
        raftDao.save(raftPersistence);
    }
}

