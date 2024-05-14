package com.example.kostplan.util;

import com.example.kostplan.entity.ActivityLevel;
import com.example.kostplan.entity.Ingredient;
import com.example.kostplan.entity.Recipe;
import com.example.kostplan.entity.WeightGoal;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

public class HealthUtil {
	private HealthUtil() {}

	/**
	 * Calculates a calorie goal based on a weight goal, a physical activity level and BMR (Basal Metabolic Rate,
	 * using the Harris-Benedict equation).
	 * @return A calorie goal.
	 */
	public static double calculateCalorieGoal(
		WeightGoal weightGoal,
		ActivityLevel activityLevel,
		boolean male,
		int weight,
		int height,
		LocalDate dob
	) {
		final double weightFactor = 10.0;
		final double heightFactor = 6.25;
		final double ageFactor = 5.0;
		final double constantTerm = male ? 5.0 : -161.0;

		long ageInYears = ChronoUnit.YEARS.between(dob, LocalDate.now());
		double bmr = (weightFactor * weight)
			+ (heightFactor * height)
			- (ageFactor * ageInYears)
			+ constantTerm;

		long weightGoalTerm = 0;

		if (weightGoal != null) {
			switch (weightGoal) {
				case EQUILIBRIUM -> {}
				case GAIN -> weightGoalTerm = 500;
				case LOSS -> weightGoalTerm = -500;
				case MUSCLE -> weightGoalTerm = 300;
			}
		}

		double activityLevelFactor = 1.2;

		if (activityLevel != null) {
			switch (activityLevel) {
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
	 */
	public static void scaleRecipe(double calorieGoal, Recipe recipe, List<Recipe> scheduledMeals)
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
