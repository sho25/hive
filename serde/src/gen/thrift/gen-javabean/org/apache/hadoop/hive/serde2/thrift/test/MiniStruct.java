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
name|serde2
operator|.
name|thrift
operator|.
name|test
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
name|Generated
argument_list|(
name|value
operator|=
literal|"Autogenerated by Thrift Compiler (0.9.3)"
argument_list|)
specifier|public
class|class
name|MiniStruct
implements|implements
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TBase
argument_list|<
name|MiniStruct
argument_list|,
name|MiniStruct
operator|.
name|_Fields
argument_list|>
implements|,
name|java
operator|.
name|io
operator|.
name|Serializable
implements|,
name|Cloneable
implements|,
name|Comparable
argument_list|<
name|MiniStruct
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
literal|"MiniStruct"
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
name|MY_STRING_FIELD_DESC
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
literal|"my_string"
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
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TField
name|MY_ENUM_FIELD_DESC
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
literal|"my_enum"
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
name|Map
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|IScheme
argument_list|>
argument_list|,
name|SchemeFactory
argument_list|>
name|schemes
init|=
operator|new
name|HashMap
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|IScheme
argument_list|>
argument_list|,
name|SchemeFactory
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|schemes
operator|.
name|put
argument_list|(
name|StandardScheme
operator|.
name|class
argument_list|,
operator|new
name|MiniStructStandardSchemeFactory
argument_list|()
argument_list|)
expr_stmt|;
name|schemes
operator|.
name|put
argument_list|(
name|TupleScheme
operator|.
name|class
argument_list|,
operator|new
name|MiniStructTupleSchemeFactory
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|my_string
decl_stmt|;
comment|// optional
specifier|private
name|MyEnum
name|my_enum
decl_stmt|;
comment|// optional
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
name|MY_STRING
argument_list|(
operator|(
name|short
operator|)
literal|1
argument_list|,
literal|"my_string"
argument_list|)
block|,
comment|/**      *       * @see MyEnum      */
name|MY_ENUM
argument_list|(
operator|(
name|short
operator|)
literal|2
argument_list|,
literal|"my_enum"
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
comment|// MY_STRING
return|return
name|MY_STRING
return|;
case|case
literal|2
case|:
comment|// MY_ENUM
return|return
name|MY_ENUM
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
comment|// isset id assignments
specifier|private
specifier|static
specifier|final
name|_Fields
name|optionals
index|[]
init|=
block|{
name|_Fields
operator|.
name|MY_STRING
block|,
name|_Fields
operator|.
name|MY_ENUM
block|}
decl_stmt|;
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
name|MY_STRING
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
literal|"my_string"
argument_list|,
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TFieldRequirementType
operator|.
name|OPTIONAL
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
expr_stmt|;
name|tmpMap
operator|.
name|put
argument_list|(
name|_Fields
operator|.
name|MY_ENUM
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
literal|"my_enum"
argument_list|,
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TFieldRequirementType
operator|.
name|OPTIONAL
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
name|EnumMetaData
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
name|ENUM
argument_list|,
name|MyEnum
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
name|MiniStruct
operator|.
name|class
argument_list|,
name|metaDataMap
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MiniStruct
parameter_list|()
block|{   }
comment|/**    * Performs a deep copy on<i>other</i>.    */
specifier|public
name|MiniStruct
parameter_list|(
name|MiniStruct
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|.
name|isSetMy_string
argument_list|()
condition|)
block|{
name|this
operator|.
name|my_string
operator|=
name|other
operator|.
name|my_string
expr_stmt|;
block|}
if|if
condition|(
name|other
operator|.
name|isSetMy_enum
argument_list|()
condition|)
block|{
name|this
operator|.
name|my_enum
operator|=
name|other
operator|.
name|my_enum
expr_stmt|;
block|}
block|}
specifier|public
name|MiniStruct
name|deepCopy
parameter_list|()
block|{
return|return
operator|new
name|MiniStruct
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|this
operator|.
name|my_string
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|my_enum
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|String
name|getMy_string
parameter_list|()
block|{
return|return
name|this
operator|.
name|my_string
return|;
block|}
specifier|public
name|void
name|setMy_string
parameter_list|(
name|String
name|my_string
parameter_list|)
block|{
name|this
operator|.
name|my_string
operator|=
name|my_string
expr_stmt|;
block|}
specifier|public
name|void
name|unsetMy_string
parameter_list|()
block|{
name|this
operator|.
name|my_string
operator|=
literal|null
expr_stmt|;
block|}
comment|/** Returns true if field my_string is set (has been assigned a value) and false otherwise */
specifier|public
name|boolean
name|isSetMy_string
parameter_list|()
block|{
return|return
name|this
operator|.
name|my_string
operator|!=
literal|null
return|;
block|}
specifier|public
name|void
name|setMy_stringIsSet
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
if|if
condition|(
operator|!
name|value
condition|)
block|{
name|this
operator|.
name|my_string
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/**    *     * @see MyEnum    */
specifier|public
name|MyEnum
name|getMy_enum
parameter_list|()
block|{
return|return
name|this
operator|.
name|my_enum
return|;
block|}
comment|/**    *     * @see MyEnum    */
specifier|public
name|void
name|setMy_enum
parameter_list|(
name|MyEnum
name|my_enum
parameter_list|)
block|{
name|this
operator|.
name|my_enum
operator|=
name|my_enum
expr_stmt|;
block|}
specifier|public
name|void
name|unsetMy_enum
parameter_list|()
block|{
name|this
operator|.
name|my_enum
operator|=
literal|null
expr_stmt|;
block|}
comment|/** Returns true if field my_enum is set (has been assigned a value) and false otherwise */
specifier|public
name|boolean
name|isSetMy_enum
parameter_list|()
block|{
return|return
name|this
operator|.
name|my_enum
operator|!=
literal|null
return|;
block|}
specifier|public
name|void
name|setMy_enumIsSet
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
if|if
condition|(
operator|!
name|value
condition|)
block|{
name|this
operator|.
name|my_enum
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|setFieldValue
parameter_list|(
name|_Fields
name|field
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
switch|switch
condition|(
name|field
condition|)
block|{
case|case
name|MY_STRING
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetMy_string
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setMy_string
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
name|MY_ENUM
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetMy_enum
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setMy_enum
argument_list|(
operator|(
name|MyEnum
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
block|}
specifier|public
name|Object
name|getFieldValue
parameter_list|(
name|_Fields
name|field
parameter_list|)
block|{
switch|switch
condition|(
name|field
condition|)
block|{
case|case
name|MY_STRING
case|:
return|return
name|getMy_string
argument_list|()
return|;
case|case
name|MY_ENUM
case|:
return|return
name|getMy_enum
argument_list|()
return|;
block|}
throw|throw
operator|new
name|IllegalStateException
argument_list|()
throw|;
block|}
comment|/** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
specifier|public
name|boolean
name|isSet
parameter_list|(
name|_Fields
name|field
parameter_list|)
block|{
if|if
condition|(
name|field
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|()
throw|;
block|}
switch|switch
condition|(
name|field
condition|)
block|{
case|case
name|MY_STRING
case|:
return|return
name|isSetMy_string
argument_list|()
return|;
case|case
name|MY_ENUM
case|:
return|return
name|isSetMy_enum
argument_list|()
return|;
block|}
throw|throw
operator|new
name|IllegalStateException
argument_list|()
throw|;
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
name|MiniStruct
condition|)
return|return
name|this
operator|.
name|equals
argument_list|(
operator|(
name|MiniStruct
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
name|MiniStruct
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
name|this_present_my_string
init|=
literal|true
operator|&&
name|this
operator|.
name|isSetMy_string
argument_list|()
decl_stmt|;
name|boolean
name|that_present_my_string
init|=
literal|true
operator|&&
name|that
operator|.
name|isSetMy_string
argument_list|()
decl_stmt|;
if|if
condition|(
name|this_present_my_string
operator|||
name|that_present_my_string
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_my_string
operator|&&
name|that_present_my_string
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
name|my_string
operator|.
name|equals
argument_list|(
name|that
operator|.
name|my_string
argument_list|)
condition|)
return|return
literal|false
return|;
block|}
name|boolean
name|this_present_my_enum
init|=
literal|true
operator|&&
name|this
operator|.
name|isSetMy_enum
argument_list|()
decl_stmt|;
name|boolean
name|that_present_my_enum
init|=
literal|true
operator|&&
name|that
operator|.
name|isSetMy_enum
argument_list|()
decl_stmt|;
if|if
condition|(
name|this_present_my_enum
operator|||
name|that_present_my_enum
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_my_enum
operator|&&
name|that_present_my_enum
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
name|my_enum
operator|.
name|equals
argument_list|(
name|that
operator|.
name|my_enum
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
name|boolean
name|present_my_string
init|=
literal|true
operator|&&
operator|(
name|isSetMy_string
argument_list|()
operator|)
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|present_my_string
argument_list|)
expr_stmt|;
if|if
condition|(
name|present_my_string
condition|)
name|list
operator|.
name|add
argument_list|(
name|my_string
argument_list|)
expr_stmt|;
name|boolean
name|present_my_enum
init|=
literal|true
operator|&&
operator|(
name|isSetMy_enum
argument_list|()
operator|)
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|present_my_enum
argument_list|)
expr_stmt|;
if|if
condition|(
name|present_my_enum
condition|)
name|list
operator|.
name|add
argument_list|(
name|my_enum
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|list
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|MiniStruct
name|other
parameter_list|)
block|{
if|if
condition|(
operator|!
name|getClass
argument_list|()
operator|.
name|equals
argument_list|(
name|other
operator|.
name|getClass
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
name|int
name|lastComparison
init|=
literal|0
decl_stmt|;
name|lastComparison
operator|=
name|Boolean
operator|.
name|valueOf
argument_list|(
name|isSetMy_string
argument_list|()
argument_list|)
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|isSetMy_string
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|lastComparison
operator|!=
literal|0
condition|)
block|{
return|return
name|lastComparison
return|;
block|}
if|if
condition|(
name|isSetMy_string
argument_list|()
condition|)
block|{
name|lastComparison
operator|=
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
name|this
operator|.
name|my_string
argument_list|,
name|other
operator|.
name|my_string
argument_list|)
expr_stmt|;
if|if
condition|(
name|lastComparison
operator|!=
literal|0
condition|)
block|{
return|return
name|lastComparison
return|;
block|}
block|}
name|lastComparison
operator|=
name|Boolean
operator|.
name|valueOf
argument_list|(
name|isSetMy_enum
argument_list|()
argument_list|)
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|isSetMy_enum
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|lastComparison
operator|!=
literal|0
condition|)
block|{
return|return
name|lastComparison
return|;
block|}
if|if
condition|(
name|isSetMy_enum
argument_list|()
condition|)
block|{
name|lastComparison
operator|=
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
name|this
operator|.
name|my_enum
argument_list|,
name|other
operator|.
name|my_enum
argument_list|)
expr_stmt|;
if|if
condition|(
name|lastComparison
operator|!=
literal|0
condition|)
block|{
return|return
name|lastComparison
return|;
block|}
block|}
return|return
literal|0
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
name|void
name|read
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
name|schemes
operator|.
name|get
argument_list|(
name|iprot
operator|.
name|getScheme
argument_list|()
argument_list|)
operator|.
name|getScheme
argument_list|()
operator|.
name|read
argument_list|(
name|iprot
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|write
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
name|schemes
operator|.
name|get
argument_list|(
name|oprot
operator|.
name|getScheme
argument_list|()
argument_list|)
operator|.
name|getScheme
argument_list|()
operator|.
name|write
argument_list|(
name|oprot
argument_list|,
name|this
argument_list|)
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
literal|"MiniStruct("
argument_list|)
decl_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|isSetMy_string
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"my_string:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|my_string
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
name|my_string
argument_list|)
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|isSetMy_enum
argument_list|()
condition|)
block|{
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
literal|"my_enum:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|my_enum
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
name|my_enum
argument_list|)
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
block|}
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
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
block|{
comment|// check for required fields
comment|// check for sub-struct validity
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
specifier|private
specifier|static
class|class
name|MiniStructStandardSchemeFactory
implements|implements
name|SchemeFactory
block|{
specifier|public
name|MiniStructStandardScheme
name|getScheme
parameter_list|()
block|{
return|return
operator|new
name|MiniStructStandardScheme
argument_list|()
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|MiniStructStandardScheme
extends|extends
name|StandardScheme
argument_list|<
name|MiniStruct
argument_list|>
block|{
specifier|public
name|void
name|read
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
name|MiniStruct
name|struct
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
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TField
name|schemeField
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
name|schemeField
operator|=
name|iprot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
if|if
condition|(
name|schemeField
operator|.
name|type
operator|==
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
name|STOP
condition|)
block|{
break|break;
block|}
switch|switch
condition|(
name|schemeField
operator|.
name|id
condition|)
block|{
case|case
literal|1
case|:
comment|// MY_STRING
if|if
condition|(
name|schemeField
operator|.
name|type
operator|==
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
condition|)
block|{
name|struct
operator|.
name|my_string
operator|=
name|iprot
operator|.
name|readString
argument_list|()
expr_stmt|;
name|struct
operator|.
name|setMy_stringIsSet
argument_list|(
literal|true
argument_list|)
expr_stmt|;
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
name|schemeField
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
literal|2
case|:
comment|// MY_ENUM
if|if
condition|(
name|schemeField
operator|.
name|type
operator|==
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
name|I32
condition|)
block|{
name|struct
operator|.
name|my_enum
operator|=
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|serde2
operator|.
name|thrift
operator|.
name|test
operator|.
name|MyEnum
operator|.
name|findByValue
argument_list|(
name|iprot
operator|.
name|readI32
argument_list|()
argument_list|)
expr_stmt|;
name|struct
operator|.
name|setMy_enumIsSet
argument_list|(
literal|true
argument_list|)
expr_stmt|;
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
name|schemeField
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
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
name|schemeField
operator|.
name|type
argument_list|)
expr_stmt|;
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
name|struct
operator|.
name|validate
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|write
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
parameter_list|,
name|MiniStruct
name|struct
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
name|struct
operator|.
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
name|struct
operator|.
name|my_string
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|struct
operator|.
name|isSetMy_string
argument_list|()
condition|)
block|{
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|MY_STRING_FIELD_DESC
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeString
argument_list|(
name|struct
operator|.
name|my_string
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|struct
operator|.
name|my_enum
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|struct
operator|.
name|isSetMy_enum
argument_list|()
condition|)
block|{
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|MY_ENUM_FIELD_DESC
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeI32
argument_list|(
name|struct
operator|.
name|my_enum
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
block|}
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
block|}
specifier|private
specifier|static
class|class
name|MiniStructTupleSchemeFactory
implements|implements
name|SchemeFactory
block|{
specifier|public
name|MiniStructTupleScheme
name|getScheme
parameter_list|()
block|{
return|return
operator|new
name|MiniStructTupleScheme
argument_list|()
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|MiniStructTupleScheme
extends|extends
name|TupleScheme
argument_list|<
name|MiniStruct
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|void
name|write
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
name|prot
parameter_list|,
name|MiniStruct
name|struct
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
name|TTupleProtocol
name|oprot
init|=
operator|(
name|TTupleProtocol
operator|)
name|prot
decl_stmt|;
name|BitSet
name|optionals
init|=
operator|new
name|BitSet
argument_list|()
decl_stmt|;
if|if
condition|(
name|struct
operator|.
name|isSetMy_string
argument_list|()
condition|)
block|{
name|optionals
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|struct
operator|.
name|isSetMy_enum
argument_list|()
condition|)
block|{
name|optionals
operator|.
name|set
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|oprot
operator|.
name|writeBitSet
argument_list|(
name|optionals
argument_list|,
literal|2
argument_list|)
expr_stmt|;
if|if
condition|(
name|struct
operator|.
name|isSetMy_string
argument_list|()
condition|)
block|{
name|oprot
operator|.
name|writeString
argument_list|(
name|struct
operator|.
name|my_string
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|struct
operator|.
name|isSetMy_enum
argument_list|()
condition|)
block|{
name|oprot
operator|.
name|writeI32
argument_list|(
name|struct
operator|.
name|my_enum
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|read
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
name|prot
parameter_list|,
name|MiniStruct
name|struct
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
name|TTupleProtocol
name|iprot
init|=
operator|(
name|TTupleProtocol
operator|)
name|prot
decl_stmt|;
name|BitSet
name|incoming
init|=
name|iprot
operator|.
name|readBitSet
argument_list|(
literal|2
argument_list|)
decl_stmt|;
if|if
condition|(
name|incoming
operator|.
name|get
argument_list|(
literal|0
argument_list|)
condition|)
block|{
name|struct
operator|.
name|my_string
operator|=
name|iprot
operator|.
name|readString
argument_list|()
expr_stmt|;
name|struct
operator|.
name|setMy_stringIsSet
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|incoming
operator|.
name|get
argument_list|(
literal|1
argument_list|)
condition|)
block|{
name|struct
operator|.
name|my_enum
operator|=
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|serde2
operator|.
name|thrift
operator|.
name|test
operator|.
name|MyEnum
operator|.
name|findByValue
argument_list|(
name|iprot
operator|.
name|readI32
argument_list|()
argument_list|)
expr_stmt|;
name|struct
operator|.
name|setMy_enumIsSet
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

