package r01f.model.search.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.enums.EnumExtended;
import r01f.enums.EnumExtendedWrapper;
import r01f.exceptions.Throwables;
import r01f.marshalling.simple.SimpleMarshallerCustomXmlTransformers.XmlReadCustomTransformer;
import r01f.marshalling.simple.SimpleMarshallerCustomXmlTransformers.XmlWriteCustomTransformer;
import r01f.model.metadata.IndexableFieldID;
import r01f.model.search.query.BooleanQueryClause.QualifiedQueryClause;
import r01f.model.search.query.BooleanQueryClause.QueryClauseOccur;
import r01f.model.search.query.QueryClauseSerializerUtils.ContainedTextSpec;
import r01f.model.search.query.QueryClauseSerializerUtils.STRING_ESCAPE;
import r01f.reflection.ReflectionUtils;
import r01f.types.Range;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * Encapsulates all the xml<->java marshalling stuff for query clauses
 */
class QueryClauseXMLMarshallers {
/////////////////////////////////////////////////////////////////////////////////////////
// 	BooleanQueryClause > QualifiedQueryClause
/////////////////////////////////////////////////////////////////////////////////////////
	public static final class QualifiedQueryClauseCustomMarshaller<Q extends QueryClause> 
				   implements XmlReadCustomTransformer<QualifiedQueryClause<Q>>,
		 				      XmlWriteCustomTransformer {
		
		@Override @SuppressWarnings("unchecked")
		public String xmlFromBean(final boolean isAttribute,
								  final Object bean) {
			QualifiedQueryClause<Q> qry = (QualifiedQueryClause<Q>)bean;
			if (qry.getClause() instanceof BooleanQueryClause) {
				//BooleanQueryClause boolQry = (BooleanQueryClause)qry.getClause();
				throw new UnsupportedOperationException("Nested BooleanQueries are NOT supported... for the moment");
			} else if (qry.getClause() instanceof HasDataQueryClause) {
				// HasData
				return Strings.of("<clause predicate='{}' " + 
										      "occur='{}' " +
						                "forMetaData='{}'/>")
							  .customizeWith(PREDICATE.fromQuery(qry.getClause()).name(),
									  		 qry.getOccur().name(),
									  		 qry.getClause().getFieldId())
							  .asString();
			} else {
				// NOT a boolean query clause
				return Strings.of("<clause predicate='{}' " + 
										      "occur='{}' " +
						                "forMetaData='{}' " + 
										       "type='{}'>" + 
						           		"{}" + 
								  "</clause>")
							  .customizeWith(PREDICATE.fromQuery(qry.getClause()).name(),
									  		 qry.getOccur().name(),
									  		 qry.getClause().getFieldId(),
									  		 qry.getClause().getValueType().getName(),
									  		 QueryClauseSerializerUtils.serializeValue(qry.getClause(),STRING_ESCAPE.XML))
							  .asString();
			}
		}	
		
		private static final Pattern CLAUSE_XML_PATTERN = Pattern.compile("<clause predicate=['\\\"](" + PREDICATE.pattern() + ")['\\\"]" + " " + 	// [1] predicateType
																				      "occur=['\\\"](MUST|MUST_NOT|SHOULD)['\\\"]" + " " +			// [2] occur
																			    "forMetaData=['\\\"]([^'\\\"]+)['\\\"]" + " " + 					// [3] metaData
  																				       "type=['\\\"]([^'\\\"]+)['\\\"]" + ">" + 					// [4] data type
																			    "(?:<!\\[CDATA\\[)?(.+?)(?:\\]\\]>)?" + 							// [5] value
																	      "<\\/clause>");
		private static final Pattern HASDATA_CLAUSE_XML_PATTERN = Pattern.compile("<clause predicate=['\\\"](" + PREDICATE.pattern() + ")['\\\"]" + " " + 	// [1] predicateType
																				              "occur=['\\\"](MUST|MUST_NOT|SHOULD)['\\\"]" + " " +			// [2] occur
																			            "forMetaData=['\\\"]([^'\\\"]+)['\\\"]/>");							// [3] metaData
		
		@Override @SuppressWarnings("unchecked")
		public QualifiedQueryClause<Q> beanFromXml(final boolean isAttribute,
												   final CharSequence xml) {
			// Find the matcher 
			Matcher m = CLAUSE_XML_PATTERN.matcher(xml);
			if (!m.find()) {
				m = HASDATA_CLAUSE_XML_PATTERN.matcher(xml);
				if (!m.find()) return null; //throw new IllegalArgumentException(xml + " does NOT match a clause pattern");
			}
			
			// Find the components
			Q qryClause = null;
			PREDICATE predicateType = PREDICATE.fromName(m.group(1));
			QueryClauseOccur occur = QueryClauseOccur.fromName(m.group(2));
			IndexableFieldID fieldId = new IndexableFieldID(m.group(3));
			Class<?> dataType = m.groupCount() >= 4 ? ReflectionUtils.typeFromClassName(m.group(4)) : null;
			String valueStr = m.groupCount() >= 5 ? m.group(5) : null;
			switch(predicateType) {
			case CONTAINED:
				qryClause = (Q)ContainedInQueryClause.forField(fieldId)
													 .within(QueryClauseSerializerUtils.spectrumArrayFromString(valueStr,
															 				   				  			        dataType));
				break;
			case EQUALS:
				qryClause = (Q)EqualsQueryClause.forField(fieldId)
												.of(QueryClauseSerializerUtils.instanceFromString(valueStr,
																	 			   				  dataType));
				break;
			case RANGE:
				qryClause = (Q)RangeQueryClause.forField(fieldId)
											   .of(Range.unsafeParse(valueStr,
													   		   		 dataType));
				break;
			case CONTAINS:
				ContainedTextSpec containedTextSpec = ContainedTextSpec.fromSerializedFormat(valueStr);
				qryClause = (Q)ContainsTextQueryClause.forField(fieldId)
													  .forSpec(containedTextSpec);
				break;
			case HAS_DATA:
				qryClause = (Q)HasDataQueryClause.forField(fieldId);
				break;
			case BOOLEAN:
			default:
				throw new IllegalArgumentException("The query clause " + xml + " does NOT have a valid format");
			}
			// Create the qualified query clause 
			QualifiedQueryClause<?> outClause = new QualifiedQueryClause<Q>(qryClause,occur);
			return (QualifiedQueryClause<Q>)outClause;
		}
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONTAINS TEXT
/////////////////////////////////////////////////////////////////////////////////////////
	public static final class ContainsTextQueryXMLCustomMarshallers 
		           implements XmlReadCustomTransformer<ContainsTextQueryClause>,
		 				      XmlWriteCustomTransformer {
		@Override
		public String xmlFromBean(final boolean isAttribute,
								  final Object bean) {
			ContainsTextQueryClause containsQry = (ContainsTextQueryClause)bean;
			return Strings.of("<containsTextClause forMetaData='{}'>{}</containsTextClause>")
						  .customizeWith(containsQry.getFieldId(),
								  		 QueryClauseSerializerUtils.serializeValue(containsQry,STRING_ESCAPE.XML))
						  .asString();
		}
		
		// <containsTextClause forMetaData='{}' type='{}'>{}</containsTextClause> = <containsTextClause forMetaData=['\"]([^'\"]+)['\"]>(.+)<\/containsTextClause>
		private static final Pattern CONTAINS_XML_PATTERN = Pattern.compile("<containsTextClause forMetaData=['\\\"]([^'\\\"]+)['\\\"]>" +
																					"(.+)" + 
																			"<\\/containsTextClause>");
		
		@Override
		public ContainsTextQueryClause beanFromXml(final boolean isAttribute,
												   final CharSequence xml) {
			Matcher m = CONTAINS_XML_PATTERN.matcher(xml);
			if (m.find()) {
				IndexableFieldID fieldId = new IndexableFieldID(m.group(1));
				ContainedTextSpec containsTextSpec = ContainedTextSpec.fromSerializedFormat(m.group(2));
				return new ContainsTextQueryClause(fieldId,
							  		  	           containsTextSpec);
			} 
			throw new IllegalArgumentException(Throwables.message("{} is NOT a legal XML representation of the {} clause",xml,ContainsTextQueryClause.class));
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  EQUALS
/////////////////////////////////////////////////////////////////////////////////////////
	public static final class EqualsQueryXMLCustomMarshallers<T>
		 	       implements XmlReadCustomTransformer<EqualsQueryClause<T>>,
		 				      XmlWriteCustomTransformer {
	
		@Override @SuppressWarnings("unchecked")
		public String xmlFromBean(final boolean isAttribute,
								  final Object object) {
			EqualsQueryClause<T> eqQry = (EqualsQueryClause<T>)object;
			Class<?> dataType = eqQry.getValue().getClass();
			return Strings.of("<equalsClause forMetaData='{}' type='{}'>{}</equalsClause>")
						  .customizeWith(eqQry.getFieldId(),dataType.getName(),
								  		 QueryClauseSerializerUtils.serializeValue(eqQry,STRING_ESCAPE.XML))
						  .asString();
		}
		
		// <equalsClause forMetaData='{}' type='{}'>{}</equalsClause> = <equalsClause forMetaData=['\"]([^'\"]+)['\"] type=['\"]([^'\"]+)['\"]>(.+)<\/equalsClause>
		private static final Pattern EQ_XML_PATTERN = Pattern.compile("<equalsClause forMetaData=['\\\"]([^'\\\"]+)['\\\"] " + 
																						   "type=['\\\"]([^'\\\"]+)['\\\"]>" + 
																			"(.+)" + 
																	  "<\\/equalsClause>");
		
		@Override 
		public EqualsQueryClause<T> beanFromXml(final boolean isAttribute,
												final CharSequence xml) {
			Matcher m = EQ_XML_PATTERN.matcher(xml);
			if (m.find()) {
				IndexableFieldID fieldId = new IndexableFieldID(m.group(1));
				Class<T> dataType = ReflectionUtils.typeFromClassName(m.group(2));
				T value = QueryClauseSerializerUtils.instanceFromString(m.group(3),
										   				 				dataType);
				return new EqualsQueryClause<T>(fieldId,
							  		  	        value);
			} 
			throw new IllegalArgumentException(Throwables.message("{} is NOT a legal XML representation of the {} clause",xml,EqualsQueryClause.class));
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CONTAINED IN
/////////////////////////////////////////////////////////////////////////////////////////
	public static final class ContainedInQueryXMLCustomMarshallers<T>
		 	       implements XmlReadCustomTransformer<ContainedInQueryClause<T>>,
		 				      XmlWriteCustomTransformer {
		
		@Override @SuppressWarnings("unchecked")
		public String xmlFromBean(final boolean isAttribute,
								  final Object object) {
			ContainedInQueryClause<T> containedInQry = (ContainedInQueryClause<T>)object;
			Class<?> dataType = containedInQry.getSpectrum().getClass().getComponentType();
			return Strings.of("<containedInClause forMetaData='{}' type='{}'>{}</containedInClause>")
						  .customizeWith(containedInQry.getFieldId(),dataType.getName(),
								  		 QueryClauseSerializerUtils.serializeValue(containedInQry,STRING_ESCAPE.NONE))
						  .asString();
		}
		
		// <containedInClause forMetaData='{}' type='{}'>{}</containedInClause> = <containedInClause forMetaData=['\"]([^'\"]+)['\"] type=['\"]([^'\"]+)['\"]>(.+)<\/containedInClause>
		private static final Pattern CONTAINEDIN_XML_PATTERN = Pattern.compile("<containedInClause forMetaData=['\\\"]([^'\\\"]+)['\\\"] " +
																									     "type=['\\\"]([^'\\\"]+)['\\\"]>" +
																						"(.+)" + 
																			   "<\\/containedInClause>");
		
		@Override 
		public ContainedInQueryClause<T> beanFromXml(final boolean isAttribute,
													 final CharSequence xml) {
			Matcher m = CONTAINEDIN_XML_PATTERN.matcher(xml);
			if (m.find()) {
				IndexableFieldID fieldId = new IndexableFieldID(m.group(1));
				Class<T> dataType = ReflectionUtils.typeFromClassName(m.group(2));
				T[] spectrum = QueryClauseSerializerUtils.spectrumArrayFromString(m.group(3),
												 			    			      dataType);
				return new ContainedInQueryClause<T>(fieldId,
						  		  					 spectrum);
			} 
			throw new IllegalArgumentException(Throwables.message("{} is NOT a legal XML representation of the {} clause",xml,ContainedInQueryClause.class));
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  HASDATA
/////////////////////////////////////////////////////////////////////////////////////////
	public static final class HasDataQueryXMLCustomMarshallers
		 	       implements XmlReadCustomTransformer<HasDataQueryClause>,
		 				      XmlWriteCustomTransformer {
	
		@Override 
		public String xmlFromBean(final boolean isAttribute,
								  final Object object) {
			HasDataQueryClause hasDataQry = (HasDataQueryClause)object;
			return Strings.of("<hasDataClause forMetaData='{}'/>")
						  .customizeWith(hasDataQry.getFieldId())
						  .asString();
		}
		
		// <hasDataClause forMetaData='{}'/> = <hasDataClause forMetaData=['\"]([^'\"]+)['\"]/>
		private static final Pattern HASDATA_XML_PATTERN = Pattern.compile("<hasDataClause forMetaData=['\\\"]([^'\\\"]+)['\\\"]/>");
		
		@Override 
		public HasDataQueryClause beanFromXml(final boolean isAttribute,
											  final CharSequence xml) {
			Matcher m = HASDATA_XML_PATTERN.matcher(xml);
			if (m.find()) {
				IndexableFieldID fieldId = new IndexableFieldID(m.group(1));
				return new HasDataQueryClause(fieldId);
			} 
			throw new IllegalArgumentException(Throwables.message("{} is NOT a legal XML representation of the {} clause",xml,HasDataQueryClause.class));
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  RANGE
/////////////////////////////////////////////////////////////////////////////////////////
	protected static final class RangeQueryXMLCustomMarshallers<T extends Comparable<T>>
		 	    implements XmlReadCustomTransformer<RangeQueryClause<T>>,
		 			       XmlWriteCustomTransformer {
		
		@Override @SuppressWarnings("unchecked")
		public String xmlFromBean(final boolean isAttribute,
								  final Object object) {
			RangeQueryClause<T> rangeQry = (RangeQueryClause<T>)object;
			Class<T> rangeDataType = rangeQry.getRange().getDataType();
			return Strings.of("<rangeClause forMetaData='{}' type='{}'>{}</rangeClause>")
						  .customizeWith(rangeQry.getFieldId(),rangeDataType.getName(),
								  		 QueryClauseSerializerUtils.serializeValue(rangeQry,STRING_ESCAPE.NONE))
						  .asString();
		}
		
		// <rangeClause forMetaData='{}' type='{}'>{}</rangeClause> = <rangeClause forMetaData=['\"]([^'\"]+)['\"] type=['\"]([^'\"]+)['\"]>(.+)<\/rangeClause>
		private static final Pattern RANGE_XML_PATTERN = Pattern.compile("<rangeClause forMetaData=['\\\"]([^'\\\"]+)['\\\"] type=['\\\"]([^'\\\"]+)['\\\"]>(.+)<\\/rangeClause>");
		
		@Override
		public RangeQueryClause<T> beanFromXml(final boolean isAttribute,
											   final CharSequence xml) {
			Matcher m = RANGE_XML_PATTERN.matcher(xml);
			if (m.find()) {
				IndexableFieldID fieldId = new IndexableFieldID(m.group(1));
				Class<T> rangeDataType = ReflectionUtils.typeFromClassName(m.group(2));
				String rangeStr = m.group(3);
				Range<T> range = Range.parse(rangeStr,
								  		     rangeDataType);
				RangeQueryClause<T> outRangeQry = new RangeQueryClause<T>(fieldId,
																  		  range);
				return outRangeQry;
			} 
			throw new IllegalArgumentException(Throwables.message("{} is NOT a legal XML representation of the {} clause",xml,RangeQueryClause.class));
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	@RequiredArgsConstructor
	private enum PREDICATE 
	  implements EnumExtended<PREDICATE> {
		EQUALS,
		CONTAINED,
		CONTAINS,
		HAS_DATA,
		RANGE,
		BOOLEAN;
		
		
		private static EnumExtendedWrapper<PREDICATE> _enums = EnumExtendedWrapper.create(PREDICATE.class);
		
		public static String pattern() {
			return CollectionUtils.of(PREDICATE.values()).toStringSeparatedWith('|');
		}
		public static PREDICATE fromName(final String name) {
			return _enums.fromName(name);
		}
		public static PREDICATE fromQuery(final QueryClause clause) {
			PREDICATE outPredicate = null;
			if (clause instanceof EqualsQueryClause) {
				outPredicate = EQUALS;
			} else if (clause instanceof ContainsTextQueryClause) {
				outPredicate = CONTAINS;
			} else if (clause instanceof ContainedInQueryClause) {
				outPredicate = CONTAINED;
			} else if (clause instanceof HasDataQueryClause) {
				outPredicate = HAS_DATA;
			} else if (clause instanceof RangeQueryClause) {
				outPredicate = RANGE;
			} else if (clause instanceof BooleanQueryClause) {
				outPredicate = BOOLEAN;
			}
			return outPredicate;
		}
		@Override
		public boolean isIn(PREDICATE... els) {
			return _enums.isIn(this,els);
		}
		@Override
		public boolean is(PREDICATE el) {
			return _enums.is(this,el);
		}
	}
}
