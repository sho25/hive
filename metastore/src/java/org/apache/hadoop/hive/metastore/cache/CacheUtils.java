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
name|metastore
operator|.
name|cache
package|;
end_package

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
name|HashMap
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
name|regex
operator|.
name|Pattern
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
name|Order
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
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|SkewedInfo
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
name|StorageDescriptor
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
name|metastore
operator|.
name|cache
operator|.
name|CachedStore
operator|.
name|PartitionWrapper
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
name|cache
operator|.
name|CachedStore
operator|.
name|TableWrapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|HiveStringUtils
import|;
end_import

begin_class
specifier|public
class|class
name|CacheUtils
block|{
specifier|private
specifier|static
specifier|final
name|String
name|delimit
init|=
literal|"\u0001"
decl_stmt|;
specifier|public
specifier|static
name|String
name|buildKey
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
return|return
name|dbName
operator|+
name|delimit
operator|+
name|tableName
return|;
block|}
specifier|public
specifier|static
name|String
name|buildKey
parameter_list|(
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
name|partVals
parameter_list|)
block|{
name|String
name|key
init|=
name|buildKey
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|partVals
operator|==
literal|null
operator|||
name|partVals
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
name|key
return|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|partVals
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|key
operator|+=
name|partVals
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|!=
name|partVals
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|)
block|{
name|key
operator|+=
name|delimit
expr_stmt|;
block|}
block|}
return|return
name|key
return|;
block|}
specifier|public
specifier|static
name|String
name|buildKey
parameter_list|(
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
name|partVals
parameter_list|,
name|String
name|colName
parameter_list|)
block|{
name|String
name|key
init|=
name|buildKey
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partVals
argument_list|)
decl_stmt|;
return|return
name|key
operator|+
name|delimit
operator|+
name|colName
return|;
block|}
specifier|public
specifier|static
name|Table
name|assemble
parameter_list|(
name|TableWrapper
name|wrapper
parameter_list|)
block|{
name|Table
name|t
init|=
name|wrapper
operator|.
name|getTable
argument_list|()
operator|.
name|deepCopy
argument_list|()
decl_stmt|;
if|if
condition|(
name|wrapper
operator|.
name|getSdHash
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|StorageDescriptor
name|sdCopy
init|=
name|SharedCache
operator|.
name|getSdFromCache
argument_list|(
name|wrapper
operator|.
name|getSdHash
argument_list|()
argument_list|)
operator|.
name|deepCopy
argument_list|()
decl_stmt|;
if|if
condition|(
name|sdCopy
operator|.
name|getBucketCols
argument_list|()
operator|==
literal|null
condition|)
block|{
name|sdCopy
operator|.
name|setBucketCols
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sdCopy
operator|.
name|getSortCols
argument_list|()
operator|==
literal|null
condition|)
block|{
name|sdCopy
operator|.
name|setSortCols
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Order
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sdCopy
operator|.
name|getSkewedInfo
argument_list|()
operator|==
literal|null
condition|)
block|{
name|sdCopy
operator|.
name|setSkewedInfo
argument_list|(
operator|new
name|SkewedInfo
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|sdCopy
operator|.
name|setLocation
argument_list|(
name|wrapper
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
name|sdCopy
operator|.
name|setParameters
argument_list|(
name|wrapper
operator|.
name|getParameters
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|setSd
argument_list|(
name|sdCopy
argument_list|)
expr_stmt|;
block|}
return|return
name|t
return|;
block|}
specifier|public
specifier|static
name|Partition
name|assemble
parameter_list|(
name|PartitionWrapper
name|wrapper
parameter_list|)
block|{
name|Partition
name|p
init|=
name|wrapper
operator|.
name|getPartition
argument_list|()
operator|.
name|deepCopy
argument_list|()
decl_stmt|;
if|if
condition|(
name|wrapper
operator|.
name|getSdHash
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|StorageDescriptor
name|sdCopy
init|=
name|SharedCache
operator|.
name|getSdFromCache
argument_list|(
name|wrapper
operator|.
name|getSdHash
argument_list|()
argument_list|)
operator|.
name|deepCopy
argument_list|()
decl_stmt|;
if|if
condition|(
name|sdCopy
operator|.
name|getBucketCols
argument_list|()
operator|==
literal|null
condition|)
block|{
name|sdCopy
operator|.
name|setBucketCols
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sdCopy
operator|.
name|getSortCols
argument_list|()
operator|==
literal|null
condition|)
block|{
name|sdCopy
operator|.
name|setSortCols
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Order
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sdCopy
operator|.
name|getSkewedInfo
argument_list|()
operator|==
literal|null
condition|)
block|{
name|sdCopy
operator|.
name|setSkewedInfo
argument_list|(
operator|new
name|SkewedInfo
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|sdCopy
operator|.
name|setLocation
argument_list|(
name|wrapper
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
name|sdCopy
operator|.
name|setParameters
argument_list|(
name|wrapper
operator|.
name|getParameters
argument_list|()
argument_list|)
expr_stmt|;
name|p
operator|.
name|setSd
argument_list|(
name|sdCopy
argument_list|)
expr_stmt|;
block|}
return|return
name|p
return|;
block|}
specifier|public
specifier|static
name|boolean
name|matches
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|pattern
parameter_list|)
block|{
name|String
index|[]
name|subpatterns
init|=
name|pattern
operator|.
name|trim
argument_list|()
operator|.
name|split
argument_list|(
literal|"\\|"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|subpattern
range|:
name|subpatterns
control|)
block|{
name|subpattern
operator|=
literal|"(?i)"
operator|+
name|subpattern
operator|.
name|replaceAll
argument_list|(
literal|"\\?"
argument_list|,
literal|".{1}"
argument_list|)
operator|.
name|replaceAll
argument_list|(
literal|"\\*"
argument_list|,
literal|".*"
argument_list|)
operator|.
name|replaceAll
argument_list|(
literal|"\\^"
argument_list|,
literal|"\\\\^"
argument_list|)
operator|.
name|replaceAll
argument_list|(
literal|"\\$"
argument_list|,
literal|"\\\\$"
argument_list|)
expr_stmt|;
if|if
condition|(
name|Pattern
operator|.
name|matches
argument_list|(
name|subpattern
argument_list|,
name|HiveStringUtils
operator|.
name|normalizeIdentifier
argument_list|(
name|name
argument_list|)
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

