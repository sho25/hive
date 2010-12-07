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
name|Database
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
literal|"Database"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TField
name|NAME_FIELD_DESC
init|=
operator|new
name|TField
argument_list|(
literal|"name"
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
name|DESCRIPTION_FIELD_DESC
init|=
operator|new
name|TField
argument_list|(
literal|"description"
argument_list|,
name|TType
operator|.
name|STRING
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
name|LOCATION_URI_FIELD_DESC
init|=
operator|new
name|TField
argument_list|(
literal|"locationUri"
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
name|PARAMETERS_FIELD_DESC
init|=
operator|new
name|TField
argument_list|(
literal|"parameters"
argument_list|,
name|TType
operator|.
name|MAP
argument_list|,
operator|(
name|short
operator|)
literal|4
argument_list|)
decl_stmt|;
specifier|private
name|String
name|name
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|NAME
init|=
literal|1
decl_stmt|;
specifier|private
name|String
name|description
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DESCRIPTION
init|=
literal|2
decl_stmt|;
specifier|private
name|String
name|locationUri
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|LOCATIONURI
init|=
literal|3
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|PARAMETERS
init|=
literal|4
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
name|NAME
argument_list|,
operator|new
name|FieldMetaData
argument_list|(
literal|"name"
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
name|DESCRIPTION
argument_list|,
operator|new
name|FieldMetaData
argument_list|(
literal|"description"
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
name|LOCATIONURI
argument_list|,
operator|new
name|FieldMetaData
argument_list|(
literal|"locationUri"
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
name|PARAMETERS
argument_list|,
operator|new
name|FieldMetaData
argument_list|(
literal|"parameters"
argument_list|,
name|TFieldRequirementType
operator|.
name|DEFAULT
argument_list|,
operator|new
name|MapMetaData
argument_list|(
name|TType
operator|.
name|MAP
argument_list|,
operator|new
name|FieldValueMetaData
argument_list|(
name|TType
operator|.
name|STRING
argument_list|)
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
name|Database
operator|.
name|class
argument_list|,
name|metaDataMap
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Database
parameter_list|()
block|{   }
specifier|public
name|Database
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|description
parameter_list|,
name|String
name|locationUri
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
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
name|description
operator|=
name|description
expr_stmt|;
name|this
operator|.
name|locationUri
operator|=
name|locationUri
expr_stmt|;
name|this
operator|.
name|parameters
operator|=
name|parameters
expr_stmt|;
block|}
comment|/**    * Performs a deep copy on<i>other</i>.    */
specifier|public
name|Database
parameter_list|(
name|Database
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|.
name|isSetName
argument_list|()
condition|)
block|{
name|this
operator|.
name|name
operator|=
name|other
operator|.
name|name
expr_stmt|;
block|}
if|if
condition|(
name|other
operator|.
name|isSetDescription
argument_list|()
condition|)
block|{
name|this
operator|.
name|description
operator|=
name|other
operator|.
name|description
expr_stmt|;
block|}
if|if
condition|(
name|other
operator|.
name|isSetLocationUri
argument_list|()
condition|)
block|{
name|this
operator|.
name|locationUri
operator|=
name|other
operator|.
name|locationUri
expr_stmt|;
block|}
if|if
condition|(
name|other
operator|.
name|isSetParameters
argument_list|()
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|__this__parameters
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|other_element
range|:
name|other
operator|.
name|parameters
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|other_element_key
init|=
name|other_element
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|other_element_value
init|=
name|other_element
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|String
name|__this__parameters_copy_key
init|=
name|other_element_key
decl_stmt|;
name|String
name|__this__parameters_copy_value
init|=
name|other_element_value
decl_stmt|;
name|__this__parameters
operator|.
name|put
argument_list|(
name|__this__parameters_copy_key
argument_list|,
name|__this__parameters_copy_value
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|parameters
operator|=
name|__this__parameters
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Database
name|clone
parameter_list|()
block|{
return|return
operator|new
name|Database
argument_list|(
name|this
argument_list|)
return|;
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
block|}
specifier|public
name|void
name|unsetName
parameter_list|()
block|{
name|this
operator|.
name|name
operator|=
literal|null
expr_stmt|;
block|}
comment|// Returns true if field name is set (has been asigned a value) and false otherwise
specifier|public
name|boolean
name|isSetName
parameter_list|()
block|{
return|return
name|this
operator|.
name|name
operator|!=
literal|null
return|;
block|}
specifier|public
name|String
name|getDescription
parameter_list|()
block|{
return|return
name|this
operator|.
name|description
return|;
block|}
specifier|public
name|void
name|setDescription
parameter_list|(
name|String
name|description
parameter_list|)
block|{
name|this
operator|.
name|description
operator|=
name|description
expr_stmt|;
block|}
specifier|public
name|void
name|unsetDescription
parameter_list|()
block|{
name|this
operator|.
name|description
operator|=
literal|null
expr_stmt|;
block|}
comment|// Returns true if field description is set (has been asigned a value) and false otherwise
specifier|public
name|boolean
name|isSetDescription
parameter_list|()
block|{
return|return
name|this
operator|.
name|description
operator|!=
literal|null
return|;
block|}
specifier|public
name|String
name|getLocationUri
parameter_list|()
block|{
return|return
name|this
operator|.
name|locationUri
return|;
block|}
specifier|public
name|void
name|setLocationUri
parameter_list|(
name|String
name|locationUri
parameter_list|)
block|{
name|this
operator|.
name|locationUri
operator|=
name|locationUri
expr_stmt|;
block|}
specifier|public
name|void
name|unsetLocationUri
parameter_list|()
block|{
name|this
operator|.
name|locationUri
operator|=
literal|null
expr_stmt|;
block|}
comment|// Returns true if field locationUri is set (has been asigned a value) and false otherwise
specifier|public
name|boolean
name|isSetLocationUri
parameter_list|()
block|{
return|return
name|this
operator|.
name|locationUri
operator|!=
literal|null
return|;
block|}
specifier|public
name|int
name|getParametersSize
parameter_list|()
block|{
return|return
operator|(
name|this
operator|.
name|parameters
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|this
operator|.
name|parameters
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|void
name|putToParameters
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|val
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|parameters
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|parameters
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|parameters
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getParameters
parameter_list|()
block|{
return|return
name|this
operator|.
name|parameters
return|;
block|}
specifier|public
name|void
name|setParameters
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
parameter_list|)
block|{
name|this
operator|.
name|parameters
operator|=
name|parameters
expr_stmt|;
block|}
specifier|public
name|void
name|unsetParameters
parameter_list|()
block|{
name|this
operator|.
name|parameters
operator|=
literal|null
expr_stmt|;
block|}
comment|// Returns true if field parameters is set (has been asigned a value) and false otherwise
specifier|public
name|boolean
name|isSetParameters
parameter_list|()
block|{
return|return
name|this
operator|.
name|parameters
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
name|NAME
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetName
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setName
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
name|DESCRIPTION
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetDescription
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setDescription
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
name|LOCATIONURI
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetLocationUri
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setLocationUri
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
name|PARAMETERS
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetParameters
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setParameters
argument_list|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
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
name|NAME
case|:
return|return
name|getName
argument_list|()
return|;
case|case
name|DESCRIPTION
case|:
return|return
name|getDescription
argument_list|()
return|;
case|case
name|LOCATIONURI
case|:
return|return
name|getLocationUri
argument_list|()
return|;
case|case
name|PARAMETERS
case|:
return|return
name|getParameters
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
name|NAME
case|:
return|return
name|isSetName
argument_list|()
return|;
case|case
name|DESCRIPTION
case|:
return|return
name|isSetDescription
argument_list|()
return|;
case|case
name|LOCATIONURI
case|:
return|return
name|isSetLocationUri
argument_list|()
return|;
case|case
name|PARAMETERS
case|:
return|return
name|isSetParameters
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
name|Database
condition|)
return|return
name|this
operator|.
name|equals
argument_list|(
operator|(
name|Database
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
name|Database
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
name|this
operator|.
name|isSetName
argument_list|()
decl_stmt|;
name|boolean
name|that_present_name
init|=
literal|true
operator|&&
name|that
operator|.
name|isSetName
argument_list|()
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
name|this_present_description
init|=
literal|true
operator|&&
name|this
operator|.
name|isSetDescription
argument_list|()
decl_stmt|;
name|boolean
name|that_present_description
init|=
literal|true
operator|&&
name|that
operator|.
name|isSetDescription
argument_list|()
decl_stmt|;
if|if
condition|(
name|this_present_description
operator|||
name|that_present_description
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_description
operator|&&
name|that_present_description
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
name|description
operator|.
name|equals
argument_list|(
name|that
operator|.
name|description
argument_list|)
condition|)
return|return
literal|false
return|;
block|}
name|boolean
name|this_present_locationUri
init|=
literal|true
operator|&&
name|this
operator|.
name|isSetLocationUri
argument_list|()
decl_stmt|;
name|boolean
name|that_present_locationUri
init|=
literal|true
operator|&&
name|that
operator|.
name|isSetLocationUri
argument_list|()
decl_stmt|;
if|if
condition|(
name|this_present_locationUri
operator|||
name|that_present_locationUri
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_locationUri
operator|&&
name|that_present_locationUri
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
name|locationUri
operator|.
name|equals
argument_list|(
name|that
operator|.
name|locationUri
argument_list|)
condition|)
return|return
literal|false
return|;
block|}
name|boolean
name|this_present_parameters
init|=
literal|true
operator|&&
name|this
operator|.
name|isSetParameters
argument_list|()
decl_stmt|;
name|boolean
name|that_present_parameters
init|=
literal|true
operator|&&
name|that
operator|.
name|isSetParameters
argument_list|()
decl_stmt|;
if|if
condition|(
name|this_present_parameters
operator|||
name|that_present_parameters
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_parameters
operator|&&
name|that_present_parameters
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
name|parameters
operator|.
name|equals
argument_list|(
name|that
operator|.
name|parameters
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
name|NAME
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
name|DESCRIPTION
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
name|description
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
name|LOCATIONURI
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
name|locationUri
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
name|PARAMETERS
case|:
if|if
condition|(
name|field
operator|.
name|type
operator|==
name|TType
operator|.
name|MAP
condition|)
block|{
block|{
name|TMap
name|_map4
init|=
name|iprot
operator|.
name|readMapBegin
argument_list|()
decl_stmt|;
name|this
operator|.
name|parameters
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
literal|2
operator|*
name|_map4
operator|.
name|size
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|_i5
init|=
literal|0
init|;
name|_i5
operator|<
name|_map4
operator|.
name|size
condition|;
operator|++
name|_i5
control|)
block|{
name|String
name|_key6
decl_stmt|;
name|String
name|_val7
decl_stmt|;
name|_key6
operator|=
name|iprot
operator|.
name|readString
argument_list|()
expr_stmt|;
name|_val7
operator|=
name|iprot
operator|.
name|readString
argument_list|()
expr_stmt|;
name|this
operator|.
name|parameters
operator|.
name|put
argument_list|(
name|_key6
argument_list|,
name|_val7
argument_list|)
expr_stmt|;
block|}
name|iprot
operator|.
name|readMapEnd
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
name|name
operator|!=
literal|null
condition|)
block|{
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|NAME_FIELD_DESC
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
name|description
operator|!=
literal|null
condition|)
block|{
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|DESCRIPTION_FIELD_DESC
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeString
argument_list|(
name|this
operator|.
name|description
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
name|locationUri
operator|!=
literal|null
condition|)
block|{
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|LOCATION_URI_FIELD_DESC
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeString
argument_list|(
name|this
operator|.
name|locationUri
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
name|parameters
operator|!=
literal|null
condition|)
block|{
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|PARAMETERS_FIELD_DESC
argument_list|)
expr_stmt|;
block|{
name|oprot
operator|.
name|writeMapBegin
argument_list|(
operator|new
name|TMap
argument_list|(
name|TType
operator|.
name|STRING
argument_list|,
name|TType
operator|.
name|STRING
argument_list|,
name|this
operator|.
name|parameters
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|_iter8
range|:
name|this
operator|.
name|parameters
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|oprot
operator|.
name|writeString
argument_list|(
name|_iter8
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeString
argument_list|(
name|_iter8
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|oprot
operator|.
name|writeMapEnd
argument_list|()
expr_stmt|;
block|}
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
literal|"Database("
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
literal|"name:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|name
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
name|name
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
literal|"description:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|description
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
name|description
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
literal|"locationUri:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|locationUri
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
name|locationUri
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
literal|"parameters:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|parameters
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
name|parameters
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

