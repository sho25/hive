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
operator|.
name|calcite
operator|.
name|translator
operator|.
name|opconventer
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
name|HashMap
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
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rex
operator|.
name|RexCall
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
name|FunctionInfo
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
name|OperatorFactory
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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveTableFunctionScan
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
name|calcite
operator|.
name|reloperators
operator|.
name|HiveTableScan
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
name|calcite
operator|.
name|translator
operator|.
name|ExprNodeConverter
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
name|calcite
operator|.
name|translator
operator|.
name|opconventer
operator|.
name|HiveOpConverter
operator|.
name|OpAttr
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
name|RowResolver
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
name|SemanticAnalyzer
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
name|plan
operator|.
name|UDTFDesc
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
name|GenericUDTF
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
name|TypeInfoUtils
import|;
end_import

begin_class
class|class
name|HiveTableFunctionScanVisitor
extends|extends
name|HiveRelNodeVisitor
argument_list|<
name|HiveTableFunctionScan
argument_list|>
block|{
name|HiveTableFunctionScanVisitor
parameter_list|(
name|HiveOpConverter
name|hiveOpConverter
parameter_list|)
block|{
name|super
argument_list|(
name|hiveOpConverter
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|OpAttr
name|visit
parameter_list|(
name|HiveTableFunctionScan
name|scanRel
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Translating operator rel#"
operator|+
name|scanRel
operator|.
name|getId
argument_list|()
operator|+
literal|":"
operator|+
name|scanRel
operator|.
name|getRelTypeName
argument_list|()
operator|+
literal|" with row type: ["
operator|+
name|scanRel
operator|.
name|getRowType
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|RexCall
name|call
init|=
operator|(
name|RexCall
operator|)
name|scanRel
operator|.
name|getCall
argument_list|()
decl_stmt|;
name|RowResolver
name|rowResolver
init|=
operator|new
name|RowResolver
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|scanRel
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldNames
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|functionFieldNames
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|exprCols
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
name|colExprMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|pos
operator|++
control|)
block|{
name|ExprNodeConverter
name|converter
init|=
operator|new
name|ExprNodeConverter
argument_list|(
name|SemanticAnalyzer
operator|.
name|DUMMY_TABLE
argument_list|,
name|fieldNames
operator|.
name|get
argument_list|(
name|pos
argument_list|)
argument_list|,
name|scanRel
operator|.
name|getRowType
argument_list|()
argument_list|,
name|scanRel
operator|.
name|getRowType
argument_list|()
argument_list|,
operator|(
operator|(
name|HiveTableScan
operator|)
name|scanRel
operator|.
name|getInput
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|getPartOrVirtualCols
argument_list|()
argument_list|,
name|scanRel
operator|.
name|getCluster
argument_list|()
operator|.
name|getTypeFactory
argument_list|()
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|exprCol
init|=
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|accept
argument_list|(
name|converter
argument_list|)
decl_stmt|;
name|colExprMap
operator|.
name|put
argument_list|(
name|HiveConf
operator|.
name|getColumnInternalName
argument_list|(
name|pos
argument_list|)
argument_list|,
name|exprCol
argument_list|)
expr_stmt|;
name|exprCols
operator|.
name|add
argument_list|(
name|exprCol
argument_list|)
expr_stmt|;
name|ColumnInfo
name|columnInfo
init|=
operator|new
name|ColumnInfo
argument_list|(
name|HiveConf
operator|.
name|getColumnInternalName
argument_list|(
name|pos
argument_list|)
argument_list|,
name|exprCol
operator|.
name|getWritableObjectInspector
argument_list|()
argument_list|,
name|SemanticAnalyzer
operator|.
name|DUMMY_TABLE
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|rowResolver
operator|.
name|put
argument_list|(
name|columnInfo
operator|.
name|getTabAlias
argument_list|()
argument_list|,
name|columnInfo
operator|.
name|getAlias
argument_list|()
argument_list|,
name|columnInfo
argument_list|)
expr_stmt|;
name|functionFieldNames
operator|.
name|add
argument_list|(
name|HiveConf
operator|.
name|getColumnInternalName
argument_list|(
name|pos
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|OpAttr
name|inputOpAf
init|=
name|hiveOpConverter
operator|.
name|dispatch
argument_list|(
name|scanRel
operator|.
name|getInputs
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|TableScanOperator
name|op
init|=
operator|(
name|TableScanOperator
operator|)
name|inputOpAf
operator|.
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|setRowLimit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Operator
argument_list|<
name|?
argument_list|>
name|output
init|=
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
operator|new
name|SelectDesc
argument_list|(
name|exprCols
argument_list|,
name|functionFieldNames
argument_list|,
literal|false
argument_list|)
argument_list|,
operator|new
name|RowSchema
argument_list|(
name|rowResolver
operator|.
name|getRowSchema
argument_list|()
argument_list|)
argument_list|,
name|op
argument_list|)
decl_stmt|;
name|output
operator|.
name|setColumnExprMap
argument_list|(
name|colExprMap
argument_list|)
expr_stmt|;
name|Operator
argument_list|<
name|?
argument_list|>
name|funcOp
init|=
name|genUDTFPlan
argument_list|(
name|call
argument_list|,
name|functionFieldNames
argument_list|,
name|output
argument_list|,
name|rowResolver
argument_list|)
decl_stmt|;
return|return
operator|new
name|OpAttr
argument_list|(
literal|null
argument_list|,
operator|new
name|HashSet
argument_list|<
name|Integer
argument_list|>
argument_list|()
argument_list|,
name|funcOp
argument_list|)
return|;
block|}
specifier|private
name|Operator
argument_list|<
name|?
argument_list|>
name|genUDTFPlan
parameter_list|(
name|RexCall
name|call
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|colAliases
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|input
parameter_list|,
name|RowResolver
name|rowResolver
parameter_list|)
throws|throws
name|SemanticException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"genUDTFPlan, Col aliases: {}"
argument_list|,
name|colAliases
argument_list|)
expr_stmt|;
name|GenericUDTF
name|genericUDTF
init|=
name|createGenericUDTF
argument_list|(
name|call
argument_list|)
decl_stmt|;
name|StructObjectInspector
name|rowOI
init|=
name|createStructObjectInspector
argument_list|(
name|rowResolver
argument_list|,
name|colAliases
argument_list|)
decl_stmt|;
name|StructObjectInspector
name|outputOI
init|=
name|genericUDTF
operator|.
name|initialize
argument_list|(
name|rowOI
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ColumnInfo
argument_list|>
name|columnInfos
init|=
name|createColumnInfos
argument_list|(
name|outputOI
argument_list|)
decl_stmt|;
comment|// Add the UDTFOperator to the operator DAG
return|return
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
operator|new
name|UDTFDesc
argument_list|(
name|genericUDTF
argument_list|,
literal|false
argument_list|)
argument_list|,
operator|new
name|RowSchema
argument_list|(
name|columnInfos
argument_list|)
argument_list|,
name|input
argument_list|)
return|;
block|}
specifier|private
name|GenericUDTF
name|createGenericUDTF
parameter_list|(
name|RexCall
name|call
parameter_list|)
throws|throws
name|SemanticException
block|{
name|String
name|functionName
init|=
name|call
operator|.
name|getOperator
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|FunctionInfo
name|fi
init|=
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
name|functionName
argument_list|)
decl_stmt|;
return|return
name|fi
operator|.
name|getGenericUDTF
argument_list|()
return|;
block|}
specifier|private
name|StructObjectInspector
name|createStructObjectInspector
parameter_list|(
name|RowResolver
name|rowResolver
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|colAliases
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// Create the object inspector for the input columns and initialize the UDTF
name|List
argument_list|<
name|String
argument_list|>
name|colNames
init|=
name|rowResolver
operator|.
name|getColumnInfos
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|ci
lambda|->
name|ci
operator|.
name|getInternalName
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|colOIs
init|=
name|rowResolver
operator|.
name|getColumnInfos
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|ci
lambda|->
name|ci
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|colNames
argument_list|,
name|colOIs
argument_list|)
return|;
block|}
specifier|private
name|List
argument_list|<
name|ColumnInfo
argument_list|>
name|createColumnInfos
parameter_list|(
name|StructObjectInspector
name|outputOI
parameter_list|)
block|{
comment|// Generate the output column info's / row resolver using internal names.
name|List
argument_list|<
name|ColumnInfo
argument_list|>
name|columnInfos
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|StructField
name|sf
range|:
name|outputOI
operator|.
name|getAllStructFieldRefs
argument_list|()
control|)
block|{
comment|// Since the UDTF operator feeds into a LVJ operator that will rename all the internal names, we can just use
comment|// field name from the UDTF's OI as the internal name
name|ColumnInfo
name|col
init|=
operator|new
name|ColumnInfo
argument_list|(
name|sf
operator|.
name|getFieldName
argument_list|()
argument_list|,
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|sf
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|col
operator|.
name|setAlias
argument_list|(
name|sf
operator|.
name|getFieldName
argument_list|()
argument_list|)
expr_stmt|;
name|columnInfos
operator|.
name|add
argument_list|(
name|col
argument_list|)
expr_stmt|;
block|}
return|return
name|columnInfos
return|;
block|}
block|}
end_class

end_unit

