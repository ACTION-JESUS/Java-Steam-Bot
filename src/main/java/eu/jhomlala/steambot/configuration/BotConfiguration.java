package eu.jhomlala.steambot.configuration;

public class BotConfiguration {

	private String username;
	private String password;
	private String steamGuardCode;
	private boolean acceptFriendRequest = false;
	private boolean acceptTradeRequest = false;
	private int friendMessagesCacheSize;


	public static class Builder
	{
		private String username;
		private String password;
		private String steamGuardCode;
		private boolean acceptFriendRequest = false;
		private boolean acceptTradeRequest = false;
		private int friendMessagesCacheSize = 1000;
		public Builder setUsername(String username)
		{
			this.username = username;
			return this;
		}
		
		public Builder setPassword(String password)
		{
			this.password = password;
			return this;
		}
		
		public Builder setSteamGuardCode(String steamGuardCode)
		{
			this.steamGuardCode = steamGuardCode;
			return this;
		}
		
		public Builder setAcceptFriendRequest(boolean acceptFriendRequest)
		{
			this.acceptFriendRequest = acceptFriendRequest;
			return this;
		}
		
		public Builder setAcceptTradeRequest(boolean acceptTradeRequest)
		{
			this.acceptTradeRequest = acceptTradeRequest;
			return this;
		}
		public Builder setFriendMessagesCacheSize(int friendMessagesCacheSize)
		{
			this.friendMessagesCacheSize = friendMessagesCacheSize;
			return this;
		}
		
		public BotConfiguration build()
		{
			return new BotConfiguration(this);
		}
		
		
	}
	
	private BotConfiguration(Builder builder) {
		this.username = builder.username;
		this.password = builder.password;
		this.steamGuardCode = builder.steamGuardCode;
		this.acceptFriendRequest = builder.acceptFriendRequest;
		this.acceptTradeRequest = builder.acceptTradeRequest;
		this.friendMessagesCacheSize = builder.friendMessagesCacheSize;
	}
	
	
	public String getUsername() {
		return username;
	}



	public String getPassword() {
		return password;
	}

	public String getSteamGuardCode() {
		return steamGuardCode;
	}

	public boolean isAcceptFriendRequest() {
		return acceptFriendRequest;
	}

	public boolean isAcceptTradeRequest() {
		return acceptTradeRequest;
	}


	public int getFriendMessagesCacheSize() {
		return friendMessagesCacheSize;
	}
	
	
}
