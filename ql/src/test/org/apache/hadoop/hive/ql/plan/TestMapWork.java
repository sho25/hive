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
name|plan
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
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
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|fs
operator|.
name|Path
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_class
specifier|public
class|class
name|TestMapWork
block|{
annotation|@
name|Test
specifier|public
name|void
name|testGetAndSetConsistency
parameter_list|()
block|{
name|MapWork
name|mw
init|=
operator|new
name|MapWork
argument_list|()
decl_stmt|;
name|LinkedHashMap
argument_list|<
name|Path
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|pathToAliases
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|pathToAliases
operator|.
name|put
argument_list|(
operator|new
name|Path
argument_list|(
literal|"p0"
argument_list|)
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"a1"
argument_list|,
literal|"a2"
argument_list|)
argument_list|)
expr_stmt|;
name|mw
operator|.
name|setPathToAliases
argument_list|(
name|pathToAliases
argument_list|)
expr_stmt|;
name|LinkedHashMap
argument_list|<
name|Path
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|pta
init|=
name|mw
operator|.
name|getPathToAliases
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|pathToAliases
argument_list|,
name|pta
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPath
parameter_list|()
block|{
name|Path
name|p1
init|=
operator|new
name|Path
argument_list|(
literal|"hdfs://asd/asd"
argument_list|)
decl_stmt|;
name|Path
name|p2
init|=
operator|new
name|Path
argument_list|(
literal|"hdfs://asd/asd/"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|p1
argument_list|,
name|p2
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

