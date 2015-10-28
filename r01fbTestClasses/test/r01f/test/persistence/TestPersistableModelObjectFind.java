package r01f.test.persistence;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.joda.time.DateTime;
import org.junit.Assert;

import r01f.guids.CommonOIDs.UserCode;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.services.client.api.delegates.ClientAPIDelegateForModelObjectFindServices;
import r01f.types.Range;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.base.Stopwatch;

@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public class TestPersistableModelObjectFind<O extends OID,M extends PersistableModelObject<O>> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private final ClientAPIDelegateForModelObjectFindServices<O,M> _findAPI;
	private final TestPersistableModelObjectFactory<O,M> _modelObjFactory;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static <O extends OID,M extends PersistableModelObject<O>> TestPersistableModelObjectFind<O,M> create(final ClientAPIDelegateForModelObjectFindServices<O,M> findAPI,
																												 final TestPersistableModelObjectFactory<O,M> modelObjFactory) {
		return new TestPersistableModelObjectFind<O,M>(findAPI,
													   modelObjFactory);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Tests the CRUD API (creates an entity, updates it, loads it and finally deletes it)
	 * @param modelObject
	 */
	public void testFind() {		
		System.out.println("[init][TEST BASIC FIND]-----------------------------------------------------------------------");
		
		Stopwatch stopWatch = Stopwatch.createStarted();
		
		// [0]: SetUp: create some test objects
		_modelObjFactory.setUpMockModelObjs(5);
		
		// [1] - All entities
		System.out.println("FIND ALL ENTITY's OIDS_____________________________");
		Collection<O> allOids = _findAPI.findAll();
		System.out.println(">> " + allOids);
		Assert.assertTrue(CollectionUtils.hasData(allOids));

		// [2] - By create / last update date		
		Range<Date> dateRange = Range.open(DateTime.now().minusDays(1).toDate(),
										   DateTime.now().plusDays(1).toDate());
		
		System.out.println("FIND ENTITY's OIDs BY CREATE DATE: " + dateRange.asString() + " __________________");
		Collection<O> oidsByCreateDate = _findAPI.findByCreateDate(dateRange);
		System.out.println(">> " + oidsByCreateDate);
		Assert.assertTrue(CollectionUtils.hasData(oidsByCreateDate));
		
		System.out.println("FIND ENTITY's OIDs BY LAST UPDATE DATE: " + dateRange.asString() + " ______________");
		Collection<O> oidsByLastUpdatedDate = _findAPI.findByCreateDate(dateRange);
		System.out.println(">> " + oidsByLastUpdatedDate);
		Assert.assertTrue(CollectionUtils.hasData(oidsByLastUpdatedDate));
		
		// [3] - By creator / last updator
		UserCode user = UserCode.forId("myNameIsGOD");
		
		System.out.println("FIND ENTITY's OIDs BY CREATOR: " + user + " ________________________");
		Collection<O> oidsByCreator = _findAPI.findByCreator(user);
		System.out.println(">> " + oidsByCreator);

		System.out.println("FIND ENTITY's OIDs BY LAST UPDATOR: " + user + " ___________________");
		Collection<O> oidsByLastUpdator = _findAPI.findByLastUpdator(user);
		System.out.println(">> " + oidsByLastUpdator);
		
		
		System.out.println("[end ][TEST BASIC FIND] (elapsed time: " + NumberFormat.getNumberInstance(Locale.getDefault()).format(stopWatch.elapsed(TimeUnit.MILLISECONDS)) + " milis) -------------------------");
		
		// [99]: Delete previously created test objects to restore DB state
		_modelObjFactory.tearDownCreatedMockModelObjs();
	}
}
