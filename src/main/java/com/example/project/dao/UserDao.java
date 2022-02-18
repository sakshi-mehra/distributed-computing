package com.example.project.dao;

import com.example.project.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserDao extends CrudRepository<User, Long> {
    List<User> findAll();
}

