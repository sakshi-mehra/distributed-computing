package com.example.project.repo;

import java.util.List;

/**
 * @author revanth on 2/15/22
 */
public interface PostInfo {

    void addNewPost(String post);

    List<String> getAllPost();
}
