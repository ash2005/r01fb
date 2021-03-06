package r01f.persistence.index;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.services.interfaces.IndexManagementServices;

@XmlRootElement(name="indexManagementCommand")
@Accessors(prefix="_")
@NoArgsConstructor
public class IndexManagementCommand 
  implements Serializable {

	private static final long serialVersionUID = 8316497654004305088L;

/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The action to execute
	 */
	@XmlAttribute(name="action")
	@Getter @Setter private IndexManagementAction _action;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  PRIVATE CONSTRUCTORS & FACTORY
/////////////////////////////////////////////////////////////////////////////////////////
	private IndexManagementCommand(final IndexManagementAction action) {
		_action = action;
	}
	public static IndexManagementCommand toCloseIndex() {
		return new IndexManagementCommand(IndexManagementAction.CLOSE_INDEX);
	}
	public static IndexManagementCommand toOpenIndex() {
		return new IndexManagementCommand(IndexManagementAction.OPEN_INDEX);
	}
	public static IndexManagementCommand toOptimizeIndex() {
		return new IndexManagementCommand(IndexManagementAction.OPTIMIZE_INDEX);
	}
	public static IndexManagementCommand toTruncateIndex() {
		return new IndexManagementCommand(IndexManagementAction.TRUNCATE_INDEX);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Actions to be executed on a search engine index (see {@link IndexManagementServices}) 
	 */
	public static enum IndexManagementAction {
		CLOSE_INDEX,	
		OPEN_INDEX,
		OPTIMIZE_INDEX,
		TRUNCATE_INDEX;
	}
}
