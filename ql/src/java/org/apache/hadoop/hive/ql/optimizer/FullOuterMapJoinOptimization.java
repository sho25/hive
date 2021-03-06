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
package|;
end_package

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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
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
name|SemanticException
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
name|MapJoinDesc
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
name|TableDesc
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
name|serde
operator|.
name|serdeConstants
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
name|SerDeUtils
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
name|TypeInfoUtils
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
name|base
operator|.
name|Joiner
import|;
end_import

begin_comment
comment|/**  * FULL OUTER MapJoin planning.  */
end_comment

begin_class
specifier|public
class|class
name|FullOuterMapJoinOptimization
block|{
name|FullOuterMapJoinOptimization
parameter_list|()
block|{   }
specifier|public
specifier|static
name|void
name|removeFilterMap
parameter_list|(
name|MapJoinDesc
name|mapJoinDesc
parameter_list|)
throws|throws
name|SemanticException
block|{
name|int
index|[]
index|[]
name|filterMaps
init|=
name|mapJoinDesc
operator|.
name|getFilterMap
argument_list|()
decl_stmt|;
if|if
condition|(
name|filterMaps
operator|==
literal|null
condition|)
block|{
return|return;
block|}
specifier|final
name|byte
name|posBigTable
init|=
operator|(
name|byte
operator|)
name|mapJoinDesc
operator|.
name|getPosBigTable
argument_list|()
decl_stmt|;
specifier|final
name|int
name|numAliases
init|=
name|mapJoinDesc
operator|.
name|getExprs
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TableDesc
argument_list|>
name|valueFilteredTblDescs
init|=
name|mapJoinDesc
operator|.
name|getValueFilteredTblDescs
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|numAliases
condition|;
name|pos
operator|++
control|)
block|{
if|if
condition|(
name|pos
operator|!=
name|posBigTable
condition|)
block|{
name|int
index|[]
name|filterMap
init|=
name|filterMaps
index|[
name|pos
index|]
decl_stmt|;
name|TableDesc
name|tableDesc
init|=
name|valueFilteredTblDescs
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
name|Properties
name|properties
init|=
name|tableDesc
operator|.
name|getProperties
argument_list|()
decl_stmt|;
name|String
name|columnNameProperty
init|=
name|properties
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|)
decl_stmt|;
name|String
name|columnNameDelimiter
init|=
name|properties
operator|.
name|containsKey
argument_list|(
name|serdeConstants
operator|.
name|COLUMN_NAME_DELIMITER
argument_list|)
condition|?
name|properties
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|COLUMN_NAME_DELIMITER
argument_list|)
else|:
name|String
operator|.
name|valueOf
argument_list|(
name|SerDeUtils
operator|.
name|COMMA
argument_list|)
decl_stmt|;
name|String
name|columnTypeProperty
init|=
name|properties
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columnNameList
decl_stmt|;
if|if
condition|(
name|columnNameProperty
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|columnNameList
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|columnNameList
operator|=
name|Arrays
operator|.
name|asList
argument_list|(
name|columnNameProperty
operator|.
name|split
argument_list|(
name|columnNameDelimiter
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|truncatedColumnNameList
init|=
name|columnNameList
operator|.
name|subList
argument_list|(
literal|0
argument_list|,
name|columnNameList
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
decl_stmt|;
name|String
name|truncatedColumnNameProperty
init|=
name|Joiner
operator|.
name|on
argument_list|(
name|columnNameDelimiter
argument_list|)
operator|.
name|join
argument_list|(
name|truncatedColumnNameList
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypeList
decl_stmt|;
if|if
condition|(
name|columnTypeProperty
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|columnTypeList
operator|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|columnTypeList
operator|=
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
argument_list|(
name|columnTypeProperty
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|columnTypeList
operator|.
name|get
argument_list|(
name|columnTypeList
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|shortTypeInfo
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Expecting filterTag smallint as last column type"
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|truncatedColumnTypeList
init|=
name|columnTypeList
operator|.
name|subList
argument_list|(
literal|0
argument_list|,
name|columnTypeList
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
decl_stmt|;
name|String
name|truncatedColumnTypeProperty
init|=
name|Joiner
operator|.
name|on
argument_list|(
literal|","
argument_list|)
operator|.
name|join
argument_list|(
name|truncatedColumnTypeList
argument_list|)
decl_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
name|truncatedColumnNameProperty
argument_list|)
expr_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|truncatedColumnTypeProperty
argument_list|)
expr_stmt|;
block|}
block|}
name|mapJoinDesc
operator|.
name|setFilterMap
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

