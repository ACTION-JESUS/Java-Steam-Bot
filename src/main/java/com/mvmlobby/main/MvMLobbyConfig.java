package com.mvmlobby.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MvMLobbyConfig {

	private String botname;
	private String steamUserId;
	private String steamPassword;
	private String steamGuardCode;
	private String steamAPIKey;
	private String db_host;
	private String db_name;
	private String db_userid;
	private String db_password;

	private static MvMLobbyConfig config = new MvMLobbyConfig();
	
	public static MvMLobbyConfig getInstance() { 
		return config;
	}
	
	public MvMLobbyConfig() {
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream("mvmlobbybot.properties");

			// load a properties file
			prop.load(input);

			this.botname = prop.getProperty("botname");
			this.steamUserId = prop.getProperty("steam_userid");
			this.steamPassword = prop.getProperty("steam_password");
			this.steamGuardCode = prop.getProperty("steam_guard_code");
			this.steamAPIKey = prop.getProperty("steam_api_key");
			this.db_host = prop.getProperty("db_host");
			this.db_name = prop.getProperty("db_name");
			this.db_userid = prop.getProperty("db_userid");
			this.db_password = prop.getProperty("db_password");

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String getBotname() {
		return botname;
	}

	public void setBotname(String botname) {
		this.botname = botname;
	}

	public String getSteamUserId() {
		return steamUserId;
	}

	public void setSteamUserId(String steamUserId) {
		this.steamUserId = steamUserId;
	}

	public String getSteamPassword() {
		return steamPassword;
	}

	public void setSteamPassword(String steamPassword) {
		this.steamPassword = steamPassword;
	}

	public String getSteamGuardCode() {
		return steamGuardCode;
	}

	public void setSteamGuardCode(String steamGuardCode) {
		this.steamGuardCode = steamGuardCode;
	}

	public String getSteamAPIKey() {
		return steamAPIKey;
	}

	public void setSteamAPIKey(String steamAPIKey) {
		this.steamAPIKey = steamAPIKey;
	}
	
	public String getDb_host() {
		return db_host;
	}

	public void setDb_host(String db_host) {
		this.db_host = db_host;
	}

	public String getDb_name() {
		return db_name;
	}

	public void setDb_name(String db_name) {
		this.db_name = db_name;
	}

	public String getDb_userid() {
		return db_userid;
	}

	public void setDb_userid(String db_userid) {
		this.db_userid = db_userid;
	}

	public String getDb_password() {
		return db_password;
	}

	public void setDb_password(String db_password) {
		this.db_password = db_password;
	}

}
