package com.example.kostplan.repository;

import com.example.kostplan.entity.Role;
import com.example.kostplan.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
				role INTEGER NOT NULL,
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
				id INTEGER,
				week INTEGER,
				title TEXT,
				description TEXT,
				nutrition_info TEXT,
				instructions TEXT,
				time TEXT,
				PRIMARY KEY (id)
			);
			""");

		this.jdbc.execute("""
			CREATE TABLE IF NOT EXISTS Ingredient(
				id INTEGER,
				recipe INTEGER,
				name TEXT,
				quantity INTEGER,
				unit TEXT,
				calories INTEGER,
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

	public User getUserByUsername(String username)
		throws DataAccessException
	{
		String userQuery = """
		SELECT username, password, role, name, male, weight, dob, height
		FROM User
		WHERE username = ?;
		""";

		List<User> users = this.jdbc.query(
			userQuery,
			(rs, rowNum) -> new User(
				rs.getString("username"),
				rs.getString("password"),
				rs.getString("name"),
				Role.values()[rs.getInt("role")],
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
            INSERT INTO User(username, password, role, name, male, weight, dob, height)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?);
            """;

		this.jdbc.update(
			sql,
			user.getUsername(),
			user.getPassword(),
			user.getRole().ordinal(),
			user.getName(),
			user.isMale(),
			user.getWeight(),
			user.getDob(),
			user.getHeight()
		);
	}
}
