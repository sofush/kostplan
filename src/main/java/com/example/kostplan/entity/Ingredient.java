package com.example.kostplan.entity;

public class Ingredient {
	/**
	 * A unique identifier assigned by the database.
	 */
	private int id;

	/**
	 * The name of this ingredient.
	 */
	private String name;

	/**
	 * A quantity.
	 */
	private int quantity;

	/**
	 * The unit used to measure this ingredient such as grams or litres.
	 * A null value or an empty string means that the measurement is unitless.
	 */
	private String unit;

	/**
	 * The number of calories in this ingredient per unit.
	 */
	private int calories;

	public Ingredient(int id, String name, int quantity, String unit, int calories) {
		this.id = id;
		this.name = name;
		this.quantity = quantity;
		this.unit = unit;
		this.calories = calories;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public int getCalories() {
		return calories;
	}

	public void setCalories(int calories) {
		this.calories = calories;
	}

	public int sumCalories() {
		return this.quantity * this.calories;
	}
}
