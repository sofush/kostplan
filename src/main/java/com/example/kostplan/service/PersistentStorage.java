package com.example.kostplan.service;

import com.example.kostplan.entity.*;
import org.springframework.dao.DataAccessException;

import java.time.LocalDate;
import java.util.List;

public interface PersistentStorage {
	User findUserByUsername(String username) throws DataAccessException;
	void addUser(User user) throws DataAccessException;
	Recipe findRecipeById(int id) throws DataAccessException;
	List<Recipe> findRecipesForWeek(int weekIndex) throws DataAccessException;
	void addDay(Day day) throws DataAccessException;
	void updateDay(Day day) throws DataAccessException;
	Day findDay(String username, LocalDate date) throws DataAccessException;
	byte[] findRecipeImage(int recipeId) throws DataAccessException;
	void addRecipeImage(int recipeId, byte[] imageBytes) throws DataAccessException;
}
