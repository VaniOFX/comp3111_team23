package com.example.bot.spring;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoggerHandler implements EventHandler {

	private StorageEngine database = new PSQLDatabaseEngine();
	@Autowired
	private LineCommunicator lineCom;
	
	@Override
	public String handleEvent(ArrayList<String> inputArray) {
		database.logQuestion(inputArray.get(1));
		lineCom.pushCustomerNotification(new ArrayList<String>(Arrays.asList("U7284687917ae6c74fdca2ba21f055e78")), "Someone is asking: ");
		return MessageHandler.DEFAULTANSWER;
	}

}
