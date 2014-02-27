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
name|Arrays
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
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
name|hive
operator|.
name|serde2
operator|.
name|io
operator|.
name|ShortWritable
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
name|TestMapJoinRowContainer
block|{
annotation|@
name|Test
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|Exception
block|{
name|MapJoinRowContainer
name|container1
init|=
operator|new
name|MapJoinEagerRowContainer
argument_list|()
decl_stmt|;
name|container1
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
operator|new
name|Text
argument_list|(
literal|"f0"
argument_list|)
block|,
literal|null
block|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|0xf
argument_list|)
block|}
argument_list|)
expr_stmt|;
name|container1
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|null
block|,
operator|new
name|Text
argument_list|(
literal|"f1"
argument_list|)
block|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|0xf
argument_list|)
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|container1
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|null
block|,
literal|null
block|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|0xf
argument_list|)
block|}
argument_list|)
expr_stmt|;
name|container1
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Object
index|[]
block|{
operator|new
name|Text
argument_list|(
literal|"f0"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"f1"
argument_list|)
block|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|0x1
argument_list|)
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|MapJoinRowContainer
name|container2
init|=
name|Utilities
operator|.
name|serde
argument_list|(
name|container1
argument_list|,
literal|"f0,f1,filter"
argument_list|,
literal|"string,string,smallint"
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|testEquality
argument_list|(
name|container1
argument_list|,
name|container2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|container1
operator|.
name|rowCount
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|container2
operator|.
name|getAliasFilter
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

