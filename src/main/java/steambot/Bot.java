package steambot;


import uk.co.thomasc.steamkit.base.generated.steamlanguage.EResult;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.SteamUser;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.LoggedOnCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.types.LogOnDetails;
import uk.co.thomasc.steamkit.steam3.steamclient.SteamClient;
import uk.co.thomasc.steamkit.steam3.steamclient.callbackmgr.CallbackMsg;
import uk.co.thomasc.steamkit.steam3.steamclient.callbacks.ConnectedCallback;
import uk.co.thomasc.steamkit.util.cSharp.ip.ProtocolType;

public class Bot {

	SteamClient client;
	SteamUser steamUser;
	public Bot()
	{

		
		client = new SteamClient(ProtocolType.Tcp);
		
		
		
		steamUser = client.getHandler(SteamUser.class);
		
		
		boolean isRunning = true;
		
		
		
		client.connect();
		while(isRunning)
		{
			
			
			CallbackMsg callback = client.getCallback(true);
			System.out.println(callback);
			if (callback instanceof ConnectedCallback)
			{
				onConnected(callback);
			}
			if (callback instanceof LoggedOnCallback)
			{
				onLoggedIn(callback);
			}
			
			client.waitForCallback(1000);
		}
		
		
		
	}
	
	public void onLoggedIn(CallbackMsg callback)
	{
		LoggedOnCallback loggedOnCallBack = (LoggedOnCallback) callback;
		if (loggedOnCallBack.getResult() == EResult.OK)
		{
			System.out.println("Logged into steam network.");
			System.out.println(loggedOnCallBack.getClientSteamID());
		}
		
		
	}
	
	public void onConnected(CallbackMsg callback)
	{
		ConnectedCallback connectedCallback = (ConnectedCallback) callback;
		
		if (connectedCallback.getResult() == EResult.OK)
		{
			System.out.println("Connected to Steam!");
		}
		else
		{
			System.out.println("Cant connect to Steam!");
			return;
		}
		String login = "khomlala3";
		String password = "pa55word2015";
		
		LogOnDetails logOnDetails = new LogOnDetails();
		logOnDetails.username = login;
		logOnDetails.password = password;
		
		System.out.println("Logging into steam account...");
		steamUser.logOn(logOnDetails);


}
}
