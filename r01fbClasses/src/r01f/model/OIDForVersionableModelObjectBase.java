package r01f.model;

import java.util.Arrays;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.ObjectUtils;

import r01f.exceptions.Throwables;
import r01f.guids.OID;
import r01f.reflection.ReflectionUtils;
import r01f.util.types.Strings;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Objects;

@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class OIDForVersionableModelObjectBase
  		   implements OIDForVersionableModelObject {

	private static final long serialVersionUID = -6745951154429443045L;
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public <O extends OID> boolean is(final O other) {
		return this.equals(other);
	}
	@Override
	public <O extends OID> boolean isNOT(final O other) {
		return !this.is(other);
	}
	@Override
	public String toString() {
		String outStr = null;
		if (this.getOid() != null && this.getVersion() != null) {
			outStr = Strings.customized("{}/{}",this.getOid(),this.getVersion());
		} else {
			throw new IllegalStateException(Throwables.message("A {} without oid (only oid or version is NOT allowed",this.getClass()));
		} 
		return outStr;
	}
	@Override @GwtIncompatible("not supported by gwt")
	protected Object clone() throws CloneNotSupportedException {
		if (this.getOid() == null || this.getVersion() == null) throw new IllegalStateException(Throwables.message("The oid of type {} has NO state",this.getClass()));
		Object clonedOid = ObjectUtils.clone(this.getOid());
		Object clonedVersion = ObjectUtils.clone(this.getVersion());
		Object outOid = ReflectionUtils.createInstanceOf(this.getClass(),
														 new Class<?>[] {clonedOid.getClass(),clonedVersion.getClass()},
														 new Object[] {clonedOid,clonedVersion});
		return outOid;
	}
	@Override
	public int compareTo(final OID o) {
		if (o == null) return -1;
		if (this.getOid() == null) return 1;
		return this.toString().compareTo(o.toString());
	}
	@Override
	public int hashCode() {
		return Objects.hashCode(this.getOid(),this.getVersion());
	}
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (obj instanceof OIDForVersionableModelObjectBase) {
			OIDForVersionableModelObjectBase versionable = (OIDForVersionableModelObjectBase)obj;
			boolean oidEquals = Objects.equal(versionable.getOid(),this.getOid()); 
			boolean versionEquals = Objects.equal(versionable.getVersion(),this.getVersion());
			return oidEquals && versionEquals;
		} 
		return false;
	}
	@Override
	public <O extends OID> boolean isContainedIn(final O... oids) {
		return oids != null ? this.isContainedIn(Arrays.asList(oids))
							: false;
	}
	@Override
	public <O extends OID> boolean isNOTContainedIn(final O... oids) {
		return !this.isContainedIn(oids);
	}
	@Override
	public <O extends OID> boolean isContainedIn(final Iterable<O> oids) {
    	boolean outIsContained = false;
    	if (oids != null) {
    		for (O oid : oids) {
    			if (oid.equals(this)) {
    				outIsContained = true;
    				break;
    			}
    		}
    	}
    	return outIsContained;
	}
	@Override
	public <O extends OID> boolean isNOTContainedIn(final Iterable<O> oids) {
    	return !this.isContainedIn(oids);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public <O extends OID> O cast() {
		return (O)this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String asString() {
		return this.toString();
	}
	public String memoCode() {
		return this.toString();
	}
}
