package com.example.project.raft.model;

import com.example.project.raft.communication.Message;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

/**
 * @author revanth on 4/2/22
 */
@Getter
@Setter
public class BaseMessage implements Message {

    @SerializedName("sender_name")
    String senderName;

    @SerializedName("request")
    RequestType requestType;

    long term;

    String key;

    String value;
}
