package r01f.types.dirtytrack;

import java.util.Collection;
import java.util.Set;

import lombok.experimental.Accessors;

/**
 * Changes tracked {@link Set}
 * @param <V>
 */
@Accessors(prefix="_")
public class ChangesTrackedSet<V> 
     extends ChangesTrackedCollection<V>
  implements Set<V> {
	private static final long serialVersionUID = 4672694252186117679L;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ChangesTrackedSet(Collection<V> entries) {
		super(entries);
	}

}
