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
name|hcatalog
operator|.
name|mapreduce
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|conf
operator|.
name|Configuration
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
name|FileSystem
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
name|conf
operator|.
name|HiveConf
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
name|CommandNeedRetryException
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
name|serde
operator|.
name|Constants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|ErrorType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|DefaultHCatRecord
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|HCatRecord
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatFieldSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatSchemaUtils
import|;
end_import

begin_class
specifier|public
class|class
name|TestHCatDynamicPartitioned
extends|extends
name|HCatMapReduceTest
block|{
specifier|private
name|List
argument_list|<
name|HCatRecord
argument_list|>
name|writeRecords
decl_stmt|;
specifier|private
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|dataColumns
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|initialize
parameter_list|()
throws|throws
name|Exception
block|{
name|tableName
operator|=
literal|"testHCatDynamicPartitionedTable"
expr_stmt|;
name|generateWriteRecords
argument_list|(
literal|20
argument_list|,
literal|5
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|generateDataColumns
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|generateDataColumns
parameter_list|()
throws|throws
name|HCatException
block|{
name|dataColumns
operator|=
operator|new
name|ArrayList
argument_list|<
name|HCatFieldSchema
argument_list|>
argument_list|()
expr_stmt|;
name|dataColumns
operator|.
name|add
argument_list|(
name|HCatSchemaUtils
operator|.
name|getHCatFieldSchema
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"c1"
argument_list|,
name|Constants
operator|.
name|INT_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|dataColumns
operator|.
name|add
argument_list|(
name|HCatSchemaUtils
operator|.
name|getHCatFieldSchema
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"c2"
argument_list|,
name|Constants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|dataColumns
operator|.
name|add
argument_list|(
name|HCatSchemaUtils
operator|.
name|getHCatFieldSchema
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"p1"
argument_list|,
name|Constants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|generateWriteRecords
parameter_list|(
name|int
name|max
parameter_list|,
name|int
name|mod
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
name|writeRecords
operator|=
operator|new
name|ArrayList
argument_list|<
name|HCatRecord
argument_list|>
argument_list|()
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
name|max
condition|;
name|i
operator|++
control|)
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|objList
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|objList
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|objList
operator|.
name|add
argument_list|(
literal|"strvalue"
operator|+
name|i
argument_list|)
expr_stmt|;
name|objList
operator|.
name|add
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
operator|(
name|i
operator|%
name|mod
operator|)
operator|+
name|offset
argument_list|)
argument_list|)
expr_stmt|;
name|writeRecords
operator|.
name|add
argument_list|(
operator|new
name|DefaultHCatRecord
argument_list|(
name|objList
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getPartitionKeys
parameter_list|()
block|{
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fields
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
decl_stmt|;
name|fields
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"p1"
argument_list|,
name|Constants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|fields
return|;
block|}
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getTableColumns
parameter_list|()
block|{
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fields
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
decl_stmt|;
name|fields
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"c1"
argument_list|,
name|Constants
operator|.
name|INT_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|fields
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"c2"
argument_list|,
name|Constants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|fields
return|;
block|}
specifier|public
name|void
name|testHCatDynamicPartitionedTable
parameter_list|()
throws|throws
name|Exception
block|{
name|generateWriteRecords
argument_list|(
literal|20
argument_list|,
literal|5
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|runMRCreate
argument_list|(
literal|null
argument_list|,
name|dataColumns
argument_list|,
name|writeRecords
argument_list|,
literal|20
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|runMRRead
argument_list|(
literal|20
argument_list|)
expr_stmt|;
comment|//Read with partition filter
name|runMRRead
argument_list|(
literal|4
argument_list|,
literal|"p1 = \"0\""
argument_list|)
expr_stmt|;
name|runMRRead
argument_list|(
literal|8
argument_list|,
literal|"p1 = \"1\" or p1 = \"3\""
argument_list|)
expr_stmt|;
name|runMRRead
argument_list|(
literal|4
argument_list|,
literal|"p1 = \"4\""
argument_list|)
expr_stmt|;
comment|// read from hive to test
name|String
name|query
init|=
literal|"select * from "
operator|+
name|tableName
decl_stmt|;
name|int
name|retCode
init|=
name|driver
operator|.
name|run
argument_list|(
name|query
argument_list|)
operator|.
name|getResponseCode
argument_list|()
decl_stmt|;
if|if
condition|(
name|retCode
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Error "
operator|+
name|retCode
operator|+
literal|" running query "
operator|+
name|query
argument_list|)
throw|;
block|}
name|ArrayList
argument_list|<
name|String
argument_list|>
name|res
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|driver
operator|.
name|getResults
argument_list|(
name|res
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20
argument_list|,
name|res
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|//Test for duplicate publish
name|IOException
name|exc
init|=
literal|null
decl_stmt|;
try|try
block|{
name|generateWriteRecords
argument_list|(
literal|20
argument_list|,
literal|5
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|runMRCreate
argument_list|(
literal|null
argument_list|,
name|dataColumns
argument_list|,
name|writeRecords
argument_list|,
literal|20
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|exc
operator|=
name|e
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|exc
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|exc
operator|instanceof
name|HCatException
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Got exception of type ["
operator|+
operator|(
operator|(
name|HCatException
operator|)
name|exc
operator|)
operator|.
name|getErrorType
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|"] Expected ERROR_PUBLISHING_PARTITION or ERROR_MOVE_FAILED"
argument_list|,
operator|(
name|ErrorType
operator|.
name|ERROR_PUBLISHING_PARTITION
operator|==
operator|(
operator|(
name|HCatException
operator|)
name|exc
operator|)
operator|.
name|getErrorType
argument_list|()
operator|)
operator|||
operator|(
name|ErrorType
operator|.
name|ERROR_MOVE_FAILED
operator|==
operator|(
operator|(
name|HCatException
operator|)
name|exc
operator|)
operator|.
name|getErrorType
argument_list|()
operator|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testHCatDynamicPartitionMaxPartitions
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
name|hc
init|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|maxParts
init|=
name|hiveConf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DYNAMICPARTITIONMAXPARTS
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Max partitions allowed = "
operator|+
name|maxParts
argument_list|)
expr_stmt|;
name|IOException
name|exc
init|=
literal|null
decl_stmt|;
try|try
block|{
name|generateWriteRecords
argument_list|(
name|maxParts
operator|+
literal|5
argument_list|,
name|maxParts
operator|+
literal|2
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|runMRCreate
argument_list|(
literal|null
argument_list|,
name|dataColumns
argument_list|,
name|writeRecords
argument_list|,
name|maxParts
operator|+
literal|5
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|exc
operator|=
name|e
expr_stmt|;
block|}
if|if
condition|(
name|HCatConstants
operator|.
name|HCAT_IS_DYNAMIC_MAX_PTN_CHECK_ENABLED
condition|)
block|{
name|assertTrue
argument_list|(
name|exc
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|exc
operator|instanceof
name|HCatException
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ErrorType
operator|.
name|ERROR_TOO_MANY_DYNAMIC_PTNS
argument_list|,
operator|(
operator|(
name|HCatException
operator|)
name|exc
operator|)
operator|.
name|getErrorType
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertTrue
argument_list|(
name|exc
operator|==
literal|null
argument_list|)
expr_stmt|;
name|runMRRead
argument_list|(
name|maxParts
operator|+
literal|5
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

