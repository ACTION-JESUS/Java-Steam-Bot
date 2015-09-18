package eu.jhomlala.steambot.main;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;
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
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.types.Friend;
import uk.co.thomasc.steamkit.steam3.handlers.steamtrading.callbacks.TradeProposedCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.SteamUser;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.LoggedOffCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.LoggedOnCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.LoginKeyCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.UpdateMachineAuthCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.types.LogOnDetails;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.types.MachineAuthDetails;
import uk.co.thomasc.steamkit.steam3.steamclient.SteamClient;
import uk.co.thomasc.steamkit.steam3.steamclient.callbackmgr.CallbackMsg;
import uk.co.thomasc.steamkit.steam3.steamclient.callbackmgr.JobCallback;
import uk.co.thomasc.steamkit.steam3.steamclient.callbacks.ConnectedCallback;
import uk.co.thomasc.steamkit.steam3.steamclient.callbacks.DisconnectedCallback;
import uk.co.thomasc.steamkit.steam3.webapi.WebAPI;
import uk.co.thomasc.steamkit.types.JobID;
import uk.co.thomasc.steamkit.types.keyvalue.KeyValue;
import uk.co.thomasc.steamkit.types.steamid.SteamID;
import uk.co.thomasc.steamkit.util.KeyDictionary;
import uk.co.thomasc.steamkit.util.WebHelpers;
import uk.co.thomasc.steamkit.util.cSharp.ip.ProtocolType;
import uk.co.thomasc.steamkit.util.crypto.CryptoHelper;
import uk.co.thomasc.steamkit.util.crypto.RSACrypto;

import com.mvmlobby.dao.LobbySQL;
import com.mvmlobby.threads.DBNotificationChecker;

import eu.jhomlala.steambot.configuration.BotConfiguration;
import eu.jhomlala.steambot.controllers.FriendChatController;
import eu.jhomlala.steambot.controllers.FriendListController;
import eu.jhomlala.steambot.utils.Log;

public class SteamBot extends Thread implements DBNotificationChecker.NotifyUsers {

	private BotConfiguration botConfiguration;
	private SteamClient steamClient;
	private Logger log;
	private SteamFriends steamFriends;
	// private SteamTrading steamTrading;
	private SteamUser steamUser;
	private String WebApiNounce;
	private DBNotificationChecker notificationChecker;

	// Controllers:
	private FriendListController friendListController;
	private FriendChatController friendChatController;

	public SteamBot() {

		this.botConfiguration = BotConfiguration.getInstance();
		this.log = Log.getInstance();
		this.friendListController = new FriendListController();
		this.friendChatController = new FriendChatController(this);
		this.startSteamClient();
		
		this.start();
		
		notificationChecker = new DBNotificationChecker(this);
		notificationChecker.start();

	}
	
	private void startSteamClient() {
		log.info("startSteamClient - begin");
		this.steamClient = new SteamClient(ProtocolType.Tcp);
		this.steamClient.connect();
		this.steamFriends = steamClient.getHandler(SteamFriends.class);
//		this.steamTrading = steamClient.getHandler(SteamTrading.class);
		this.steamUser = steamClient.getHandler(SteamUser.class);		
		log.info("startSteamClient - end");
	}
	
	@Override
	public void run() {
		while(true)
		{
			if (steamClient != null) {
				CallbackMsg callback = steamClient.getCallback(true);
				if (callback != null  && !(callback instanceof PersonaStateCallback)) {	// too much spam from PersonaStateCallback
					log.info("Received callback: "+callback);
				}
				steamClient.waitForCallback(200);
				
				if (callback instanceof ConnectedCallback) {
					
					onConnectedCallback(callback);
					
				} else if (callback instanceof LoggedOnCallback) {
					
					onLoggedIn(callback);
					
				} else if (callback instanceof FriendsListCallback) {
					
					onFriendsListCallback(callback);
					
				} else if (callback instanceof ChatInviteCallback) {
					
					onChatInviteCallback(callback);
					
				} else if (callback instanceof FriendMsgCallback) {
					
					onFriendMessage(callback);
					
				} else if (callback instanceof PersonaStateCallback) {
					
					onPersonaStateCallback(callback);
					
				} else if (callback instanceof TradeProposedCallback) {
					
					onTradeProposedCallback(callback);
					
				} else if (callback instanceof LoginKeyCallback) {
					
					onLoginKeyCallback(callback);
					
				} else if (callback instanceof JobCallback) {
					
					onUpdateMachineAuth(callback);
					
				} else if (callback instanceof DisconnectedCallback || callback instanceof LoggedOffCallback) {
					
					restart();
					
				}
				
			}
		}
	}

	public void restart() {
		steamClient.disconnect();
		log.info("reconnecting to the steam client in 2 seconds");
		try {Thread.sleep(2000);} catch (Exception e) {};
		// steamClient.connect();
		startSteamClient();
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
		logOnDetails.username = botConfiguration.getSteamUserId();
		logOnDetails.password = botConfiguration.getSteamPassword();

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
				sentryFile.close();
			} catch (Exception e) {
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
		friendListController.updateFriendList(steamFriends, friendsListCallback.getFriendList());
	}

	public void onPersonaStateCallback(CallbackMsg callbackReceived) {
//		PersonaStateCallback personaStateCallback = (PersonaStateCallback) callbackReceived;
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
//		byte[] sentryHash;
		File sentryFile = new File("sentry.bin");
		if (!sentryFile.exists())
		{
			try {
				sentryFile.createNewFile();
				
			} catch (IOException e) {
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
			iSentryFile.close();
			
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
			log.warn("Error writing sentry file.");
		}
		
		
	}

	public void onFriendMessage(CallbackMsg callback) {
		FriendMsgCallback friendMsgCallback = (FriendMsgCallback) callback;
		friendChatController.handleChat(friendMsgCallback);

	}

	public void onTradeProposedCallback(CallbackMsg callback) {
//		TradeProposedCallback tradeProposedCallback = (TradeProposedCallback) callback;
//		SteamID steamID = tradeProposedCallback.getOtherClient();

		// sendMessage(steamID,EChatEntryType.ChatMsg,"I dont accept direct trades.Type !trade to start trading with me.");
		// steamTrading.cancelTrade(steamID);
//		log.info("Accept trade request from" + steamID);
//		steamTrading.trade(steamID);
	}

	private KeyValue authenticate(LoginKeyCallback callback) throws Exception {

//		Base64.Encoder encoder64 = Base64.getUrlEncoder();
		// log.info("Universe:"+steamClient.getConnectedUniverse());
		// log.info("STEAMID:"+steamClient.getSteamId().convertToLong());

//		int myuniqueid = callback.getUniqueId();
//		Encoder en;
//		String uniqueIdString = String.valueOf(myuniqueid);
//		byte[] uniqueid = uniqueIdString.getBytes("UTF-8");
//		String sessionID = encoder64.encodeToString(uniqueid);

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
				+ steamClient.getSteamId().convertToLong()
				+ "&sessionkey=" + WebHelpers.UrlEncode(cryptedSessionKey)
				+ "&encrypted_loginkey="
				+ WebHelpers.UrlEncode(cryptedLoginKey) + "&format=vdf";

		final String apiKey = botConfiguration.getSteamAPIKey();
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
			String teamMsg = "\n*** An error occurred creating your team on mvmlobby.com\n\n";
			try {
				if (new LobbySQL().insertPlayerLobbyId(senderSteamID, lobbyId)) {
					teamMsg = "\nClick here to set up a team and send invites:\nhttp://mvmlobby.com/teams.php\n\n";
				}
			} catch (Exception e) {
				log.info("An error occurred while running LobbySQL.insertPlayerLobbyId");
			}
			
			msg.append(teamMsg)
				.append("Or create a team on http://steamcommunity.com/groups/twocitiesveterans/events with this info:\n")
				.append("Connect with this console command:\n")
				.append("connect_lobby ").append(lobbyId.convertToLong()).append("\n")
			;

			sendMessage(senderSteamID, EChatEntryType.ChatMsg, msg.toString());

		}
	}

	public void sendNewTeamNotification(Long team_id, Long lobby_id, String player_name,
			String mission_name, String tour_name, String region_name,
			Integer slots_available, String mvm_group_name) {
		
		StringBuilder msg = new StringBuilder();
		msg
			.append("\n").append(player_name).append(" created a team:").append("\n")
			.append("Tour: ").append(tour_name).append("\n")
			.append("Mission: ").append(mission_name).append("\n")
			.append("Players needed: ").append(slots_available).append("\n")
			.append("Click here to join: steam://joinlobby/440/").append(lobby_id).append("\n")
			.append("Or here for more details on all teams\n")
			.append("http://mvmlobby.com/teams.php");
		
		Iterator<Map.Entry<Long,Friend>> i = friendListController.getFriendList().entrySet().iterator();
		while (i.hasNext()){
			Map.Entry<Long,Friend> entry = i.next();
			Friend f = entry.getValue();
			EPersonaState state = steamFriends.getFriendPersonaState(f.getSteamId());
			if (state == EPersonaState.Online || state == EPersonaState.LookingToPlay) {
				log.info(" - sending invitation to " + f.getSteamId().toString() + " for teamid " + team_id);
				steamFriends.sendChatMessage(f.getSteamId(), EChatEntryType.ChatMsg, msg.toString());
			}
		}
		
	}

}
