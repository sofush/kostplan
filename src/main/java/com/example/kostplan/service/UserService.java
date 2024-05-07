package com.example.kostplan.service;

import com.example.kostplan.entity.Role;
import com.example.kostplan.entity.User;
import com.example.kostplan.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserService {
	private final UserRepository repository;
	private final PasswordEncoder encoder;

	@Autowired
	public UserService(UserRepository repository, PasswordEncoder encoder) {
		this.repository = repository;
		this.encoder = encoder;
	}

	public User getUserByUsername(String username)
		throws DataAccessException
	{
		return this.repository.getUserByUsername(username);
	}

	public void addUser(String username, String password, String name, boolean male, int weight, LocalDate dob, int height)
		throws DataAccessException
	{
		// Encode the password using the PasswordEncoder bean.
		String encodedPassword = this.encoder.encode(password);

		User user = new User(
			username,
			encodedPassword,
			name,
			Role.USER,
			male,
			weight,
			dob,
			height
		);

		this.repository.addUser(user);
	}
}
