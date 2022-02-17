package com.example.project.service.Impl;

import com.example.project.models.DataModel;

public interface IDataService {

	/*public List<UserLogin> getAllUsers();

	public void save(UserLogin person);*/

    public DataModel getUser(DataModel loginModel);
}
