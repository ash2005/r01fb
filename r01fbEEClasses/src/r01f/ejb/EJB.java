package r01f.ejb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;

import com.google.inject.BindingAnnotation;

/**
 * Java EE5 sopota la inyección de dependencias pero SOLO EN PARTE: 
 * 	- Se puede inyectar referencias a un EJIE pero SOLO a determinados componentes como EJBs, Servlets etc. 
 *  - NO se pueden inyectar dependencias a clases POJO normales
 * Esta anotación intenta "simular" la notación estandar <code>@EJB</code> para inyectar una dependencia de un
 * EJB en una clase POJO que en J2EE5 se utiliza para inyectar dependencias a EJBs en otros EJBs o Servlets 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@BindingAnnotation
public @interface EJB {
	String jndiName();							// nombre JNDI del EJB
	boolean local() default true;				// tipo de referencia (local o remota)
	Class<? extends EJBHome> homeType() ;//default EJBHome.class;					// Interfaz home (solo EJB1.x y EJB2.x -NO es necesario en EJB3)
	Class<? extends EJBLocalHome> localHomeType();// default EJBLocalHome.class;	// Interfaz home local (solo EJB1.x y EJB2.x -NO es necesario en EJB3)
}