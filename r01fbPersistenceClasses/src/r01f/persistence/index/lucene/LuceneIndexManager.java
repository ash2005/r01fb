package r01f.persistence.index.lucene;

import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.persistence.index.IndexManager;
import r01f.persistence.lucene.LuceneIndex;
import r01f.usercontext.UserContext;

/**
 * Base Lucene index manager 
 */
@Accessors(prefix="_")
public final class LuceneIndexManager 
		implements IndexManager {
/////////////////////////////////////////////////////////////////////////////////////////
//  FINAL STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Lucene index to search against
	 */
	@Getter(AccessLevel.PROTECTED) private final LuceneIndex _luceneIndex;
/////////////////////////////////////////////////////////////////////////////////////////
//  INJECTED CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	@Inject
	public LuceneIndexManager(final LuceneIndex luceneIndex) {
		_luceneIndex = luceneIndex;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  INDEX CONTROL METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void open(final UserContext userContext) {
		_luceneIndex.open();
	}
	@Override
	public void close(final UserContext userContext) {
		_luceneIndex.close();
	}
	@Override
	public void optimize(final UserContext userContext) {
		_luceneIndex.optimize();
	}
	@Override
	public void truncate(final UserContext userContext) {
		_luceneIndex.truncate();
	}
}
