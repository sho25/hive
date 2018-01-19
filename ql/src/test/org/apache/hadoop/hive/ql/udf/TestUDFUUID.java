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
name|udf
package|;
end_package

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|exec
operator|.
name|FunctionRegistry
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFBridge
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
name|TestUDFUUID
extends|extends
name|TestCase
block|{
annotation|@
name|Test
specifier|public
name|void
name|testUUID
parameter_list|()
throws|throws
name|Exception
block|{
name|UDFUUID
name|udf
init|=
operator|new
name|UDFUUID
argument_list|()
decl_stmt|;
name|String
name|id1
init|=
name|udf
operator|.
name|evaluate
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|id2
init|=
name|udf
operator|.
name|evaluate
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|id1
operator|.
name|equals
argument_list|(
name|id2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|id1
operator|.
name|length
argument_list|()
argument_list|,
literal|36
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|id2
operator|.
name|length
argument_list|()
argument_list|,
literal|36
argument_list|)
expr_stmt|;
name|GenericUDFBridge
name|bridge
init|=
operator|new
name|GenericUDFBridge
argument_list|(
literal|"uuid"
argument_list|,
literal|false
argument_list|,
name|UDFUUID
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|FunctionRegistry
operator|.
name|isDeterministic
argument_list|(
name|bridge
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

