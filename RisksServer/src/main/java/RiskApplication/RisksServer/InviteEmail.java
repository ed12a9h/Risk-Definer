/**
 * Risk Definer Web Service
 * Produced by Adam Hustwit
 * 
 * This file contains code sending emails to users
 * using SMTP2GO
 */
package RiskApplication.RisksServer;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class InviteEmail {
	// Function to send email to user.
	public void sendEmail (String pName, Integer pRecID, String email) {
		// SMTP Server Details
		Properties properties = new Properties();
        properties.put("mail.smtp.host", "mail.smtp2go.com");
        properties.put("mail.smtp.port", "2525");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        // creates a new session with an authenticator
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("riskdefiner", "cVsXsAs9rUZ0");
            }
        };
        Session session = Session.getInstance(properties, auth);
		
        // Write Email
		Message msg = new MimeMessage(session);
		try {
			// From Email
			msg.setFrom(new InternetAddress("noreply@riskdefiner.me"));
			// To Email
			InternetAddress[] toAddresses = { new InternetAddress(email) };
			msg.setRecipients(Message.RecipientType.TO, toAddresses);
			// Email Subject
			msg.setSubject("Risk Definer Project Invite: "+pName);
			// Email Content
			msg.setContent("<p>Hello,</p><p>This is an automated email from Risk Definer (do not"
					+ " respond) to inform you that you have been sent an invitation to view the"
					+ " below project.</p><p>Project Name: " + pName +"</p><p>URL: <a href="
					+ "'https://riskdefiner.me/client/risk.html#'>"+pRecID+"'>"
					+ "https:riskdefiner.me/client/risk.html#"+pRecID+"</a></p><p>Please login "
					+ "using your Google account associated with the email "+email+"</p><p>"
					+ "From Risk Definer.</p>", "text/html");
	        // sends the e-mail
	        Transport.send(msg);
	        System.out.println("Email sent.");
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
