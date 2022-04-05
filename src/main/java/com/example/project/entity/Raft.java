package com.example.project.entity;

import javax.persistence.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Raft")
public class Raft {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "currentTerm", nullable = false, length = 30)
    private int currentTerm;

    @Column(name = "votedFor", nullable = false, length = 30)
    private String votedFor;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getcurrentTerm() {
        return currentTerm;
    }

    public void setcurrentTerm(int currentTerm) {
        this.currentTerm = currentTerm;
    }

    public String getvotedFor() {
        return votedFor;
    }

    public void setvotedFor(String votedFor) {
        this.votedFor = votedFor;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", currentTerm=" + currentTerm + ", votedFor=" + votedFor + "]";
    }
}

