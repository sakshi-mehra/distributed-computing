package com.example.project.controller;

import com.example.project.ProjectApplication;
import com.example.project.entity.User;
import com.example.project.service.Impl.UserService;
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

    public String forwardRequest(String uri, User user) {

        URL url = null;

        try {
            url = new URL(uri);
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection)con;
            http.setRequestMethod("POST");
            http.setDoOutput(true);

            Map<String,String> params = new HashMap<>();
            params.put("firstName", user.getFirstName());
            params.put("lastName", user.getLastName());

            StringJoiner sj = new StringJoiner("&");
            for(Map.Entry<String,String> entry : params.entrySet()) {
                try {
                    sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                            + URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    LOGGER.error(e.toString());
                    return "";
                }
            }

            byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);

            http.setFixedLengthStreamingMode(out.length);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            http.connect();
            try(OutputStream os = http.getOutputStream()) {
                os.write(out);
            }

            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String decodedString = null;
            while ((decodedString = in.readLine()) != null) {
                sb.append(decodedString);
            }
            in.close();

            return sb.toString();

        } catch (MalformedURLException e) {
            LOGGER.error(e.toString());
        } catch (ProtocolException e) {
            LOGGER.error(e.toString());
        } catch (IOException e) {
            LOGGER.error(e.toString());
        }

        return "";
    }

    @PostMapping("/add")
    public String add(User user) {

        if (ProjectApplication.IS_LEADER) {
            LOGGER.info("This is leader node, calling APIs of other nodes");
            String url1 = "http://server-2:8081/add";
            String url2 = "http://server-3:8082/add";
            String response;
            response = forwardRequest(url1, user);
            if (!response.contains("User added successfully")) {
                LOGGER.error("Request failed in server-2");
                return "addFailed";
            }

            response = forwardRequest(url2, user);
            if (!response.contains("User added successfully")) {
                LOGGER.error("Request failed in server-3");
                return "addFailed";
            }
        } else {
            LOGGER.info("This is non-leader node, got request from leader");
        }

        try {
            userService.addUser(user);
            return "addSuccess";
        } catch (Exception e) {
            return "addFailed";
        }
    }
}