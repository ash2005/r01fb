package r01f.resources;

import r01f.patterns.IsBuilder;


public class ResourcesReloadControlBuilder 
  implements IsBuilder {
	/**
	 * Creates the default {@link ResourcesReloadControl}
	 * @return
	 */
	public static ResourcesReloadControl createDefault() {
		return ResourcesReloadControlBuilder.createFor(ResourcesReloadControlDef.DEFAULT);
	}
	/**
	 * Creates a {@link ResourcesReloadControl} using a definition
	 * @param def
	 * @return
	 */
	public static ResourcesReloadControl createFor(final ResourcesReloadControlDef def) {
		return def.createResourcesReloadControl();
	}
}
