package r01f.marshalling.simple;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.encoding.TextEncoder;

class XMLBuilder {
///////////////////////////////////////////////////////////////////////////////////////////
//  ELEMENTO XML
///////////////////////////////////////////////////////////////////////////////////////////
@Accessors(prefix="_")
@NoArgsConstructor
static class XMLElement {
    @Getter @Setter private XMLElement _parentNode;
    @Getter @Setter private String _tag;
    @Getter @Setter private String _text;
    @Getter @Setter private boolean _isCDATA;        				// El contenido del nodo es CDATA?
    @Getter @Setter private List<XMLElement> _childElements;		// Sub elementos
    @Getter @Setter private List<XMLAttribute> _attributes;			// Atributos
    @Getter @Setter private String _rawXml;	

//  :::::: FLUENT API PARA CREAR UN NODO    
    public static XMLElement create(final String tag) {
    	XMLElement outEl = new XMLElement();
    	outEl.setTag(tag);
    	return outEl;
    }
    public static XMLElement createRaw(final String xml) {
    	XMLElement outEl = new XMLElement();
    	outEl.setRawXml(xml);
    	return outEl;
    }
    public XMLElement withText(final String nodeText) {
    	this.setText(nodeText);
    	return this;
    }
    public XMLElement cdata(final boolean isCDATA) {
    	this.setCDATA(isCDATA);
    	return this;
    }
    public XMLElement addAttribute(final String attrName,final String attrValue) {
    	XMLAttribute newAttr = new XMLAttribute(attrName,attrValue);
    	if (_attributes == null) _attributes = new ArrayList<XMLAttribute>();
    	_attributes.add(newAttr);
    	return this;
    }
    public XMLElement newChildElement(final XMLElement childElement) {
    	if (_childElements == null) _childElements = new ArrayList<XMLElement>();
    	_childElements.add(childElement);
    	return childElement;
    } 
//  :::::: METODOS          
    /**
     * @return true si el nodo está vacio
     */
    public boolean isEmpty() {
        boolean empty = _rawXml == null ? (_text == null && _childElements == null && _attributes == null)
        								: false;
        return empty;
    }
    public StringBuilder asText() {
    	return this.asText(null);
    }
    public StringBuilder asText(final TextEncoder textEncoder) {
    	StringBuilder sb = null;
    	if (_rawXml == null) {
	        // Intentar "intuir" un tamaño...
	        int size = (_attributes != null && _attributes.size() > 0) ? _attributes.size() * 128 : 32;
	        size += (_childElements != null && _childElements.size() > 0) ? _childElements.size() * 256 : 32;
	        
	        sb = new StringBuilder(size);	        
	        sb.append( "<").append( _tag );
	        if (_attributes != null) {
	            for (XMLAttribute attr : _attributes) {
	                sb.append(" ").append(attr.asText(textEncoder));
	            }
	        }
	        if (_text == null && _childElements == null) {
	            sb.append("/>");
	        } else {
	            sb.append(">");
	            if (_isCDATA) sb.append("<![CDATA[");   // Si el nodo es CDATA, imprimir <![CDATA[
	            
	            if (_text != null) sb.append(textEncoder != null ? textEncoder.encode(_text) : _text);            
	            if (_childElements != null) {
	                for (XMLElement el : _childElements) { 
	                    sb.append( el.asText(textEncoder) );
	                }                    
	            }
	            if (_isCDATA) sb.append("]]>");         // Si el nodo es CDATA, imprimir ]]>
	            sb.append("</").append(_tag).append(">");
	        }
    	} else {
    		sb = new StringBuilder(_rawXml);
    	}
    	return sb;
    }   
}



///////////////////////////////////////////////////////////////////////////////////////////
//  ATRIBUTO DE UN ELEMENTO XML
///////////////////////////////////////////////////////////////////////////////////////////
@Accessors(prefix="_")
@NoArgsConstructor @AllArgsConstructor
static class XMLAttribute {
    @Getter @Setter private String _name;
    @Getter @Setter private String _value;
           
    /**
     * @return true si está vacio
     */
    public boolean isEmpty() {
        return (_value == null);
    }    
    public StringBuilder asText(TextEncoder textEncoder) {
    	StringBuilder attr = new StringBuilder("");
        if (_name != null) {
        	attr.append(_name).append("='")
        						  .append(textEncoder != null ? textEncoder.encode(_value) : _value)
        						  .append("'");
        }
        return attr;
    }

}
}
