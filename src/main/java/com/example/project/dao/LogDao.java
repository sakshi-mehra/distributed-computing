package com.example.project.dao;

import com.example.project.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author revanth on 4/21/22
 */
public interface LogDao extends JpaRepository<Log, Long> {

    @Override
    Log getById(Long id);

    long getLastLogTerm();
}
