package com.example.project;

import com.example.project.raft.RaftImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;

@SpringBootApplication
public class ProjectApplication {

    public static boolean IS_LEADER = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectApplication.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(ProjectApplication.class, args);

        String leaderEnv = System.getenv("IS_LEADER");
        LOGGER.info("Leader environment variable : " + leaderEnv);
        if (leaderEnv.equals("true"))
            IS_LEADER = true;

        LOGGER.info("Is Leader  : " + IS_LEADER);

        try {
            RaftImpl raft = new RaftImpl();
            raft.init();
        } catch (IOException e) {
            LOGGER.error("Raft init failed with error");
            LOGGER.error(e.getMessage(), e);
            System.exit(1);
        }
    }
}
