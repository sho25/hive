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
name|hive
operator|.
name|common
operator|.
name|util
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
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
name|common
operator|.
name|type
operator|.
name|Date
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

begin_class
specifier|public
class|class
name|TestDateParser
block|{
name|DateParser
name|parser
init|=
operator|new
name|DateParser
argument_list|()
decl_stmt|;
name|Date
name|date
init|=
operator|new
name|Date
argument_list|()
decl_stmt|;
name|void
name|checkValidCase
parameter_list|(
name|String
name|strValue
parameter_list|,
name|Date
name|expected
parameter_list|)
block|{
name|Date
name|dateValue
init|=
name|parser
operator|.
name|parseDate
argument_list|(
name|strValue
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|dateValue
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|parser
operator|.
name|parseDate
argument_list|(
name|strValue
argument_list|,
name|date
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|date
argument_list|)
expr_stmt|;
block|}
name|void
name|checkInvalidCase
parameter_list|(
name|String
name|strValue
parameter_list|)
block|{
name|Date
name|dateValue
init|=
name|parser
operator|.
name|parseDate
argument_list|(
name|strValue
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|dateValue
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|parser
operator|.
name|parseDate
argument_list|(
name|strValue
argument_list|,
name|date
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testValidCases
parameter_list|()
throws|throws
name|Exception
block|{
name|checkValidCase
argument_list|(
literal|"1945-12-31"
argument_list|,
name|Date
operator|.
name|valueOf
argument_list|(
literal|"1945-12-31"
argument_list|)
argument_list|)
expr_stmt|;
name|checkValidCase
argument_list|(
literal|"1946-01-01"
argument_list|,
name|Date
operator|.
name|valueOf
argument_list|(
literal|"1946-01-01"
argument_list|)
argument_list|)
expr_stmt|;
name|checkValidCase
argument_list|(
literal|"2001-11-12"
argument_list|,
name|Date
operator|.
name|valueOf
argument_list|(
literal|"2001-11-12"
argument_list|)
argument_list|)
expr_stmt|;
name|checkValidCase
argument_list|(
literal|"0004-05-06"
argument_list|,
name|Date
operator|.
name|valueOf
argument_list|(
literal|"0004-05-06"
argument_list|)
argument_list|)
expr_stmt|;
name|checkValidCase
argument_list|(
literal|"1678-09-10"
argument_list|,
name|Date
operator|.
name|valueOf
argument_list|(
literal|"1678-09-10"
argument_list|)
argument_list|)
expr_stmt|;
name|checkValidCase
argument_list|(
literal|"9999-10-11"
argument_list|,
name|Date
operator|.
name|valueOf
argument_list|(
literal|"9999-10-11"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Timestamp strings should parse ok
name|checkValidCase
argument_list|(
literal|"2001-11-12 01:02:03"
argument_list|,
name|Date
operator|.
name|valueOf
argument_list|(
literal|"2001-11-12"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Leading spaces
name|checkValidCase
argument_list|(
literal|" 1946-01-01"
argument_list|,
name|Date
operator|.
name|valueOf
argument_list|(
literal|"1946-01-01"
argument_list|)
argument_list|)
expr_stmt|;
name|checkValidCase
argument_list|(
literal|" 2001-11-12 01:02:03"
argument_list|,
name|Date
operator|.
name|valueOf
argument_list|(
literal|"2001-11-12"
argument_list|)
argument_list|)
expr_stmt|;
name|checkValidCase
argument_list|(
literal|"2001-13-12"
argument_list|,
name|Date
operator|.
name|valueOf
argument_list|(
literal|"2002-01-12"
argument_list|)
argument_list|)
expr_stmt|;
name|checkValidCase
argument_list|(
literal|"2001-11-31"
argument_list|,
name|Date
operator|.
name|valueOf
argument_list|(
literal|"2001-12-01"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInvalidCases
parameter_list|()
throws|throws
name|Exception
block|{
name|checkInvalidCase
argument_list|(
literal|"2001"
argument_list|)
expr_stmt|;
name|checkInvalidCase
argument_list|(
literal|"2001-01"
argument_list|)
expr_stmt|;
name|checkInvalidCase
argument_list|(
literal|"abc"
argument_list|)
expr_stmt|;
name|checkInvalidCase
argument_list|(
literal|" 2001 "
argument_list|)
expr_stmt|;
name|checkInvalidCase
argument_list|(
literal|"a2001-01-01"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

