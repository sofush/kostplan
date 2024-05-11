package com.example.kostplan.service;

import com.example.kostplan.entity.Day;
import com.example.kostplan.entity.Recipe;
import com.example.kostplan.entity.Role;
import com.example.kostplan.entity.User;
import com.example.kostplan.repository.UserRepository;
import com.example.kostplan.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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

	@PreAuthorize("#username == authentication.principal.username || hasRole('ADMIN')")
	public void addDay(String username, LocalDate date, Recipe breakfast, Recipe lunch, Recipe dinner)
		throws DataAccessException
	{
		Day day = new Day(date, username, breakfast, lunch, dinner);
		this.repository.addDay(day);
	}

	@PreAuthorize("#username == authentication.principal.username || hasRole('ADMIN')")
	public void updateDay(String username, LocalDate date, Recipe breakfast, Recipe lunch, Recipe dinner)
		throws DataAccessException
	{
		this.repository.updateDay(new Day(
			date,
			username,
			breakfast,
			lunch,
			dinner
		));
	}

	@PreAuthorize("#username == authentication.principal.username || hasRole('ADMIN')")
	public Day findDay(String username, LocalDate localDate)
		throws DataAccessException
	{
		return this.repository.findDay(username, localDate);
	}

	public Recipe findRecipeById(Integer recipeId)
		throws DataAccessException
	{
		return this.repository.findRecipeById(recipeId);
	}

	public List<Recipe> findRecipesForWeek(int weekIndex)
		throws DataAccessException
	{
		return this.repository.findRecipesForWeek(weekIndex);
	}

	/**
	 * Retrieves a user's list of Day objects for a specified week from persistent storage. If the Day object does not
	 * exist in persistent storage, the element will instead be replaced by an empty Day instance.
	 * @param username A user's username.
	 * @param weekIndex The week to filter for.
	 * @return An ordered list of seven Day objects (monday through sunday).
	 */
	@PreAuthorize("#username == authentication.principal.username || hasRole('ADMIN')")
	public List<Day> findDaysOfWeek(String username, int weekIndex) {
		return DateUtil.calculateDatesOfNthWeek(weekIndex).stream()
			.map((date) -> {
				Day day = new Day(date, username, null, null, null);

				try {
					Day dayResult = this.repository.findDay(username, date);

					if (dayResult != null)
						day = dayResult;
				} catch (Exception e) {
					// Let the program return an empty Day instance below.
				}

				return day;
			})
			.toList();
	}
}
