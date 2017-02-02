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
name|objectinspector
operator|.
name|UnionObjectInspector
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
name|StandardUnionObjectInspector
operator|.
name|StandardUnion
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
argument_list|<
name|ExprNodeColumnDesc
argument_list|>
block|{
specifier|private
specifier|transient
name|boolean
name|simpleCase
decl_stmt|;
specifier|private
specifier|transient
name|StructObjectInspector
name|inspector
decl_stmt|;
specifier|private
specifier|transient
name|StructField
name|field
decl_stmt|;
specifier|private
specifier|transient
name|StructObjectInspector
index|[]
name|inspectors
decl_stmt|;
specifier|private
specifier|transient
name|StructField
index|[]
name|fields
decl_stmt|;
specifier|private
specifier|transient
name|boolean
index|[]
name|unionField
decl_stmt|;
specifier|public
name|ExprNodeColumnEvaluator
parameter_list|(
name|ExprNodeColumnDesc
name|expr
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|expr
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
name|String
index|[]
name|unionfields
init|=
name|names
index|[
literal|0
index|]
operator|.
name|split
argument_list|(
literal|"\\:"
argument_list|)
decl_stmt|;
if|if
condition|(
name|names
operator|.
name|length
operator|==
literal|1
operator|&&
name|unionfields
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|simpleCase
operator|=
literal|true
expr_stmt|;
name|inspector
operator|=
operator|(
name|StructObjectInspector
operator|)
name|rowInspector
expr_stmt|;
name|field
operator|=
name|inspector
operator|.
name|getStructFieldRef
argument_list|(
name|names
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
return|return
name|outputOI
operator|=
name|field
operator|.
name|getFieldObjectInspector
argument_list|()
return|;
block|}
name|simpleCase
operator|=
literal|false
expr_stmt|;
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
name|unionField
operator|=
operator|new
name|boolean
index|[
name|names
operator|.
name|length
index|]
expr_stmt|;
name|int
name|unionIndex
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
if|if
condition|(
name|unionIndex
operator|==
operator|-
literal|1
condition|)
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
else|else
block|{
name|inspectors
index|[
name|i
index|]
operator|=
call|(
name|StructObjectInspector
call|)
argument_list|(
operator|(
name|UnionObjectInspector
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
argument_list|)
operator|.
name|getObjectInspectors
argument_list|()
operator|.
name|get
argument_list|(
name|unionIndex
argument_list|)
expr_stmt|;
block|}
block|}
comment|// to support names like _colx:1._coly
name|unionfields
operator|=
name|names
index|[
name|i
index|]
operator|.
name|split
argument_list|(
literal|"\\:"
argument_list|)
expr_stmt|;
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
name|unionfields
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|unionfields
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|unionIndex
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|unionfields
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|unionField
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|unionIndex
operator|=
operator|-
literal|1
expr_stmt|;
name|unionField
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
return|return
name|outputOI
operator|=
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
if|if
condition|(
name|simpleCase
condition|)
block|{
return|return
name|inspector
operator|.
name|getStructFieldData
argument_list|(
name|row
argument_list|,
name|field
argument_list|)
return|;
block|}
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
if|if
condition|(
name|unionField
index|[
name|i
index|]
condition|)
block|{
name|o
operator|=
operator|(
operator|(
name|StandardUnion
operator|)
name|o
operator|)
operator|.
name|getObject
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|o
return|;
block|}
block|}
end_class

end_unit

