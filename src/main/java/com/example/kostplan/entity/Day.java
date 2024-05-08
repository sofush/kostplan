package com.example.kostplan.entity;

import java.time.LocalDate;

public class Day {
	/**
	 * This day's date.
	 */
	private LocalDate date;

	/**
	 * The username of a user which this day belongs to.
	 */
	private String username;

	/**
	 * The recipe which has been assigned for breakfast.
	 */
	private Recipe breakfast;

	/**
	 * The recipe which has been assigned for lunch.
	 */
	private Recipe lunch;

	/**
	 * The recipe which has been assigned for dinner.
	 */
	private Recipe dinner;

	public Day(LocalDate date, String username, Recipe breakfast, Recipe lunch, Recipe dinner) {
		this.date = date;
		this.username = username;
		this.breakfast = breakfast;
		this.lunch = lunch;
		this.dinner = dinner;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Recipe getBreakfast() {
		return breakfast;
	}

	public void setBreakfast(Recipe breakfast) {
		this.breakfast = breakfast;
	}

	public Recipe getLunch() {
		return lunch;
	}

	public void setLunch(Recipe lunch) {
		this.lunch = lunch;
	}

	public Recipe getDinner() {
		return dinner;
	}

	public void setDinner(Recipe dinner) {
		this.dinner = dinner;
	}
}
