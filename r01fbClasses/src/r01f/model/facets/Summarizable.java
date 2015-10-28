package r01f.model.facets;

import lombok.RequiredArgsConstructor;
import r01f.model.IndexableModelObject;
import r01f.types.summary.Summary;
import r01f.util.types.Strings;

/**
 * Usage:
 * <pre class='brush:java'>
 * 		Summarizable outSummarizable = new InmutableSummarizable(this.getClass()) {
 * 												@Override
												public Summary getSummary() {
													// use SummaryBuilder to build a summary
												}
 * 									   }
 * </pre>
 */
public interface Summarizable {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Interface to be implemented by {@link IndexableModelObject}s that can be summarized to
	 * be full-text indexed
	 */
	public static interface HasSummaryFacet
			 		extends ModelObjectFacet {
		/**
		 * @return the full text summary
		 */
		public Summarizable asSummarizable();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the summary
	 * @return
	 */
	public Summary getSummary();
	/**
	 * Sets the summary
	 * @param summary
	 */
	public void setSummary(final Summary summary);
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor
	public static abstract class InmutableSummarizable
				      implements Summarizable {
		private final Class<?> _type;
		
		@Override
		public void setSummary(final Summary summary) {
			throw new UnsupportedOperationException(Strings.customized("Cannot set summary on {} objects",_type));
		}
	}
}
