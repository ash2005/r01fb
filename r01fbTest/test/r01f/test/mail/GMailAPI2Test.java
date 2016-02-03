package r01f.test.mail;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import r01f.guids.CommonOIDs.AppCode;
import r01f.mail.GMailAPIMailSender;
import r01f.mail.GoogleAPI.GoogleAPIClientEMailAddress;
import r01f.mail.GoogleAPI.GoogleAPIClientID;
import r01f.mail.GoogleAPI.GoogleAPIClientIDP12KeyPath;
import r01f.mail.GoogleAPI.GoogleAPIServiceAccountClientData;
import r01f.types.contact.EMail;

import com.google.api.services.gmail.GmailScopes;

public class GMailAPI2Test { 
	// AppCode
	private static final AppCode APP_CODE = AppCode.forId("X47B");
	
	// Path to the client_secret.json file downloaded from the Developer Console
	private static final String SERVICE_ACCOUNT_P12_SECRET_PATH = "D:/temp_dev/x47b/google_apis/server_app.p12";

/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) {	
		
		try {
			System.out.println("----->Sending email message using gmail...");
			GoogleAPIServiceAccountClientData serviceAccountClientID = new GoogleAPIServiceAccountClientData(APP_CODE,
																											 GoogleAPIClientID.of("327116756300-thcjqf1mvrn0geefnu6ef3pe2sm61i2q.apps.googleusercontent.com"),
																										 	 GoogleAPIClientEMailAddress.of("327116756300-thcjqf1mvrn0geefnu6ef3pe2sm61i2q@developer.gserviceaccount.com"),
																										 	 GoogleAPIClientIDP12KeyPath.loadedFromFileSystem(SERVICE_ACCOUNT_P12_SECRET_PATH),
																										 	 EMail.of("admin@futuretelematics.net"),
																										 	 GmailScopes.all());
			 JavaMailSender mailSender = GMailAPIMailSender.create(serviceAccountClientID);
			
			// [1] - Create a MimeMessagePreparator
			MimeMessagePreparator msgPreparator = _createMimeMessagePreparator(EMail.of("futuretelematics@gmail.com"),
																		       EMail.of("a-lara@ejie.eus"),
																		       "A TEST mail message sent using GMail API",
																		       "Just testing GMail API");
			// [2] - Send the message
	        mailSender.send(msgPreparator);
			System.out.println("----->Message sent!!");
		} catch(Throwable th) {
			th.printStackTrace(System.out);
		}
	}
	private static MimeMessagePreparator _createMimeMessagePreparator(final EMail to,
													   				  final EMail from,
													   				  final String subject,
													   				  final String text) {
		return new MimeMessagePreparator() {
							@Override
				            public void prepare(final MimeMessage mimeMessage) throws Exception {	
								_createMimeMessageHelper(mimeMessage,
														 to,from,
														 subject,text);
				            }
        };
	}
	private static MimeMessageHelper _createMimeMessageHelper(final MimeMessage mimeMessage,
													   		  final EMail to,final EMail from,
													   		  final String subject,final String text) throws MessagingException {
	    MimeMessageHelper message = new MimeMessageHelper(mimeMessage,
	    												  true);	// multi-part!!
	    // To & From
	    message.setTo(to.asString());
	    message.setFrom(from.asString());
	    
	    // Subject
	    message.setSubject(subject);
	    
	    // Text
	    message.setText(text,
	    				true);	// html message	
	    return message;
	}
}
