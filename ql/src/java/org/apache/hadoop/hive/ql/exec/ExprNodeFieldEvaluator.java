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
name|ExprNodeFieldDesc
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
name|ListObjectInspector
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

begin_comment
comment|/**  * This Evaluator can evaluate s.f for s as both struct and list of struct. If s  * is struct, then s.f is the field. If s is list of struct, then s.f is the  * list of struct field.  */
end_comment

begin_class
specifier|public
class|class
name|ExprNodeFieldEvaluator
extends|extends
name|ExprNodeEvaluator
argument_list|<
name|ExprNodeFieldDesc
argument_list|>
block|{
specifier|transient
name|ExprNodeEvaluator
name|leftEvaluator
decl_stmt|;
specifier|transient
name|ObjectInspector
name|leftInspector
decl_stmt|;
specifier|transient
name|StructObjectInspector
name|structObjectInspector
decl_stmt|;
specifier|transient
name|StructField
name|field
decl_stmt|;
specifier|transient
name|ObjectInspector
name|structFieldObjectInspector
decl_stmt|;
specifier|transient
name|ObjectInspector
name|resultObjectInspector
decl_stmt|;
specifier|public
name|ExprNodeFieldEvaluator
parameter_list|(
name|ExprNodeFieldDesc
name|desc
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
argument_list|(
name|desc
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|leftEvaluator
operator|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|desc
operator|.
name|getDesc
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|initialize
parameter_list|(
name|ObjectInspector
name|rowInspector
parameter_list|)
throws|throws
name|HiveException
block|{
name|leftInspector
operator|=
name|leftEvaluator
operator|.
name|initialize
argument_list|(
name|rowInspector
argument_list|)
expr_stmt|;
if|if
condition|(
name|expr
operator|.
name|getIsList
argument_list|()
condition|)
block|{
name|structObjectInspector
operator|=
call|(
name|StructObjectInspector
call|)
argument_list|(
operator|(
name|ListObjectInspector
operator|)
name|leftInspector
argument_list|)
operator|.
name|getListElementObjectInspector
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|structObjectInspector
operator|=
operator|(
name|StructObjectInspector
operator|)
name|leftInspector
expr_stmt|;
block|}
name|field
operator|=
name|structObjectInspector
operator|.
name|getStructFieldRef
argument_list|(
name|expr
operator|.
name|getFieldName
argument_list|()
argument_list|)
expr_stmt|;
name|structFieldObjectInspector
operator|=
name|field
operator|.
name|getFieldObjectInspector
argument_list|()
expr_stmt|;
if|if
condition|(
name|expr
operator|.
name|getIsList
argument_list|()
condition|)
block|{
name|resultObjectInspector
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|structFieldObjectInspector
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|resultObjectInspector
operator|=
name|structFieldObjectInspector
expr_stmt|;
block|}
return|return
name|outputOI
operator|=
name|resultObjectInspector
return|;
block|}
specifier|private
name|List
argument_list|<
name|Object
argument_list|>
name|cachedList
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|Object
name|_evaluate
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|version
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Get the result in leftInspectableObject
name|Object
name|left
init|=
name|leftEvaluator
operator|.
name|evaluate
argument_list|(
name|row
argument_list|,
name|version
argument_list|)
decl_stmt|;
if|if
condition|(
name|expr
operator|.
name|getIsList
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|?
argument_list|>
name|list
init|=
operator|(
operator|(
name|ListObjectInspector
operator|)
name|leftInspector
operator|)
operator|.
name|getList
argument_list|(
name|left
argument_list|)
decl_stmt|;
if|if
condition|(
name|list
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
name|cachedList
operator|.
name|clear
argument_list|()
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
name|list
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|cachedList
operator|.
name|add
argument_list|(
name|structObjectInspector
operator|.
name|getStructFieldData
argument_list|(
name|list
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|field
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|cachedList
return|;
block|}
block|}
else|else
block|{
return|return
name|structObjectInspector
operator|.
name|getStructFieldData
argument_list|(
name|left
argument_list|,
name|field
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

