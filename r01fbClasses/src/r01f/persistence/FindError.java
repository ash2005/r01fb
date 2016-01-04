package r01f.persistence;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@XmlRootElement(name="findError")
@Accessors(prefix="_")
public class FindError<T>
	 extends PersistenceOperationOnObjectError<Collection<T>>
  implements FindResult<T>  {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The found object type
	 * (beware that {@link PersistenceOperationOnObjectOK} wraps a {@link Collection} 
	 *  of this objects)
	 */
	@XmlAttribute(name="findedObjType")
	@Getter @Setter protected Class<T> _findedObjectType;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public FindError() {
		// nothing
	}
	FindError(final Class<T> entityType,
			  		  final Throwable th) {
		super(Collection.class,
			  PersistenceRequestedOperation.FIND,
			  th);
		_findedObjectType = entityType;
	}
	FindError(final Class<T> entityType,
			  		  final String errMsg,final PersistenceErrorType errorCode) {
		super(Collection.class,
			  PersistenceRequestedOperation.FIND,
			  errMsg,errorCode);
		_findedObjectType = entityType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public FindOK<T> asFindOK() {
		throw new ClassCastException();
	}
	@Override
	public FindError<T> asFindError() {
		return this;
	}
}
