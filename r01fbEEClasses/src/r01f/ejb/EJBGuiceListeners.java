package r01f.ejb;

import java.lang.reflect.Field;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;

import r01f.reflection.Reflection;

import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * ver:
 * 		http://javarebel.blogspot.com.es/2010/09/injecting-ejb-to-pojo-using-google.html
 * 		http://javarebel.blogspot.com.es/2010/09/using-aspectj-with-rad.html
 * también ver:
 * 		http://gianluigidavassiuk.blogspot.com.es/2009/11/ejb3-plus-guice-how-exotic-part-3.html
 * 
 * GUICE custom injection que se encarga de ser notificada cuando guice va a inyectar en un tipo
 * En este caso, simplemente "busca" todos los miembros anotados con <code>@EJB</code> y registra 
 * un listener 
 * 
 * Una custom injection de GUICE requiere:
 * 		- TypeListener: es notificado cuando guice va a hacer la inyección en un tipo 
 * 		- MemberInjector / InjectionListener: es notificado después de que guice inyecta una instancia  
 * 		- El registro de ambos
 */
public class EJBGuiceListeners {
	/**
	 * Listener que "atiende" a ser notificado cuando GUICE va a inyectar en un objeto de un tipo
	 */
	public static class EJBTypeListener implements TypeListener {
		@Override
		public <I> void hear(TypeLiteral<I> typeLiteral,TypeEncounter<I> typeEncounter) {
			// Recorre todos los miembros de la clase y busca aquellos que están anotados
			// con la anotación @EJB
			for (Field field : typeLiteral.getRawType().getDeclaredFields()) {
				if (field.isAnnotationPresent(EJB.class)) {
					// si el miembro está anotado con @EJB, registrarlo para que guice invoque al método register del Injector
					typeEncounter.register(new EJBMemberInjector<I>(field));
				}
			}
		 }
	}
	/**
	 * Listener que "atiende" a ser notificado cuando GUICE va a inyectar una instancia
	 * @param <T>
	 */
	public static class EJBMemberInjector<T> implements MembersInjector<T> {
		private final Field field;
		private EJB annotation;
		
		EJBMemberInjector(Field f) {
			this.field = f;
			this.annotation = f.getAnnotation(EJB.class);
		}
		
		@Override
		public void injectMembers(T t) {
			// Obtener una instancia del EJB
			Object ejb = null;
			if (annotation.localHomeType() == EJBLocalHome.class || annotation.homeType() == EJBHome.class) {
				// EJB20
				Class<?> homeType = annotation.local() ? annotation.localHomeType() : annotation.homeType();
				ejb = EJBFactory.createEJB2(annotation.jndiName(),annotation.local(),homeType);
			} else {	
				// EJB3
				ejb = EJBFactory.createEJB3(annotation.jndiName(),annotation.local(),this.field.getType());	
			}
			// Establecer el valor del miembro
			Reflection.of(t).field(this.field).set(ejb);
		}
	}
}
