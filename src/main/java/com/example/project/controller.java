package com.example.project;

import com.example.project.entity.Data;
import com.example.project.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class controller {

    @Autowired
    DataService dataService;

    @GetMapping("/home")
    public String index() {
        return "index";
    }

    @GetMapping("/users")
    public String getUsers(Model model) {
        List<Data> userList = dataService.getAllUsers();
        model.addAttribute("users", userList);
        return "users";
    }

    @GetMapping("/addUser")
    public String addUser(Model model) {
        model.addAttribute("data", new Data());
        return "adduser";
    }

    @PostMapping("/add")
    public String add(Data data) {
        try{
            dataService.addUser(data);
            System.out.println(data.getFirstName());
            System.out.println(data.getLastName());
            return "addSuccess";
        }
        catch(Exception e){
            return "addFailed";
        }
    }
}