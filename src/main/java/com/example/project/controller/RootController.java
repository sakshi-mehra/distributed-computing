package com.example.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author revanth on 2/17/22
 */

@Controller
public class RootController {

    @GetMapping("/home")
    public String index() {
        return "index";
    }
}
