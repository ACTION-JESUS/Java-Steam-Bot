package eu.jhomlala.steambot.main;

import org.apache.log4j.Logger;

import uk.co.thomasc.steamkit.base.generated.steamlanguage.EChatEntryType;
import uk.co.thomasc.steamkit.base.generated.steamlanguage.EPersonaState;
import uk.co.thomasc.steamkit.base.generated.steamlanguage.EResult;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.SteamFriends;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.FriendMsgCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.FriendsListCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.SteamUser;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.LoggedOnCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.types.LogOnDetails;
import uk.co.thomasc.steamkit.steam3.steamclient.SteamClient;
import uk.co.thomasc.steamkit.steam3.steamclient.callbackmgr.CallbackMsg;
import uk.co.thomasc.steamkit.steam3.steamclient.callbacks.ConnectedCallback;
import uk.co.thomasc.steamkit.types.steamid.SteamID;
import uk.co.thomasc.steamkit.util.cSharp.ip.ProtocolType;
import eu.jhomlala.steambot.configuration.BotConfiguration;
import eu.jhomlala.steambot.controllers.FriendChatController;
import eu.jhomlala.steambot.controllers.FriendListController;
import eu.jhomlala.steambot.exceptions.InvalidSteamBotConfigurationException;
import eu.jhomlala.steambot.utils.Log;


public class SteamBot {
	
	private BotConfiguration botConfiguration;
	private SteamClient steamClient;
	private Logger log;
	private boolean isRunning;
	private BotThread botThread;
	private SteamFriends steamFriends;
	//Controllers:
	private FriendListController friendListController;
	private FriendChatController friendChatController;
	
	
	public SteamBot(BotConfiguration botConfiguration) throws InvalidSteamBotConfigurationException {
		
		if(botConfiguration == null)
			throw new InvalidSteamBotConfigurationException("Configuration is empty.");
		
		this.botConfiguration = botConfiguration;
		this.log = Log.getInstance();
		this.steamClient = new SteamClient(ProtocolType.Tcp);
		this.isRunning = false;
		this.botThread = new BotThread(this);
		this.steamClient.connect();
		this.friendListController = new FriendListController();
		this.friendChatController = new FriendChatController(this);
		this.steamFriends = steamClient.getHandler(SteamFriends.class);
		
	}
	
	
	public void start()
	{
		log.info("Bot Thread started.");
		botThread.setRunning(true);
		if (botThread.isStarted() == false)
		{
			botThread.start();
			botThread.setStarted(true);
		}
		
		
	}

	
	public void stop()
	{
		log.info("Bot thread stopped.");
		botThread.setRunning(false);
	}
	
	public SteamClient getSteamClient() {
		return steamClient;
	}

	public BotConfiguration getBotConfiguration() {
		return botConfiguration;
	}

	public BotThread getBotThread() {
		return botThread;
	}

	public void setBotThread(BotThread botThread) {
		this.botThread = botThread;
	}
	
	public void onConnectedCallback(CallbackMsg callbackReceived)
	{
		ConnectedCallback connectedCallback = (ConnectedCallback) callbackReceived;
		
		if (connectedCallback.getResult() == EResult.OK)
		{
			log.info("Connected to steam server.");
		}
		else
		{
			log.info("Cant connect to steam server.");
			return;
		}
		
		LogOnDetails logOnDetails = new LogOnDetails();
		logOnDetails.username = botConfiguration.getUsername();
		logOnDetails.password = botConfiguration.getPassword();
		
		if (botConfiguration.getSteamGuardCode() !=null)
			logOnDetails.authCode = botConfiguration.getSteamGuardCode();
		
		log.info("Logging into steam account: login:"+logOnDetails.username);
		
		SteamUser steamUser = steamClient.getHandler(SteamUser.class);
		steamUser.logOn(logOnDetails);
	
	}
	
	public void onLoggedIn(CallbackMsg callback)
	{
		LoggedOnCallback loggedOnCallBack = (LoggedOnCallback) callback;
		if (loggedOnCallBack.getResult() == EResult.OK)
		{
			log.info("Logged into steam network.");
			log.info("Account STEAMID:"+ loggedOnCallBack.getClientSteamID());
			setPersonaState(EPersonaState.Online);
			
		}
		else
		{
			log.info("Login failed.");
			
			if (loggedOnCallBack.getResult() == EResult.AccountLogonDenied)
			{
				log.info("Please specify Steam Guard code in bot configuration.");
			}
			else
			{
				log.info("Error code: "+loggedOnCallBack.getResult());
			}
		}
		
	}
	
	public void onFriendsListCallback(CallbackMsg callbackReceived)
	{
		FriendsListCallback friendsListCallback = (FriendsListCallback) callbackReceived;
		friendListController.updateFriendList(friendsListCallback.getFriendList());		
	}
	
	public void setPersonaState(EPersonaState state)
	{
		if (state == null) throw new IllegalArgumentException("State is empty");
		steamFriends.setPersonaState(state);
	}


	public void sendMessage(SteamID sender, EChatEntryType type, String message) {
		steamFriends.sendChatMessage(sender, type, message);
		
	}


	public void onFriendMessage(CallbackMsg callback) {
		FriendMsgCallback friendMsgCallback = (FriendMsgCallback) callback;
		friendChatController.handleChat(friendMsgCallback);
		
	}
	
}
