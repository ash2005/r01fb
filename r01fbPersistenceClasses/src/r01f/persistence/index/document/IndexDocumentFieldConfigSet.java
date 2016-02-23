	package r01f.persistence.index.document;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import r01f.guids.OID;
import r01f.model.IndexableModelObject;
import r01f.model.metadata.FieldMetaData;
import r01f.model.metadata.FieldMetaDataID;
import r01f.model.metadata.ModelObjectTypeMetaData;
import r01f.model.metadata.ModelObjectTypeMetaDataBuilder;
import r01f.persistence.index.document.IndexDocumentFieldConfigFactories.IndexDocumentFieldConfigFactory;
import r01f.persistence.index.document.IndexDocumentFieldConfigFactories.IndexDocumentFieldConfigFactoryMatchingFieldNameByEquality;
import r01f.persistence.index.document.IndexDocumentFieldConfigFactories.IndexDocumentFieldConfigFactoryMatchingFieldNameByPattern;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * Holds the field's config for document's indexed fields 
 * @see IndexDocumentFieldConfigSet
 */
@Slf4j
public class IndexDocumentFieldConfigSet<M extends IndexableModelObject> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Customizer that gives chance to an app to customize the {@link IndexDocumentFieldConfig} 
	 * generated from a {@link FieldMetaData} 
	 */
	private final IndexDocumentFieldConfigCustomizer _indexDocumentFieldConfigCustomizer;
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The type of the model object to be indexed
	 */
	protected final Class<M> _indexableModelObjType;
	/**
	 * MetaData about the model object
	 */
	protected final transient ModelObjectTypeMetaData _modelObjectMetaData;
	/**
	 * Cached fields config
	 */
	private Map<IndexDocumentFieldID,IndexDocumentFieldConfig<?>> _fields;
	/**
	 * A list of factories that creates config for index document's fields
	 * This list is traversed when there's the need to create a field config
	 * for a field that was not already created (it's not in the cache)
	 */
	private List<IndexDocumentFieldConfigFactory> _fieldFactories;	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public IndexDocumentFieldConfigSet(final Class<M> indexableModelObjType) {
		this(indexableModelObjType,
			 null);		// index document's fields use default config (are NOT customized)
	}
	public IndexDocumentFieldConfigSet(final Class<M> indexableModelObjType,
									   final IndexDocumentFieldConfigCustomizer indexDocFieldCustomizer) {
		// ** do not move **
		_indexableModelObjType = indexableModelObjType;
		_modelObjectMetaData = ModelObjectTypeMetaDataBuilder.createFor(_indexableModelObjType);
		_indexDocumentFieldConfigCustomizer = indexDocFieldCustomizer;
		// **
		
		// Register index document factories for every model object field
		// (the ones defined at model object's metadata)
		if (_modelObjectMetaData != null) {
			Map<FieldMetaDataID,? extends FieldMetaData> fieldsMetaData = _modelObjectMetaData.getFieldsMetaData();
			if (CollectionUtils.hasData(fieldsMetaData)) {
				for (Map.Entry<FieldMetaDataID,? extends FieldMetaData> me : fieldsMetaData.entrySet()) {
					FieldMetaDataID metaDataId = me.getKey();
					FieldMetaData metaData = me.getValue();
					
					log.debug("Registering index document's fieldFactory for metaData {} as {}",metaDataId,metaData.getIndexableFieldId());
					this.registerFactoryFor(metaData);
				}
			}
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ACCESSORS
/////////////////////////////////////////////////////////////////////////////////////////
	public Class<M> getSubjectModelObjectType() {
		return _indexableModelObjType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FACTORY REGISTER
/////////////////////////////////////////////////////////////////////////////////////////
	public IndexDocumentFieldConfigSet<M> registerFactories(final IndexDocumentFieldConfigFactory... factories) {
		if (CollectionUtils.hasData(factories)) {
			if (_fieldFactories == null) _fieldFactories = Lists.newArrayList();
			for (IndexDocumentFieldConfigFactory factory : factories) {
				log.debug("Registering index document's fieldFactory: {}",factory.getId());
				_fieldFactories.add(factory);
			}
		}
		return this;
	}
	public IndexDocumentFieldConfigSet<M> registerFactoriesFor(final FieldMetaData... fieldsMetaData) {
		if (CollectionUtils.hasData(fieldsMetaData)) {
			for (final FieldMetaData fieldMetaData : fieldsMetaData) {
				this.registerFactoryFor(fieldMetaData);
			}
		}
		return this;
	}
	public IndexDocumentFieldConfigSet<M> registerFactoriesFor(final Collection<FieldMetaData> fieldsMetaData) {	
		if (CollectionUtils.hasData(fieldsMetaData)) {
			for (final FieldMetaData fieldMetaData : fieldsMetaData) {
				this.registerFactoryFor(fieldMetaData);
			}
		}
		return this;
	}
	public IndexDocumentFieldConfigSet<M> registerFactoryFor(final FieldMetaData fieldMetaData) {
		if (_fieldFactories == null) _fieldFactories = Lists.newArrayList();
		
		log.debug("Registering index document's fieldFactory: {}",fieldMetaData.getIndexableFieldId());
		
		// security check
		if (fieldMetaData.getSearchEngineIndexingConfig() == null) {
			log.error("The metadata config for {} does NOT have search engine indexing config! This metadata will NOT be indexed",fieldMetaData.getFieldId());
			return this;
		}
		
		// NOT multi-dimensional fields
		// they're stored as indexableFieldId = value
		if (!fieldMetaData.hasMultipleDimensions()) {
			_fieldFactories.add(new IndexDocumentFieldConfigFactoryMatchingFieldNameByEquality(fieldMetaData.getIndexableFieldId()) {
											@Override
											public IndexDocumentFieldConfig<?> createFieldConfigFor(final IndexDocumentFieldID fieldId) {
												IndexDocumentFieldConfig<?> outIdxDocFieldCfg = IndexDocumentFieldConfig.createStandardFor(fieldId,
																								  										   fieldMetaData);
												if (_indexDocumentFieldConfigCustomizer != null) _indexDocumentFieldConfigCustomizer.customize(outIdxDocFieldCfg);
												return outIdxDocFieldCfg;
											}
					   			});
		}
		// multi-dimensional fields (ie language texts)
		// these fields are stored as: indexableFieldId.[dimension]  = value
		// for example, if the language is the dimension the indexed fields will be named as:
		//			myField.es = value_es
		//			myField.en = value_en
		//			...
		else {
			_fieldFactories.add(new IndexDocumentFieldConfigFactoryMatchingFieldNameByPattern(IndexDocumentFieldID.dynamicDimensionDependantFieldNamePattern(fieldMetaData.getIndexableFieldId())) {
										@Override
										public IndexDocumentFieldConfig<?> createFieldConfigFor(final IndexDocumentFieldID fieldId) {
											IndexDocumentFieldConfig<?> outIdxDocFieldCfg = IndexDocumentFieldConfig.createStandardFor(fieldId,
																							  										   fieldMetaData);
											if (_indexDocumentFieldConfigCustomizer != null) _indexDocumentFieldConfigCustomizer.customize(outIdxDocFieldCfg);
											return outIdxDocFieldCfg;
										}
						   	   });
		}
				return this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CACHE ADD 
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds some index document's fields config to the set
	 * @param fields 
	 * @return
	 */
	public IndexDocumentFieldConfigSet<M> add(final IndexDocumentFieldConfig<?>... fields) {
		Preconditions.checkArgument(CollectionUtils.hasData(fields),
									"Provided index document's fields config cannot be empty");
		if (_fields == null) _fields = Maps.newHashMap();
		for (IndexDocumentFieldConfig<?> field : fields) {
			_fields.put(field.getId(),
						field);
		}
		return this;
	}
	/**
	 * Adds some index document's fields config to the set
	 * @param fields
	 * @return
	 */
	public IndexDocumentFieldConfigSet<M> add(final Collection<IndexDocumentFieldConfig<?>> fields) {
		Preconditions.checkArgument(CollectionUtils.hasData(fields),
									"Provided index document's fields config cannot be empty");
		if (_fields == null) _fields = Maps.newHashMap();
		for (IndexDocumentFieldConfig<?> field : fields) {
			_fields.put(field.getId(),
						field);
		}
		return this;
	}
	/**
	 * Adds some index document's fields config to the set
	 * @param other
	 * @return
	 */
	public IndexDocumentFieldConfigSet<M> addAll(final IndexDocumentFieldConfigSet<M> other) {
		if (CollectionUtils.hasData(other.getConfigForFields())) {
			this.add(other.getConfigForFields());
		}
		return this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  RETRIEVE
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean hasData() {
		return CollectionUtils.hasData(_fields);
	}
	/**
	 * @return the number of index document's Field config at the Set
	 */
	public int size() {
		return _fields != null ? _fields.size()
							   : 0;
	}
	/**
	 * @return a map of index document's fields config indexed by field name
	 */
	public Map<IndexDocumentFieldID,IndexDocumentFieldConfig<?>> getConfigForFieldsMap() {
		return _fields;
	}
	/**
	 * @return all the index document's fields config
	 */
	public Collection<IndexDocumentFieldConfig<?>> getConfigForFields() {
		return _fields != null ? _fields.values()
							   : null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the index document's field config for a field
	 * @param fieldId
	 * @return
	 */
	public IndexDocumentFieldConfig<?> of(final IndexDocumentFieldID fieldId) {
		return this.getConfigFor(fieldId);
	}
	/**
	 * Returns the index document's field config for the field which id is provided
	 * @param id
	 * @return
	 */
	public IndexDocumentFieldConfig<?> getConfigFor(final IndexDocumentFieldID fieldId) {
		// check the cache
		IndexDocumentFieldConfig<?> outCfg = this.getConfigOrNullFor(fieldId);
		if (outCfg == null) {
			// a bit of debugging...
			StringBuilder dbgErr = new StringBuilder(_fieldFactories.size() * 50);
			dbgErr.append("NO suitable index document's field factory were found for field '")
				  .append(fieldId)
				  .append("' ");
			dbgErr.append("; tried: ");	
			for (Iterator<IndexDocumentFieldConfigFactory> it = _fieldFactories.iterator(); it.hasNext(); ) {
				IndexDocumentFieldConfigFactory factory = it.next();
				dbgErr.append(factory.getId());
				if (it.hasNext()) dbgErr.append(", ");
			}
			throw new IllegalStateException(dbgErr.toString());
		}
		return outCfg;
	}
	/**
	 * Returns the index document's field config for the field which id is provided
	 * @param id
	 * @return
	 */
	public IndexDocumentFieldConfig<?> getConfigOrNullFor(final IndexDocumentFieldID fieldId) {
		// check the cache
		IndexDocumentFieldConfig<?> outCfg = _fields != null ? _fields.get(fieldId)
							   						  		 : null;
		// The field is NOT in the cache... try to find a suitable factory
		if (outCfg == null) {
			if (CollectionUtils.isNullOrEmpty(_fieldFactories)) throw new IllegalStateException("There's NO index document's field factory registered so the config for the required field '" + fieldId + "' could NOT be created!");
			for (IndexDocumentFieldConfigFactory factory : _fieldFactories) {
				if (factory.isUsableFor(fieldId)) {
					outCfg = factory.createFieldConfigFor(fieldId);
					this.add(outCfg);	// cache...
					break;
				} 
			}
		} 
		return outCfg;
	}
}
