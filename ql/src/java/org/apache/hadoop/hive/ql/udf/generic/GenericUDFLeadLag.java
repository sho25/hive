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
name|udf
operator|.
name|generic
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
name|PTFPartition
operator|.
name|PTFPartitionIterator
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
name|PTFUtils
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
name|UDFArgumentTypeException
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ConstantObjectInspector
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
name|ObjectInspectorConverters
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
name|ObjectInspectorConverters
operator|.
name|Converter
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
name|io
operator|.
name|IntWritable
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|GenericUDFLeadLag
extends|extends
name|GenericUDF
block|{
specifier|transient
name|ExprNodeEvaluator
name|exprEvaluator
decl_stmt|;
specifier|transient
name|PTFPartitionIterator
argument_list|<
name|Object
argument_list|>
name|pItr
decl_stmt|;
specifier|transient
name|ObjectInspector
name|firstArgOI
decl_stmt|;
specifier|transient
name|ObjectInspector
name|defaultArgOI
decl_stmt|;
specifier|transient
name|Converter
name|defaultValueConverter
decl_stmt|;
name|int
name|amt
decl_stmt|;
static|static
block|{
name|PTFUtils
operator|.
name|makeTransient
argument_list|(
name|GenericUDFLeadLag
operator|.
name|class
argument_list|,
literal|"exprEvaluator"
argument_list|,
literal|"pItr"
argument_list|,
literal|"firstArgOI"
argument_list|,
literal|"defaultArgOI"
argument_list|,
literal|"defaultValueConverter"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|evaluate
parameter_list|(
name|DeferredObject
index|[]
name|arguments
parameter_list|)
throws|throws
name|HiveException
block|{
name|Object
name|defaultVal
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|arguments
operator|.
name|length
operator|==
literal|3
condition|)
block|{
name|defaultVal
operator|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|defaultValueConverter
operator|.
name|convert
argument_list|(
name|arguments
index|[
literal|2
index|]
operator|.
name|get
argument_list|()
argument_list|)
argument_list|,
name|defaultArgOI
argument_list|)
expr_stmt|;
block|}
name|int
name|idx
init|=
name|pItr
operator|.
name|getIndex
argument_list|()
operator|-
literal|1
decl_stmt|;
name|int
name|start
init|=
literal|0
decl_stmt|;
name|int
name|end
init|=
name|pItr
operator|.
name|getPartition
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
try|try
block|{
name|Object
name|ret
init|=
literal|null
decl_stmt|;
name|int
name|newIdx
init|=
name|getIndex
argument_list|(
name|amt
argument_list|)
decl_stmt|;
if|if
condition|(
name|newIdx
operator|>=
name|end
operator|||
name|newIdx
operator|<
name|start
condition|)
block|{
name|ret
operator|=
name|defaultVal
expr_stmt|;
block|}
else|else
block|{
name|Object
name|row
init|=
name|getRow
argument_list|(
name|amt
argument_list|)
decl_stmt|;
name|ret
operator|=
name|exprEvaluator
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|ret
operator|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|ret
argument_list|,
name|firstArgOI
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
finally|finally
block|{
name|Object
name|currRow
init|=
name|pItr
operator|.
name|resetToIndex
argument_list|(
name|idx
argument_list|)
decl_stmt|;
comment|// reevaluate expression on current Row, to trigger the Lazy object
comment|// caches to be reset to the current row.
name|exprEvaluator
operator|.
name|evaluate
argument_list|(
name|currRow
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|initialize
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
if|if
condition|(
operator|!
operator|(
name|arguments
operator|.
name|length
operator|>=
literal|1
operator|&&
name|arguments
operator|.
name|length
operator|<=
literal|3
operator|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
name|arguments
operator|.
name|length
operator|-
literal|1
argument_list|,
literal|"Incorrect invocation of "
operator|+
name|_getFnName
argument_list|()
operator|+
literal|": _FUNC_(expr, amt, default)"
argument_list|)
throw|;
block|}
name|amt
operator|=
literal|1
expr_stmt|;
if|if
condition|(
name|arguments
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|ObjectInspector
name|amtOI
init|=
name|arguments
index|[
literal|1
index|]
decl_stmt|;
if|if
condition|(
operator|!
name|ObjectInspectorUtils
operator|.
name|isConstantObjectInspector
argument_list|(
name|amtOI
argument_list|)
operator|||
operator|(
name|amtOI
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
operator|)
operator|||
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|amtOI
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
operator|!=
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
operator|.
name|INT
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
name|_getFnName
argument_list|()
operator|+
literal|" amount must be a integer value "
operator|+
name|amtOI
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was passed as parameter 1."
argument_list|)
throw|;
block|}
name|Object
name|o
init|=
operator|(
operator|(
name|ConstantObjectInspector
operator|)
name|amtOI
operator|)
operator|.
name|getWritableConstantValue
argument_list|()
decl_stmt|;
name|amt
operator|=
operator|(
operator|(
name|IntWritable
operator|)
name|o
operator|)
operator|.
name|get
argument_list|()
expr_stmt|;
if|if
condition|(
name|amt
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
literal|" amount can not be nagative. Specified: "
operator|+
name|amt
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|arguments
operator|.
name|length
operator|==
literal|3
condition|)
block|{
name|defaultArgOI
operator|=
name|arguments
index|[
literal|2
index|]
expr_stmt|;
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|arguments
index|[
literal|2
index|]
argument_list|,
name|arguments
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|defaultValueConverter
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|arguments
index|[
literal|2
index|]
argument_list|,
name|arguments
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
name|firstArgOI
operator|=
name|arguments
index|[
literal|0
index|]
expr_stmt|;
return|return
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|firstArgOI
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
return|;
block|}
specifier|public
name|ExprNodeEvaluator
name|getExprEvaluator
parameter_list|()
block|{
return|return
name|exprEvaluator
return|;
block|}
specifier|public
name|void
name|setExprEvaluator
parameter_list|(
name|ExprNodeEvaluator
name|exprEvaluator
parameter_list|)
block|{
name|this
operator|.
name|exprEvaluator
operator|=
name|exprEvaluator
expr_stmt|;
block|}
specifier|public
name|PTFPartitionIterator
argument_list|<
name|Object
argument_list|>
name|getpItr
parameter_list|()
block|{
return|return
name|pItr
return|;
block|}
specifier|public
name|void
name|setpItr
parameter_list|(
name|PTFPartitionIterator
argument_list|<
name|Object
argument_list|>
name|pItr
parameter_list|)
block|{
name|this
operator|.
name|pItr
operator|=
name|pItr
expr_stmt|;
block|}
specifier|public
name|ObjectInspector
name|getFirstArgOI
parameter_list|()
block|{
return|return
name|firstArgOI
return|;
block|}
specifier|public
name|void
name|setFirstArgOI
parameter_list|(
name|ObjectInspector
name|firstArgOI
parameter_list|)
block|{
name|this
operator|.
name|firstArgOI
operator|=
name|firstArgOI
expr_stmt|;
block|}
specifier|public
name|ObjectInspector
name|getDefaultArgOI
parameter_list|()
block|{
return|return
name|defaultArgOI
return|;
block|}
specifier|public
name|void
name|setDefaultArgOI
parameter_list|(
name|ObjectInspector
name|defaultArgOI
parameter_list|)
block|{
name|this
operator|.
name|defaultArgOI
operator|=
name|defaultArgOI
expr_stmt|;
block|}
specifier|public
name|Converter
name|getDefaultValueConverter
parameter_list|()
block|{
return|return
name|defaultValueConverter
return|;
block|}
specifier|public
name|void
name|setDefaultValueConverter
parameter_list|(
name|Converter
name|defaultValueConverter
parameter_list|)
block|{
name|this
operator|.
name|defaultValueConverter
operator|=
name|defaultValueConverter
expr_stmt|;
block|}
specifier|public
name|int
name|getAmt
parameter_list|()
block|{
return|return
name|amt
return|;
block|}
specifier|public
name|void
name|setAmt
parameter_list|(
name|int
name|amt
parameter_list|)
block|{
name|this
operator|.
name|amt
operator|=
name|amt
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDisplayString
parameter_list|(
name|String
index|[]
name|children
parameter_list|)
block|{
assert|assert
operator|(
name|children
operator|.
name|length
operator|==
literal|2
operator|)
assert|;
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
name|_getFnName
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
name|sb
operator|.
name|append
argument_list|(
name|children
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|children
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
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
specifier|protected
specifier|abstract
name|String
name|_getFnName
parameter_list|()
function_decl|;
specifier|protected
specifier|abstract
name|Object
name|getRow
parameter_list|(
name|int
name|amt
parameter_list|)
throws|throws
name|HiveException
function_decl|;
specifier|protected
specifier|abstract
name|int
name|getIndex
parameter_list|(
name|int
name|amt
parameter_list|)
function_decl|;
block|}
end_class

end_unit

