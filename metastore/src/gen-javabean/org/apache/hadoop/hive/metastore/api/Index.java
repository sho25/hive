begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Autogenerated by Thrift  *  * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|meta_data
operator|.
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|*
import|;
end_import

begin_class
specifier|public
class|class
name|Index
implements|implements
name|TBase
implements|,
name|java
operator|.
name|io
operator|.
name|Serializable
implements|,
name|Cloneable
block|{
specifier|private
specifier|static
specifier|final
name|TStruct
name|STRUCT_DESC
init|=
operator|new
name|TStruct
argument_list|(
literal|"Index"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TField
name|INDEX_NAME_FIELD_DESC
init|=
operator|new
name|TField
argument_list|(
literal|"indexName"
argument_list|,
name|TType
operator|.
name|STRING
argument_list|,
operator|(
name|short
operator|)
literal|1
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TField
name|INDEX_TYPE_FIELD_DESC
init|=
operator|new
name|TField
argument_list|(
literal|"indexType"
argument_list|,
name|TType
operator|.
name|I32
argument_list|,
operator|(
name|short
operator|)
literal|2
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TField
name|TABLE_NAME_FIELD_DESC
init|=
operator|new
name|TField
argument_list|(
literal|"tableName"
argument_list|,
name|TType
operator|.
name|STRING
argument_list|,
operator|(
name|short
operator|)
literal|3
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TField
name|DB_NAME_FIELD_DESC
init|=
operator|new
name|TField
argument_list|(
literal|"dbName"
argument_list|,
name|TType
operator|.
name|STRING
argument_list|,
operator|(
name|short
operator|)
literal|4
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TField
name|COL_NAMES_FIELD_DESC
init|=
operator|new
name|TField
argument_list|(
literal|"colNames"
argument_list|,
name|TType
operator|.
name|LIST
argument_list|,
operator|(
name|short
operator|)
literal|5
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TField
name|PART_NAME_FIELD_DESC
init|=
operator|new
name|TField
argument_list|(
literal|"partName"
argument_list|,
name|TType
operator|.
name|STRING
argument_list|,
operator|(
name|short
operator|)
literal|6
argument_list|)
decl_stmt|;
specifier|private
name|String
name|indexName
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|INDEXNAME
init|=
literal|1
decl_stmt|;
specifier|private
name|int
name|indexType
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|INDEXTYPE
init|=
literal|2
decl_stmt|;
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|TABLENAME
init|=
literal|3
decl_stmt|;
specifier|private
name|String
name|dbName
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DBNAME
init|=
literal|4
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|colNames
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|COLNAMES
init|=
literal|5
decl_stmt|;
specifier|private
name|String
name|partName
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|PARTNAME
init|=
literal|6
decl_stmt|;
specifier|private
specifier|final
name|Isset
name|__isset
init|=
operator|new
name|Isset
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
class|class
name|Isset
implements|implements
name|java
operator|.
name|io
operator|.
name|Serializable
block|{
specifier|public
name|boolean
name|indexType
init|=
literal|false
decl_stmt|;
block|}
specifier|public
specifier|static
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|FieldMetaData
argument_list|>
name|metaDataMap
init|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|FieldMetaData
argument_list|>
argument_list|()
block|{
block|{
name|put
argument_list|(
name|INDEXNAME
argument_list|,
operator|new
name|FieldMetaData
argument_list|(
literal|"indexName"
argument_list|,
name|TFieldRequirementType
operator|.
name|DEFAULT
argument_list|,
operator|new
name|FieldValueMetaData
argument_list|(
name|TType
operator|.
name|STRING
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|INDEXTYPE
argument_list|,
operator|new
name|FieldMetaData
argument_list|(
literal|"indexType"
argument_list|,
name|TFieldRequirementType
operator|.
name|DEFAULT
argument_list|,
operator|new
name|FieldValueMetaData
argument_list|(
name|TType
operator|.
name|I32
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|TABLENAME
argument_list|,
operator|new
name|FieldMetaData
argument_list|(
literal|"tableName"
argument_list|,
name|TFieldRequirementType
operator|.
name|DEFAULT
argument_list|,
operator|new
name|FieldValueMetaData
argument_list|(
name|TType
operator|.
name|STRING
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|DBNAME
argument_list|,
operator|new
name|FieldMetaData
argument_list|(
literal|"dbName"
argument_list|,
name|TFieldRequirementType
operator|.
name|DEFAULT
argument_list|,
operator|new
name|FieldValueMetaData
argument_list|(
name|TType
operator|.
name|STRING
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|COLNAMES
argument_list|,
operator|new
name|FieldMetaData
argument_list|(
literal|"colNames"
argument_list|,
name|TFieldRequirementType
operator|.
name|DEFAULT
argument_list|,
operator|new
name|ListMetaData
argument_list|(
name|TType
operator|.
name|LIST
argument_list|,
operator|new
name|FieldValueMetaData
argument_list|(
name|TType
operator|.
name|STRING
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|PARTNAME
argument_list|,
operator|new
name|FieldMetaData
argument_list|(
literal|"partName"
argument_list|,
name|TFieldRequirementType
operator|.
name|DEFAULT
argument_list|,
operator|new
name|FieldValueMetaData
argument_list|(
name|TType
operator|.
name|STRING
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
decl_stmt|;
static|static
block|{
name|FieldMetaData
operator|.
name|addStructMetaDataMap
argument_list|(
name|Index
operator|.
name|class
argument_list|,
name|metaDataMap
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Index
parameter_list|()
block|{   }
specifier|public
name|Index
parameter_list|(
name|String
name|indexName
parameter_list|,
name|int
name|indexType
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|dbName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|colNames
parameter_list|,
name|String
name|partName
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|this
operator|.
name|indexName
operator|=
name|indexName
expr_stmt|;
name|this
operator|.
name|indexType
operator|=
name|indexType
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|indexType
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|colNames
operator|=
name|colNames
expr_stmt|;
name|this
operator|.
name|partName
operator|=
name|partName
expr_stmt|;
block|}
comment|/**    * Performs a deep copy on<i>other</i>.    */
specifier|public
name|Index
parameter_list|(
name|Index
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|.
name|isSetIndexName
argument_list|()
condition|)
block|{
name|this
operator|.
name|indexName
operator|=
name|other
operator|.
name|indexName
expr_stmt|;
block|}
name|__isset
operator|.
name|indexType
operator|=
name|other
operator|.
name|__isset
operator|.
name|indexType
expr_stmt|;
name|this
operator|.
name|indexType
operator|=
name|other
operator|.
name|indexType
expr_stmt|;
if|if
condition|(
name|other
operator|.
name|isSetTableName
argument_list|()
condition|)
block|{
name|this
operator|.
name|tableName
operator|=
name|other
operator|.
name|tableName
expr_stmt|;
block|}
if|if
condition|(
name|other
operator|.
name|isSetDbName
argument_list|()
condition|)
block|{
name|this
operator|.
name|dbName
operator|=
name|other
operator|.
name|dbName
expr_stmt|;
block|}
if|if
condition|(
name|other
operator|.
name|isSetColNames
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|__this__colNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|other_element
range|:
name|other
operator|.
name|colNames
control|)
block|{
name|__this__colNames
operator|.
name|add
argument_list|(
name|other_element
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|colNames
operator|=
name|__this__colNames
expr_stmt|;
block|}
if|if
condition|(
name|other
operator|.
name|isSetPartName
argument_list|()
condition|)
block|{
name|this
operator|.
name|partName
operator|=
name|other
operator|.
name|partName
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Index
name|clone
parameter_list|()
block|{
return|return
operator|new
name|Index
argument_list|(
name|this
argument_list|)
return|;
block|}
specifier|public
name|String
name|getIndexName
parameter_list|()
block|{
return|return
name|this
operator|.
name|indexName
return|;
block|}
specifier|public
name|void
name|setIndexName
parameter_list|(
name|String
name|indexName
parameter_list|)
block|{
name|this
operator|.
name|indexName
operator|=
name|indexName
expr_stmt|;
block|}
specifier|public
name|void
name|unsetIndexName
parameter_list|()
block|{
name|this
operator|.
name|indexName
operator|=
literal|null
expr_stmt|;
block|}
comment|// Returns true if field indexName is set (has been asigned a value) and false otherwise
specifier|public
name|boolean
name|isSetIndexName
parameter_list|()
block|{
return|return
name|this
operator|.
name|indexName
operator|!=
literal|null
return|;
block|}
specifier|public
name|int
name|getIndexType
parameter_list|()
block|{
return|return
name|this
operator|.
name|indexType
return|;
block|}
specifier|public
name|void
name|setIndexType
parameter_list|(
name|int
name|indexType
parameter_list|)
block|{
name|this
operator|.
name|indexType
operator|=
name|indexType
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|indexType
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|unsetIndexType
parameter_list|()
block|{
name|this
operator|.
name|__isset
operator|.
name|indexType
operator|=
literal|false
expr_stmt|;
block|}
comment|// Returns true if field indexType is set (has been asigned a value) and false otherwise
specifier|public
name|boolean
name|isSetIndexType
parameter_list|()
block|{
return|return
name|this
operator|.
name|__isset
operator|.
name|indexType
return|;
block|}
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|this
operator|.
name|tableName
return|;
block|}
specifier|public
name|void
name|setTableName
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
specifier|public
name|void
name|unsetTableName
parameter_list|()
block|{
name|this
operator|.
name|tableName
operator|=
literal|null
expr_stmt|;
block|}
comment|// Returns true if field tableName is set (has been asigned a value) and false otherwise
specifier|public
name|boolean
name|isSetTableName
parameter_list|()
block|{
return|return
name|this
operator|.
name|tableName
operator|!=
literal|null
return|;
block|}
specifier|public
name|String
name|getDbName
parameter_list|()
block|{
return|return
name|this
operator|.
name|dbName
return|;
block|}
specifier|public
name|void
name|setDbName
parameter_list|(
name|String
name|dbName
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
block|}
specifier|public
name|void
name|unsetDbName
parameter_list|()
block|{
name|this
operator|.
name|dbName
operator|=
literal|null
expr_stmt|;
block|}
comment|// Returns true if field dbName is set (has been asigned a value) and false otherwise
specifier|public
name|boolean
name|isSetDbName
parameter_list|()
block|{
return|return
name|this
operator|.
name|dbName
operator|!=
literal|null
return|;
block|}
specifier|public
name|int
name|getColNamesSize
parameter_list|()
block|{
return|return
operator|(
name|this
operator|.
name|colNames
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|this
operator|.
name|colNames
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|java
operator|.
name|util
operator|.
name|Iterator
argument_list|<
name|String
argument_list|>
name|getColNamesIterator
parameter_list|()
block|{
return|return
operator|(
name|this
operator|.
name|colNames
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|this
operator|.
name|colNames
operator|.
name|iterator
argument_list|()
return|;
block|}
specifier|public
name|void
name|addToColNames
parameter_list|(
name|String
name|elem
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|colNames
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|colNames
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|colNames
operator|.
name|add
argument_list|(
name|elem
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getColNames
parameter_list|()
block|{
return|return
name|this
operator|.
name|colNames
return|;
block|}
specifier|public
name|void
name|setColNames
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|colNames
parameter_list|)
block|{
name|this
operator|.
name|colNames
operator|=
name|colNames
expr_stmt|;
block|}
specifier|public
name|void
name|unsetColNames
parameter_list|()
block|{
name|this
operator|.
name|colNames
operator|=
literal|null
expr_stmt|;
block|}
comment|// Returns true if field colNames is set (has been asigned a value) and false otherwise
specifier|public
name|boolean
name|isSetColNames
parameter_list|()
block|{
return|return
name|this
operator|.
name|colNames
operator|!=
literal|null
return|;
block|}
specifier|public
name|String
name|getPartName
parameter_list|()
block|{
return|return
name|this
operator|.
name|partName
return|;
block|}
specifier|public
name|void
name|setPartName
parameter_list|(
name|String
name|partName
parameter_list|)
block|{
name|this
operator|.
name|partName
operator|=
name|partName
expr_stmt|;
block|}
specifier|public
name|void
name|unsetPartName
parameter_list|()
block|{
name|this
operator|.
name|partName
operator|=
literal|null
expr_stmt|;
block|}
comment|// Returns true if field partName is set (has been asigned a value) and false otherwise
specifier|public
name|boolean
name|isSetPartName
parameter_list|()
block|{
return|return
name|this
operator|.
name|partName
operator|!=
literal|null
return|;
block|}
specifier|public
name|void
name|setFieldValue
parameter_list|(
name|int
name|fieldID
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
switch|switch
condition|(
name|fieldID
condition|)
block|{
case|case
name|INDEXNAME
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetIndexName
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setIndexName
argument_list|(
operator|(
name|String
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|INDEXTYPE
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetIndexType
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setIndexType
argument_list|(
operator|(
name|Integer
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|TABLENAME
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetTableName
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setTableName
argument_list|(
operator|(
name|String
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|DBNAME
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetDbName
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setDbName
argument_list|(
operator|(
name|String
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|COLNAMES
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetColNames
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setColNames
argument_list|(
operator|(
name|List
argument_list|<
name|String
argument_list|>
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|PARTNAME
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetPartName
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setPartName
argument_list|(
operator|(
name|String
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Field "
operator|+
name|fieldID
operator|+
literal|" doesn't exist!"
argument_list|)
throw|;
block|}
block|}
specifier|public
name|Object
name|getFieldValue
parameter_list|(
name|int
name|fieldID
parameter_list|)
block|{
switch|switch
condition|(
name|fieldID
condition|)
block|{
case|case
name|INDEXNAME
case|:
return|return
name|getIndexName
argument_list|()
return|;
case|case
name|INDEXTYPE
case|:
return|return
operator|new
name|Integer
argument_list|(
name|getIndexType
argument_list|()
argument_list|)
return|;
case|case
name|TABLENAME
case|:
return|return
name|getTableName
argument_list|()
return|;
case|case
name|DBNAME
case|:
return|return
name|getDbName
argument_list|()
return|;
case|case
name|COLNAMES
case|:
return|return
name|getColNames
argument_list|()
return|;
case|case
name|PARTNAME
case|:
return|return
name|getPartName
argument_list|()
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Field "
operator|+
name|fieldID
operator|+
literal|" doesn't exist!"
argument_list|)
throw|;
block|}
block|}
comment|// Returns true if field corresponding to fieldID is set (has been asigned a value) and false otherwise
specifier|public
name|boolean
name|isSet
parameter_list|(
name|int
name|fieldID
parameter_list|)
block|{
switch|switch
condition|(
name|fieldID
condition|)
block|{
case|case
name|INDEXNAME
case|:
return|return
name|isSetIndexName
argument_list|()
return|;
case|case
name|INDEXTYPE
case|:
return|return
name|isSetIndexType
argument_list|()
return|;
case|case
name|TABLENAME
case|:
return|return
name|isSetTableName
argument_list|()
return|;
case|case
name|DBNAME
case|:
return|return
name|isSetDbName
argument_list|()
return|;
case|case
name|COLNAMES
case|:
return|return
name|isSetColNames
argument_list|()
return|;
case|case
name|PARTNAME
case|:
return|return
name|isSetPartName
argument_list|()
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Field "
operator|+
name|fieldID
operator|+
literal|" doesn't exist!"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|that
parameter_list|)
block|{
if|if
condition|(
name|that
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|that
operator|instanceof
name|Index
condition|)
return|return
name|this
operator|.
name|equals
argument_list|(
operator|(
name|Index
operator|)
name|that
argument_list|)
return|;
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|equals
parameter_list|(
name|Index
name|that
parameter_list|)
block|{
if|if
condition|(
name|that
operator|==
literal|null
condition|)
return|return
literal|false
return|;
name|boolean
name|this_present_indexName
init|=
literal|true
operator|&&
name|this
operator|.
name|isSetIndexName
argument_list|()
decl_stmt|;
name|boolean
name|that_present_indexName
init|=
literal|true
operator|&&
name|that
operator|.
name|isSetIndexName
argument_list|()
decl_stmt|;
if|if
condition|(
name|this_present_indexName
operator|||
name|that_present_indexName
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_indexName
operator|&&
name|that_present_indexName
operator|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|this
operator|.
name|indexName
operator|.
name|equals
argument_list|(
name|that
operator|.
name|indexName
argument_list|)
condition|)
return|return
literal|false
return|;
block|}
name|boolean
name|this_present_indexType
init|=
literal|true
decl_stmt|;
name|boolean
name|that_present_indexType
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|this_present_indexType
operator|||
name|that_present_indexType
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_indexType
operator|&&
name|that_present_indexType
operator|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|this
operator|.
name|indexType
operator|!=
name|that
operator|.
name|indexType
condition|)
return|return
literal|false
return|;
block|}
name|boolean
name|this_present_tableName
init|=
literal|true
operator|&&
name|this
operator|.
name|isSetTableName
argument_list|()
decl_stmt|;
name|boolean
name|that_present_tableName
init|=
literal|true
operator|&&
name|that
operator|.
name|isSetTableName
argument_list|()
decl_stmt|;
if|if
condition|(
name|this_present_tableName
operator|||
name|that_present_tableName
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_tableName
operator|&&
name|that_present_tableName
operator|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|this
operator|.
name|tableName
operator|.
name|equals
argument_list|(
name|that
operator|.
name|tableName
argument_list|)
condition|)
return|return
literal|false
return|;
block|}
name|boolean
name|this_present_dbName
init|=
literal|true
operator|&&
name|this
operator|.
name|isSetDbName
argument_list|()
decl_stmt|;
name|boolean
name|that_present_dbName
init|=
literal|true
operator|&&
name|that
operator|.
name|isSetDbName
argument_list|()
decl_stmt|;
if|if
condition|(
name|this_present_dbName
operator|||
name|that_present_dbName
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_dbName
operator|&&
name|that_present_dbName
operator|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|this
operator|.
name|dbName
operator|.
name|equals
argument_list|(
name|that
operator|.
name|dbName
argument_list|)
condition|)
return|return
literal|false
return|;
block|}
name|boolean
name|this_present_colNames
init|=
literal|true
operator|&&
name|this
operator|.
name|isSetColNames
argument_list|()
decl_stmt|;
name|boolean
name|that_present_colNames
init|=
literal|true
operator|&&
name|that
operator|.
name|isSetColNames
argument_list|()
decl_stmt|;
if|if
condition|(
name|this_present_colNames
operator|||
name|that_present_colNames
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_colNames
operator|&&
name|that_present_colNames
operator|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|this
operator|.
name|colNames
operator|.
name|equals
argument_list|(
name|that
operator|.
name|colNames
argument_list|)
condition|)
return|return
literal|false
return|;
block|}
name|boolean
name|this_present_partName
init|=
literal|true
operator|&&
name|this
operator|.
name|isSetPartName
argument_list|()
decl_stmt|;
name|boolean
name|that_present_partName
init|=
literal|true
operator|&&
name|that
operator|.
name|isSetPartName
argument_list|()
decl_stmt|;
if|if
condition|(
name|this_present_partName
operator|||
name|that_present_partName
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_partName
operator|&&
name|that_present_partName
operator|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|this
operator|.
name|partName
operator|.
name|equals
argument_list|(
name|that
operator|.
name|partName
argument_list|)
condition|)
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
specifier|public
name|void
name|read
parameter_list|(
name|TProtocol
name|iprot
parameter_list|)
throws|throws
name|TException
block|{
name|TField
name|field
decl_stmt|;
name|iprot
operator|.
name|readStructBegin
argument_list|()
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|field
operator|=
name|iprot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
if|if
condition|(
name|field
operator|.
name|type
operator|==
name|TType
operator|.
name|STOP
condition|)
block|{
break|break;
block|}
switch|switch
condition|(
name|field
operator|.
name|id
condition|)
block|{
case|case
name|INDEXNAME
case|:
if|if
condition|(
name|field
operator|.
name|type
operator|==
name|TType
operator|.
name|STRING
condition|)
block|{
name|this
operator|.
name|indexName
operator|=
name|iprot
operator|.
name|readString
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|TProtocolUtil
operator|.
name|skip
argument_list|(
name|iprot
argument_list|,
name|field
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|INDEXTYPE
case|:
if|if
condition|(
name|field
operator|.
name|type
operator|==
name|TType
operator|.
name|I32
condition|)
block|{
name|this
operator|.
name|indexType
operator|=
name|iprot
operator|.
name|readI32
argument_list|()
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|indexType
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|TProtocolUtil
operator|.
name|skip
argument_list|(
name|iprot
argument_list|,
name|field
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|TABLENAME
case|:
if|if
condition|(
name|field
operator|.
name|type
operator|==
name|TType
operator|.
name|STRING
condition|)
block|{
name|this
operator|.
name|tableName
operator|=
name|iprot
operator|.
name|readString
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|TProtocolUtil
operator|.
name|skip
argument_list|(
name|iprot
argument_list|,
name|field
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|DBNAME
case|:
if|if
condition|(
name|field
operator|.
name|type
operator|==
name|TType
operator|.
name|STRING
condition|)
block|{
name|this
operator|.
name|dbName
operator|=
name|iprot
operator|.
name|readString
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|TProtocolUtil
operator|.
name|skip
argument_list|(
name|iprot
argument_list|,
name|field
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|COLNAMES
case|:
if|if
condition|(
name|field
operator|.
name|type
operator|==
name|TType
operator|.
name|LIST
condition|)
block|{
block|{
name|TList
name|_list44
init|=
name|iprot
operator|.
name|readListBegin
argument_list|()
decl_stmt|;
name|this
operator|.
name|colNames
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|_list44
operator|.
name|size
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|_i45
init|=
literal|0
init|;
name|_i45
operator|<
name|_list44
operator|.
name|size
condition|;
operator|++
name|_i45
control|)
block|{
name|String
name|_elem46
decl_stmt|;
name|_elem46
operator|=
name|iprot
operator|.
name|readString
argument_list|()
expr_stmt|;
name|this
operator|.
name|colNames
operator|.
name|add
argument_list|(
name|_elem46
argument_list|)
expr_stmt|;
block|}
name|iprot
operator|.
name|readListEnd
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|TProtocolUtil
operator|.
name|skip
argument_list|(
name|iprot
argument_list|,
name|field
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|PARTNAME
case|:
if|if
condition|(
name|field
operator|.
name|type
operator|==
name|TType
operator|.
name|STRING
condition|)
block|{
name|this
operator|.
name|partName
operator|=
name|iprot
operator|.
name|readString
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|TProtocolUtil
operator|.
name|skip
argument_list|(
name|iprot
argument_list|,
name|field
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
name|TProtocolUtil
operator|.
name|skip
argument_list|(
name|iprot
argument_list|,
name|field
operator|.
name|type
argument_list|)
expr_stmt|;
break|break;
block|}
name|iprot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
block|}
name|iprot
operator|.
name|readStructEnd
argument_list|()
expr_stmt|;
name|validate
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|write
parameter_list|(
name|TProtocol
name|oprot
parameter_list|)
throws|throws
name|TException
block|{
name|validate
argument_list|()
expr_stmt|;
name|oprot
operator|.
name|writeStructBegin
argument_list|(
name|STRUCT_DESC
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|indexName
operator|!=
literal|null
condition|)
block|{
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|INDEX_NAME_FIELD_DESC
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeString
argument_list|(
name|this
operator|.
name|indexName
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
block|}
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|INDEX_TYPE_FIELD_DESC
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeI32
argument_list|(
name|this
operator|.
name|indexType
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|tableName
operator|!=
literal|null
condition|)
block|{
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|TABLE_NAME_FIELD_DESC
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeString
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|dbName
operator|!=
literal|null
condition|)
block|{
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|DB_NAME_FIELD_DESC
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeString
argument_list|(
name|this
operator|.
name|dbName
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|colNames
operator|!=
literal|null
condition|)
block|{
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|COL_NAMES_FIELD_DESC
argument_list|)
expr_stmt|;
block|{
name|oprot
operator|.
name|writeListBegin
argument_list|(
operator|new
name|TList
argument_list|(
name|TType
operator|.
name|STRING
argument_list|,
name|this
operator|.
name|colNames
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|_iter47
range|:
name|this
operator|.
name|colNames
control|)
block|{
name|oprot
operator|.
name|writeString
argument_list|(
name|_iter47
argument_list|)
expr_stmt|;
block|}
name|oprot
operator|.
name|writeListEnd
argument_list|()
expr_stmt|;
block|}
name|oprot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|partName
operator|!=
literal|null
condition|)
block|{
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|PART_NAME_FIELD_DESC
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeString
argument_list|(
name|this
operator|.
name|partName
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
block|}
name|oprot
operator|.
name|writeFieldStop
argument_list|()
expr_stmt|;
name|oprot
operator|.
name|writeStructEnd
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"Index("
argument_list|)
decl_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"indexName:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|indexName
operator|==
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"null"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|indexName
argument_list|)
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
if|if
condition|(
operator|!
name|first
condition|)
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"indexType:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|indexType
argument_list|)
expr_stmt|;
name|first
operator|=
literal|false
expr_stmt|;
if|if
condition|(
operator|!
name|first
condition|)
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"tableName:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|tableName
operator|==
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"null"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
if|if
condition|(
operator|!
name|first
condition|)
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"dbName:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|dbName
operator|==
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"null"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|dbName
argument_list|)
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
if|if
condition|(
operator|!
name|first
condition|)
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"colNames:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|colNames
operator|==
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"null"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|colNames
argument_list|)
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
if|if
condition|(
operator|!
name|first
condition|)
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"partName:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|partName
operator|==
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"null"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|partName
argument_list|)
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|void
name|validate
parameter_list|()
throws|throws
name|TException
block|{
comment|// check for required fields
comment|// check that fields of type enum have valid values
block|}
block|}
end_class

end_unit

