begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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
name|serde2
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
name|Properties
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
name|hadoop
operator|.
name|io
operator|.
name|Text
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_class
specifier|public
class|class
name|TestOpenCSVSerde
block|{
specifier|private
specifier|final
name|OpenCSVSerde
name|csv
init|=
operator|new
name|OpenCSVSerde
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
literal|"a,b,c"
argument_list|)
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
literal|"string,string,string"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeserialize
parameter_list|()
throws|throws
name|Exception
block|{
name|csv
operator|.
name|initialize
argument_list|(
literal|null
argument_list|,
name|props
argument_list|)
expr_stmt|;
specifier|final
name|Text
name|in
init|=
operator|new
name|Text
argument_list|(
literal|"hello,\"yes, okay\",1"
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|row
init|=
operator|(
name|List
argument_list|<
name|String
argument_list|>
operator|)
name|csv
operator|.
name|deserialize
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"hello"
argument_list|,
name|row
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"yes, okay"
argument_list|,
name|row
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1"
argument_list|,
name|row
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeserializeCustomSeparators
parameter_list|()
throws|throws
name|Exception
block|{
name|props
operator|.
name|setProperty
argument_list|(
name|OpenCSVSerde
operator|.
name|SEPARATORCHAR
argument_list|,
literal|"\t"
argument_list|)
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|OpenCSVSerde
operator|.
name|QUOTECHAR
argument_list|,
literal|"'"
argument_list|)
expr_stmt|;
name|csv
operator|.
name|initialize
argument_list|(
literal|null
argument_list|,
name|props
argument_list|)
expr_stmt|;
specifier|final
name|Text
name|in
init|=
operator|new
name|Text
argument_list|(
literal|"hello\t'yes\tokay'\t1"
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|row
init|=
operator|(
name|List
argument_list|<
name|String
argument_list|>
operator|)
name|csv
operator|.
name|deserialize
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"hello"
argument_list|,
name|row
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"yes\tokay"
argument_list|,
name|row
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1"
argument_list|,
name|row
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeserializeCustomEscape
parameter_list|()
throws|throws
name|Exception
block|{
name|props
operator|.
name|setProperty
argument_list|(
name|OpenCSVSerde
operator|.
name|QUOTECHAR
argument_list|,
literal|"'"
argument_list|)
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|OpenCSVSerde
operator|.
name|ESCAPECHAR
argument_list|,
literal|"\\"
argument_list|)
expr_stmt|;
name|csv
operator|.
name|initialize
argument_list|(
literal|null
argument_list|,
name|props
argument_list|)
expr_stmt|;
specifier|final
name|Text
name|in
init|=
operator|new
name|Text
argument_list|(
literal|"hello,'yes\\'okay',1"
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|row
init|=
operator|(
name|List
argument_list|<
name|String
argument_list|>
operator|)
name|csv
operator|.
name|deserialize
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"hello"
argument_list|,
name|row
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"yes'okay"
argument_list|,
name|row
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1"
argument_list|,
name|row
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

