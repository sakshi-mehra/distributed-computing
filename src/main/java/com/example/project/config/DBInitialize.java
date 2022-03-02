package com.example.project.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DBInitialize {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void initialize() {
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
//            statement.execute("DROP TABLE IF EXISTS USER");
            statement.executeUpdate(
                    "CREATE TABLE USER(" +
                            "id INTEGER PRIMARY KEY, " +
                            "firstName VARCHAR(30) NOT NULL, " +
                            "lastName VARCHAR(30) NOT NULL)");
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
