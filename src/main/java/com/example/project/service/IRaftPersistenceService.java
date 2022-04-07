package com.example.project.service;

import com.example.project.entity.RaftPersistenceInfo;

import java.util.List;

public interface IRaftPersistenceService {

    List<RaftPersistenceInfo> getAllInfo();

    RaftPersistenceInfo getDataById(Long id);

    void update(RaftPersistenceInfo raft);
}
