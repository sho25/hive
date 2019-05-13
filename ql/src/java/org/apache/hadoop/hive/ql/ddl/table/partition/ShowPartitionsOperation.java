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
name|partition
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
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
name|ErrorMsg
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
name|DDLOperation
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
name|DDLUtils
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
name|Table
import|;
end_import

begin_comment
comment|/**  * Operation process of showing the partitions of a table.  */
end_comment

begin_class
specifier|public
class|class
name|ShowPartitionsOperation
extends|extends
name|DDLOperation
block|{
specifier|private
specifier|final
name|ShowPartitionsDesc
name|desc
decl_stmt|;
specifier|public
name|ShowPartitionsOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|ShowPartitionsDesc
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|context
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
specifier|public
name|int
name|execute
parameter_list|()
throws|throws
name|HiveException
block|{
name|Table
name|tbl
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getTable
argument_list|(
name|desc
operator|.
name|getTabName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|tbl
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ErrorMsg
operator|.
name|TABLE_NOT_PARTITIONED
argument_list|,
name|desc
operator|.
name|getTabName
argument_list|()
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|parts
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|desc
operator|.
name|getPartSpec
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|parts
operator|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getPartitionNames
argument_list|(
name|tbl
operator|.
name|getDbName
argument_list|()
argument_list|,
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|,
name|desc
operator|.
name|getPartSpec
argument_list|()
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|parts
operator|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getPartitionNames
argument_list|(
name|tbl
operator|.
name|getDbName
argument_list|()
argument_list|,
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// write the results in the file
try|try
init|(
name|DataOutputStream
name|outStream
init|=
name|DDLUtils
operator|.
name|getOutputStream
argument_list|(
operator|new
name|Path
argument_list|(
name|desc
operator|.
name|getResFile
argument_list|()
argument_list|)
argument_list|,
name|context
argument_list|)
init|)
block|{
name|context
operator|.
name|getFormatter
argument_list|()
operator|.
name|showTablePartitions
argument_list|(
name|outStream
argument_list|,
name|parts
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|,
name|ErrorMsg
operator|.
name|GENERIC_ERROR
argument_list|,
literal|"show partitions for table "
operator|+
name|desc
operator|.
name|getTabName
argument_list|()
argument_list|)
throw|;
block|}
return|return
literal|0
return|;
block|}
block|}
end_class

end_unit

