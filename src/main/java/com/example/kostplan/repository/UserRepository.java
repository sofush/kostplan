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
}
