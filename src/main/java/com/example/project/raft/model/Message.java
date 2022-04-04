package com.example.project.raft.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

/**
 * @author revanth on 4/2/22
 */
@Getter
@Setter
public class Message {

    @SerializedName("sender_name")
    String senderName;

    @SerializedName("request")
    RequestType requestType;

    String term;

    String key;

    String value;
}
