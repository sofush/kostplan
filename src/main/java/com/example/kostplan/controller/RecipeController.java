package com.example.kostplan.controller;

import com.example.kostplan.entity.Day;
import com.example.kostplan.entity.Recipe;
import com.example.kostplan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class RecipeController {
	private final UserService service;

	@Autowired
	public RecipeController(UserService service) {
		this.service = service;
	}

	@GetMapping("/week")
	public String displayWeek(
		@RequestParam(required = false, name = "index") Integer weekIndex,
		Principal principal,
		Model model
	) {
		if (weekIndex == null) {
			weekIndex = this.service.calculateCurrentWeekIndex();
		}

		List<Day> daysOfWeek = this.service.findDaysOfWeek(principal.getName(), weekIndex);
		model.addAttribute("weekIndex", weekIndex);
		model.addAttribute("days", daysOfWeek);
		return "calendar";
	}

	@GetMapping("/pick/{weekday}/{meal}")
	public String pickRecipe(
		@PathVariable("weekday") String weekday,
		@PathVariable("meal") String meal,
		Model model
	) {
		int weekIndex = this.service.calculateCurrentWeekIndex();

		try {
			List<Recipe> recipes = this.service.findRecipesForWeek(weekIndex);
			model.addAttribute("recipes", recipes);
		} catch (DataAccessException e) {
			return "redirect:/week?error=database";
		}

		model.addAttribute("weekday", weekday);
		model.addAttribute("meal", meal);
		return "pick-a-recipe";
	}

	@GetMapping("/assign/{weekday}/{meal}/{id}")
	public String assignRecipeToDay(
		@PathVariable("weekday") String weekday,
		@PathVariable("meal") String meal,
		@PathVariable("id") Integer recipeId,
		Principal principal,
		Model model
	) {
		int weekIndex = this.service.calculateCurrentWeekIndex();
		Optional<LocalDate> date = this.service.calculateDatesOfNthWeek(weekIndex)
			.stream()
			.filter((d) -> d.getDayOfWeek().toString().toLowerCase().contentEquals(weekday))
			.findFirst();

		if (date.isEmpty()) {
			model.addAttribute("error", "day-of-week");
			return "redirect:/pick/" + weekday + "/" + meal;
		}

		Day day;
		Recipe recipe;

		try {
			recipe = this.service.findRecipeById(recipeId);
			day = this.service.findDay(principal.getName(), date.get());

			if (day == null) {
				this.service.addDay(
					principal.getName(),
					date.get(),
					null,
					null,
					null
				);

				day = this.service.findDay(principal.getName(), date.get());
			}
		} catch (DataAccessException e) {
			model.addAttribute("error", "database");
			return "redirect:/pick/" + weekday + "/" + meal;
		}

		if (recipe == null) {
			model.addAttribute("error", "invalid-recipe");
			return "redirect:/pick/" + weekday + "/" + meal;
		}

		switch (meal) {
			case "breakfast" -> day.setBreakfast(recipe);
			case "lunch" -> day.setLunch(recipe);
			case "dinner" -> day.setDinner(recipe);
		}

		this.service.updateDay(
			day.getUsername(),
			day.getDate(),
			day.getBreakfast(),
			day.getLunch(),
			day.getDinner()
		);
		return "redirect:/week";
	}
}
