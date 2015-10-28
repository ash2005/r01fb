package r01f.model.search;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.guids.CommonOIDs.UserCode;
import r01f.guids.OID;
import r01f.locale.Language;
import r01f.model.ModelObject;
import r01f.model.metadata.FieldMetaData;
import r01f.model.metadata.FieldMetaDataID;
import r01f.model.metadata.HasFieldsMetaDataForHasFullTextSummaryModelObject;
import r01f.model.metadata.IndexableFieldID;
import r01f.model.metadata.ModelObjectTypeMetaData;
import r01f.model.metadata.ModelObjectTypeMetaDataBuilder;
import r01f.model.search.query.BooleanQueryClause;
import r01f.model.search.query.BooleanQueryClause.QualifiedQueryClause;
import r01f.model.search.query.BooleanQueryClause.QueryClauseOccur;
import r01f.model.search.query.ContainedInQueryClause;
import r01f.model.search.query.ContainsTextQueryClause;
import r01f.model.search.query.ContainsTextQueryClause.ContainedTextAt;
import r01f.model.search.query.EqualsQueryClause;
import r01f.model.search.query.HasDataQueryClause;
import r01f.model.search.query.QueryClause;
import r01f.model.search.query.RangeQueryClause;
import r01f.reflection.ReflectionUtils;
import r01f.types.Range;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

@Slf4j
@Accessors(prefix="_")
public abstract class SearchFilterForModelObjectBase<SELF_TYPE extends SearchFilterForModelObjectBase<SELF_TYPE>>
    	   implements SearchFilterForModelObject {

	private static final long serialVersionUID = -6979312697491380544L;

/////////////////////////////////////////////////////////////////////////////////////////
//  SERIALIZABLE FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The language in which the user is doing the filter
	 */
	@XmlAttribute(name="uiLanguage")
	@Getter @Setter private Language _UILanguage;
	/**
	 * The query that wraps all the clauses
	 */
	@XmlElement
	@Getter @Setter private BooleanQueryClause _booleanQuery;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  NOT SERIALIZABLE FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlTransient
	@Getter private transient Collection<Class<? extends ModelObject>> _modelObjectTypes;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public SearchFilterForModelObjectBase(final Class<? extends ModelObject> modelObjectType) {
		Collection<Class<? extends ModelObject>> modelObjectTypes = new HashSet<Class<? extends ModelObject>>(1);
		modelObjectTypes.add(modelObjectType);
		this.setModelObjectTypesToBeFiltered(modelObjectTypes);
	}
	public SearchFilterForModelObjectBase(final Class<? extends ModelObject>... modelObjectTypes) {
		this.setModelObjectTypesToBeFiltered(Arrays.asList(modelObjectTypes));
	}
	public SearchFilterForModelObjectBase(final Collection<Class<? extends ModelObject>> modelObjectTypes) {
		this.setModelObjectTypesToBeFiltered(modelObjectTypes);
	}
	protected <F extends SearchFilterForModelObjectBase<F>> void _copy(final F other) {
		_UILanguage = other.getUILanguage();
		_booleanQuery = other.getBooleanQuery();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  UI LANGUAGE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the language the user is searching in... this could NOT be the same
	 * as the language the user is filtering objects
	 * @param lang
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public SELF_TYPE theUserIsSearchingIn(final Language lang) {
		_UILanguage = lang;
		return (SELF_TYPE)this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FILTERED OBJECT TYPES
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Collection<Class<? extends ModelObject>> getFilteredModelObjectTypes() {
		return _getFilteredModelObjectTypes();
	}
	/**
	 * Sets the model object types to filter by
	 * @param modelObjTypes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public SELF_TYPE setModelObjectTypesToFilterBy(final Collection<Class<? extends ModelObject>> modelObjTypes) {
		this.setModelObjectTypesToBeFiltered(modelObjTypes);
		return (SELF_TYPE)this;
	}
	/**
	 * Sets the model object types to filter by
	 * @param modelObjTypes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public SELF_TYPE setModelObjectTypesToFilterBy(final Class<? extends ModelObject>... modelObjTypes) {
		Collection<Class<? extends ModelObject>> colTypes = CollectionUtils.of(modelObjTypes)
																		   .asSet();
		this.setModelObjectTypesToBeFiltered(colTypes);
		return (SELF_TYPE)this;
	}
	@Override
	public void setModelObjectTypesToBeFiltered(final Collection<Class<? extends ModelObject>> modelObjectTypes) {
		Preconditions.checkArgument(CollectionUtils.hasData(modelObjectTypes),"The model object types to be filtered MUST not be null or empty");
		
		_modelObjectTypes = modelObjectTypes;
		
		// All the type's facets are stored at the TYPE_FACETS_FIELD_ID as a multi-valued field
		Set<Long> modelObjectTypeCodes = FluentIterable.from(modelObjectTypes)
													   .transform(new Function<Class<? extends ModelObject>,Long>() {
																		@Override 
																		public Long apply(final Class<? extends ModelObject> modelObjType) {
																			return ModelObjectTypeMetaDataBuilder.createFor(modelObjType)
																												 .getTypeCode();
																		}
														 
													 			 })
													   .toSet();
		
		IndexableFieldID typeFacetsFieldId = IndexableFieldID.fromMetaDataId(ModelObjectTypeMetaData.TYPE_FACETS_FIELD_ID);
		
		QueryClause clause = _findQueryClause(typeFacetsFieldId);
		if (clause != null) {
			ContainedInQueryClause<Long> typeContainedIn = clause.cast();
			typeContainedIn.setSpectrumFrom(modelObjectTypeCodes);
		} else {
			ContainedInQueryClause<Long> typeContainedIn = ContainedInQueryClause.<Long>forField(typeFacetsFieldId)
									   					   						 .within(modelObjectTypeCodes.toArray(new Long[modelObjectTypeCodes.size()]));
			_addClause(typeContainedIn,QueryClauseOccur.MUST);
		}
	}
	/**
	 * Returns the filtered model object types
	 * @return
	 */
	Collection<Class<? extends ModelObject>> _getFilteredModelObjectTypes() {
		return _modelObjectTypes;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  OID
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public <O extends OID> O getOid() {
		// obviously, only a single model object type can be set to be filtered if also an oid is set
		ModelObjectTypeMetaData modelObjectMetaData = _singleModelObjectMetadata();
		
		// Get the equals query clause if it exists
		QueryClause clause = _findQueryClause(modelObjectMetaData.getOIDFieldMetaData().getIndexableFieldId());
		EqualsQueryClause<OID> oidClause = clause != null ? (EqualsQueryClause<OID>)clause : null;
		return oidClause != null ? (O)oidClause.getEqValue() : null;
	}
	@Override
	public <O extends OID> void setOid(final O oid) {
		if (oid == null) return;
		
		// obviously, only a single model object type can be set to be filtered if also an oid is set
		ModelObjectTypeMetaData modelObjectMetaData = _singleModelObjectMetadata();
		
		// If there exists an equals query clause, modify it; if not set it
		IndexableFieldID oidFieldId = modelObjectMetaData.getOIDFieldMetaData().getIndexableFieldId();
		QueryClause clause = _findQueryClause(oidFieldId);
		if (clause != null) {
			// existing clause
			if (clause instanceof EqualsQueryClause) {
				EqualsQueryClause<O> oidClause = clause.cast();
				oidClause.setEqValue(oid);
			} else {
				log.warn(Strings.customized("The oid clause is NOT of the expected type {} or is null",EqualsQueryClause.class));
			}
		} else {
			// Non existing clause
			EqualsQueryClause<O> oidClause = EqualsQueryClause.forField(oidFieldId)
															  .of(oid);
			_addClause(oidClause,QueryClauseOccur.MUST);
		}
	}
	/**
	 * Fluent-API to set the oid 
	 * @param oid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <O extends OID> SELF_TYPE withOid(final O oid) {
		this.setOid(oid);
		return (SELF_TYPE)this;
	}
	/**
	 * If the filter was set for a single model object, it returns this model object's metadata
	 * @return
	 */
	private ModelObjectTypeMetaData _singleModelObjectMetadata() {
		Preconditions.checkState(CollectionUtils.hasData(_modelObjectTypes) && _modelObjectTypes.size() == 1,
								 "The filter is supposed to be set for a SINGLE model object type");
		Class<? extends ModelObject> singleModelObjType = CollectionUtils.pickOneAndOnlyElement(_modelObjectTypes);
		return ModelObjectTypeMetaDataBuilder.createFor(singleModelObjType);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  TEXT
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean hasTextFilter() {
		Preconditions.checkState(CollectionUtils.hasData(_modelObjectTypes),
								 "No model object type was set to the filter so no text clauses could be set");
		
		FieldMetaData fullTextField = _textFieldMetaData();
		
		return _checkQueryClause(fullTextField.getIndexableFieldId());
	}
	@Override
	public String getText() {
		Preconditions.checkState(CollectionUtils.hasData(_modelObjectTypes),
								 "No model object type was set to the filter so no text clauses could be set");
		
		FieldMetaData fullTextField = _textFieldMetaData();
		QueryClause clause = _findQueryClause(fullTextField.getIndexableFieldId());
		if (clause == null) throw new IllegalStateException(Throwables.message("NO query clause for field {} at query={}",fullTextField.getIndexableFieldId(),this.toCriteriaString()));
		
		ContainsTextQueryClause textClause = clause.cast();
		return textClause != null ? textClause.getText() : null;
	}
	@Override
	public Language getTextLanguage() {
		Preconditions.checkState(CollectionUtils.hasData(_modelObjectTypes),
								 "No model object type was set to the filter so no text clauses could be set");
		
		FieldMetaData fullTextField = _textFieldMetaData();
		QueryClause clause = _findQueryClause(fullTextField.getIndexableFieldId());
		if (clause == null) throw new IllegalStateException(Throwables.message("NO query clause for field {} at query={}",fullTextField.getIndexableFieldId(),this.toCriteriaString()));
		
		ContainsTextQueryClause textClause = clause.cast();
		return textClause != null ? textClause.getLang() : null;
	}
	@Override
	public void setText(final String text) {
		this.setText(text,
					 null);		// language independent
	}
	@Override
	public void setText(final String text,
						final Language lang) {
		if (Strings.isNullOrEmpty(text)) return;
		
		Preconditions.checkState(CollectionUtils.hasData(_modelObjectTypes),
								 "No model object type was set at the filter so a text clause cannot be set");
		
		FieldMetaData fullTextField = _textFieldMetaData();
		if (fullTextField == null) throw new IllegalStateException(Throwables.message("Searched model object types ({}) DO NOT have a fullText metaData defined",_modelObjectTypes));
		QueryClause clause = _findQueryClause(fullTextField.getIndexableFieldId());
		if (clause != null) {
			// existing clause
			if (clause instanceof ContainsTextQueryClause) {
				ContainsTextQueryClause textClause = clause.cast();
				textClause.setText(text);
			} else {
				log.warn(Strings.customized("The text clause is NOT of the expected type {} or is null",ContainsTextQueryClause.class));
			}
		} else {
			// Non existing clause
			ContainsTextQueryClause textClause = ContainsTextQueryClause.forField(fullTextField.getIndexableFieldId())
															  			.fullText(text)
															  			.in(lang);
			_addClause(textClause,QueryClauseOccur.MUST);
		}
	}
	/**
	 * Fluent-API to set the full text search 
	 * @param oid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public SearchFilterTextLangStep<SELF_TYPE> withText(final String text) {
		return new SearchFilterTextLangStep<SELF_TYPE>((SELF_TYPE)this,
													   text);
	}	
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class SearchFilterTextLangStep<FILTER_TYPE extends SearchFilterForModelObjectBase<FILTER_TYPE>> {
		private final FILTER_TYPE _filter;
		private final String _text;
		
		public FILTER_TYPE in(final Language lang) {
			_filter.setText(_text,lang);
			return _filter;
		}
		public FILTER_TYPE languageIndependent() {
			_filter.setText(_text);
			return _filter;
		}
	}
	/**
	 * If multiple model object types are set, ensure that all them implements HasFullTextSummaryFacet
	 * and that the metadata used to store the text is the same
	 * @param modelObjectTypes
	 * @return
	 */
	private FieldMetaData _textFieldMetaData() {
		FieldMetaData textField = _findFieldMetaDataOrThrow(HasFieldsMetaDataForHasFullTextSummaryModelObject.FULL_TEXT_SUMMARY_FIELD_ID);
		return textField;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CREATOR & EDITOR
/////////////////////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
	public SELF_TYPE createdBy(final UserCode creator) {
		Preconditions.checkArgument(creator != null,"The creator cannot be null");
		
		IndexableFieldID fieldId = IndexableFieldID.fromMetaDataId(ModelObjectTypeMetaData.CREATOR_FIELD_ID);
		
		QueryClause clause = _findQueryClause(fieldId);
		if (clause != null) {
			EqualsQueryClause<UserCode> eqClause = clause.cast();
			eqClause.setEqValue(creator);
		} else {
			EqualsQueryClause<UserCode> eqClause = EqualsQueryClause.forField(fieldId)	
																	.of(creator);
			_addClause(eqClause,QueryClauseOccur.MUST);
		}
		return (SELF_TYPE)this;
    }
	public UserCode getCreator() {
		return _queryClauseValueOrNull(IndexableFieldID.fromMetaDataId(ModelObjectTypeMetaData.CREATOR_FIELD_ID));
	}
    @SuppressWarnings("unchecked")
	public SELF_TYPE lastEditedBy(final UserCode lastEditor) {
		Preconditions.checkArgument(lastEditor != null,"The creator cannot be null");
		
		IndexableFieldID fieldId = IndexableFieldID.fromMetaDataId(ModelObjectTypeMetaData.LAST_UPDATOR_FIELD_ID);
		
		QueryClause clause = _findQueryClause(fieldId);
		if (clause != null) {
			EqualsQueryClause<UserCode> eqClause = clause.cast();
			eqClause.setEqValue(lastEditor);
		} else {
			EqualsQueryClause<UserCode> eqClause = EqualsQueryClause.forField(fieldId)	
																	.of(lastEditor);
			_addClause(eqClause,QueryClauseOccur.MUST);
		}
		return (SELF_TYPE)this;
    }
	public UserCode getLastEditor() {
		return _queryClauseValueOrNull(IndexableFieldID.fromMetaDataId(ModelObjectTypeMetaData.LAST_UPDATOR_FIELD_ID));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CREATE / LAST UPDATE DATE
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	public SELF_TYPE createdInRange(final Range<Date> range) {
		Preconditions.checkArgument(range != null,"The time range cannot be null");
		
		IndexableFieldID fieldId = IndexableFieldID.fromMetaDataId(ModelObjectTypeMetaData.CREATE_DATE_FIELD_ID);
		
		QueryClause clause = _findQueryClause(fieldId);
		if (clause != null) {
			RangeQueryClause<Date> rangeClause = clause.cast();
			rangeClause.setRange(range);
		} else {
			RangeQueryClause<Date> rangeClause = RangeQueryClause.forField(fieldId)
																 .of(range);
			_addClause(rangeClause,QueryClauseOccur.MUST);
		}
		return (SELF_TYPE)this;
	}
	public Range<Date> getCreatedRange() {
		return _queryClauseValueOrNull(IndexableFieldID.fromMetaDataId(ModelObjectTypeMetaData.CREATE_DATE_FIELD_ID));
	}
	@SuppressWarnings("unchecked")
	public SELF_TYPE lastUpdatedInRange(final Range<Date> range) {
		Preconditions.checkArgument(range != null,"The time range cannot be null");
		
		IndexableFieldID fieldId = IndexableFieldID.fromMetaDataId(ModelObjectTypeMetaData.LAST_UPDATE_DATE_FIELD_ID);
		
		QueryClause clause = _findQueryClause(fieldId);
		if (clause != null) {
			RangeQueryClause<Date> rangeClause = clause.cast();
			rangeClause.setRange(range);
		} else {
			RangeQueryClause<Date> rangeClause = RangeQueryClause.forField(fieldId)
																 .of(range);
			_addClause(rangeClause,QueryClauseOccur.MUST);
		}
		return (SELF_TYPE)this;
	}
	public Range<Date> getLastUpdatedRange() {
		return _queryClauseValueOrNull(IndexableFieldID.fromMetaDataId(ModelObjectTypeMetaData.LAST_UPDATE_DATE_FIELD_ID));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METADATA
/////////////////////////////////////////////////////////////////////////////////////////
    public SELF_TYPE mustMeetThisMetaDataCondition(final QueryClause metaDataCondition) {
    	return _addOrUpdateClause(metaDataCondition,QueryClauseOccur.MUST);
    }
    @SuppressWarnings("unchecked")
    public SELF_TYPE mustMeetThisMetaDataCondition(final Set<QueryClause> metaDataConditions) {
    	for (QueryClause condition : metaDataConditions) {
    		this.mustMeetThisMetaDataCondition(condition);
    	}
    	return (SELF_TYPE)this;
    }
    public SELF_TYPE mustNOTMeetThisMetaDataCondition(final QueryClause metaData) {
    	return _addOrUpdateClause(metaData,QueryClauseOccur.MUST_NOT);
    }
    @SuppressWarnings("unchecked")
    public SELF_TYPE mustNOTMeetThisMetaDataCondition(final Set<QueryClause> metaDataConditions) {
    	for (QueryClause condition : metaDataConditions) {
    		this.mustNOTMeetThisMetaDataCondition(condition);
    	}
    	return (SELF_TYPE)this;
    }
    public SELF_TYPE canMeetThisMetaDataCondition(final QueryClause metaDataCondition) {
    	return _addOrUpdateClause(metaDataCondition,QueryClauseOccur.SHOULD);
    }
    @SuppressWarnings("unchecked")
    public SELF_TYPE canMeetThisMetaDataCondition(final Set<QueryClause> metaDataConditions) {
    	Preconditions.checkArgument(metaDataConditions != null,"The metadata condition cannot be null");
    	for (QueryClause condition : metaDataConditions) {
    		this.canMeetThisMetaDataCondition(condition);
    	}
    	return (SELF_TYPE)this;
    }
	
	
	
/////////////////////////////////////////////////////////////////////////////////////////
//  UTILITY METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	protected boolean _checkQueryClause(final IndexableFieldID fieldId) {
		QueryClause clause = _booleanQuery != null ? _booleanQuery.findQueryClause(fieldId) 
									 			   : null;
		return clause != null;
	}
	protected QueryClause _findQueryClause(final IndexableFieldID fieldId) {
		QueryClause outClause = _booleanQuery != null ? _booleanQuery.findQueryClause(fieldId) 
									 				  : null;
		return outClause;
	}
	protected QueryClause _findQueryClause(final IndexableFieldID fieldId,final QueryClauseOccur occur) {
		QueryClause outClause = _booleanQuery != null ? _booleanQuery.findQueryClause(fieldId,occur) 
									 				  : null;
		return outClause;
	}
	protected QueryClause _findQueryClauseOfType(final IndexableFieldID fieldId,
												 final Class<? extends QueryClause> clauseType) {
		QueryClause outClause = _booleanQuery != null ? _booleanQuery.findQueryClauseOfType(fieldId,clauseType)
													  : null;
		return outClause;
	}
	protected QueryClause _findQueryClauseOfType(final IndexableFieldID fieldId,
												 final Class<? extends QueryClause> clauseType,
												 final QueryClauseOccur occur) {
		QueryClause outClause = _booleanQuery != null ? _booleanQuery.findQueryClauseOfType(fieldId,clauseType,occur)
									 				  : null;
		return outClause;
	}
	@SuppressWarnings("unchecked")
	protected <T> T _queryClauseValueOrNull(final IndexableFieldID fieldId) {
		QueryClause clause = _findQueryClause(fieldId);
		return clause != null ? (T)clause.getValue()
							  : null;
	}
	@SuppressWarnings("unchecked")
	protected <T> T _queryClauseValueOrNull(final IndexableFieldID fieldId,final QueryClauseOccur occur) {
		QueryClause clause = _findQueryClause(fieldId,occur);
		return clause != null ? (T)clause.getValue()
							  : null;
	}
	@SuppressWarnings("unchecked")
	protected <T> T _queryClauseValueOrNull(final IndexableFieldID fieldId,
											final Class<? extends QueryClause> clauseType) {
		QueryClause clause = _findQueryClauseOfType(fieldId,clauseType);
		return clause != null ? (T)clause.getValue()
						  : null;
	}
	@SuppressWarnings("unchecked")
	protected <T> T _queryClauseValueOrNull(final IndexableFieldID fieldId,
											final Class<? extends QueryClause> clauseType,
											final QueryClauseOccur occur) {
		QueryClause clause = _findQueryClauseOfType(fieldId,clauseType,occur);
		return clause != null ? (T)clause.getValue()
						  : null;
	}
	protected void _addClause(final QueryClause clause,
							  final QueryClauseOccur occur) {
		if (clause == null) return;
		QueryClauseOccur theOccur = occur != null ? occur : QueryClauseOccur.MUST;
		
		if (_booleanQuery == null) _booleanQuery = new BooleanQueryClause(new HashSet<QualifiedQueryClause<? extends QueryClause>>());
		_booleanQuery.add(clause,theOccur);
	}
	protected boolean _removeAllClausesFor(final IndexableFieldID fieldId) {
		if (_booleanQuery == null) return false;
		return _booleanQuery.removeAllFor(fieldId);
	}
	@SuppressWarnings("unchecked")
    protected SELF_TYPE _addOrUpdateClause(final QueryClause clause,
    									   final QueryClauseOccur clauseOccur) {
		Preconditions.checkArgument(clause != null,"The clause condition cannot be null");
		QueryClause existing = _findQueryClause(clause.getFieldId());
		if (existing != null) {
			_removeAllClausesFor(clause.getFieldId());
			_addClause(clause,
					   clauseOccur);
		} else {
			_addClause(clause,
					   clauseOccur);
		}
    	return (SELF_TYPE)this;
    }
	@SuppressWarnings("unchecked")
    protected <T> SELF_TYPE _addOrUpdateEqualsClause(final IndexableFieldID fieldId,
    												 final T eqValue,
    												 final QueryClauseOccur clauseOccur) {
		Preconditions.checkArgument(eqValue != null,"The value to be set in the equals clause cannot be null");
		
		QueryClause clause = _findQueryClause(fieldId);
		if (clause != null) {
			EqualsQueryClause<T> eq = clause.cast();
			eq.setEqValue(eqValue);
		} else {
			if (eqValue instanceof Range || eqValue instanceof com.google.common.collect.Range) throw new IllegalArgumentException("For range conditions use _addOrUpdateRangeClause!");
			EqualsQueryClause<T> eq = EqualsQueryClause.forField(fieldId)
													   .of(eqValue);
			_addClause(eq,clauseOccur);
		}
    	return (SELF_TYPE)this;
    }
	@SuppressWarnings("unchecked")
    protected <T> SELF_TYPE _addOrUpdateContainedInClause(final IndexableFieldID fieldId,
    													  final T[] range,
    													  final QueryClauseOccur clauseOccur) {
		Preconditions.checkArgument(CollectionUtils.hasData(range),"The values to be set in the contained in clause cannot be null");
		
		QueryClause clause = _findQueryClause(fieldId);
		if (clause != null) {
			ContainedInQueryClause<T> contained = clause.cast();
			contained.setSpectrum(range);
		} else {
			ContainedInQueryClause<T> contained = ContainedInQueryClause.<T>forField(fieldId)
															 			.within(range);
			_addClause(contained,clauseOccur);
		}
    	return (SELF_TYPE)this;
    }
	@SuppressWarnings("unchecked")
	protected <T extends Comparable<T>> SELF_TYPE _addOrUpdateRangeClause(final IndexableFieldID fieldId,
												    					  final Range<T> range,
												    					  final QueryClauseOccur clauseOccur) {
		QueryClause clause = _findQueryClause(fieldId);
		if (clause != null) {
			RangeQueryClause<T> requestRangeClause = clause.cast();
			requestRangeClause.setRange(range);
		} else {
			RangeQueryClause<T> requestRangeClause = RangeQueryClause.forField(fieldId)
																     .of(range);
			_addClause(requestRangeClause,clauseOccur);
		}
    	return (SELF_TYPE)this;
	}
	protected <T extends Comparable<T>> SELF_TYPE _addOrUpdateRangeClause(final IndexableFieldID fieldId,
												    					  final com.google.common.collect.Range<T> range,
												    					  final QueryClauseOccur clauseOccur) {
		return _addOrUpdateRangeClause(fieldId,
									   new Range<T>(range),
									   clauseOccur);
	}
	@SuppressWarnings("unchecked")
	protected SELF_TYPE _addOrUpdateContainsTextClause(final IndexableFieldID fieldId,
													   final String text,
													   final QueryClauseOccur clauseOccur) {
		Preconditions.checkArgument(Strings.isNOTNullOrEmpty(text),"The text to be set in the contains text clause cannot be null");
		QueryClause clause = _findQueryClause(fieldId);
		if (clause != null) {
			ContainsTextQueryClause contains = clause.cast();
			contains.setText(text);
		} else {
			ContainsTextQueryClause contains = ContainsTextQueryClause.forField(fieldId)
																	  .at(ContainedTextAt.FULL)
																	  .text(text)
																	  .languageIndependent();
			_addClause(contains,clauseOccur);
		}
		return (SELF_TYPE)this;
	}
	@SuppressWarnings("unchecked")
    protected SELF_TYPE _addOrUpdateHasDataClause(final IndexableFieldID fieldId,
    											  final QueryClauseOccur clauseOccur) {
		QueryClause clause = _findQueryClause(fieldId);
		if (clause != null) {
			// nothing... the clause already exists
		} else {
			HasDataQueryClause hasData = HasDataQueryClause.forField(fieldId);
			_addClause(hasData,clauseOccur);
		}
    	return (SELF_TYPE)this;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Finds a field's metadata when multiple object types are returned all with a common {@link IndexableFieldID}
	 * For example, if TypeA and TypeB are searched and both have a summary metadata, 
	 * the summary field must be mapped to the same {@link FieldMetaDataID} (ie SUMMARY) with the same type
	 * 
	 * This method ensures that all searched object types have the summary field and that the metadata type is the same
	 * @param modelObjMetaDataType 
	 * @return
	 */
	protected FieldMetaData _findFieldMetaDataOrNull(final FieldMetaDataID metaDataId) {
		return _findFieldMetaData(metaDataId,
							      false);	// return null 
	}
	/**
	 * Finds a field's metadata when multiple object types are returned all with a common {@link IndexableFieldID}
	 * For example, if TypeA and TypeB are searched and both have a summary metadata, 
	 * the summary field must be mapped to the same {@link FieldMetaDataID} (ie SUMMARY) with the same type
	 * 
	 * This method ensures that all searched object types have the summary field and that the metadata type is the same
	 * @param modelObjMetaDataType 
	 * @return
	 */
	protected FieldMetaData _findFieldMetaDataOrThrow(final FieldMetaDataID metaDataId) {
		return _findFieldMetaData(metaDataId,
								  true);	// throw an exception if metadata not found
	}
	private FieldMetaData _findFieldMetaData(final FieldMetaDataID metaDataId,
											 final boolean strict) {
		// If multiple model object types are set, ensure that all them have the provided metadata id
		FieldMetaData outFieldMetaData = null;
		for (Class<? extends ModelObject> modelObjectType : _modelObjectTypes) {
			ModelObjectTypeMetaData modelObjectMetaData = ModelObjectTypeMetaDataBuilder.createFor(modelObjectType);
			
			// check that this model object contains the field
			FieldMetaData thisModelObjFieldMetaData = modelObjectMetaData.getFieldMetaDataFor(metaDataId);
			if (outFieldMetaData != null					// other model object has the field to be found
			 && thisModelObjFieldMetaData == null) {	// ... but not this one
				throw new IllegalStateException(Throwables.message("The model object metadata for the type {} set at the search filter DOES NOT have the {} field sob the clause cannot be set to the filter because NOT all filtered types ({}) have this metadata",
											  		  		       modelObjectType,metaDataId,CollectionUtils.of(_modelObjectTypes).toStringCommaSeparated()));
			}
			
			// check the field data type
			if (thisModelObjFieldMetaData != null) {
				if (outFieldMetaData == null) {
					outFieldMetaData = thisModelObjFieldMetaData;
				} else if (outFieldMetaData.getDataType() != thisModelObjFieldMetaData.getDataType()) {	// check the field data type is the same 
					throw new IllegalStateException(Throwables.message("Multiple model object types were set at the filter ({}) BUT not all of them uses the same type for the field with id={}",
																	   CollectionUtils.of(_modelObjectTypes).toStringCommaSeparated(),outFieldMetaData.getIndexableFieldId()));
				}
			}
		}
		if (strict && outFieldMetaData == null) throw new IllegalStateException(Throwables.message("Any of the search filter model objects ({}) has a metadata with id={}",
																								   CollectionUtils.of(_modelObjectTypes).toStringCommaSeparated(),metaDataId));
		return outFieldMetaData;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
    public SearchFilterAsCriteriaString toCriteriaString() {
		// The filter as as a criteria string has the following structure:
		//		uiLanguage:es#filterType:MySearchFilterType#modelObjTypes:{modelObjTypeCode,modelObjTypeCode,...}#{boolean query as criteriaString}
		//			[1]      #                  [2]        #               [3]                                   #             [4]
		
		// [1] - Compose the uiLanguage part
		String uiLangPart = _UILanguage != null ? Strings.customized("uiLanguage:{}",_UILanguage) 
											    : null;
		// [2] - Compose the filter's type part
		String filterTypePart = Strings.customized("filterType:{}",this.getClass().getCanonicalName());
		
		// [3] - Compose the model object filter's type part 
		// 2.b: model objs type codes
		Collection<Long> modelObjTypesCodes = FluentIterable.from(_modelObjectTypes)
														    .transform(new Function<Class<? extends ModelObject>,Long>() {
																				@Override
																				public Long apply(final Class<? extends ModelObject> modelObjType) {
																					return ModelObjectTypeMetaDataBuilder.createFor(modelObjType)
																													     .getTypeCode();
																				}
														   			  })
														    .toSet();
		String modelObjTypesCriteriaStrPart = Strings.customized("modelObjTypes:{}",CollectionUtils.of(modelObjTypesCodes)
																				 			       .toStringCommaSeparated());
		
		// [3] - Compose the boolean query criteria string
		String boolClausesPart = this.getBooleanQuery().encodeAsString();
		
		// --- Join them all
		String outCriteriaStr = null;
		if (uiLangPart != null && boolClausesPart != null) {
			outCriteriaStr = Strings.of("{}#{}#{}#{}")
									.customizeWith(uiLangPart,filterTypePart,
												   modelObjTypesCriteriaStrPart,
												   boolClausesPart)
									.asString();
		} else if (uiLangPart != null && boolClausesPart == null) {
			outCriteriaStr = Strings.of("{}#{}#{}")
									.customizeWith(uiLangPart,filterTypePart,
												   modelObjTypesCriteriaStrPart)
									.asString();
		} else if (uiLangPart == null && boolClausesPart != null) {
			outCriteriaStr = Strings.of("{}#{}#{}")
									.customizeWith(filterTypePart,
												   modelObjTypesCriteriaStrPart,
												   boolClausesPart)
									.asString();
		} else if (uiLangPart == null && boolClausesPart == null) {
			outCriteriaStr = modelObjTypesCriteriaStrPart;
		}
		return SearchFilterAsCriteriaString.of(outCriteriaStr);
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static final Pattern CRITERIA_PATTERN = Pattern.compile("(?:uiLanguage:(" + Language.pattern() + ")#)?filterType:([^#]+)#modelObjTypes:([^#]+)?#(.*)");
	
	public static <F extends SearchFilterForModelObject> F fromCriteriaString(final SearchFilterAsCriteriaString criteriaStr) {
    	F outFilter = null;
		if (!criteriaStr.hasData()) {
			log.error("NO filter info available");
		} else {			
			String uiLangStr = null;
			String filterTypeStr = null;
			String modelObjTypesStr = null;
			String clausesStr = null;
			Matcher m = CRITERIA_PATTERN.matcher(criteriaStr.asString());
			if (m.find()) {
				uiLangStr = m.group(1);
				filterTypeStr = m.group(2);
				modelObjTypesStr = m.group(3);
				clausesStr = m.group(4);
				
				// [1] - Create a filter instance from the info available at the criteria string				
				// 1.2 - Get the filtered model obj types
				Collection<Class<? extends ModelObject>> modelObjTypes = Sets.newHashSet();
				String[] modelObjTypeCodes = modelObjTypesStr.split(",");
				for (String typeCodeStr : modelObjTypeCodes) {
					long typeCode = Long.parseLong(typeCodeStr);
					ModelObjectTypeMetaData modelObjMetaData = ModelObjectTypeMetaDataBuilder.createFor(typeCode);
					modelObjTypes.add(modelObjMetaData.getType());
				}
				// 1.2 - Create the filter
				Class<? extends SearchFilterForModelObject> filterType = ReflectionUtils.typeFromClassName(filterTypeStr);
				// outFilter = ReflectionUtils.<F>createInstanceOf(filterType,
				//												   new Class<?>[] {Collection.class},new Object[] {modelObjTypes});
				outFilter = ReflectionUtils.<F>createInstanceOf(filterType);
				if (CollectionUtils.hasData(modelObjTypes)) outFilter.setModelObjectTypesToBeFiltered(modelObjTypes);
				
				// [2] - Set filter data
				if (Strings.isNOTNullOrEmpty(uiLangStr)) outFilter.setUILanguage(Language.fromName(uiLangStr));							
				
				if (Strings.isNOTNullOrEmpty(clausesStr)) {
					Class<? extends ModelObject> anyModelObjType = CollectionUtils.pickOneElement(modelObjTypes);	// if multiple model obj types are set at the filter
																													// all MUST share the filtered fields, so any of the 
																													// types metadata can be used to decode the field clauses
					ModelObjectTypeMetaData modelObjMetaData = ModelObjectTypeMetaDataBuilder.createFor(anyModelObjType);
					BooleanQueryClause boolQry = BooleanQueryClause.fromString(clausesStr,
																			   modelObjMetaData);
					((SearchFilterForModelObjectBase<?>)outFilter).setBooleanQuery(boolQry);
				} 
			} else {
				log.error("The criteria string {} does NOT mathch the required pattern {}",criteriaStr,CRITERIA_PATTERN.toString());
			}
		}
		return outFilter;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
}
