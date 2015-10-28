package r01f.inject;

import lombok.RequiredArgsConstructor;
import r01f.reflection.ReflectionUtils;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;

/**
 * GUICE {@link com.google.inject.matcher.Matchers} extensions
 */
public class Matchers {
	
	public static ClassToTypeLiteralMatcherAdapter subclassesOf(final Class<?>... types) {
		return new ClassToTypeLiteralMatcherAdapter(types);
	}
	
	/**
	 * Adapts a Matcher<Class> to a Matcher<TypeLiteral>
	 * @see https://groups.google.com/forum/#!topic/google-guice/9IK1zQzWHLk
	 */
	@RequiredArgsConstructor @SuppressWarnings("rawtypes")
	public static class ClassToTypeLiteralMatcherAdapter 
	 		    extends AbstractMatcher<TypeLiteral> {
		
		private final Class<?>[] _types;

		@Override
		public boolean matches(final TypeLiteral typeLiteral) {
			boolean outMatches = false;
			for (Class<?> type : _types) {
				if (ReflectionUtils.isSubClassOf(typeLiteral.getRawType(),type)) {
					outMatches = true;
					break;
				}
			}
			return outMatches;
		}
	}
}
