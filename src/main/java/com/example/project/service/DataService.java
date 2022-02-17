package com.example.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.project.dao.DataDao;
import com.example.project.entity.Data;
import com.example.project.models.DataModel;
import com.example.project.service.Impl.IDataService;

import java.util.LinkedList;
import java.util.List;


@Service
public class DataService implements IDataService {

    @Autowired
    private DataDao dataDao;

    @Override
    public List<Data> getAllUsers() {
        List<Data> list = new LinkedList<>();
        Data d =new Data();
        d.setId(1);
        d.setFirstName("Revanth");
        d.setLastName("K");
        list.add(d);
        return list;
    }

    @Override
    public DataModel getUser(DataModel loginModel) {
        DataModel model = new DataModel();
        Data user = dataDao.findUserLoginByFirstName(loginModel.getFirstName());
        if (user != null) {
//            model.setEmail(user.getEmail());
            model.setFirstName(user.getFirstName());
            model.setId(user.getId());
            model.setLastName(user.getLastName());
//            model.setMobile(user.getPassword());
//            model.setPassword(user.getPassword());
//            model.setUserName(user.getUserName());
        }
        return model;
    }
}

