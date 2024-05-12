package com.example.kostplan.util;

import com.example.kostplan.entity.Ingredient;
import com.example.kostplan.entity.Recipe;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

public class HealthUtil {
	private HealthUtil() {}

	/**
	 * Calculates the BMR (Basal Metabolic Rate) using the Harris-Benedict equation.
	 * @return the BMR in kilocalories.
	 */
	public static double calculateBMR(boolean male, int weight, int height, LocalDate dob) {
		double weightFactor = male ? 13.752 : 9.563;
		double heightFactor = male ? 5 : 1.85;
		double ageFactor = male ? 6.755 : 4.676;
		double constantTerm = male ? 66.473 : 655.096;

		long ageInYears = ChronoUnit.YEARS.between(LocalDate.now(), dob);

		return (weightFactor * weight)
			+ (heightFactor * height)
			- (ageFactor * ageInYears)
			+ constantTerm;
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
