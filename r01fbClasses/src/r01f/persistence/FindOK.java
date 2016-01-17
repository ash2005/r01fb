package r01f.persistence;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.exceptions.Throwables;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

@XmlRootElement(name="foundObjects")
@Accessors(prefix="_")
public class FindOK<T>
	 extends PersistenceOperationOnObjectOK<Collection<T>>
  implements FindResult<T> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The found object type
	 * (beware that {@link PersistenceOperationOnObjectOK} wraps a {@link Collection} 
	 *  of this objects)
	 */
	@XmlAttribute(name="foundObjType")
	@Getter @Setter protected Class<T> _foundObjectType;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public FindOK() {
		/* nothing */
	}
	protected FindOK(final Class<T> entityType) {
		super(Collection.class,		// The find methods return Collection of objects
			  PersistenceRequestedOperation.FIND,PersistencePerformedOperation.from(PersistenceRequestedOperation.FIND));
		_foundObjectType = entityType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * When a single result is expected, this method returns this entity
	 * @return
	 */
	public T getSingleExpectedOrThrow() {
		T outEntity = null;
		Collection<T> entities = this.getOrThrow();
		if (CollectionUtils.hasData(entities)) {
			outEntity = CollectionUtils.of(entities).pickOneAndOnlyElement("A single instance of {} was expected to be found BUT {} were found",_foundObjectType,entities.size());
		} else {
			throw new IllegalStateException(Throwables.message("A single instance of {} was expected to be found BUT NONE were found",_foundObjectType));
		}
		return outEntity;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public FindOK<T> asFindOK() {
		return this;
	}
	@Override
	public FindError<T> asFindError() {
		throw new ClassCastException();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("{} persistence operation requested on entity of type {} and found {} results",
								  _requestedOperation,_objectType,CollectionUtils.safeSize(_operationExecResult));
	}
}
