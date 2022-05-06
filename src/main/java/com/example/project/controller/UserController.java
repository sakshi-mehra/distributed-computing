package com.example.project.controller;

import com.example.project.ProjectApplication;
import com.example.project.entity.User;
import com.example.project.raft.communication.Configs;
import com.example.project.raft.communication.Receiver;
import com.example.project.raft.communication.Sender;
import com.example.project.raft.model.BaseMessage;
import com.example.project.raft.model.RequestType;
import com.example.project.raft.tasks.ReceiveCallback;
import com.example.project.service.Impl.UserService;
import com.example.project.utils.AnnotationExclusionStrategy;
import com.example.project.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;


@Controller
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserService userService;

    @GetMapping("/users")
    public String getUsers(Model model) {
        List<User> userList = userService.getAllUsers();
        model.addAttribute("users", userList);
        return "users";
    }

    @GetMapping("/addUser")
    public String addUser(Model model) {
        model.addAttribute("user", new User());
        return "adduser";
    }

    @PostMapping("/add")
    public String add(User user) {

        Gson gson = new GsonBuilder().setExclusionStrategies(new AnnotationExclusionStrategy()).create();
        Sender sender = new Sender(Configs.GROUP_NAME, Configs.PORT);

        BaseMessage baseMessage = new BaseMessage();
        baseMessage.setSenderName(Utils.getHostName());
        baseMessage.setRequestType(RequestType.LEADER_INFO);
        baseMessage.setTerm(0);
        baseMessage.setKey(null);
        baseMessage.setValue(null);

        for (int i = 0; i < Configs.NODE_COUNT; i++) {
            try {
                sender.uniCast("Node" + (i + 1), gson.toJson(baseMessage));
                LOGGER.info(gson.toJson(baseMessage));
                LOGGER.info("********Message sent to Node" + (i + 1));
                break;
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        String leaderNode;
        try {
            final String[] leader_info = new String[1];
            Receiver receiver = new Receiver(new ReceiveCallback() {
                @Override
                public void receive(String message) {
                    leader_info[0] = message;
                }
            }, Configs.GROUP_NAME2, Configs.PORT1);
            receiver.receive();
            receiver.stop();
            LOGGER.info("Receiver" + leader_info[0]);
            BaseMessage leaderMsg = gson.fromJson(leader_info[0], BaseMessage.class);
            leaderNode = leaderMsg.getValue();
            LOGGER.info(leaderNode + "is the leader node");
        } catch (IOException e) {
            e.printStackTrace();
            return "addFailed";
        }

        LOGGER.info("Sending store info to the leader");

        baseMessage = new BaseMessage();
        baseMessage.setRequestType(RequestType.STORE);
        baseMessage.setValue(gson.toJson(user));
        baseMessage.setKey("UM");

        try {
            sender.uniCast(leaderNode, gson.toJson(baseMessage));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            e.printStackTrace();
            return "addFailed";
        }

        return "addSuccess";
    }
}