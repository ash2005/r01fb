package r01f.persistence.internal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import org.eclipse.persistence.config.PersistenceUnitProperties;

@Accessors(prefix="_")
@RequiredArgsConstructor
enum BBDDSchemaTablesDDLAction {
	NONE(PersistenceUnitProperties.SCHEMA_GENERATION_NONE_ACTION,PersistenceUnitProperties.NONE),
	CREATE_TABLES(PersistenceUnitProperties.SCHEMA_GENERATION_CREATE_ACTION,PersistenceUnitProperties.CREATE_ONLY),
	CREATE_OR_EXTEND_TABLES(PersistenceUnitProperties.CREATE_OR_EXTEND,PersistenceUnitProperties.CREATE_OR_EXTEND),
	DROP_TABLES(PersistenceUnitProperties.SCHEMA_GENERATION_DROP_ACTION,PersistenceUnitProperties.DROP_ONLY),
	DROP_AND_CREATE_TABLES(PersistenceUnitProperties.SCHEMA_GENERATION_DROP_AND_CREATE_ACTION,PersistenceUnitProperties.DROP_AND_CREATE);

	@Getter private final String _jpaAction;
	@Getter private final String _eclipseLinkAction;
	
	public static BBDDSchemaTablesDDLAction fromName(final String name) {
		return BBDDSchemaTablesDDLAction.valueOf(name.toUpperCase());
	}
}
