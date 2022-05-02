package com.example.project.service.Impl;
import com.example.project.service.IMessagePersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.project.dao.MessagePersistenceDao;

@Service
public class MessagePersistenceService implements IMessagePersistenceService {

    @Autowired
    private MessagePersistenceDao msgDao;

    @Override
    public void update(StoreMessage msgPersistence) {
        msgDao.save(msgPersistence);
    }
}