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
name|Partition
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
name|List
argument_list|<
name|String
argument_list|>
name|values
decl_stmt|;
specifier|private
name|String
name|dbName
decl_stmt|;
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|private
name|int
name|createTime
decl_stmt|;
specifier|private
name|int
name|lastAccessTime
decl_stmt|;
specifier|private
name|StorageDescriptor
name|sd
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
name|values
init|=
literal|false
decl_stmt|;
specifier|public
name|boolean
name|dbName
init|=
literal|false
decl_stmt|;
specifier|public
name|boolean
name|tableName
init|=
literal|false
decl_stmt|;
specifier|public
name|boolean
name|createTime
init|=
literal|false
decl_stmt|;
specifier|public
name|boolean
name|lastAccessTime
init|=
literal|false
decl_stmt|;
specifier|public
name|boolean
name|sd
init|=
literal|false
decl_stmt|;
specifier|public
name|boolean
name|parameters
init|=
literal|false
decl_stmt|;
block|}
specifier|public
name|Partition
parameter_list|()
block|{ }
specifier|public
name|Partition
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|values
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|int
name|createTime
parameter_list|,
name|int
name|lastAccessTime
parameter_list|,
name|StorageDescriptor
name|sd
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
name|values
operator|=
name|values
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|values
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|dbName
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
name|__isset
operator|.
name|tableName
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|createTime
operator|=
name|createTime
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|createTime
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|lastAccessTime
operator|=
name|lastAccessTime
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|lastAccessTime
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|sd
operator|=
name|sd
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|sd
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|parameters
operator|=
name|parameters
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|parameters
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|int
name|getValuesSize
parameter_list|()
block|{
return|return
operator|(
name|this
operator|.
name|values
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|this
operator|.
name|values
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
name|getValuesIterator
parameter_list|()
block|{
return|return
operator|(
name|this
operator|.
name|values
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|this
operator|.
name|values
operator|.
name|iterator
argument_list|()
return|;
block|}
specifier|public
name|void
name|addToValues
parameter_list|(
name|String
name|elem
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|values
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|values
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
name|values
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
name|values
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getValues
parameter_list|()
block|{
return|return
name|this
operator|.
name|values
return|;
block|}
specifier|public
name|void
name|setValues
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|values
parameter_list|)
block|{
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|values
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|unsetValues
parameter_list|()
block|{
name|this
operator|.
name|values
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|values
operator|=
literal|false
expr_stmt|;
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
name|this
operator|.
name|__isset
operator|.
name|dbName
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|unsetDbName
parameter_list|()
block|{
name|this
operator|.
name|__isset
operator|.
name|dbName
operator|=
literal|false
expr_stmt|;
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
name|this
operator|.
name|__isset
operator|.
name|tableName
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|unsetTableName
parameter_list|()
block|{
name|this
operator|.
name|__isset
operator|.
name|tableName
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|int
name|getCreateTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|createTime
return|;
block|}
specifier|public
name|void
name|setCreateTime
parameter_list|(
name|int
name|createTime
parameter_list|)
block|{
name|this
operator|.
name|createTime
operator|=
name|createTime
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|createTime
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|unsetCreateTime
parameter_list|()
block|{
name|this
operator|.
name|__isset
operator|.
name|createTime
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|int
name|getLastAccessTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|lastAccessTime
return|;
block|}
specifier|public
name|void
name|setLastAccessTime
parameter_list|(
name|int
name|lastAccessTime
parameter_list|)
block|{
name|this
operator|.
name|lastAccessTime
operator|=
name|lastAccessTime
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|lastAccessTime
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|unsetLastAccessTime
parameter_list|()
block|{
name|this
operator|.
name|__isset
operator|.
name|lastAccessTime
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|StorageDescriptor
name|getSd
parameter_list|()
block|{
return|return
name|this
operator|.
name|sd
return|;
block|}
specifier|public
name|void
name|setSd
parameter_list|(
name|StorageDescriptor
name|sd
parameter_list|)
block|{
name|this
operator|.
name|sd
operator|=
name|sd
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|sd
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|unsetSd
parameter_list|()
block|{
name|this
operator|.
name|sd
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|sd
operator|=
literal|false
expr_stmt|;
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
name|this
operator|.
name|__isset
operator|.
name|parameters
operator|=
literal|true
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
name|this
operator|.
name|__isset
operator|.
name|parameters
operator|=
literal|true
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
name|this
operator|.
name|__isset
operator|.
name|parameters
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
name|Partition
condition|)
return|return
name|this
operator|.
name|equals
argument_list|(
operator|(
name|Partition
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
name|Partition
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
name|this_present_values
init|=
literal|true
operator|&&
operator|(
name|this
operator|.
name|values
operator|!=
literal|null
operator|)
decl_stmt|;
name|boolean
name|that_present_values
init|=
literal|true
operator|&&
operator|(
name|that
operator|.
name|values
operator|!=
literal|null
operator|)
decl_stmt|;
if|if
condition|(
name|this_present_values
operator|||
name|that_present_values
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_values
operator|&&
name|that_present_values
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
name|values
operator|.
name|equals
argument_list|(
name|that
operator|.
name|values
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
operator|(
name|this
operator|.
name|dbName
operator|!=
literal|null
operator|)
decl_stmt|;
name|boolean
name|that_present_dbName
init|=
literal|true
operator|&&
operator|(
name|that
operator|.
name|dbName
operator|!=
literal|null
operator|)
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
name|this_present_tableName
init|=
literal|true
operator|&&
operator|(
name|this
operator|.
name|tableName
operator|!=
literal|null
operator|)
decl_stmt|;
name|boolean
name|that_present_tableName
init|=
literal|true
operator|&&
operator|(
name|that
operator|.
name|tableName
operator|!=
literal|null
operator|)
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
name|this_present_createTime
init|=
literal|true
decl_stmt|;
name|boolean
name|that_present_createTime
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|this_present_createTime
operator|||
name|that_present_createTime
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_createTime
operator|&&
name|that_present_createTime
operator|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|this
operator|.
name|createTime
operator|!=
name|that
operator|.
name|createTime
condition|)
return|return
literal|false
return|;
block|}
name|boolean
name|this_present_lastAccessTime
init|=
literal|true
decl_stmt|;
name|boolean
name|that_present_lastAccessTime
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|this_present_lastAccessTime
operator|||
name|that_present_lastAccessTime
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_lastAccessTime
operator|&&
name|that_present_lastAccessTime
operator|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|this
operator|.
name|lastAccessTime
operator|!=
name|that
operator|.
name|lastAccessTime
condition|)
return|return
literal|false
return|;
block|}
name|boolean
name|this_present_sd
init|=
literal|true
operator|&&
operator|(
name|this
operator|.
name|sd
operator|!=
literal|null
operator|)
decl_stmt|;
name|boolean
name|that_present_sd
init|=
literal|true
operator|&&
operator|(
name|that
operator|.
name|sd
operator|!=
literal|null
operator|)
decl_stmt|;
if|if
condition|(
name|this_present_sd
operator|||
name|that_present_sd
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_sd
operator|&&
name|that_present_sd
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
name|sd
operator|.
name|equals
argument_list|(
name|that
operator|.
name|sd
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
operator|(
name|this
operator|.
name|parameters
operator|!=
literal|null
operator|)
decl_stmt|;
name|boolean
name|that_present_parameters
init|=
literal|true
operator|&&
operator|(
name|that
operator|.
name|parameters
operator|!=
literal|null
operator|)
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
name|LIST
condition|)
block|{
block|{
name|TList
name|_list35
init|=
name|iprot
operator|.
name|readListBegin
argument_list|()
decl_stmt|;
name|this
operator|.
name|values
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|_list35
operator|.
name|size
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|_i36
init|=
literal|0
init|;
name|_i36
operator|<
name|_list35
operator|.
name|size
condition|;
operator|++
name|_i36
control|)
block|{
name|String
name|_elem37
init|=
literal|null
decl_stmt|;
name|_elem37
operator|=
name|iprot
operator|.
name|readString
argument_list|()
expr_stmt|;
name|this
operator|.
name|values
operator|.
name|add
argument_list|(
name|_elem37
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
name|values
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
name|dbName
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
name|dbName
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
name|tableName
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
name|tableName
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
name|I32
condition|)
block|{
name|this
operator|.
name|createTime
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
name|createTime
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
literal|5
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
name|lastAccessTime
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
name|lastAccessTime
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
literal|6
case|:
if|if
condition|(
name|field
operator|.
name|type
operator|==
name|TType
operator|.
name|STRUCT
condition|)
block|{
name|this
operator|.
name|sd
operator|=
operator|new
name|StorageDescriptor
argument_list|()
expr_stmt|;
name|this
operator|.
name|sd
operator|.
name|read
argument_list|(
name|iprot
argument_list|)
expr_stmt|;
name|this
operator|.
name|__isset
operator|.
name|sd
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
literal|7
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
name|_map38
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
name|_map38
operator|.
name|size
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|_i39
init|=
literal|0
init|;
name|_i39
operator|<
name|_map38
operator|.
name|size
condition|;
operator|++
name|_i39
control|)
block|{
name|String
name|_key40
decl_stmt|;
name|String
name|_val41
decl_stmt|;
name|_key40
operator|=
name|iprot
operator|.
name|readString
argument_list|()
expr_stmt|;
name|_val41
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
name|_key40
argument_list|,
name|_val41
argument_list|)
expr_stmt|;
block|}
name|iprot
operator|.
name|readMapEnd
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|__isset
operator|.
name|parameters
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
literal|"Partition"
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
name|values
operator|!=
literal|null
condition|)
block|{
name|field
operator|.
name|name
operator|=
literal|"values"
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
literal|1
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
name|STRING
argument_list|,
name|this
operator|.
name|values
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|_iter42
range|:
name|this
operator|.
name|values
control|)
block|{
name|oprot
operator|.
name|writeString
argument_list|(
name|_iter42
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
name|dbName
operator|!=
literal|null
condition|)
block|{
name|field
operator|.
name|name
operator|=
literal|"dbName"
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
name|tableName
operator|!=
literal|null
condition|)
block|{
name|field
operator|.
name|name
operator|=
literal|"tableName"
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
name|tableName
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
block|}
name|field
operator|.
name|name
operator|=
literal|"createTime"
expr_stmt|;
name|field
operator|.
name|type
operator|=
name|TType
operator|.
name|I32
expr_stmt|;
name|field
operator|.
name|id
operator|=
literal|4
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
name|writeI32
argument_list|(
name|this
operator|.
name|createTime
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
name|field
operator|.
name|name
operator|=
literal|"lastAccessTime"
expr_stmt|;
name|field
operator|.
name|type
operator|=
name|TType
operator|.
name|I32
expr_stmt|;
name|field
operator|.
name|id
operator|=
literal|5
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
name|writeI32
argument_list|(
name|this
operator|.
name|lastAccessTime
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
name|sd
operator|!=
literal|null
condition|)
block|{
name|field
operator|.
name|name
operator|=
literal|"sd"
expr_stmt|;
name|field
operator|.
name|type
operator|=
name|TType
operator|.
name|STRUCT
expr_stmt|;
name|field
operator|.
name|id
operator|=
literal|6
expr_stmt|;
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|field
argument_list|)
expr_stmt|;
name|this
operator|.
name|sd
operator|.
name|write
argument_list|(
name|oprot
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
name|field
operator|.
name|name
operator|=
literal|"parameters"
expr_stmt|;
name|field
operator|.
name|type
operator|=
name|TType
operator|.
name|MAP
expr_stmt|;
name|field
operator|.
name|id
operator|=
literal|7
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
name|String
name|_iter43
range|:
name|this
operator|.
name|parameters
operator|.
name|keySet
argument_list|()
control|)
block|{
name|oprot
operator|.
name|writeString
argument_list|(
name|_iter43
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeString
argument_list|(
name|this
operator|.
name|parameters
operator|.
name|get
argument_list|(
name|_iter43
argument_list|)
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
literal|"Partition("
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"values:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|values
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|",dbName:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|dbName
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|",tableName:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|",createTime:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|createTime
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|",lastAccessTime:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|lastAccessTime
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|",sd:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|sd
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|",parameters:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|parameters
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

