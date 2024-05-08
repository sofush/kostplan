package com.example.kostplan.entity;

import java.util.List;

public class Recipe {
	/**
	 * A unique identifier assigned by the database.
	 */
	private Integer id;

	/**
	 * An index which determines which week this recipe belongs to.
	 */
	private int week;

	/**
	 * The title of the recipe/dish.
	 */
	private String title;

	/**
	 * A description of the recipe/dish.
	 */
	private String description;

	/**
	 * Nutritional facts.
	 */
	private String nutrition_info;

	/**
	 * The list of ingredients that are needed to cook this recipe/dish.
	 */
	private List<Ingredient> ingredients;

	/**
	 * The instructions one would follow to cook this recipe/dish.
	 */
	private String instructions;

	/**
	 * A description of how much time is needed to cook this recipe/dish.
	 */
	private String time;

	public Recipe(int week, String title, String description, String nutrition_info, List<Ingredient> ingredients, String instructions, String time) {
		this.id = null;
		this.week = week;
		this.title = title;
		this.description = description;
		this.nutrition_info = nutrition_info;
		this.ingredients = ingredients;
		this.instructions = instructions;
		this.time = time;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public int getWeek() {
		return week;
	}

	public void setWeek(int week) {
		this.week = week;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNutrition_info() {
		return nutrition_info;
	}

	public void setNutrition_info(String nutrition_info) {
		this.nutrition_info = nutrition_info;
	}

	public List<Ingredient> getIngredients() {
		return ingredients;
	}

	public void setIngredients(List<Ingredient> ingredients) {
		this.ingredients = ingredients;
	}

	public String getInstructions() {
		return instructions;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int sumCalories() {
		if (this.ingredients == null)
			return 0;

		return this.ingredients.stream()
			.map(Ingredient::sumCalories)
			.reduce(Integer::sum)
			.orElse(0);
	}
}