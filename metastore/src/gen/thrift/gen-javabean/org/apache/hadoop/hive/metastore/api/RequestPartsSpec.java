begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Autogenerated by Thrift Compiler (0.9.0)  *  * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING  *  @generated  */
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
name|commons
operator|.
name|lang
operator|.
name|builder
operator|.
name|HashCodeBuilder
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
specifier|public
class|class
name|RequestPartsSpec
extends|extends
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TUnion
argument_list|<
name|RequestPartsSpec
argument_list|,
name|RequestPartsSpec
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
literal|"RequestPartsSpec"
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
name|NAMES_FIELD_DESC
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
literal|"names"
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
name|LIST
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
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TField
name|EXPRS_FIELD_DESC
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
literal|"exprs"
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
name|LIST
argument_list|,
operator|(
name|short
operator|)
literal|2
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
name|NAMES
argument_list|(
operator|(
name|short
operator|)
literal|1
argument_list|,
literal|"names"
argument_list|)
block|,
name|EXPRS
argument_list|(
operator|(
name|short
operator|)
literal|2
argument_list|,
literal|"exprs"
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
comment|// NAMES
return|return
name|NAMES
return|;
case|case
literal|2
case|:
comment|// EXPRS
return|return
name|EXPRS
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
name|NAMES
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
literal|"names"
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
name|ListMetaData
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
name|LIST
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
name|FieldValueMetaData
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
name|STRING
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|tmpMap
operator|.
name|put
argument_list|(
name|_Fields
operator|.
name|EXPRS
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
literal|"exprs"
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
name|ListMetaData
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
name|LIST
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
name|DropPartitionsExpr
operator|.
name|class
argument_list|)
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
name|RequestPartsSpec
operator|.
name|class
argument_list|,
name|metaDataMap
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RequestPartsSpec
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|RequestPartsSpec
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
name|RequestPartsSpec
parameter_list|(
name|RequestPartsSpec
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
name|RequestPartsSpec
name|deepCopy
parameter_list|()
block|{
return|return
operator|new
name|RequestPartsSpec
argument_list|(
name|this
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|RequestPartsSpec
name|names
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|value
parameter_list|)
block|{
name|RequestPartsSpec
name|x
init|=
operator|new
name|RequestPartsSpec
argument_list|()
decl_stmt|;
name|x
operator|.
name|setNames
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|x
return|;
block|}
specifier|public
specifier|static
name|RequestPartsSpec
name|exprs
parameter_list|(
name|List
argument_list|<
name|DropPartitionsExpr
argument_list|>
name|value
parameter_list|)
block|{
name|RequestPartsSpec
name|x
init|=
operator|new
name|RequestPartsSpec
argument_list|()
decl_stmt|;
name|x
operator|.
name|setExprs
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
name|NAMES
case|:
if|if
condition|(
name|value
operator|instanceof
name|List
condition|)
block|{
break|break;
block|}
throw|throw
operator|new
name|ClassCastException
argument_list|(
literal|"Was expecting value of type List<String> for field 'names', but got "
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
case|case
name|EXPRS
case|:
if|if
condition|(
name|value
operator|instanceof
name|List
condition|)
block|{
break|break;
block|}
throw|throw
operator|new
name|ClassCastException
argument_list|(
literal|"Was expecting value of type List<DropPartitionsExpr> for field 'exprs', but got "
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
name|NAMES
case|:
if|if
condition|(
name|field
operator|.
name|type
operator|==
name|NAMES_FIELD_DESC
operator|.
name|type
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|names
decl_stmt|;
block|{
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TList
name|_list354
init|=
name|iprot
operator|.
name|readListBegin
argument_list|()
decl_stmt|;
name|names
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|_list354
operator|.
name|size
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|_i355
init|=
literal|0
init|;
name|_i355
operator|<
name|_list354
operator|.
name|size
condition|;
operator|++
name|_i355
control|)
block|{
name|String
name|_elem356
decl_stmt|;
comment|// required
name|_elem356
operator|=
name|iprot
operator|.
name|readString
argument_list|()
expr_stmt|;
name|names
operator|.
name|add
argument_list|(
name|_elem356
argument_list|)
expr_stmt|;
block|}
name|iprot
operator|.
name|readListEnd
argument_list|()
expr_stmt|;
block|}
return|return
name|names
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
case|case
name|EXPRS
case|:
if|if
condition|(
name|field
operator|.
name|type
operator|==
name|EXPRS_FIELD_DESC
operator|.
name|type
condition|)
block|{
name|List
argument_list|<
name|DropPartitionsExpr
argument_list|>
name|exprs
decl_stmt|;
block|{
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TList
name|_list357
init|=
name|iprot
operator|.
name|readListBegin
argument_list|()
decl_stmt|;
name|exprs
operator|=
operator|new
name|ArrayList
argument_list|<
name|DropPartitionsExpr
argument_list|>
argument_list|(
name|_list357
operator|.
name|size
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|_i358
init|=
literal|0
init|;
name|_i358
operator|<
name|_list357
operator|.
name|size
condition|;
operator|++
name|_i358
control|)
block|{
name|DropPartitionsExpr
name|_elem359
decl_stmt|;
comment|// required
name|_elem359
operator|=
operator|new
name|DropPartitionsExpr
argument_list|()
expr_stmt|;
name|_elem359
operator|.
name|read
argument_list|(
name|iprot
argument_list|)
expr_stmt|;
name|exprs
operator|.
name|add
argument_list|(
name|_elem359
argument_list|)
expr_stmt|;
block|}
name|iprot
operator|.
name|readListEnd
argument_list|()
expr_stmt|;
block|}
return|return
name|exprs
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
name|NAMES
case|:
name|List
argument_list|<
name|String
argument_list|>
name|names
init|=
operator|(
name|List
argument_list|<
name|String
argument_list|>
operator|)
name|value_
decl_stmt|;
block|{
name|oprot
operator|.
name|writeListBegin
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
name|TList
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
name|STRING
argument_list|,
name|names
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|_iter360
range|:
name|names
control|)
block|{
name|oprot
operator|.
name|writeString
argument_list|(
name|_iter360
argument_list|)
expr_stmt|;
block|}
name|oprot
operator|.
name|writeListEnd
argument_list|()
expr_stmt|;
block|}
return|return;
case|case
name|EXPRS
case|:
name|List
argument_list|<
name|DropPartitionsExpr
argument_list|>
name|exprs
init|=
operator|(
name|List
argument_list|<
name|DropPartitionsExpr
argument_list|>
operator|)
name|value_
decl_stmt|;
block|{
name|oprot
operator|.
name|writeListBegin
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
name|TList
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
name|exprs
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|DropPartitionsExpr
name|_iter361
range|:
name|exprs
control|)
block|{
name|_iter361
operator|.
name|write
argument_list|(
name|oprot
argument_list|)
expr_stmt|;
block|}
name|oprot
operator|.
name|writeListEnd
argument_list|()
expr_stmt|;
block|}
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
name|NAMES
case|:
name|List
argument_list|<
name|String
argument_list|>
name|names
decl_stmt|;
block|{
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TList
name|_list362
init|=
name|iprot
operator|.
name|readListBegin
argument_list|()
decl_stmt|;
name|names
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|_list362
operator|.
name|size
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|_i363
init|=
literal|0
init|;
name|_i363
operator|<
name|_list362
operator|.
name|size
condition|;
operator|++
name|_i363
control|)
block|{
name|String
name|_elem364
decl_stmt|;
comment|// required
name|_elem364
operator|=
name|iprot
operator|.
name|readString
argument_list|()
expr_stmt|;
name|names
operator|.
name|add
argument_list|(
name|_elem364
argument_list|)
expr_stmt|;
block|}
name|iprot
operator|.
name|readListEnd
argument_list|()
expr_stmt|;
block|}
return|return
name|names
return|;
case|case
name|EXPRS
case|:
name|List
argument_list|<
name|DropPartitionsExpr
argument_list|>
name|exprs
decl_stmt|;
block|{
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TList
name|_list365
init|=
name|iprot
operator|.
name|readListBegin
argument_list|()
decl_stmt|;
name|exprs
operator|=
operator|new
name|ArrayList
argument_list|<
name|DropPartitionsExpr
argument_list|>
argument_list|(
name|_list365
operator|.
name|size
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|_i366
init|=
literal|0
init|;
name|_i366
operator|<
name|_list365
operator|.
name|size
condition|;
operator|++
name|_i366
control|)
block|{
name|DropPartitionsExpr
name|_elem367
decl_stmt|;
comment|// required
name|_elem367
operator|=
operator|new
name|DropPartitionsExpr
argument_list|()
expr_stmt|;
name|_elem367
operator|.
name|read
argument_list|(
name|iprot
argument_list|)
expr_stmt|;
name|exprs
operator|.
name|add
argument_list|(
name|_elem367
argument_list|)
expr_stmt|;
block|}
name|iprot
operator|.
name|readListEnd
argument_list|()
expr_stmt|;
block|}
return|return
name|exprs
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
name|NAMES
case|:
name|List
argument_list|<
name|String
argument_list|>
name|names
init|=
operator|(
name|List
argument_list|<
name|String
argument_list|>
operator|)
name|value_
decl_stmt|;
block|{
name|oprot
operator|.
name|writeListBegin
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
name|TList
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
name|STRING
argument_list|,
name|names
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|_iter368
range|:
name|names
control|)
block|{
name|oprot
operator|.
name|writeString
argument_list|(
name|_iter368
argument_list|)
expr_stmt|;
block|}
name|oprot
operator|.
name|writeListEnd
argument_list|()
expr_stmt|;
block|}
return|return;
case|case
name|EXPRS
case|:
name|List
argument_list|<
name|DropPartitionsExpr
argument_list|>
name|exprs
init|=
operator|(
name|List
argument_list|<
name|DropPartitionsExpr
argument_list|>
operator|)
name|value_
decl_stmt|;
block|{
name|oprot
operator|.
name|writeListBegin
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
name|TList
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
name|exprs
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|DropPartitionsExpr
name|_iter369
range|:
name|exprs
control|)
block|{
name|_iter369
operator|.
name|write
argument_list|(
name|oprot
argument_list|)
expr_stmt|;
block|}
name|oprot
operator|.
name|writeListEnd
argument_list|()
expr_stmt|;
block|}
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
name|NAMES
case|:
return|return
name|NAMES_FIELD_DESC
return|;
case|case
name|EXPRS
case|:
return|return
name|EXPRS_FIELD_DESC
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
name|List
argument_list|<
name|String
argument_list|>
name|getNames
parameter_list|()
block|{
if|if
condition|(
name|getSetField
argument_list|()
operator|==
name|_Fields
operator|.
name|NAMES
condition|)
block|{
return|return
operator|(
name|List
argument_list|<
name|String
argument_list|>
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
literal|"Cannot get field 'names' because union is currently set to "
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
name|setNames
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
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
name|NAMES
expr_stmt|;
name|value_
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|DropPartitionsExpr
argument_list|>
name|getExprs
parameter_list|()
block|{
if|if
condition|(
name|getSetField
argument_list|()
operator|==
name|_Fields
operator|.
name|EXPRS
condition|)
block|{
return|return
operator|(
name|List
argument_list|<
name|DropPartitionsExpr
argument_list|>
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
literal|"Cannot get field 'exprs' because union is currently set to "
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
name|setExprs
parameter_list|(
name|List
argument_list|<
name|DropPartitionsExpr
argument_list|>
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
name|EXPRS
expr_stmt|;
name|value_
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|boolean
name|isSetNames
parameter_list|()
block|{
return|return
name|setField_
operator|==
name|_Fields
operator|.
name|NAMES
return|;
block|}
specifier|public
name|boolean
name|isSetExprs
parameter_list|()
block|{
return|return
name|setField_
operator|==
name|_Fields
operator|.
name|EXPRS
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
name|RequestPartsSpec
condition|)
block|{
return|return
name|equals
argument_list|(
operator|(
name|RequestPartsSpec
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
name|RequestPartsSpec
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
name|RequestPartsSpec
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
name|HashCodeBuilder
name|hcb
init|=
operator|new
name|HashCodeBuilder
argument_list|()
decl_stmt|;
name|hcb
operator|.
name|append
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
name|hcb
operator|.
name|append
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
name|hcb
operator|.
name|append
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
name|hcb
operator|.
name|append
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|hcb
operator|.
name|toHashCode
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

