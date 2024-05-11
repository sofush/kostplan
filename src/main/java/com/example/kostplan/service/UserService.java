package com.example.kostplan.service;

import com.example.kostplan.entity.Day;
import com.example.kostplan.entity.Recipe;
import com.example.kostplan.entity.Role;
import com.example.kostplan.entity.User;
import com.example.kostplan.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class UserService {
	private static final LocalDate WEEK_ORIGIN = LocalDate.of(2024, Month.MAY, 6);
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

	public void addDay(String username, LocalDate date, Recipe breakfast, Recipe lunch, Recipe dinner)
		throws DataAccessException
	{
		Day day = new Day(date, username, breakfast, lunch, dinner);
		this.repository.addDay(day);
	}

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
	public List<Day> findDaysOfWeek(String username, int weekIndex) {
		return this.calculateDatesOfNthWeek(weekIndex).stream()
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

	/**
	 * Calculate the of dates a week given a week index, where the week index is the number of weeks since a fixed
	 * time origin (May 6th, 2024).
	 * @param weekIndex The week to filter for.
	 * @return An ordered list of dates of the requested week (monday through sunday).
	 */
	public List<LocalDate> calculateDatesOfNthWeek(int weekIndex) {
		LocalDate monday = UserService.WEEK_ORIGIN.plusWeeks(weekIndex);

		return IntStream.range(0, 7)
			.mapToObj(monday::plusDays)
			.toList();
	}

	/**
	 * Calculates the number of weeks that have passed since a fixed time origin (May 6th, 2024).
	 * @return The number of weeks that have passed since May 6th, 2024.
	 */
	public int calculateCurrentWeekIndex() {
		return (int)ChronoUnit.WEEKS.between(UserService.WEEK_ORIGIN, LocalDate.now());
	}
}
