package com.example.kostplan.entity;

import com.example.kostplan.util.HealthUtil;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
	 * A BCrypt hash. This string contains the version of Bcrypt which was used to encrypt,
	 * the strength (number of rounds), the salt in cleartext, and finally the actual hash of the password + salt.
	 */
	private String password;

	/**
	 * The user's real name.
	 */
	private String name;

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

	public User(String username, String password, String name, Role role, boolean male, int weight, LocalDate dob, int height) {
		this.username = username;
		this.password = password;
		this.name = name;
		this.role = role;
		this.male = male;
		this.weight = weight;
		this.dob = dob;
		this.height = height;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username)
		throws IllegalArgumentException
	{
		if (username == null || username.length() < 3 || username.length() > 20) {
			throw new IllegalArgumentException("Username must be between 3-20 characters long.");
		}

		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password)
		throws IllegalArgumentException
	{
		if (password == null || password.length() < 3) {
			throw new IllegalArgumentException("Password must be at least 3 characters long.");
		}

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

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public LocalDate getDob() {
		return dob;
	}

	public void setDob(LocalDate dob)
		throws IllegalArgumentException
	{
		if (dob.isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("Date of birth must not be in the future.");
		}

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

	/**
	 * Calculates the BMR (Basal Metabolic Rate) of this user, using the Harris-Benedict equation.
	 * @return the user's BMR in kilocalories.
	 */
	public double calculateBMR() {
		return HealthUtil.calculateBMR(
			this.isMale(),
			this.getWeight(),
			this.getHeight(),
			this.getDob()
		);
	}
}
