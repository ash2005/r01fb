package r01f.twilio;

import com.google.inject.Provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.guids.CommonOIDs.Password;
import r01f.httpclient.HttpClient;
import r01f.httpclient.HttpClientProxySettings;
import r01f.twilio.TwilioService.TwilioAPIClientID;
import r01f.twilio.TwilioService.TwilioAPIData;
import r01f.types.contact.Phone;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

/**
 * Provides a {@link TwilioService} using a properties file info
 * The properties file MUST contain a config section like:
 * <pre class='xml'>
 *		<twilio>
 *			<accountSID>xxx</accountSID>
 *			<authToken>yyy</authToken>
 *			<voicePhoneNumber>+34538160343</voicePhoneNumber>
 *			<messagingPhoneNumber>+34538160343</messagingPhoneNumber>
 *		</twilio>
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
public class TwilioServiceProvider 
  implements Provider<TwilioService> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final XMLPropertiesForAppComponent _props;
	private final String _propsRootNode;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public TwilioService get() {	
		boolean disableTwilio = false;
		
		// Test proxy connection to see if proxy is needed
		HttpClientProxySettings proxySettings = null;
		try {
			proxySettings = HttpClient.guessProxySettings(_props,_propsRootNode);
		} catch(Throwable th) {
			log.error("Error while guessing the proxy settings to use Twilio: {}",th.getMessage(),th);
			disableTwilio = true;	// the mail sender cannot be used
		}
				
		// Get the twilio api info from the properties file
		TwilioAPIData apiData = TwilioServiceProvider.apiDataFromProperties(_props,_propsRootNode);
		
		// Create the service
		TwilioService outTwilioCallService = new TwilioService(apiData,
															   proxySettings);
		if (disableTwilio) outTwilioCallService.setDisabled();
		
		log.info("Created a {} instance",outTwilioCallService.getClass());
		return outTwilioCallService;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	static TwilioAPIData apiDataFromProperties(final XMLPropertiesForAppComponent props,
											   final String propsRootNode) {
		String accountSID = props.propertyAt(propsRootNode + "/twilio/accountSID")
								 .asString();
		String authToken  = props.propertyAt(propsRootNode + "/twilio/authToken")
								 .asString();
		String twilioVoicePhoneNumber = props.propertyAt(propsRootNode + "/twilio/voicePhoneNumber")
											 .asString();
		String twilioMessagingPhoneNumber = props.propertyAt(propsRootNode + "/twilio/messagingPhoneNumber")
										    	 .asString();
		
		// Check
		if (accountSID == null || authToken == null) {
			throw new IllegalStateException(Throwables.message("Cannot configure Twilio API: the properties file does NOT contains a the accountSID / authToken at {} in {} properties file",
															   propsRootNode + "/twilio",props.getAppCode()));
		}
		if (Strings.isNullOrEmpty(twilioVoicePhoneNumber) && Strings.isNullOrEmpty(twilioMessagingPhoneNumber)) {
			throw new IllegalStateException(Throwables.message("Cannot configure Twilio API: there's neither a voice-enabled twilio phone number nor a messaging-enabled twilio phone number configured at {} in {} properties file",
															   propsRootNode + "/twilio",props.getAppCode()));
		}
		if (Strings.isNullOrEmpty(twilioVoicePhoneNumber)) log.warn("There's NO voice-enabled twilio phone number configured at {} in {} properties file: VOICE CALLS ARE NOT ENABLED!",
																	propsRootNode + "/twilio/voicePhoneNumber",props.getAppCode());
		if (Strings.isNullOrEmpty(twilioMessagingPhoneNumber)) log.warn("There's NO messaging-enabled twilio phone number configured at {} in {} properties file: MESSAGING IS NOT ENABLED!",
																		propsRootNode + "/twilio/voicePhoneNumber",props.getAppCode());
		
		// Create the Twilio service
		TwilioAPIData apiData = new TwilioAPIData(TwilioAPIClientID.of(accountSID),Password.forId(authToken),
												  Strings.isNOTNullOrEmpty(twilioVoicePhoneNumber) ? Phone.of(twilioVoicePhoneNumber) : null,
												  Strings.isNOTNullOrEmpty(twilioMessagingPhoneNumber) ? Phone.of(twilioMessagingPhoneNumber) : null);
		return apiData;
	}
}
