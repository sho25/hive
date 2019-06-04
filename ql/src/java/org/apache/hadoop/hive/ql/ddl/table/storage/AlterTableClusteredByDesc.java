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
name|ql
operator|.
name|ddl
operator|.
name|table
operator|.
name|storage
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|ql
operator|.
name|ddl
operator|.
name|DDLTask2
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
name|ddl
operator|.
name|table
operator|.
name|AbstractAlterTableDesc
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
name|SemanticException
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
name|plan
operator|.
name|AlterTableDesc
operator|.
name|AlterTableTypes
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
name|plan
operator|.
name|Explain
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
name|plan
operator|.
name|Explain
operator|.
name|Level
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
name|util
operator|.
name|DirectionUtils
import|;
end_import

begin_comment
comment|/**  * DDL task description for ALTER TABLE ... CLUSTERED BY ... SORTED BY ... [INTO ... BUCKETS] commands.  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Clustered By"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|AlterTableClusteredByDesc
extends|extends
name|AbstractAlterTableDesc
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
static|static
block|{
name|DDLTask2
operator|.
name|registerOperation
argument_list|(
name|AlterTableClusteredByDesc
operator|.
name|class
argument_list|,
name|AlterTableClusteredByOperation
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|final
name|int
name|numberBuckets
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|bucketColumns
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Order
argument_list|>
name|sortColumns
decl_stmt|;
specifier|public
name|AlterTableClusteredByDesc
parameter_list|(
name|String
name|tableName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
parameter_list|,
name|int
name|numberBuckets
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|bucketColumns
parameter_list|,
name|List
argument_list|<
name|Order
argument_list|>
name|sortColumns
parameter_list|)
throws|throws
name|SemanticException
block|{
name|super
argument_list|(
name|AlterTableTypes
operator|.
name|CLUSTERED_BY
argument_list|,
name|tableName
argument_list|,
name|partitionSpec
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|numberBuckets
operator|=
name|numberBuckets
expr_stmt|;
name|this
operator|.
name|bucketColumns
operator|=
name|bucketColumns
expr_stmt|;
name|this
operator|.
name|sortColumns
operator|=
name|sortColumns
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"number of buckets"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|int
name|getNumberBuckets
parameter_list|()
block|{
return|return
name|numberBuckets
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"bucket columns"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getBucketColumns
parameter_list|()
block|{
return|return
name|bucketColumns
return|;
block|}
specifier|public
name|List
argument_list|<
name|Order
argument_list|>
name|getSortColumns
parameter_list|()
block|{
return|return
name|sortColumns
return|;
block|}
comment|// Only for explaining
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"sort columns"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getSortColumnsExplain
parameter_list|()
block|{
return|return
name|sortColumns
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|t
lambda|->
name|t
operator|.
name|getCol
argument_list|()
operator|+
literal|" "
operator|+
name|DirectionUtils
operator|.
name|codeToText
argument_list|(
name|t
operator|.
name|getOrder
argument_list|()
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|mayNeedWriteId
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

