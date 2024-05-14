package com.example.kostplan.entity;

import java.time.LocalDate;
import java.util.List;

/**
 * Information about a user including account details and physical properties.
 */
public class User {
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
	 * The user's email address.
	 */
	private String emailAddress;

	/**
	 * The user's phone number.
	 */
	private String phoneNumber;

	/**
	 * The user's role in the system (normal user, admin, chef, etc.)
	 */
	private Role role;

	/**
	 * The user's weight goal.
	 */
	private WeightGoal weightGoal;

	/**
	 * The user's physical activity level.
	 */
	private ActivityLevel activityLevel;

	/**
	 * Whether the user is male.
	 */
	private boolean male;

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

	public User(String username,
				String password,
				String emailAddress,
				String phoneNumber,
				Role role,
				WeightGoal weightGoal,
				ActivityLevel activityLevel,
				boolean male,
				int weight,
				LocalDate dob,
				int height
	) {
		this.username = username;
		this.password = password;
		this.emailAddress = emailAddress;
		this.phoneNumber = phoneNumber;
		this.role = role;
		this.weightGoal = weightGoal;
		this.activityLevel = activityLevel;
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

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public WeightGoal getWeightGoal() {
		return weightGoal;
	}

	public void setWeightGoal(WeightGoal weightGoal) {
		this.weightGoal = weightGoal;
	}

	public ActivityLevel getActivityLevel() {
		return activityLevel;
	}

	public void setActivityLevel(ActivityLevel activityLevel) {
		this.activityLevel = activityLevel;
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
}
