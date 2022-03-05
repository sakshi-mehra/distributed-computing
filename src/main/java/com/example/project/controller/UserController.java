package com.example.project.controller;

import com.example.project.ProjectApplication;
import com.example.project.entity.User;
import com.example.project.service.Impl.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpHeaders;
import java.util.HashMap;
import java.util.List;


@Controller
public class UserController {

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

        if (ProjectApplication.IS_LEADER) {
            // Call other 2 service APIs
            // on any failure return
            String uri_b = "http://localhost:8081/add";
            String uri_c = "http://localhost:8082/add";
            RestTemplate restTemplate = new RestTemplate();
            User newUser = new User();
            newUser.setFirstName(user.getFirstName());
            newUser.setLastName(user.getLastName());
            try{
                String result_b = restTemplate.getForObject(uri_b,String.class,newUser);
            }
            catch (Exception e) {
                return "addFailed";
            }
            try{
                String result_c = restTemplate.getForObject(uri_c,String.class,newUser);
            }
            catch (Exception e) {
                return "addFailed";
            }
        }

        try {
            userService.addUser(user);
            return "addSuccess";
        } catch (Exception e) {
            return "addFailed";
        }
    }
}