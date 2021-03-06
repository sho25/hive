begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
operator|.
name|repeat
import|;
end_import

begin_import
import|import static
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
name|Warehouse
operator|.
name|DEFAULT_CATALOG_NAME
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|AccessibleObject
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Array
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Modifier
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|Map
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|ClassUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|builder
operator|.
name|EqualsBuilder
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
name|ColumnStatistics
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
name|MetaException
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
name|NoSuchObjectException
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
name|Partition
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

begin_class
specifier|public
class|class
name|VerifyingObjectStore
extends|extends
name|ObjectStore
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|VerifyingObjectStore
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|VerifyingObjectStore
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" is being used - test run"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Partition
argument_list|>
name|getPartitionsByFilter
parameter_list|(
name|String
name|catName
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|String
name|filter
parameter_list|,
name|short
name|maxParts
parameter_list|)
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
block|{
name|List
argument_list|<
name|Partition
argument_list|>
name|sqlResults
init|=
name|getPartitionsByFilterInternal
argument_list|(
name|catName
argument_list|,
name|dbName
argument_list|,
name|tblName
argument_list|,
name|filter
argument_list|,
name|maxParts
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|ormResults
init|=
name|getPartitionsByFilterInternal
argument_list|(
name|catName
argument_list|,
name|dbName
argument_list|,
name|tblName
argument_list|,
name|filter
argument_list|,
name|maxParts
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|verifyLists
argument_list|(
name|sqlResults
argument_list|,
name|ormResults
argument_list|,
name|Partition
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
name|sqlResults
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Partition
argument_list|>
name|getPartitionsByNames
parameter_list|(
name|String
name|catName
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partNames
parameter_list|)
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
block|{
name|List
argument_list|<
name|Partition
argument_list|>
name|sqlResults
init|=
name|getPartitionsByNamesInternal
argument_list|(
name|catName
argument_list|,
name|dbName
argument_list|,
name|tblName
argument_list|,
name|partNames
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|ormResults
init|=
name|getPartitionsByNamesInternal
argument_list|(
name|catName
argument_list|,
name|dbName
argument_list|,
name|tblName
argument_list|,
name|partNames
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|verifyLists
argument_list|(
name|sqlResults
argument_list|,
name|ormResults
argument_list|,
name|Partition
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
name|sqlResults
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|getPartitionsByExpr
parameter_list|(
name|String
name|catName
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|byte
index|[]
name|expr
parameter_list|,
name|String
name|defaultPartitionName
parameter_list|,
name|short
name|maxParts
parameter_list|,
name|List
argument_list|<
name|Partition
argument_list|>
name|result
parameter_list|)
throws|throws
name|TException
block|{
name|List
argument_list|<
name|Partition
argument_list|>
name|ormParts
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
name|boolean
name|sqlResult
init|=
name|getPartitionsByExprInternal
argument_list|(
name|catName
argument_list|,
name|dbName
argument_list|,
name|tblName
argument_list|,
name|expr
argument_list|,
name|defaultPartitionName
argument_list|,
name|maxParts
argument_list|,
name|result
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|boolean
name|ormResult
init|=
name|getPartitionsByExprInternal
argument_list|(
name|catName
argument_list|,
name|dbName
argument_list|,
name|tblName
argument_list|,
name|expr
argument_list|,
name|defaultPartitionName
argument_list|,
name|maxParts
argument_list|,
name|ormParts
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|sqlResult
operator|!=
name|ormResult
condition|)
block|{
name|String
name|msg
init|=
literal|"The unknown flag is different - SQL "
operator|+
name|sqlResult
operator|+
literal|", ORM "
operator|+
name|ormResult
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|MetaException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
name|verifyLists
argument_list|(
name|result
argument_list|,
name|ormParts
argument_list|,
name|Partition
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
name|sqlResult
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Partition
argument_list|>
name|getPartitions
parameter_list|(
name|String
name|catName
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|int
name|maxParts
parameter_list|)
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
block|{
name|List
argument_list|<
name|Partition
argument_list|>
name|sqlResults
init|=
name|getPartitionsInternal
argument_list|(
name|catName
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|,
name|maxParts
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|ormResults
init|=
name|getPartitionsInternal
argument_list|(
name|catName
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|,
name|maxParts
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|verifyLists
argument_list|(
name|sqlResults
argument_list|,
name|ormResults
argument_list|,
name|Partition
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
name|sqlResults
return|;
block|}
annotation|@
name|Override
specifier|public
name|ColumnStatistics
name|getTableColumnStatistics
parameter_list|(
name|String
name|catName
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|colNames
parameter_list|,
name|String
name|engine
parameter_list|)
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
block|{
name|ColumnStatistics
name|sqlResult
init|=
name|getTableColumnStatisticsInternal
argument_list|(
name|catName
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|,
name|colNames
argument_list|,
name|engine
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|ColumnStatistics
name|jdoResult
init|=
name|getTableColumnStatisticsInternal
argument_list|(
name|catName
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|,
name|colNames
argument_list|,
name|engine
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|verifyObjects
argument_list|(
name|sqlResult
argument_list|,
name|jdoResult
argument_list|,
name|ColumnStatistics
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
name|sqlResult
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|ColumnStatistics
argument_list|>
name|getPartitionColumnStatistics
parameter_list|(
name|String
name|catName
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partNames
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|colNames
parameter_list|,
name|String
name|engine
parameter_list|)
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
block|{
name|List
argument_list|<
name|ColumnStatistics
argument_list|>
name|sqlResult
init|=
name|getPartitionColumnStatisticsInternal
argument_list|(
name|catName
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partNames
argument_list|,
name|colNames
argument_list|,
name|engine
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ColumnStatistics
argument_list|>
name|jdoResult
init|=
name|getPartitionColumnStatisticsInternal
argument_list|(
name|catName
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partNames
argument_list|,
name|colNames
argument_list|,
name|engine
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|verifyLists
argument_list|(
name|sqlResult
argument_list|,
name|jdoResult
argument_list|,
name|ColumnStatistics
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
name|sqlResult
return|;
block|}
specifier|private
name|void
name|verifyObjects
parameter_list|(
name|Object
name|sqlResult
parameter_list|,
name|Object
name|jdoResult
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|EqualsBuilder
operator|.
name|reflectionEquals
argument_list|(
name|sqlResult
argument_list|,
name|jdoResult
argument_list|)
condition|)
return|return;
name|StringBuilder
name|errorStr
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"Objects are different: \n"
argument_list|)
decl_stmt|;
try|try
block|{
name|dumpObject
argument_list|(
name|errorStr
argument_list|,
literal|"SQL"
argument_list|,
name|sqlResult
argument_list|,
name|clazz
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|errorStr
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
name|dumpObject
argument_list|(
name|errorStr
argument_list|,
literal|"ORM"
argument_list|,
name|jdoResult
argument_list|,
name|clazz
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|errorStr
operator|.
name|append
argument_list|(
literal|"Error getting the diff: "
operator|+
name|t
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|error
argument_list|(
literal|"Different results: \n"
operator|+
name|errorStr
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Different results from SQL and ORM, see log for details"
argument_list|)
throw|;
block|}
specifier|private
parameter_list|<
name|T
parameter_list|>
name|void
name|verifyLists
parameter_list|(
name|Collection
argument_list|<
name|T
argument_list|>
name|sqlResults
parameter_list|,
name|Collection
argument_list|<
name|T
argument_list|>
name|ormResults
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|)
throws|throws
name|MetaException
block|{
specifier|final
name|int
name|MAX_DIFFS
init|=
literal|5
decl_stmt|;
if|if
condition|(
name|sqlResults
operator|.
name|size
argument_list|()
operator|!=
name|ormResults
operator|.
name|size
argument_list|()
condition|)
block|{
name|String
name|msg
init|=
literal|"Lists are not the same size: SQL "
operator|+
name|sqlResults
operator|.
name|size
argument_list|()
operator|+
literal|", ORM "
operator|+
name|ormResults
operator|.
name|size
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|MetaException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
name|Iterator
argument_list|<
name|T
argument_list|>
name|sqlIter
init|=
name|sqlResults
operator|.
name|iterator
argument_list|()
decl_stmt|,
name|ormIter
init|=
name|ormResults
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|StringBuilder
name|errorStr
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|int
name|errors
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|partIx
init|=
literal|0
init|;
name|partIx
operator|<
name|sqlResults
operator|.
name|size
argument_list|()
condition|;
operator|++
name|partIx
control|)
block|{
assert|assert
name|sqlIter
operator|.
name|hasNext
argument_list|()
operator|&&
name|ormIter
operator|.
name|hasNext
argument_list|()
assert|;
name|T
name|p1
init|=
name|sqlIter
operator|.
name|next
argument_list|()
decl_stmt|,
name|p2
init|=
name|ormIter
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|EqualsBuilder
operator|.
name|reflectionEquals
argument_list|(
name|p1
argument_list|,
name|p2
argument_list|)
condition|)
continue|continue;
name|errorStr
operator|.
name|append
argument_list|(
literal|"Results are different at list index "
operator|+
name|partIx
operator|+
literal|": \n"
argument_list|)
expr_stmt|;
try|try
block|{
name|dumpObject
argument_list|(
name|errorStr
argument_list|,
literal|"SQL"
argument_list|,
name|p1
argument_list|,
name|clazz
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|errorStr
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
name|dumpObject
argument_list|(
name|errorStr
argument_list|,
literal|"ORM"
argument_list|,
name|p2
argument_list|,
name|clazz
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|errorStr
operator|.
name|append
argument_list|(
literal|"\n\n"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"Error getting the diff at list index "
operator|+
name|partIx
decl_stmt|;
name|errorStr
operator|.
name|append
argument_list|(
literal|"\n\n"
operator|+
name|msg
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|t
argument_list|)
expr_stmt|;
break|break;
block|}
if|if
condition|(
operator|++
name|errors
operator|==
name|MAX_DIFFS
condition|)
block|{
name|errorStr
operator|.
name|append
argument_list|(
literal|"\n\nToo many diffs, giving up (lists might be sorted differently)"
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|errorStr
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Different results: \n"
operator|+
name|errorStr
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Different results from SQL and ORM, see log for details"
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|void
name|dumpObject
parameter_list|(
name|StringBuilder
name|errorStr
parameter_list|,
name|String
name|name
parameter_list|,
name|Object
name|p
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|c
parameter_list|,
name|int
name|level
parameter_list|)
throws|throws
name|IllegalAccessException
block|{
name|String
name|offsetStr
init|=
name|repeat
argument_list|(
literal|"  "
argument_list|,
name|level
argument_list|)
decl_stmt|;
if|if
condition|(
name|p
operator|==
literal|null
operator|||
name|c
operator|==
name|String
operator|.
name|class
operator|||
name|c
operator|.
name|isPrimitive
argument_list|()
operator|||
name|ClassUtils
operator|.
name|wrapperToPrimitive
argument_list|(
name|c
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|errorStr
operator|.
name|append
argument_list|(
name|offsetStr
argument_list|)
operator|.
name|append
argument_list|(
name|name
operator|+
literal|": ["
operator|+
name|p
operator|+
literal|"]\n"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ClassUtils
operator|.
name|isAssignable
argument_list|(
name|c
argument_list|,
name|Iterable
operator|.
name|class
argument_list|)
condition|)
block|{
name|errorStr
operator|.
name|append
argument_list|(
name|offsetStr
argument_list|)
operator|.
name|append
argument_list|(
name|name
operator|+
literal|" is an iterable\n"
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|?
argument_list|>
name|i1
init|=
operator|(
operator|(
name|Iterable
argument_list|<
name|?
argument_list|>
operator|)
name|p
operator|)
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|i1
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Object
name|o1
init|=
name|i1
operator|.
name|next
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|t
init|=
name|o1
operator|==
literal|null
condition|?
name|Object
operator|.
name|class
else|:
name|o1
operator|.
name|getClass
argument_list|()
decl_stmt|;
comment|// ...
name|dumpObject
argument_list|(
name|errorStr
argument_list|,
name|name
operator|+
literal|"["
operator|+
operator|(
name|i
operator|++
operator|)
operator|+
literal|"]"
argument_list|,
name|o1
argument_list|,
name|t
argument_list|,
name|level
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|c
operator|.
name|isArray
argument_list|()
condition|)
block|{
name|int
name|len
init|=
name|Array
operator|.
name|getLength
argument_list|(
name|p
argument_list|)
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|t
init|=
name|c
operator|.
name|getComponentType
argument_list|()
decl_stmt|;
name|errorStr
operator|.
name|append
argument_list|(
name|offsetStr
argument_list|)
operator|.
name|append
argument_list|(
name|name
operator|+
literal|" is an array\n"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|len
condition|;
operator|++
name|i
control|)
block|{
name|dumpObject
argument_list|(
name|errorStr
argument_list|,
name|name
operator|+
literal|"["
operator|+
name|i
operator|+
literal|"]"
argument_list|,
name|Array
operator|.
name|get
argument_list|(
name|p
argument_list|,
name|i
argument_list|)
argument_list|,
name|t
argument_list|,
name|level
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|ClassUtils
operator|.
name|isAssignable
argument_list|(
name|c
argument_list|,
name|Map
operator|.
name|class
argument_list|)
condition|)
block|{
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|c1
init|=
operator|(
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|p
decl_stmt|;
name|errorStr
operator|.
name|append
argument_list|(
name|offsetStr
argument_list|)
operator|.
name|append
argument_list|(
name|name
operator|+
literal|" is a map\n"
argument_list|)
expr_stmt|;
name|dumpObject
argument_list|(
name|errorStr
argument_list|,
name|name
operator|+
literal|".keys"
argument_list|,
name|c1
operator|.
name|keySet
argument_list|()
argument_list|,
name|Set
operator|.
name|class
argument_list|,
name|level
operator|+
literal|1
argument_list|)
expr_stmt|;
name|dumpObject
argument_list|(
name|errorStr
argument_list|,
name|name
operator|+
literal|".vals"
argument_list|,
name|c1
operator|.
name|values
argument_list|()
argument_list|,
name|Collection
operator|.
name|class
argument_list|,
name|level
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|errorStr
operator|.
name|append
argument_list|(
name|offsetStr
argument_list|)
operator|.
name|append
argument_list|(
name|name
operator|+
literal|" is of type "
operator|+
name|c
operator|.
name|getCanonicalName
argument_list|()
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
comment|// TODO: this doesn't include superclass.
name|Field
index|[]
name|fields
init|=
name|c
operator|.
name|getDeclaredFields
argument_list|()
decl_stmt|;
name|AccessibleObject
operator|.
name|setAccessible
argument_list|(
name|fields
argument_list|,
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fields
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Field
name|f
init|=
name|fields
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|f
operator|.
name|getName
argument_list|()
operator|.
name|indexOf
argument_list|(
literal|'$'
argument_list|)
operator|!=
operator|-
literal|1
operator|||
name|Modifier
operator|.
name|isStatic
argument_list|(
name|f
operator|.
name|getModifiers
argument_list|()
argument_list|)
condition|)
continue|continue;
name|dumpObject
argument_list|(
name|errorStr
argument_list|,
name|name
operator|+
literal|"."
operator|+
name|f
operator|.
name|getName
argument_list|()
argument_list|,
name|f
operator|.
name|get
argument_list|(
name|p
argument_list|)
argument_list|,
name|f
operator|.
name|getType
argument_list|()
argument_list|,
name|level
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

