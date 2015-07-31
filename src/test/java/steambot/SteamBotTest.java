package steambot;

import eu.jhomlala.steambot.configuration.BotConfiguration;
import eu.jhomlala.steambot.exceptions.InvalidSteamBotConfigurationException;
import eu.jhomlala.steambot.main.SteamBot;

public class SteamBotTest {

	public static void main(String[] args) throws InvalidSteamBotConfigurationException {
	
		BotConfiguration config = new BotConfiguration.Builder().
										setUsername("").
										setPassword("").
										setSteamGuardCode("").
										build();
		
		final SteamBot steambot = new SteamBot(config);
		steambot.start();
		
		
	}

}
