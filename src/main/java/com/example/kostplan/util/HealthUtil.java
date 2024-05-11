package com.example.kostplan.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
public class HealthUtil {
	private HealthUtil() {}

	/**
	 * Calculates the BMR (Basal Metabolic Rate) using the Harris-Benedict equation.
	 * @return the BMR in kilocalories.
	 */
	public static int calculateBMR(boolean male, int weight, int height, LocalDate dob) {
		double weightFactor = male ? 13.752 : 9.563;
		double heightFactor = male ? 5 : 1.85;
		double ageFactor = male ? 6.755 : 4.676;
		double constantTerm = male ? 66.473 : 655.096;

		long ageInYears = ChronoUnit.YEARS.between(LocalDate.now(), dob);

		double bmr = (weightFactor * weight)
			+ (heightFactor * height)
			- (ageFactor * ageInYears)
			+ constantTerm;

		return (int)bmr;
	}
}
