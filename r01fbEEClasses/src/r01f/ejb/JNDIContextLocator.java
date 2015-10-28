package r01f.ejb;

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Singleton holder de los contextos
 */
public final class JNDIContextLocator {
    private static final String DEFAULT_JNDI_CONTEXT_URL = "default";
    
///////////////////////////////////////////////////////////////////////////////////////////
//  MIEMBROS
///////////////////////////////////////////////////////////////////////////////////////////
    private Map<String,Context> _contextCache = null;
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR Y OBTENCIÓN DE UNA INSTANCIA DEL Singleton
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Constructor privado para "prevenir" que algun espabiladillo cree una
     * instancia de la cache de contextos
     * La llamada a este constructor está garantizado que es una única vez
     * ya que se hace desde la clase estática LocatorSingletonHolder (ver nota)
     */
    JNDIContextLocator() {
        _contextCache = new Hashtable<String,Context>(3,0.7F);         // Mapa de referencias a home objects
    }
    /**
     * Proporcina acceso a la instancia única de la clase EJBHomeFactory
     * @param props las propiedades para inicializar el contexto jndi
     * @return La instancia
     */
    public static JNDIContextLocator getInstance() {
        return JNDIContextCacheSingletonHolder.instance;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  SINGLETON HOLDER
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Clase holder de un singleton.
     *      Si a alguien se le ocurre referirse a el JNDIContextCache, el singleton
     *      NO SERÁ CREADO hasta que se haga la primera llamada a getInstance
     *      En este momento la VM se referirá a la clase JNDIContextCacheSingletonHolder, la cargará
     *      y su miembro estático (el singleton) se instanciará.
     *
     *      Como se puede ver NO HAY SINCRONIZACIÓN, sin embargo esto es "thread safe".
     *      Cuando la VM intenta cargar una clase se garantiza que mientras dure la carga
     *      ningún otro thread molestará.
     */
    private static final class JNDIContextCacheSingletonHolder {
        static final JNDIContextLocator instance = new JNDIContextLocator();
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Obtiene el contexto JNDI
     * @return el contexto
     * @throws NamingException Si ocurre alguna excepción al buscar el contexto
     */
    public Context getInitialContext() throws NamingException {
    	Context outCtx = this.getInitialContext((Properties)null);
        return outCtx;
    }
    /**
     * Obtiene el contexto JNDI
     * @param contextURL
     * @return el contexto
     * @throws NamingException Si ocurre alguna excepción al buscar el contexto
     */
    public Context getInitialContext(final String contextURL) throws NamingException {
    	Context outCtx = null;
    	if (contextURL != null) {
	    	Properties props = new Properties();
	    	props.put("url",contextURL);
	    	outCtx = this.getInitialContext(props);
    	} else {
    		outCtx = this.getInitialContext((Properties)null);
    	}
        return outCtx;
    }
    /**
     * Obtiene el contexto JNDI
     * @param props las propiedades para inicializar el contexto jndi
     * @return El contexto JNDI
     * @throws NamingException Si ocurre alguna excepción al buscar el contexto
     */
    public Context getInitialContext(final Properties props) throws NamingException {
    	Context outCtx = null;
    	if (props == null) {
    		// Contexto por defecto
    		outCtx = _contextCache.get(DEFAULT_JNDI_CONTEXT_URL);
    		if (outCtx == null) {
        		outCtx = new InitialContext();
        		_contextCache.put(DEFAULT_JNDI_CONTEXT_URL,outCtx);		// cachear
    		}
    	} else {
    		// Contexto personalizado
        	String url = props.get("url") != null ? props.get("url").toString() : null;	
    		String factory = props.get("factory") != null ? props.get("factory").toString() : null;
        	String user = props.get("user") != null ? props.get("user").toString() : null;
        	String password = props.get("password") != null ? props.get("password").toString() : null;
        	
        	if (url != null) {
        		outCtx = _contextCache.get(url);
        	} else {
        		outCtx = _contextCache.get(DEFAULT_JNDI_CONTEXT_URL);
        	}
    	    if (outCtx == null && url != null) {
        		Properties h = new Properties();
        								h.put(Context.PROVIDER_URL,url);
        	    if (factory != null) 	h.put(Context.INITIAL_CONTEXT_FACTORY,factory);
        	    if (user != null) 		h.put(Context.SECURITY_PRINCIPAL, user);
        	    if (password != null) 	h.put(Context.SECURITY_CREDENTIALS, password);
        	    outCtx = new InitialContext(h);
    	    } else if (outCtx == null && url == null) {
    	    	outCtx = new InitialContext();
    	    }
    	    _contextCache.put(url,outCtx);							// cachear
		}
    	return outCtx;
    }
}
