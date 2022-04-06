package com.example.project.service.Impl;

import com.example.project.entity.Raft;
import com.example.project.service.IRaftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.project.dao.RaftDao;

import java.util.List;


@Service
public class RaftService implements IRaftService {

    @Autowired
    private RaftDao raftDao;

    @Override
    public List<Raft> getAllUsers() {
        List<Raft> list = raftDao.findAll();
        return list;
    }

    @Override
    public void metricUpdate(Raft raft) {
        Raft raftmetric = raftDao.save(raft);
    }
}

