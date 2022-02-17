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
    public void initialize(){
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.execute("DROP TABLE IF EXISTS Data");
            statement.executeUpdate(
                    "CREATE TABLE Data(" +
                            "id INTEGER Primary key, " +
                            "firstName varchar(30) not null, " +
                            "lastName varchar(30) not null)");
            statement.executeUpdate(
                    "INSERT INTO Data " +
                            "(firstName,lastName) " +
                            "VALUES " + "('Bharat','Verma')"
            );
            statement.close();
            connection.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
