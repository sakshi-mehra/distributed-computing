package com.example.project.entity;

import javax.persistence.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Raft")
public class RaftPersistenceInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "currentTerm", nullable = true, length = 30)
    private long currentTerm;

    @Column(name = "votedFor", nullable = true, length = 30)
    private String votedFor;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCurrentTerm() {
        return currentTerm;
    }

    public void setCurrentTerm(long currentTerm) {
        this.currentTerm = currentTerm;
    }

    public String getVotedFor() {
        return votedFor;
    }

    public void setVotedFor(String votedFor) {
        this.votedFor = votedFor;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", currentTerm=" + currentTerm + ", votedFor=" + votedFor + "]";
    }
}

