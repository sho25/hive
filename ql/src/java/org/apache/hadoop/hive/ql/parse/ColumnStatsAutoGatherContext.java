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
name|parse
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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|Context
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
name|QueryState
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
name|vector
operator|.
name|VectorizationContext
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
name|parse
operator|.
name|BaseSemanticAnalyzer
operator|.
name|AnalyzeRewriteContext
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
name|ExprNodeConstantDesc
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
name|ExprNodeGenericFuncDesc
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
name|LoadFileDesc
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
name|SelectDesc
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
name|GenericUDF
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
name|TypeInfo
import|;
end_import

begin_comment
comment|/**  * ColumnStatsAutoGatherContext: This is passed to the compiler when set  * hive.stats.autogather=true during the INSERT OVERWRITE command.  *  **/
end_comment

begin_class
specifier|public
class|class
name|ColumnStatsAutoGatherContext
block|{
specifier|public
name|AnalyzeRewriteContext
name|analyzeRewrite
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|LoadFileDesc
argument_list|>
name|loadFileWork
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|SemanticAnalyzer
name|sa
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|final
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|columns
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partitionColumns
decl_stmt|;
specifier|private
name|boolean
name|isInsertInto
decl_stmt|;
specifier|private
name|Table
name|tbl
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
decl_stmt|;
specifier|private
name|Context
name|origCtx
decl_stmt|;
specifier|public
name|ColumnStatsAutoGatherContext
parameter_list|(
name|SemanticAnalyzer
name|sa
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|,
name|Table
name|tbl
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|,
name|boolean
name|isInsertInto
parameter_list|,
name|Context
name|ctx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|sa
operator|=
name|sa
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|op
operator|=
name|op
expr_stmt|;
name|this
operator|.
name|tbl
operator|=
name|tbl
expr_stmt|;
name|this
operator|.
name|partSpec
operator|=
name|partSpec
expr_stmt|;
name|this
operator|.
name|isInsertInto
operator|=
name|isInsertInto
expr_stmt|;
name|this
operator|.
name|origCtx
operator|=
name|ctx
expr_stmt|;
name|columns
operator|=
name|tbl
operator|.
name|getCols
argument_list|()
expr_stmt|;
name|partitionColumns
operator|=
name|tbl
operator|.
name|getPartCols
argument_list|()
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|LoadFileDesc
argument_list|>
name|getLoadFileWork
parameter_list|()
block|{
return|return
name|loadFileWork
return|;
block|}
specifier|public
name|AnalyzeRewriteContext
name|getAnalyzeRewrite
parameter_list|()
block|{
return|return
name|analyzeRewrite
return|;
block|}
specifier|public
name|void
name|setAnalyzeRewrite
parameter_list|(
name|AnalyzeRewriteContext
name|analyzeRewrite
parameter_list|)
block|{
name|this
operator|.
name|analyzeRewrite
operator|=
name|analyzeRewrite
expr_stmt|;
block|}
specifier|public
name|void
name|insertAnalyzePipeline
parameter_list|()
throws|throws
name|SemanticException
block|{
comment|// 1. Generate the statement of analyze table [tablename] compute statistics for columns
comment|// In non-partitioned table case, it will generate TS-SEL-GBY-RS-GBY-SEL-FS operator
comment|// In static-partitioned table case, it will generate TS-FIL(partitionKey)-SEL-GBY(partitionKey)-RS-GBY-SEL-FS operator
comment|// In dynamic-partitioned table case, it will generate TS-SEL-GBY(partitionKey)-RS-GBY-SEL-FS operator
comment|// However, we do not need to specify the partition-spec because (1) the data is going to be inserted to that specific partition
comment|// (2) we can compose the static/dynamic partition using a select operator in replaceSelectOperatorProcess..
name|String
name|analyzeCommand
init|=
literal|"analyze table `"
operator|+
name|tbl
operator|.
name|getDbName
argument_list|()
operator|+
literal|"`.`"
operator|+
name|tbl
operator|.
name|getTableName
argument_list|()
operator|+
literal|"`"
operator|+
literal|" compute statistics for columns "
decl_stmt|;
comment|// 2. Based on the statement, generate the selectOperator
name|Operator
argument_list|<
name|?
argument_list|>
name|selOp
init|=
literal|null
decl_stmt|;
try|try
block|{
name|selOp
operator|=
name|genSelOpForAnalyze
argument_list|(
name|analyzeCommand
argument_list|,
name|origCtx
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
decl||
name|ParseException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|// 3. attach this SEL to the operator right before FS
name|op
operator|.
name|getChildOperators
argument_list|()
operator|.
name|add
argument_list|(
name|selOp
argument_list|)
expr_stmt|;
name|selOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|selOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
comment|// 4. address the colExp, colList, etc for the SEL
try|try
block|{
name|replaceSelectOperatorProcess
argument_list|(
operator|(
name|SelectOperator
operator|)
name|selOp
argument_list|,
name|op
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
specifier|private
name|Operator
name|genSelOpForAnalyze
parameter_list|(
name|String
name|analyzeCommand
parameter_list|,
name|Context
name|origCtx
parameter_list|)
throws|throws
name|IOException
throws|,
name|ParseException
throws|,
name|SemanticException
block|{
comment|//0. initialization
name|Context
name|ctx
init|=
operator|new
name|Context
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|ctx
operator|.
name|setExplainConfig
argument_list|(
name|origCtx
operator|.
name|getExplainConfig
argument_list|()
argument_list|)
expr_stmt|;
name|ASTNode
name|tree
init|=
name|ParseUtils
operator|.
name|parse
argument_list|(
name|analyzeCommand
argument_list|,
name|ctx
argument_list|)
decl_stmt|;
comment|//1. get the ColumnStatsSemanticAnalyzer
name|BaseSemanticAnalyzer
name|baseSem
init|=
name|SemanticAnalyzerFactory
operator|.
name|get
argument_list|(
operator|new
name|QueryState
argument_list|(
name|conf
argument_list|)
argument_list|,
name|tree
argument_list|)
decl_stmt|;
name|ColumnStatsSemanticAnalyzer
name|colSem
init|=
operator|(
name|ColumnStatsSemanticAnalyzer
operator|)
name|baseSem
decl_stmt|;
comment|//2. get the rewritten AST
name|ASTNode
name|ast
init|=
name|colSem
operator|.
name|rewriteAST
argument_list|(
name|tree
argument_list|,
name|this
argument_list|)
decl_stmt|;
name|baseSem
operator|=
name|SemanticAnalyzerFactory
operator|.
name|get
argument_list|(
operator|new
name|QueryState
argument_list|(
name|conf
argument_list|)
argument_list|,
name|ast
argument_list|)
expr_stmt|;
name|SemanticAnalyzer
name|sem
init|=
operator|(
name|SemanticAnalyzer
operator|)
name|baseSem
decl_stmt|;
name|QB
name|qb
init|=
operator|new
name|QB
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|ASTNode
name|child
init|=
name|ast
decl_stmt|;
name|ParseContext
name|subPCtx
init|=
operator|(
operator|(
name|SemanticAnalyzer
operator|)
name|sem
operator|)
operator|.
name|getParseContext
argument_list|()
decl_stmt|;
name|subPCtx
operator|.
name|setContext
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
operator|(
operator|(
name|SemanticAnalyzer
operator|)
name|sem
operator|)
operator|.
name|initParseCtx
argument_list|(
name|subPCtx
argument_list|)
expr_stmt|;
name|sem
operator|.
name|doPhase1
argument_list|(
name|child
argument_list|,
name|qb
argument_list|,
name|sem
operator|.
name|initPhase1Ctx
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// This will trigger new calls to metastore to collect metadata
comment|// TODO: cache the information from the metastore
name|sem
operator|.
name|getMetaData
argument_list|(
name|qb
argument_list|)
expr_stmt|;
name|Operator
argument_list|<
name|?
argument_list|>
name|operator
init|=
name|sem
operator|.
name|genPlan
argument_list|(
name|qb
argument_list|)
decl_stmt|;
comment|//3. populate the load file work so that ColumnStatsTask can work
name|loadFileWork
operator|.
name|addAll
argument_list|(
name|sem
operator|.
name|getLoadFileWork
argument_list|()
argument_list|)
expr_stmt|;
comment|//4. because there is only one TS for analyze statement, we can get it.
if|if
condition|(
name|sem
operator|.
name|topOps
operator|.
name|values
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"ColumnStatsAutoGatherContext is expecting exactly one TS, but finds "
operator|+
name|sem
operator|.
name|topOps
operator|.
name|values
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
throw|;
block|}
name|operator
operator|=
name|sem
operator|.
name|topOps
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
expr_stmt|;
comment|//5. get the first SEL after TS
while|while
condition|(
operator|!
operator|(
name|operator
operator|instanceof
name|SelectOperator
operator|)
condition|)
block|{
name|operator
operator|=
name|operator
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
return|return
name|operator
return|;
block|}
comment|/**    * @param operator : the select operator in the analyze statement    * @param input : the operator right before FS in the insert overwrite statement    * @throws HiveException     */
specifier|private
name|void
name|replaceSelectOperatorProcess
parameter_list|(
name|SelectOperator
name|operator
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|input
parameter_list|)
throws|throws
name|HiveException
block|{
name|RowSchema
name|selRS
init|=
name|operator
operator|.
name|getSchema
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
name|signature
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|OpParseContext
name|inputCtx
init|=
name|sa
operator|.
name|opParseCtx
operator|.
name|get
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|RowResolver
name|inputRR
init|=
name|inputCtx
operator|.
name|getRowResolver
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
name|columns
init|=
name|inputRR
operator|.
name|getColumnInfos
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|colList
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|columnNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
name|columnExprMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
comment|// the column positions in the operator should be like this
comment|//<----non-partition columns---->|<--static partition columns-->|<--dynamic partition columns-->
comment|//        ExprNodeColumnDesc      |      ExprNodeConstantDesc    |     ExprNodeColumnDesc
comment|//           from input           |         generate itself      |        from input
comment|//                                |
comment|// 1. deal with non-partition columns
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|this
operator|.
name|columns
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ColumnInfo
name|col
init|=
name|columns
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|exprNodeDesc
init|=
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|col
argument_list|)
decl_stmt|;
name|colList
operator|.
name|add
argument_list|(
name|exprNodeDesc
argument_list|)
expr_stmt|;
name|String
name|internalName
init|=
name|selRS
operator|.
name|getColumnNames
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|columnNames
operator|.
name|add
argument_list|(
name|internalName
argument_list|)
expr_stmt|;
name|columnExprMap
operator|.
name|put
argument_list|(
name|internalName
argument_list|,
name|exprNodeDesc
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|selRS
operator|.
name|getSignature
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// if there is any partition column (in static partition or dynamic
comment|// partition or mixed case)
name|int
name|dynamicPartBegin
init|=
operator|-
literal|1
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
name|partitionColumns
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ExprNodeDesc
name|exprNodeDesc
init|=
literal|null
decl_stmt|;
name|String
name|partColName
init|=
name|partitionColumns
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|// 2. deal with static partition columns
if|if
condition|(
name|partSpec
operator|!=
literal|null
operator|&&
name|partSpec
operator|.
name|containsKey
argument_list|(
name|partColName
argument_list|)
operator|&&
name|partSpec
operator|.
name|get
argument_list|(
name|partColName
argument_list|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|dynamicPartBegin
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Dynamic partition columns should not come before static partition columns."
argument_list|)
throw|;
block|}
name|exprNodeDesc
operator|=
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|partSpec
operator|.
name|get
argument_list|(
name|partColName
argument_list|)
argument_list|)
expr_stmt|;
name|TypeInfo
name|srcType
init|=
name|exprNodeDesc
operator|.
name|getTypeInfo
argument_list|()
decl_stmt|;
name|TypeInfo
name|destType
init|=
name|selRS
operator|.
name|getSignature
argument_list|()
operator|.
name|get
argument_list|(
name|this
operator|.
name|columns
operator|.
name|size
argument_list|()
operator|+
name|i
argument_list|)
operator|.
name|getType
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|srcType
operator|.
name|equals
argument_list|(
name|destType
argument_list|)
condition|)
block|{
comment|// This may be possible when srcType is string but destType is integer
name|exprNodeDesc
operator|=
name|ParseUtils
operator|.
name|createConversionCast
argument_list|(
name|exprNodeDesc
argument_list|,
operator|(
name|PrimitiveTypeInfo
operator|)
name|destType
argument_list|)
expr_stmt|;
block|}
block|}
comment|// 3. dynamic partition columns
else|else
block|{
name|dynamicPartBegin
operator|++
expr_stmt|;
name|ColumnInfo
name|col
init|=
name|columns
operator|.
name|get
argument_list|(
name|this
operator|.
name|columns
operator|.
name|size
argument_list|()
operator|+
name|dynamicPartBegin
argument_list|)
decl_stmt|;
name|TypeInfo
name|srcType
init|=
name|col
operator|.
name|getType
argument_list|()
decl_stmt|;
name|TypeInfo
name|destType
init|=
name|selRS
operator|.
name|getSignature
argument_list|()
operator|.
name|get
argument_list|(
name|this
operator|.
name|columns
operator|.
name|size
argument_list|()
operator|+
name|i
argument_list|)
operator|.
name|getType
argument_list|()
decl_stmt|;
name|exprNodeDesc
operator|=
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|col
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|srcType
operator|.
name|equals
argument_list|(
name|destType
argument_list|)
condition|)
block|{
name|exprNodeDesc
operator|=
name|ParseUtils
operator|.
name|createConversionCast
argument_list|(
name|exprNodeDesc
argument_list|,
operator|(
name|PrimitiveTypeInfo
operator|)
name|destType
argument_list|)
expr_stmt|;
block|}
block|}
name|colList
operator|.
name|add
argument_list|(
name|exprNodeDesc
argument_list|)
expr_stmt|;
name|String
name|internalName
init|=
name|selRS
operator|.
name|getColumnNames
argument_list|()
operator|.
name|get
argument_list|(
name|this
operator|.
name|columns
operator|.
name|size
argument_list|()
operator|+
name|i
argument_list|)
decl_stmt|;
name|columnNames
operator|.
name|add
argument_list|(
name|internalName
argument_list|)
expr_stmt|;
name|columnExprMap
operator|.
name|put
argument_list|(
name|internalName
argument_list|,
name|exprNodeDesc
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|selRS
operator|.
name|getSignature
argument_list|()
operator|.
name|get
argument_list|(
name|this
operator|.
name|columns
operator|.
name|size
argument_list|()
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|operator
operator|.
name|setConf
argument_list|(
operator|new
name|SelectDesc
argument_list|(
name|colList
argument_list|,
name|columnNames
argument_list|)
argument_list|)
expr_stmt|;
name|operator
operator|.
name|setColumnExprMap
argument_list|(
name|columnExprMap
argument_list|)
expr_stmt|;
name|selRS
operator|.
name|setSignature
argument_list|(
name|signature
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
specifier|public
name|String
name|getCompleteName
parameter_list|()
block|{
return|return
name|tbl
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|tbl
operator|.
name|getTableName
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isInsertInto
parameter_list|()
block|{
return|return
name|isInsertInto
return|;
block|}
specifier|public
specifier|static
name|boolean
name|canRunAutogatherStats
parameter_list|(
name|Operator
name|curr
parameter_list|)
block|{
comment|// check the ObjectInspector
for|for
control|(
name|ColumnInfo
name|cinfo
range|:
name|curr
operator|.
name|getSchema
argument_list|()
operator|.
name|getSignature
argument_list|()
control|)
block|{
if|if
condition|(
name|cinfo
operator|.
name|getIsVirtualCol
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
elseif|else
if|if
condition|(
name|cinfo
operator|.
name|getObjectInspector
argument_list|()
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
return|return
literal|false
return|;
block|}
else|else
block|{
switch|switch
condition|(
operator|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|cinfo
operator|.
name|getType
argument_list|()
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
case|case
name|BYTE
case|:
case|case
name|SHORT
case|:
case|case
name|INT
case|:
case|case
name|LONG
case|:
case|case
name|TIMESTAMP
case|:
case|case
name|FLOAT
case|:
case|case
name|DOUBLE
case|:
case|case
name|STRING
case|:
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
case|case
name|BINARY
case|:
case|case
name|DECIMAL
case|:
comment|// TODO: Support case DATE:
break|break;
default|default:
return|return
literal|false
return|;
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

