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
name|plan
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|List
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
name|lang
operator|.
name|builder
operator|.
name|HashCodeBuilder
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
name|ErrorMsg
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
name|UDFArgumentException
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
name|Utilities
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
name|session
operator|.
name|SessionState
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
name|session
operator|.
name|SessionState
operator|.
name|LogHelper
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
name|ql
operator|.
name|udf
operator|.
name|generic
operator|.
name|GenericUDFBaseCompare
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
name|GenericUDFBridge
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

begin_comment
comment|/**  * Describes a GenericFunc node.  */
end_comment

begin_class
specifier|public
class|class
name|ExprNodeGenericFuncDesc
extends|extends
name|ExprNodeDesc
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
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
name|ExprNodeGenericFuncDesc
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/**    * In case genericUDF is Serializable, we will serialize the object.    *    * In case genericUDF does not implement Serializable, Java will remember the    * class of genericUDF and creates a new instance when deserialized. This is    * exactly what we want.    */
specifier|private
name|GenericUDF
name|genericUDF
decl_stmt|;
specifier|private
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|chidren
decl_stmt|;
specifier|private
specifier|transient
name|String
name|funcText
decl_stmt|;
comment|/**    * This class uses a writableObjectInspector rather than a TypeInfo to store    * the canonical type information for this NodeDesc.    */
specifier|private
specifier|transient
name|ObjectInspector
name|writableObjectInspector
decl_stmt|;
comment|//Is this an expression that should perform a comparison for sorted searches
specifier|private
name|boolean
name|isSortedExpr
decl_stmt|;
specifier|public
name|ExprNodeGenericFuncDesc
parameter_list|()
block|{
empty_stmt|;
block|}
comment|/* If the function has an explicit name like func(args) then call a    * constructor that explicitly provides the function name in the    * funcText argument.    */
specifier|public
name|ExprNodeGenericFuncDesc
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|,
name|GenericUDF
name|genericUDF
parameter_list|,
name|String
name|funcText
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
parameter_list|)
block|{
name|this
argument_list|(
name|TypeInfoUtils
operator|.
name|getStandardWritableObjectInspectorFromTypeInfo
argument_list|(
name|typeInfo
argument_list|)
argument_list|,
name|genericUDF
argument_list|,
name|funcText
argument_list|,
name|children
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ExprNodeGenericFuncDesc
parameter_list|(
name|ObjectInspector
name|oi
parameter_list|,
name|GenericUDF
name|genericUDF
parameter_list|,
name|String
name|funcText
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
parameter_list|)
block|{
name|super
argument_list|(
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|oi
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|writableObjectInspector
operator|=
name|ObjectInspectorUtils
operator|.
name|getWritableObjectInspector
argument_list|(
name|oi
argument_list|)
expr_stmt|;
assert|assert
operator|(
name|genericUDF
operator|!=
literal|null
operator|)
assert|;
name|this
operator|.
name|genericUDF
operator|=
name|genericUDF
expr_stmt|;
name|this
operator|.
name|chidren
operator|=
name|children
expr_stmt|;
name|this
operator|.
name|funcText
operator|=
name|funcText
expr_stmt|;
block|}
comment|// Backward-compatibility interfaces for functions without a user-visible name.
specifier|public
name|ExprNodeGenericFuncDesc
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|,
name|GenericUDF
name|genericUDF
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
parameter_list|)
block|{
name|this
argument_list|(
name|typeInfo
argument_list|,
name|genericUDF
argument_list|,
literal|null
argument_list|,
name|children
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ExprNodeGenericFuncDesc
parameter_list|(
name|ObjectInspector
name|oi
parameter_list|,
name|GenericUDF
name|genericUDF
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
parameter_list|)
block|{
name|this
argument_list|(
name|oi
argument_list|,
name|genericUDF
argument_list|,
literal|null
argument_list|,
name|children
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|getWritableObjectInspector
parameter_list|()
block|{
return|return
name|writableObjectInspector
return|;
block|}
specifier|public
name|GenericUDF
name|getGenericUDF
parameter_list|()
block|{
return|return
name|genericUDF
return|;
block|}
specifier|public
name|void
name|setGenericUDF
parameter_list|(
name|GenericUDF
name|genericUDF
parameter_list|)
block|{
name|this
operator|.
name|genericUDF
operator|=
name|genericUDF
expr_stmt|;
block|}
specifier|public
name|void
name|setChildren
parameter_list|(
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
parameter_list|)
block|{
name|chidren
operator|=
name|children
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|getChildren
parameter_list|()
block|{
return|return
name|chidren
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|genericUDF
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"("
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|chidren
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
name|i
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|chidren
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getExprString
parameter_list|()
block|{
comment|// Get the children expr strings
name|String
index|[]
name|childrenExprStrings
init|=
operator|new
name|String
index|[
name|chidren
operator|.
name|size
argument_list|()
index|]
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
name|childrenExprStrings
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|childrenExprStrings
index|[
name|i
index|]
operator|=
name|chidren
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getExprString
argument_list|()
expr_stmt|;
block|}
return|return
name|genericUDF
operator|.
name|getDisplayString
argument_list|(
name|childrenExprStrings
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getCols
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|colList
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|chidren
operator|!=
literal|null
condition|)
block|{
name|int
name|pos
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|pos
operator|<
name|chidren
operator|.
name|size
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|colCh
init|=
name|chidren
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getCols
argument_list|()
decl_stmt|;
name|colList
operator|=
name|Utilities
operator|.
name|mergeUniqElems
argument_list|(
name|colList
argument_list|,
name|colCh
argument_list|)
expr_stmt|;
name|pos
operator|++
expr_stmt|;
block|}
block|}
return|return
name|colList
return|;
block|}
annotation|@
name|Override
specifier|public
name|ExprNodeDesc
name|clone
parameter_list|()
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|cloneCh
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|(
name|chidren
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|ch
range|:
name|chidren
control|)
block|{
name|cloneCh
operator|.
name|add
argument_list|(
name|ch
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ExprNodeGenericFuncDesc
name|clone
init|=
operator|new
name|ExprNodeGenericFuncDesc
argument_list|(
name|typeInfo
argument_list|,
name|FunctionRegistry
operator|.
name|cloneGenericUDF
argument_list|(
name|genericUDF
argument_list|)
argument_list|,
name|funcText
argument_list|,
name|cloneCh
argument_list|)
decl_stmt|;
return|return
name|clone
return|;
block|}
comment|/**    * Create a ExprNodeGenericFuncDesc based on the genericUDFClass and the    * children parameters. If the function has an explicit name, the    * newInstance method should be passed the function name in the funcText    * argument.    *    * @throws UDFArgumentException    */
specifier|public
specifier|static
name|ExprNodeGenericFuncDesc
name|newInstance
parameter_list|(
name|GenericUDF
name|genericUDF
parameter_list|,
name|String
name|funcText
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
name|ObjectInspector
index|[]
name|childrenOIs
init|=
operator|new
name|ObjectInspector
index|[
name|children
operator|.
name|size
argument_list|()
index|]
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
name|childrenOIs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|childrenOIs
index|[
name|i
index|]
operator|=
name|children
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getWritableObjectInspector
argument_list|()
expr_stmt|;
block|}
comment|// Check if a bigint is implicitely cast to a double as part of a comparison
comment|// Perform the check here instead of in GenericUDFBaseCompare to guarantee it is only run once per operator
if|if
condition|(
name|genericUDF
operator|instanceof
name|GenericUDFBaseCompare
operator|&&
name|children
operator|.
name|size
argument_list|()
operator|==
literal|2
condition|)
block|{
name|TypeInfo
name|oiTypeInfo0
init|=
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTypeInfo
argument_list|()
decl_stmt|;
name|TypeInfo
name|oiTypeInfo1
init|=
name|children
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getTypeInfo
argument_list|()
decl_stmt|;
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
operator|(
name|ss
operator|!=
literal|null
operator|)
condition|?
name|ss
operator|.
name|getConf
argument_list|()
else|:
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|LogHelper
name|console
init|=
operator|new
name|LogHelper
argument_list|(
name|LOG
argument_list|)
decl_stmt|;
comment|// For now, if a bigint is going to be cast to a double throw an error or warning
if|if
condition|(
operator|(
name|oiTypeInfo0
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
operator|&&
name|oiTypeInfo1
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|)
operator|)
operator|||
operator|(
name|oiTypeInfo0
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|)
operator|&&
name|oiTypeInfo1
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
operator|)
condition|)
block|{
if|if
condition|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMAPREDMODE
argument_list|)
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"strict"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
name|ErrorMsg
operator|.
name|NO_COMPARE_BIGINT_STRING
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
else|else
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"WARNING: Comparing a bigint and a string may result in a loss of precision."
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
operator|(
name|oiTypeInfo0
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
argument_list|)
operator|&&
name|oiTypeInfo1
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|)
operator|)
operator|||
operator|(
name|oiTypeInfo0
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|)
operator|&&
name|oiTypeInfo1
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
argument_list|)
operator|)
condition|)
block|{
if|if
condition|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMAPREDMODE
argument_list|)
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"strict"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
name|ErrorMsg
operator|.
name|NO_COMPARE_BIGINT_DOUBLE
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
else|else
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"WARNING: Comparing a bigint and a double may result in a loss of precision."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|ObjectInspector
name|oi
init|=
name|genericUDF
operator|.
name|initializeAndFoldConstants
argument_list|(
name|childrenOIs
argument_list|)
decl_stmt|;
name|String
index|[]
name|requiredJars
init|=
name|genericUDF
operator|.
name|getRequiredJars
argument_list|()
decl_stmt|;
name|String
index|[]
name|requiredFiles
init|=
name|genericUDF
operator|.
name|getRequiredFiles
argument_list|()
decl_stmt|;
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|requiredJars
operator|!=
literal|null
condition|)
block|{
name|SessionState
operator|.
name|ResourceType
name|t
init|=
name|SessionState
operator|.
name|find_resource_type
argument_list|(
literal|"JAR"
argument_list|)
decl_stmt|;
try|try
block|{
name|ss
operator|.
name|add_resources
argument_list|(
name|t
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|requiredJars
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|requiredFiles
operator|!=
literal|null
condition|)
block|{
name|SessionState
operator|.
name|ResourceType
name|t
init|=
name|SessionState
operator|.
name|find_resource_type
argument_list|(
literal|"FILE"
argument_list|)
decl_stmt|;
try|try
block|{
name|ss
operator|.
name|add_resources
argument_list|(
name|t
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|requiredFiles
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
operator|new
name|ExprNodeGenericFuncDesc
argument_list|(
name|oi
argument_list|,
name|genericUDF
argument_list|,
name|funcText
argument_list|,
name|children
argument_list|)
return|;
block|}
comment|/* Backward-compatibility interface for the case where there is no explicit    * name for the function.    */
specifier|public
specifier|static
name|ExprNodeGenericFuncDesc
name|newInstance
parameter_list|(
name|GenericUDF
name|genericUDF
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
return|return
name|newInstance
argument_list|(
name|genericUDF
argument_list|,
literal|null
argument_list|,
name|children
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSame
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|ExprNodeGenericFuncDesc
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ExprNodeGenericFuncDesc
name|dest
init|=
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|o
decl_stmt|;
if|if
condition|(
operator|!
name|typeInfo
operator|.
name|equals
argument_list|(
name|dest
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
operator|||
operator|!
name|genericUDF
operator|.
name|getClass
argument_list|()
operator|.
name|equals
argument_list|(
name|dest
operator|.
name|getGenericUDF
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|genericUDF
operator|instanceof
name|GenericUDFBridge
condition|)
block|{
name|GenericUDFBridge
name|bridge
init|=
operator|(
name|GenericUDFBridge
operator|)
name|genericUDF
decl_stmt|;
name|GenericUDFBridge
name|bridge2
init|=
operator|(
name|GenericUDFBridge
operator|)
name|dest
operator|.
name|getGenericUDF
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|bridge
operator|.
name|getUdfClassName
argument_list|()
operator|.
name|equals
argument_list|(
name|bridge2
operator|.
name|getUdfClassName
argument_list|()
argument_list|)
operator|||
operator|!
name|bridge
operator|.
name|getUdfName
argument_list|()
operator|.
name|equals
argument_list|(
name|bridge2
operator|.
name|getUdfName
argument_list|()
argument_list|)
operator|||
name|bridge
operator|.
name|isOperator
argument_list|()
operator|!=
name|bridge2
operator|.
name|isOperator
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
if|if
condition|(
name|chidren
operator|.
name|size
argument_list|()
operator|!=
name|dest
operator|.
name|getChildren
argument_list|()
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|chidren
operator|.
name|size
argument_list|()
condition|;
name|pos
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|chidren
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|isSame
argument_list|(
name|dest
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
name|pos
argument_list|)
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|superHashCode
init|=
name|super
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|HashCodeBuilder
name|builder
init|=
operator|new
name|HashCodeBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|appendSuper
argument_list|(
name|superHashCode
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|chidren
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|toHashCode
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isSortedExpr
parameter_list|()
block|{
return|return
name|isSortedExpr
return|;
block|}
specifier|public
name|void
name|setSortedExpr
parameter_list|(
name|boolean
name|isSortedExpr
parameter_list|)
block|{
name|this
operator|.
name|isSortedExpr
operator|=
name|isSortedExpr
expr_stmt|;
block|}
specifier|public
name|String
name|getFuncText
parameter_list|()
block|{
return|return
name|this
operator|.
name|funcText
return|;
block|}
block|}
end_class

end_unit

