package com.example.project.service;

import com.example.project.entity.User;

import java.util.List;

public interface IUserService {

    List<User> getAllUsers();

    void addUser(User user);
}
