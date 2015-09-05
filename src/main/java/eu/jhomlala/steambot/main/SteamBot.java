/*
 * //
// Sample 5: SteamGuard
//
// this sample goes into detail for how to handle steamguard protected accounts and how to login to them
//
// SteamGuard works by enforcing a two factor authentication scheme
// upon first logon to an account with SG enabled, the steam server will email an authcode to the validated address of the account
// this authcode token can be used as the second factor during logon, but the token has a limited time span in which it is valid
//
// after a client logs on using the authcode, the steam server will generate a blob of random data that the client stores called a "sentry file"
// this sentry file is then used in all subsequent logons as the second factor
// ownership of this file provides proof that the machine being used to logon is owned by the client in question
//
// the usual login flow is thus:
// 1. connect to the server
// 2. logon to account with only username and password
// at this point, if the account is steamguard protected, the LoggedOnCallback will have a result of AccountLogonDenied
// the server will disconnect the client and email the authcode
//
// the login flow must then be restarted:
// 1. connect to server
// 2. logon to account using username, password, and authcode
// at this point, login wil succeed and a UpdateMachineAuthCallback callback will be posted with the sentry file data from the steam server
// the client will save the file, and reply to the server informing that it has accepted the sentry file
// 
// all subsequent logons will use this flow:
// 1. connect to server
// 2. logon to account using username, password, and sha-1 hash of the sentry file
 */

package eu.jhomlala.steambot.main;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Scanner;

import org.apache.log4j.Logger;

import uk.co.thomasc.steamkit.base.generated.steamlanguage.EChatEntryType;
import uk.co.thomasc.steamkit.base.generated.steamlanguage.EPersonaState;
import uk.co.thomasc.steamkit.base.generated.steamlanguage.EResult;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.SteamFriends;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.ChatInviteCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.FriendMsgCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.FriendsListCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.PersonaStateCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamtrading.SteamTrading;
import uk.co.thomasc.steamkit.steam3.handlers.steamtrading.callbacks.TradeProposedCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.SteamUser;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.LoggedOnCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.LoginKeyCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.UpdateMachineAuthCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.types.LogOnDetails;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.types.MachineAuthDetails;
import uk.co.thomasc.steamkit.steam3.steamclient.SteamClient;
import uk.co.thomasc.steamkit.steam3.steamclient.callbackmgr.CallbackMsg;
import uk.co.thomasc.steamkit.steam3.steamclient.callbackmgr.JobCallback;
import uk.co.thomasc.steamkit.steam3.steamclient.callbacks.ConnectedCallback;
import uk.co.thomasc.steamkit.steam3.webapi.WebAPI;
import uk.co.thomasc.steamkit.types.JobID;
import uk.co.thomasc.steamkit.types.keyvalue.KeyValue;
import uk.co.thomasc.steamkit.types.steamid.SteamID;
import uk.co.thomasc.steamkit.util.KeyDictionary;
import uk.co.thomasc.steamkit.util.WebHelpers;
import uk.co.thomasc.steamkit.util.cSharp.ip.ProtocolType;
import uk.co.thomasc.steamkit.util.crypto.CryptoHelper;
import uk.co.thomasc.steamkit.util.crypto.RSACrypto;

import com.mvmlobby.main.MvMLobbyConfig;

import eu.jhomlala.steambot.configuration.BotConfiguration;
import eu.jhomlala.steambot.controllers.FriendChatController;
import eu.jhomlala.steambot.controllers.FriendListController;
import eu.jhomlala.steambot.exceptions.InvalidSteamBotConfigurationException;
import eu.jhomlala.steambot.utils.Log;

public class SteamBot {

	private BotConfiguration botConfiguration;
	private SteamClient steamClient;
	private Logger log;
	// private boolean isRunning;
	private BotThread botThread;
	private SteamFriends steamFriends;
	private SteamTrading steamTrading;
	private SteamUser steamUser;
	// private String sessionID;
	// private String token;
	private String WebApiNounce;

	// Controllers:
	private FriendListController friendListController;
	private FriendChatController friendChatController;

	public SteamBot(BotConfiguration botConfiguration)
			throws InvalidSteamBotConfigurationException {

		if (botConfiguration == null)
			throw new InvalidSteamBotConfigurationException(
					"Configuration is empty.");

		this.botConfiguration = botConfiguration;
		this.log = Log.getInstance();
		this.steamClient = new SteamClient(ProtocolType.Tcp);
		// this.isRunning = false;

		this.steamClient.connect();
		this.friendListController = new FriendListController();
		this.friendChatController = new FriendChatController(this);
		this.steamFriends = steamClient.getHandler(SteamFriends.class);
		this.steamTrading = steamClient.getHandler(SteamTrading.class);
		this.steamUser = steamClient.getHandler(SteamUser.class);
		// this.sessionID = "";
		// this.token = "";

	}

	public void start() {
		log.info("Bot Thread started.");
		botThread = new BotThread(this);
		botThread.setRunning(true);
		if (botThread.isStarted() == false) {
			botThread.start();
			botThread.setStarted(true);
		}

	}

	public void restart() {
		steamClient.disconnect();
		stop();
		steamClient.connect();
		start();

	}

	public void stop() {
		log.info("Bot thread stopped.");
		botThread.setRunning(false);
		botThread = null;
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

	public void onConnectedCallback(CallbackMsg callbackReceived) {
		ConnectedCallback connectedCallback = (ConnectedCallback) callbackReceived;

		if (connectedCallback.getResult() == EResult.OK) {
			log.info("Connected to steam server.");
		} else {
			log.info("Cant connect to steam server.");
			return;
		}

		LogOnDetails logOnDetails = new LogOnDetails();
		logOnDetails.username = botConfiguration.getUsername();
		logOnDetails.password = botConfiguration.getPassword();

		if (botConfiguration.getSteamGuardCode() != null)
			logOnDetails.authCode = botConfiguration.getSteamGuardCode();
		
		byte[] sentryHash = null;
		File file = new File("sentry.bin");
		if (file.exists())
		{
			try {
				RandomAccessFile sentryFile = new RandomAccessFile(file,"rw");
				byte [] sentryFileContent = new byte[(int)sentryFile.length()];
				sentryFile.readFully(sentryFileContent);
				sentryHash = CryptoHelper.SHAHash(sentryFileContent);
				
				logOnDetails.sentryFileHash = sentryHash;
				log.info("Sentry file added to login details.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		log.info("Logging into steam account: login:" + logOnDetails.username);

		steamUser.logOn(logOnDetails);

	}

	public void onLoggedIn(CallbackMsg callback) {
		LoggedOnCallback loggedOnCallBack = (LoggedOnCallback) callback;
		if (loggedOnCallBack.getResult() == EResult.OK) {
			log.info("Logged into steam network.");
			// log.info("Account STEAMID:"+
			// loggedOnCallBack.getClientSteamID());
			setPersonaState(EPersonaState.Online);
			// log.info("NOUNCE:"+loggedOnCallBack.getWebAPIUserNonce());
			this.WebApiNounce = loggedOnCallBack.getWebAPIUserNonce();
		} else {
			log.info("Login failed.");

			if (loggedOnCallBack.getResult() == EResult.AccountLogonDenied) {
				log.info("This acount have Steam Guard.");
				log.info("Write steam guard code:");
				Scanner scanIn = new Scanner(System.in);
				String authCode = scanIn.nextLine();
				scanIn.close();

				this.botConfiguration.setSteamGuardCode(authCode);
				restart();

			} else {
				if (loggedOnCallBack.getResult() == EResult.InvalidLoginAuthCode) {
					log.info("Current Steam Guard code has expired.");
					log.info("Write steam guard code:");
					Scanner scanIn = new Scanner(System.in);
					String authCode = scanIn.nextLine();
					scanIn.close();

					this.botConfiguration.setSteamGuardCode(authCode);
					restart();
				} else {
					log.info("Error code: " + loggedOnCallBack.getResult());
				}
			}
		}

	}

	public void onFriendsListCallback(CallbackMsg callbackReceived) {
		FriendsListCallback friendsListCallback = (FriendsListCallback) callbackReceived;
		friendListController.updateFriendList(friendsListCallback
				.getFriendList());
	}

	public void onPersonaStateCallback(CallbackMsg callbackReceived) {
		PersonaStateCallback personaStateCallback = (PersonaStateCallback) callbackReceived;
	}

	public void setPersonaState(EPersonaState state) {
		if (state == null)
			throw new IllegalArgumentException("State is empty");
		steamFriends.setPersonaState(state);
	}

	public void sendMessage(SteamID sender, EChatEntryType type, String message) {
		steamFriends.sendChatMessage(sender, type, message);

	}

	// update sentry file
	public void onUpdateMachineAuth(CallbackMsg callback) {
		JobCallback call = (JobCallback) callback;
		
		JobID jobID = call.getJobId();
		
		log.info("Updating sentry file.");
		UpdateMachineAuthCallback updateMachineAuthCallback = (UpdateMachineAuthCallback) call.getCallback();
		byte[] sentryHash;
		File sentryFile = new File("sentry.bin");
		if (!sentryFile.exists())
		{
			try {
				sentryFile.createNewFile();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		int offset = updateMachineAuthCallback.getOffset();
		byte[] data = updateMachineAuthCallback.getData();
		int fileSize;
		try {
			RandomAccessFile iSentryFile = new RandomAccessFile(sentryFile,"rw");
			iSentryFile.seek(offset);
			iSentryFile.write(data);
			fileSize = (int) iSentryFile.length();
			
			iSentryFile.seek(0);
			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
			byte[] fileContent = new byte[fileSize];
			iSentryFile.readFully(fileContent);
			
			byte[] sentryFileSha1 = sha1.digest(fileContent);
			
			//send sentryfile to steam network...
			MachineAuthDetails details = new MachineAuthDetails();
			details.jobId = jobID.getValue();
			details.fileName = sentryFile.getName();
			details.bytesWritten = updateMachineAuthCallback.getBytesToWrite();
			details.fileSize = fileSize;
			details.offset = offset;
			details.result = EResult.OK;
			details.oneTimePassword = updateMachineAuthCallback.getOneTimePassword();
			details.sentryFileHash = sentryFileSha1;
			steamUser.sendMachineAuthResponse(details);
			
			log.info("MachineAuth Reponse has been send.");
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn("Error writing sentry file.");
		}
		
		
	}

	public void onFriendMessage(CallbackMsg callback) {
		FriendMsgCallback friendMsgCallback = (FriendMsgCallback) callback;
		friendChatController.handleChat(friendMsgCallback);

	}

	public void onTradeProposedCallback(CallbackMsg callback) {
		TradeProposedCallback tradeProposedCallback = (TradeProposedCallback) callback;
		SteamID steamID = tradeProposedCallback.getOtherClient();

		// sendMessage(steamID,EChatEntryType.ChatMsg,"I dont accept direct trades.Type !trade to start trading with me.");
		// steamTrading.cancelTrade(steamID);
		log.info("Accept trade request from" + steamID);
		steamTrading.trade(steamID);
	}

	private KeyValue authenticate(LoginKeyCallback callback) throws Exception {

		Base64.Encoder encoder64 = Base64.getUrlEncoder();
		// log.info("Universe:"+steamClient.getConnectedUniverse());
		// log.info("STEAMID:"+steamClient.getSteamId().convertToLong());

		int myuniqueid = callback.getUniqueId();
		Encoder en;
		String uniqueIdString = String.valueOf(myuniqueid);
		byte[] uniqueid = uniqueIdString.getBytes("UTF-8");
		String sessionID = encoder64.encodeToString(uniqueid);

		byte[] sessionKey = CryptoHelper.GenerateRandomBlock(32);
		byte[] cryptedSessionKey = null;

		RSACrypto rsa = new RSACrypto(KeyDictionary.getPublicKey(steamClient
				.getConnectedUniverse()));
		cryptedSessionKey = rsa.encrypt(sessionKey);

		byte[] loginKey = new byte[20];
		byte[] myLoginKey = this.WebApiNounce.getBytes("ASCII");

		System.arraycopy(myLoginKey, 0, loginKey, 0, myLoginKey.length);

		byte[] cryptedLoginKey = CryptoHelper.SymmetricEncrypt(loginKey,
				sessionKey);

		String urlParameters = "steamid="
				+ getSteamClient().getSteamId().convertToLong()
				+ "&sessionkey=" + WebHelpers.UrlEncode(cryptedSessionKey)
				+ "&encrypted_loginkey="
				+ WebHelpers.UrlEncode(cryptedLoginKey) + "&format=vdf";

		final String apiKey = MvMLobbyConfig.getInstance().getSteamAPIKey();
		final WebAPI userAuth = new WebAPI("ISteamUserAuth", apiKey);
		return userAuth.KeyValueAuthenticate(urlParameters);
	}

	public void onLoginKeyCallback(CallbackMsg callback) {

		LoginKeyCallback loginKeyCallback = (LoginKeyCallback) callback;
		log.info("Account authenticating.");
		while (true) {
			try {
				KeyValue authenticationRespond = authenticate(loginKeyCallback);
				log.info(authenticationRespond.get("token").asString());
				if (authenticationRespond != null) {
					log.info("Account authenticated.");
					
					// ACTION JESUS:  Correctly set the bot name instead of using the AccountCache default
					this.steamFriends.setPersonaName(botConfiguration.getBotname());

					break;
				} else {
					try {
						log.warn("Authentication failed.Retry in 2 seconds...");
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// recommended because catching InterruptedException
						// clears interrupt flag
						Thread.currentThread().interrupt();
						// you probably want to quit if the thread is
						// interrupted
						return;
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	
	
	// ChatInviteCallback
	public void onChatInviteCallback(CallbackMsg callback) {
		ChatInviteCallback chatInviteCallback = (ChatInviteCallback) callback;
		
		if (chatInviteCallback.getChatRoomType().toString().equals("Lobby")) {

			SteamID lobbyId =  chatInviteCallback.getChatRoomID();
			SteamID senderSteamID = chatInviteCallback.getFriendChatID();		// the person who sent the invite

			/*
			  Existing bot output:
			 
			 	Your lobby ID is:  109775242299196025

				Players can join your lobby with this URL:
				steam://joinlobby/440/109775242299196025
				
				Or connect via console with:
				connect_lobby 109775242299196025
				
				---- new message ------------------------------
				Connect with this console command:
				connect_lobby 109775242299241580
				
				Or join using this link:
				steam://joinlobby/440/109775242299241580
				
				Click here to set up a team and send invites:
				http://mvmlobby.com/teams.php

			 */
			StringBuilder msg = new StringBuilder();
			msg.append("\nConnect with this console command:\n")
				.append("connect_lobby ").append(lobbyId.convertToLong()).append("\n\n")
				.append("Or join using this link:\n")
				.append("steam://joinlobby/440/").append(lobbyId.convertToLong()).append("\n\n")
				.append("Create a Two Cities Veterans event here:\n")
				.append("http://steamcommunity.com/groups/twocitiesveterans/events")
			;

			sendMessage(senderSteamID, EChatEntryType.ChatMsg, msg.toString());

		}
	}

}
