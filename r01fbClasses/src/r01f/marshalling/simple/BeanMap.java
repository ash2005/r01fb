package r01f.marshalling.simple;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.marshalling.MarshallerException;
import r01f.marshalling.simple.DataTypes.DataType;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * Modela la configuración de mapeo de una clase de xml a objetos
 */
@Accessors(prefix="_")
class BeanMap {  
///////////////////////////////////////////////////////////////////////////////////////////
//  MIEMBROS
///////////////////////////////////////////////////////////////////////////////////////////    
    @Getter @Setter	private BeanXMLMap _xmlMap;
    @Getter @Setter private String _typeName;          			// Nombre de la clase  (incluyendo el paquete)
    @Getter @Setter	private DataType _dataType;					// Definición del tipo de dato (siempre será Object)
    @Getter @Setter private boolean _useAccessors = true;    	// Indica si hay que utilizar los metodos get/set
    @Getter @Setter private String _oidAccessorMethod;			// Método a llamar para obtener el oid
    @Getter 	    private Map<String,FieldMap> _fields;      	// Miembros de la clase indexados por el nombre del miembro 
    // Si se utilizan custom transformers en lugar de mapeo de campos...
    // IMPORTANTE:	Son SINGLETONS, es decir, para un beanMap concreto hay UNA SOLA instancia del transformer
    //				por lo tanto, los transformers, NO DEBEN TENER ESTADO
    @Getter @Setter private SimpleMarshallerCustomXmlTransformers _customXMLTransformers;
                
    				private Map<String,FieldMap> _attrFieldsByXmlNodeName;		// cache para indexar los atributos por tag
    				private Map<String,FieldMap> _elementsFieldsByXmlNodeName;	// cache para indexar los elementos por tag
    				private Map<String,FieldMap> _finalFields;					// cache de fields finales
    				private Map<String,FieldMap> _nonFinalFields;				// cache de fields NO finales
    				private FieldMap _oidField;									// cache del field que es oid
    				
//    public void setXmlMap(String nodeName) {
//    	_xmlMap = new BeanXMLMap(nodeName);
//    }
///////////////////////////////////////////////////////////////////////////////////////////
//  DEFINICION DEL MAPEO A XML
///////////////////////////////////////////////////////////////////////////////////////////
    @Accessors(prefix="_")
    @NoArgsConstructor @AllArgsConstructor
    class BeanXMLMap {    	
    	@Getter @Setter private String _nodeName;	// Nombre del nodo XML (elemento o atributo)
    	
	    /**
	     * Devuelve los fields mapeados como atributo
	     * @return
	     */
	    public Collection<FieldMap> getFieldsMappedAsXmlAttributes() {
	    	Collection<FieldMap> outColFields = null;
	    	if (_fields == null) return null;
	        for (FieldMap fm : _fields.values()) {
	        	if (fm.getXmlMap().isAttribute()) {
	        		if (outColFields == null) outColFields = Lists.newArrayList();
	        		outColFields.add(fm);
	        	}
	        }
	        return outColFields;
	    }
	    /**
	     * Devuelve los fields que proceden de un objeto que se expande como atributo
	     * @return
	     */
	    public Collection<FieldMap> getFieldsExpandedAsXmlAttributes() {
	    	Collection<FieldMap> outAttrsFromExpandedObjs = null;
	    	if (_fields == null) return null;
	        for (FieldMap fm : _fields.values()) {
	        	if (fm.getXmlMap().isAttribute() && fm.getXmlMap().isExpandableAsAttributes()) {
	        		if (outAttrsFromExpandedObjs == null) outAttrsFromExpandedObjs = Lists.newArrayList();
	        		outAttrsFromExpandedObjs.add(fm);
	        	}
	        }
	        return outAttrsFromExpandedObjs;
	    }
	    /**
	     * Checks if all object's fields are mapped as attributes
	     * @return
	     */
	    public boolean areAllFieldsMappedAsXmlAttributes() {
	    	boolean allAttributes = true;
	    	if (_fields == null) return false;
	        for (FieldMap fm : _fields.values()) {
	        	if (!fm.getXmlMap().isAttribute()) {
	        		allAttributes = false;
	        		break;
	        	}
	        }
	        return allAttributes;
	    }
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORES
///////////////////////////////////////////////////////////////////////////////////////////    
    /** 
     * Constructor vacío
     */
    public BeanMap() {
        super();
        _xmlMap = new BeanXMLMap();
    }
    /**
     * Constructor en base al nombre del tipo (incluido el paquete)
     * @param newType el nombre del tipo
     */
    public BeanMap(final String newTypeName) {
        this();
        _typeName = newTypeName;
        _dataType = DataType.create(_typeName);
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * @return true si el bean está transformado xm<->java utilizado un transformer customizado
     */
    public boolean isCustomXmlTransformed() {
    	return _customXMLTransformers != null && _customXMLTransformers.getXmlReadTransformer() != null && _customXMLTransformers.getXmlWriteTransformer() != null;
    }
    /** 
     * Devuelve la definición de un miembro a partir del nombre del nodo xml 
     * @param xmlNodeName Nombre del nodo XML
     * @param isAttribute true si el nodo es un atributo
     * @return la definición del mapeo del campo
     */
    public FieldMap getFieldFromXmlNode(final String xmlNodeName,
    									final boolean isAttribute) {
        if (_fields == null) return null;
        // Primero "tira" de cache...
        FieldMap outField = null;
        if (isAttribute && _attrFieldsByXmlNodeName != null) {
        	outField = _attrFieldsByXmlNodeName.get(xmlNodeName);
        } else if (_elementsFieldsByXmlNodeName != null) {
        	outField = _elementsFieldsByXmlNodeName.get(xmlNodeName);
        }
        return outField;        
    }
    /**
     * Obtiene un field que puede albergar el tipo que se pasa
     * @param type el tipo
     * @return el primer field que puede albergar el tipo que se pasa... si hay mas de un tipo devuelve SOLO el primero
     */
    public FieldMap getFieldForType(final Class<?> type) {
    	if (_fields == null) return null;
    	FieldMap outFieldMap = null;
    	for (FieldMap fm : _fields.values()) {
    		if (fm.getDataType().getType().isAssignableFrom(type)) {
    			outFieldMap = fm;
    			break;
    		}
    	}
    	return outFieldMap;
    }
    /**
     * Devuelve el miembro que es oid
     * @return el miembro que es oid
     */
    public FieldMap getOidField() {
        if (_fields == null) return null;
        return _oidField;
    }    
    /**
     * Devuelve la definición del miembro
     * @param fieldName Nombre del miembro
     * @return La definición del miembro
     */
    public FieldMap getField(final String fieldName) {
        if (_fields == null) return null;
        return _fields.get(fieldName);
    }
    /**
     * Devuelve los fields finales del bean
     * @return los fields finales
     */
    public Map<String,FieldMap> getFinalFields() {
    	return _finalFields;
    }
    /**
     * Devuelve los fields no finales del bean
     * @return
     */
    public Map<String,FieldMap> getNonFinalFields() {
    	return _nonFinalFields;
    }
    /**
     * Devuelve los fields tipo colección del bean
     * @return
     */
    public Collection<FieldMap> getCollectionOrMapFields() {
    	Collection<FieldMap> outColFields = null;
    	if (_fields == null) return null;
        for (FieldMap fm : _fields.values()) {
        	DataType dataType = fm.getDataType();
        	if (dataType.isCollection() || dataType.isMap()) {
        		if (outColFields == null) outColFields = Lists.newArrayList();
        		outColFields.add(fm);
        	}
        }
        return outColFields;
    }
    /**
     * Checks if all object's fields are mapped as attributes
     * @return
     */
    public boolean areAllFieldsMappedAsAttributes() {
    	boolean allAttributes = true;
    	if (_fields == null) return false;
        for (FieldMap fm : _fields.values()) {
        	if (!fm.getXmlMap().isAttribute()) {
        		allAttributes = false;
        		break;
        	}
        }
        return allAttributes;
    }
    /**
     * Establece un miembro en la clase como elemento
     * @param newField
     * @throws MarshallerException si el miembro que se está intentando introducir ya existia en el bean
     */
	public void addField(final FieldMap newField) throws MarshallerException {
		newField.setDeclaringBeanMap(this);		// asociar el field con este bean
        if (newField.getName() != null) {
            if (_fields == null) _fields = Maps.newLinkedHashMap();			// es importante mantener el orden
            FieldMap other = _fields.put(newField.getName(),newField);
            if (other != null) {
            	String msg = Strings.of("El miembro {} ya existe en la clase {}: hay dos miembros con el mismo nombre para la misma clase en el fichero de mapeo!")
            					    .customizeWith(newField.getName(),this.getTypeName())
            					    .asString();
            	throw new MarshallerException(msg);
            }
            // Cachear los fields finales
            if (newField.isFinal()) {
            	if (_finalFields == null) _finalFields = Maps.newLinkedHashMap();
            	_finalFields.put(newField.getName(),newField);
            } else {
            	if (_nonFinalFields == null) _nonFinalFields = Maps.newLinkedHashMap();
            	_nonFinalFields.put(newField.getName(),newField);
            }
        }
    }
//    /**
//     * Obtiene la definición los posibles miembros tipo colección que contienen objetos 
//     * englobados en un determinado tag
//     * @param beanTag nombre de la clase (incluyendo paquete) de los objetos contenidos en la coleccion
//     * @return la definición del mapeo del campo
//     */
//    public List<FieldMap> getFieldFromCollecionContaindedBeansXmlNode(final String beanTag) {
//        if (_fields == null || _colFieldsContainingBeansEnclosedByXmlNodeName == null) return null;
//        // Primero tirar de cache...
//        List<FieldMap> outFields = _colFieldsContainingBeansEnclosedByXmlNodeName.get(beanTag); 
//        if (outFields != null && !outFields.isEmpty()) {
//        	return outFields;
//        }
//        return null;  	
//    }
///////////////////////////////////////////////////////////////////////////////////////////
//	CACHE
///////////////////////////////////////////////////////////////////////////////////////////
	void initIndexes() {
        if (_fields == null) return;   
        
        // Inicializar el miembro oid
        for ( FieldMap fm : _fields.values() ) {
            if (fm.isOid()) {
            	_oidField = fm;
            	break;
            }
        }        
        
        // Inicializar el indice de atributos y elementos por tag xml
		Map<String,FieldMap> attrs = new HashMap<String,FieldMap>();
		Map<String,FieldMap> els = new HashMap<String,FieldMap>();
        for (FieldMap fm : _fields.values()) {
    		if (fm.getXmlMap().isAttribute()) {
    			attrs.put(fm.getXmlMap().getNodeName(),fm);
    		} else {
    			els.put(fm.getXmlMap().getNodeName(),fm);
    		} 
        }
        if (attrs.size() > 0) {
	        _attrFieldsByXmlNodeName = new LinkedHashMap<String,FieldMap>(attrs.size(),1F);
	        _attrFieldsByXmlNodeName.putAll(attrs);
        }
        if (els.size() > 0) {
			_elementsFieldsByXmlNodeName = new LinkedHashMap<String,FieldMap>(els.size(),1F);
			_elementsFieldsByXmlNodeName.putAll(els);
        }
        
// If uncomment Add to the fields list!!! > private Map<String,List<FieldMap>> _colFieldsContainingBeansEnclosedByXmlNodeName;
//        // Inicializar la cache de miembros tipo colección que contienen beans "englobados" en un tag xml
//        // (puede haber más de un miembro tipo colección que "contenga" beans "englobados" en un mismo tag xml)
//		Map<String,List<FieldMap>> colEls = new LinkedHashMap<String,List<FieldMap>>();
//        for (FieldMap fm : _fields.values()) {
//        	DataType dataType = fm.getDataType();
//        	if (dataType.isCollection() && !dataType.asCollection().getValueElementsDataType().isSimple()) {
//        		BeanMap colElsBeanMap = dataType.asCollection().getValueElementsDataType().getBeanMap();
//        		if (colElsBeanMap != null) { 	// si es null es una colección de tipos simples (String, etc) 
//        			String elXmlEnclosingTag = colElsBeanMap.getXmlMap().getNodeName();
//        			List<FieldMap> colFields = colEls.get(elXmlEnclosingTag);
//        			if (colFields == null) {
//        				colFields = new ArrayList<FieldMap>();
//        				colEls.put(elXmlEnclosingTag,colFields);
//        			}
//        			colFields.add(fm);
//        		}
//        	} else if ((dataType.isMap() && !dataType.asMap().getValueElementsDataType().isSimple())
//        			|| (dataType.getType() == LanguageTexts.class)) {
//        		BeanMap mapValueElsBeanMap = dataType.asMap().getValueElementsDataType().getBeanMap();
//        		if (mapValueElsBeanMap != null) { 	// si es null es una colección de tipos simples (String, etc) 
//        			String elXmlEnclosingTag = mapValueElsBeanMap.getXmlMap().getNodeName();
//        			List<FieldMap> colFields = colEls.get(elXmlEnclosingTag);
//        			if (colFields == null) {
//        				colFields = new ArrayList<FieldMap>();
//        				colEls.put(elXmlEnclosingTag,colFields);
//        			}
//        			colFields.add(fm);
//        		}
//        	}
//        }
//        if (colEls.size() > 0) {
//	        _colFieldsContainingBeansEnclosedByXmlNodeName = new LinkedHashMap<String,List<FieldMap>>(colEls.size(),1F);
//	        _colFieldsContainingBeansEnclosedByXmlNodeName.putAll(colEls);
//        }
	}
///////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
///////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Devuelve la representación en String de las clases
     */    
    public String debugInfo() {
    	return toXml();
    }
    /**
     * Devuelve la representación en XML del beanMap
     * @return el xml
     */
    public String toXml() {
    	String outXml = null;
    	if (this.isCustomXmlTransformed()) {
    		outXml = Strings.of("<class name='{}' fromElement='{}'>\r\n")
    						.add("\t<customXmlTransformers>\r\n")
    						.add("\t\t<xmlRead>{}</xmlRead>\r\n")
    						.add("\t\t<xmlWrite>{}</xmlWrite>\r\n")
    						.add("\t</customXmlTransformers>\r\n")
    						.add("</class>")
    						.customizeWith(_typeName,_xmlMap.getNodeName(),
    									   _customXMLTransformers.getXmlReadTransformer().getClass().getName(),
    									   _customXMLTransformers.getXmlWriteTransformer().getClass().getName())
    						.toString();
    	} else {
	    	String clsOpen = Strings.create(200).add("<class name='{}' fromElement='{}' useAccessors='{}'>")
	    									    .customizeWith(_typeName,
	    									    			   _xmlMap.getNodeName(),
	    									    			   Boolean.toString(_useAccessors))
	    									    .asString();
	    	String clsEnd = null;
	    	
	    	StringBuffer fields = new StringBuffer(600);
	    	if (CollectionUtils.hasData(_fields)) {
		    	for (FieldMap fm : _fields.values()) {
		    		fields.append("\r\n\t").append(fm.toXml());
		    	}
		    	clsEnd = "\r\n</class>";
	    	} else {
	    		clsEnd = "</class>";
	    	}
	    	outXml = clsOpen + fields.toString() + clsEnd;
    	}
    	return outXml;
    }
}
