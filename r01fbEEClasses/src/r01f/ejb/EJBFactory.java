package r01f.ejb;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import r01f.reflection.Reflection;
import r01f.util.types.Strings;

/**
 * Factoría de EJB3
 */
public class EJBFactory {
///////////////////////////////////////////////////////////////////////////////
// EJB3
///////////////////////////////////////////////////////////////////////////////
	/**
	 * Obtiene un EJB a partir de su nombre JNDI
	 * @param jndiName nombre JNDI
	 * @param local true si se trata de una referencia al EJB local
	 * @param type tipo del objeto
	 * @return la referencia al EJB
	 */
	public static <T> T createEJB3(String jndiName,boolean local,Class<T> type) {
		return EJBFactory.<T>createEJB3(null,jndiName,local,type);
	}
	/**
	 * Obtiene un EJB a partir de su nombre JNDI
	 * @param jndiContextProps propiedades para obtener una referencia al contexto jndi del LDAP
	 * @param jndiName nombre JNDI
	 * @param local true si se trata de una referencia al EJB local
	 * @param type tipo del objeto
	 * @return la referencia al EJB
	 */
	@SuppressWarnings("unchecked")
	public static <T> T createEJB3(Properties jndiContextProps,
								   String jndiName,boolean local,Class<T> type) {
		T outEJB = null;			
		try {
			Context initialContext = JNDIContextLocator.getInstance().getInitialContext(jndiContextProps);
			String theJNDIName = jndiName + (local ? "Local" : "");	// Por "convenio" el nombre JNDI del EJB local tiene el sufijo "Local"
			Object obj = initialContext.lookup(theJNDIName);
			if (local) { 
				outEJB = (T)obj;
			} else {
				outEJB = (T)PortableRemoteObject.narrow(obj,type);
			}
		} catch(NamingException namEx) {
			// TODO log
			String err = Strings.of("NO se ha podido obtener un contexto jndi contra el LDAP")
								.asString();
			System.out.println(err);
		}
		return outEJB;
	}
///////////////////////////////////////////////////////////////////////////////
// EJB2.x
///////////////////////////////////////////////////////////////////////////////
	/**
	 * Obtiene un EJB a partir de su nombre JNDI
	 * @param jndiName nombre JNDI
	 * @param local true si se trata de una referencia al EJB local
	 * @param homeType tipo del interfaz home
	 * @return la referencia al EJB
	 */
	public static <T> T createEJB2(String jndiName,boolean local,Class<?> homeType) {
		return EJBFactory.<T>createEJB2(null,jndiName,local,homeType);
	}
	/**
	 * Obtiene un EJB a partir de su nombre JNDI
	 * @param jndiContextProps propiedades para obtener una referencia al contexto jndi del LDAP
	 * @param jndiName nombre JNDI
	 * @param local true si se trata de una referencia al EJB local
	 * @param homeType tipo del interfaz home
	 * @return la referencia al EJB
	 */
	public static <T> T createEJB2(Properties jndiContextProps,
								   String jndiName,boolean local,Class<?> homeType) {
		T outEJB = null;			
		try {
			Context initialContext = JNDIContextLocator.getInstance().getInitialContext(jndiContextProps);
			String theJNDIName = jndiName + (local ? "Local" : "");	// Por "convenio" el nombre JNDI del EJB local tiene el sufijo "Local"
			Object home = initialContext.lookup(theJNDIName);
			Object homeNarrowed = PortableRemoteObject.narrow(home,homeType);
			outEJB = Reflection.of(homeNarrowed).method("create").<T>invoke();	// llamar al método create en el objeto home
		} catch(NamingException namEx) {
			// TODO log
			String err = Strings.of("NO se ha podido obtener un contexto jndi contra el LDAP")
								.asString();
			System.out.println(err);
		}
		return outEJB;
	}	
}
