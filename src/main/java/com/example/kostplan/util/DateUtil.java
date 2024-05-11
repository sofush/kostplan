package com.example.kostplan.util;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.IntStream;

public class DateUtil {
	private static final LocalDate WEEK_ORIGIN = LocalDate.of(2024, Month.MAY, 6);

	/**
	 * Calculate the of dates a week given a week index, where the week index is the number of weeks since a fixed
	 * time origin (May 6th, 2024).
	 * @param weekIndex The week to filter for.
	 * @return An ordered list of dates of the requested week (monday through sunday).
	 */
	public static List<LocalDate> calculateDatesOfNthWeek(int weekIndex) {
		LocalDate monday = DateUtil.WEEK_ORIGIN.plusWeeks(weekIndex);

		return IntStream.range(0, 7)
			.mapToObj(monday::plusDays)
			.toList();
	}

	/**
	 * Calculates the number of weeks that have passed since a fixed time origin (May 6th, 2024).
	 * @return The number of weeks that have passed since May 6th, 2024.
	 */
	public static int calculateCurrentWeekIndex() {
		return (int) ChronoUnit.WEEKS.between(DateUtil.WEEK_ORIGIN, LocalDate.now());
	}
}
