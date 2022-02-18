package com.example.project.service;

import com.example.project.entity.User;

import java.util.List;

public interface IUserService {

    public List<User> getAllUsers();

    public void addUser(User user);
}
