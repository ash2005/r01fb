package r01f.test.mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.codec.binary.Base64;

import r01f.guids.CommonOIDs.AppCode;
import r01f.mail.GoogleAPI;
import r01f.mail.GoogleAPI.GoogleAPIClientEMailAddress;
import r01f.mail.GoogleAPI.GoogleAPIClientID;
import r01f.mail.GoogleAPI.GoogleAPIClientIDJsonKeyPath;
import r01f.mail.GoogleAPI.GoogleAPIClientIDP12KeyPath;
import r01f.mail.GoogleAPI.GoogleAPINativeApplicationClientData;
import r01f.mail.GoogleAPI.GoogleAPIServiceAccountClientData;
import r01f.types.contact.EMail;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

public class GMailAPITest { 
	// AppCode
	private static final AppCode APP_CODE = AppCode.forId("X47B");
	// Email address of the user, or "me" can be used to represent the currently
	// authorized user.
	private static final String USER = "me";
	
	// Path to the client_secret.json file downloaded from the Developer Console
	private static final String NATIVE_APP_CLIENT_SECRET_PATH = "D:/temp_dev/x47b/google_apis/client_app.json";
	private static final String SERVICE_ACCOUNT_P12_SECRET_PATH = "D:/temp_dev/x47b/google_apis/server_app.p12";

/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) {	
		try {
			// [1] - Create the transport & json factory
			HttpTransport httpTransport = GoogleAPI.createHttpTransport().noProxy();
			JsonFactory jsonFactory = GoogleAPI.createJsonFactory();
			
			// [2] - Create the google credential
			boolean useServerToken = true;
			GoogleCredential credential = null;
			if (useServerToken) {
				GoogleAPIServiceAccountClientData serviceAccountClientID = new GoogleAPIServiceAccountClientData(APP_CODE,
																												 GoogleAPIClientID.of("327116756300-thcjqf1mvrn0geefnu6ef3pe2sm61i2q.apps.googleusercontent.com"),
																											 	 GoogleAPIClientEMailAddress.of("327116756300-thcjqf1mvrn0geefnu6ef3pe2sm61i2q@developer.gserviceaccount.com"),
																											 	 GoogleAPIClientIDP12KeyPath.loadedFromFileSystem(SERVICE_ACCOUNT_P12_SECRET_PATH),
																											 	 EMail.of("admin@futuretelematics.net"),
																											 	 GmailScopes.all());
				credential = GoogleAPI.createCredentialForServiceAccount(httpTransport,
															   			 jsonFactory,
															   			 serviceAccountClientID);
			} else {				
				credential = GoogleAPI.createCredentialForNativeApp(httpTransport,
															   		jsonFactory,
															   		new GoogleAPINativeApplicationClientData(APP_CODE,
															   												 GoogleAPIClientID.of("327116756300-fd4u232iat8srb3gumlfsqdn244ksc8h.apps.googleusercontent.com"),
															   											     GoogleAPIClientIDJsonKeyPath.loadedFromFileSystem(NATIVE_APP_CLIENT_SECRET_PATH),
															   											     GmailScopes.all()));
			}
			
			// [3] - Create the gmail service
			Gmail gmailService = GoogleAPI.createGmailService(httpTransport,
													 		  jsonFactory,
													 		  APP_CODE,
													 		  credential);
			
			// [4] - Send a test messag
			_sendMessage(gmailService,
						 USER,
						 _createEmail("i-olabarria@ejie.eus","admin@futuretelematics.net",
								 	  "Test OK server side!",
								 	  "I got it!! It works!"));
			
		} catch(Throwable th) {
			th.printStackTrace(System.out);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  see: https://developers.google.com/gmail/api/guides/sending
/////////////////////////////////////////////////////////////////////////////////////////
	private static MimeMessage _createEmail(final String to,final String from,
		  								    final String subject,
		  								    final String bodyText) throws MessagingException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		
		MimeMessage email = new MimeMessage(session);
		InternetAddress toAddress = new InternetAddress(to);
		InternetAddress fromAddress = new InternetAddress(from);
		
		email.setFrom(fromAddress);
		email.addRecipient(javax.mail.Message.RecipientType.TO,
		                   toAddress);
		email.setSubject(subject);
		email.setText(bodyText);
		return email;
	}
	private static Message _createMessageWithEmail(final MimeMessage email) throws MessagingException,
																				   IOException {
	    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	    email.writeTo(bytes);
	    String encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray());
	    Message message = new Message();
	    message.setRaw(encodedEmail);
	    return message;
	}
	private static void _sendMessage(final Gmail service,
								     final String userId,
								     final MimeMessage email) throws MessagingException,
								     								 IOException {
	    Message message = _createMessageWithEmail(email);
	    message = service.users()
	    				 .messages()
	    				 	.send(userId,message)
	    				 	.execute();
	    
	    System.out.println("Message id: " + message.getId());
	    System.out.println(message.toPrettyString());
  }
}
