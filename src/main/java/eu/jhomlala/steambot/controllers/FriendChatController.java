package eu.jhomlala.steambot.controllers;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.FriendMsgCallback;
import eu.jhomlala.steambot.configuration.BotConfiguration;
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
		this.messageCacheSize = BotConfiguration.getInstance().getFriendMessagesCacheSize();
		this.log = Log.getInstance();
	}

	public void handleChat(FriendMsgCallback callback) {
		
		//We dont handle empty messages
		if (callback.getMessage().length() == 0)
			return;
		
		log.info("Message from "+callback.getSender() +" :"+callback.getMessage());
		if (messageCache.size() <= messageCacheSize) {

			SteamChat steamChat = new SteamChat(new Date(),
					callback.getSender(), callback.getMessage());
			messageCache.add(steamChat);
			
		}
		
		sendResponse(callback);

	}

	private void sendResponse(FriendMsgCallback callback) {
		
		String callbackMessage = callback.getMessage();
		String returnMessage = null;

		if(callbackMessage.equals("about"))
		{
			BotConfiguration config = BotConfiguration.getInstance();
			returnMessage = "\nJava SteamBot"
					+"\n----------------------------------------"
					+"\nVersion:" +config.getVersion()
					+"\nAuthor:" + config.getAuthor()
					+"\nRelease date: "+config.getRelease_date()
					+"\n----------------------------------------";
			
		}

		if (returnMessage == null)
		{
			returnMessage = "You can chat with me but I don't do anything but give you your MvM lobby ID.";
		}
		
		
		steamBot.sendMessage(callback.getSender(),callback.getEntryType(),returnMessage);
		
	}
	
	public List<SteamChat> getAllMessages()
	{
		return messageCache;
	}

}
