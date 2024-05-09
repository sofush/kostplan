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

	public User findUserByUsername(String username)
		throws DataAccessException
	{
		return this.repository.findUserByUsername(username);
	}

	public void addUser(String username, String password, String name, boolean male, int weight, LocalDate dob, int height)
		throws DataAccessException, IllegalArgumentException
	{
		if (username == null || username.length() < 3 || username.length() > 20) {
			throw new IllegalArgumentException("Username must be between 3-20 characters long.");
		}

		if (password == null || password.length() < 3) {
			throw new IllegalArgumentException("Password must be at least 3 characters long.");
		}

		if (dob.isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("Date of birth must not be in the future.");
		}

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
