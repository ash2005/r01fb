package r01f.persistence;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

@XmlRootElement(name="persistenceOperationExecOK")
@Accessors(prefix="_")
public class PersistenceOperationExecOK<T>
	 extends PersistenceOperationExecResult<T>
  implements PersistenceOperationOK {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS  
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlElement
	@Getter @Setter protected T _operationExecResult;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public T getOrThrow() throws PersistenceException {
		return _operationExecResult;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public PersistenceOperationExecError<T> asOperationExecError() {
		throw new ClassCastException();
	}
	@Override
	public PersistenceOperationExecOK<T> asOperationExecOK() {
		return this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isCRUDOK() {
		return this instanceof CRUDOK;
	}
	@Override
	public boolean isCRUDOnMultipleOK() {
		return this instanceof CRUDOnMultipleOK;
	}
	@Override
	public boolean isFindOK() {
		return this instanceof FindOK;
	}
	@Override
	public boolean isFindSummariesOK() {
		return this instanceof FindSummariesOK;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getDetailedMessage() {
		// info about the returned object
		String resultInfo = null;
		if (_operationExecResult != null) {
			if (CollectionUtils.isCollection(_operationExecResult.getClass())) {
				resultInfo = Strings.customized("Collection of {} objects",
												CollectionUtils.safeSize((Collection<?>)_operationExecResult));
			} else {
				resultInfo = Strings.customized("an object of type {}",
												_operationExecResult.getClass());
			}
		} else {
			resultInfo = "null";
		}
		// the debug info
		return Strings.customized("The execution of '{}' operation was SUCCESSFUL returning {}",
						  		  _requestedOperationName,
						  		  resultInfo);	
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		return this.getDetailedMessage();
	}
}
