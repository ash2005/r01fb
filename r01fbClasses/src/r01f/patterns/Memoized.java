package r01f.patterns;

import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Base type to help memoization pattern implementation (Memoization is similar to LazyLoading)
 * The normal use is:
 * <pre class='brush:java'>
 * 		public class MyMemoizedInteger extends Memoized<Integer> {
 * 			protected Integer supply() {
 * 				// call an api or something to supply an integer
 * 			}
 * 		}
 * 		
 * 		MyMemoizedInteger memoizedInt = new MyMemoizedInteger();
 * 		int theInt = memoizedInt.get();		// returns the memoized instance or a supplied new one if it was NOT yet supplied
 * </pre>
 * It's also possible to set the memoized instance externally, so the supply() method would never be called
 * <pre class='brush:java'>
 * @Accessors(prefix="_")
 * public class MyMemoizedContainerType { 
 * 		@Getter private Memoized<String> _memo = new Memoized<String>() {
	 * 													@Override 
	 * 													public String supply() {
	 * 														return "aaaa";
	 * 													}
 * 										 		 };
 * } 
 * MyMemoizedContainerType memoContainer = new MyMemoizedContainerType();
 * memoContainer.getMemo().setInstance("bbb");	// the memoized instance will always be bbb
 * </pre>
 * 
 * @param <T> the type to be memoized
 */
@Accessors(prefix="_")
public abstract class Memoized<T> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The memoized instance
	 */
	@Setter protected T _instance;
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Returns the memoized instance or a new one if it has NOT been supplied
	 * @return the memoized instance
	 */
	public T get() {
		if (_instance == null) _instance = this.supply();
		return _instance;
	}
	/**
	 * Resets the memoized value
	 */
	public void reset() {
		_instance = null;
	}
	/**
	 * Supplies an instance to be memoized
	 * @return the memoized instance
	 */
	protected abstract T supply();
}
