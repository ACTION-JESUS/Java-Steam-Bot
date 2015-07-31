package eu.jhomlala.steambot.utils;

import org.apache.log4j.Logger;



public class Log {

	private static Logger logger = Logger.getLogger("");
	
	public static Logger getInstance()
	{
		return logger;	
	}

	
}
