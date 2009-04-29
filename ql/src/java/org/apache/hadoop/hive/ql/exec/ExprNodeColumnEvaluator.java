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
name|exec
package|;
end_package

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
name|StructObjectInspector
import|;
end_import

begin_comment
comment|/**  * This evaluator gets the column from the row object.  */
end_comment

begin_class
specifier|public
class|class
name|ExprNodeColumnEvaluator
extends|extends
name|ExprNodeEvaluator
block|{
specifier|protected
name|exprNodeColumnDesc
name|expr
decl_stmt|;
specifier|transient
name|StructObjectInspector
index|[]
name|inspectors
decl_stmt|;
specifier|transient
name|StructField
index|[]
name|fields
decl_stmt|;
specifier|public
name|ExprNodeColumnEvaluator
parameter_list|(
name|exprNodeColumnDesc
name|expr
parameter_list|)
block|{
name|this
operator|.
name|expr
operator|=
name|expr
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
comment|// We need to support field names like KEY.0, VALUE.1 between
comment|// map-reduce boundary.
name|String
index|[]
name|names
init|=
name|expr
operator|.
name|getColumn
argument_list|()
operator|.
name|split
argument_list|(
literal|"\\."
argument_list|)
decl_stmt|;
name|inspectors
operator|=
operator|new
name|StructObjectInspector
index|[
name|names
operator|.
name|length
index|]
expr_stmt|;
name|fields
operator|=
operator|new
name|StructField
index|[
name|names
operator|.
name|length
index|]
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
name|names
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|inspectors
index|[
literal|0
index|]
operator|=
operator|(
name|StructObjectInspector
operator|)
name|rowInspector
expr_stmt|;
block|}
else|else
block|{
name|inspectors
index|[
name|i
index|]
operator|=
operator|(
name|StructObjectInspector
operator|)
name|fields
index|[
name|i
operator|-
literal|1
index|]
operator|.
name|getFieldObjectInspector
argument_list|()
expr_stmt|;
block|}
name|fields
index|[
name|i
index|]
operator|=
name|inspectors
index|[
name|i
index|]
operator|.
name|getStructFieldRef
argument_list|(
name|names
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|fields
index|[
name|names
operator|.
name|length
operator|-
literal|1
index|]
operator|.
name|getFieldObjectInspector
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|evaluate
parameter_list|(
name|Object
name|row
parameter_list|)
throws|throws
name|HiveException
block|{
name|Object
name|o
init|=
name|row
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
name|fields
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|o
operator|=
name|inspectors
index|[
name|i
index|]
operator|.
name|getStructFieldData
argument_list|(
name|o
argument_list|,
name|fields
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|o
return|;
block|}
block|}
end_class

end_unit

