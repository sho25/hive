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
name|optimizer
operator|.
name|listbucketingpruner
package|;
end_package

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

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
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  *  * Test {@link ListBucketingPruner}  *  */
end_comment

begin_class
specifier|public
class|class
name|TestListBucketingPrunner
block|{
annotation|@
name|Test
specifier|public
name|void
name|testSkipSkewedDirectory1
parameter_list|()
block|{
name|Assert
operator|.
name|assertFalse
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|skipSkewedDirectory
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSkipSkewedDirectory2
parameter_list|()
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|skipSkewedDirectory
argument_list|(
name|Boolean
operator|.
name|FALSE
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSkipSkewedDirectory3
parameter_list|()
block|{
name|Assert
operator|.
name|assertFalse
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|skipSkewedDirectory
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAndBoolOperand
parameter_list|()
block|{
comment|/**      * Operand one|Operand another | And result      */
comment|// unknown | T | unknown
name|Assert
operator|.
name|assertNull
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|andBoolOperand
argument_list|(
literal|null
argument_list|,
name|Boolean
operator|.
name|TRUE
argument_list|)
argument_list|)
expr_stmt|;
comment|// unknown | F | F
name|Assert
operator|.
name|assertFalse
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|andBoolOperand
argument_list|(
literal|null
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
argument_list|)
expr_stmt|;
comment|// unknown | unknown | unknown
name|Assert
operator|.
name|assertNull
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|andBoolOperand
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|// T | T | T
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|andBoolOperand
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|,
name|Boolean
operator|.
name|TRUE
argument_list|)
argument_list|)
expr_stmt|;
comment|// T | F | F
name|Assert
operator|.
name|assertFalse
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|andBoolOperand
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
argument_list|)
expr_stmt|;
comment|// T | unknown | unknown
name|Assert
operator|.
name|assertNull
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|andBoolOperand
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|// F | T | F
name|Assert
operator|.
name|assertFalse
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|andBoolOperand
argument_list|(
name|Boolean
operator|.
name|FALSE
argument_list|,
name|Boolean
operator|.
name|TRUE
argument_list|)
argument_list|)
expr_stmt|;
comment|// F | F | F
name|Assert
operator|.
name|assertFalse
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|andBoolOperand
argument_list|(
name|Boolean
operator|.
name|FALSE
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
argument_list|)
expr_stmt|;
comment|// F | unknown | F
name|Assert
operator|.
name|assertFalse
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|andBoolOperand
argument_list|(
name|Boolean
operator|.
name|FALSE
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOrBoolOperand
parameter_list|()
block|{
comment|// Operand one|Operand another | or result
comment|// unknown | T | T
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|orBoolOperand
argument_list|(
literal|null
argument_list|,
name|Boolean
operator|.
name|TRUE
argument_list|)
argument_list|)
expr_stmt|;
comment|// unknown | F | unknown
name|Assert
operator|.
name|assertNull
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|orBoolOperand
argument_list|(
literal|null
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
argument_list|)
expr_stmt|;
comment|// unknown | unknown | unknown
name|Assert
operator|.
name|assertNull
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|orBoolOperand
argument_list|(
literal|null
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
argument_list|)
expr_stmt|;
comment|// T | T | T
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|orBoolOperand
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|,
name|Boolean
operator|.
name|TRUE
argument_list|)
argument_list|)
expr_stmt|;
comment|// T | F | T
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|orBoolOperand
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
argument_list|)
expr_stmt|;
comment|// T | unknown | unknown
name|Assert
operator|.
name|assertNull
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|orBoolOperand
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|// F | T | T
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|orBoolOperand
argument_list|(
name|Boolean
operator|.
name|FALSE
argument_list|,
name|Boolean
operator|.
name|TRUE
argument_list|)
argument_list|)
expr_stmt|;
comment|// F | F | F
name|Assert
operator|.
name|assertFalse
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|orBoolOperand
argument_list|(
name|Boolean
operator|.
name|FALSE
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
argument_list|)
expr_stmt|;
comment|// F | unknown | unknown
name|Assert
operator|.
name|assertNull
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|orBoolOperand
argument_list|(
name|Boolean
operator|.
name|FALSE
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNotBoolOperand
parameter_list|()
block|{
comment|// Operand | Not
comment|// T | F
name|Assert
operator|.
name|assertFalse
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|notBoolOperand
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
argument_list|)
expr_stmt|;
comment|// F | T
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|notBoolOperand
argument_list|(
name|Boolean
operator|.
name|FALSE
argument_list|)
argument_list|)
expr_stmt|;
comment|// unknown | unknown
name|Assert
operator|.
name|assertNull
argument_list|(
name|ListBucketingPrunerUtils
operator|.
name|notBoolOperand
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

