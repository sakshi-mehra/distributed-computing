package com.example.project.entity;

import com.example.project.utils.Exclude;
import com.google.gson.annotations.SerializedName;

import javax.persistence.*;

/**
 * @author revanth on 4/21/22
 */
@Entity
@Table(name="Log")
public class Log {

    @Exclude
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long logId;

    @SerializedName("Term")
    @Column(name = "term", nullable = false)
    private Long term;

    @SerializedName("Key")
    @Column(name = "entryKey", nullable = false)
    private String entryKey;

    @SerializedName("Value")
    @Column(name = "entryValue")
    private String entryValue;

    public Long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public Long getTerm() {
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

    @Override
    public String toString() {
        return "Log{" +
                "logId=" + logId +
                ", term=" + term +
                ", entryKey='" + entryKey + '\'' +
                ", entryValue='" + entryValue + '\'' +
                '}';
    }
}
