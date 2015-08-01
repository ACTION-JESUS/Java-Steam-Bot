package eu.jhomlala.steambot.main;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;




import uk.co.thomasc.steamkit.base.generated.steamlanguage.EChatEntryType;
import uk.co.thomasc.steamkit.base.generated.steamlanguage.EPersonaState;
import uk.co.thomasc.steamkit.base.generated.steamlanguage.EResult;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.SteamFriends;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.FriendMsgCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.FriendsListCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.PersonaStateCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamtrading.SteamTrading;
import uk.co.thomasc.steamkit.steam3.handlers.steamtrading.callbacks.TradeProposedCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.SteamUser;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.LoggedOnCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.LoginKeyCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.types.LogOnDetails;
import uk.co.thomasc.steamkit.steam3.steamclient.SteamClient;
import uk.co.thomasc.steamkit.steam3.steamclient.callbackmgr.CallbackMsg;
import uk.co.thomasc.steamkit.steam3.steamclient.callbacks.ConnectedCallback;
import uk.co.thomasc.steamkit.steam3.webapi.WebAPI;
import uk.co.thomasc.steamkit.types.keyvalue.KeyValue;
import uk.co.thomasc.steamkit.types.steamid.SteamID;
import uk.co.thomasc.steamkit.util.KeyDictionary;
import uk.co.thomasc.steamkit.util.WebHelpers;
import uk.co.thomasc.steamkit.util.cSharp.ip.ProtocolType;
import uk.co.thomasc.steamkit.util.crypto.CryptoHelper;
import uk.co.thomasc.steamkit.util.crypto.RSACrypto;
import eu.jhomlala.steambot.configuration.BotConfiguration;
import eu.jhomlala.steambot.controllers.FriendChatController;
import eu.jhomlala.steambot.controllers.FriendListController;
import eu.jhomlala.steambot.exceptions.InvalidSteamBotConfigurationException;
import eu.jhomlala.steambot.utils.Log;
import java.util.Base64;
import java.util.Base64.Encoder;
public class SteamBot {
	
	private BotConfiguration botConfiguration;
	private SteamClient steamClient;
	private Logger log;
	private boolean isRunning;
	private BotThread botThread;
	private SteamFriends steamFriends;
	private SteamTrading steamTrading;
	private String sessionID;
	private String token;
	private String WebApiNounce;
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
		this.steamTrading = steamClient.getHandler(SteamTrading.class);
		this.sessionID = "";
		this.token = "";
		
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
			log.info("NOUNCE:"+loggedOnCallBack.getWebAPIUserNonce());
			this.WebApiNounce = loggedOnCallBack.getWebAPIUserNonce();
			
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
				if (loggedOnCallBack.getResult() == EResult.InvalidLoginAuthCode)
				{
					log.info("Current Steam Guard code has expired.");
				}
				else
				{
					log.info("Error code: "+loggedOnCallBack.getResult());
				}
			}
		}
		
	}
	
	public void onFriendsListCallback(CallbackMsg callbackReceived)
	{
		FriendsListCallback friendsListCallback = (FriendsListCallback) callbackReceived;
		friendListController.updateFriendList(friendsListCallback.getFriendList());		
	}
	
	public void onPersonaStateCallback(CallbackMsg callbackReceived)
	{
		PersonaStateCallback personaStateCallback = (PersonaStateCallback) callbackReceived;
		
	
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
	
	public void onTradeProposedCallback(CallbackMsg callback)
	{
		TradeProposedCallback tradeProposedCallback = (TradeProposedCallback) callback;
		SteamID steamID = tradeProposedCallback.getOtherClient();
		
		//sendMessage(steamID,EChatEntryType.ChatMsg,"I dont accept direct trades.Type !trade to start trading with me.");
		//steamTrading.cancelTrade(steamID);
		log.info("Accept trade request from"+steamID);
		steamTrading.trade(steamID);
	}
	
	
	boolean authenticate(LoginKeyCallback callback) throws Exception {
		
	
		  Base64.Encoder encoder64 = Base64.getUrlEncoder();
		log.info("Universe:"+steamClient.getConnectedUniverse());
		log.info("STEAMID:"+steamClient.getSteamId().convertToLong());
		
		int myuniqueid = callback.getUniqueId();
		Encoder en;
		String uniqueIdString = String.valueOf(myuniqueid);
		byte[] uniqueid = uniqueIdString.getBytes("UTF-8");
		String sessionID = encoder64.encodeToString(uniqueid);
		
		
		byte[] sessionKey = CryptoHelper.GenerateRandomBlock(32);
		byte[] cryptedSessionKey = null;
		
		
		RSACrypto rsa = new RSACrypto(KeyDictionary.getPublicKey(steamClient.getConnectedUniverse()));
		cryptedSessionKey = rsa.encrypt(sessionKey);
		
		byte[] loginKey = new byte[20];
		byte[] myLoginKey = this.WebApiNounce.getBytes("ASCII");
		
		System.out.println(myLoginKey.length); 
		System.arraycopy(myLoginKey,0,loginKey,0,myLoginKey.length);
		
		byte[] cryptedLoginKey = CryptoHelper.SymmetricEncrypt(loginKey,sessionKey);
		
		
		String urlParameters="steamid="+getSteamClient().getSteamId().convertToLong()+"&sessionkey="+WebHelpers.UrlEncode(cryptedSessionKey)+"&encrypted_loginkey="+WebHelpers.UrlEncode(cryptedLoginKey)+"&format=vdf";
		
		
		final WebAPI userAuth = new WebAPI("ISteamUserAuth", "FD6AD4442ED27719F2A42B697B040EF9");
		userAuth.KeyValueAuthenticate(urlParameters);
		
	
		
		
		KeyValue authResult;

		
		
		
		
		return true;
	}


	public void onLoginKeyCallback(CallbackMsg callback) {
		
		LoginKeyCallback loginKeyCallback = (LoginKeyCallback) callback;
		log.info("Account authenticating.");
		while(true)
		{
			try {
				if (authenticate(loginKeyCallback))
				{
					log.info("Account authenticated.");
					break;
				}
				else
				{
					try {
					    // to sleep 10 seconds
					    Thread.sleep(2000);
					} catch (InterruptedException e) {
					    // recommended because catching InterruptedException clears interrupt flag
					    Thread.currentThread().interrupt();
					    // you probably want to quit if the thread is interrupted
					    return;
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
}
