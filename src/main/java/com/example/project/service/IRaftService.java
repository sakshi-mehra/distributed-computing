package com.example.project.service;

import com.example.project.entity.Raft;

import java.util.List;

public interface IRaftService {

    public List<Raft> getAllUsers();

    public void metricUpdate(Raft raft);
}
