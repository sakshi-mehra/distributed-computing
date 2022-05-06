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

    @Column(name = "term", nullable = true, length = 30)
    private long term;

    @Column(name = "lastApplied", nullable = true, length = 30)
    private long lastApplied;

    @Column(name = "votedFor", nullable = true, length = 30)
    private String votedFor;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public String getVotedFor() {
        return votedFor;
    }

    public void setVotedFor(String votedFor) {
        this.votedFor = votedFor;
    }

    public long getLastApplied() {
        return lastApplied;
    }

    public void setLastApplied(long lastApplied) {
        this.lastApplied = lastApplied;
    }

    @Override
    public String toString() {
        return "RaftPersistenceInfo{" +
                "id=" + id +
                ", term=" + term +
                ", lastApplied=" + lastApplied +
                ", votedFor='" + votedFor + '\'' +
                '}';
    }
}

