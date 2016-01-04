package r01f.services.persistence;

import java.util.Date;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import lombok.experimental.Accessors;
import r01f.guids.CommonOIDs.UserCode;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.persistence.FindOIDsResult;
import r01f.services.interfaces.FindServicesForModelObject;
import r01f.types.Range;
import r01f.usercontext.UserContext;


/**
 * Core service base for persistence services
 * 
 * INJECTED STATUS
 * ===============
 * 	The TRANSACTIONAL methods are located at the Services layer (this type); 
 * 	Some operations might span multiple @Transactional-annotated methods so it's very important
 *  	for the {@link EntityManager} to have Extended-scope
 * 		see http://piotrnowicki.com/2012/11/types-of-entitymanagers-application-managed-entitymanager/
 * 		 or http://www.javacodegeeks.com/2013/06/jpa-2-entitymanagers-transactions-and-everything-around-it.html 
 *    	
 * 	The {@link EntityManager} should be created at the services layer (this type) and handled to every delegated
 * 	type (crud, search, etc)
 * 
 *  	Beware that:
 *  	<ul>
 *  		<li>This type is (at the end) injected in a service-layer that usually is a {@link Singleton} instance</li>
 *  		<li>{@link EntityManager} is NOT usually thread safe and an {@link EntityManagerFactory} should be used if the type is NOT thread safe to create an {@link EntityManager}</li>
 *  		<li>Because this type is (at the end) injected in a {@link Singleton} and it's NOT thread-safe, the {@link EntityManagerFactory} should be used</li>
 *  		<li>When creating an {@link EntityManager} from a {@link EntityManagerFactory} the application is responsible for creation and
 *  			removal of the {@link EntityManager}... so it's an [Application-Managed {@link EntityManager}] and these types of
 *  			managers ALLWAYS have EXTENDED SCOPE (see http://piotrnowicki.com/2012/11/types-of-entitymanagers-application-managed-entitymanager/)</li>
 *  	</ul>
 *  	See
 *  	<ul> 
 *  		<li>http://www.javacodegeeks.com/2013/06/jpa-2-entitymanagers-transactions-and-everything-around-it.html</li>
 *  		<li>http://piotrnowicki.com/2012/11/types-of-entitymanagers-application-managed-entitymanager/</li>
 *  	</ul>
 *
 * @param <O>
 * @param <M>
 * @param <FD>
 */
@Accessors(prefix="_")
public abstract class CoreFindServiceForModelObjectBase<O extends OID,M extends PersistableModelObject<O>>
			  extends CorePersistenceServiceBase 
		   implements FindServicesForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIND
/////////////////////////////////////////////////////////////////////////////////////////
	@Override 
	public FindOIDsResult<O> findAll(final UserContext userContext) {
		return this.createDelegateAs(FindServicesForModelObject.class)
						.findAll(userContext);
	}
	@Override
	public FindOIDsResult<O> findByCreateDate(final UserContext userContext,
											  final Range<Date> createDate) {
		return this.createDelegateAs(FindServicesForModelObject.class)
						.findByCreateDate(userContext,
										  createDate);
	}
	@Override
	public FindOIDsResult<O> findByLastUpdateDate(final UserContext userContext,
												  final Range<Date> lastUpdateDate) {
		return this.createDelegateAs(FindServicesForModelObject.class)
						.findByLastUpdateDate(userContext,
											  lastUpdateDate);
	}
	@Override 
	public FindOIDsResult<O> findByCreator(final UserContext userContext,
										   final UserCode creatorUserCode) {
		return this.createDelegateAs(FindServicesForModelObject.class)
						.findByCreator(userContext,
									   creatorUserCode);
	}
	@Override 
	public FindOIDsResult<O> findByLastUpdator(final UserContext userContext,
											   final UserCode lastUpdtorUserCode) {
		return this.createDelegateAs(FindServicesForModelObject.class)
						.findByLastUpdator(userContext,
										   lastUpdtorUserCode);
	}
}
