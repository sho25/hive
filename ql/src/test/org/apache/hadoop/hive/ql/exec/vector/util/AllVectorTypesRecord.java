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
name|exec
operator|.
name|vector
operator|.
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
import|;
end_import

begin_comment
comment|/**  *  * AllTypesRecord.  *  */
end_comment

begin_class
specifier|public
class|class
name|AllVectorTypesRecord
block|{
specifier|private
specifier|final
name|Byte
name|ctinyInt
decl_stmt|;
specifier|private
specifier|final
name|Short
name|csmallInt
decl_stmt|;
specifier|private
specifier|final
name|Integer
name|cint
decl_stmt|;
specifier|private
specifier|final
name|Long
name|cbigInt
decl_stmt|;
specifier|private
specifier|final
name|Float
name|cfloat
decl_stmt|;
specifier|private
specifier|final
name|Double
name|cdouble
decl_stmt|;
specifier|private
specifier|final
name|String
name|cstring1
decl_stmt|;
specifier|private
specifier|final
name|String
name|cstring2
decl_stmt|;
specifier|private
specifier|final
name|Timestamp
name|ctimestamp1
decl_stmt|;
specifier|private
specifier|final
name|Timestamp
name|ctimestamp2
decl_stmt|;
specifier|private
specifier|final
name|Boolean
name|cboolean1
decl_stmt|;
specifier|private
specifier|final
name|Boolean
name|cboolean2
decl_stmt|;
comment|/**    *    * @param ctinyInt    * @param csmallInt    * @param cint    * @param cbigInt    * @param cfloat    * @param cdouble    * @param cstring1    * @param cstring2    * @param ctimestamp1    * @param ctimestamp2    * @param cboolean1    * @param cboolean2    */
specifier|public
name|AllVectorTypesRecord
parameter_list|(
name|Byte
name|ctinyInt
parameter_list|,
name|Short
name|csmallInt
parameter_list|,
name|Integer
name|cint
parameter_list|,
name|Long
name|cbigInt
parameter_list|,
name|Float
name|cfloat
parameter_list|,
name|Double
name|cdouble
parameter_list|,
name|String
name|cstring1
parameter_list|,
name|String
name|cstring2
parameter_list|,
name|Timestamp
name|ctimestamp1
parameter_list|,
name|Timestamp
name|ctimestamp2
parameter_list|,
name|Boolean
name|cboolean1
parameter_list|,
name|Boolean
name|cboolean2
parameter_list|)
block|{
name|this
operator|.
name|ctinyInt
operator|=
name|ctinyInt
expr_stmt|;
name|this
operator|.
name|csmallInt
operator|=
name|csmallInt
expr_stmt|;
name|this
operator|.
name|cint
operator|=
name|cint
expr_stmt|;
name|this
operator|.
name|cbigInt
operator|=
name|cbigInt
expr_stmt|;
name|this
operator|.
name|cfloat
operator|=
name|cfloat
expr_stmt|;
name|this
operator|.
name|cdouble
operator|=
name|cdouble
expr_stmt|;
name|this
operator|.
name|cstring1
operator|=
name|cstring1
expr_stmt|;
name|this
operator|.
name|cstring2
operator|=
name|cstring2
expr_stmt|;
name|this
operator|.
name|ctimestamp1
operator|=
name|ctimestamp1
expr_stmt|;
name|this
operator|.
name|ctimestamp2
operator|=
name|ctimestamp2
expr_stmt|;
name|this
operator|.
name|cboolean1
operator|=
name|cboolean1
expr_stmt|;
name|this
operator|.
name|cboolean2
operator|=
name|cboolean2
expr_stmt|;
block|}
specifier|public
specifier|static
specifier|final
name|String
name|TABLE_NAME
init|=
literal|"alltypesorc"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TABLE_CREATE_COMMAND
init|=
literal|"CREATE TABLE "
operator|+
name|TABLE_NAME
operator|+
literal|"("
operator|+
literal|"ctinyint tinyint, "
operator|+
literal|"csmallint smallint, "
operator|+
literal|"cint int, "
operator|+
literal|"cbigint bigint, "
operator|+
literal|"cfloat float, "
operator|+
literal|"cdouble double, "
operator|+
literal|"cstring1 string, "
operator|+
literal|"cstring2 string, "
operator|+
literal|"ctimestamp1 timestamp, "
operator|+
literal|"ctimestamp2 timestamp, "
operator|+
literal|"cboolean1 boolean, "
operator|+
literal|"cboolean2 boolean) "
operator|+
literal|"STORED AS ORC"
decl_stmt|;
block|}
end_class

end_unit

