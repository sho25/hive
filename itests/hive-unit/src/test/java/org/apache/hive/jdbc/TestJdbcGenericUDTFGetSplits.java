begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|jdbc
package|;
end_package

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
name|llap
operator|.
name|FieldDesc
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
name|llap
operator|.
name|LlapBaseInputFormat
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
name|llap
operator|.
name|LlapInputSplit
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
name|serde2
operator|.
name|typeinfo
operator|.
name|DecimalTypeInfo
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
name|mapred
operator|.
name|JobConf
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
import|import
name|java
operator|.
name|sql
operator|.
name|ResultSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Statement
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|UUID
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
name|assertNotNull
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

begin_comment
comment|/**  * TestJdbcGenericUDTFGetSplits.  */
end_comment

begin_class
specifier|public
class|class
name|TestJdbcGenericUDTFGetSplits
extends|extends
name|AbstractTestJdbcGenericUDTFGetSplits
block|{
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|200000
argument_list|)
specifier|public
name|void
name|testGenericUDTFOrderBySplitCount1
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testGenericUDTFOrderBySplitCount1
argument_list|(
literal|"get_splits"
argument_list|,
operator|new
name|int
index|[]
block|{
literal|10
block|,
literal|1
block|,
literal|0
block|,
literal|1
block|,
literal|10
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalPrecisionAndScale
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|Statement
name|stmt
init|=
name|hs2Conn
operator|.
name|createStatement
argument_list|()
init|)
block|{
name|stmt
operator|.
name|execute
argument_list|(
literal|"CREATE TABLE decimal_test_table(decimal_col DECIMAL(6,2))"
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"INSERT INTO decimal_test_table VALUES(2507.92)"
argument_list|)
expr_stmt|;
name|ResultSet
name|rs
init|=
name|stmt
operator|.
name|executeQuery
argument_list|(
literal|"SELECT * FROM decimal_test_table"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|rs
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
name|rs
operator|.
name|close
argument_list|()
expr_stmt|;
name|String
name|url
init|=
name|miniHS2
operator|.
name|getJdbcURL
argument_list|()
decl_stmt|;
name|String
name|user
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
decl_stmt|;
name|String
name|pwd
init|=
name|user
decl_stmt|;
name|String
name|handleId
init|=
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|sql
init|=
literal|"SELECT avg(decimal_col)/3 FROM decimal_test_table"
decl_stmt|;
comment|// make request through llap-ext-client
name|JobConf
name|job
init|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|job
operator|.
name|set
argument_list|(
name|LlapBaseInputFormat
operator|.
name|URL_KEY
argument_list|,
name|url
argument_list|)
expr_stmt|;
name|job
operator|.
name|set
argument_list|(
name|LlapBaseInputFormat
operator|.
name|USER_KEY
argument_list|,
name|user
argument_list|)
expr_stmt|;
name|job
operator|.
name|set
argument_list|(
name|LlapBaseInputFormat
operator|.
name|PWD_KEY
argument_list|,
name|pwd
argument_list|)
expr_stmt|;
name|job
operator|.
name|set
argument_list|(
name|LlapBaseInputFormat
operator|.
name|QUERY_KEY
argument_list|,
name|sql
argument_list|)
expr_stmt|;
name|job
operator|.
name|set
argument_list|(
name|LlapBaseInputFormat
operator|.
name|HANDLE_ID
argument_list|,
name|handleId
argument_list|)
expr_stmt|;
name|LlapBaseInputFormat
name|llapBaseInputFormat
init|=
operator|new
name|LlapBaseInputFormat
argument_list|()
decl_stmt|;
comment|//schema split
name|LlapInputSplit
name|schemaSplit
init|=
operator|(
name|LlapInputSplit
operator|)
name|llapBaseInputFormat
operator|.
name|getSplits
argument_list|(
name|job
argument_list|,
literal|0
argument_list|)
index|[
literal|0
index|]
decl_stmt|;
name|assertNotNull
argument_list|(
name|schemaSplit
argument_list|)
expr_stmt|;
name|FieldDesc
name|fieldDesc
init|=
name|schemaSplit
operator|.
name|getSchema
argument_list|()
operator|.
name|getColumns
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|DecimalTypeInfo
name|type
init|=
operator|(
name|DecimalTypeInfo
operator|)
name|fieldDesc
operator|.
name|getTypeInfo
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|12
argument_list|,
name|type
operator|.
name|getPrecision
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|8
argument_list|,
name|type
operator|.
name|scale
argument_list|()
argument_list|)
expr_stmt|;
name|LlapBaseInputFormat
operator|.
name|close
argument_list|(
name|handleId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

