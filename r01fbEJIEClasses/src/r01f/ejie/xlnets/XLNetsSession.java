package r01f.ejie.xlnets;

import java.util.Date;
import java.util.Map;

import org.w3c.dom.Node;

public interface XLNetsSession {

	public abstract Date getLoginDate();

	public abstract String getSessionUID();

	public abstract XLNetsUser getUser();

	public abstract XLNetsOrgNode getOrganizacion();

	public abstract XLNetsOrgNode getGrupo();

	public abstract XLNetsOrgNode getUnidad();

	public abstract Map<String, XLNetsProfile> getPerfiles();

	public abstract Map<String, String> getAttributes();
	/**
	 * @return el perfil a partir de su identificador
	 */
	public XLNetsProfile getPerfil(String pefOid);

	/**
	 * Añade información del usuario que viene de otro XML de XLNets
	 * @param userNode nodo xml con la información de usuario
	 */
	public abstract void setUserInfo(Node userNode);

	/**
	 * Comprueba si un usuario pertenece a una determinada organizacion
	 * @param orgOID: El oid de la organizacion
	 * @return: true si pertenece o false si no es asi
	 */
	public abstract boolean userBelongsTo(String orgOID);

	/**
	 * Devuelve true o false en funcion de si el usuario tiene o no el perfil solicitado
	 * @param profile El perfil solicitado
	 * @return True si el usuario tiene el perfil o false si no es asi
	 */
	public abstract boolean hasProfile(String profileOID);

	/**
	 * Devuelve un atributo del contexto (ip, paginaLogin, paginaPortal, paginaPrincipal, lenguaje, etc)
	 * @param attrName El nombre del atributo
	 * @return El atributo (String)
	 */
	public abstract String getAttribute(String attrName);

}