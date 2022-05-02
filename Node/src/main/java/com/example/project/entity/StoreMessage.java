package com.example.project.entity;

import javax.persistence.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Message")
public class StoreMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "senderName", nullable = true, length = 30)
    private String senderName;

    @Column(name = "request", nullable = true, length = 30)
    private String requestType;

    @Column(name = "term", nullable = true, length = 30)
    private long term;

    @Column(name = "key", nullable = true, length = 30)
    private String key;

    @Column(name = "value", nullable = true, length = 30)
    private String value;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getVal() {
        return value;
    }

    public void setVal(String val) {
        this.value = value;
    }
}
