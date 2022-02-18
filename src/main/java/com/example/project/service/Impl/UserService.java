package com.example.project.service.Impl;

import com.example.project.entity.User;
import com.example.project.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.project.dao.UserDao;

import java.util.List;


@Service
public class UserService implements IUserService {

    @Autowired
    private UserDao userDao;

    @Override
    public List<User> getAllUsers() {
        List<User> list = userDao.findAll();
        return list;
    }

    @Override
    public void addUser(User data) {
        User user = userDao.save(data);
    }
}

