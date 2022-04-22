package com.example.project.entity;

import javax.persistence.*;

/**
 * @author revanth on 4/21/22
 */
@Entity
@Table(name="Log")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long logId;

    @Column(name = "term", nullable = false)
    private long term;

    @Column(name = "entryKey", nullable = false)
    private String entryKey;

    @Column(name = "entryValue", nullable = true)
    private String entryValue;

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public String getEntryKey() {
        return entryKey;
    }

    public void setEntryKey(String entryKey) {
        this.entryKey = entryKey;
    }

    public String getEntryValue() {
        return entryValue;
    }

    public void setEntryValue(String entryValue) {
        this.entryValue = entryValue;
    }
}
