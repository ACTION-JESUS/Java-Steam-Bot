package eu.jhomlala.steambot.controllers;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.SteamFriends;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.types.Friend;
import eu.jhomlala.steambot.utils.Log;

public class FriendListController {

	private Map<Long,Friend> friendList;	// <accountid,Friend>
	private Date lastUpdate;
	private Logger log;
	
	public FriendListController()
	{
		friendList = new Hashtable<Long,Friend>();
		log = Log.getInstance();
	}
	
	public void updateFriendList(SteamFriends steamfriends, Set<Friend> friends)
	{
		lastUpdate = new Date();
		String relationship = "";
		for (Friend friend: friends)
		{
			
			switch (friend.getRelationship()) {
			case None:
				// Player removed this bot as a friend
				relationship = "None";
				deleteFriend(friend);
				break;
				
			case Blocked:
				// Player blocked or removed the friend invitation
				relationship = "Blocked";
				steamfriends.removeFriend(friend.getSteamId());
				deleteFriend(friend);
				break;
				
			case PendingInvitee:
				relationship = "PendingInvitee";
				break;
				
			case RequestRecipient:
				// Player sent a friend invitation.  Adding the friend will trigger the "Friend" relationship on success.
				relationship = "RequestRecipient";
				steamfriends.addFriend(friend.getSteamId());
				break;
				
			case RequestInitiator:
				relationship = "RequestInitiator";
				break;
				
			case PendingInviter:
				relationship = "PendingInviter";
				break;
				
			case Friend:
				// Friend relationship confirmed
				relationship = "Friend";
				addFriend(friend);
				break;
				
			case Ignored:
				relationship = "Ignored";
				break;
				
			case IgnoredFriend:
				relationship = "IgnoredFriend";
				break;
				
			case SuggestedFriend:
				relationship = "SuggestedFriend";
				break;
				
			}
			log.info("Player " + friend.getSteamId().toString() + " changed the relationship to " + relationship);
		}
	}
	
	public void addFriend(Friend friend)
	{
		friendList.put(friend.getSteamId().getAccountID(), friend);
		log.info("Added friend : " + friend.getSteamId());
	}
	
	public void deleteFriend(Friend friend)
	{
		friendList.remove(friend.getSteamId().getAccountID());
		log.info("Deleted friend " + friend.getSteamId());
	}

	public Map<Long,Friend> getFriendList() {
		return friendList;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}
	
	
	
}
