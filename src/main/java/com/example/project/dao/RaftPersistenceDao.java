package com.example.project.dao;

import java.util.List;

import com.example.project.entity.RaftPersistenceInfo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RaftPersistenceDao extends JpaRepository<RaftPersistenceInfo, Long> {
    List<RaftPersistenceInfo> findAll();

    RaftPersistenceInfo getById(Long id);
}
