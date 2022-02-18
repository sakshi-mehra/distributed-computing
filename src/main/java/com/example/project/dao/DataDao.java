package com.example.project.dao;

import org.springframework.data.repository.CrudRepository;

import com.example.project.entity.Data;

import java.util.List;

public interface DataDao extends CrudRepository<Data, Long> {

    List<Data> findAll();
}

