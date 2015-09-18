package eu.jhomlala.steambot.controllers;

import java.util.ArrayList;
import java.util.List;

import eu.jhomlala.steambot.configuration.BotConfiguration;
import eu.jhomlala.steambot.main.SteamBot;

public class FriendChat {

	private SteamBot steamBot;
	private List<String> messageCache;
	private int messageCacheSize;
	
	public FriendChat(SteamBot steamBot) {
		super();
		this.steamBot = steamBot;
		this.messageCache = new ArrayList<String>();
		this.messageCacheSize = BotConfiguration.getInstance().getFriendMessagesCacheSize();
	}
	
	public void handleChat()
	{
		
	}
	
	
	
}
