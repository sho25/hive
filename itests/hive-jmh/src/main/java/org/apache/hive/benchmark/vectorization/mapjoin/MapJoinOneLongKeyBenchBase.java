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
name|hive
operator|.
name|benchmark
operator|.
name|vectorization
operator|.
name|mapjoin
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
name|mapjoin
operator|.
name|MapJoinTestConfig
operator|.
name|MapJoinTestImplementation
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
name|vector
operator|.
name|mapjoin
operator|.
name|MapJoinTestDescription
operator|.
name|SmallTableGenerationParameters
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
name|vector
operator|.
name|mapjoin
operator|.
name|MapJoinTestDescription
operator|.
name|SmallTableGenerationParameters
operator|.
name|ValueOption
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
name|plan
operator|.
name|VectorMapJoinDesc
operator|.
name|VectorMapJoinVariation
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
name|typeinfo
operator|.
name|TypeInfo
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
name|typeinfo
operator|.
name|TypeInfoFactory
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|MapJoinOneLongKeyBenchBase
extends|extends
name|AbstractMapJoin
block|{
specifier|public
name|void
name|doSetup
parameter_list|(
name|VectorMapJoinVariation
name|vectorMapJoinVariation
parameter_list|,
name|MapJoinTestImplementation
name|mapJoinImplementation
parameter_list|)
throws|throws
name|Exception
block|{
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|long
name|seed
init|=
literal|2543
decl_stmt|;
name|int
name|rowCount
init|=
literal|10000000
decl_stmt|;
comment|// 10,000,000.
name|String
index|[]
name|bigTableColumnNames
init|=
operator|new
name|String
index|[]
block|{
literal|"number1"
block|}
decl_stmt|;
name|TypeInfo
index|[]
name|bigTableTypeInfos
init|=
operator|new
name|TypeInfo
index|[]
block|{
name|TypeInfoFactory
operator|.
name|longTypeInfo
block|}
decl_stmt|;
name|int
index|[]
name|bigTableKeyColumnNums
init|=
operator|new
name|int
index|[]
block|{
literal|0
block|}
decl_stmt|;
name|String
index|[]
name|smallTableValueColumnNames
init|=
operator|new
name|String
index|[]
block|{
literal|"sv1"
block|,
literal|"sv2"
block|}
decl_stmt|;
name|TypeInfo
index|[]
name|smallTableValueTypeInfos
init|=
operator|new
name|TypeInfo
index|[]
block|{
name|TypeInfoFactory
operator|.
name|dateTypeInfo
block|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
block|}
decl_stmt|;
name|int
index|[]
name|bigTableRetainColumnNums
init|=
operator|new
name|int
index|[]
block|{
literal|0
block|}
decl_stmt|;
name|int
index|[]
name|smallTableRetainKeyColumnNums
init|=
operator|new
name|int
index|[]
block|{}
decl_stmt|;
name|int
index|[]
name|smallTableRetainValueColumnNums
init|=
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|1
block|}
decl_stmt|;
name|SmallTableGenerationParameters
name|smallTableGenerationParameters
init|=
operator|new
name|SmallTableGenerationParameters
argument_list|()
decl_stmt|;
name|smallTableGenerationParameters
operator|.
name|setValueOption
argument_list|(
name|ValueOption
operator|.
name|ONLY_ONE
argument_list|)
expr_stmt|;
name|setupMapJoin
argument_list|(
name|hiveConf
argument_list|,
name|seed
argument_list|,
name|rowCount
argument_list|,
name|vectorMapJoinVariation
argument_list|,
name|mapJoinImplementation
argument_list|,
name|bigTableColumnNames
argument_list|,
name|bigTableTypeInfos
argument_list|,
name|bigTableKeyColumnNums
argument_list|,
name|smallTableValueColumnNames
argument_list|,
name|smallTableValueTypeInfos
argument_list|,
name|bigTableRetainColumnNums
argument_list|,
name|smallTableRetainKeyColumnNums
argument_list|,
name|smallTableRetainValueColumnNums
argument_list|,
name|smallTableGenerationParameters
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

