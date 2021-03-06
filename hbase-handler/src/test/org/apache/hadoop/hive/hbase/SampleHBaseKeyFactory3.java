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
name|hbase
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|Arrays
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
name|List
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|filter
operator|.
name|BinaryComparator
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
name|hbase
operator|.
name|filter
operator|.
name|CompareFilter
operator|.
name|CompareOp
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
name|hbase
operator|.
name|filter
operator|.
name|Filter
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
name|hbase
operator|.
name|filter
operator|.
name|FilterList
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
name|hbase
operator|.
name|filter
operator|.
name|FilterList
operator|.
name|Operator
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
name|hbase
operator|.
name|filter
operator|.
name|RowFilter
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
name|hbase
operator|.
name|ColumnMappings
operator|.
name|ColumnMapping
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
name|index
operator|.
name|IndexSearchCondition
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
name|ExprNodeDesc
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
name|Deserializer
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
name|StructTypeInfo
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
name|mapred
operator|.
name|JobConf
import|;
end_import

begin_comment
comment|/**  * Simple extension of {@link SampleHBaseKeyFactory2} with exception of using filters instead of start  * and stop keys  * */
end_comment

begin_class
specifier|public
class|class
name|SampleHBaseKeyFactory3
extends|extends
name|SampleHBaseKeyFactory2
block|{
annotation|@
name|Override
specifier|public
name|DecomposedPredicate
name|decomposePredicate
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|Deserializer
name|deserializer
parameter_list|,
name|ExprNodeDesc
name|predicate
parameter_list|)
block|{
name|SampleHBasePredicateDecomposer
name|decomposedPredicate
init|=
operator|new
name|SampleHBasePredicateDecomposer
argument_list|(
name|keyMapping
argument_list|)
decl_stmt|;
return|return
name|decomposedPredicate
operator|.
name|decomposePredicate
argument_list|(
name|keyMapping
operator|.
name|columnName
argument_list|,
name|predicate
argument_list|)
return|;
block|}
block|}
end_class

begin_class
class|class
name|SampleHBasePredicateDecomposer
extends|extends
name|AbstractHBaseKeyPredicateDecomposer
block|{
specifier|private
specifier|static
specifier|final
name|int
name|FIXED_LENGTH
init|=
literal|10
decl_stmt|;
specifier|private
name|ColumnMapping
name|keyMapping
decl_stmt|;
name|SampleHBasePredicateDecomposer
parameter_list|(
name|ColumnMapping
name|keyMapping
parameter_list|)
block|{
name|this
operator|.
name|keyMapping
operator|=
name|keyMapping
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|HBaseScanRange
name|getScanRange
parameter_list|(
name|List
argument_list|<
name|IndexSearchCondition
argument_list|>
name|searchConditions
parameter_list|)
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|IndexSearchCondition
argument_list|>
argument_list|>
name|fieldConds
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|IndexSearchCondition
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexSearchCondition
name|condition
range|:
name|searchConditions
control|)
block|{
name|String
name|fieldName
init|=
name|condition
operator|.
name|getFields
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|List
argument_list|<
name|IndexSearchCondition
argument_list|>
name|fieldCond
init|=
name|fieldConds
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldCond
operator|==
literal|null
condition|)
block|{
name|fieldConds
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|fieldCond
operator|=
operator|new
name|ArrayList
argument_list|<
name|IndexSearchCondition
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|fieldCond
operator|.
name|add
argument_list|(
name|condition
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Filter
argument_list|>
name|filters
init|=
operator|new
name|ArrayList
argument_list|<
name|Filter
argument_list|>
argument_list|()
decl_stmt|;
name|HBaseScanRange
name|range
init|=
operator|new
name|HBaseScanRange
argument_list|()
decl_stmt|;
name|StructTypeInfo
name|type
init|=
operator|(
name|StructTypeInfo
operator|)
name|keyMapping
operator|.
name|columnType
decl_stmt|;
for|for
control|(
name|String
name|name
range|:
name|type
operator|.
name|getAllStructFieldNames
argument_list|()
control|)
block|{
name|List
argument_list|<
name|IndexSearchCondition
argument_list|>
name|fieldCond
init|=
name|fieldConds
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldCond
operator|==
literal|null
operator|||
name|fieldCond
operator|.
name|size
argument_list|()
operator|>
literal|2
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|IndexSearchCondition
name|condition
range|:
name|fieldCond
control|)
block|{
if|if
condition|(
name|condition
operator|.
name|getConstantDesc
argument_list|()
operator|.
name|getValue
argument_list|()
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|String
name|comparisonOp
init|=
name|condition
operator|.
name|getComparisonOp
argument_list|()
decl_stmt|;
name|String
name|constantVal
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|condition
operator|.
name|getConstantDesc
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|valueAsBytes
init|=
name|toBinary
argument_list|(
name|constantVal
argument_list|,
name|FIXED_LENGTH
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|comparisonOp
operator|.
name|endsWith
argument_list|(
literal|"UDFOPEqualOrGreaterThan"
argument_list|)
condition|)
block|{
name|filters
operator|.
name|add
argument_list|(
operator|new
name|RowFilter
argument_list|(
name|CompareOp
operator|.
name|GREATER_OR_EQUAL
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|valueAsBytes
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|comparisonOp
operator|.
name|endsWith
argument_list|(
literal|"UDFOPGreaterThan"
argument_list|)
condition|)
block|{
name|filters
operator|.
name|add
argument_list|(
operator|new
name|RowFilter
argument_list|(
name|CompareOp
operator|.
name|GREATER
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|valueAsBytes
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|comparisonOp
operator|.
name|endsWith
argument_list|(
literal|"UDFOPEqualOrLessThan"
argument_list|)
condition|)
block|{
name|filters
operator|.
name|add
argument_list|(
operator|new
name|RowFilter
argument_list|(
name|CompareOp
operator|.
name|LESS_OR_EQUAL
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|valueAsBytes
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|comparisonOp
operator|.
name|endsWith
argument_list|(
literal|"UDFOPLessThan"
argument_list|)
condition|)
block|{
name|filters
operator|.
name|add
argument_list|(
operator|new
name|RowFilter
argument_list|(
name|CompareOp
operator|.
name|LESS
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|valueAsBytes
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|comparisonOp
operator|+
literal|" is not a supported comparison operator"
argument_list|)
throw|;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|filters
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|range
operator|.
name|addFilter
argument_list|(
operator|new
name|FilterList
argument_list|(
name|Operator
operator|.
name|MUST_PASS_ALL
argument_list|,
name|filters
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|range
return|;
block|}
specifier|private
name|byte
index|[]
name|toBinary
parameter_list|(
name|String
name|value
parameter_list|,
name|int
name|max
parameter_list|,
name|boolean
name|end
parameter_list|,
name|boolean
name|nextBA
parameter_list|)
block|{
return|return
name|toBinary
argument_list|(
name|value
operator|.
name|getBytes
argument_list|()
argument_list|,
name|max
argument_list|,
name|end
argument_list|,
name|nextBA
argument_list|)
return|;
block|}
specifier|private
name|byte
index|[]
name|toBinary
parameter_list|(
name|byte
index|[]
name|value
parameter_list|,
name|int
name|max
parameter_list|,
name|boolean
name|end
parameter_list|,
name|boolean
name|nextBA
parameter_list|)
block|{
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
name|max
operator|+
literal|1
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|value
argument_list|,
literal|0
argument_list|,
name|bytes
argument_list|,
literal|0
argument_list|,
name|Math
operator|.
name|min
argument_list|(
name|value
operator|.
name|length
argument_list|,
name|max
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|end
condition|)
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|bytes
argument_list|,
name|value
operator|.
name|length
argument_list|,
name|max
argument_list|,
operator|(
name|byte
operator|)
literal|0xff
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nextBA
condition|)
block|{
name|bytes
index|[
name|max
index|]
operator|=
literal|0x01
expr_stmt|;
block|}
return|return
name|bytes
return|;
block|}
block|}
end_class

end_unit

