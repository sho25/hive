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
name|binarysortable
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

begin_class
specifier|public
class|class
name|MyTestClass
block|{
name|Byte
name|myByte
decl_stmt|;
name|Short
name|myShort
decl_stmt|;
name|Integer
name|myInt
decl_stmt|;
name|Long
name|myLong
decl_stmt|;
name|Float
name|myFloat
decl_stmt|;
name|Double
name|myDouble
decl_stmt|;
name|String
name|myString
decl_stmt|;
name|MyTestInnerStruct
name|myStruct
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|myList
decl_stmt|;
specifier|public
name|MyTestClass
parameter_list|()
block|{   }
specifier|public
name|MyTestClass
parameter_list|(
name|Byte
name|b
parameter_list|,
name|Short
name|s
parameter_list|,
name|Integer
name|i
parameter_list|,
name|Long
name|l
parameter_list|,
name|Float
name|f
parameter_list|,
name|Double
name|d
parameter_list|,
name|String
name|st
parameter_list|,
name|MyTestInnerStruct
name|is
parameter_list|,
name|List
argument_list|<
name|Integer
argument_list|>
name|li
parameter_list|)
block|{
name|myByte
operator|=
name|b
expr_stmt|;
name|myShort
operator|=
name|s
expr_stmt|;
name|myInt
operator|=
name|i
expr_stmt|;
name|myLong
operator|=
name|l
expr_stmt|;
name|myFloat
operator|=
name|f
expr_stmt|;
name|myDouble
operator|=
name|d
expr_stmt|;
name|myString
operator|=
name|st
expr_stmt|;
name|myStruct
operator|=
name|is
expr_stmt|;
name|myList
operator|=
name|li
expr_stmt|;
block|}
block|}
end_class

end_unit

