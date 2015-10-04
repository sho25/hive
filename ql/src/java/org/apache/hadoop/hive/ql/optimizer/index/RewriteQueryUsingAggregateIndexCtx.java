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
name|optimizer
operator|.
name|index
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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|ColumnInfo
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
name|FunctionRegistry
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
name|GroupByOperator
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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|OperatorUtils
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
name|RowSchema
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
name|SelectOperator
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
name|TableScanOperator
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
name|lib
operator|.
name|NodeProcessorCtx
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
name|Hive
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
name|metadata
operator|.
name|Table
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
name|optimizer
operator|.
name|ColumnPrunerProcFactory
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
name|ParseContext
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
name|AggregationDesc
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
name|ExprNodeColumnDesc
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
name|GroupByDesc
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
name|OperatorDesc
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
name|TableScanDesc
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDAFEvaluator
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
name|PrimitiveTypeInfo
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

begin_comment
comment|/**  * RewriteQueryUsingAggregateIndexCtx class stores the  * context for the {@link RewriteQueryUsingAggregateIndex}  * used to rewrite operator plan with index table instead of base table.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|RewriteQueryUsingAggregateIndexCtx
implements|implements
name|NodeProcessorCtx
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RewriteQueryUsingAggregateIndexCtx
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|RewriteQueryUsingAggregateIndexCtx
parameter_list|(
name|ParseContext
name|parseContext
parameter_list|,
name|Hive
name|hiveDb
parameter_list|,
name|RewriteCanApplyCtx
name|canApplyCtx
parameter_list|)
block|{
name|this
operator|.
name|parseContext
operator|=
name|parseContext
expr_stmt|;
name|this
operator|.
name|hiveDb
operator|=
name|hiveDb
expr_stmt|;
name|this
operator|.
name|canApplyCtx
operator|=
name|canApplyCtx
expr_stmt|;
name|this
operator|.
name|indexTableName
operator|=
name|canApplyCtx
operator|.
name|getIndexTableName
argument_list|()
expr_stmt|;
name|this
operator|.
name|alias
operator|=
name|canApplyCtx
operator|.
name|getAlias
argument_list|()
expr_stmt|;
name|this
operator|.
name|aggregateFunction
operator|=
name|canApplyCtx
operator|.
name|getAggFunction
argument_list|()
expr_stmt|;
name|this
operator|.
name|indexKey
operator|=
name|canApplyCtx
operator|.
name|getIndexKey
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
name|RewriteQueryUsingAggregateIndexCtx
name|getInstance
parameter_list|(
name|ParseContext
name|parseContext
parameter_list|,
name|Hive
name|hiveDb
parameter_list|,
name|RewriteCanApplyCtx
name|canApplyCtx
parameter_list|)
block|{
return|return
operator|new
name|RewriteQueryUsingAggregateIndexCtx
argument_list|(
name|parseContext
argument_list|,
name|hiveDb
argument_list|,
name|canApplyCtx
argument_list|)
return|;
block|}
specifier|private
specifier|final
name|Hive
name|hiveDb
decl_stmt|;
specifier|private
specifier|final
name|ParseContext
name|parseContext
decl_stmt|;
specifier|private
name|RewriteCanApplyCtx
name|canApplyCtx
decl_stmt|;
comment|//We need the GenericUDAFEvaluator for GenericUDAF function "sum"
specifier|private
name|GenericUDAFEvaluator
name|eval
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|String
name|indexTableName
decl_stmt|;
specifier|private
specifier|final
name|String
name|alias
decl_stmt|;
specifier|private
specifier|final
name|String
name|aggregateFunction
decl_stmt|;
specifier|private
name|ExprNodeColumnDesc
name|aggrExprNode
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|indexKey
decl_stmt|;
specifier|public
name|ParseContext
name|getParseContext
parameter_list|()
block|{
return|return
name|parseContext
return|;
block|}
specifier|public
name|Hive
name|getHiveDb
parameter_list|()
block|{
return|return
name|hiveDb
return|;
block|}
specifier|public
name|String
name|getIndexName
parameter_list|()
block|{
return|return
name|indexTableName
return|;
block|}
specifier|public
name|GenericUDAFEvaluator
name|getEval
parameter_list|()
block|{
return|return
name|eval
return|;
block|}
specifier|public
name|void
name|setEval
parameter_list|(
name|GenericUDAFEvaluator
name|eval
parameter_list|)
block|{
name|this
operator|.
name|eval
operator|=
name|eval
expr_stmt|;
block|}
specifier|public
name|void
name|setAggrExprNode
parameter_list|(
name|ExprNodeColumnDesc
name|aggrExprNode
parameter_list|)
block|{
name|this
operator|.
name|aggrExprNode
operator|=
name|aggrExprNode
expr_stmt|;
block|}
specifier|public
name|ExprNodeColumnDesc
name|getAggrExprNode
parameter_list|()
block|{
return|return
name|aggrExprNode
return|;
block|}
specifier|public
name|String
name|getAlias
parameter_list|()
block|{
return|return
name|alias
return|;
block|}
specifier|public
name|String
name|getAggregateFunction
parameter_list|()
block|{
return|return
name|aggregateFunction
return|;
block|}
specifier|public
name|String
name|getIndexKey
parameter_list|()
block|{
return|return
name|indexKey
return|;
block|}
specifier|public
name|void
name|setIndexKey
parameter_list|(
name|String
name|indexKey
parameter_list|)
block|{
name|this
operator|.
name|indexKey
operator|=
name|indexKey
expr_stmt|;
block|}
specifier|public
name|void
name|invokeRewriteQueryProc
parameter_list|()
throws|throws
name|SemanticException
block|{
name|this
operator|.
name|replaceTableScanProcess
argument_list|(
name|canApplyCtx
operator|.
name|getTableScanOperator
argument_list|()
argument_list|)
expr_stmt|;
comment|//We need aggrExprNode. Thus, replaceGroupByOperatorProcess should come before replaceSelectOperatorProcess
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|canApplyCtx
operator|.
name|getGroupByOperators
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|index
operator|++
control|)
block|{
name|this
operator|.
name|replaceGroupByOperatorProcess
argument_list|(
name|canApplyCtx
operator|.
name|getGroupByOperators
argument_list|()
operator|.
name|get
argument_list|(
name|index
argument_list|)
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|SelectOperator
name|selectperator
range|:
name|canApplyCtx
operator|.
name|getSelectOperators
argument_list|()
control|)
block|{
name|this
operator|.
name|replaceSelectOperatorProcess
argument_list|(
name|selectperator
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * This method replaces the original TableScanOperator with the new    * TableScanOperator and metadata that scans over the index table rather than    * scanning over the original table.    *    */
specifier|private
name|void
name|replaceTableScanProcess
parameter_list|(
name|TableScanOperator
name|scanOperator
parameter_list|)
throws|throws
name|SemanticException
block|{
name|RewriteQueryUsingAggregateIndexCtx
name|rewriteQueryCtx
init|=
name|this
decl_stmt|;
name|String
name|alias
init|=
name|rewriteQueryCtx
operator|.
name|getAlias
argument_list|()
decl_stmt|;
comment|// Need to remove the original TableScanOperators from these data structures
comment|// and add new ones
name|Map
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|topOps
init|=
name|rewriteQueryCtx
operator|.
name|getParseContext
argument_list|()
operator|.
name|getTopOps
argument_list|()
decl_stmt|;
comment|// remove original TableScanOperator
name|topOps
operator|.
name|remove
argument_list|(
name|alias
argument_list|)
expr_stmt|;
name|String
name|indexTableName
init|=
name|rewriteQueryCtx
operator|.
name|getIndexName
argument_list|()
decl_stmt|;
name|Table
name|indexTableHandle
init|=
literal|null
decl_stmt|;
try|try
block|{
name|indexTableHandle
operator|=
name|rewriteQueryCtx
operator|.
name|getHiveDb
argument_list|()
operator|.
name|getTable
argument_list|(
name|indexTableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error while getting the table handle for index table."
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// construct a new descriptor for the index table scan
name|TableScanDesc
name|indexTableScanDesc
init|=
operator|new
name|TableScanDesc
argument_list|(
name|indexTableHandle
argument_list|)
decl_stmt|;
name|indexTableScanDesc
operator|.
name|setGatherStats
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|String
name|k
init|=
name|indexTableName
operator|+
name|Path
operator|.
name|SEPARATOR
decl_stmt|;
name|indexTableScanDesc
operator|.
name|setStatsAggPrefix
argument_list|(
name|k
argument_list|)
expr_stmt|;
name|scanOperator
operator|.
name|setConf
argument_list|(
name|indexTableScanDesc
argument_list|)
expr_stmt|;
comment|// Construct the new RowResolver for the new TableScanOperator
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
name|sigRS
init|=
operator|new
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
name|StructObjectInspector
name|rowObjectInspector
init|=
operator|(
name|StructObjectInspector
operator|)
name|indexTableHandle
operator|.
name|getDeserializer
argument_list|()
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|StructField
name|field
init|=
name|rowObjectInspector
operator|.
name|getStructFieldRef
argument_list|(
name|rewriteQueryCtx
operator|.
name|getIndexKey
argument_list|()
argument_list|)
decl_stmt|;
name|sigRS
operator|.
name|add
argument_list|(
operator|new
name|ColumnInfo
argument_list|(
name|field
operator|.
name|getFieldName
argument_list|()
argument_list|,
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|field
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
argument_list|,
name|indexTableName
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error while creating the RowResolver for new TableScanOperator."
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|RowSchema
name|rs
init|=
operator|new
name|RowSchema
argument_list|(
name|sigRS
argument_list|)
decl_stmt|;
comment|// Set row resolver for new table
name|String
name|newAlias
init|=
name|indexTableName
decl_stmt|;
name|int
name|index
init|=
name|alias
operator|.
name|lastIndexOf
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|>=
literal|0
condition|)
block|{
name|newAlias
operator|=
name|alias
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|index
argument_list|)
operator|+
literal|":"
operator|+
name|indexTableName
expr_stmt|;
block|}
comment|// Scan operator now points to other table
name|scanOperator
operator|.
name|getConf
argument_list|()
operator|.
name|setAlias
argument_list|(
name|newAlias
argument_list|)
expr_stmt|;
name|scanOperator
operator|.
name|setAlias
argument_list|(
name|indexTableName
argument_list|)
expr_stmt|;
name|topOps
operator|.
name|put
argument_list|(
name|newAlias
argument_list|,
name|scanOperator
argument_list|)
expr_stmt|;
name|rewriteQueryCtx
operator|.
name|getParseContext
argument_list|()
operator|.
name|setTopOps
argument_list|(
operator|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
operator|)
name|topOps
argument_list|)
expr_stmt|;
name|ColumnPrunerProcFactory
operator|.
name|setupNeededColumns
argument_list|(
name|scanOperator
argument_list|,
name|rs
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|rewriteQueryCtx
operator|.
name|getIndexKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * This method replaces the original SelectOperator with the new    * SelectOperator with a new column indexed_key_column.    */
specifier|private
name|void
name|replaceSelectOperatorProcess
parameter_list|(
name|SelectOperator
name|operator
parameter_list|)
throws|throws
name|SemanticException
block|{
name|RewriteQueryUsingAggregateIndexCtx
name|rewriteQueryCtx
init|=
name|this
decl_stmt|;
comment|// we need to set the colList, outputColumnNames, colExprMap,
comment|// rowSchema for only that SelectOperator which precedes the GroupByOperator
comment|// count(indexed_key_column) needs to be replaced by
comment|// sum(`_count_of_indexed_key_column`)
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|selColList
init|=
name|operator
operator|.
name|getConf
argument_list|()
operator|.
name|getColList
argument_list|()
decl_stmt|;
name|selColList
operator|.
name|add
argument_list|(
name|rewriteQueryCtx
operator|.
name|getAggrExprNode
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|selOutputColNames
init|=
name|operator
operator|.
name|getConf
argument_list|()
operator|.
name|getOutputColumnNames
argument_list|()
decl_stmt|;
name|selOutputColNames
operator|.
name|add
argument_list|(
name|rewriteQueryCtx
operator|.
name|getAggrExprNode
argument_list|()
operator|.
name|getColumn
argument_list|()
argument_list|)
expr_stmt|;
name|operator
operator|.
name|getColumnExprMap
argument_list|()
operator|.
name|put
argument_list|(
name|rewriteQueryCtx
operator|.
name|getAggrExprNode
argument_list|()
operator|.
name|getColumn
argument_list|()
argument_list|,
name|rewriteQueryCtx
operator|.
name|getAggrExprNode
argument_list|()
argument_list|)
expr_stmt|;
name|RowSchema
name|selRS
init|=
name|operator
operator|.
name|getSchema
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ColumnInfo
argument_list|>
name|selRSSignature
init|=
name|selRS
operator|.
name|getSignature
argument_list|()
decl_stmt|;
comment|// Need to create a new type for Column[_count_of_indexed_key_column] node
name|PrimitiveTypeInfo
name|pti
init|=
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"bigint"
argument_list|)
decl_stmt|;
name|pti
operator|.
name|setTypeName
argument_list|(
literal|"bigint"
argument_list|)
expr_stmt|;
name|ColumnInfo
name|newCI
init|=
operator|new
name|ColumnInfo
argument_list|(
name|rewriteQueryCtx
operator|.
name|getAggregateFunction
argument_list|()
argument_list|,
name|pti
argument_list|,
literal|""
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|selRSSignature
operator|.
name|add
argument_list|(
name|newCI
argument_list|)
expr_stmt|;
name|selRS
operator|.
name|setSignature
argument_list|(
operator|(
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
operator|)
name|selRSSignature
argument_list|)
expr_stmt|;
name|operator
operator|.
name|setSchema
argument_list|(
name|selRS
argument_list|)
expr_stmt|;
block|}
comment|/**    * We need to replace the count(indexed_column_key) GenericUDAF aggregation    * function for group-by construct to "sum" GenericUDAF. This method creates a    * new operator tree for a sample query that creates a GroupByOperator with    * sum aggregation function and uses that GroupByOperator information to    * replace the original GroupByOperator aggregation information. It replaces    * the AggregationDesc (aggregation descriptor) of the old GroupByOperator    * with the new Aggregation Desc of the new GroupByOperator.    * @return    */
specifier|private
name|void
name|replaceGroupByOperatorProcess
parameter_list|(
name|GroupByOperator
name|operator
parameter_list|,
name|int
name|index
parameter_list|)
throws|throws
name|SemanticException
block|{
name|RewriteQueryUsingAggregateIndexCtx
name|rewriteQueryCtx
init|=
name|this
decl_stmt|;
comment|// We need to replace the GroupByOperator which is before RS
if|if
condition|(
name|index
operator|==
literal|0
condition|)
block|{
comment|// the query contains the sum aggregation GenericUDAF
name|String
name|selReplacementCommand
init|=
literal|"select sum(`"
operator|+
name|rewriteQueryCtx
operator|.
name|getAggregateFunction
argument_list|()
operator|+
literal|"`)"
operator|+
literal|" from `"
operator|+
name|rewriteQueryCtx
operator|.
name|getIndexName
argument_list|()
operator|+
literal|"` group by "
operator|+
name|rewriteQueryCtx
operator|.
name|getIndexKey
argument_list|()
operator|+
literal|" "
decl_stmt|;
comment|// retrieve the operator tree for the query, and the required GroupByOperator from it
name|Operator
argument_list|<
name|?
argument_list|>
name|newOperatorTree
init|=
name|RewriteParseContextGenerator
operator|.
name|generateOperatorTree
argument_list|(
name|rewriteQueryCtx
operator|.
name|getParseContext
argument_list|()
operator|.
name|getConf
argument_list|()
argument_list|,
name|selReplacementCommand
argument_list|)
decl_stmt|;
comment|// we get our new GroupByOperator here
name|GroupByOperator
name|newGbyOperator
init|=
name|OperatorUtils
operator|.
name|findLastOperatorUpstream
argument_list|(
name|newOperatorTree
argument_list|,
name|GroupByOperator
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|newGbyOperator
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Error replacing GroupBy operator."
argument_list|)
throw|;
block|}
comment|// we need this information to set the correct colList, outputColumnNames
comment|// in SelectOperator
name|ExprNodeColumnDesc
name|aggrExprNode
init|=
literal|null
decl_stmt|;
comment|// Construct the new AggregationDesc to get rid of the current
comment|// internal names and replace them with new internal names
comment|// as required by the operator tree
name|GroupByDesc
name|newConf
init|=
name|newGbyOperator
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|AggregationDesc
argument_list|>
name|newAggrList
init|=
name|newConf
operator|.
name|getAggregators
argument_list|()
decl_stmt|;
if|if
condition|(
name|newAggrList
operator|!=
literal|null
operator|&&
name|newAggrList
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|AggregationDesc
name|aggregationDesc
range|:
name|newAggrList
control|)
block|{
name|rewriteQueryCtx
operator|.
name|setEval
argument_list|(
name|aggregationDesc
operator|.
name|getGenericUDAFEvaluator
argument_list|()
argument_list|)
expr_stmt|;
name|aggrExprNode
operator|=
operator|(
name|ExprNodeColumnDesc
operator|)
name|aggregationDesc
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|rewriteQueryCtx
operator|.
name|setAggrExprNode
argument_list|(
name|aggrExprNode
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Now the GroupByOperator has the new AggregationList;
comment|// sum(`_count_of_indexed_key`)
comment|// instead of count(indexed_key)
name|GroupByDesc
name|oldConf
init|=
name|operator
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|oldConf
operator|.
name|setAggregators
argument_list|(
operator|(
name|ArrayList
argument_list|<
name|AggregationDesc
argument_list|>
operator|)
name|newAggrList
argument_list|)
expr_stmt|;
name|operator
operator|.
name|setConf
argument_list|(
name|oldConf
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// we just need to reset the GenericUDAFEvaluator and its name for this
comment|// GroupByOperator whose parent is the ReduceSinkOperator
name|GroupByDesc
name|childConf
init|=
operator|(
name|GroupByDesc
operator|)
name|operator
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|AggregationDesc
argument_list|>
name|childAggrList
init|=
name|childConf
operator|.
name|getAggregators
argument_list|()
decl_stmt|;
if|if
condition|(
name|childAggrList
operator|!=
literal|null
operator|&&
name|childAggrList
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|AggregationDesc
name|aggregationDesc
range|:
name|childAggrList
control|)
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|paraList
init|=
name|aggregationDesc
operator|.
name|getParameters
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|parametersOIList
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
name|ExprNodeDesc
name|expr
range|:
name|paraList
control|)
block|{
name|parametersOIList
operator|.
name|add
argument_list|(
name|expr
operator|.
name|getWritableObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|GenericUDAFEvaluator
name|evaluator
init|=
name|FunctionRegistry
operator|.
name|getGenericUDAFEvaluator
argument_list|(
literal|"sum"
argument_list|,
name|parametersOIList
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|aggregationDesc
operator|.
name|setGenericUDAFEvaluator
argument_list|(
name|evaluator
argument_list|)
expr_stmt|;
name|aggregationDesc
operator|.
name|setGenericUDAFName
argument_list|(
literal|"sum"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

