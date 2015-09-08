package com.mvmlobby.main;

import eu.jhomlala.steambot.configuration.BotConfiguration;
import eu.jhomlala.steambot.exceptions.InvalidSteamBotConfigurationException;
import eu.jhomlala.steambot.main.SteamBot;


public class MvMLobbyBot {
	   
	public static void main(String[] args) {

		MvMLobbyConfig lobbyConfig = MvMLobbyConfig.getInstance();

		BotConfiguration.Builder builder = new BotConfiguration.Builder()
								.setAcceptFriendRequest(true)
								.setBotname(lobbyConfig.getBotname())
								.setUsername(lobbyConfig.getSteamUserId())
								.setPassword(lobbyConfig.getSteamPassword());
		
		if (lobbyConfig.getSteamGuardCode() != null && !lobbyConfig.getSteamGuardCode().equals("")) {
			builder.setSteamGuardCode(lobbyConfig.getSteamGuardCode());
		}
		
		BotConfiguration config = builder.build();
		
		try {
			SteamBot steamBot = new SteamBot(config);
			steamBot.start();
		} catch (InvalidSteamBotConfigurationException e) {
			System.out.println("SteamBot configuration is invalid");
		}

	}

}
