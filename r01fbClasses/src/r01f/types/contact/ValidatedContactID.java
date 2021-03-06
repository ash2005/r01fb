package r01f.types.contact;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

import com.google.common.annotations.GwtIncompatible;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.types.CanBeRepresentedAsString;
import r01f.types.annotations.Inmutable;



@Inmutable
@NoArgsConstructor @AllArgsConstructor
@Accessors(prefix="_")
public abstract class ValidatedContactID 
           implements CanBeRepresentedAsString,
    		   		  Serializable {
	
	private static final long serialVersionUID = 7691819813799837148L;
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlElement
	@Getter @Setter private String _id;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Validates the id 
	 */
	@GwtIncompatible(value = "Not Compatible for GWT")
	public abstract boolean isValid();
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return this.asString();
	}
	@Override
	public String asString() {
		return _id != null ? _id.toString() : null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static <I extends ValidatedContactID> String asStringOrNull(final I id) {
		return id != null ? id.asString()
						  : null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public int hashCode() {
		return _id.hashCode();
	}
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj.getClass() == this.getClass()) {
			ValidatedContactID id = (ValidatedContactID)obj;
			return id.getId().equals(_id);
		}
		return false;
	}	
}
