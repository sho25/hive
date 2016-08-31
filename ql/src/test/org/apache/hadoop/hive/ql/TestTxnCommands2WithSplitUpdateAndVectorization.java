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
name|ql
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
name|conf
operator|.
name|HiveConf
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

begin_comment
comment|/**  * Same as TestTxnCommands2WithSplitUpdate but tests ACID tables with vectorization turned on by  * default, and having 'transactional_properties' set to 'default'. This specifically tests the  * fast VectorizedOrcAcidRowBatchReader for ACID tables with split-update turned on.  */
end_comment

begin_class
specifier|public
class|class
name|TestTxnCommands2WithSplitUpdateAndVectorization
extends|extends
name|TestTxnCommands2WithSplitUpdate
block|{
specifier|public
name|TestTxnCommands2WithSplitUpdateAndVectorization
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|setUpWithTableProperties
argument_list|(
literal|"'transactional'='true','transactional_properties'='default'"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_VECTORIZATION_ENABLED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|Test
specifier|public
name|void
name|testFailureOnAlteringTransactionalProperties
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Override to do nothing, as the this test is not related with vectorization.
comment|// The parent class creates a temporary table in this test and alters its properties.
comment|// To not override this test, that temporary table needs to be renamed. However, as
comment|// mentioned this does not serve any purpose, as this test does not relate to vectorization.
block|}
block|}
end_class

end_unit

