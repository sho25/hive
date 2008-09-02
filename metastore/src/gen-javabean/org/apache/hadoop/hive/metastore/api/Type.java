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
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|*
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|*
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|transport
operator|.
name|*
import|;
end_import

begin_class
specifier|public
class|class
name|Type
implements|implements
name|TBase
implements|,
name|java
operator|.
name|io
operator|.
name|Serializable
block|{
specifier|private
name|String
name|name
decl_stmt|;
specifier|private
name|String
name|type1
decl_stmt|;
specifier|private
name|String
name|type2
decl_stmt|;
specifier|private
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fields
decl_stmt|;
specifier|public
specifier|final
name|Isset
name|__isset
init|=
operator|new
name|Isset
argument_list|()
decl_stmt|;
specifier|public
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
name|name
init|=
literal|false
decl_stmt|;
specifier|public
name|boolean
name|type1
init|=
literal|false
decl_stmt|;
specifier|public
name|boolean
name|type2
init|=
literal|false
decl_stmt|;
specifier|public
name|boolean
name|fields
init|=
literal|false
decl_stmt|;
block|}
specifier|public
name|Type
parameter_list|()
block|{   }
specifier|public
name|Type
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|type1
parameter_list|,
name|String
name|type2
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fields
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|name
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|type1
operator|=
name|type1
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|type1
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|type2
operator|=
name|type2
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|type2
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|fields
operator|=
name|fields
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|fields
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|this
operator|.
name|name
return|;
block|}
specifier|public
name|void
name|setName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|name
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|unsetName
parameter_list|()
block|{
name|this
operator|.
name|__isset
operator|.
name|name
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|String
name|getType1
parameter_list|()
block|{
return|return
name|this
operator|.
name|type1
return|;
block|}
specifier|public
name|void
name|setType1
parameter_list|(
name|String
name|type1
parameter_list|)
block|{
name|this
operator|.
name|type1
operator|=
name|type1
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|type1
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|unsetType1
parameter_list|()
block|{
name|this
operator|.
name|__isset
operator|.
name|type1
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|String
name|getType2
parameter_list|()
block|{
return|return
name|this
operator|.
name|type2
return|;
block|}
specifier|public
name|void
name|setType2
parameter_list|(
name|String
name|type2
parameter_list|)
block|{
name|this
operator|.
name|type2
operator|=
name|type2
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|type2
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|unsetType2
parameter_list|()
block|{
name|this
operator|.
name|__isset
operator|.
name|type2
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|int
name|getFieldsSize
parameter_list|()
block|{
return|return
operator|(
name|this
operator|.
name|fields
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|this
operator|.
name|fields
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
name|FieldSchema
argument_list|>
name|getFieldsIterator
parameter_list|()
block|{
return|return
operator|(
name|this
operator|.
name|fields
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|this
operator|.
name|fields
operator|.
name|iterator
argument_list|()
return|;
block|}
specifier|public
name|void
name|addToFields
parameter_list|(
name|FieldSchema
name|elem
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|fields
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|fields
operator|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|fields
operator|.
name|add
argument_list|(
name|elem
argument_list|)
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|fields
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getFields
parameter_list|()
block|{
return|return
name|this
operator|.
name|fields
return|;
block|}
specifier|public
name|void
name|setFields
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fields
parameter_list|)
block|{
name|this
operator|.
name|fields
operator|=
name|fields
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|fields
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|unsetFields
parameter_list|()
block|{
name|this
operator|.
name|fields
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|fields
operator|=
literal|false
expr_stmt|;
block|}
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
name|Type
condition|)
return|return
name|this
operator|.
name|equals
argument_list|(
operator|(
name|Type
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
name|Type
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
name|this_present_name
init|=
literal|true
operator|&&
operator|(
name|this
operator|.
name|name
operator|!=
literal|null
operator|)
decl_stmt|;
name|boolean
name|that_present_name
init|=
literal|true
operator|&&
operator|(
name|that
operator|.
name|name
operator|!=
literal|null
operator|)
decl_stmt|;
if|if
condition|(
name|this_present_name
operator|||
name|that_present_name
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_name
operator|&&
name|that_present_name
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
name|name
operator|.
name|equals
argument_list|(
name|that
operator|.
name|name
argument_list|)
condition|)
return|return
literal|false
return|;
block|}
name|boolean
name|this_present_type1
init|=
literal|true
operator|&&
operator|(
name|this
operator|.
name|__isset
operator|.
name|type1
operator|)
operator|&&
operator|(
name|this
operator|.
name|type1
operator|!=
literal|null
operator|)
decl_stmt|;
name|boolean
name|that_present_type1
init|=
literal|true
operator|&&
operator|(
name|that
operator|.
name|__isset
operator|.
name|type1
operator|)
operator|&&
operator|(
name|that
operator|.
name|type1
operator|!=
literal|null
operator|)
decl_stmt|;
if|if
condition|(
name|this_present_type1
operator|||
name|that_present_type1
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_type1
operator|&&
name|that_present_type1
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
name|type1
operator|.
name|equals
argument_list|(
name|that
operator|.
name|type1
argument_list|)
condition|)
return|return
literal|false
return|;
block|}
name|boolean
name|this_present_type2
init|=
literal|true
operator|&&
operator|(
name|this
operator|.
name|__isset
operator|.
name|type2
operator|)
operator|&&
operator|(
name|this
operator|.
name|type2
operator|!=
literal|null
operator|)
decl_stmt|;
name|boolean
name|that_present_type2
init|=
literal|true
operator|&&
operator|(
name|that
operator|.
name|__isset
operator|.
name|type2
operator|)
operator|&&
operator|(
name|that
operator|.
name|type2
operator|!=
literal|null
operator|)
decl_stmt|;
if|if
condition|(
name|this_present_type2
operator|||
name|that_present_type2
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_type2
operator|&&
name|that_present_type2
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
name|type2
operator|.
name|equals
argument_list|(
name|that
operator|.
name|type2
argument_list|)
condition|)
return|return
literal|false
return|;
block|}
name|boolean
name|this_present_fields
init|=
literal|true
operator|&&
operator|(
name|this
operator|.
name|__isset
operator|.
name|fields
operator|)
operator|&&
operator|(
name|this
operator|.
name|fields
operator|!=
literal|null
operator|)
decl_stmt|;
name|boolean
name|that_present_fields
init|=
literal|true
operator|&&
operator|(
name|that
operator|.
name|__isset
operator|.
name|fields
operator|)
operator|&&
operator|(
name|that
operator|.
name|fields
operator|!=
literal|null
operator|)
decl_stmt|;
if|if
condition|(
name|this_present_fields
operator|||
name|that_present_fields
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_fields
operator|&&
name|that_present_fields
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
name|fields
operator|.
name|equals
argument_list|(
name|that
operator|.
name|fields
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
operator|-
literal|1
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
name|name
operator|=
name|iprot
operator|.
name|readString
argument_list|()
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|name
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
operator|-
literal|2
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
name|type1
operator|=
name|iprot
operator|.
name|readString
argument_list|()
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|type1
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
operator|-
literal|3
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
name|type2
operator|=
name|iprot
operator|.
name|readString
argument_list|()
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|type2
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
operator|-
literal|4
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
name|_list0
init|=
name|iprot
operator|.
name|readListBegin
argument_list|()
decl_stmt|;
name|this
operator|.
name|fields
operator|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|(
name|_list0
operator|.
name|size
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|_i1
init|=
literal|0
init|;
name|_i1
operator|<
name|_list0
operator|.
name|size
condition|;
operator|++
name|_i1
control|)
block|{
name|FieldSchema
name|_elem2
init|=
operator|new
name|FieldSchema
argument_list|()
decl_stmt|;
name|_elem2
operator|=
operator|new
name|FieldSchema
argument_list|()
expr_stmt|;
name|_elem2
operator|.
name|read
argument_list|(
name|iprot
argument_list|)
expr_stmt|;
name|this
operator|.
name|fields
operator|.
name|add
argument_list|(
name|_elem2
argument_list|)
expr_stmt|;
block|}
name|iprot
operator|.
name|readListEnd
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|__isset
operator|.
name|fields
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
name|TStruct
name|struct
init|=
operator|new
name|TStruct
argument_list|(
literal|"Type"
argument_list|)
decl_stmt|;
name|oprot
operator|.
name|writeStructBegin
argument_list|(
name|struct
argument_list|)
expr_stmt|;
name|TField
name|field
init|=
operator|new
name|TField
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|name
operator|!=
literal|null
condition|)
block|{
name|field
operator|.
name|name
operator|=
literal|"name"
expr_stmt|;
name|field
operator|.
name|type
operator|=
name|TType
operator|.
name|STRING
expr_stmt|;
name|field
operator|.
name|id
operator|=
operator|-
literal|1
expr_stmt|;
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|field
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeString
argument_list|(
name|this
operator|.
name|name
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
name|type1
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|__isset
operator|.
name|type1
condition|)
block|{
name|field
operator|.
name|name
operator|=
literal|"type1"
expr_stmt|;
name|field
operator|.
name|type
operator|=
name|TType
operator|.
name|STRING
expr_stmt|;
name|field
operator|.
name|id
operator|=
operator|-
literal|2
expr_stmt|;
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|field
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeString
argument_list|(
name|this
operator|.
name|type1
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
name|this
operator|.
name|type2
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|__isset
operator|.
name|type2
condition|)
block|{
name|field
operator|.
name|name
operator|=
literal|"type2"
expr_stmt|;
name|field
operator|.
name|type
operator|=
name|TType
operator|.
name|STRING
expr_stmt|;
name|field
operator|.
name|id
operator|=
operator|-
literal|3
expr_stmt|;
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|field
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeString
argument_list|(
name|this
operator|.
name|type2
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
name|this
operator|.
name|fields
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|__isset
operator|.
name|fields
condition|)
block|{
name|field
operator|.
name|name
operator|=
literal|"fields"
expr_stmt|;
name|field
operator|.
name|type
operator|=
name|TType
operator|.
name|LIST
expr_stmt|;
name|field
operator|.
name|id
operator|=
operator|-
literal|4
expr_stmt|;
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|field
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
name|STRUCT
argument_list|,
name|this
operator|.
name|fields
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|FieldSchema
name|_iter3
range|:
name|this
operator|.
name|fields
control|)
block|{
name|_iter3
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
literal|"Type("
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"name:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|name
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|",type1:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|type1
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|",type2:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|type2
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|",fields:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|fields
argument_list|)
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
block|}
end_class

end_unit

