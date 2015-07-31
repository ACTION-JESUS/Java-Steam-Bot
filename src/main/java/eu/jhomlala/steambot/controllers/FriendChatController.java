package eu.jhomlala.steambot.controllers;
import eu.jhomlala.steambot.configuration.SteamBotConfiguration;
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
		if (callbackMessage.equals("!buying"))
		{
			
		}
		if(callbackMessage.equals("!selling"))
		{
			
		}
		if(callbackMessage.equals("!message"))
		{
			
		}
		if(callbackMessage.equals("!about"))
		{
			returnMessage = "\nJava SteamBot"
					+"\n----------------------------------------"
					+"\nVersion:" +SteamBotConfiguration.currentBotVersion
					+"\nAuthor:" + SteamBotConfiguration.authorName
					+"\nRelease date: "+SteamBotConfiguration.relaseDate
					+"\n----------------------------------------";
			
		}
		if (callbackMessage.equals("!help"))
		{
			returnMessage = getCommandList();
		}
		if (returnMessage == null)
		{
			returnMessage = "I dont know this command.To check available commands, please"
					+" write: !help";
		}
		
		
		steamBot.sendMessage(callback.getSender(),callback.getEntryType(),returnMessage);
		
	}
	
	private String getCommandList()
	{
		String commandList = "\nCommand List:"
							+"\n----------------------------------------------------"
							+"\n!buying - show my buy offers"
							+"\n!selling - show my seling offers"
							+"\n!message - leave message for bot owner"
							+"\n!about - show info about bot"
							+"\n!help - show help"
							+"\n-----------------------------------------------------";
		return commandList;
	}
	
	public List<SteamChat> getAllMessages()
	{
		return messageCache;
	}

}
