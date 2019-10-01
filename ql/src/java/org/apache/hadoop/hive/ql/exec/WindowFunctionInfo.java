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
name|GenericUDAFResolver
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|AnnotationUtils
import|;
end_import

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
class|class
name|WindowFunctionInfo
extends|extends
name|FunctionInfo
block|{
specifier|private
specifier|final
name|boolean
name|supportsWindow
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|pivotResult
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|impliesOrder
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|supportsWithinGroup
decl_stmt|;
specifier|public
name|WindowFunctionInfo
parameter_list|(
name|FunctionType
name|functionType
parameter_list|,
name|String
name|functionName
parameter_list|,
name|GenericUDAFResolver
name|resolver
parameter_list|,
name|FunctionResource
index|[]
name|resources
parameter_list|)
block|{
name|super
argument_list|(
name|functionType
argument_list|,
name|functionName
argument_list|,
name|resolver
argument_list|,
name|resources
argument_list|)
expr_stmt|;
name|WindowFunctionDescription
name|def
init|=
name|AnnotationUtils
operator|.
name|getAnnotation
argument_list|(
name|resolver
operator|.
name|getClass
argument_list|()
argument_list|,
name|WindowFunctionDescription
operator|.
name|class
argument_list|)
decl_stmt|;
name|supportsWindow
operator|=
name|def
operator|==
literal|null
condition|?
literal|true
else|:
name|def
operator|.
name|supportsWindow
argument_list|()
expr_stmt|;
name|pivotResult
operator|=
name|def
operator|==
literal|null
condition|?
literal|false
else|:
name|def
operator|.
name|pivotResult
argument_list|()
expr_stmt|;
name|impliesOrder
operator|=
name|def
operator|==
literal|null
condition|?
literal|false
else|:
name|def
operator|.
name|impliesOrder
argument_list|()
expr_stmt|;
name|supportsWithinGroup
operator|=
name|def
operator|==
literal|null
condition|?
literal|false
else|:
name|def
operator|.
name|supportsWithinGroup
argument_list|()
expr_stmt|;
block|}
specifier|public
name|boolean
name|isSupportsWindow
parameter_list|()
block|{
return|return
name|supportsWindow
return|;
block|}
specifier|public
name|boolean
name|isPivotResult
parameter_list|()
block|{
return|return
name|pivotResult
return|;
block|}
specifier|public
name|boolean
name|isImpliesOrder
parameter_list|()
block|{
return|return
name|impliesOrder
return|;
block|}
specifier|public
name|boolean
name|supportsWithinGroup
parameter_list|()
block|{
return|return
name|supportsWithinGroup
return|;
block|}
block|}
end_class

end_unit

