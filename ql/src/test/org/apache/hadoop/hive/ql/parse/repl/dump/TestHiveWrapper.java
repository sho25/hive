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
name|parse
operator|.
name|repl
operator|.
name|dump
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
name|metastore
operator|.
name|api
operator|.
name|Table
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
name|metadata
operator|.
name|HiveException
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
name|parse
operator|.
name|ReplicationSpec
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
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|InOrder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|runners
operator|.
name|MockitoJUnitRunner
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|MockitoJUnitRunner
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestHiveWrapper
block|{
annotation|@
name|Mock
specifier|private
name|HiveWrapper
operator|.
name|Tuple
operator|.
name|Function
argument_list|<
name|ReplicationSpec
argument_list|>
name|specFunction
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|HiveWrapper
operator|.
name|Tuple
operator|.
name|Function
argument_list|<
name|Table
argument_list|>
name|tableFunction
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|replicationIdIsRequestedBeforeObjectDefinition
parameter_list|()
throws|throws
name|HiveException
block|{
operator|new
name|HiveWrapper
operator|.
name|Tuple
argument_list|<>
argument_list|(
name|specFunction
argument_list|,
name|tableFunction
argument_list|)
expr_stmt|;
name|InOrder
name|inOrder
init|=
name|Mockito
operator|.
name|inOrder
argument_list|(
name|specFunction
argument_list|,
name|tableFunction
argument_list|)
decl_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|specFunction
argument_list|)
operator|.
name|fromMetaStore
argument_list|()
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|tableFunction
argument_list|)
operator|.
name|fromMetaStore
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

