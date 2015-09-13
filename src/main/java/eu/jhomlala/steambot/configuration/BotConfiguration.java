package eu.jhomlala.steambot.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BotConfiguration {

	private String botname;
	private String steamUserId;
	private String steamPassword;
	private String steamGuardCode;
	private String steamAPIKey;
	private String db_host;
	private String db_name;
	private String db_userid;
	private String db_password;
	private int friendMessagesCacheSize = 10000;
	private String version;
	private String author;
	private String release_date;

	private static BotConfiguration config = new BotConfiguration();
	
	public static BotConfiguration getInstance() { 
		return config;
	}
	
	public BotConfiguration() {
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream("mvmlobbybot.properties");

			// load a properties file
			prop.load(input);

			// required
			this.botname = prop.getProperty("botname");
			this.steamUserId = prop.getProperty("steam_userid");
			this.steamPassword = prop.getProperty("steam_password");
			this.steamGuardCode = prop.getProperty("steam_guard_code");
			this.steamAPIKey = prop.getProperty("steam_api_key");
			this.version = prop.getProperty("version");
			this.author = prop.getProperty("author");
			this.release_date = prop.getProperty("release_date");
			
			// optional
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

	public int getFriendMessagesCacheSize() {
		return friendMessagesCacheSize;
	}

	public void setFriendMessagesCacheSize(int friendMessagesCacheSize) {
		this.friendMessagesCacheSize = friendMessagesCacheSize;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getRelease_date() {
		return release_date;
	}

	public void setRelease_date(String release_date) {
		this.release_date = release_date;
	}

}
