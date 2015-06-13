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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|Description
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDAFMkCollectionEvaluator
operator|.
name|BufferType
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
name|TypeInfo
import|;
end_import

begin_comment
comment|/**  * GenericUDAFCollectSet  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"collect_set"
argument_list|,
name|value
operator|=
literal|"_FUNC_(x) - Returns a set of objects with duplicate elements eliminated"
argument_list|)
specifier|public
class|class
name|GenericUDAFCollectSet
extends|extends
name|AbstractGenericUDAFResolver
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|GenericUDAFCollectSet
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|GenericUDAFCollectSet
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|GenericUDAFEvaluator
name|getEvaluator
parameter_list|(
name|TypeInfo
index|[]
name|parameters
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|parameters
operator|.
name|length
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
name|parameters
operator|.
name|length
operator|-
literal|1
argument_list|,
literal|"Exactly one argument is expected."
argument_list|)
throw|;
block|}
switch|switch
condition|(
name|parameters
index|[
literal|0
index|]
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|PRIMITIVE
case|:
case|case
name|STRUCT
case|:
case|case
name|MAP
case|:
case|case
name|LIST
case|:
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"Only primitive, struct, list or map type arguments are accepted but "
operator|+
name|parameters
index|[
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was passed as parameter 1."
argument_list|)
throw|;
block|}
return|return
operator|new
name|GenericUDAFMkCollectionEvaluator
argument_list|(
name|BufferType
operator|.
name|SET
argument_list|)
return|;
block|}
block|}
end_class

end_unit

