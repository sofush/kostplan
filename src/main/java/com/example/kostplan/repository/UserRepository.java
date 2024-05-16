package com.example.kostplan.repository;

import com.example.kostplan.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Repository
public class UserRepository {
	private final JdbcTemplate jdbc;

	@Autowired
	public UserRepository(JdbcTemplate jdbc)
		throws DataAccessException
	{
		this.jdbc = jdbc;

		if (this.jdbc == null) {
			return;
		}

		this.jdbc.execute("""
			CREATE TABLE IF NOT EXISTS User(
				username VARCHAR(20),
				password TEXT NOT NULL,
				email_address TEXT NOT NULL,
				phone_number TEXT NOT NULL,
				role INTEGER NOT NULL,
				weight_goal INTEGER NOT NULL,
				activity_level INTEGER NOT NULL,
				name TEXT,
				male BOOLEAN NOT NULL,
				weight INTEGER NOT NULL,
				dob DATE NOT NULL,
				height INTEGER NOT NULL,
				PRIMARY KEY (username)
			);
			""");

		this.jdbc.execute("""
			CREATE TABLE IF NOT EXISTS Recipe(
				id INTEGER AUTO_INCREMENT,
				week INTEGER,
				title TEXT,
				description TEXT,
				instructions TEXT,
				time TEXT,
				PRIMARY KEY (id)
			);
			""");

		this.jdbc.execute("""
			CREATE TABLE IF NOT EXISTS RecipeImage(
				id INTEGER AUTO_INCREMENT,
				recipe INTEGER NOT NULL,
				bytes LONGBLOB,
				PRIMARY KEY (id),
				FOREIGN KEY (recipe) REFERENCES Recipe(id)
			);
			""");

		this.jdbc.execute("""
			CREATE TABLE IF NOT EXISTS Ingredient(
				id INTEGER AUTO_INCREMENT,
				recipe INTEGER,
				name TEXT,
				quantity DOUBLE,
				unit TEXT,
				calories DOUBLE,
				PRIMARY KEY (id),
				FOREIGN KEY (recipe) REFERENCES Recipe(id)
			);
			""");

		this.jdbc.execute("""
			CREATE TABLE IF NOT EXISTS Day(
				date DATE,
				username VARCHAR(20),
				breakfast INTEGER,
				lunch INTEGER,
				dinner INTEGER,
				PRIMARY KEY (date, username),
				FOREIGN KEY (breakfast) REFERENCES Recipe(id),
				FOREIGN KEY (lunch) REFERENCES Recipe(id),
				FOREIGN KEY (dinner) REFERENCES Recipe(id)
			);
			""");
	}

	public User findUserByUsername(String username)
		throws DataAccessException
	{
		String userQuery = """
			SELECT username, password, email_address, phone_number, role, weight_goal, activity_level, male, weight, dob, height
			FROM User
			WHERE username = ?;
			""";

		List<User> users = this.jdbc.query(
			userQuery,
			(rs, rowNum) -> new User(
				rs.getString("username"),
				rs.getString("password"),
				rs.getString("email_address"),
				rs.getString("phone_number"),
				Role.values()[rs.getInt("role")],
				WeightGoal.values()[rs.getInt("weight_goal")],
				ActivityLevel.values()[rs.getInt("activity_level")],
				rs.getBoolean("male"),
				rs.getInt("weight"),
				rs.getDate("dob").toLocalDate(),
				rs.getInt("height")
			),
			username
		);

		if (users.isEmpty())
			return null;

		return users.getFirst();
	}

	public void addUser(User user)
		throws DataAccessException
	{
		String sql = """
            INSERT INTO User(username, password, email_address, phone_number, role, weight_goal, activity_level, male, weight, dob, height)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;

		this.jdbc.update(
			sql,
			user.getUsername(),
			user.getPassword(),
			user.getEmailAddress(),
			user.getPhoneNumber(),
			user.getRole().ordinal(),
			user.getWeightGoal().ordinal(),
			user.getActivityLevel().ordinal(),
			user.isMale(),
			user.getWeight(),
			user.getDob(),
			user.getHeight()
		);
	}

	public Recipe findRecipeById(int id)
		throws DataAccessException
	{
		String recipeQuery = """
            SELECT id, week, title, description, instructions, time
            FROM Recipe
            WHERE id = ?;
            """;

		List<Recipe> recipes = this.jdbc.query(
			recipeQuery,
			(rs, rowNum) -> new Recipe(
				rs.getInt("id"),
				rs.getInt("week"),
				rs.getString("title"),
				rs.getString("description"),
				null,
				rs.getString("instructions"),
				rs.getString("time")
			),
			id
		);

		if (recipes.isEmpty())
			return null;

		Recipe recipe = recipes.getFirst();
		List<Ingredient> ingredients = this.findIngredientsForRecipe(recipe.getId());
		recipe.setIngredients(ingredients);

		return recipe;
	}

	public List<Recipe> findRecipesForWeek(int weekIndex)
		throws DataAccessException
	{
		String recipeQuery = """
            SELECT id, week, title, description, instructions, time
            FROM Recipe
            WHERE week = ?;
            """;

		List<Recipe> recipes = this.jdbc.query(
			recipeQuery,
			(rs, rowNum) -> new Recipe(
				rs.getInt("id"),
				rs.getInt("week"),
				rs.getString("title"),
				rs.getString("description"),
				null,
				rs.getString("instructions"),
				rs.getString("time")
			),
			weekIndex
		);

		for (Recipe recipe : recipes) {
			List<Ingredient> ingredients = this.findIngredientsForRecipe(recipe.getId());
			recipe.setIngredients(ingredients);
		}

		return recipes;
	}

	private List<Ingredient> findIngredientsForRecipe(int recipeId)
		throws DataAccessException
	{
		String ingredientQuery = """
            SELECT id, recipe, name, quantity, unit, calories
            FROM Ingredient
            WHERE recipe = ?;
            """;

		return this.jdbc.query(
			ingredientQuery,
			(rs, rowNum) -> new Ingredient(
				rs.getInt("id"),
				rs.getString("name"),
				rs.getDouble("quantity"),
				rs.getString("unit"),
				rs.getDouble("calories")
			),
			recipeId
		);
	}

	public void addDay(Day day)
		throws DataAccessException
	{
		String sql = """
            INSERT INTO Day(date, username, breakfast, lunch, dinner)
            VALUES (?, ?, ?, ?, ?);
            """;

		Integer breakfastId = null;
		Integer lunchId = null;
		Integer dinnerId = null;

		if (day.getBreakfast() != null) breakfastId = day.getBreakfast().getId();
		if (day.getLunch() != null) lunchId = day.getLunch().getId();
		if (day.getDinner() != null) dinnerId = day.getDinner().getId();

		this.jdbc.update(
			sql,
			day.getDate(),
			day.getUsername(),
			breakfastId,
			lunchId,
			dinnerId
		);
	}

	public void updateDay(Day day)
		throws DataAccessException
	{
		String sql = """
            UPDATE Day
            SET breakfast = ?, lunch = ?, dinner = ?
            WHERE date = ? and username = ?;
            """;

		Integer breakfastId = null;
		Integer lunchId = null;
		Integer dinnerId = null;

		if (day.getBreakfast() != null) breakfastId = day.getBreakfast().getId();
		if (day.getLunch() != null) lunchId = day.getLunch().getId();
		if (day.getDinner() != null) dinnerId = day.getDinner().getId();

		this.jdbc.update(
			sql,
			breakfastId,
			lunchId,
			dinnerId,
			day.getDate(),
			day.getUsername()
		);
	}

	public Day findDay(String username, LocalDate date)
		throws DataAccessException
	{
		String sql = """
            SELECT date, username, breakfast, lunch, dinner
            FROM Day
            WHERE date = ? and username = ?;
            """;

		List<Day> days = this.jdbc.query(
			sql,
			(rs, rowNum) -> new Day(
				rs.getDate("date").toLocalDate(),
				rs.getString("username"),
				this.findRecipeById(rs.getInt("breakfast")),
				this.findRecipeById(rs.getInt("lunch")),
				this.findRecipeById(rs.getInt("dinner"))
			),
			date,
			username
		);

		if (days.isEmpty())
			return null;

		return days.getFirst();
	}

	public byte[] findRecipeImage(int recipeId)
		throws DataAccessException
	{
		String sql = """
            SELECT bytes
            FROM RecipeImage
            WHERE recipe = ?;
            """;

		List<byte[]> bytesList = this.jdbc.query(
			sql,
			(rs, rowNum) -> {
				try {
					return rs.getBlob("bytes").getBinaryStream().readAllBytes();
				} catch (IOException e) {
					return null;
				}
			},
			recipeId
		);

		if (bytesList.isEmpty())
			return null;

		return bytesList.getFirst();
	}

	public void addRecipeImage(int recipeId, byte[] imageBytes)
		throws DataAccessException
	{
		String sql = """
            INSERT INTO RecipeImage(recipe, bytes)
            VALUES (?, ?);
            """;

		this.jdbc.update(
			sql,
			recipeId,
			imageBytes
		);
	}
}
