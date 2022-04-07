package com.example.project;

import com.example.project.raft.RaftImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class ProjectApplication {

    public static boolean IS_LEADER = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectApplication.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RaftImpl raft;

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(ProjectApplication.class, args);

        String leaderEnv = System.getenv("IS_LEADER");
        LOGGER.info("Leader environment variable : " + leaderEnv);
        if (leaderEnv.equals("true"))
            IS_LEADER = true;

        LOGGER.info("Is Leader  : " + IS_LEADER);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void afterStartup() {
        raft.init();
    }
}
