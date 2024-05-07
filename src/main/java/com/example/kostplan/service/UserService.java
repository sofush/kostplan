package com.example.kostplan.service;

import com.example.kostplan.entity.User;
import com.example.kostplan.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class UserService {
	private final UserRepository repository;

	@Autowired
	public UserService(UserRepository repository) {
		this.repository = repository;
	}

	public User getUserByUsername(String username)
		throws DataAccessException
	{
		return this.repository.getUserByUsername(username);
	}
}
