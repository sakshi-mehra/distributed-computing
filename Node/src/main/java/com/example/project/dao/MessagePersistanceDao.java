package com.example.project.dao;

import java.util.List;

import com.example.project.entity.StoreMessage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RaftPersistenceDao extends JpaRepository<RaftPersistenceInfo, Long> {

    StoreMessage getById(Long id);
}