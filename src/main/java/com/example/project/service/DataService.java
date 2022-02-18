package com.example.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.project.dao.DataDao;
import com.example.project.entity.Data;
import com.example.project.models.DataModel;
import com.example.project.service.Impl.IDataService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


@Service
public class DataService implements IDataService {

    @Autowired
    private DataDao dataDao;

    @Override
    public List<Data> getAllUsers() {
        List<Data> list = dataDao.findAll();

//        Data d =new Data();
//        d.setId(1);
//        d.setFirstName("Revanth");
//        d.setLastName("K");
//        list.add(d);
        return list;
    }

    @Override
    public void addUser(Data loginModel) {
        Data user = dataDao.save(loginModel);
    }
}

