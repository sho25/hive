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
name|dataset
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
name|Set
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
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  * TestDatasetParser: test class for DataSetParser  */
end_comment

begin_class
specifier|public
class|class
name|TestDatasetParser
block|{
annotation|@
name|Test
specifier|public
name|void
name|testParseOne
parameter_list|()
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|expected
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"mydataset"
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|DatasetParser
operator|.
name|parseDatasetsFromLine
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%smydataset"
argument_list|,
name|DatasetParser
operator|.
name|DATASET_PREFIX
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParseMultiple
parameter_list|()
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|expected
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"one"
argument_list|,
literal|"two"
argument_list|,
literal|"three"
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|DatasetParser
operator|.
name|parseDatasetsFromLine
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%sone,two,three"
argument_list|,
name|DatasetParser
operator|.
name|DATASET_PREFIX
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParseUnique
parameter_list|()
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|expected
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"one"
argument_list|,
literal|"two"
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|DatasetParser
operator|.
name|parseDatasetsFromLine
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%sone,one,two"
argument_list|,
name|DatasetParser
operator|.
name|DATASET_PREFIX
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParseNone
parameter_list|()
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|DatasetParser
operator|.
name|parseDatasetsFromLine
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s"
argument_list|,
name|DatasetParser
operator|.
name|DATASET_PREFIX
argument_list|)
argument_list|)
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

