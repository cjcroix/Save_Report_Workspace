package org.apache.jmeter.visualizers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.jmeter.util.JMeterUtils;

/**
 * This class is used to send an SMS notification at the end of the test.It
 * simultaneously sends the message to multiple carriers using the free email to
 * SMS gateways.
 * 
 * @author Save Report Project Group 2014
 * 
 */ 
class SaveReportSms implements Runnable {
	String mobileNumber;
	String carrierCode;
	String messageAddress;
	public static String testEndTime;
	String smtpServer;
	String authRequired;
	String authenticationType;
	String portNumber;
	String username;
	String password;

	SaveReportSms(String address) {
		messageAddress = address;
	}

	public SaveReportSms() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Reads the Mobile Number from the text file.
	 */
	private void getMobileNumber() {
		// TODO Auto-generated method stub
		char[] number = new char[10];
		File mobileNoFile = new File(JMeterUtils.getJMeterBinDir()
				+ File.separatorChar + "SaveReportMobileNumber.txt");
		try {
			FileReader reader = new FileReader(mobileNoFile);
			reader.read(number);
			if (number.toString() != "")
				mobileNumber = String.valueOf(number);
			reader.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This funtion is called by a SaveReport object to initialize an object of
	 * this class.
	 * 
	 * @param testEnded
	 */
	public void initialize(String testEnded) {
		getMobileNumber();
		testEndTime = testEnded;

		final String[] addressList = new String[] {
				mobileNumber + "@ideacellular.net"/*, // Idea goa/Maharashtra Idea
				// Cellular /Gujarat Idea
				// Cellular

				"91" + mobileNumber + "@airtelap.com",
				"919840" + mobileNumber + "@airtelchennai.com", // Chennai
				// Skycell /
				// Airtel
				"919810" + mobileNumber + "@airtelmail.com", // delhi airtel
				"919816" + mobileNumber + "@airtelmail.com", // Himachai Pradesh
				// Airtel
				"919845" + mobileNumber + "@airtelkk.com", // Karnataka Airtel
				"919895" + mobileNumber + "@airtelkerala.com", // Kerala Airtel
				"919890" + mobileNumber + "@airtelmail.com", // Goa Airtel
				"919896" + mobileNumber + "@airtelmail.com", // Haryana Airtel
				"919831" + mobileNumber + "@airtelkol.com", // Kolkata Airtel
				"919893" + mobileNumber + "@airtelmail.com",// Madhya Pradesh
				// Airtel
				"919815" + mobileNumber + "@airtelmail.com", // Punjab Airtel
				"919894" + mobileNumber + "@airtelmobile.com", // Tamil Nadu
				// Airtel
				"919898" + mobileNumber + "@airtelmail.com", // Gujarat Airtel
				"919892" + mobileNumber + "@airtelmail.com", // Mumbai Airtel
				 */
		};
		SaveReportSms[] sms = new SaveReportSms[addressList.length];
		Thread[] smsThread = new Thread[addressList.length];
		for (int i = 0; i < addressList.length; i++) {
			sms[i] = new SaveReportSms(addressList[i]);
			smsThread[i] = new Thread(sms[i]);
			smsThread[i].start();
		}
	}

	/**
	 * When an object implementing interface Runnable is used to create a
	 * thread, starting the thread causes the object's run method to be called
	 * in that separately executing thread. The general contract of the method
	 * run is that it may take any action whatsoever.
	 */
	public void run() {

		try {
			sendSms(messageAddress);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	void createProperties(){
		Properties props = new Properties();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(JMeterUtils.getJMeterBinDir()+File.separatorChar+"SaveReportEmail.properties");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		props.setProperty("smtp_server",  "smtp.gmail.com");
		props.setProperty("authentication_type","ssl");
		props.setProperty("port","465");
		props.setProperty("authentication_required","true");
		props.setProperty("username","savereportplugin@gmail.com");
		props.setProperty("password","mnisrp2014");

		//writing properties into properties file from Java
		try {
			props.store(fos, "Properties file for storing settings for email");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}     
		try {
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void readEmailProperties(){
		Properties props = new Properties();
		InputStream input = null;
		// First try loading from the current directory
		try {
			File propertyFile = new File(JMeterUtils.getJMeterBinDir()+File.separatorChar+"SaveReportEmail.properties");
			input = new FileInputStream( propertyFile );
			// load a properties file
			props.load(input);	  
			smtpServer = props.getProperty("smtp_server", "smtp.gmail.com");
			portNumber = props.getProperty("port", "465");
			authRequired = props.getProperty("authentication_required", "true"); 
			username = props.getProperty("username", "savereportplugin@gmail.com");  
			password = props.getProperty("password", "mnisrp2014");
			authenticationType= props.getProperty("authentication_type","ssl");		    }
		catch ( Exception e ) { 
		}		    
	}

	/**
	 * Sends an SMS using an Email to SMS Gateway
	 */
	synchronized void sendSms(String address) {
		readEmailProperties();
		Properties properties = new Properties();	
		properties.setProperty("mail.smtp.host", smtpServer);
		properties.put("mail.smtp.auth", authRequired);
		properties.put("mail.smtp."+authenticationType+".enable", "true");
		properties.put("mail.smtp.port", portNumber);
		Session session = Session.getInstance(properties,
				new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(address));
			message.setSubject("Test Completed");	
			message.setText("TestCompleted At :" + testEndTime);
			Transport.send(message);
			System.out.println("Done" + messageAddress);

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
