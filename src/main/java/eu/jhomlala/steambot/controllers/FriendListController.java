package eu.jhomlala.steambot.controllers;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.types.Friend;
import uk.co.thomasc.steamkit.types.steamid.SteamID;
import eu.jhomlala.steambot.utils.Log;
public class FriendListController {

	private List<Friend> friendList;
	private Date lastUpdate;
	private Logger log;
	
	public FriendListController()
	{
		friendList = new ArrayList<Friend>();
		log = Log.getInstance();
	}
	
	public void updateFriendList(Set<Friend> friends)
	{
		int updateActions = 0;
		int addActions = 0;
		lastUpdate = new Date();
		for (Friend friend: friends)
		{
			if (isFriendInList(friend.getSteamId()))
			{
				updateFriend(friend);
				updateActions++;
			}
			else
			{
				addFriend(friend);
				addActions++;
			}
		}
		log.info("Friendlist update: Update actions: " +updateActions+" Add Actions: "+addActions);
		log.info("Friendlist update date: "+lastUpdate);
	}
	
	public void addFriend(Friend friend)
	{
		log.info(friend.getSteamId());
		friendList.add(friend);
	}
	
	public void updateFriend(Friend friend)
	{
		for (Friend friendFromList: friendList)
		{
			if (friendFromList.getSteamId().getAccountID() == friend.getSteamId().getAccountID())
			{
				friendList.remove(friendFromList);
				friendList.add(friend);
			}
		}
	}

	public boolean isFriendInList(SteamID steamID)
	{
		for (Friend friend: friendList)
		{
			if (friend.getSteamId().getAccountID() == steamID.getAccountID())
			{
				return true;
			}
		}
		return false;
	}
	public List<Friend> getFriendList() {
		return friendList;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}
	
	
	
}
