begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
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
name|serde
operator|.
name|serdeConstants
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
name|hive
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
name|hive
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
name|hive
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
name|hive
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
name|hive
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import static
name|junit
operator|.
name|framework
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_class
specifier|public
class|class
name|TestHCatNonPartitioned
extends|extends
name|HCatMapReduceTest
block|{
specifier|private
specifier|static
name|List
argument_list|<
name|HCatRecord
argument_list|>
name|writeRecords
decl_stmt|;
specifier|static
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|partitionColumns
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|oneTimeSetUp
parameter_list|()
throws|throws
name|Exception
block|{
name|dbName
operator|=
literal|null
expr_stmt|;
comment|//test if null dbName works ("default" is used)
name|tableName
operator|=
literal|"testHCatNonPartitionedTable"
expr_stmt|;
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
literal|20
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
name|partitionColumns
operator|=
operator|new
name|ArrayList
argument_list|<
name|HCatFieldSchema
argument_list|>
argument_list|()
expr_stmt|;
name|partitionColumns
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
name|serdeConstants
operator|.
name|INT_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|partitionColumns
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
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
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
comment|//empty list, non partitioned
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
name|serdeConstants
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
name|serdeConstants
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
name|Test
specifier|public
name|void
name|testHCatNonPartitionedTable
parameter_list|()
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|runMRCreate
argument_list|(
literal|null
argument_list|,
name|partitionColumns
argument_list|,
name|writeRecords
argument_list|,
literal|10
argument_list|,
literal|true
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
name|runMRCreate
argument_list|(
literal|null
argument_list|,
name|partitionColumns
argument_list|,
name|writeRecords
argument_list|,
literal|20
argument_list|,
literal|true
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
name|assertEquals
argument_list|(
name|ErrorType
operator|.
name|ERROR_NON_EMPTY_TABLE
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
comment|//Test for publish with invalid partition key name
name|exc
operator|=
literal|null
expr_stmt|;
name|partitionMap
operator|.
name|clear
argument_list|()
expr_stmt|;
name|partitionMap
operator|.
name|put
argument_list|(
literal|"px"
argument_list|,
literal|"p1value2"
argument_list|)
expr_stmt|;
try|try
block|{
name|runMRCreate
argument_list|(
name|partitionMap
argument_list|,
name|partitionColumns
argument_list|,
name|writeRecords
argument_list|,
literal|20
argument_list|,
literal|true
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
name|assertEquals
argument_list|(
name|ErrorType
operator|.
name|ERROR_INVALID_PARTITION_VALUES
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
comment|//Read should get 10 rows
name|runMRRead
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|hiveReadTest
argument_list|()
expr_stmt|;
block|}
comment|//Test that data inserted through hcatoutputformat is readable from hive
specifier|private
name|void
name|hiveReadTest
parameter_list|()
throws|throws
name|Exception
block|{
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
literal|10
argument_list|,
name|res
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

