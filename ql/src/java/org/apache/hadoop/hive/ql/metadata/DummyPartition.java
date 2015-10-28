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
name|metadata
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
name|LinkedHashMap
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
name|FieldSchema
import|;
end_import

begin_comment
comment|/**  * A Hive Table Partition: is a fundamental storage unit within a Table. Currently, Hive does not support  * hierarchical partitions - For eg: if partition ds=1, hr=1 exists, there is no way to access ds=1  *  * Hierarchical partitions are needed in some cases, for eg. locking. For now, create a dummy partition to  * satisfy this  */
end_comment

begin_class
specifier|public
class|class
name|DummyPartition
extends|extends
name|Partition
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
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
literal|"hive.ql.metadata.DummyPartition"
argument_list|)
decl_stmt|;
specifier|private
name|String
name|name
decl_stmt|;
specifier|private
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
decl_stmt|;
specifier|public
name|DummyPartition
parameter_list|()
block|{   }
specifier|public
name|DummyPartition
parameter_list|(
name|Table
name|tbl
parameter_list|,
name|String
name|name
parameter_list|)
throws|throws
name|HiveException
block|{
name|setTable
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
specifier|public
name|DummyPartition
parameter_list|(
name|Table
name|tbl
parameter_list|,
name|String
name|name
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
throws|throws
name|HiveException
block|{
name|setTable
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|partSpec
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|partSpec
argument_list|)
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
name|String
name|getCompleteName
parameter_list|()
block|{
return|return
name|getName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getSpec
parameter_list|()
block|{
return|return
name|partSpec
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getValues
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FieldSchema
name|fs
range|:
name|this
operator|.
name|getTable
argument_list|()
operator|.
name|getPartCols
argument_list|()
control|)
block|{
name|values
operator|.
name|add
argument_list|(
name|partSpec
operator|.
name|get
argument_list|(
name|fs
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|values
return|;
block|}
block|}
end_class

end_unit

