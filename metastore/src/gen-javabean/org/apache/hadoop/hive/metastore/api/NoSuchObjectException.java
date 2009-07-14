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
name|NoSuchObjectException
extends|extends
name|Exception
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
literal|"NoSuchObjectException"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TField
name|MESSAGE_FIELD_DESC
init|=
operator|new
name|TField
argument_list|(
literal|"message"
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
name|String
name|message
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|MESSAGE
init|=
literal|1
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
block|{   }
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
name|MESSAGE
argument_list|,
operator|new
name|FieldMetaData
argument_list|(
literal|"message"
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
name|NoSuchObjectException
operator|.
name|class
argument_list|,
name|metaDataMap
argument_list|)
expr_stmt|;
block|}
specifier|public
name|NoSuchObjectException
parameter_list|()
block|{   }
specifier|public
name|NoSuchObjectException
parameter_list|(
name|String
name|message
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|this
operator|.
name|message
operator|=
name|message
expr_stmt|;
block|}
comment|/**    * Performs a deep copy on<i>other</i>.    */
specifier|public
name|NoSuchObjectException
parameter_list|(
name|NoSuchObjectException
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|.
name|isSetMessage
argument_list|()
condition|)
block|{
name|this
operator|.
name|message
operator|=
name|other
operator|.
name|message
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|NoSuchObjectException
name|clone
parameter_list|()
block|{
return|return
operator|new
name|NoSuchObjectException
argument_list|(
name|this
argument_list|)
return|;
block|}
specifier|public
name|String
name|getMessage
parameter_list|()
block|{
return|return
name|this
operator|.
name|message
return|;
block|}
specifier|public
name|void
name|setMessage
parameter_list|(
name|String
name|message
parameter_list|)
block|{
name|this
operator|.
name|message
operator|=
name|message
expr_stmt|;
block|}
specifier|public
name|void
name|unsetMessage
parameter_list|()
block|{
name|this
operator|.
name|message
operator|=
literal|null
expr_stmt|;
block|}
comment|// Returns true if field message is set (has been asigned a value) and false otherwise
specifier|public
name|boolean
name|isSetMessage
parameter_list|()
block|{
return|return
name|this
operator|.
name|message
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
name|MESSAGE
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetMessage
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setMessage
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
name|MESSAGE
case|:
return|return
name|getMessage
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
name|MESSAGE
case|:
return|return
name|isSetMessage
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
name|NoSuchObjectException
condition|)
return|return
name|this
operator|.
name|equals
argument_list|(
operator|(
name|NoSuchObjectException
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
name|NoSuchObjectException
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
name|this_present_message
init|=
literal|true
operator|&&
name|this
operator|.
name|isSetMessage
argument_list|()
decl_stmt|;
name|boolean
name|that_present_message
init|=
literal|true
operator|&&
name|that
operator|.
name|isSetMessage
argument_list|()
decl_stmt|;
if|if
condition|(
name|this_present_message
operator|||
name|that_present_message
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_message
operator|&&
name|that_present_message
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
name|message
operator|.
name|equals
argument_list|(
name|that
operator|.
name|message
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
name|MESSAGE
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
name|message
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
name|message
operator|!=
literal|null
condition|)
block|{
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|MESSAGE_FIELD_DESC
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeString
argument_list|(
name|this
operator|.
name|message
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
literal|"NoSuchObjectException("
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
literal|"message:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|message
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
name|message
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

