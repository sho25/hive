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
name|serde2
operator|.
name|typeinfo
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfo
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
name|TypeInfoUtils
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
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  * TypeInfoUtils Test.  */
end_comment

begin_class
specifier|public
class|class
name|TestTypeInfoUtils
block|{
specifier|static
name|void
name|parseTypeString
parameter_list|(
name|String
name|typeString
parameter_list|,
name|boolean
name|exceptionExpected
parameter_list|)
block|{
name|boolean
name|caughtException
init|=
literal|false
decl_stmt|;
try|try
block|{
name|TypeInfoUtils
operator|.
name|getTypeInfoFromTypeString
argument_list|(
name|typeString
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|err
parameter_list|)
block|{
name|caughtException
operator|=
literal|true
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"parsing typestring "
operator|+
name|typeString
argument_list|,
name|exceptionExpected
argument_list|,
name|caughtException
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTypeInfoParser
parameter_list|()
block|{
name|String
index|[]
name|validTypeStrings
init|=
block|{
literal|"int"
block|,
literal|"string"
block|,
literal|"varchar(10)"
block|,
literal|"char(15)"
block|,
literal|"array<int>"
block|}
decl_stmt|;
name|String
index|[]
name|invalidTypeStrings
init|=
block|{
literal|"array<"
block|,
literal|"varchar(123"
block|,
literal|"varchar(123,"
block|,
literal|"varchar()"
block|,
literal|"varchar("
block|,
literal|"char(123"
block|,
literal|"char(123,)"
block|,
literal|"char()"
block|,
literal|"char("
block|,
literal|"decimal()"
block|}
decl_stmt|;
for|for
control|(
name|String
name|typeString
range|:
name|validTypeStrings
control|)
block|{
name|parseTypeString
argument_list|(
name|typeString
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|typeString
range|:
name|invalidTypeStrings
control|)
block|{
name|parseTypeString
argument_list|(
name|typeString
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testQualifiedTypeNoParams
parameter_list|()
block|{
name|boolean
name|caughtException
init|=
literal|false
decl_stmt|;
try|try
block|{
name|TypeInfoUtils
operator|.
name|getTypeInfoFromTypeString
argument_list|(
literal|"varchar"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|err
parameter_list|)
block|{
name|caughtException
operator|=
literal|true
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"varchar TypeInfo with no params should fail"
argument_list|,
literal|true
argument_list|,
name|caughtException
argument_list|)
expr_stmt|;
try|try
block|{
name|TypeInfoUtils
operator|.
name|getTypeInfoFromTypeString
argument_list|(
literal|"char"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|err
parameter_list|)
block|{
name|caughtException
operator|=
literal|true
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"char TypeInfo with no params should fail"
argument_list|,
literal|true
argument_list|,
name|caughtException
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|DecimalTestCase
block|{
name|String
name|typeString
decl_stmt|;
name|int
name|expectedPrecision
decl_stmt|;
name|int
name|expectedScale
decl_stmt|;
specifier|public
name|DecimalTestCase
parameter_list|(
name|String
name|typeString
parameter_list|,
name|int
name|expectedPrecision
parameter_list|,
name|int
name|expectedScale
parameter_list|)
block|{
name|this
operator|.
name|typeString
operator|=
name|typeString
expr_stmt|;
name|this
operator|.
name|expectedPrecision
operator|=
name|expectedPrecision
expr_stmt|;
name|this
operator|.
name|expectedScale
operator|=
name|expectedScale
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimal
parameter_list|()
block|{
name|DecimalTestCase
index|[]
name|testCases
init|=
block|{
operator|new
name|DecimalTestCase
argument_list|(
literal|"decimal"
argument_list|,
literal|10
argument_list|,
literal|0
argument_list|)
block|,
operator|new
name|DecimalTestCase
argument_list|(
literal|"decimal(1)"
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|)
block|,
operator|new
name|DecimalTestCase
argument_list|(
literal|"decimal(25)"
argument_list|,
literal|25
argument_list|,
literal|0
argument_list|)
block|,
operator|new
name|DecimalTestCase
argument_list|(
literal|"decimal(2,0)"
argument_list|,
literal|2
argument_list|,
literal|0
argument_list|)
block|,
operator|new
name|DecimalTestCase
argument_list|(
literal|"decimal(2,1)"
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|)
block|,
operator|new
name|DecimalTestCase
argument_list|(
literal|"decimal(25,10)"
argument_list|,
literal|25
argument_list|,
literal|10
argument_list|)
block|,
operator|new
name|DecimalTestCase
argument_list|(
literal|"decimal(38,20)"
argument_list|,
literal|38
argument_list|,
literal|20
argument_list|)
block|}
decl_stmt|;
for|for
control|(
name|DecimalTestCase
name|testCase
range|:
name|testCases
control|)
block|{
name|TypeInfo
name|typeInfo
init|=
name|TypeInfoUtils
operator|.
name|getTypeInfoFromTypeString
argument_list|(
name|testCase
operator|.
name|typeString
argument_list|)
decl_stmt|;
name|DecimalTypeInfo
name|decimalType
init|=
operator|(
name|DecimalTypeInfo
operator|)
name|typeInfo
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Failed for "
operator|+
name|testCase
operator|.
name|typeString
argument_list|,
name|testCase
operator|.
name|expectedPrecision
argument_list|,
name|decimalType
operator|.
name|getPrecision
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Failed for "
operator|+
name|testCase
operator|.
name|typeString
argument_list|,
name|testCase
operator|.
name|expectedScale
argument_list|,
name|decimalType
operator|.
name|getScale
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

