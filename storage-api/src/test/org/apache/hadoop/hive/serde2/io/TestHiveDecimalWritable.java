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
name|io
package|;
end_package

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|*
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
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigDecimal
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigInteger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteOrder
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

begin_comment
comment|/**  * Unit tests for tsting the fast allocation-free conversion  * between HiveDecimalWritable and Decimal128  */
end_comment

begin_class
specifier|public
class|class
name|TestHiveDecimalWritable
block|{
annotation|@
name|Test
specifier|public
name|void
name|testHiveDecimalWritable
parameter_list|()
block|{
name|HiveDecimalWritable
name|decWritable
decl_stmt|;
name|HiveDecimal
name|nullDec
init|=
literal|null
decl_stmt|;
name|decWritable
operator|=
operator|new
name|HiveDecimalWritable
argument_list|(
name|nullDec
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|!
name|decWritable
operator|.
name|isSet
argument_list|()
argument_list|)
expr_stmt|;
name|decWritable
operator|=
operator|new
name|HiveDecimalWritable
argument_list|(
literal|"1"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|decWritable
operator|.
name|isSet
argument_list|()
argument_list|)
expr_stmt|;
comment|// UNDONE: more!
block|}
block|}
end_class

end_unit

