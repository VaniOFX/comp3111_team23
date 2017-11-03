package com.example.bot.spring;


import java.util.ArrayList;
import java.util.Date;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat; 

public class MessageHandler {
		
	private StorageEngine database;
	private Customer customer;
	private Tour tour;
	private TourBooking booking;
	private boolean cusNulls=true, bookNulls=true;
	private Date date;
	
	public MessageHandler() {
		database = new PSQLDatabaseEngine();
		customer = new Customer(null,null,0,0);
		tour = new Tour(null,null,0,null,null,null,0,0);
		booking = new TourBooking(tour, customer);
	}
	
	
	public String handleTextContent(ArrayList<String> inputArray){
		String intent = inputArray.get(0).toLowerCase();
		
		//return default string if not found
		String answer = "Excuse me I cannot understand what you are trying to say. Could you try again?";
		
		
		if(intent.length()>=8 && intent.substring(intent.length()-8).toLowerCase().equals("question")){
			//get answer from the FAQ table in the database
			answer = getAsnwer(inputArray.get(0).substring(0,intent.length() - 8));
		}
		else if(intent.toLowerCase().equals("booktour")){
			try {
				answer = handleBookingIntent(inputArray);
			} catch (Exception e) {
				answer = answer + "Make sure you spell the tour name and the date right (the date has to be in the following form yyyy-mm-dd)";
				date = null;
			}
		}
		else if(intent.toLowerCase().equals("confirmation")){
			answer = completeBooking();
		}
		else if (intent.equals("greeting")) { 
			answer = "Hello, how can I help you today?";
		}
				
		return answer;
	}
	
	public void setCustomer(String userId){
		if(customer.getId()  == null){
			customer.setId(userId);
			try {
				customer = database.getCustomerDetails(customer.getId());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private String handleBookingIntent(ArrayList<String> inputArray) throws Exception {
		
		String[] currentAttribute;
		for(int i = 1; i < inputArray.size(); i++){
			//separate the attribute name and attribute value
			currentAttribute = inputArray.get(i).split(":");
			if(customer.nullValues().size() > 0 && checkBelongToCustomer(currentAttribute))
				continue;
			else if(checkBelongToBooking(currentAttribute)) 
				continue;
		}
		
		//default string in case of insufficient amount of attributes
		String answer = "Please provide more details about the tour and the people going, in the following format:";
		
		appendNullAttributes(booking,answer,cusNulls);
		appendNullAttributes(customer,answer,bookNulls);
		
		//if no more attributes needed ask for confirmation
		if(!cusNulls && !bookNulls) 
			answer = "Are you sure you want to make this booking? Press Y";			
		
		return answer;
	}



	private String completeBooking() {
		//default answer in case something goes wrong
		String answer = "Something went wrong.Sorry for the inconvenience, could you please provide us with all the details again."; 
		
		if(!cusNulls && !bookNulls) {
			try {
				database.addCustomer(customer);
				database.addBooking(booking);
				answer = "Your booking is complete";
			} catch (URISyntaxException e) {
				answer = "Sorry I could not complete the booking. The server is not working, please try again later";
			}
		}
		resetHandler();
		return answer;
	}


	private boolean checkBelongToCustomer(String[] attributes) {
		boolean successful = false;
		
		//TODO: input validate everything 
		
		switch(attributes[0]){
			case "builtin.encyclopedia.people.person":
				customer.setName(attributes[1]);
				successful = true;
				break;
			case "builtin.age":
				customer.setAge(Integer.parseInt(attributes[1]));
				successful = true;
				break;
		}
		return successful;
	}


	private boolean checkBelongToBooking(String[] attributes) throws Exception {
		boolean successful = false;
		
		//TODO: input validate everything 
		
		switch(attributes[0]){
			case "numberOfAdults":
				booking.setAdultsNumber(Integer.parseInt(attributes[1]));
				successful = true;
				break;
			case "numberOfChildren":
				booking.setChildrenNumber(Integer.parseInt(attributes[1]));
				successful = true;
				break;
			case "numberOfToddlers":
				booking.setToddlersNumber(Integer.parseInt(attributes[1]));
				successful = true;
				break;
			case "tourType":
				tour.setId(database.getGeneralTourDetails(attributes[1]).getId());
				setTour();
				break;
			case "builtin.datetimeV2.date":
				date = new SimpleDateFormat("yyyy-mm-dd").parse(attributes[1]);
				setTour();
				break;
				
			/*case "specialRequests":
				booking.setSpecialRequests(attributes[1]);
				successful = true;
				break;*/
		}
		return successful;
	}

	private void setTour() throws Exception{
		if(tour == null && date != null & tour.getId() != null)
			tour = database.getTourDetails(tour.getId(), date);
	}

	private String getAsnwer(String question) {
		String answer = "Excuse me, I do not have an answer for your question." +
							"We have sent it to our staff. Please wait for them to respond";
		try {
			answer = database.getFAQResponse(question);
		} catch (Exception e) {
			database.logQuestion(question);
		}
		return answer;
	}
	

	private void resetHandler(){
		customer = new Customer(null,null,0,0);
		tour = new Tour(null,null,0,null,null,null,0,0);
		booking = new TourBooking(tour, customer);
		bookNulls = true;
		cusNulls = true;
	}
	
	private void appendNullAttributes(Object object, String str, boolean containNulls){
		ArrayList<String> nulls;
		if(object.getClass().equals(Customer.class)){
			nulls = ((Customer) object).nullValues();
		}else if(object.getClass().equals(TourBooking.class)){
			nulls = ((TourBooking) object).nullValues();
		}else
			return;
		if(nulls.size()>0){
			for(String s: nulls){
				str = str + "\n" + s;
			}
		}else
			containNulls = false;		
	}
}
