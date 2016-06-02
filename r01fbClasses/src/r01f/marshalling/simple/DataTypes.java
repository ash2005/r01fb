package r01f.marshalling.simple;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.enums.EnumWithCodeAndMultipleLabels;
import r01f.enums.EnumWithCodeAndMultipleLabelsWrapper;
import r01f.reflection.ReflectionUtils;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

public class DataTypes {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTES
/////////////////////////////////////////////////////////////////////////////////////////
	static final Pattern DATATYPE_PATTERN = Pattern.compile("(Map|Collection)?:?([a-zA-Z0-9.$]+)((?:\\(|\\[).*(?:\\)|\\]))?");
															//"(Map|Collection)?\\:?([a-zA-Z0-9.$]+)((?:\\(|\\[).*(?:\\)|\\]))?"
	static final Pattern DATE_PATTERN = Pattern.compile("[a-zA-Z]+(?:\\((.+)\\))?");
	static final Pattern ENUM_PATTERN = Pattern.compile("Enum(?:\\((.+)\\))?");
/////////////////////////////////////////////////////////////////////////////////////////
//	DATA TYPES
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Wraps the data type
     */
    @Accessors(prefix="_")
    @NoArgsConstructor(access=AccessLevel.PRIVATE)		// obligar a usar la factoría
    public static class DataType {
    					private String _text;			// Descripción del tipo de dato
    	@Getter @Setter private BeanMap _beanMap;		// se establece en la carga del mapeo (clase SimpleMarshallerMappings)
    	@Getter @Setter private String _name;
    	@Getter	@Setter private DataTypeEnum _typeDef;
    	@Getter @Setter	private Class<?> _type;			// Tipo del objeto
    	@Getter @Setter private boolean _canBeCreatedFromString;	// true if the object can be created from a string (has a valueOf method)
    	
    	// Cache to avoid regular expressions
    	@Getter	@Setter(AccessLevel.PROTECTED) private boolean _simple;		// true si es un objeto simple (string, long, int, etc)
    	@Getter @Setter(AccessLevel.PROTECTED) private boolean _map;		// true si es un mapa
    	@Getter	@Setter(AccessLevel.PROTECTED) private boolean _collection;	// true si es una lista, set o array
    	@Getter	@Setter(AccessLevel.PROTECTED) private boolean _enum;		// true si es un enum
    	@Getter	@Setter(AccessLevel.PROTECTED) private boolean _object;		// true si es un objeto complejo 
    	@Getter	@Setter(AccessLevel.PROTECTED) private boolean _date;		// true si es una fecha
    	@Getter @Setter(AccessLevel.PROTECTED) private boolean _javaType;	// true si es una definición de un tipo java (Class)
    	
    	public DataType(final String name,final DataTypeEnum typeDef) {
    		_name = name;
    		_typeDef = typeDef;
    	}
    	public boolean is(final Class<?> type) {
    		return _type == type;
    	}
    	public boolean isAnyOf(final Class<?>... types) {
    		boolean outIs = false;
    		for (Class<?> type : types) {
    			if (_type == type) {
    				outIs = true;
    				break;
    			}
    		}
    		return outIs;
    	}
    	public boolean isImplementingAnyOf(final Class<?>... types) {
    		boolean outIs = false;
    		for (Class<?> type : types) {
    			if (ReflectionUtils.isImplementing(_type,type)) {
    				outIs = true;
    				break;
    			}
    		}
    		return outIs;
    	}
    	public boolean isCollectionOrMap() {
    		return _collection || _map;
    	}
    	public boolean isXML() {
    		return _typeDef == DataTypeEnum.XML;
    	}
    	public boolean isInterface() {
    		return ReflectionUtils.isInterface(_type);
    	}
    	public boolean isAbstract() {
    		return ReflectionUtils.isAbstract(_type);
    	}
    	public boolean isInstanciable() {
    		return ReflectionUtils.isInstanciable(_type);
    	}
    	public ObjectType asObject() {
    		return (ObjectType)this;
    	}
    	public CollectionType asCollection() {
    		return (CollectionType)this;
    	}
    	public MapCollectionType asMap() {
    		return (MapCollectionType)this;
    	}
    	public EnumType asEnum() {
    		return (EnumType)this;
    	}
    	public DateType asDate() {
    		return (DateType)this;
    	}
    	public JavaClassType asJavaType() {
    		return (JavaClassType)this;
    	}
		@Override
		public String toString() { return _text; }

		public String debugInfo() {
			return Strings.of(_name).add(" -").add(_typeDef.getTypeNames()[0]).add("-").asString();
		}
		/**
		 * Factoria del tipo de dato a partir de su descripción
		 * <pre>
		 * 		- Tipos normales:	[nombre tipo]
		 * 		- Mapas:			Map:[tipo mapa](tipo key,tipo value)
		 * 		- Colecciones:		Collection:[tipo coleccion](tipo values)
		 * </pre>
		 * @param text
		 * @return
		 */
		public static DataType create(final String text) {
			DataType outDataType = null;
			
			String name = text;
			if (name == null) throw new IllegalArgumentException("Cannot create a DataType: the description cannot be known; maybe it's due to a unknown collection element type"); 
			if (name.equalsIgnoreCase("Object")) name = "java.lang.Object";
			DataTypeEnum typeDef = DataTypeEnum.fromTypeName(text);
			// -- Complex objects
			if (typeDef.is(DataTypeEnum.OBJECT)) {
				outDataType = new ObjectType(name,typeDef,
											 !Object.class.getName().equals(name));	// es complejo si NO es java.lang.Object
			}
			// -- Colecciones
			if (typeDef.isIn(DataTypeEnum.COLLECTION,DataTypeEnum.MAP,DataTypeEnum.ARRAY)) {
				// Establece el nombre de los elementos de la coleccion
				// ...en esta fase NO se puede establecer la referencia al DataType de los elementos de la colección
				String keyElsTypeName = null;	// solo mapas
				String valueElsTypeName = null;	// listas y mapas
				Matcher m = DATATYPE_PATTERN.matcher(name);
				if (m.find()) {
					if (typeDef == DataTypeEnum.ARRAY) {
						valueElsTypeName = m.group(2);
						outDataType = new CollectionType(m.group(2),
														 typeDef,
												 	 	 valueElsTypeName);
					} else if (typeDef == DataTypeEnum.COLLECTION) {
						valueElsTypeName = m.group(3) != null ? m.group(3).substring(1,m.group(3).length()-1) 
															  : DataTypeEnum.OBJECT.getTypeNames()[0];
						
						outDataType = new CollectionType(m.group(2),
														 typeDef,
												 	 	 valueElsTypeName);
					} else if (typeDef == DataTypeEnum.MAP) {
						String[] types = m.group(3) != null ? m.group(3).substring(1,m.group(3).length()-1).split(",")
															: new String[] {DataTypeEnum.OBJECT.getTypeNames()[0],DataTypeEnum.OBJECT.getTypeNames()[0]};
						keyElsTypeName = types.length == 2 ? types[0] : DataTypeEnum.OBJECT.getTypeNames()[0];
						valueElsTypeName = types.length == 2 ? types[1] : DataTypeEnum.OBJECT.getTypeNames()[0];
						
						outDataType = new MapCollectionType(m.group(2),
															typeDef,
															keyElsTypeName,valueElsTypeName);
					}
				}
			} 

			// -- Dates
			if (typeDef.isIn(DataTypeEnum.SQLDATE,DataTypeEnum.DATE)) {
				String dateFmt = null;
				Matcher m = DATE_PATTERN.matcher(name);
				if (m.find()) {
					dateFmt = m.group(1);	// en la definición se indica el tipo ej: Date(dd/MM/yyyy)
				} else {
					dateFmt = "millis";		// en la definición NO se indica el fomato
				}
				outDataType = new DateType(name,typeDef,dateFmt);
			}
			
			// -- Enums
			if (typeDef == DataTypeEnum.ENUM) {
				String enumTypeName = null;
				Matcher m = ENUM_PATTERN.matcher(name);
				if (m.find()) { 
					enumTypeName = m.group(1);
					if (enumTypeName == null) enumTypeName = "java.lang.Enum";
					outDataType = new EnumType(name,typeDef,
											   enumTypeName);
				} 
			}
			
			// -- Tipos java
			if (typeDef == DataTypeEnum.JAVACLASS) {
				outDataType = new JavaClassType(name,typeDef);
			}
			
			// Tipo simple
			if (outDataType == null) outDataType = new DataType(name,typeDef);
			boolean isSimple = (!typeDef.isIn(DataTypeEnum.COLLECTION,DataTypeEnum.MAP,DataTypeEnum.OBJECT));	// aqui entran tambien los enums y los Dates
			outDataType.setSimple(isSimple);
			
			// Finalmente intentar obtener el tipo
			Class<?> type = MappingReflectionUtils.typeOf(outDataType);
			outDataType.setType(type);
			
			// Cache if the object can be created from a String
			outDataType.setCanBeCreatedFromString(ReflectionUtils.canBeCreatedFromString(type));
			
			outDataType._text = text;	// Guardar la descripción (para debug)
			return outDataType;
		}
    }
///////////////////////////////////////////////////////////////////////////////
//	DATATYPE COLLECTION
///////////////////////////////////////////////////////////////////////////////
    @Accessors(prefix="_")
    public static abstract class CollectionTypeBase 
                         extends DataType {
    	
    	@Getter	@Setter	private DataType _valueElementsDataType;		// Se establece en la carga del mapeo (clase SimpleMarshallerMappings)
    	
    	public CollectionTypeBase(final String name,final DataTypeEnum type,
    						      final String elsTypeName) {
    		super(name,type);
    		_valueElementsDataType = DataType.create(elsTypeName);
    	}
    	public Class<?> getValueElementsType() {
    		return _valueElementsDataType.getType();
    	}
    }
    @Accessors(prefix="_")
    public static class CollectionType 
                extends CollectionTypeBase {
    	public CollectionType(final String name,final DataTypeEnum type,
    						  final String elsTypeName) {
    		super(name,type,elsTypeName);
    		this.setCollection(true);
    	}
    }
    @Accessors(prefix="_")
    public static class MapCollectionType 
                extends CollectionTypeBase {
    	@Getter	@Setter	private DataType _keyElementsDataType;		// Se establece en la carga del mapeo (clase SimpleMarshallerMappings)
    	
    	public MapCollectionType(final String name,final DataTypeEnum type,
    							 final String keyElsTypeName,final String valueElsTypeName) {
    		super(name,type,valueElsTypeName);
    		_keyElementsDataType = DataType.create(keyElsTypeName);
    		this.setMap(true);
    	}
    	public Class<?> getKeyElementsType() {
    		return _keyElementsDataType.getType();
    	}
    }
///////////////////////////////////////////////////////////////////////////////
//	DATATYPE OBJECT
///////////////////////////////////////////////////////////////////////////////
    @Accessors(prefix="_")
    public static class ObjectType 
    			extends DataType {
    	
    	@Getter	private boolean _knownType;		// true si es un objeto y es de tipo conocido
    	
    	public ObjectType(final String name,final DataTypeEnum type,
    					  final boolean knownType) {
    		super(name,type);
    		this.setObject(true);
    		_knownType = knownType;
    	}
    	/**
    	 * @return an array with final fields 
    	 */
    	public Map<String,FieldMap> finalFields() {
    		return super.getBeanMap().getFinalFields();
    	}
    	/**
    	 * @return an array with non final fields
    	 */
    	public Map<String,FieldMap> nonFinalFields() {
    		return super.getBeanMap().getNonFinalFields();
    	}
    	/**
    	 * @return all the data type fields
    	 */
    	public Map<String,FieldMap> fields() {
    		return super.getBeanMap().getFields();
    	}
    	/**
    	 * @return true if it has any mapped field
    	 */
    	public boolean hasFields() {
    		return CollectionUtils.hasData(super.getBeanMap().getFields());
    	}
    	/**
    	 * @return true si el objeto es inmutable (todos los campos son finales)
    	 */
    	public boolean isInmutable() {
    		return this.getBeanMap().getFields() != null && this.getBeanMap().getFinalFields() != null
    			&& this.getBeanMap().getFields().size() > 0 && this.getBeanMap().getFinalFields().size() > 0
    		    && this.getBeanMap().getFields().size() == this.getBeanMap().getFinalFields().size(); 
    	}
    	/**
    	 * @return true if the object has only a single simple final field
    	 */
    	public boolean hasOnlyOneFinalSimpleField() {
    		return this.finalFields().size() == 1					// with only a single final field
	        	&& CollectionUtils.of(this.finalFields())			// that is simple (String, int, long, etc)
	        	 				  .pickOneAndOnlyEntry().getValue()	// ... for example an OID object with a final String 
	        	 				  .getDataType().isSimple();
    	}
    	/**
    	 * @return the DataType of the single final simple field (supossing this type has only a single final field)
    	 */
    	public DataType getSingleFinalSimpleField() {
    		DataType outDataType = CollectionUtils.of(this.finalFields())
    	 				   					  	  .pickOneAndOnlyEntry().getValue()
    	 				   					  	  .getDataType();
    		return outDataType;
    	}
    	/**
    	 * @return true if the object has only a single simple field
    	 */
    	public boolean hasOnlyOneSimpleField() {
    		return this.fields() != null
    			&& this.fields().size() == 1				// with only a single field
	        	&& CollectionUtils.of(this.fields())		// that is simple (String, int, long, etc)
	        	 				  .pickOneAndOnlyEntry().getValue()			// ... for example an OID object with a final String 
	        	 				  .getDataType().isSimple();
    	}
    	/**
    	 * @return the DataType of the single simple field (supossing this type has only a single field)
    	 */
    	public DataType getSingleSimpleField() {
    		DataType outDataType = CollectionUtils.of(this.fields())
    	 				   					  	  .pickOneAndOnlyEntry().getValue()
    	 				   					  	  .getDataType();
    		return outDataType;
    	}
    }
///////////////////////////////////////////////////////////////////////////////
//	DATATYPE ENUM
///////////////////////////////////////////////////////////////////////////////
    @Accessors(prefix="_")
    public static class EnumType 
    			extends DataType {
    	@Getter private String _enumTypeName;	// tipo del enum
    	
    	public EnumType(final String name,final DataTypeEnum type,
    					final String enumTypeName) {
    		super(name,type);
    		this.setEnum(true);
    		_enumTypeName = enumTypeName;
    	}
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  DATATYPE CLASS
/////////////////////////////////////////////////////////////////////////////////////////
    @Accessors(prefix="_")
    public static class JavaClassType 
    			extends DataType {
    	
    	public JavaClassType(final String name,final DataTypeEnum type) {
    		super(name,type);
    		this.setJavaType(true);
    	}
    }
///////////////////////////////////////////////////////////////////////////////
//	DATATYPE DATE
///////////////////////////////////////////////////////////////////////////////
    @Accessors(prefix="_")
    public static class DateType 
    			extends DataType {
    	@Getter @Setter private String _dateFormat;		// formato de la fecha
    	
    	public DateType(final String name,final DataTypeEnum type,
    					final String dateFormat) {
    		super(name,type);
    		this.setDate(true);
    		_dateFormat = dateFormat;
    	}
    }
    
///////////////////////////////////////////////////////////////////////////////
//	DATA TYPES
///////////////////////////////////////////////////////////////////////////////
	/**
	 * Tipos de datos
	 */
    @Accessors(prefix = "_")
	public enum DataTypeEnum
	 implements EnumWithCodeAndMultipleLabels<Integer,DataTypeEnum> {
		NULL		(-1,"null"),
		STRING		(1,"java.lang.String","String"),		STRINGBUILDER	(2,"java.lang.StringBuilder","StringBuilder"),	STRINGBUFFER	(3,"java.lang.StringBuffer","StringBuffer"),
		INTEGER		(4,"java.lang.Integer","Integer"),		INTEGER_P		(5,"int"),
		LONG		(6,"java.lang.Long","Long"),			LONG_P			(7,"long"),
		DOUBLE		(8,"java.lang.Double","Double"),		DOUBLE_P		(9,"double"),
		FLOAT		(10,"java.lang.Float","Float"),			FLOAT_P			(11,"float"),
		NUMBER		(12,"java.lang.Number"),
		BOOLEAN		(13,"java.lang.Boolean","Boolean"),		BOOLEAN_P		(14,"boolean"),
		DATE		(15,"java.util.Date","Date"),
		SQLDATE		(16,"java.sql.Date","SQLDate"),
		OBJECT		(17,"java.lang.Object","Object"),
		XML			(18,"XML"),
		MAP			(19,"Map"),
		COLLECTION 	(20,"Collection"),
		ARRAY	 	(21,"Array"),
		ENUM	 	(22,"java.lang.Enum","Enum"),
		JAVACLASS	(23,"java.lang.Class","Class");	
		
			
		@Getter private final Integer _code;
		@Getter private final Class<Integer> _codeType = Integer.class;
		
		@Getter private final String[] _typeNames;  
		private DataTypeEnum(final int code,final String... typeNames) {
			_code = code;
			_typeNames = typeNames;
		}
		// --- Metodos de utilidad
		public boolean canBeFromTypeName(final String typeName) {
			return this.canBeFrom(typeName);
		} 
		// --- Metodos de EnumWithCodeAndMultipleLabels		
		@Override 
		public boolean is(final DataTypeEnum otherType) {
			return this == otherType;
		}
		@Override
		public boolean isIn(final DataTypeEnum... dataTypes) {
			return enums.isIn(this,dataTypes);
		}
		@Override 
		public boolean canBeFrom(final String desc) {
			return enums.canBeFrom(this,desc);
		}
		@Override
		public String getLabel() {
			return _typeNames != null && _typeNames.length > 0  ? _typeNames[0] : null;
		}
		@Override
		public String[] getLabels() {
			return _typeNames;
		}
		// --- Metodos estaticos 
		private static EnumWithCodeAndMultipleLabelsWrapper<Integer,DataTypeEnum> enums = new EnumWithCodeAndMultipleLabelsWrapper<Integer,DataTypeEnum>(DataTypeEnum.values());
																									//.strict();		// Lanza IllegalArgumentException si NO se encuentra un elemento en un método fromXX
		public static DataTypeEnum fromCode(final int code) {
			return enums.fromCode(code);
		}
		public static DataTypeEnum fromType(final Class<?> type) {
			DataTypeEnum outDataType = null;
			// Para los mapas, colecciones, arrays, etc el nombre del tipo NO vale...
			if (CollectionUtils.isMap(type)) {
				outDataType = DataTypeEnum.MAP;
			} else if (CollectionUtils.isCollection(type)) {
				outDataType = DataTypeEnum.COLLECTION;
			} else if (CollectionUtils.isArray(type)) {
				outDataType = DataTypeEnum.ARRAY;
			} else if (ReflectionUtils.isSubClassOf(type,Enum.class)) {
				outDataType = DataTypeEnum.ENUM;
			}
			// para el resto de tipos se puede seguir llamando al método obtención del tipo a partir del nombre
			else {
				outDataType = DataTypeEnum.fromTypeName(type.getClass().getName());
			}
			return outDataType;
		}
		public static DataTypeEnum fromTypeName(final String typeName) {
			DataTypeEnum theDataType = null;
			//  Tipos java: javaType 										ej: java.lang.String
			//       Mapas: Map:javaMapType(key java type,val java type) 	ej: Map:java.util.Map(java.lang.String,java.lang.String))
			// Colecciones: Collection:javaColType(val java type			ej: Collection:java.util.List(java.lang.String)
			Matcher m = Strings.of(typeName)
							   .matcher(DATATYPE_PATTERN);	//"(Map|Collection)?\\:?([a-zA-Z0-9.$]+)((?:\\(|\\[).*(?:\\)|\\]))?");
			if (m.find()) {
				String colType = m.group(1);			// Map | Collection
				String javaType = m.group(2);			// Java Type
				String arrayColMapType = m.group(3);	// array or java collection|map elements type
				if (colType != null) {
					// Componer una expresión regular que machea el tipo de colección
					if (colType.equalsIgnoreCase("Map")) {
						theDataType = DataTypeEnum.MAP;
					} else if (colType.equalsIgnoreCase("Collection")) {
						theDataType = DataTypeEnum.COLLECTION;
					} 
				} else if (arrayColMapType != null && arrayColMapType.equals("[]")) {
					theDataType = DataTypeEnum.ARRAY;
				} else {
					// Componer una expresión regular que machea el nombre del tipo
					Pattern p  = Pattern.compile(javaType);
					theDataType = enums.elementMatching(p);
					if (theDataType == null) theDataType = DataTypeEnum.OBJECT;
				}
			} else {
				throw new IllegalArgumentException("The provided java type is not valid");
			}
			return theDataType;
		}
		public static DataTypeEnum fromTypeName(Pattern regEx) {
			return enums.elementMatching(regEx);
		}
		public static DataTypeEnum fromName(String name) {
			return enums.fromName(name);
		}
	}
}
