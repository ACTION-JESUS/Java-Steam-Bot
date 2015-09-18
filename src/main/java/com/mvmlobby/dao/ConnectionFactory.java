package com.mvmlobby.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import eu.jhomlala.steambot.configuration.BotConfiguration;

public class ConnectionFactory {
	// static reference to itself
	private static ConnectionFactory instance = new ConnectionFactory();
	public static String url;
	public static String userid;
	public static String password;
	public static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";

	// private constructor
	private ConnectionFactory() {
		BotConfiguration config = BotConfiguration.getInstance();
		// url = "jdbc:mysql://localhost/mbmlobby"
		url = "jdbc:mysql://" + config.getDb_host() + "/" + config.getDb_name();
		userid = config.getDb_userid();
		password = config.getDb_password();
		
		try {
			Class.forName(DRIVER_CLASS);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private Connection createConnection() {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(url, userid, password);
		} catch (SQLException e) {
			System.out.println("ERROR: Unable to Connect to Database.");
		}
		return connection;
	}

	public static Connection getConnection() {
		return instance.createConnection();
	}
}
