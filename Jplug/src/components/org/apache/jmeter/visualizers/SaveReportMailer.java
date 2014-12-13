package org.apache.jmeter.visualizers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.DefaultListModel;
import javax.activation.*;
import org.apache.jmeter.util.JMeterUtils;
import au.com.bytecode.opencsv.CSVReader;

/**
 * This class is used to send an Email notification containing the PDF report as
 * attachment to the recipient at the end of the test. Email is sent using the
 * javax.mail package.
 * 
 * @author Save Report Project Group 2014
 * 
 */
class SaveReportMailer {
	String mailingList;
	DefaultListModel<String> listModel = new DefaultListModel<String>();
	String smtpServer;
	String authRequired;
	String authenticationType;
	String portNumber;
	String username;
	String password;

	public void createMailingList() {
		StringBuilder listBuilder = new StringBuilder();
		for (int i = 0; i < listModel.getSize(); i++) {
			if (i == 0)
				listBuilder.append(listModel.get(i));
			else
				listBuilder.append("," + listModel.get(i));
		}
		mailingList = listBuilder.toString();

	}

	/**
	 * Creates a ListModel model from the list of Email-Ids in the CSV file
	 * SaveReportMailingList.csv
	 * 
	 * @throws IOException
	 */
	private void createEmailModel() throws IOException {
		File emailFile = new File(JMeterUtils.getJMeterBinDir()
				+ File.separatorChar + "SaveReportMailingList.csv");

		listModel.removeAllElements();

		if (emailFile.exists() && emailFile.length() > 0) {

			FileReader fr;
			try {
				fr = new FileReader(JMeterUtils.getJMeterBinDir()
						+ File.separatorChar + "SaveReportMailingList.csv");
				CSVReader cr = new CSVReader(fr);
				String row1[];
				while (true) {
					synchronized (this) {
						row1 = cr.readNext();
						if (row1 == null)
							break;
						else {
							listModel.addElement(row1[0]);

						}
					}
				}
				cr.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates a property file in jmeter bin if it does not exist
	 */
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


		//writing properites into properties file from Java
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
			authenticationType= props.getProperty("authentication_type","ssl");
			input.close();
		}
		catch ( Exception e ) { createProperties(); }
	}
	/**
	 * Sends the Email to the list of recipients specified.
	 * 
	 * @param reportPath
	 * @param endDateTime
	 * @throws IOException
	 */
	public void mailReport(String reportPath, String endDateTime)
			throws IOException {

		readEmailProperties();
		createEmailModel();
		createMailingList();
		String to = mailingList;
		// 1) get the session object
		Properties properties = System.getProperties();
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

		// 2) compose message
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.addRecipients(Message.RecipientType.CC, to);
			message.setSubject("Test Completed at " + endDateTime);

			// 3) create MimeBodyPart object and set your message text
			BodyPart messageBodyPart1 = new MimeBodyPart();
			messageBodyPart1.setText("\n\nTest completed at : " + endDateTime);

			// 4) create new MimeBodyPart object and set DataHandler object to
			// this object
			MimeBodyPart messageBodyPart2 = new MimeBodyPart();

			// String filename = ;//change accordingly
			DataSource source = new FileDataSource(reportPath);
			messageBodyPart2.setDataHandler(new DataHandler(source));
			messageBodyPart2.setFileName(reportPath);

			// 5) create Multipart object and add MimeBodyPart objects to this
			// object
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart1);
			multipart.addBodyPart(messageBodyPart2);

			// 6) set the multipart object to the message object
			message.setContent(multipart);

			// 7) send message
			Transport.send(message);

			System.out.println("Email has been sent...");
		} catch (MessagingException ex) {
			ex.printStackTrace();
		}
	}
}