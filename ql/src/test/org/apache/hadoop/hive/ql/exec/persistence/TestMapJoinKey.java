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
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestMapJoinKey
block|{
annotation|@
name|Test
specifier|public
name|void
name|testEqualityHashCode
parameter_list|()
throws|throws
name|Exception
block|{
name|MapJoinKeyObject
name|key1
init|=
operator|new
name|MapJoinKeyObject
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"key"
block|}
argument_list|)
decl_stmt|;
name|MapJoinKeyObject
name|key2
init|=
operator|new
name|MapJoinKeyObject
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"key"
block|}
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|testEquality
argument_list|(
name|key1
argument_list|,
name|key2
argument_list|)
expr_stmt|;
name|key1
operator|=
operator|new
name|MapJoinKeyObject
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|148
block|,
literal|null
block|}
argument_list|)
expr_stmt|;
name|key2
operator|=
operator|new
name|MapJoinKeyObject
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|148
block|,
literal|null
block|}
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|testEquality
argument_list|(
name|key1
argument_list|,
name|key2
argument_list|)
expr_stmt|;
name|key1
operator|=
operator|new
name|MapJoinKeyObject
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|null
block|,
literal|"key1"
block|}
argument_list|)
expr_stmt|;
name|key2
operator|=
operator|new
name|MapJoinKeyObject
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|null
block|,
literal|"key2"
block|}
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|key1
operator|.
name|equals
argument_list|(
name|key2
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|Exception
block|{
name|MapJoinKeyObject
name|key1
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
literal|"field0"
argument_list|)
block|,
literal|null
block|,
operator|new
name|Text
argument_list|(
literal|"field2"
argument_list|)
block|}
argument_list|)
decl_stmt|;
name|MapJoinKeyObject
name|key2
init|=
name|Utilities
operator|.
name|serde
argument_list|(
name|key1
argument_list|,
literal|"f0,f1,f2"
argument_list|,
literal|"string,string,string"
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|testEquality
argument_list|(
name|key1
argument_list|,
name|key2
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

