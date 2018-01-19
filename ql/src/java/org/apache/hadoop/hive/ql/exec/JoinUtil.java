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
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|persistence
operator|.
name|RowContainer
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
name|io
operator|.
name|HiveSequenceFileOutputFormat
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
name|ql
operator|.
name|plan
operator|.
name|JoinDesc
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
name|AbstractSerDe
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
name|SerDeException
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
name|hive
operator|.
name|serde2
operator|.
name|lazybinary
operator|.
name|LazyBinarySerDe
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
name|ObjectInspectorFactory
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
name|ObjectInspectorUtils
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
name|ObjectInspectorUtils
operator|.
name|ObjectInspectorCopyOption
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
name|PrimitiveObjectInspector
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
name|StructField
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
name|StructObjectInspector
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
name|mapred
operator|.
name|Reporter
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
name|SequenceFileInputFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|ReflectionUtil
import|;
end_import

begin_class
specifier|public
class|class
name|JoinUtil
block|{
comment|/**    * Represents the join result between two tables    */
specifier|public
specifier|static
enum|enum
name|JoinResult
block|{
name|MATCH
block|,
comment|// A match is found
name|NOMATCH
block|,
comment|// No match is found, and the current row will be dropped
name|SPILL
comment|// The current row has been spilled to disk, as the join is postponed
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|ObjectInspector
argument_list|>
index|[]
name|getObjectInspectorsFromEvaluators
parameter_list|(
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
index|[]
name|exprEntries
parameter_list|,
name|ObjectInspector
index|[]
name|inputObjInspector
parameter_list|,
name|int
name|posBigTableAlias
parameter_list|,
name|int
name|tagLen
parameter_list|)
throws|throws
name|HiveException
block|{
name|List
argument_list|<
name|ObjectInspector
argument_list|>
index|[]
name|result
init|=
operator|new
name|List
index|[
name|tagLen
index|]
decl_stmt|;
name|int
name|iterate
init|=
name|Math
operator|.
name|min
argument_list|(
name|exprEntries
operator|.
name|length
argument_list|,
name|inputObjInspector
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
name|alias
init|=
literal|0
init|;
name|alias
operator|<
name|iterate
condition|;
name|alias
operator|++
control|)
block|{
name|ObjectInspector
name|inputOI
init|=
name|inputObjInspector
index|[
name|alias
index|]
decl_stmt|;
comment|// For vectorized reduce-side operators getting inputs from a reduce sink,
comment|// the row object inspector will get a flattened version of the object inspector
comment|// where the nested key/value structs are replaced with a single struct:
comment|// Example: { key: { reducesinkkey0:int }, value: { _col0:int, _col1:int, .. } }
comment|// Would get converted to the following for a vectorized input:
comment|//   { 'key.reducesinkkey0':int, 'value._col0':int, 'value._col1':int, .. }
comment|// The ExprNodeEvaluator initialzation below gets broken with the flattened
comment|// object inpsectors, so convert it back to the a form that contains the
comment|// nested key/value structs.
name|inputOI
operator|=
name|unflattenObjInspector
argument_list|(
name|inputOI
argument_list|)
expr_stmt|;
if|if
condition|(
name|alias
operator|==
operator|(
name|byte
operator|)
name|posBigTableAlias
operator|||
name|exprEntries
index|[
name|alias
index|]
operator|==
literal|null
operator|||
name|inputOI
operator|==
literal|null
condition|)
block|{
comment|// skip the driver and directly loadable tables
continue|continue;
block|}
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
name|exprList
init|=
name|exprEntries
index|[
name|alias
index|]
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|fieldOIList
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|exprList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|fieldOIList
operator|.
name|add
argument_list|(
name|exprList
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|initialize
argument_list|(
name|inputOI
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|result
index|[
name|alias
index|]
operator|=
name|fieldOIList
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|ObjectInspector
argument_list|>
index|[]
name|getStandardObjectInspectors
parameter_list|(
name|List
argument_list|<
name|ObjectInspector
argument_list|>
index|[]
name|aliasToObjectInspectors
parameter_list|,
name|int
name|posBigTableAlias
parameter_list|,
name|int
name|tagLen
parameter_list|)
block|{
name|List
argument_list|<
name|ObjectInspector
argument_list|>
index|[]
name|result
init|=
operator|new
name|List
index|[
name|tagLen
index|]
decl_stmt|;
for|for
control|(
name|byte
name|alias
init|=
literal|0
init|;
name|alias
operator|<
name|aliasToObjectInspectors
operator|.
name|length
condition|;
name|alias
operator|++
control|)
block|{
comment|//get big table
if|if
condition|(
name|alias
operator|==
operator|(
name|byte
operator|)
name|posBigTableAlias
operator|||
name|aliasToObjectInspectors
index|[
name|alias
index|]
operator|==
literal|null
condition|)
block|{
comment|//skip the big tables
continue|continue;
block|}
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|oiList
init|=
name|aliasToObjectInspectors
index|[
name|alias
index|]
decl_stmt|;
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|fieldOIList
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|oiList
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|oiList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|fieldOIList
operator|.
name|add
argument_list|(
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|oiList
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|result
index|[
name|alias
index|]
operator|=
name|fieldOIList
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
specifier|static
name|int
name|populateJoinKeyValue
parameter_list|(
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
index|[]
name|outMap
parameter_list|,
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|inputMap
parameter_list|,
name|int
name|posBigTableAlias
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
name|populateJoinKeyValue
argument_list|(
name|outMap
argument_list|,
name|inputMap
argument_list|,
literal|null
argument_list|,
name|posBigTableAlias
argument_list|,
name|conf
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|int
name|populateJoinKeyValue
parameter_list|(
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
index|[]
name|outMap
parameter_list|,
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|inputMap
parameter_list|,
name|Byte
index|[]
name|order
parameter_list|,
name|int
name|posBigTableAlias
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{
name|int
name|total
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|e
range|:
name|inputMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|e
operator|.
name|getValue
argument_list|()
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|Byte
name|key
init|=
name|order
operator|==
literal|null
condition|?
name|e
operator|.
name|getKey
argument_list|()
else|:
name|order
index|[
name|e
operator|.
name|getKey
argument_list|()
index|]
decl_stmt|;
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
name|valueFields
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeEvaluator
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|expr
range|:
name|e
operator|.
name|getValue
argument_list|()
control|)
block|{
if|if
condition|(
name|key
operator|==
operator|(
name|byte
operator|)
name|posBigTableAlias
condition|)
block|{
name|valueFields
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|valueFields
operator|.
name|add
argument_list|(
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|expr
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|outMap
index|[
name|key
index|]
operator|=
name|valueFields
expr_stmt|;
name|total
operator|+=
name|valueFields
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
return|return
name|total
return|;
block|}
comment|/**    * Return the key as a standard object. StandardObject can be inspected by a    * standard ObjectInspector.    */
specifier|public
specifier|static
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|computeKeys
parameter_list|(
name|Object
name|row
parameter_list|,
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
name|keyFields
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|keyFieldsOI
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Compute the keys
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|nr
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|keyFields
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|keyFields
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|nr
operator|.
name|add
argument_list|(
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|keyFields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
argument_list|,
name|keyFieldsOI
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|nr
return|;
block|}
comment|/**    * Return the value as a standard object. StandardObject can be inspected by a    * standard ObjectInspector.    */
specifier|public
specifier|static
name|Object
index|[]
name|computeMapJoinValues
parameter_list|(
name|Object
name|row
parameter_list|,
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
name|valueFields
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|valueFieldsOI
parameter_list|,
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
name|filters
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|filtersOI
parameter_list|,
name|int
index|[]
name|filterMap
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Compute the keys
name|Object
index|[]
name|nr
decl_stmt|;
if|if
condition|(
name|filterMap
operator|!=
literal|null
condition|)
block|{
name|nr
operator|=
operator|new
name|Object
index|[
name|valueFields
operator|.
name|size
argument_list|()
operator|+
literal|1
index|]
expr_stmt|;
comment|// add whether the row is filtered or not.
name|nr
index|[
name|valueFields
operator|.
name|size
argument_list|()
index|]
operator|=
operator|new
name|ShortWritable
argument_list|(
name|isFiltered
argument_list|(
name|row
argument_list|,
name|filters
argument_list|,
name|filtersOI
argument_list|,
name|filterMap
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|nr
operator|=
operator|new
name|Object
index|[
name|valueFields
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|valueFields
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|nr
index|[
name|i
index|]
operator|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|valueFields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
argument_list|,
name|valueFieldsOI
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
expr_stmt|;
block|}
return|return
name|nr
return|;
block|}
comment|/**    * Return the value as a standard object. StandardObject can be inspected by a    * standard ObjectInspector.    * If it would be tagged by filter, reserve one more slot for that.    * outValues can be passed in to avoid allocation    */
specifier|public
specifier|static
name|List
argument_list|<
name|Object
argument_list|>
name|computeValues
parameter_list|(
name|Object
name|row
parameter_list|,
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
name|valueFields
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|valueFieldsOI
parameter_list|,
name|boolean
name|hasFilter
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Compute the values
name|int
name|reserve
init|=
name|hasFilter
condition|?
name|valueFields
operator|.
name|size
argument_list|()
operator|+
literal|1
else|:
name|valueFields
operator|.
name|size
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|nr
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|reserve
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|valueFields
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|nr
operator|.
name|add
argument_list|(
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|valueFields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
argument_list|,
name|valueFieldsOI
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|nr
return|;
block|}
specifier|private
specifier|static
specifier|final
name|short
index|[]
name|MASKS
decl_stmt|;
static|static
block|{
name|int
name|num
init|=
literal|32
decl_stmt|;
name|MASKS
operator|=
operator|new
name|short
index|[
name|num
index|]
expr_stmt|;
name|MASKS
index|[
literal|0
index|]
operator|=
literal|1
expr_stmt|;
for|for
control|(
name|int
name|idx
init|=
literal|1
init|;
name|idx
operator|<
name|num
condition|;
name|idx
operator|++
control|)
block|{
name|MASKS
index|[
name|idx
index|]
operator|=
call|(
name|short
call|)
argument_list|(
literal|2
operator|*
name|MASKS
index|[
name|idx
operator|-
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Returns true if the row does not pass through filters.    */
specifier|protected
specifier|static
name|boolean
name|isFiltered
parameter_list|(
name|Object
name|row
parameter_list|,
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
name|filters
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|filtersOIs
parameter_list|)
throws|throws
name|HiveException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|filters
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ExprNodeEvaluator
name|evaluator
init|=
name|filters
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Object
name|condition
init|=
name|evaluator
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|Boolean
name|result
init|=
call|(
name|Boolean
call|)
argument_list|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|filtersOIs
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|condition
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
operator|||
operator|!
name|result
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Returns true if the row does not pass through filters.    */
specifier|protected
specifier|static
name|short
name|isFiltered
parameter_list|(
name|Object
name|row
parameter_list|,
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
name|filters
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|ois
parameter_list|,
name|int
index|[]
name|filterMap
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// apply join filters on the row.
name|short
name|ret
init|=
literal|0
decl_stmt|;
name|int
name|j
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|filterMap
operator|.
name|length
condition|;
name|i
operator|+=
literal|2
control|)
block|{
name|int
name|tag
init|=
name|filterMap
index|[
name|i
index|]
decl_stmt|;
name|int
name|length
init|=
name|filterMap
index|[
name|i
operator|+
literal|1
index|]
decl_stmt|;
name|boolean
name|passed
init|=
literal|true
decl_stmt|;
for|for
control|(
init|;
name|length
operator|>
literal|0
condition|;
name|length
operator|--
operator|,
name|j
operator|++
control|)
block|{
if|if
condition|(
name|passed
condition|)
block|{
name|Object
name|condition
init|=
name|filters
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|Boolean
name|result
init|=
call|(
name|Boolean
call|)
argument_list|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|ois
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|condition
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
operator|||
operator|!
name|result
condition|)
block|{
name|passed
operator|=
literal|false
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|passed
condition|)
block|{
name|ret
operator||=
name|MASKS
index|[
name|tag
index|]
expr_stmt|;
block|}
block|}
return|return
name|ret
return|;
block|}
specifier|protected
specifier|static
name|boolean
name|isFiltered
parameter_list|(
name|short
name|filter
parameter_list|,
name|int
name|tag
parameter_list|)
block|{
return|return
operator|(
name|filter
operator|&
name|MASKS
index|[
name|tag
index|]
operator|)
operator|!=
literal|0
return|;
block|}
specifier|protected
specifier|static
name|boolean
name|hasAnyFiltered
parameter_list|(
name|short
name|tag
parameter_list|)
block|{
return|return
name|tag
operator|!=
literal|0
return|;
block|}
specifier|public
specifier|static
name|TableDesc
name|getSpillTableDesc
parameter_list|(
name|Byte
name|alias
parameter_list|,
name|TableDesc
index|[]
name|spillTableDesc
parameter_list|,
name|JoinDesc
name|conf
parameter_list|,
name|boolean
name|noFilter
parameter_list|)
block|{
if|if
condition|(
name|spillTableDesc
operator|==
literal|null
operator|||
name|spillTableDesc
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|spillTableDesc
operator|=
name|initSpillTables
argument_list|(
name|conf
argument_list|,
name|noFilter
argument_list|)
expr_stmt|;
block|}
return|return
name|spillTableDesc
index|[
name|alias
index|]
return|;
block|}
specifier|public
specifier|static
name|AbstractSerDe
name|getSpillSerDe
parameter_list|(
name|byte
name|alias
parameter_list|,
name|TableDesc
index|[]
name|spillTableDesc
parameter_list|,
name|JoinDesc
name|conf
parameter_list|,
name|boolean
name|noFilter
parameter_list|)
block|{
name|TableDesc
name|desc
init|=
name|getSpillTableDesc
argument_list|(
name|alias
argument_list|,
name|spillTableDesc
argument_list|,
name|conf
argument_list|,
name|noFilter
argument_list|)
decl_stmt|;
if|if
condition|(
name|desc
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|AbstractSerDe
name|sd
init|=
operator|(
name|AbstractSerDe
operator|)
name|ReflectionUtil
operator|.
name|newInstance
argument_list|(
name|desc
operator|.
name|getDeserializerClass
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|sd
argument_list|,
literal|null
argument_list|,
name|desc
operator|.
name|getProperties
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
name|sd
return|;
block|}
specifier|public
specifier|static
name|TableDesc
index|[]
name|initSpillTables
parameter_list|(
name|JoinDesc
name|conf
parameter_list|,
name|boolean
name|noFilter
parameter_list|)
block|{
name|int
name|tagLen
init|=
name|conf
operator|.
name|getTagLength
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|exprs
init|=
name|conf
operator|.
name|getExprs
argument_list|()
decl_stmt|;
name|TableDesc
index|[]
name|spillTableDesc
init|=
operator|new
name|TableDesc
index|[
name|tagLen
index|]
decl_stmt|;
for|for
control|(
name|int
name|tag
init|=
literal|0
init|;
name|tag
operator|<
name|exprs
operator|.
name|size
argument_list|()
condition|;
name|tag
operator|++
control|)
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|valueCols
init|=
name|exprs
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|tag
argument_list|)
decl_stmt|;
name|int
name|columnSize
init|=
name|valueCols
operator|.
name|size
argument_list|()
decl_stmt|;
name|StringBuilder
name|colNames
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|StringBuilder
name|colTypes
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|columnSize
operator|<=
literal|0
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|columnSize
condition|;
name|k
operator|++
control|)
block|{
name|String
name|newColName
init|=
name|tag
operator|+
literal|"_VALUE_"
operator|+
name|k
decl_stmt|;
comment|// any name, it does not
comment|// matter.
name|colNames
operator|.
name|append
argument_list|(
name|newColName
argument_list|)
expr_stmt|;
name|colNames
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
name|colTypes
operator|.
name|append
argument_list|(
name|valueCols
operator|.
name|get
argument_list|(
name|k
argument_list|)
operator|.
name|getTypeString
argument_list|()
argument_list|)
expr_stmt|;
name|colTypes
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|noFilter
condition|)
block|{
name|colNames
operator|.
name|append
argument_list|(
literal|"filtered"
argument_list|)
expr_stmt|;
name|colNames
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
name|colTypes
operator|.
name|append
argument_list|(
name|TypeInfoFactory
operator|.
name|shortTypeInfo
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
name|colTypes
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
comment|// remove the last ','
name|colNames
operator|.
name|setLength
argument_list|(
name|colNames
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
name|colTypes
operator|.
name|setLength
argument_list|(
name|colTypes
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
name|TableDesc
name|tblDesc
init|=
operator|new
name|TableDesc
argument_list|(
name|SequenceFileInputFormat
operator|.
name|class
argument_list|,
name|HiveSequenceFileOutputFormat
operator|.
name|class
argument_list|,
name|Utilities
operator|.
name|makeProperties
argument_list|(
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
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
literal|""
operator|+
name|Utilities
operator|.
name|ctrlaCode
argument_list|,
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
operator|.
name|LIST_COLUMNS
argument_list|,
name|colNames
operator|.
name|toString
argument_list|()
argument_list|,
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
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|colTypes
operator|.
name|toString
argument_list|()
argument_list|,
name|serdeConstants
operator|.
name|SERIALIZATION_LIB
argument_list|,
name|LazyBinarySerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|spillTableDesc
index|[
name|tag
index|]
operator|=
name|tblDesc
expr_stmt|;
block|}
return|return
name|spillTableDesc
return|;
block|}
specifier|public
specifier|static
name|RowContainer
argument_list|<
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
name|getRowContainer
parameter_list|(
name|Configuration
name|hconf
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|structFieldObjectInspectors
parameter_list|,
name|Byte
name|alias
parameter_list|,
name|int
name|containerSize
parameter_list|,
name|TableDesc
index|[]
name|spillTableDesc
parameter_list|,
name|JoinDesc
name|conf
parameter_list|,
name|boolean
name|noFilter
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|HiveException
block|{
name|TableDesc
name|tblDesc
init|=
name|JoinUtil
operator|.
name|getSpillTableDesc
argument_list|(
name|alias
argument_list|,
name|spillTableDesc
argument_list|,
name|conf
argument_list|,
name|noFilter
argument_list|)
decl_stmt|;
name|AbstractSerDe
name|serde
init|=
name|JoinUtil
operator|.
name|getSpillSerDe
argument_list|(
name|alias
argument_list|,
name|spillTableDesc
argument_list|,
name|conf
argument_list|,
name|noFilter
argument_list|)
decl_stmt|;
if|if
condition|(
name|serde
operator|==
literal|null
condition|)
block|{
name|containerSize
operator|=
operator|-
literal|1
expr_stmt|;
block|}
name|RowContainer
argument_list|<
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
name|rc
init|=
operator|new
name|RowContainer
argument_list|<
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
argument_list|(
name|containerSize
argument_list|,
name|hconf
argument_list|,
name|reporter
argument_list|)
decl_stmt|;
name|StructObjectInspector
name|rcOI
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|tblDesc
operator|!=
literal|null
condition|)
block|{
comment|// arbitrary column names used internally for serializing to spill table
name|List
argument_list|<
name|String
argument_list|>
name|colNames
init|=
name|Utilities
operator|.
name|getColumnNames
argument_list|(
name|tblDesc
operator|.
name|getProperties
argument_list|()
argument_list|)
decl_stmt|;
comment|// object inspector for serializing input tuples
name|rcOI
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|colNames
argument_list|,
name|structFieldObjectInspectors
argument_list|)
expr_stmt|;
block|}
name|rc
operator|.
name|setSerDe
argument_list|(
name|serde
argument_list|,
name|rcOI
argument_list|)
expr_stmt|;
name|rc
operator|.
name|setTableDesc
argument_list|(
name|tblDesc
argument_list|)
expr_stmt|;
return|return
name|rc
return|;
block|}
specifier|private
specifier|static
name|String
name|KEY_FIELD_PREFIX
init|=
operator|(
name|Utilities
operator|.
name|ReduceField
operator|.
name|KEY
operator|+
literal|"."
operator|)
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|String
name|VALUE_FIELD_PREFIX
init|=
operator|(
name|Utilities
operator|.
name|ReduceField
operator|.
name|VALUE
operator|+
literal|"."
operator|)
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
comment|/**    * Create a new struct object inspector for the list of struct fields, first removing the    * prefix from the field name.    * @param fields    * @param prefixToRemove    * @return    */
specifier|private
specifier|static
name|ObjectInspector
name|createStructFromFields
parameter_list|(
name|List
argument_list|<
name|StructField
argument_list|>
name|fields
parameter_list|,
name|String
name|prefixToRemove
parameter_list|)
block|{
name|int
name|prefixLength
init|=
name|prefixToRemove
operator|.
name|length
argument_list|()
operator|+
literal|1
decl_stmt|;
comment|// also remove the '.' after the prefix
name|ArrayList
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|fieldOIs
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|StructField
name|field
range|:
name|fields
control|)
block|{
name|fieldNames
operator|.
name|add
argument_list|(
name|field
operator|.
name|getFieldName
argument_list|()
operator|.
name|substring
argument_list|(
name|prefixLength
argument_list|)
argument_list|)
expr_stmt|;
name|fieldOIs
operator|.
name|add
argument_list|(
name|field
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|fieldNames
argument_list|,
name|fieldOIs
argument_list|)
return|;
block|}
comment|/**    * Checks the input object inspector to see if it is in for form of a flattened struct    * like the ones generated by a vectorized reduce sink input:    *   { 'key.reducesinkkey0':int, 'value._col0':int, 'value._col1':int, .. }    * If so, then it creates an "unflattened" struct that contains nested key/value    * structs:    *   { key: { reducesinkkey0:int }, value: { _col0:int, _col1:int, .. } }    *    * @param oi    * @return unflattened object inspector if unflattening is needed,    *         otherwise the original object inspector    */
specifier|private
specifier|static
name|ObjectInspector
name|unflattenObjInspector
parameter_list|(
name|ObjectInspector
name|oi
parameter_list|)
block|{
if|if
condition|(
name|oi
operator|instanceof
name|StructObjectInspector
condition|)
block|{
comment|// Check if all fields start with "key." or "value."
comment|// If so, then unflatten by adding an additional level of nested key and value structs
comment|// Example: { "key.reducesinkkey0":int, "key.reducesinkkey1": int, "value._col6":int }
comment|// Becomes
comment|//   { "key": { "reducesinkkey0":int, "reducesinkkey1":int }, "value": { "_col6":int } }
name|ArrayList
argument_list|<
name|StructField
argument_list|>
name|keyFields
init|=
operator|new
name|ArrayList
argument_list|<
name|StructField
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|StructField
argument_list|>
name|valueFields
init|=
operator|new
name|ArrayList
argument_list|<
name|StructField
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|StructField
name|field
range|:
operator|(
operator|(
name|StructObjectInspector
operator|)
name|oi
operator|)
operator|.
name|getAllStructFieldRefs
argument_list|()
control|)
block|{
name|String
name|fieldNameLower
init|=
name|field
operator|.
name|getFieldName
argument_list|()
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
if|if
condition|(
name|fieldNameLower
operator|.
name|startsWith
argument_list|(
name|KEY_FIELD_PREFIX
argument_list|)
condition|)
block|{
name|keyFields
operator|.
name|add
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fieldNameLower
operator|.
name|startsWith
argument_list|(
name|VALUE_FIELD_PREFIX
argument_list|)
condition|)
block|{
name|valueFields
operator|.
name|add
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Not a flattened struct, no need to unflatten
return|return
name|oi
return|;
block|}
block|}
comment|// All field names are of the form "key." or "value."
comment|// Create key/value structs and add the respective fields to each one
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|reduceFieldOIs
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
name|reduceFieldOIs
operator|.
name|add
argument_list|(
name|createStructFromFields
argument_list|(
name|keyFields
argument_list|,
name|Utilities
operator|.
name|ReduceField
operator|.
name|KEY
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|reduceFieldOIs
operator|.
name|add
argument_list|(
name|createStructFromFields
argument_list|(
name|valueFields
argument_list|,
name|Utilities
operator|.
name|ReduceField
operator|.
name|VALUE
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Finally create the outer struct to contain the key, value structs
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|Utilities
operator|.
name|reduceFieldNameList
argument_list|,
name|reduceFieldOIs
argument_list|)
return|;
block|}
return|return
name|oi
return|;
block|}
block|}
end_class

end_unit

