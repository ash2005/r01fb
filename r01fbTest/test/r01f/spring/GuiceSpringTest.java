package r01f.spring;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.mail.internet.MimeMessage;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.ui.velocity.VelocityEngineFactory;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.google.common.base.Charsets;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.inject.SpringIntegration;
import r01f.internal.R01F;
import r01f.mail.GMailSMTPMailSender;


public class GuiceSpringTest {
///////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static void main(final String[] args) {
		Injector injector = Guice.createInjector(new GuiceOnlyBootStrapModule());
		
		MailManager mailMgr = injector.getInstance(MailManager.class);
		mailMgr.send();
		System.out.println("Sent!");
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  GUICE TO SPRING
/////////////////////////////////////////////////////////////////////////////////////////
	private static class GuiceSpringDelegatingBootStrapModule
		         extends AbstractModule {
		
		@Override @SuppressWarnings("resource")
		protected void configure() {
			R01F.initSystemEnv();
			
			bind(MailManager.class).toInstance(new MailManager());
			
			ApplicationContext springAppContext = new AnnotationConfigApplicationContext(SpringConfiguration.class);
			bind(BeanFactory.class).toInstance(springAppContext);
			
			// Bind spring managed beans
			// (remember that spring binds the @Bean-annotated method with the method name)
			bind(JavaMailSender.class).toProvider(SpringIntegration.fromSpring(JavaMailSender.class,
																			   "mailSender"));
			bind(MailMessage.class).toProvider(SpringIntegration.fromSpring(MailMessage.class,
																			"mailMessageTemplate"));
			bind(VelocityEngine.class).toProvider(SpringIntegration.fromSpring(VelocityEngine.class,
																			   "velocityEngine"));
		}
	}
	@Configuration
	@NoArgsConstructor 
	@SuppressWarnings("static-method")
	private static class SpringConfiguration {
		
		@Bean @Scope("singleton")	// the method name (mockMessageService) becomes the bean "name"
		JavaMailSender mailSender() {
			return GMailSMTPMailSender.create("futuretelematics",
										   "qewsvvedftyinsgm");
		}
		@Bean @Scope("singleton")	// the method name (mockMessageService) becomes the bean "name"
		MailMessage mailMessageTemplate() {
			SimpleMailMessage outMsgTemplate = new SimpleMailMessage();
			return outMsgTemplate;
		}
		@Bean @Scope("singleton")	// the method name (mockMessageService) becomes the bean "name"
		VelocityEngineFactory velocityEngine() {
			Properties velocityProps = new Properties();
			velocityProps.put("resource.loader","class");
	        velocityProps.put("class.resource.loader.class","org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
			
			VelocityEngineFactory outVelocityEngineFactory = new VelocityEngineFactoryBean();
			outVelocityEngineFactory.setVelocityProperties(velocityProps);
			return outVelocityEngineFactory;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  GUICE
/////////////////////////////////////////////////////////////////////////////////////////
	private static class GuiceOnlyBootStrapModule
		         extends AbstractModule {
		
		@Override
		protected void configure() {
			R01F.initSystemEnv();
			
			bind(MailManager.class).toInstance(new MailManager());
			
			bind(JavaMailSender.class).toInstance(GMailSMTPMailSender.create("futuretelematics",
										   								     "qewsvvedftyinsgm"));
			bind(MailMessage.class).toInstance(new SimpleMailMessage());
			
			bind(VelocityEngine.class).toProvider(new Provider<VelocityEngine>() {
															@Override
															public VelocityEngine get() {
																Properties velocityProps = new Properties();
																velocityProps.put("resource.loader","class");
														        velocityProps.put("class.resource.loader.class","org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
																
																VelocityEngineFactory velocityEngineFactory = new VelocityEngineFactoryBean();
																velocityEngineFactory.setVelocityProperties(velocityProps);
																VelocityEngine outVelocityEngine = null;
																try  {
																	outVelocityEngine = velocityEngineFactory.createVelocityEngine();
																} catch(IOException ioEx) {
																	ioEx.printStackTrace(System.out);
																}
																return outVelocityEngine;
															}
												  })
									  .in(Singleton.class);
		}
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@NoArgsConstructor
	private static class MailManager {
		@Inject
		@Getter private JavaMailSender _mailSender;
		
		@Inject 
		@Getter private SimpleMailMessage _mailMsgTemplate;
		
		@Inject 
		@Getter private VelocityEngine _velocityEngine;
		
		public void send() {
	        try {
//	        	SimpleMailMessage msg = _createMailMessage();
//	            _mailSender.send(msg);
	            
	            MimeMessagePreparator msgPreparator = _createMailMessageUsingVelocity();
	            _mailSender.send(msgPreparator);
	            
	        } catch (MailException mailEx) {
	            mailEx.printStackTrace(System.out);
	        }
	    }
		private SimpleMailMessage _createMailMessage() {
	        // Create a thread safe "copy" of the template message and customize it
	        SimpleMailMessage msg = new SimpleMailMessage(_mailMsgTemplate);
	        msg.setTo("a-lara@ejie.es");
	        msg.setFrom("futuretelematics@gmail.com");
	        msg.setSubject("Testing");
	        msg.setText("Madafaka!!!");
	        return msg;
		}
		private MimeMessagePreparator _createMailMessageUsingVelocity() {
	        MimeMessagePreparator preparator = new MimeMessagePreparator() {
	        											@Override
											            public void prepare(MimeMessage mimeMessage) throws Exception {
											                MimeMessageHelper message = new MimeMessageHelper(mimeMessage,
											                												  true);	// multi-part!!
											                
											                message.setTo("a-lara@ejie.es");
											                message.setFrom("futuretelematics@gmail.com"); 
											                message.setSubject("Test!!!");
											                
											                // Text... using velocity
											                Map<String,Object> model = new HashMap<String,Object>();
											                model.put("user",new User("Alex","a-lara@ejie.es"));
											                String text = VelocityEngineUtils.mergeTemplateIntoString(_velocityEngine,
											                														  "template.vm",
											                														  Charsets.ISO_8859_1.name(),
											                														  model);
											                message.setText(text, true);
											                
											                // Image
											                ClassPathResource res = new ClassPathResource("logo.gif");
											                message.addInline("logoOpenData", res);											               											                
											            }
	        };
	        return preparator;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	@NoArgsConstructor @AllArgsConstructor
	public static class User {
		@Getter @Setter private String _userName;
		@Getter @Setter private String _emailAddress;
	}
}
