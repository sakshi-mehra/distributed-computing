package com.example.project.service;
import com.example.project.entity.StoreMessage;

public interface IMessagePersistenceService {

    void update(StoreMessage msg);
}