package com.example.project.dao;

import org.springframework.data.repository.CrudRepository;

import com.example.project.entity.Data;

public interface DataDao extends CrudRepository<Data, Long> {

    Data findUserLoginByFirstName(String username);
}
