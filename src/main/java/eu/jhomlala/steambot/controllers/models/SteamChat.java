package eu.jhomlala.steambot.controllers.models;

import java.util.Date;

import uk.co.thomasc.steamkit.types.steamid.SteamID;

public class SteamChat {

	private Date date;
	private SteamID steamID;
	private String message;
	public SteamChat(Date date, SteamID steamID, String message) {
		super();
		this.date = date;
		this.steamID = steamID;
		this.message = message;
	}
	public Date getDate() {
		return date;
	}
	public SteamID getSteamID() {
		return steamID;
	}
	public String getMessage() {
		return message;
	}
	
	
	
}
