package com.example.kostplan.entity;

/**
 * Roles that can apply to the users in our system.
 */
public enum Role {
	/**
	 * Privileges to view and edit the user's own profile (should apply to all users).
	 */
	USER,

	/**
	 * Privileges to view and edit other users' profiles.
	 */
	ADMIN,

	/**
	 * Privileges to view, edit and delete recipes, the weekly meal plan etc.
	 */
	CHEF,
}
