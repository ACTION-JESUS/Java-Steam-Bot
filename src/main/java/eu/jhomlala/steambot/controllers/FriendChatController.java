package eu.jhomlala.steambot.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.FriendMsgCallback;
import eu.jhomlala.steambot.controllers.models.SteamChat;
import eu.jhomlala.steambot.main.SteamBot;
import eu.jhomlala.steambot.utils.Log;

public class FriendChatController {

	private SteamBot steamBot;
	private List<SteamChat> messageCache;
	private int messageCacheSize;
	private Logger log;
	public FriendChatController(SteamBot steamBot) {
		super();
		this.steamBot = steamBot;
		this.messageCache = new ArrayList<SteamChat>();
		this.messageCacheSize = steamBot.getBotConfiguration()
				.getFriendMessagesCacheSize();
		this.log = Log.getInstance();
	}

	public void handleChat(FriendMsgCallback callback) {
		
		log.info("Message from "+callback.getSender() +" :"+callback.getMessage());
		if (messageCache.size() <= messageCacheSize) {

			SteamChat steamChat = new SteamChat(new Date(),
					callback.getSender(), callback.getMessage());
			messageCache.add(steamChat);
			
		}
		
		sendResponse(callback);

	}

	private void sendResponse(FriendMsgCallback callback) {
		
		String message = "Hello!";
		steamBot.sendMessage(callback.getSender(),callback.getEntryType(),message);
		
	}
	
	

}
