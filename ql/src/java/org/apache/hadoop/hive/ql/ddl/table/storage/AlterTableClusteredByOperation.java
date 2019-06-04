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
name|ql
operator|.
name|ddl
operator|.
name|DDLOperationContext
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
name|AbstractAlterTableOperation
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
name|Utilities
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
name|HiveException
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

begin_comment
comment|/**  * Operation process of clustering a table by some column.  */
end_comment

begin_class
specifier|public
class|class
name|AlterTableClusteredByOperation
extends|extends
name|AbstractAlterTableOperation
block|{
specifier|private
specifier|final
name|AlterTableClusteredByDesc
name|desc
decl_stmt|;
specifier|public
name|AlterTableClusteredByOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|AlterTableClusteredByDesc
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|,
name|desc
argument_list|)
expr_stmt|;
name|this
operator|.
name|desc
operator|=
name|desc
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doAlteration
parameter_list|(
name|Table
name|table
parameter_list|,
name|Partition
name|partition
parameter_list|)
throws|throws
name|HiveException
block|{
name|StorageDescriptor
name|sd
init|=
name|getStorageDescriptor
argument_list|(
name|table
argument_list|,
name|partition
argument_list|)
decl_stmt|;
comment|// validate sort columns and bucket columns
name|List
argument_list|<
name|String
argument_list|>
name|columns
init|=
name|Utilities
operator|.
name|getColumnNamesFromFieldSchema
argument_list|(
name|table
operator|.
name|getCols
argument_list|()
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|validateColumnNames
argument_list|(
name|columns
argument_list|,
name|desc
operator|.
name|getBucketColumns
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|desc
operator|.
name|getSortColumns
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Utilities
operator|.
name|validateColumnNames
argument_list|(
name|columns
argument_list|,
name|Utilities
operator|.
name|getColumnNamesFromSortCols
argument_list|(
name|desc
operator|.
name|getSortColumns
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|sd
operator|.
name|setBucketCols
argument_list|(
name|desc
operator|.
name|getBucketColumns
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setNumBuckets
argument_list|(
name|desc
operator|.
name|getNumberBuckets
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setSortCols
argument_list|(
name|desc
operator|.
name|getSortColumns
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

