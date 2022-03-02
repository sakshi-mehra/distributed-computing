package com.example.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class ProjectApplication {

    public static boolean IS_LEADER = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectApplication.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(ProjectApplication.class, args);

        String leaderEnv = System.getenv("IS_LEADER");
        LOGGER.info("Leader env : " + leaderEnv);
        if (leaderEnv.equals("true"))
            IS_LEADER = true;
    }
}
