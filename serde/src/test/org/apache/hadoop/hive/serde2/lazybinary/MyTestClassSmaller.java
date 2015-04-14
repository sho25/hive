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
name|serde2
operator|.
name|lazybinary
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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
name|HiveChar
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
name|HiveDecimal
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
name|HiveIntervalDayTime
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
name|HiveIntervalYearMonth
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
name|HiveVarchar
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
name|binarysortable
operator|.
name|MyTestClass
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
name|binarysortable
operator|.
name|MyTestInnerStruct
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
name|binarysortable
operator|.
name|MyTestPrimitiveClass
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
name|binarysortable
operator|.
name|MyTestPrimitiveClass
operator|.
name|ExtraTypeInfo
import|;
end_import

begin_class
specifier|public
class|class
name|MyTestClassSmaller
block|{
specifier|public
name|Boolean
name|myBool
decl_stmt|;
specifier|public
name|Byte
name|myByte
decl_stmt|;
specifier|public
name|Short
name|myShort
decl_stmt|;
specifier|public
name|Integer
name|myInt
decl_stmt|;
specifier|public
name|Long
name|myLong
decl_stmt|;
specifier|public
name|Float
name|myFloat
decl_stmt|;
specifier|public
name|Double
name|myDouble
decl_stmt|;
specifier|public
name|String
name|myString
decl_stmt|;
specifier|public
name|HiveChar
name|myHiveChar
decl_stmt|;
specifier|public
name|HiveVarchar
name|myHiveVarchar
decl_stmt|;
specifier|public
name|byte
index|[]
name|myBinary
decl_stmt|;
specifier|public
name|HiveDecimal
name|myDecimal
decl_stmt|;
specifier|public
name|Date
name|myDate
decl_stmt|;
specifier|public
name|Timestamp
name|myTimestamp
decl_stmt|;
specifier|public
name|HiveIntervalYearMonth
name|myIntervalYearMonth
decl_stmt|;
specifier|public
name|HiveIntervalDayTime
name|myIntervalDayTime
decl_stmt|;
name|MyTestInnerStruct
name|myStruct
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|int
name|smallerCount
init|=
literal|17
decl_stmt|;
specifier|public
name|int
name|randomFill
parameter_list|(
name|Random
name|r
parameter_list|,
name|ExtraTypeInfo
name|extraTypeInfo
parameter_list|)
block|{
name|int
name|randField
init|=
name|r
operator|.
name|nextInt
argument_list|(
name|smallerCount
argument_list|)
decl_stmt|;
name|int
name|field
init|=
literal|0
decl_stmt|;
name|myBool
operator|=
operator|(
name|randField
operator|==
name|field
operator|++
operator|)
condition|?
literal|null
else|:
operator|(
name|r
operator|.
name|nextInt
argument_list|(
literal|1
argument_list|)
operator|==
literal|1
operator|)
expr_stmt|;
name|myByte
operator|=
operator|(
name|randField
operator|==
name|field
operator|++
operator|)
condition|?
literal|null
else|:
name|Byte
operator|.
name|valueOf
argument_list|(
operator|(
name|byte
operator|)
name|r
operator|.
name|nextInt
argument_list|()
argument_list|)
expr_stmt|;
name|myShort
operator|=
operator|(
name|randField
operator|==
name|field
operator|++
operator|)
condition|?
literal|null
else|:
name|Short
operator|.
name|valueOf
argument_list|(
operator|(
name|short
operator|)
name|r
operator|.
name|nextInt
argument_list|()
argument_list|)
expr_stmt|;
name|myInt
operator|=
operator|(
name|randField
operator|==
name|field
operator|++
operator|)
condition|?
literal|null
else|:
name|Integer
operator|.
name|valueOf
argument_list|(
name|r
operator|.
name|nextInt
argument_list|()
argument_list|)
expr_stmt|;
name|myLong
operator|=
operator|(
name|randField
operator|==
name|field
operator|++
operator|)
condition|?
literal|null
else|:
name|Long
operator|.
name|valueOf
argument_list|(
name|r
operator|.
name|nextLong
argument_list|()
argument_list|)
expr_stmt|;
name|myFloat
operator|=
operator|(
name|randField
operator|==
name|field
operator|++
operator|)
condition|?
literal|null
else|:
name|Float
operator|.
name|valueOf
argument_list|(
name|r
operator|.
name|nextFloat
argument_list|()
operator|*
literal|10
operator|-
literal|5
argument_list|)
expr_stmt|;
name|myDouble
operator|=
operator|(
name|randField
operator|==
name|field
operator|++
operator|)
condition|?
literal|null
else|:
name|Double
operator|.
name|valueOf
argument_list|(
name|r
operator|.
name|nextDouble
argument_list|()
operator|*
literal|10
operator|-
literal|5
argument_list|)
expr_stmt|;
name|myString
operator|=
operator|(
name|randField
operator|==
name|field
operator|++
operator|)
condition|?
literal|null
else|:
name|MyTestPrimitiveClass
operator|.
name|getRandString
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|myHiveChar
operator|=
operator|(
name|randField
operator|==
name|field
operator|++
operator|)
condition|?
literal|null
else|:
name|MyTestPrimitiveClass
operator|.
name|getRandHiveChar
argument_list|(
name|r
argument_list|,
name|extraTypeInfo
argument_list|)
expr_stmt|;
name|myHiveVarchar
operator|=
operator|(
name|randField
operator|==
name|field
operator|++
operator|)
condition|?
literal|null
else|:
name|MyTestPrimitiveClass
operator|.
name|getRandHiveVarchar
argument_list|(
name|r
argument_list|,
name|extraTypeInfo
argument_list|)
expr_stmt|;
name|myBinary
operator|=
name|MyTestPrimitiveClass
operator|.
name|getRandBinary
argument_list|(
name|r
argument_list|,
name|r
operator|.
name|nextInt
argument_list|(
literal|1000
argument_list|)
argument_list|)
expr_stmt|;
name|myDecimal
operator|=
operator|(
name|randField
operator|==
name|field
operator|++
operator|)
condition|?
literal|null
else|:
name|MyTestPrimitiveClass
operator|.
name|getRandHiveDecimal
argument_list|(
name|r
argument_list|,
name|extraTypeInfo
argument_list|)
expr_stmt|;
name|myDate
operator|=
operator|(
name|randField
operator|==
name|field
operator|++
operator|)
condition|?
literal|null
else|:
name|MyTestPrimitiveClass
operator|.
name|getRandDate
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|myTimestamp
operator|=
operator|(
name|randField
operator|==
name|field
operator|++
operator|)
condition|?
literal|null
else|:
name|MyTestPrimitiveClass
operator|.
name|getRandTimestamp
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|myIntervalYearMonth
operator|=
operator|(
name|randField
operator|==
name|field
operator|++
operator|)
condition|?
literal|null
else|:
name|MyTestPrimitiveClass
operator|.
name|getRandIntervalYearMonth
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|myIntervalDayTime
operator|=
operator|(
name|randField
operator|==
name|field
operator|++
operator|)
condition|?
literal|null
else|:
name|MyTestPrimitiveClass
operator|.
name|getRandIntervalDayTime
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|myStruct
operator|=
operator|(
name|randField
operator|==
name|field
operator|++
operator|)
condition|?
literal|null
else|:
operator|new
name|MyTestInnerStruct
argument_list|(
name|r
operator|.
name|nextInt
argument_list|(
literal|5
argument_list|)
operator|-
literal|2
argument_list|,
name|r
operator|.
name|nextInt
argument_list|(
literal|5
argument_list|)
operator|-
literal|2
argument_list|)
expr_stmt|;
return|return
name|field
return|;
block|}
block|}
end_class

end_unit

