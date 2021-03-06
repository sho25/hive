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
name|ppr
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
name|LinkedHashMap
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
name|Properties
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
name|lang3
operator|.
name|tuple
operator|.
name|Pair
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
name|hive_metastoreConstants
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
name|Partition
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
name|VirtualColumn
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
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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

begin_class
specifier|public
class|class
name|PartExprEvalUtils
block|{
comment|/**    * Evaluate expression with partition columns    *    * @param expr    * @param rowObjectInspector    * @return value returned by the expression    * @throws HiveException    */
specifier|static
specifier|public
name|Object
name|evalExprWithPart
parameter_list|(
name|ExprNodeDesc
name|expr
parameter_list|,
name|Partition
name|p
parameter_list|,
name|List
argument_list|<
name|VirtualColumn
argument_list|>
name|vcs
parameter_list|,
name|StructObjectInspector
name|rowObjectInspector
parameter_list|)
throws|throws
name|HiveException
block|{
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
name|p
operator|.
name|getSpec
argument_list|()
decl_stmt|;
name|Properties
name|partProps
init|=
name|p
operator|.
name|getSchema
argument_list|()
decl_stmt|;
name|String
name|pcolTypes
init|=
name|partProps
operator|.
name|getProperty
argument_list|(
name|hive_metastoreConstants
operator|.
name|META_TABLE_PARTITION_COLUMN_TYPES
argument_list|)
decl_stmt|;
name|String
index|[]
name|partKeyTypes
init|=
name|pcolTypes
operator|.
name|trim
argument_list|()
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
if|if
condition|(
name|partSpec
operator|.
name|size
argument_list|()
operator|!=
name|partKeyTypes
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Internal error : Partition Spec size, "
operator|+
name|partSpec
operator|.
name|size
argument_list|()
operator|+
literal|" doesn't match partition key definition size, "
operator|+
name|partKeyTypes
operator|.
name|length
argument_list|)
throw|;
block|}
name|boolean
name|hasVC
init|=
name|vcs
operator|!=
literal|null
operator|&&
operator|!
name|vcs
operator|.
name|isEmpty
argument_list|()
decl_stmt|;
name|Object
index|[]
name|rowWithPart
init|=
operator|new
name|Object
index|[
name|hasVC
condition|?
literal|3
else|:
literal|2
index|]
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
name|Object
argument_list|>
name|partValues
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
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
name|int
name|i
init|=
literal|0
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
name|ObjectInspector
name|oi
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|partKeyTypes
index|[
name|i
operator|++
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|partValues
operator|.
name|add
argument_list|(
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|,
name|oi
argument_list|)
operator|.
name|convert
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|partObjectInspectors
operator|.
name|add
argument_list|(
name|oi
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
if|if
condition|(
name|hasVC
condition|)
block|{
name|ois
operator|.
name|add
argument_list|(
name|VirtualColumn
operator|.
name|getVCSObjectInspector
argument_list|(
name|vcs
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|ExprNodeEvaluator
name|evaluator
init|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|expr
argument_list|)
decl_stmt|;
name|ObjectInspector
name|evaluateResultOI
init|=
name|evaluator
operator|.
name|initialize
argument_list|(
name|rowWithPartObjectInspector
argument_list|)
decl_stmt|;
name|Object
name|evaluateResultO
init|=
name|evaluator
operator|.
name|evaluate
argument_list|(
name|rowWithPart
argument_list|)
decl_stmt|;
return|return
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|evaluateResultOI
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|evaluateResultO
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Pair
argument_list|<
name|PrimitiveObjectInspector
argument_list|,
name|ExprNodeEvaluator
argument_list|>
name|prepareExpr
parameter_list|(
name|ExprNodeGenericFuncDesc
name|expr
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partColumnNames
parameter_list|,
name|List
argument_list|<
name|PrimitiveTypeInfo
argument_list|>
name|partColumnTypeInfos
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Create the row object
name|List
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
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|partColumnNames
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|partObjectInspectors
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|partColumnTypeInfos
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|StructObjectInspector
name|objectInspector
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|partColumnNames
argument_list|,
name|partObjectInspectors
argument_list|)
decl_stmt|;
name|ExprNodeEvaluator
name|evaluator
init|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|expr
argument_list|)
decl_stmt|;
name|ObjectInspector
name|evaluateResultOI
init|=
name|evaluator
operator|.
name|initialize
argument_list|(
name|objectInspector
argument_list|)
decl_stmt|;
return|return
name|Pair
operator|.
name|of
argument_list|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|evaluateResultOI
argument_list|,
name|evaluator
argument_list|)
return|;
block|}
specifier|static
specifier|public
name|Object
name|evaluateExprOnPart
parameter_list|(
name|Pair
argument_list|<
name|PrimitiveObjectInspector
argument_list|,
name|ExprNodeEvaluator
argument_list|>
name|pair
parameter_list|,
name|Object
name|partColValues
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
name|pair
operator|.
name|getLeft
argument_list|()
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|pair
operator|.
name|getRight
argument_list|()
operator|.
name|evaluate
argument_list|(
name|partColValues
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

