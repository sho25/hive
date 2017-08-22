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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfo
import|;
end_import

begin_comment
comment|/**  * An abstract class to help facilitate existing implementations of  *<tt>GenericUDAFResolver</tt> to migrate towards the newly introduced  * interface {@link GenericUDAFResolver2}. This class provides a default  * implementation of this new API and in turn calls  * the existing API {@link GenericUDAFResolver#getEvaluator(TypeInfo[])} by  * ignoring the extra parameter information available via the  *<tt>GenericUDAFParameterInfo</tt> interface.  *  */
end_comment

begin_class
annotation|@
name|Deprecated
specifier|public
specifier|abstract
class|class
name|AbstractGenericUDAFResolver
implements|implements
name|GenericUDAFResolver2
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
annotation|@
name|Override
specifier|public
name|GenericUDAFEvaluator
name|getEvaluator
parameter_list|(
name|GenericUDAFParameterInfo
name|info
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|info
operator|.
name|isAllColumns
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"The specified syntax for UDAF invocation is invalid."
argument_list|)
throw|;
block|}
return|return
name|getEvaluator
argument_list|(
name|info
operator|.
name|getParameters
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|GenericUDAFEvaluator
name|getEvaluator
parameter_list|(
name|TypeInfo
index|[]
name|info
parameter_list|)
throws|throws
name|SemanticException
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"This UDAF does not support the deprecated getEvaluator() method."
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

