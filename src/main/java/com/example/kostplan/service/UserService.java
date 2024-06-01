package com.example.kostplan.service;

import com.example.kostplan.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Service
public class UserService {
	private final PersistentStorage storage;
	private final PasswordEncoder encoder;

	@Autowired
	public UserService(PersistentStorage storage, PasswordEncoder encoder) {
		this.storage = storage;
		this.encoder = encoder;
	}

	public User findUserByUsername(String username)
		throws DataAccessException
	{
		return this.storage.findUserByUsername(username);
	}

	public void addUser(
		String username,
		String password,
		String emailAddress,
		String phoneNumber,
		WeightGoal weightGoal,
		ActivityLevel activityLevel,
		boolean male,
		int weight,
		LocalDate dob,
		int height
	)
		throws DataAccessException, IllegalArgumentException
	{
		if (username == null || username.length() < 3 || username.length() > 20) {
			throw new IllegalArgumentException("Username must be between 3-20 characters long.");
		}

		if (password == null || password.length() < 3) {
			throw new IllegalArgumentException("Password must be at least 3 characters long.");
		}

		if (dob == null || dob.isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("Date of birth must not be in the future.");
		}

		if (emailAddress == null || emailAddress.isEmpty()) {
			throw new IllegalArgumentException("Email address must have a value.");
		}

		if (phoneNumber == null || phoneNumber.isEmpty()) {
			throw new IllegalArgumentException("Phone number must have a value.");
		}

		if (weightGoal == null) {
			throw new IllegalArgumentException("Weight goal must have a value.");
		}

		if (activityLevel == null) {
			throw new IllegalArgumentException("Activity level must have a value.");
		}

		// Encode the password using the PasswordEncoder bean.
		String encodedPassword = this.encoder.encode(password);

		User user = new User(
			username,
			encodedPassword,
			emailAddress,
			phoneNumber,
			Role.USER,
			weightGoal,
			activityLevel,
			male,
			weight,
			dob,
			height
		);

		this.storage.addUser(user);
	}

	@PreAuthorize("(@subscription.isActive() && #username == authentication.principal.username) || hasRole('ADMIN')")
	public void addDay(
		String username,
		LocalDate date,
		Integer breakfastId,
		Integer lunchId,
		Integer dinnerId
	)
		throws DataAccessException
	{
		Recipe breakfast = null, lunch = null, dinner = null;

		if (breakfastId != null) breakfast = this.findRecipeById(breakfastId);
		if (lunchId != null) lunch = this.findRecipeById(lunchId);
		if (dinnerId != null) dinner = this.findRecipeById(dinnerId);

		Day day = new Day(date, username, breakfast, lunch, dinner);
		this.storage.addDay(day);
	}

	@PreAuthorize("(@subscription.isActive() && #username == authentication.principal.username) || hasRole('ADMIN')")
	public void updateDay(
		String username,
		LocalDate date,
		Integer breakfastId,
		Integer lunchId,
		Integer dinnerId
	)
		throws DataAccessException
	{
		Recipe breakfast = null, lunch = null, dinner = null;

		if (breakfastId != null) breakfast = this.findRecipeById(breakfastId);
		if (lunchId != null) lunch = this.findRecipeById(lunchId);
		if (dinnerId != null) dinner = this.findRecipeById(dinnerId);

		this.storage.updateDay(new Day(
			date,
			username,
			breakfast,
			lunch,
			dinner
		));
	}

	@PreAuthorize("(@subscription.isActive() && #username == authentication.principal.username) || hasRole('ADMIN')")
	public Day findDay(String username, LocalDate localDate)
		throws DataAccessException
	{
		return this.storage.findDay(username, localDate);
	}

	@PreAuthorize("@subscription.isActive()")
	public Recipe findRecipeById(Integer recipeId)
		throws DataAccessException
	{
		return this.storage.findRecipeById(recipeId);
	}

	@PreAuthorize("@subscription.isActive()")
	public List<Recipe> findRecipesForWeek(int weekIndex)
		throws DataAccessException
	{
		return this.storage.findRecipesForWeek(weekIndex);
	}

	/**
	 * Retrieves a user's list of Day objects for a specified week from persistent storage. If the Day object does not
	 * exist in persistent storage, the element will instead be replaced by an empty Day instance.
	 * @param username A user's username.
	 * @param weekIndex The week to filter for.
	 * @return An ordered list of seven Day objects (monday through sunday).
	 */
	@PreAuthorize("(@subscription.isActive() && #username == authentication.principal.username) || hasRole('ADMIN')")
	public List<Day> findDaysOfWeek(String username, int weekIndex) {
		final LocalDate WEEK_ORIGIN = LocalDate.of(2024, Month.MAY, 6);
		LocalDate monday = WEEK_ORIGIN.plusWeeks(weekIndex);

		return IntStream.range(0, 7)
			.mapToObj(monday::plusDays)
			.map((date) -> {
				Day day = new Day(date, username, null, null, null);

				try {
					Day dayResult = this.storage.findDay(username, date);

					if (dayResult != null)
						day = dayResult;
				} catch (Exception e) {
					// Let the program return an empty Day instance below.
				}

				return day;
			})
			.toList();
	}


	@PreAuthorize("@subscription.isActive()")
	public byte[] findRecipeImage(int recipeId)
		throws DataAccessException
	{
		return this.storage.findRecipeImage(recipeId);
	}

	@PreAuthorize("hasRole('ADMIN') || hasRole('CHEF')")
	public void addRecipeImage(int recipeId, byte[] imageBytes)
		throws DataAccessException
	{
		this.storage.addRecipeImage(recipeId, imageBytes);
	}

	/**
	 * Calculates a calorie goal based on a user's weight goal, a physical activity level and BMR (Basal Metabolic
	 * Rate, using the revised Harris-Benedict equation).
	 * @return A calorie goal.
	 */
	@PreAuthorize("(@subscription.isActive() && #username == authentication.principal.username) || hasRole('ADMIN')")
	public double calculateCalorieGoal(String username)
		throws DataAccessException
	{
		User user = this.findUserByUsername(username);

		final double weightFactor = 10.0;
		final double heightFactor = 6.25;
		final double ageFactor = 5.0;
		final double constantTerm = user.isMale() ? 5.0 : -161.0;

		long ageInYears = ChronoUnit.YEARS.between(user.getDob(), LocalDate.now());
		double bmr = (weightFactor * user.getWeight())
			+ (heightFactor * user.getHeight())
			- (ageFactor * ageInYears)
			+ constantTerm;

		long weightGoalTerm = 0;

		if (user.getWeightGoal() != null) {
			switch (user.getWeightGoal()) {
				case EQUILIBRIUM -> {}
				case GAIN -> weightGoalTerm = 500;
				case LOSS -> weightGoalTerm = -500;
				case MUSCLE -> weightGoalTerm = 300;
			}
		}

		double activityLevelFactor = 1.2;

		if (user.getActivityLevel() != null) {
			switch (user.getActivityLevel()) {
				case INACTIVE -> {}
				case LOW -> activityLevelFactor = 1.5;
				case MODERATE -> activityLevelFactor = 1.7;
				case HIGH -> activityLevelFactor = 1.9;
				case VERY_HIGH -> activityLevelFactor = 2.4;
			}
		}

		return (bmr * activityLevelFactor) + weightGoalTerm;
	}

	/**
	 * Scales a recipe up or down to meet a fixed calorie goal by modifying the recipe's ingredient list (specifically
	 * the ingredient's quantity value). This takes into account a list of other meals a person has/will eat that day,
	 * as to scale all meals proportionally.
	 * @param calorieGoal The person's calorie goal.
	 * @param recipe The recipe to scale up or down.
	 * @param scheduledMeals The recipes of the meals the person will also eat that day.
	 */
	@PreAuthorize("@subscription.isActive() || hasRole('ADMIN')")
	public void scaleRecipe(double calorieGoal, Recipe recipe, List<Recipe> scheduledMeals)
		throws IllegalArgumentException
	{
		if (recipe == null || scheduledMeals == null) {
			throw new IllegalArgumentException("Arguments must not be null.");
		}

		double totalCalories = recipe.sumCalories() + scheduledMeals.stream()
			.filter(Objects::nonNull)
			.map(Recipe::sumCalories)
			.reduce(Double::sum)
			.orElse(0.0);
		double scaleFactor = calorieGoal / totalCalories;

		for (Ingredient ingredient : recipe.getIngredients()) {
			double newQuantity = ingredient.getQuantity() * scaleFactor;
			ingredient.setQuantity(newQuantity);
		}
	}
}
