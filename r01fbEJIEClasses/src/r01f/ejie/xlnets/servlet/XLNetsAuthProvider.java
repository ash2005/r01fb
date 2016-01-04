package r01f.ejie.xlnets.servlet;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import r01f.ejie.xlnets.XLNetsUserSession;
import r01f.ejie.xlnets.servlet.XLNetsAppCfg.ProviderDef;

interface XLNetsAuthProvider {

	public abstract ServletRequest getRequest();

	public abstract void setRequest(final ServletRequest _request);

	public abstract ProviderDef getProviderDef();

	public abstract void setProviderDef(final ProviderDef _providerDef);

	public abstract String getProviderId();

	public abstract void setProviderId(final String _providerId);

	/**
	 * @param appCfg configuración de la aplicacion
	 * @return la sesión XLNets
	 */
	public abstract XLNetsUserSession getXLNetsSession();

	/////////////////////////////////////////////////////////////////////////////////////////
	//  METODOS
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Devuelve un contexto de seguridad que construye el filtro de autorización
	 * Puede hacer login de Aplicación o de usuario en XLNets, en caso de hacer de usuario:
	 * <ul>
	 *      <li>Si el usuario se ha autenticado, devuelve un objeto con el contexto
	 *        de la sesión</li>
	 *      <li>Si el usuario no se ha autenticado devuelve null</li>
	 * @param appCfg la configuración de la aplicación
	 * @return Un objeto con el contexto o null si el usuario no se ha autenticado
	 */
	public abstract XLNetsAuthCtx getAuthContext();

	/**
	 * Redirige al usuario a la página de login
	 * @param res la response
	 * @param returnURL La url a la que ha de devolver al usuario la aplicación de login una vez
	 *                   que este ha hecho login
	 */
	public abstract void redirectToLogin(final ServletResponse res,final String returnURL);

	/**
	 * Consulta los datos de autorización del destino cuya configuracion
	 * se pasa como parametro
	 * @param authCtx contexto de autorización
	 * @param targetCfg La configuracion del target
	 * @return un objeto {@link XLNetsTargetCtx} con el contexto de autorizacion para el destino
	 */
	public abstract XLNetsTargetCtx authorize(final XLNetsAuthCtx authCtx,
			XLNetsTargetCfg targetCfg, boolean override);

}