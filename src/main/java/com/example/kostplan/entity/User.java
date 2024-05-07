package com.example.kostplan.entity;

import java.time.LocalDate;
import java.util.List;

/**
 * Information about a user including account details and physical properties.
 */
public class User {
	private Role role;
	private boolean male;

	/**
	 * The user's username which must be unique.
	 */
	private String username;

	/**
	 * The user's hashed password.
	 */
	private String password;

	/**
	 * The user's real name.
	 */
	private String name;

	/**
	 * The user's weight goal in kilograms.
	 */
	private int weight_goal;
	/**
	 * The user's specified weight measurement in kilograms.
	 */
	private int weight;

	/**
	 * The user's date of birth.
	 */
	private LocalDate dob;

	/**
	 * The user's specified height measurement in centimetres.
	 */
	private int height;

	public User(String username, String password, String name, Role role, boolean male, int weight_goal, int weight, LocalDate dob, int height) {
		this.username = username;
		this.password = password;
		this.name = name;
		this.role = role;
		this.male = male;
		this.weight_goal = weight_goal;
		this.weight = weight;
		this.dob = dob;
		this.height = height;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public boolean isMale() {
		return male;
	}

	public void setMale(boolean male) {
		this.male = male;
	}

	public int getWeightGoal() {
		return weight_goal;
	}

	public void setWeightGoal(int weight_goal) {
		this.weight_goal = weight_goal;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public LocalDate getDob() {
		return dob;
	}

	public void setDob(LocalDate dob) {
		this.dob = dob;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * Creates a list of Spring authorities that determine which privileges the user has in our system.
	 * @return a list of Spring authorities.
	 */
	public List<String> getAuthorities() {
		switch (this.getRole()) {
			case Role.USER -> { return List.of("ROLE_USER"); }
			case Role.CHEF -> { return List.of("ROLE_USER", "ROLE_CHEF"); }
			case Role.ADMIN -> { return List.of("ROLE_USER", "ROLE_CHEF", "ROLE_ADMIN"); }
			default -> { return List.of(); }
		}
	}
}
