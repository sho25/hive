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

begin_comment
comment|/*  * PartitionPruner.java  *  * Created on April 9, 2008, 3:48 PM  *  * To change this template, choose Tools | Template Manager  * and open the template in the editor.  */
end_comment

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|antlr
operator|.
name|runtime
operator|.
name|tree
operator|.
name|*
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
name|ExprNodeEvaluator
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
name|ExprNodeEvaluatorFactory
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
name|*
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
name|exprNodeColumnDesc
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
name|exprNodeConstantDesc
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
name|exprNodeDesc
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
name|exprNodeFieldDesc
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
name|exprNodeFuncDesc
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
name|exprNodeIndexDesc
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
name|exprNodeNullDesc
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
name|ql
operator|.
name|typeinfo
operator|.
name|TypeInfoUtils
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
name|UDFOPAnd
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
name|UDFOPNot
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
name|UDFOPOr
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
name|InspectableObject
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
name|StructObjectInspector
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

begin_class
specifier|public
class|class
name|PartitionPruner
block|{
comment|// The log
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
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
literal|"hive.ql.parse.PartitionPruner"
argument_list|)
decl_stmt|;
specifier|private
name|String
name|tableAlias
decl_stmt|;
specifier|private
name|QBMetaData
name|metaData
decl_stmt|;
specifier|private
name|Table
name|tab
decl_stmt|;
specifier|private
name|exprNodeDesc
name|prunerExpr
decl_stmt|;
comment|/** Creates a new instance of PartitionPruner */
specifier|public
name|PartitionPruner
parameter_list|(
name|String
name|tableAlias
parameter_list|,
name|QBMetaData
name|metaData
parameter_list|)
block|{
name|this
operator|.
name|tableAlias
operator|=
name|tableAlias
expr_stmt|;
name|this
operator|.
name|metaData
operator|=
name|metaData
expr_stmt|;
name|this
operator|.
name|tab
operator|=
name|metaData
operator|.
name|getTableForAlias
argument_list|(
name|tableAlias
argument_list|)
expr_stmt|;
name|this
operator|.
name|prunerExpr
operator|=
operator|new
name|exprNodeConstantDesc
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
expr_stmt|;
block|}
comment|/**    * We use exprNodeConstantDesc(class,null) to represent unknown values.    * Except UDFOPAnd, UDFOPOr, and UDFOPNot, all UDFs are assumed to return unknown values     * if any of the arguments are unknown.      *      * @param expr    * @return The expression desc, will NEVER be null.    * @throws SemanticException    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|private
name|exprNodeDesc
name|genExprNodeDesc
parameter_list|(
name|CommonTree
name|expr
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|//  We recursively create the exprNodeDesc.  Base cases:  when we encounter
comment|//  a column ref, we convert that into an exprNodeColumnDesc;  when we encounter
comment|//  a constant, we convert that into an exprNodeConstantDesc.  For others we just
comment|//  build the exprNodeFuncDesc with recursively built children.
name|exprNodeDesc
name|desc
init|=
literal|null
decl_stmt|;
comment|//  Is this a simple expr node (not a TOK_COLREF or a TOK_FUNCTION or an operator)?
name|desc
operator|=
name|SemanticAnalyzer
operator|.
name|genSimpleExprNodeDesc
argument_list|(
name|expr
argument_list|)
expr_stmt|;
if|if
condition|(
name|desc
operator|!=
literal|null
condition|)
block|{
return|return
name|desc
return|;
block|}
name|int
name|tokType
init|=
name|expr
operator|.
name|getType
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|tokType
condition|)
block|{
case|case
name|HiveParser
operator|.
name|TOK_COLREF
case|:
block|{
assert|assert
operator|(
name|expr
operator|.
name|getChildCount
argument_list|()
operator|==
literal|2
operator|)
assert|;
name|String
name|tabAlias
init|=
name|SemanticAnalyzer
operator|.
name|getTableName
argument_list|(
name|expr
argument_list|)
decl_stmt|;
name|String
name|colName
init|=
name|SemanticAnalyzer
operator|.
name|getSerDeFieldExpression
argument_list|(
name|expr
argument_list|)
decl_stmt|;
if|if
condition|(
name|tabAlias
operator|==
literal|null
operator|||
name|colName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_XPATH
operator|.
name|getMsg
argument_list|(
name|expr
argument_list|)
argument_list|)
throw|;
block|}
comment|// Set value to null if it's not partition column
if|if
condition|(
name|tabAlias
operator|.
name|equals
argument_list|(
name|tableAlias
argument_list|)
operator|&&
name|tab
operator|.
name|isPartitionKey
argument_list|(
name|colName
argument_list|)
condition|)
block|{
name|desc
operator|=
operator|new
name|exprNodeColumnDesc
argument_list|(
name|String
operator|.
name|class
argument_list|,
name|colName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// might be a column from another table
try|try
block|{
name|TypeInfo
name|typeInfo
init|=
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|this
operator|.
name|metaData
operator|.
name|getTableForAlias
argument_list|(
name|tabAlias
argument_list|)
operator|.
name|getDeserializer
argument_list|()
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
decl_stmt|;
name|desc
operator|=
operator|new
name|exprNodeConstantDesc
argument_list|(
name|typeInfo
operator|.
name|getStructFieldTypeInfo
argument_list|(
name|colName
argument_list|)
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
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
break|break;
block|}
default|default:
block|{
name|boolean
name|isFunction
init|=
operator|(
name|expr
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_FUNCTION
operator|)
decl_stmt|;
comment|// Create all children
name|int
name|childrenBegin
init|=
operator|(
name|isFunction
condition|?
literal|1
else|:
literal|0
operator|)
decl_stmt|;
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|children
init|=
operator|new
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
argument_list|(
name|expr
operator|.
name|getChildCount
argument_list|()
operator|-
name|childrenBegin
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|ci
init|=
name|childrenBegin
init|;
name|ci
operator|<
name|expr
operator|.
name|getChildCount
argument_list|()
condition|;
name|ci
operator|++
control|)
block|{
name|exprNodeDesc
name|child
init|=
name|genExprNodeDesc
argument_list|(
operator|(
name|CommonTree
operator|)
name|expr
operator|.
name|getChild
argument_list|(
name|ci
argument_list|)
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|child
operator|.
name|getTypeInfo
argument_list|()
operator|!=
literal|null
operator|)
assert|;
name|children
operator|.
name|add
argument_list|(
name|child
argument_list|)
expr_stmt|;
block|}
comment|// Create function desc
name|desc
operator|=
name|SemanticAnalyzer
operator|.
name|getXpathOrFuncExprNodeDesc
argument_list|(
name|expr
argument_list|,
name|isFunction
argument_list|,
name|children
argument_list|)
expr_stmt|;
if|if
condition|(
name|desc
operator|instanceof
name|exprNodeFuncDesc
operator|&&
operator|(
operator|(
operator|(
name|exprNodeFuncDesc
operator|)
name|desc
operator|)
operator|.
name|getUDFMethod
argument_list|()
operator|.
name|getDeclaringClass
argument_list|()
operator|.
name|equals
argument_list|(
name|UDFOPAnd
operator|.
name|class
argument_list|)
operator|||
operator|(
operator|(
name|exprNodeFuncDesc
operator|)
name|desc
operator|)
operator|.
name|getUDFMethod
argument_list|()
operator|.
name|getDeclaringClass
argument_list|()
operator|.
name|equals
argument_list|(
name|UDFOPOr
operator|.
name|class
argument_list|)
operator|||
operator|(
operator|(
name|exprNodeFuncDesc
operator|)
name|desc
operator|)
operator|.
name|getUDFMethod
argument_list|()
operator|.
name|getDeclaringClass
argument_list|()
operator|.
name|equals
argument_list|(
name|UDFOPNot
operator|.
name|class
argument_list|)
operator|)
condition|)
block|{
comment|// do nothing because "And" and "Or" and "Not" supports null value evaluation
comment|// NOTE: In the future all UDFs that treats null value as UNKNOWN (both in parameters and return
comment|// values) should derive from a common base class UDFNullAsUnknown, so instead of listing the classes
comment|// here we would test whether a class is derived from that base class.
block|}
else|else
block|{
comment|// If any child is null, set this node to null
if|if
condition|(
name|mightBeUnknown
argument_list|(
name|desc
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Pruner function might be unknown: "
operator|+
name|expr
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
name|desc
operator|=
operator|new
name|exprNodeConstantDesc
argument_list|(
name|desc
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
block|}
block|}
return|return
name|desc
return|;
block|}
specifier|public
specifier|static
name|boolean
name|mightBeUnknown
parameter_list|(
name|exprNodeDesc
name|desc
parameter_list|)
block|{
if|if
condition|(
name|desc
operator|instanceof
name|exprNodeConstantDesc
condition|)
block|{
name|exprNodeConstantDesc
name|d
init|=
operator|(
name|exprNodeConstantDesc
operator|)
name|desc
decl_stmt|;
return|return
name|d
operator|.
name|getValue
argument_list|()
operator|==
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|desc
operator|instanceof
name|exprNodeNullDesc
condition|)
block|{
return|return
literal|false
return|;
block|}
elseif|else
if|if
condition|(
name|desc
operator|instanceof
name|exprNodeIndexDesc
condition|)
block|{
name|exprNodeIndexDesc
name|d
init|=
operator|(
name|exprNodeIndexDesc
operator|)
name|desc
decl_stmt|;
return|return
name|mightBeUnknown
argument_list|(
name|d
operator|.
name|getDesc
argument_list|()
argument_list|)
operator|||
name|mightBeUnknown
argument_list|(
name|d
operator|.
name|getIndex
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|desc
operator|instanceof
name|exprNodeFieldDesc
condition|)
block|{
name|exprNodeFieldDesc
name|d
init|=
operator|(
name|exprNodeFieldDesc
operator|)
name|desc
decl_stmt|;
return|return
name|mightBeUnknown
argument_list|(
name|d
operator|.
name|getDesc
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|desc
operator|instanceof
name|exprNodeFuncDesc
condition|)
block|{
name|exprNodeFuncDesc
name|d
init|=
operator|(
name|exprNodeFuncDesc
operator|)
name|desc
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
name|d
operator|.
name|getChildren
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|mightBeUnknown
argument_list|(
name|d
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
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
elseif|else
if|if
condition|(
name|desc
operator|instanceof
name|exprNodeColumnDesc
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/** Add an expression */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|public
name|void
name|addExpression
parameter_list|(
name|CommonTree
name|expr
parameter_list|)
throws|throws
name|SemanticException
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"adding pruning Tree = "
operator|+
name|expr
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
name|exprNodeDesc
name|desc
init|=
name|genExprNodeDesc
argument_list|(
name|expr
argument_list|)
decl_stmt|;
comment|// Ignore null constant expressions
if|if
condition|(
operator|!
operator|(
name|desc
operator|instanceof
name|exprNodeConstantDesc
operator|)
operator|||
operator|(
operator|(
name|exprNodeConstantDesc
operator|)
name|desc
operator|)
operator|.
name|getValue
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"adding pruning expr = "
operator|+
name|desc
argument_list|)
expr_stmt|;
name|this
operator|.
name|prunerExpr
operator|=
name|SemanticAnalyzer
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"AND"
argument_list|,
name|this
operator|.
name|prunerExpr
argument_list|,
name|desc
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** From the table metadata prune the partitions to return the partitions **/
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|public
name|Set
argument_list|<
name|Partition
argument_list|>
name|prune
parameter_list|()
throws|throws
name|HiveException
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Started pruning partiton"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"tabname = "
operator|+
name|this
operator|.
name|tab
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"prune Expression = "
operator|+
name|this
operator|.
name|prunerExpr
argument_list|)
expr_stmt|;
name|HashSet
argument_list|<
name|Partition
argument_list|>
name|ret_parts
init|=
operator|new
name|HashSet
argument_list|<
name|Partition
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
name|this
operator|.
name|tab
operator|.
name|getDeserializer
argument_list|()
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|Object
index|[]
name|rowWithPart
init|=
operator|new
name|Object
index|[
literal|2
index|]
decl_stmt|;
name|InspectableObject
name|inspectableObject
init|=
operator|new
name|InspectableObject
argument_list|()
decl_stmt|;
name|ExprNodeEvaluator
name|evaluator
init|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|this
operator|.
name|prunerExpr
argument_list|)
decl_stmt|;
for|for
control|(
name|Partition
name|part
range|:
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getPartitions
argument_list|(
name|this
operator|.
name|tab
argument_list|)
control|)
block|{
comment|// Set all the variables here
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
name|part
operator|.
name|getSpec
argument_list|()
decl_stmt|;
comment|// Create the row object
name|ArrayList
argument_list|<
name|String
argument_list|>
name|partNames
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
name|String
argument_list|>
name|partValues
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
name|partObjectInspectors
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
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|partSpec
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|partNames
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|partValues
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|partObjectInspectors
operator|.
name|add
argument_list|(
name|ObjectInspectorFactory
operator|.
name|getStandardPrimitiveObjectInspector
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|StructObjectInspector
name|partObjectInspector
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|partNames
argument_list|,
name|partObjectInspectors
argument_list|)
decl_stmt|;
name|rowWithPart
index|[
literal|1
index|]
operator|=
name|partValues
expr_stmt|;
name|ArrayList
argument_list|<
name|StructObjectInspector
argument_list|>
name|ois
init|=
operator|new
name|ArrayList
argument_list|<
name|StructObjectInspector
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|ois
operator|.
name|add
argument_list|(
name|rowObjectInspector
argument_list|)
expr_stmt|;
name|ois
operator|.
name|add
argument_list|(
name|partObjectInspector
argument_list|)
expr_stmt|;
name|StructObjectInspector
name|rowWithPartObjectInspector
init|=
name|ObjectInspectorFactory
operator|.
name|getUnionStructObjectInspector
argument_list|(
name|ois
argument_list|)
decl_stmt|;
comment|// evaluate the expression tree
name|evaluator
operator|.
name|evaluate
argument_list|(
name|rowWithPart
argument_list|,
name|rowWithPartObjectInspector
argument_list|,
name|inspectableObject
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"prune result for partition "
operator|+
name|partSpec
operator|+
literal|": "
operator|+
name|inspectableObject
operator|.
name|o
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|Boolean
operator|.
name|FALSE
operator|.
name|equals
argument_list|(
name|inspectableObject
operator|.
name|o
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"retained partition: "
operator|+
name|partSpec
argument_list|)
expr_stmt|;
name|ret_parts
operator|.
name|add
argument_list|(
name|part
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"pruned partition: "
operator|+
name|partSpec
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|// Now return the set of partitions
return|return
name|ret_parts
return|;
block|}
specifier|public
name|Table
name|getTable
parameter_list|()
block|{
return|return
name|this
operator|.
name|tab
return|;
block|}
block|}
end_class

end_unit

