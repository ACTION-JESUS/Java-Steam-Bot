package eu.jhomlala.steambot.exceptions;

public class InvalidSteamBotConfigurationException extends Exception {

	private String message;
	
	
	public InvalidSteamBotConfigurationException(String message) {
		super();
		this.message = message;
	}


	@Override
	public String toString() {
		return "InvalidSteamBotConfigurationException: "+message;
	}

	
}
