begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Autogenerated by Thrift Compiler (0.9.3)  *  * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING  *  @generated  */
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
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|scheme
operator|.
name|IScheme
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
name|scheme
operator|.
name|SchemeFactory
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
name|scheme
operator|.
name|StandardScheme
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
name|scheme
operator|.
name|TupleScheme
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
name|TTupleProtocol
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
name|TProtocolException
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
name|EncodingUtils
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
name|TException
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
name|async
operator|.
name|AsyncMethodCallback
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
name|server
operator|.
name|AbstractNonblockingServer
operator|.
name|*
import|;
end_import

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
name|EnumMap
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
name|EnumSet
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
name|java
operator|.
name|util
operator|.
name|BitSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Generated
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"cast"
block|,
literal|"rawtypes"
block|,
literal|"serial"
block|,
literal|"unchecked"
block|}
argument_list|)
annotation|@
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|FireEventRequestData
extends|extends
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TUnion
argument_list|<
name|FireEventRequestData
argument_list|,
name|FireEventRequestData
operator|.
name|_Fields
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TStruct
name|STRUCT_DESC
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TStruct
argument_list|(
literal|"FireEventRequestData"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TField
name|INSERT_DATA_FIELD_DESC
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TField
argument_list|(
literal|"insertData"
argument_list|,
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TType
operator|.
name|STRUCT
argument_list|,
operator|(
name|short
operator|)
literal|1
argument_list|)
decl_stmt|;
comment|/** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
specifier|public
enum|enum
name|_Fields
implements|implements
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TFieldIdEnum
block|{
name|INSERT_DATA
argument_list|(
operator|(
name|short
operator|)
literal|1
argument_list|,
literal|"insertData"
argument_list|)
block|;
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|_Fields
argument_list|>
name|byName
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|_Fields
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
for|for
control|(
name|_Fields
name|field
range|:
name|EnumSet
operator|.
name|allOf
argument_list|(
name|_Fields
operator|.
name|class
argument_list|)
control|)
block|{
name|byName
operator|.
name|put
argument_list|(
name|field
operator|.
name|getFieldName
argument_list|()
argument_list|,
name|field
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Find the _Fields constant that matches fieldId, or null if its not found.      */
specifier|public
specifier|static
name|_Fields
name|findByThriftId
parameter_list|(
name|int
name|fieldId
parameter_list|)
block|{
switch|switch
condition|(
name|fieldId
condition|)
block|{
case|case
literal|1
case|:
comment|// INSERT_DATA
return|return
name|INSERT_DATA
return|;
default|default:
return|return
literal|null
return|;
block|}
block|}
comment|/**      * Find the _Fields constant that matches fieldId, throwing an exception      * if it is not found.      */
specifier|public
specifier|static
name|_Fields
name|findByThriftIdOrThrow
parameter_list|(
name|int
name|fieldId
parameter_list|)
block|{
name|_Fields
name|fields
init|=
name|findByThriftId
argument_list|(
name|fieldId
argument_list|)
decl_stmt|;
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Field "
operator|+
name|fieldId
operator|+
literal|" doesn't exist!"
argument_list|)
throw|;
return|return
name|fields
return|;
block|}
comment|/**      * Find the _Fields constant that matches name, or null if its not found.      */
specifier|public
specifier|static
name|_Fields
name|findByName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|byName
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
specifier|private
specifier|final
name|short
name|_thriftId
decl_stmt|;
specifier|private
specifier|final
name|String
name|_fieldName
decl_stmt|;
name|_Fields
parameter_list|(
name|short
name|thriftId
parameter_list|,
name|String
name|fieldName
parameter_list|)
block|{
name|_thriftId
operator|=
name|thriftId
expr_stmt|;
name|_fieldName
operator|=
name|fieldName
expr_stmt|;
block|}
specifier|public
name|short
name|getThriftFieldId
parameter_list|()
block|{
return|return
name|_thriftId
return|;
block|}
specifier|public
name|String
name|getFieldName
parameter_list|()
block|{
return|return
name|_fieldName
return|;
block|}
block|}
specifier|public
specifier|static
specifier|final
name|Map
argument_list|<
name|_Fields
argument_list|,
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|meta_data
operator|.
name|FieldMetaData
argument_list|>
name|metaDataMap
decl_stmt|;
static|static
block|{
name|Map
argument_list|<
name|_Fields
argument_list|,
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|meta_data
operator|.
name|FieldMetaData
argument_list|>
name|tmpMap
init|=
operator|new
name|EnumMap
argument_list|<
name|_Fields
argument_list|,
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|meta_data
operator|.
name|FieldMetaData
argument_list|>
argument_list|(
name|_Fields
operator|.
name|class
argument_list|)
decl_stmt|;
name|tmpMap
operator|.
name|put
argument_list|(
name|_Fields
operator|.
name|INSERT_DATA
argument_list|,
operator|new
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|meta_data
operator|.
name|FieldMetaData
argument_list|(
literal|"insertData"
argument_list|,
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TFieldRequirementType
operator|.
name|DEFAULT
argument_list|,
operator|new
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|meta_data
operator|.
name|StructMetaData
argument_list|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TType
operator|.
name|STRUCT
argument_list|,
name|InsertEventRequestData
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|metaDataMap
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|tmpMap
argument_list|)
expr_stmt|;
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|meta_data
operator|.
name|FieldMetaData
operator|.
name|addStructMetaDataMap
argument_list|(
name|FireEventRequestData
operator|.
name|class
argument_list|,
name|metaDataMap
argument_list|)
expr_stmt|;
block|}
specifier|public
name|FireEventRequestData
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|FireEventRequestData
parameter_list|(
name|_Fields
name|setField
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|setField
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|FireEventRequestData
parameter_list|(
name|FireEventRequestData
name|other
parameter_list|)
block|{
name|super
argument_list|(
name|other
argument_list|)
expr_stmt|;
block|}
specifier|public
name|FireEventRequestData
name|deepCopy
parameter_list|()
block|{
return|return
operator|new
name|FireEventRequestData
argument_list|(
name|this
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|FireEventRequestData
name|insertData
parameter_list|(
name|InsertEventRequestData
name|value
parameter_list|)
block|{
name|FireEventRequestData
name|x
init|=
operator|new
name|FireEventRequestData
argument_list|()
decl_stmt|;
name|x
operator|.
name|setInsertData
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|x
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|checkType
parameter_list|(
name|_Fields
name|setField
parameter_list|,
name|Object
name|value
parameter_list|)
throws|throws
name|ClassCastException
block|{
switch|switch
condition|(
name|setField
condition|)
block|{
case|case
name|INSERT_DATA
case|:
if|if
condition|(
name|value
operator|instanceof
name|InsertEventRequestData
condition|)
block|{
break|break;
block|}
throw|throw
operator|new
name|ClassCastException
argument_list|(
literal|"Was expecting value of type InsertEventRequestData for field 'insertData', but got "
operator|+
name|value
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
throw|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown field id "
operator|+
name|setField
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|Object
name|standardSchemeReadValue
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocol
name|iprot
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TField
name|field
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
block|{
name|_Fields
name|setField
init|=
name|_Fields
operator|.
name|findByThriftId
argument_list|(
name|field
operator|.
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|setField
operator|!=
literal|null
condition|)
block|{
switch|switch
condition|(
name|setField
condition|)
block|{
case|case
name|INSERT_DATA
case|:
if|if
condition|(
name|field
operator|.
name|type
operator|==
name|INSERT_DATA_FIELD_DESC
operator|.
name|type
condition|)
block|{
name|InsertEventRequestData
name|insertData
decl_stmt|;
name|insertData
operator|=
operator|new
name|InsertEventRequestData
argument_list|()
expr_stmt|;
name|insertData
operator|.
name|read
argument_list|(
name|iprot
argument_list|)
expr_stmt|;
return|return
name|insertData
return|;
block|}
else|else
block|{
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
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
return|return
literal|null
return|;
block|}
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"setField wasn't null, but didn't match any of the case statements!"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
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
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|standardSchemeWriteValue
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocol
name|oprot
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
block|{
switch|switch
condition|(
name|setField_
condition|)
block|{
case|case
name|INSERT_DATA
case|:
name|InsertEventRequestData
name|insertData
init|=
operator|(
name|InsertEventRequestData
operator|)
name|value_
decl_stmt|;
name|insertData
operator|.
name|write
argument_list|(
name|oprot
argument_list|)
expr_stmt|;
return|return;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Cannot write union with unknown field "
operator|+
name|setField_
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|Object
name|tupleSchemeReadValue
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocol
name|iprot
parameter_list|,
name|short
name|fieldID
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
block|{
name|_Fields
name|setField
init|=
name|_Fields
operator|.
name|findByThriftId
argument_list|(
name|fieldID
argument_list|)
decl_stmt|;
if|if
condition|(
name|setField
operator|!=
literal|null
condition|)
block|{
switch|switch
condition|(
name|setField
condition|)
block|{
case|case
name|INSERT_DATA
case|:
name|InsertEventRequestData
name|insertData
decl_stmt|;
name|insertData
operator|=
operator|new
name|InsertEventRequestData
argument_list|()
expr_stmt|;
name|insertData
operator|.
name|read
argument_list|(
name|iprot
argument_list|)
expr_stmt|;
return|return
name|insertData
return|;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"setField wasn't null, but didn't match any of the case statements!"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|TProtocolException
argument_list|(
literal|"Couldn't find a field with field id "
operator|+
name|fieldID
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|tupleSchemeWriteValue
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocol
name|oprot
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
block|{
switch|switch
condition|(
name|setField_
condition|)
block|{
case|case
name|INSERT_DATA
case|:
name|InsertEventRequestData
name|insertData
init|=
operator|(
name|InsertEventRequestData
operator|)
name|value_
decl_stmt|;
name|insertData
operator|.
name|write
argument_list|(
name|oprot
argument_list|)
expr_stmt|;
return|return;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Cannot write union with unknown field "
operator|+
name|setField_
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TField
name|getFieldDesc
parameter_list|(
name|_Fields
name|setField
parameter_list|)
block|{
switch|switch
condition|(
name|setField
condition|)
block|{
case|case
name|INSERT_DATA
case|:
return|return
name|INSERT_DATA_FIELD_DESC
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown field id "
operator|+
name|setField
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TStruct
name|getStructDesc
parameter_list|()
block|{
return|return
name|STRUCT_DESC
return|;
block|}
annotation|@
name|Override
specifier|protected
name|_Fields
name|enumForId
parameter_list|(
name|short
name|id
parameter_list|)
block|{
return|return
name|_Fields
operator|.
name|findByThriftIdOrThrow
argument_list|(
name|id
argument_list|)
return|;
block|}
specifier|public
name|_Fields
name|fieldForId
parameter_list|(
name|int
name|fieldId
parameter_list|)
block|{
return|return
name|_Fields
operator|.
name|findByThriftId
argument_list|(
name|fieldId
argument_list|)
return|;
block|}
specifier|public
name|InsertEventRequestData
name|getInsertData
parameter_list|()
block|{
if|if
condition|(
name|getSetField
argument_list|()
operator|==
name|_Fields
operator|.
name|INSERT_DATA
condition|)
block|{
return|return
operator|(
name|InsertEventRequestData
operator|)
name|getFieldValue
argument_list|()
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cannot get field 'insertData' because union is currently set to "
operator|+
name|getFieldDesc
argument_list|(
name|getSetField
argument_list|()
argument_list|)
operator|.
name|name
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|setInsertData
parameter_list|(
name|InsertEventRequestData
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
throw|throw
operator|new
name|NullPointerException
argument_list|()
throw|;
name|setField_
operator|=
name|_Fields
operator|.
name|INSERT_DATA
expr_stmt|;
name|value_
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|boolean
name|isSetInsertData
parameter_list|()
block|{
return|return
name|setField_
operator|==
name|_Fields
operator|.
name|INSERT_DATA
return|;
block|}
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|instanceof
name|FireEventRequestData
condition|)
block|{
return|return
name|equals
argument_list|(
operator|(
name|FireEventRequestData
operator|)
name|other
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
specifier|public
name|boolean
name|equals
parameter_list|(
name|FireEventRequestData
name|other
parameter_list|)
block|{
return|return
name|other
operator|!=
literal|null
operator|&&
name|getSetField
argument_list|()
operator|==
name|other
operator|.
name|getSetField
argument_list|()
operator|&&
name|getFieldValue
argument_list|()
operator|.
name|equals
argument_list|(
name|other
operator|.
name|getFieldValue
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|FireEventRequestData
name|other
parameter_list|)
block|{
name|int
name|lastComparison
init|=
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TBaseHelper
operator|.
name|compareTo
argument_list|(
name|getSetField
argument_list|()
argument_list|,
name|other
operator|.
name|getSetField
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|lastComparison
operator|==
literal|0
condition|)
block|{
return|return
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TBaseHelper
operator|.
name|compareTo
argument_list|(
name|getFieldValue
argument_list|()
argument_list|,
name|other
operator|.
name|getFieldValue
argument_list|()
argument_list|)
return|;
block|}
return|return
name|lastComparison
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TFieldIdEnum
name|setField
init|=
name|getSetField
argument_list|()
decl_stmt|;
if|if
condition|(
name|setField
operator|!=
literal|null
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
name|setField
operator|.
name|getThriftFieldId
argument_list|()
argument_list|)
expr_stmt|;
name|Object
name|value
init|=
name|getFieldValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TEnum
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
operator|(
operator|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TEnum
operator|)
name|getFieldValue
argument_list|()
operator|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|list
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|list
operator|.
name|hashCode
argument_list|()
return|;
block|}
specifier|private
name|void
name|writeObject
parameter_list|(
name|java
operator|.
name|io
operator|.
name|ObjectOutputStream
name|out
parameter_list|)
throws|throws
name|java
operator|.
name|io
operator|.
name|IOException
block|{
try|try
block|{
name|write
argument_list|(
operator|new
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TCompactProtocol
argument_list|(
operator|new
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TIOStreamTransport
argument_list|(
name|out
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
name|te
parameter_list|)
block|{
throw|throw
operator|new
name|java
operator|.
name|io
operator|.
name|IOException
argument_list|(
name|te
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|readObject
parameter_list|(
name|java
operator|.
name|io
operator|.
name|ObjectInputStream
name|in
parameter_list|)
throws|throws
name|java
operator|.
name|io
operator|.
name|IOException
throws|,
name|ClassNotFoundException
block|{
try|try
block|{
name|read
argument_list|(
operator|new
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TCompactProtocol
argument_list|(
operator|new
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TIOStreamTransport
argument_list|(
name|in
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
name|te
parameter_list|)
block|{
throw|throw
operator|new
name|java
operator|.
name|io
operator|.
name|IOException
argument_list|(
name|te
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

