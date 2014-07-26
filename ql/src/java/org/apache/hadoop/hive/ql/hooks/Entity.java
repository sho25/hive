begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ql
operator|.
name|hooks
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
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
operator|.
name|Database
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|metadata
operator|.
name|DummyPartition
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|metadata
operator|.
name|Partition
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|metadata
operator|.
name|Table
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|parse
operator|.
name|SemanticAnalyzer
import|;
end_import

begin_comment
comment|/**  * This class encapsulates an object that is being read or written to by the  * query. This object may be a table, partition, dfs directory or a local  * directory.  */
end_comment

begin_class
specifier|public
class|class
name|Entity
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|/**    * The type of the entity.    */
specifier|public
specifier|static
enum|enum
name|Type
block|{
name|DATABASE
block|,
name|TABLE
block|,
name|PARTITION
block|,
name|DUMMYPARTITION
block|,
name|DFS_DIR
block|,
name|LOCAL_DIR
block|,
name|FUNCTION
block|}
comment|/**    * The database if this is a database.    */
specifier|private
name|Database
name|database
decl_stmt|;
comment|/**    * The type.    */
specifier|private
name|Type
name|typ
decl_stmt|;
comment|/**    * The table. This is null if this is a directory.    */
specifier|private
name|Table
name|t
decl_stmt|;
comment|/**    * The partition.This is null if this object is not a partition.    */
specifier|private
name|Partition
name|p
decl_stmt|;
comment|/**    * The directory if this is a directory    */
specifier|private
name|String
name|d
decl_stmt|;
comment|/**    * An object that is represented as a String    * Currently used for functions    */
specifier|private
name|String
name|stringObject
decl_stmt|;
comment|/**    * This is derived from t and p, but we need to serialize this field to make    * sure Entity.hashCode() does not need to recursively read into t and p.    */
specifier|private
name|String
name|name
decl_stmt|;
comment|/**    * Whether the output is complete or not. For eg, for dynamic partitions, the    * complete output may not be known    */
specifier|private
name|boolean
name|complete
decl_stmt|;
specifier|public
name|boolean
name|isComplete
parameter_list|()
block|{
return|return
name|complete
return|;
block|}
specifier|public
name|void
name|setComplete
parameter_list|(
name|boolean
name|complete
parameter_list|)
block|{
name|this
operator|.
name|complete
operator|=
name|complete
expr_stmt|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
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
name|Database
name|getDatabase
parameter_list|()
block|{
return|return
name|database
return|;
block|}
specifier|public
name|void
name|setDatabase
parameter_list|(
name|Database
name|database
parameter_list|)
block|{
name|this
operator|.
name|database
operator|=
name|database
expr_stmt|;
block|}
specifier|public
name|Type
name|getTyp
parameter_list|()
block|{
return|return
name|typ
return|;
block|}
specifier|public
name|void
name|setTyp
parameter_list|(
name|Type
name|typ
parameter_list|)
block|{
name|this
operator|.
name|typ
operator|=
name|typ
expr_stmt|;
block|}
specifier|public
name|Table
name|getT
parameter_list|()
block|{
return|return
name|t
return|;
block|}
specifier|public
name|void
name|setT
parameter_list|(
name|Table
name|t
parameter_list|)
block|{
name|this
operator|.
name|t
operator|=
name|t
expr_stmt|;
block|}
specifier|public
name|Partition
name|getP
parameter_list|()
block|{
return|return
name|p
return|;
block|}
specifier|public
name|void
name|setP
parameter_list|(
name|Partition
name|p
parameter_list|)
block|{
name|this
operator|.
name|p
operator|=
name|p
expr_stmt|;
block|}
specifier|public
name|String
name|getD
parameter_list|()
block|{
return|return
name|d
return|;
block|}
specifier|public
name|void
name|setD
parameter_list|(
name|String
name|d
parameter_list|)
block|{
name|this
operator|.
name|d
operator|=
name|d
expr_stmt|;
block|}
specifier|public
name|String
name|getFunctionName
parameter_list|()
block|{
if|if
condition|(
name|typ
operator|==
name|Type
operator|.
name|FUNCTION
condition|)
block|{
return|return
name|stringObject
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|void
name|setFunctionName
parameter_list|(
name|String
name|funcName
parameter_list|)
block|{
if|if
condition|(
name|typ
operator|!=
name|Type
operator|.
name|FUNCTION
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Set function can't be called on entity if the entity type is not "
operator|+
name|Type
operator|.
name|FUNCTION
argument_list|)
throw|;
block|}
name|this
operator|.
name|stringObject
operator|=
name|funcName
expr_stmt|;
block|}
comment|/**    * Only used by serialization.    */
specifier|public
name|Entity
parameter_list|()
block|{   }
comment|/**    * Constructor for a database.    *    * @param database    *          Database that is read or written to.    * @param complete    *          Means the database is target, not for table or partition, etc.    */
specifier|public
name|Entity
parameter_list|(
name|Database
name|database
parameter_list|,
name|boolean
name|complete
parameter_list|)
block|{
name|this
operator|.
name|database
operator|=
name|database
expr_stmt|;
name|this
operator|.
name|typ
operator|=
name|Type
operator|.
name|DATABASE
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|computeName
argument_list|()
expr_stmt|;
name|this
operator|.
name|complete
operator|=
name|complete
expr_stmt|;
block|}
comment|/**    * Constructor for a table.    *    * @param t    *          Table that is read or written to.    */
specifier|public
name|Entity
parameter_list|(
name|Table
name|t
parameter_list|,
name|boolean
name|complete
parameter_list|)
block|{
name|d
operator|=
literal|null
expr_stmt|;
name|p
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|t
operator|=
name|t
expr_stmt|;
name|typ
operator|=
name|Type
operator|.
name|TABLE
expr_stmt|;
name|name
operator|=
name|computeName
argument_list|()
expr_stmt|;
name|this
operator|.
name|complete
operator|=
name|complete
expr_stmt|;
block|}
comment|/**    * Constructor for a partition.    *    * @param p    *          Partition that is read or written to.    */
specifier|public
name|Entity
parameter_list|(
name|Partition
name|p
parameter_list|,
name|boolean
name|complete
parameter_list|)
block|{
name|d
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|p
operator|=
name|p
expr_stmt|;
name|t
operator|=
name|p
operator|.
name|getTable
argument_list|()
expr_stmt|;
name|typ
operator|=
name|Type
operator|.
name|PARTITION
expr_stmt|;
name|name
operator|=
name|computeName
argument_list|()
expr_stmt|;
name|this
operator|.
name|complete
operator|=
name|complete
expr_stmt|;
block|}
specifier|public
name|Entity
parameter_list|(
name|DummyPartition
name|p
parameter_list|,
name|boolean
name|complete
parameter_list|)
block|{
name|d
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|p
operator|=
name|p
expr_stmt|;
name|t
operator|=
name|p
operator|.
name|getTable
argument_list|()
expr_stmt|;
name|typ
operator|=
name|Type
operator|.
name|DUMMYPARTITION
expr_stmt|;
name|name
operator|=
name|computeName
argument_list|()
expr_stmt|;
name|this
operator|.
name|complete
operator|=
name|complete
expr_stmt|;
block|}
specifier|public
name|Entity
parameter_list|(
name|String
name|d
parameter_list|,
name|boolean
name|islocal
parameter_list|,
name|boolean
name|complete
parameter_list|)
block|{
name|this
operator|.
name|d
operator|=
name|d
expr_stmt|;
name|p
operator|=
literal|null
expr_stmt|;
name|t
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|islocal
condition|)
block|{
name|typ
operator|=
name|Type
operator|.
name|LOCAL_DIR
expr_stmt|;
block|}
else|else
block|{
name|typ
operator|=
name|Type
operator|.
name|DFS_DIR
expr_stmt|;
block|}
name|name
operator|=
name|computeName
argument_list|()
expr_stmt|;
name|this
operator|.
name|complete
operator|=
name|complete
expr_stmt|;
block|}
comment|/**    * Create an entity representing a object with given name, database namespace and type    * @param database - database namespace    * @param strObj - object name as string    * @param type - the entity type. this constructor only supports FUNCTION type currently    */
specifier|public
name|Entity
parameter_list|(
name|Database
name|database
parameter_list|,
name|String
name|strObj
parameter_list|,
name|Type
name|type
parameter_list|)
block|{
if|if
condition|(
name|type
operator|!=
name|Type
operator|.
name|FUNCTION
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"This constructor is supported only for type:"
operator|+
name|Type
operator|.
name|FUNCTION
argument_list|)
throw|;
block|}
name|this
operator|.
name|database
operator|=
name|database
expr_stmt|;
name|this
operator|.
name|stringObject
operator|=
name|strObj
expr_stmt|;
name|this
operator|.
name|typ
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|complete
operator|=
literal|true
expr_stmt|;
name|name
operator|=
name|computeName
argument_list|()
expr_stmt|;
block|}
comment|/**    * Get the parameter map of the Entity.    */
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
if|if
condition|(
name|p
operator|!=
literal|null
condition|)
block|{
return|return
name|p
operator|.
name|getParameters
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|t
operator|.
name|getParameters
argument_list|()
return|;
block|}
block|}
comment|/**    * Get the type of the entity.    */
specifier|public
name|Type
name|getType
parameter_list|()
block|{
return|return
name|typ
return|;
block|}
comment|/**    * Get the location of the entity.    */
specifier|public
name|URI
name|getLocation
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|typ
operator|==
name|Type
operator|.
name|DATABASE
condition|)
block|{
name|String
name|location
init|=
name|database
operator|.
name|getLocationUri
argument_list|()
decl_stmt|;
return|return
name|location
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|URI
argument_list|(
name|location
argument_list|)
return|;
block|}
if|if
condition|(
name|typ
operator|==
name|Type
operator|.
name|TABLE
condition|)
block|{
return|return
name|t
operator|.
name|getDataLocation
argument_list|()
operator|.
name|toUri
argument_list|()
return|;
block|}
if|if
condition|(
name|typ
operator|==
name|Type
operator|.
name|PARTITION
condition|)
block|{
return|return
name|p
operator|.
name|getDataLocation
argument_list|()
operator|.
name|toUri
argument_list|()
return|;
block|}
if|if
condition|(
name|typ
operator|==
name|Type
operator|.
name|DFS_DIR
operator|||
name|typ
operator|==
name|Type
operator|.
name|LOCAL_DIR
condition|)
block|{
return|return
operator|new
name|URI
argument_list|(
name|d
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Get the partition associated with the entity.    */
specifier|public
name|Partition
name|getPartition
parameter_list|()
block|{
return|return
name|p
return|;
block|}
comment|/**    * Get the table associated with the entity.    */
specifier|public
name|Table
name|getTable
parameter_list|()
block|{
return|return
name|t
return|;
block|}
specifier|public
name|boolean
name|isDummy
parameter_list|()
block|{
if|if
condition|(
name|typ
operator|==
name|Type
operator|.
name|DATABASE
condition|)
block|{
return|return
name|database
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|SemanticAnalyzer
operator|.
name|DUMMY_DATABASE
argument_list|)
return|;
block|}
if|if
condition|(
name|typ
operator|==
name|Type
operator|.
name|TABLE
condition|)
block|{
return|return
name|t
operator|.
name|isDummyTable
argument_list|()
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * toString function.    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|name
return|;
block|}
specifier|private
name|String
name|computeName
parameter_list|()
block|{
switch|switch
condition|(
name|typ
condition|)
block|{
case|case
name|DATABASE
case|:
return|return
literal|"database:"
operator|+
name|database
operator|.
name|getName
argument_list|()
return|;
case|case
name|TABLE
case|:
return|return
name|t
operator|.
name|getDbName
argument_list|()
operator|+
literal|"@"
operator|+
name|t
operator|.
name|getTableName
argument_list|()
return|;
case|case
name|PARTITION
case|:
return|return
name|t
operator|.
name|getDbName
argument_list|()
operator|+
literal|"@"
operator|+
name|t
operator|.
name|getTableName
argument_list|()
operator|+
literal|"@"
operator|+
name|p
operator|.
name|getName
argument_list|()
return|;
case|case
name|DUMMYPARTITION
case|:
return|return
name|p
operator|.
name|getName
argument_list|()
return|;
case|case
name|FUNCTION
case|:
return|return
name|stringObject
return|;
default|default:
return|return
name|d
return|;
block|}
block|}
comment|/**    * Equals function.    */
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|o
operator|instanceof
name|Entity
condition|)
block|{
name|Entity
name|ore
init|=
operator|(
name|Entity
operator|)
name|o
decl_stmt|;
return|return
operator|(
name|toString
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|ore
operator|.
name|toString
argument_list|()
argument_list|)
operator|)
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
comment|/**    * Hashcode function.    */
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|toString
argument_list|()
operator|.
name|hashCode
argument_list|()
return|;
block|}
block|}
end_class

end_unit

