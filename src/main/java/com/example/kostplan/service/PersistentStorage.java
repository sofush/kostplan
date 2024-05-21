package com.example.kostplan.service;

import com.example.kostplan.entity.*;

import java.time.LocalDate;
import java.util.List;

public interface PersistentStorage {
	User findUserByUsername(String username);
	void addUser(User user);
	Recipe findRecipeById(int id);
	List<Recipe> findRecipesForWeek(int weekIndex);
	void addDay(Day day);
	void updateDay(Day day);
	Day findDay(String username, LocalDate date);
	byte[] findRecipeImage(int recipeId);
	void addRecipeImage(int recipeId, byte[] imageBytes);
}
