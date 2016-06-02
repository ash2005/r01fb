package r01f.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.mail.javamail.JavaMailSender;

import r01f.exceptions.Throwables;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.Password;
import r01f.guids.CommonOIDs.UserAndPassword;
import r01f.guids.CommonOIDs.UserCode;
import r01f.httpclient.HttpClient;
import r01f.httpclient.HttpClientProxySettings;
import r01f.mail.GoogleAPI.GoogleAPIClientEMailAddress;
import r01f.mail.GoogleAPI.GoogleAPIClientID;
import r01f.mail.GoogleAPI.GoogleAPIClientIDP12KeyPath;
import r01f.mail.GoogleAPI.GoogleAPIServiceAccountClientData;
import r01f.resources.ResourcesLoaderDef.ResourcesLoaderType;
import r01f.types.Path;
import r01f.types.contact.EMail;
import r01f.types.url.Host;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

import com.google.inject.Provider;

/**
 * Provides a {@link JavaMailSender} using a properties file inf
 * The properties file MUST contain a config section like:
 * <pre class='xml'>
 *          <javaMailSenderImpls active="google_API">
 *          	<javaMailSenderImpl id='microsoft_exchange'>
 *          		<host>exchangeHost</host>
 *          	</javaMailSenderImpl>
 *          	<javaMailSenderImpl id='google_API'>
 *          		<!--
 *          		 A google API [Service Account] is used in order to avoid end-user interaction (server-to-server)
 *          		 To set-up a [Service Account] client ID: (see http://stackoverflow.com/questions/29327846/gmail-rest-api-400-bad-request-failed-precondition/29328258#29328258)
 *          			1.- Using a google apps user open the developer console
 *          			2.- Create a new project (ie MyProject)
 *          			3.- Go to [Apis & auth] > [Credentials] and create a new [Service Account] client ID
 *          			4.- Copy the [service account]'s [Client ID] (the one like xxx.apps.googleusercontent.com) for later use
 *          			5.- Now you have to Delegate domain-wide authority to the service account in order to authorize your appl to access user data on behalf of users in the Google Apps domain 
 *          			    ... so go to your google apps domain admin console
 *          			6.- Go to the [Security] section and find the [Advanced Settings] (it might be hidden so you'd have to click [Show more..])
 *          			7.- Click con [Manage API Client Access]
 *          			8.- Paste the [Client ID] you previously copied at [4] into the [Client Name] text box.
 *          			9.- To grant your app full access to gmail, at the [API Scopes] text box enter: https://mail.google.com, https://www.googleapis.com/auth/gmail.compose, https://www.googleapis.com/auth/gmail.modify, https://www.googleapis.com/auth/gmail.readonly
 *          				(it's very important that you enter ALL the scopes)
 *          		-->
 *          		<serviceAccountClientID>xxx.apps.googleusercontent.com</serviceAccountClientID>
 *          		<serviceAccountEmailAddress>xxx@developer.gserviceaccount.com</serviceAccountEmailAddress>
 *          		<p12Key loadedUsing='classPath'>my_serviceAccount.p12</p12Key>	<!-- change to fileSystem if the p12 file is found at the fileSystem -->
 *          		<googleAppsUser>admin@example.com</googleAppsUser>
 *          	</javaMailSenderImpl>
 *          	<!-- DEPRECATED!! -->
 *          	<javaMailSenderImpl id='google_SMTP'>
 *          		<!-- 
 *          		How to create an app password:
 *          			1.- Login to the account settings: https://myaccount.google.com/
 *          			2.- Find the [Signing in] section and click on [App passwords]
 *          			3.- Select [Other(custom name)] and give it a name (ie X47B)
 *          			4.- Copy the generated password and put it here
 *          		-->
 *          		<user>a_user</user>
 *          		<password>a password</password>	
 *          	</javaMailSenderImpl>
 *          </javaMailSenderImpls>
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
public class JavaMailSenderProvider 
  implements Provider<JavaMailSender> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final XMLPropertiesForAppComponent _props;
	private final String _propsRootNode;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("deprecation")
	public JavaMailSender get() {
		JavaMailSender outJavaMailSender = null;
		
		// instance the javaMailSender depending on the configured impl		
		JavaMailSenderImpl impl = _props.propertyAt(_propsRootNode + "/javaMailSenderImpls/@active")
									    .asEnumElementIgnoringCase(JavaMailSenderImpl.class);
		
		// ==== MICROSOFT EXCHANGE 
		if (impl == JavaMailSenderImpl.MICROSOFT_EXCHANGE) {
			Host exchangeHost = JavaMailSenderProvider.microsoftExchangeHostFromProperties(_props,_propsRootNode);	
			outJavaMailSender = MicrosoftExchangeSMTPMailSender.create(exchangeHost);
		} 
		
		// ==== GOOGLE GMAIL API
		else if (impl == JavaMailSenderImpl.GOOGLE_API) {
			boolean disableMailSender = false;
			
			// check if a proxy is needed
			HttpClientProxySettings proxySettings = null;
			try {
				proxySettings = HttpClient.guessProxySettings(_props,_propsRootNode);
			} catch(Throwable th) {
				log.error("Error while guessing the internet connection proxy settings to use GMail: {}",th.getMessage(),th);
				disableMailSender = true;	// the mail sender cannot be used
			}
					
			// Get the google api info from the properties file
			GoogleAPIServiceAccountClientData serviceAccountClientData = JavaMailSenderProvider.googleAPIServiceAccountClientDataFromProperties(_props,_propsRootNode);
			
			// Create the GMail Service
			JavaMailSenderGmailImpl gmailJavaMailSender = GMailAPIMailSender.create(serviceAccountClientData,	// service account data
														   							proxySettings);				// proxy settings
			if (disableMailSender) gmailJavaMailSender.setDisabled();
			outJavaMailSender = gmailJavaMailSender;
		} 
		
		// ==== GOOGLE GMAIL SMTP
		else if (impl == JavaMailSenderImpl.GOOGLE_SMTP) {
			// Get the user & password from the properties file
			UserAndPassword userAndPassword = JavaMailSenderProvider.googleSMTPServiceUserAndPassword(_props.getAppCode(),_props,_propsRootNode);
					
			// Create the GMail SMTP service
			outJavaMailSender = GMailSMTPMailSender.create(userAndPassword.getUser(),
														   userAndPassword.getPassword());
		} 
		else {
			throw new IllegalStateException(Throwables.message("JavaMailSender implementation was NOT configured at {} in {} properties file",
															   _propsRootNode + "/javaMailSenderImpls/@active",_props.getAppCode()));
		}
		log.info("Created a {} instance",outJavaMailSender.getClass());
		return outJavaMailSender;
	}
	private enum JavaMailSenderImpl {
		MICROSOFT_EXCHANGE,
		GOOGLE_API,
		GOOGLE_SMTP;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	private static final String MICROSOFT_EXCHANGE_PROPS_XPATH = "/javaMailSenderImpls/javaMailSenderImpl[@id='microsoft_exchange']";
	private static final String GOOGLE_API_PROPS_XPATH = "/javaMailSenderImpls/javaMailSenderImpl[@id='google_API']";
	private static final String GOOGLE_SMTP_PROPS_XPATH = "/javaMailSenderImpls/javaMailSenderImpl[@id='google_SMTP']";	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	static Host microsoftExchangeHostFromProperties(final XMLPropertiesForAppComponent props,
											 		final String propsRootNode) {
		Host host = props.propertyAt(propsRootNode + MICROSOFT_EXCHANGE_PROPS_XPATH + "/host")
					  	 .asHost();
		if (host == null) throw new IllegalStateException(Throwables.message("Cannot configure Microsoft Exchange SMTP: the properties file does NOT contains a the host at {} in {} properties file",
														  propsRootNode + MICROSOFT_EXCHANGE_PROPS_XPATH,props.getAppCode()));
		return host;
	}
	static GoogleAPIServiceAccountClientData googleAPIServiceAccountClientDataFromProperties(final XMLPropertiesForAppComponent props,
											 												 final String propsRootNode)	{
		String serviceAccountClientID = props.propertyAt(propsRootNode + GOOGLE_API_PROPS_XPATH + "/serviceAccountClientID")
											 .asString();
		String serviceAccountEMail = props.propertyAt(propsRootNode + GOOGLE_API_PROPS_XPATH + "/serviceAccountEmailAddress")
										  .asString();
		ResourcesLoaderType p12KeyLoader = props.propertyAt(propsRootNode + GOOGLE_API_PROPS_XPATH + "/p12Key/@loadedUsing")
								   				 .asEnumElementIgnoringCase(ResourcesLoaderType.class,
								   										    ResourcesLoaderType.FILESYSTEM);
		Path p12KeyFilePath = props.propertyAt(propsRootNode + GOOGLE_API_PROPS_XPATH + "/p12Key")
								   .asPath();
		EMail googleAppsUser = props.propertyAt(propsRootNode + GOOGLE_API_PROPS_XPATH + "/googleAppsUser")
									.asEMail();
		
		// Check
		if (serviceAccountClientID == null || serviceAccountEMail == null || p12KeyFilePath == null || googleAppsUser == null) {
			throw new IllegalStateException(Throwables.message("Cannot configure Google API: the properties file does NOT contains a the serviceAccountClientID, serviceAccountEMail, p12KeyFilePath or googleAppsUser at {} in {} properties file",
															   propsRootNode + GOOGLE_API_PROPS_XPATH,props.getAppCode()));
		}
		return new GoogleAPIServiceAccountClientData(props.getAppCode(),
												     GoogleAPIClientID.of(serviceAccountClientID),
												     GoogleAPIClientEMailAddress.of(serviceAccountEMail),
												     p12KeyLoader == ResourcesLoaderType.CLASSPATH ? GoogleAPIClientIDP12KeyPath.loadedFromClassPath(p12KeyFilePath)
														 									       : GoogleAPIClientIDP12KeyPath.loadedFromFileSystem(p12KeyFilePath),
												     googleAppsUser);
	}
	static UserAndPassword googleSMTPServiceUserAndPassword(final AppCode appCode,final XMLPropertiesForAppComponent props,
											 				final String propsRootNode) {
		UserCode user = props.propertyAt(propsRootNode + GOOGLE_SMTP_PROPS_XPATH + "/user")
							 .asUserCode();
		Password password = props.propertyAt(propsRootNode + GOOGLE_SMTP_PROPS_XPATH + "/password")
								 .asPassword();
		// Check
		if (user == null || password == null) {
			throw new IllegalStateException(Throwables.message("Cannot configure Google SMTP: the properties file does NOT contains a the user or password at {} in {} properties file",
															   propsRootNode + GOOGLE_SMTP_PROPS_XPATH,appCode));
		}
		return new UserAndPassword(user,password);
	}
}
