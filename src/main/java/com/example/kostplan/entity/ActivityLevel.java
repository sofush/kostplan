package com.example.kostplan.entity;

/**
 * Describes how often a person is physically active.
 */
public enum ActivityLevel {
	/**
	 * Very little to no activity (less than 1-2 times per week).
	 */
	INACTIVE,

	/**
	 * Active 1-2 times per week.
	 */
	LOW,

	/**
	 * Active 3-5 times per week.
	 */
	MODERATE,

	/**
	 * Active 6-7 times per week.
	 */
	HIGH,

	/**
	 * Active 1-2 times per day.
	 */
	VERY_HIGH,
}
