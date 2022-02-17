package com.example.project.service.Impl;

import com.example.project.entity.Data;
import com.example.project.models.DataModel;

import java.util.List;

public interface IDataService {

	public List<Data> getAllUsers();

	/*public void save(UserLogin person);*/

    public DataModel getUser(DataModel loginModel);
}
