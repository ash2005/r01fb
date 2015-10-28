package r01f.marshalling.simple;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.enums.EnumWithCodeAndLabel;
import r01f.enums.EnumWithCodeAndLabelWrapper;
import r01f.marshalling.simple.DataTypes.DataType;
import r01f.util.types.Strings;
import r01f.util.types.Strings.StringExtended;

/**
 * MemberMap.java
 * Modela el mapeo de un miembro de una clase a partir de un documento xml
 * que lo define
 */
@Accessors(prefix="_")
     class FieldMap 
implements Debuggable {
///////////////////////////////////////////////////////////////////////////////////////////
//  MIEMBROS
///////////////////////////////////////////////////////////////////////////////////////////
    @Getter @Setter private FieldXMLMap _xmlMap;			// Mapeo en el XML 
    @Getter @Setter private BeanMap _declaringBeanMap;     	// Definicion de la clase del miembro
    @Getter @Setter private String _name;                   // Nombre del miembro
    @Getter @Setter private DataType _dataType;             // Tipo de dato
    @Getter @Setter private boolean _final;					// true si el miembro es final
    @Getter @Setter private Relation _relation; 			// Código del tipo de relación (-1 si no tiene relación)
    @Getter @Setter private String _createMethod;     		// Metodo para crear el objeto
    @Getter @Setter private boolean _oid = false;          	// ¿El miembro es un OID de la clase ?    
    @Getter @Setter private boolean _tranzient = false;    	// ¿El miembro es transient? Los miembros transient no se serializan a XML
    
//    public void setXmlMap(String nodeName,
//    					  boolean isAttribute,boolean isCDATA,
//    					  String colElsNodeName) {
//    	_xmlMap = new FieldXMLMap(nodeName,isAttribute,isCDATA,colElsNodeName);
//    }
///////////////////////////////////////////////////////////////////////////////////////////
//  DEFINICION DEL MAPEO A XML
///////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
    @NoArgsConstructor @AllArgsConstructor
    class FieldXMLMap {    	
    	@Getter @Setter private String _nodeName;			// Nombre del nodo XML (elemento o atributo)
    	@Getter @Setter private boolean _explicitNodeName;	// indica si el nombre del nodo se ha definido explicitamente o se ha calculado a partir del nombre del miembro
	    @Getter @Setter private boolean _attribute;					// Indica si en el XML es un atributo
	    @Getter @Setter private boolean _expandableAsAttributes;	// Si el objeto tiene varios miembros, estos son "expandidos" como atributos	
	    @Getter @Setter private boolean _cdata;    			// El miembro en XML es de tipo CDATA 
	    @Getter @Setter private String _valueToIgnoreWhenWritingXML;		// Si el valor del field coincide con este valor, el field NO se serializa en el XML	
	    @Getter @Setter private String _discriminatorWhenNotInstanciable;	// Nombre del atributo que hay que incluir en el 
	    																	// xml correspondiente al campo si el tipo NO es instanciable
	    @Getter @Setter private String _colElsNodeName;	// Si el campo es una colección, aquí se indica el nombre de los nodos 
	    												// de cada uno de los elementos de la colección
	    												// Es util en el caso de colecciones de tipos simples (String, long, xml, etc)
	    												// Ejemplo: 	<myCollection>
	    												//					<item>Valor 1</item>
	    												//					<item>Valor 2</item>
	    												//				<myCollection>
	    @Getter @Setter private boolean _explicitColElsNodeName;	// indica si el nombre del nodo se ha definido explicitamente o se ha calculado a partir del nombre del miembro
	    
	    public FieldXMLMap(final FieldXMLMap other) {
	    	_nodeName = other.getNodeName();
	    	_explicitNodeName = other.isExplicitNodeName();
	    	_attribute = other.isAttribute();
	    	_expandableAsAttributes = other.isExpandableAsAttributes();
	    	_cdata = other.isCdata();
	    	_valueToIgnoreWhenWritingXML = other.getValueToIgnoreWhenWritingXML();
	    	_discriminatorWhenNotInstanciable = other.getDiscriminatorWhenNotInstanciable();
	    	_colElsNodeName = other.getColElsNodeName();
	    	_explicitColElsNodeName = other.isExplicitColElsNodeName();
	    }
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORES
///////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Constructor por defcto
     */
    public FieldMap() {
    	_xmlMap = new FieldXMLMap();
        // Por defecto no tiene metodo create
        _createMethod = null;
        // Por defecto no es un oid
        _oid = false;             
    }
    /**
     * Constructor from othero fieldMap
     * @param other
     */
    public FieldMap(final FieldMap other) {
    	_xmlMap = new FieldXMLMap(other.getXmlMap());	// clone!
    	_declaringBeanMap = other.getDeclaringBeanMap();
    	_name = other.getName();
    	_dataType = other.getDataType();
    	_final = other.isFinal();
    	_relation = other.getRelation();
    	_createMethod = other.getCreateMethod();
    	_oid = other.isOid();
    	_tranzient = other.isTranzient();
    }
    /**
     * Constructor en base al 
     * @param newName nombre del campo
     * @param newDataType tipo de dato tipo de dato
     * @param isOid true si es oid 
     */
    public FieldMap(final String newName,final DataType newDataType,
    				final boolean isOid) {
        this();
        _name = newName;
        _dataType = newDataType;
        _oid = isOid; 
    }    
    @Override   
    public String debugInfo() {
    	return this.toXml();
    }    
    
///////////////////////////////////////////////////////////////////////////////
//	RELACIONES
/////////////////////////////////////////////////////////////////////////////// 
    /**
     * Encapsula el tipo de Relacion 
     */
    @Accessors(prefix="_")
    @NoArgsConstructor(access=AccessLevel.PRIVATE)	// Para obligar a utilizar la factoría
    public static class Relation {
    	@Getter @Setter private String _name;
    			@Setter	private RelationEnum _relation;   
		/**
		 * Factoria de la relacion de dato a partir de su descripción
		 * @param text
		 * @return
		 */
		public static Relation create(String text) {
			Relation outRelation = new Relation();
			outRelation.setName(text);
			outRelation.setRelation(RelationEnum.fromRelationName(text));
			return outRelation;
		}
		@Override
		public String toString() { return _name; }	
		public boolean is(RelationEnum relation) { return _relation == relation; }
		public boolean isIn(RelationEnum... relations) { return _relation.isIn(relations); }		
    }    
	/**
	 * Relaciones
	 */
    @Accessors(prefix="_")
	@RequiredArgsConstructor 
	public enum RelationEnum implements EnumWithCodeAndLabel<Integer,RelationEnum> {
		NO(-1,"no_relation"),
		COMPOSITION(0,"composition"),
		AGGREGATION(1,"aggregation"); 	 			
		
		@Getter private final Integer _code;
		@Getter private final Class<Integer> _codeType = Integer.class;	
		
		@Getter private final String _relationName;
		
		// --- Metodos EnumWithCodeAndLabel
		@Override
		public boolean is(RelationEnum rel) {
			return this == rel;
		}
		@Override
		public boolean isIn(RelationEnum... rels) {
			return enums.isIn(this,rels);
		}	
		@Override
		public boolean canBeFrom(String desc) {
			return enums.canBeFrom(desc);
		}
		@Override
		public String getLabel() {
			return _relationName;
		}
		// --- Metodos estaticos
		private static EnumWithCodeAndLabelWrapper<Integer,RelationEnum> enums = new EnumWithCodeAndLabelWrapper<Integer,RelationEnum>(RelationEnum.values()).strict();		
		public static RelationEnum fromCode(int code) {
			return enums.fromCode(code);
		}
		public static RelationEnum fromRelationName(String relationName) {
			return enums.from(relationName);
		}
		public static RelationEnum fromName(String name) {
			return enums.fromName(name);
		}		
	}
///////////////////////////////////////////////////////////////////////////////
//	DEBUG
///////////////////////////////////////////////////////////////////////////////
    /**
     * Devuelve la representación de un {@link FieldMap} en formato XML
     * @return
     */
    public String toXml() {
    	StringExtended outField = Strings.create(200);
    	if (_xmlMap.isAttribute()) {
    		outField.add("<member name='{}' dataType='{}' fromAttribute='{}'")
    				.customizeWith(_name,_dataType.toString(),_xmlMap.getNodeName());
    	} else {
    		outField.add("<member name='{}' dataType='{}' fromElement='{}'")
    				.customizeWith(_name,_dataType.toString(),_xmlMap.getNodeName());
    		if (!_xmlMap.isExplicitNodeName()) outField.add(" explicitXmlElementMap='false'");
    		if ((_dataType.isCollection() || _dataType.isMap())
    		 && !Strings.isNullOrEmpty(_xmlMap.getColElsNodeName())) {
    			outField.add(" ofElements='{}'")
    					.customizeWith(_xmlMap.getColElsNodeName());
    			if (!_xmlMap.isExplicitColElsNodeName()) outField.add(" explicitCollectionItemsXmlElementMap='false'");
    		} else if (_dataType.isEnum()) {
    			outField.add(" enum='true'");
    		}
    		if (_xmlMap.getDiscriminatorWhenNotInstanciable() != null) outField.add(" discriminatorWhenNotInstanciable='").add(_xmlMap.getDiscriminatorWhenNotInstanciable()).add("'");
    		if (_xmlMap.isCdata()) outField.add(" isCDATA='true'");
    		if (_xmlMap.getValueToIgnoreWhenWritingXML() != null) outField.add(" doNotWriteXmlIfValueEquals='").add(_xmlMap.getDiscriminatorWhenNotInstanciable()).add("'");
    	}
    	if (_final) outField.add(" isFinal='true'");
		outField.add("/>");
    	return outField.asString();
    }
}

