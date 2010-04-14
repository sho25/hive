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
name|plan
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
name|Iterator
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
name|exec
operator|.
name|ColumnInfo
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

begin_class
specifier|public
class|class
name|DynamicPartitionCtx
implements|implements
name|Serializable
block|{
comment|/**    * default serialization ID    */
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
decl_stmt|;
comment|// partSpec is an ORDERED hash map
specifier|private
name|int
name|numDPCols
decl_stmt|;
comment|// number of dynamic partition columns
specifier|private
name|int
name|numSPCols
decl_stmt|;
comment|// number of static partition columns
specifier|private
name|String
name|spPath
decl_stmt|;
comment|// path name corresponding to SP columns
specifier|private
name|String
name|rootPath
decl_stmt|;
comment|// the root path DP columns paths start from
specifier|private
name|int
name|numBuckets
decl_stmt|;
comment|// number of buckets in each partition
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|inputToDPCols
decl_stmt|;
comment|// mapping from input column names to DP columns
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|spNames
decl_stmt|;
comment|// sp column names
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|dpNames
decl_stmt|;
comment|// dp column names
specifier|private
name|String
name|defaultPartName
decl_stmt|;
comment|// default partition name in case of null or empty value
specifier|private
name|int
name|maxPartsPerNode
decl_stmt|;
comment|// maximum dynamic partitions created per mapper/reducer
specifier|public
name|DynamicPartitionCtx
parameter_list|()
block|{   }
specifier|public
name|DynamicPartitionCtx
parameter_list|(
name|Table
name|tbl
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|,
name|String
name|defaultPartName
parameter_list|,
name|int
name|maxParts
parameter_list|)
block|{
name|this
operator|.
name|partSpec
operator|=
name|partSpec
expr_stmt|;
name|this
operator|.
name|spNames
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|dpNames
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|numBuckets
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|maxPartsPerNode
operator|=
name|maxParts
expr_stmt|;
name|this
operator|.
name|defaultPartName
operator|=
name|defaultPartName
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
name|me
range|:
name|partSpec
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|me
operator|.
name|getValue
argument_list|()
operator|==
literal|null
condition|)
block|{
name|dpNames
operator|.
name|add
argument_list|(
name|me
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|spNames
operator|.
name|add
argument_list|(
name|me
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|numDPCols
operator|=
name|dpNames
operator|.
name|size
argument_list|()
expr_stmt|;
name|this
operator|.
name|numSPCols
operator|=
name|spNames
operator|.
name|size
argument_list|()
expr_stmt|;
name|this
operator|.
name|inputToDPCols
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
if|if
condition|(
name|this
operator|.
name|numSPCols
operator|>
literal|0
condition|)
block|{
name|this
operator|.
name|spPath
operator|=
name|Warehouse
operator|.
name|makeDynamicPartName
argument_list|(
name|partSpec
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|spPath
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|mapInputToDP
parameter_list|(
name|List
argument_list|<
name|ColumnInfo
argument_list|>
name|fs
parameter_list|)
block|{
assert|assert
name|fs
operator|.
name|size
argument_list|()
operator|==
name|this
operator|.
name|numDPCols
operator|:
literal|"input DP column size != numDPCols"
assert|;
name|Iterator
argument_list|<
name|ColumnInfo
argument_list|>
name|itr1
init|=
name|fs
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|String
argument_list|>
name|itr2
init|=
name|dpNames
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|itr1
operator|.
name|hasNext
argument_list|()
operator|&&
name|itr2
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|inputToDPCols
operator|.
name|put
argument_list|(
name|itr1
operator|.
name|next
argument_list|()
operator|.
name|getInternalName
argument_list|()
argument_list|,
name|itr2
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|int
name|getMaxPartitionsPerNode
parameter_list|()
block|{
return|return
name|this
operator|.
name|maxPartsPerNode
return|;
block|}
specifier|public
name|void
name|setMaxPartitionsPerNode
parameter_list|(
name|int
name|maxParts
parameter_list|)
block|{
name|this
operator|.
name|maxPartsPerNode
operator|=
name|maxParts
expr_stmt|;
block|}
specifier|public
name|String
name|getDefaultPartitionName
parameter_list|()
block|{
return|return
name|this
operator|.
name|defaultPartName
return|;
block|}
specifier|public
name|void
name|setDefaultPartitionName
parameter_list|(
name|String
name|pname
parameter_list|)
block|{
name|this
operator|.
name|defaultPartName
operator|=
name|pname
expr_stmt|;
block|}
specifier|public
name|void
name|setNumBuckets
parameter_list|(
name|int
name|bk
parameter_list|)
block|{
name|this
operator|.
name|numBuckets
operator|=
name|bk
expr_stmt|;
block|}
specifier|public
name|int
name|getNumBuckets
parameter_list|()
block|{
return|return
name|this
operator|.
name|numBuckets
return|;
block|}
specifier|public
name|void
name|setRootPath
parameter_list|(
name|String
name|root
parameter_list|)
block|{
name|this
operator|.
name|rootPath
operator|=
name|root
expr_stmt|;
block|}
specifier|public
name|String
name|getRootPath
parameter_list|()
block|{
return|return
name|this
operator|.
name|rootPath
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getDPColNames
parameter_list|()
block|{
return|return
name|this
operator|.
name|dpNames
return|;
block|}
specifier|public
name|void
name|setDPColNames
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|dp
parameter_list|)
block|{
name|this
operator|.
name|dpNames
operator|=
name|dp
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getSPColNames
parameter_list|()
block|{
return|return
name|this
operator|.
name|spNames
return|;
block|}
specifier|public
name|void
name|setPartSpec
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ps
parameter_list|)
block|{
name|this
operator|.
name|partSpec
operator|=
name|ps
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getPartSpec
parameter_list|()
block|{
return|return
name|this
operator|.
name|partSpec
return|;
block|}
specifier|public
name|void
name|setSPColNames
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|sp
parameter_list|)
block|{
name|this
operator|.
name|spNames
operator|=
name|sp
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getInputToDPCols
parameter_list|()
block|{
return|return
name|this
operator|.
name|inputToDPCols
return|;
block|}
specifier|public
name|void
name|setInputToDPCols
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
parameter_list|)
block|{
name|this
operator|.
name|inputToDPCols
operator|=
name|map
expr_stmt|;
block|}
specifier|public
name|void
name|setNumDPCols
parameter_list|(
name|int
name|dp
parameter_list|)
block|{
name|this
operator|.
name|numDPCols
operator|=
name|dp
expr_stmt|;
block|}
specifier|public
name|int
name|getNumDPCols
parameter_list|()
block|{
return|return
name|this
operator|.
name|numDPCols
return|;
block|}
specifier|public
name|void
name|setNumSPCols
parameter_list|(
name|int
name|sp
parameter_list|)
block|{
name|this
operator|.
name|numSPCols
operator|=
name|sp
expr_stmt|;
block|}
specifier|public
name|int
name|getNumSPCols
parameter_list|()
block|{
return|return
name|this
operator|.
name|numSPCols
return|;
block|}
specifier|public
name|void
name|setSPPath
parameter_list|(
name|String
name|sp
parameter_list|)
block|{
name|this
operator|.
name|spPath
operator|=
name|sp
expr_stmt|;
block|}
specifier|public
name|String
name|getSPPath
parameter_list|()
block|{
return|return
name|this
operator|.
name|spPath
return|;
block|}
block|}
end_class

end_unit

