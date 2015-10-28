package r01f.ejb;

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import r01f.reflection.Reflection;
import r01f.reflection.ReflectionException;
import r01f.util.types.Strings;

/**
 * ServiceLocator de interfaces home para EJB20
 */
public class EJB2HomeLocator {
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTES
///////////////////////////////////////////////////////////////////////////////////////////
    private static final String LOCALHOME_INTERFACE_JNDINAME_PREFIX = "";
    private static final String LOCALHOME_INTERFACE_JNDINAME_SUFFIX = "Local";
    private static final String REMOTEHOME_INTERFACE_JNDINAME_PREFIX = "";
    private static final String REMOTEHOME_INTERFACE_JNDINAME_SUFFIX = "";
///////////////////////////////////////////////////////////////////////////////////////////
//  MIEMBROS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Caché con las referencia a los interfaces home indexadas por su nombre jndi
     */
    private transient Map<String,Object> _ejbHomeReferencesCache = null;
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR Y OBTENCION DE UNA INSTANCIA DEL Singleton
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Constructor privado para "prevenir" que algun espabiladillo cree una
     * instancia del ServiceLocator
     * La llamada a este constructor está garantizado que es una única vez
     * ya que se hace desde la clase estática LocatorSingletonHolder (ver nota)
     */
    EJB2HomeLocator() {
        _ejbHomeReferencesCache = new Hashtable<String,Object>(20,0.5F);         // Mapa de referencias a home objects
    }
    /**
     * Proporciona acceso a la instancia única de EJB2ServiceLocator
     * @return La instancia del ServiceLocator
     */
    public static EJB2HomeLocator getInstance() {
    	return EJB2HomeLocator.getInstance(null);
    }
    /**
     * Proporcina acceso a la instancia única de EJB2ServiceLocator
     * @param props las propiedades para inicializar el contexto jndi
     * @return La instancia del ServiceLocator
     */
    public static EJB2HomeLocator getInstance(Properties props) {
    	try {
    		JNDIContextLocator.getInstance().getInitialContext(props);	// Forzar la carga del contexto inicial en la cache de contextos...
        } catch (NamingException namEx) {
            // TODO logging
            String err = Strings.of("No se ha podido obtener el contexto inicial del árbol JNDI: {}").customizeWith(namEx.getMessage()).asString();
            System.out.println(err);
        }
        return LocatorSingletonHolder.instance;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  SINGLETON HOLDER
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Clase holder de un singleton.
     *      Si a alguien se le ocurre referirse al singleton, este NO SERÁ CREADO hasta que se 
     *      haga la primera llamada a getInstance. En este momento la VM se referirá a la clase 
     *      LocatorSingletonHolder, la cargará y su miembro estático (el singleton) se instanciará.
     *
     *      Como se puede ver NO HAY SINCRONIZACIÓN, sin embargo esto es "thread safe".
     *      Cuando la VM intenta cargar una clase se garantiza que mientras dure la carga
     *      ningún otro thread molestará.
     */
    private static final class LocatorSingletonHolder {
        static final EJB2HomeLocator instance = new EJB2HomeLocator();
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  OBTENCION DEL HOME
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Obtiene el Home del ejb de forma transparente para el desarrollador de acuerdo al siguiente algoritmo:
     * de busqueda:
     *      1.- Busca el home en la cache
     *      2.- Busca el home
     * @param jndiContextProviderURL url del provider de contexto JNDI
     * @param jndiName Nombre jndi del interfaz home local
     * @param local true si se busca el interfaz home local
     * @return una referencia al interfaz home
     */
    private Object _retrieveHomeReference(final String jndiContextProviderURL,
    									  final String jndiName,final boolean local) {
    	String theJNDIName = null;
    	if (local) {
    		theJNDIName = LOCALHOME_INTERFACE_JNDINAME_PREFIX + jndiName + LOCALHOME_INTERFACE_JNDINAME_SUFFIX;
    	} else {
    		theJNDIName = REMOTEHOME_INTERFACE_JNDINAME_PREFIX + jndiName + REMOTEHOME_INTERFACE_JNDINAME_SUFFIX;
    	}
    	// Buscar primero en la cache
        Object home = _ejbHomeReferencesCache.isEmpty() ? null
        												: _ejbHomeReferencesCache.get(theJNDIName);
        // Si no está en la caché ir al jndi.
        if (home == null) {
            try {
            	home = local ? JNDIContextLocator.getInstance().getInitialContext().lookup(theJNDIName)				// Si se trata del interfaz local, la url del provider jndi el LDAP local
            				 : JNDIContextLocator.getInstance().getInitialContext(jndiContextProviderURL).lookup(theJNDIName);
            	if (home != null) _ejbHomeReferencesCache.put(theJNDIName,home);		// Cachear el home
            } catch (NamingException namEx) {
            	// TODO logging
            	String err = Strings.of("NO se ha podido obtener un contexto jndi contra el LDAP: {}")
            						.customizeWith(namEx.getExplanation()).asString();
            	System.out.println(err);
            }
        }
        if (home == null) {
        	// TODO logging
			String err = Strings.of("NO se ha encontrado el interfaz home {} del ejb con el nombre jndi={}")
								.customizeWith(local ? "local":"remoto",theJNDIName).asString();
            System.out.println(err);
        }
        return home;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  OBTENCION DEL HOME LOCAL
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Obtiene el Home local del ejb
     * El acceso a este método únicamente puede hacerse a partir de la instancia del singleton:
     *      <code>EJB2ServiceLocator.getInstance().getLocalHome(jndiName);</code>
     * @param jndiName Nombre jndi del interfaz home local
     * @return una referencia al interfaz home
     */
    public EJBLocalHome getLocalHome(final String jndiName) {
    	Object home = _retrieveHomeReference(null,jndiName,true);
        return (EJBLocalHome)home;
    }
    /**
     * Obtiene el Home local del ejb
     * El acceso a este método únicamente puede hacerse a partir de la instancia del singleton:
     *      <code>EJB2ServiceLocator.getInstance().getLocalHome(jndiName,MyHome.class);</code>
     * @param jndiName Nombre jndi del ejb
     * @param homeType Definición de la Clase (Class) para devolver el objeto tipado
     * @return El objeto tipado
     */
    public EJBLocalHome getLocalHome(final String jndiName,
    								 final Class<?> homeType) {
        // Encontrar el home (sin tipar)
        EJBLocalHome home = this.getLocalHome(jndiName);
        // Tipar el objeto (cast portable para rmi-iiop)
        if (home != null) {
            try {
                home = (EJBLocalHome)PortableRemoteObject.narrow(home,homeType);
            } catch (ClassCastException ccEx) {
            	// TODO logging
                String err = Strings.of("Error al hacer un narrow a {} del objeto home de tipo {} > {}")
                					.customizeWith(homeType.getName(),home.getClass().getName(),ccEx.getMessage()).asString();
                System.out.println(err);
            }
        }
        return home;
    }
    /**
     * Obtiene el Home local del ejb 
     * El acceso a este método únicamente puede hacerse a partir de la instancia del singleton:
     *      <code>EJB2ServiceLocator.getInstance().getLocalHome(jndiName,"com.acme.ejb.MyHome");</code>
     * @param jndiName Nombre jndi del ejb
     * @param homeClassName Nombre de la Clase (Class) para devolver el objeto tipado
     * @return El objeto tipado
     */
    public EJBLocalHome getLocalHome(final String jndiName,
    								 final String homeClassName) {
    	EJBLocalHome home = null;
        try {
            home = this.getLocalHome(jndiName,Reflection.type(homeClassName).getType());
        } catch (ReflectionException cnfEx) {
        	// TODO logging
            String err = Strings.of("NO se ha encontrado la clase {} para hacer un narrow del objeto home > {}")
            					.customizeWith(homeClassName,cnfEx.getMessage()).asString();
            System.out.println(err);
        }
        return home;
    }
    /**
     * Comprueba la existencia el interfaz home local de un ejb
     * @param jndiName El nombre jndi del ejb
     * @return true si existe el intefaz local en el arbol jndi
     */
    public boolean existsLocalHome(final String jndiName) {
        Object home = this.getLocalHome(jndiName);
        return home == null ? false:true;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  OBTENCION DEL HOME REMOTO
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Obtiene el Home remoto del ejb
     * El acceso a este método únicamente puede hacerse a partir de la instancia del singleton:
     *      <code>EJB2ServiceLocator.getInstance().getHome(jndiName);</code>
     * @param jndiName Nombre jndi del ejb
     * @return referencia al interfaz home remoto
     */
    public EJBHome getRemoteHome(String jndiName) {
        return this.getRemoteHome(null,
        						  jndiName);
    }
    /**
     * Obtiene el Home remoto del ejb 
     * El acceso a este método únicamente puede hacerse a partir de la instancia del singleton:
     *      <code>EJB2ServiceLocator.getInstance().getHome(jndiName,MyHome.class);</code>
     * @param jndiName Nombre jndi del ejb
     * @param homeType Definición de la Clase (Class) para devolver el objeto tipado
     * @return referencia al interfaz home remoto
     */
    public EJBHome getRemoteHome(String jndiName,Class<?> homeType) {
        return this.getRemoteHome(null,
        						  jndiName,homeType);
    }
    /**
     * Obtiene el Home remoto del ejb 
     * El acceso a este método únicamente puede hacerse a partir de la instancia del singleton:
     *      <code>EJB2ServiceLocator.getInstance().getHome(jndiName,MyHome.class);</code>
     * @param jndiContextProviderURL url del provider de contexto JNDI
     * @param jndiName Nombre jndi del ejb
     * @return referencia al interfaz home remoto
     */
    public EJBHome getRemoteHome(final String jndiContextProviderURL,
    							 final String jndiName) {
        Object home = _retrieveHomeReference(jndiContextProviderURL,
        									 jndiName,false);
        return (EJBHome)home;
    }
    /**
     * Obtiene el Home remoto del ejb
     * El acceso a este método únicamente puede hacerse a partir de la instancia del singleton:
     *      <code>EJB2ServiceLocator.getInstance().getHome(jndiName,MyHome.class);</code>
     * @param jndiContextProviderURL url del provider de contexto JNDI
     * @param jndiName Nombre jndi del ejb
     * @param homeType Definición de la Clase (Class) para devolver el objeto tipado
     * @return El objeto tipado
     */
    public EJBHome getRemoteHome(final String jndiContextProviderURL,
    							 final String jndiName,final Class<?> homeType) {
        // Encontrar el home (sin tipar)
        EJBHome home = this.getRemoteHome(jndiContextProviderURL,
        								  jndiName);
        // Tipar el objeto (cast portable para rmi-iiop)
        if (home != null && homeType != null) {
            try {
                home = (EJBHome)PortableRemoteObject.narrow(home,homeType);
            } catch (ClassCastException ccEx) {
            	// TODO logging
                String err = Strings.of("Error al hacer un narrow a {} del objeto home de tipo {} > {}")
                					.customizeWith(homeType.getName(),home.getClass().getName(),ccEx.getMessage()).asString();
                System.out.println(err);
            }
        }
        return home;
    }
    /**
     * Obtiene el Home remoto del ejb 
     * El acceso a este método únicamente puede hacerse a partir de la instancia del singleton:
     *      <code>EJB2ServiceLocator.getInstance().getLocalHome(jndiName,"com.acme.ejb.MyHome");</code>
     * @param jndiContextProviderURL url del provider de contexto JNDI
     * @param jndiName Nombre jndi del ejb
     * @param homeClassName Nombre de la Clase (Class) para devolver el objeto tipado
     * @return El objeto tipado
     */
    public EJBHome getRemoteHome(final String jndiContextProviderURL,
    							 final String jndiName,final String homeClassName) {
    	EJBHome home = null;
        try {
            home = this.getRemoteHome(jndiContextProviderURL,
            						  jndiName,Reflection.type(homeClassName).getType());
        } catch (ReflectionException cnfEx) {
        	// TODO logging
            String err = Strings.of("NO se ha encontrado la clase {} para hacer un narrow del objeto home > {}")
            					.customizeWith(homeClassName,cnfEx.getMessage()).asString();
            System.out.println(err);
        }
        return home;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  HANDLES
////////////////////////////////////////////////////////////////////////////////////////
}
