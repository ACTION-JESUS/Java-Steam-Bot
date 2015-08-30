package eu.jhomlala.steambot.main;

import org.apache.log4j.Logger;

import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.ChatInviteCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.FriendMsgCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.FriendsListCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.PersonaStateCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamtrading.callbacks.TradeProposedCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.LoggedOnCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.LoginKeyCallback;
import uk.co.thomasc.steamkit.steam3.steamclient.callbackmgr.CallbackMsg;
import uk.co.thomasc.steamkit.steam3.steamclient.callbackmgr.JobCallback;
import uk.co.thomasc.steamkit.steam3.steamclient.callbacks.ConnectedCallback;
import eu.jhomlala.steambot.utils.Log;

public class BotThread extends Thread{
	
	private SteamBot steamBot;
	private boolean isStarted;
	private boolean isRunning;
	private Logger log;
	
	public BotThread(SteamBot steamBot)
	{
		this.steamBot = steamBot;
		this.isStarted = false;
		this.isRunning = false;
		this.log = Log.getInstance();
		
	}
	
	public void run()
	{
	
		while(true)
		{
			
			if (isRunning)
			{
				CallbackMsg callback = steamBot.getSteamClient().getCallback(true);
				if (callback != null)
					log.info("Received callback: "+callback);
				// else
					// log.info("Debug: (is this it?)"+callback);
				// uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.ChatInviteCallback@4dae1613
				steamBot.getSteamClient().waitForCallback(1000);
				
				if (callback instanceof ConnectedCallback)
				{
					steamBot.onConnectedCallback(callback);
				}
				if (callback instanceof LoggedOnCallback)
				{
					steamBot.onLoggedIn(callback);
					
				}
				if (callback instanceof FriendsListCallback)
				{
					steamBot.onFriendsListCallback(callback);
				}
				if (callback instanceof ChatInviteCallback)
				{
					steamBot.onChatInviteCallback(callback);
				}
				if (callback instanceof FriendMsgCallback)
				{
					steamBot.onFriendMessage(callback);
				}
				if (callback instanceof PersonaStateCallback)
				{
					steamBot.onPersonaStateCallback(callback);
				}
				if (callback instanceof TradeProposedCallback)
				{
					steamBot.onTradeProposedCallback(callback);
				}
				if (callback instanceof LoginKeyCallback)
				{
					steamBot.onLoginKeyCallback(callback);
				}
				if (callback instanceof JobCallback)
				{
					steamBot.onUpdateMachineAuth(callback);
				}
				
			}
			else
			{
				try {
					sleep(1000);
					//System.out.println("WAIT");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public boolean isStarted() {
		return isStarted;
	}

	public void setStarted(boolean isStarted) {
		this.isStarted = isStarted;
	}


	
	

	
}
