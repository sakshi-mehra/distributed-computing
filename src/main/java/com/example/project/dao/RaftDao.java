package com.example.project.dao;

import java.util.List;

import com.example.project.entity.Raft;

import org.springframework.data.repository.CrudRepository;

public interface RaftDao extends CrudRepository<Raft, Long> {
    List<Raft> findAll();
}
