package com.mvmlobby.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.HashMap;

import org.apache.log4j.Logger;

import eu.jhomlala.steambot.configuration.BotConfiguration;
import eu.jhomlala.steambot.utils.Log;

public class MvMLobbyDAOHelper {
	
	private static MvMLobbyDAOHelper instance = new MvMLobbyDAOHelper();
	private static String url;
	private static String userid;
	private static String password;
	private static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";
	private String GET_PLAYER_GROUPS_SQL;
	
	public static MvMLobbyDAOHelper getInstance() {
		return instance;
	}
	
	private MvMLobbyDAOHelper() {
		BotConfiguration config = BotConfiguration.getInstance();
		// url = "jdbc:mysql://localhost/mbmlobby"
		url = "jdbc:mysql://" + config.getDb_host() + "/" + config.getDb_name();
		userid = config.getDb_userid();
		password = config.getDb_password();
		
		// Set the SQL statement just once
		StringBuilder sqlSB = new StringBuilder();
		sqlSB
			.append("select mg.custom_url, mg.name ")
			.append("from player p ")
			.append("inner join player_group pg on pg.player_id = p.id ")
			.append("inner join mvm_group mg on mg.id = pg.mvm_group_id ")
			.append("where p.steamid = ?")
		;
		GET_PLAYER_GROUPS_SQL = sqlSB.toString();
		
		try {
			Class.forName(DRIVER_CLASS);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public HashMap<String,String> getPlayerMvMGroups(String steam64ID) {
		Logger log = Log.getInstance();
		Connection connection = null;
		PreparedStatement preparedStmt = null;
		ResultSet results = null;
		HashMap<String,String> groupHash = new HashMap<String,String>();

		try {
			connection = DriverManager.getConnection(url, userid, password);
			preparedStmt = connection.prepareStatement(GET_PLAYER_GROUPS_SQL);
			preparedStmt.setString(1, steam64ID);
			results = preparedStmt.executeQuery();

			while (results.next()) {
				groupHash.put(results.getString("custom_url"), results.getString("name"));
			}

			SQLWarning warning = preparedStmt.getWarnings();
			if (warning != null) {
				log.info(warning.getMessage());
			}

		} catch (SQLException e) {
			log.info("sql error: " + e.toString());
		} finally {
			if (results != null) {
				try {
					results.close();
				} catch (SQLException e) { log.info("SQL error: " + e.toString()); }
			}
			if (preparedStmt != null) {
				try {
					preparedStmt.close();
				} catch (SQLException e) { log.info("SQL error: " + e.toString()); }
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) { log.info("SQL error: " + e.toString()); }
			}
		}
		
		return groupHash;
	}
}
