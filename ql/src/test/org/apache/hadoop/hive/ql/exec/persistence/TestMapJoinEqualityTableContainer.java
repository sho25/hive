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
name|persistence
package|;
end_package

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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
import|;
end_import

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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|Text
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

begin_class
specifier|public
class|class
name|TestMapJoinEqualityTableContainer
block|{
specifier|private
specifier|static
specifier|final
name|MapJoinKeyObject
name|KEY1
init|=
operator|new
name|MapJoinKeyObject
argument_list|(
operator|new
name|Object
index|[]
block|{
operator|new
name|Text
argument_list|(
literal|"key1"
argument_list|)
block|}
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|MapJoinKeyObject
name|KEY2
init|=
operator|new
name|MapJoinKeyObject
argument_list|(
operator|new
name|Object
index|[]
block|{
operator|new
name|Text
argument_list|(
literal|"key2"
argument_list|)
block|}
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|MapJoinKeyObject
name|KEY3
init|=
operator|new
name|MapJoinKeyObject
argument_list|(
operator|new
name|Object
index|[]
block|{
operator|new
name|Text
argument_list|(
literal|"key3"
argument_list|)
block|}
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|MapJoinKeyObject
name|KEY4
init|=
operator|new
name|MapJoinKeyObject
argument_list|(
operator|new
name|Object
index|[]
block|{
operator|new
name|Text
argument_list|(
literal|"key4"
argument_list|)
block|}
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Object
index|[]
name|VALUE
init|=
operator|new
name|Object
index|[]
block|{
operator|new
name|Text
argument_list|(
literal|"value"
argument_list|)
block|}
decl_stmt|;
specifier|private
name|HashMapWrapper
name|container
decl_stmt|;
specifier|private
name|MapJoinRowContainer
name|rowContainer
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|rowContainer
operator|=
operator|new
name|MapJoinEagerRowContainer
argument_list|()
expr_stmt|;
name|rowContainer
operator|.
name|addRow
argument_list|(
name|VALUE
argument_list|)
expr_stmt|;
name|container
operator|=
operator|new
name|HashMapWrapper
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testContainerBasics
parameter_list|()
throws|throws
name|Exception
block|{
name|container
operator|.
name|put
argument_list|(
name|KEY1
argument_list|,
name|rowContainer
argument_list|)
expr_stmt|;
name|container
operator|.
name|put
argument_list|(
name|KEY2
argument_list|,
name|rowContainer
argument_list|)
expr_stmt|;
name|container
operator|.
name|put
argument_list|(
name|KEY3
argument_list|,
name|rowContainer
argument_list|)
expr_stmt|;
name|container
operator|.
name|put
argument_list|(
name|KEY4
argument_list|,
name|rowContainer
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|container
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|MapJoinKey
argument_list|,
name|MapJoinRowContainer
argument_list|>
name|localContainer
init|=
operator|new
name|HashMap
argument_list|<
name|MapJoinKey
argument_list|,
name|MapJoinRowContainer
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|MapJoinKey
argument_list|,
name|MapJoinRowContainer
argument_list|>
name|entry
range|:
name|container
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|localContainer
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Utilities
operator|.
name|testEquality
argument_list|(
name|container
operator|.
name|get
argument_list|(
name|KEY1
argument_list|)
argument_list|,
name|localContainer
operator|.
name|get
argument_list|(
name|KEY1
argument_list|)
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|testEquality
argument_list|(
name|container
operator|.
name|get
argument_list|(
name|KEY2
argument_list|)
argument_list|,
name|localContainer
operator|.
name|get
argument_list|(
name|KEY2
argument_list|)
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|testEquality
argument_list|(
name|container
operator|.
name|get
argument_list|(
name|KEY3
argument_list|)
argument_list|,
name|localContainer
operator|.
name|get
argument_list|(
name|KEY3
argument_list|)
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|testEquality
argument_list|(
name|container
operator|.
name|get
argument_list|(
name|KEY4
argument_list|)
argument_list|,
name|localContainer
operator|.
name|get
argument_list|(
name|KEY4
argument_list|)
argument_list|)
expr_stmt|;
name|container
operator|.
name|clear
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|container
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|container
operator|.
name|entrySet
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

