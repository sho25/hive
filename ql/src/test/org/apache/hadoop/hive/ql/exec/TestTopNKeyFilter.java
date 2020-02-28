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
package|;
end_package

begin_import
import|import static
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
name|VectorTopNKeyOperator
operator|.
name|checkTopNFilterEfficiency
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|hasItem
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|hasSize
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|is
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
name|assertThat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Unit test of TopNKeyFilter.  */
end_comment

begin_class
specifier|public
class|class
name|TestTopNKeyFilter
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestTopNKeyFilter
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Comparator
argument_list|<
name|TestKeyWrapper
argument_list|>
name|TEST_KEY_WRAPPER_COMPARATOR
init|=
name|Comparator
operator|.
name|comparingInt
argument_list|(
name|o
lambda|->
name|o
operator|.
name|keyValue
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testNothingCanBeForwardedIfTopNIs0
parameter_list|()
block|{
name|TopNKeyFilter
name|topNKeyFilter
init|=
operator|new
name|TopNKeyFilter
argument_list|(
literal|0
argument_list|,
name|TEST_KEY_WRAPPER_COMPARATOR
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFirstTopNKeysCanBeForwarded
parameter_list|()
block|{
name|TopNKeyFilter
name|topNKeyFilter
init|=
operator|new
name|TopNKeyFilter
argument_list|(
literal|3
argument_list|,
name|TEST_KEY_WRAPPER_COMPARATOR
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|10
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|11
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testKeyCanNotBeForwardedIfItIsDroppedOutFromTopNKeys
parameter_list|()
block|{
name|TopNKeyFilter
name|topNKeyFilter
init|=
operator|new
name|TopNKeyFilter
argument_list|(
literal|2
argument_list|,
name|TEST_KEY_WRAPPER_COMPARATOR
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMembersOfTopNKeysStillCanBeForwardedAfterNonTopNKeysTried
parameter_list|()
block|{
name|TopNKeyFilter
name|topNKeyFilter
init|=
operator|new
name|TopNKeyFilter
argument_list|(
literal|2
argument_list|,
name|TEST_KEY_WRAPPER_COMPARATOR
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEfficiencyWhenEverythingIsForwarded
parameter_list|()
block|{
name|TopNKeyFilter
name|topNKeyFilter
init|=
operator|new
name|TopNKeyFilter
argument_list|(
literal|2
argument_list|,
name|TEST_KEY_WRAPPER_COMPARATOR
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|4
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|forwardingRatio
argument_list|()
argument_list|,
name|is
argument_list|(
literal|1.0f
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEfficiencyWhenOnlyOneIsForwarded
parameter_list|()
block|{
name|TopNKeyFilter
name|topNKeyFilter
init|=
operator|new
name|TopNKeyFilter
argument_list|(
literal|1
argument_list|,
name|TEST_KEY_WRAPPER_COMPARATOR
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|4
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topNKeyFilter
operator|.
name|forwardingRatio
argument_list|()
argument_list|,
name|is
argument_list|(
literal|1
operator|/
literal|5f
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDisabling
parameter_list|()
block|{
name|TopNKeyFilter
name|efficientFilter
init|=
operator|new
name|TopNKeyFilter
argument_list|(
literal|1
argument_list|,
name|TEST_KEY_WRAPPER_COMPARATOR
argument_list|)
decl_stmt|;
name|efficientFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|efficientFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|efficientFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|TopNKeyFilter
name|inefficientFilter
init|=
operator|new
name|TopNKeyFilter
argument_list|(
literal|1
argument_list|,
name|TEST_KEY_WRAPPER_COMPARATOR
argument_list|)
decl_stmt|;
name|inefficientFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|inefficientFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|inefficientFilter
operator|.
name|canForward
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|KeyWrapper
argument_list|,
name|TopNKeyFilter
argument_list|>
name|filters
init|=
operator|new
name|HashMap
argument_list|<
name|KeyWrapper
argument_list|,
name|TopNKeyFilter
argument_list|>
argument_list|()
block|{
block|{
name|put
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|100
argument_list|)
argument_list|,
name|efficientFilter
argument_list|)
expr_stmt|;
name|put
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|200
argument_list|)
argument_list|,
name|inefficientFilter
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|Set
argument_list|<
name|KeyWrapper
argument_list|>
name|disabled
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|checkTopNFilterEfficiency
argument_list|(
name|filters
argument_list|,
name|disabled
argument_list|,
literal|0.6f
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|disabled
argument_list|,
name|hasSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|disabled
argument_list|,
name|hasItem
argument_list|(
operator|new
name|TestKeyWrapper
argument_list|(
literal|200
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test implementation of KeyWrapper.    */
specifier|private
specifier|static
class|class
name|TestKeyWrapper
extends|extends
name|KeyWrapper
block|{
specifier|private
specifier|final
name|int
name|keyValue
decl_stmt|;
name|TestKeyWrapper
parameter_list|(
name|int
name|keyValue
parameter_list|)
block|{
name|this
operator|.
name|keyValue
operator|=
name|keyValue
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|getNewKey
parameter_list|(
name|Object
name|row
parameter_list|,
name|ObjectInspector
name|rowInspector
parameter_list|)
throws|throws
name|HiveException
block|{      }
annotation|@
name|Override
specifier|public
name|void
name|setHashKey
parameter_list|()
block|{      }
annotation|@
name|Override
specifier|public
name|KeyWrapper
name|copyKey
parameter_list|()
block|{
return|return
operator|new
name|TestKeyWrapper
argument_list|(
name|this
operator|.
name|keyValue
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|copyKey
parameter_list|(
name|KeyWrapper
name|oldWrapper
parameter_list|)
block|{      }
annotation|@
name|Override
specifier|public
name|Object
index|[]
name|getKeyArray
parameter_list|()
block|{
return|return
operator|new
name|Object
index|[
literal|0
index|]
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isCopy
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|TestKeyWrapper
name|that
init|=
operator|(
name|TestKeyWrapper
operator|)
name|o
decl_stmt|;
return|return
name|keyValue
operator|==
name|that
operator|.
name|keyValue
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|keyValue
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

