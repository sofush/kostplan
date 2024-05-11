package com.example.kostplan.entity;

import java.text.DecimalFormat;

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
	private double quantity;

	/**
	 * The unit used to measure this ingredient such as grams or litres.
	 * A null value or an empty string means that the measurement is unitless.
	 */
	private String unit;

	/**
	 * The number of kilocalories in this ingredient per unit.
	 */
	private double calories;

	public Ingredient(int id, String name, double quantity, String unit, double calories) {
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

	public double getQuantity() {
		return quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public double getCalories() {
		return calories;
	}

	public void setCalories(double calories) {
		this.calories = calories;
	}

	public double sumCalories() {
		return this.quantity * this.calories;
	}

	@Override
	public String toString() {
		DecimalFormat formatter = new DecimalFormat("#.##");
		return formatter.format(this.getQuantity()) +
			' ' +
			this.getUnit() +
			' ' +
			this.getName();
	}
}
