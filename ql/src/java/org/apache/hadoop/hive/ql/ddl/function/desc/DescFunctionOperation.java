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
name|ddl
operator|.
name|function
operator|.
name|desc
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
name|ddl
operator|.
name|DDLOperationContext
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
name|ddl
operator|.
name|DDLUtils
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
name|FunctionInfo
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
name|exec
operator|.
name|FunctionInfo
operator|.
name|FunctionResource
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
operator|.
name|join
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|ddl
operator|.
name|DDLOperation
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
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|AnnotationUtils
import|;
end_import

begin_comment
comment|/**  * Operation process of describing a function.  */
end_comment

begin_class
specifier|public
class|class
name|DescFunctionOperation
extends|extends
name|DDLOperation
argument_list|<
name|DescFunctionDesc
argument_list|>
block|{
specifier|public
name|DescFunctionOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|DescFunctionDesc
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|,
name|desc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|()
throws|throws
name|HiveException
block|{
try|try
init|(
name|DataOutputStream
name|outStream
init|=
name|DDLUtils
operator|.
name|getOutputStream
argument_list|(
name|desc
operator|.
name|getResFile
argument_list|()
argument_list|,
name|context
argument_list|)
init|)
block|{
name|String
name|funcName
init|=
name|desc
operator|.
name|getName
argument_list|()
decl_stmt|;
name|FunctionInfo
name|functionInfo
init|=
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
name|funcName
argument_list|)
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|funcClass
init|=
name|functionInfo
operator|==
literal|null
condition|?
literal|null
else|:
name|functionInfo
operator|.
name|getFunctionClass
argument_list|()
decl_stmt|;
name|Description
name|description
init|=
name|funcClass
operator|==
literal|null
condition|?
literal|null
else|:
name|AnnotationUtils
operator|.
name|getAnnotation
argument_list|(
name|funcClass
argument_list|,
name|Description
operator|.
name|class
argument_list|)
decl_stmt|;
name|printBaseInfo
argument_list|(
name|outStream
argument_list|,
name|funcName
argument_list|,
name|funcClass
argument_list|,
name|description
argument_list|)
expr_stmt|;
name|outStream
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|newLineCode
argument_list|)
expr_stmt|;
name|printExtendedInfoIfRequested
argument_list|(
name|outStream
argument_list|,
name|functionInfo
argument_list|,
name|funcClass
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"describe function: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
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
return|return
literal|0
return|;
block|}
specifier|private
name|void
name|printBaseInfo
parameter_list|(
name|DataOutputStream
name|outStream
parameter_list|,
name|String
name|funcName
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|funcClass
parameter_list|,
name|Description
name|description
parameter_list|)
throws|throws
name|IOException
throws|,
name|SemanticException
block|{
if|if
condition|(
name|funcClass
operator|==
literal|null
condition|)
block|{
name|outStream
operator|.
name|writeBytes
argument_list|(
literal|"Function '"
operator|+
name|funcName
operator|+
literal|"' does not exist."
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|description
operator|==
literal|null
condition|)
block|{
name|outStream
operator|.
name|writeBytes
argument_list|(
literal|"There is no documentation for function '"
operator|+
name|funcName
operator|+
literal|"'"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outStream
operator|.
name|writeBytes
argument_list|(
name|description
operator|.
name|value
argument_list|()
operator|.
name|replace
argument_list|(
literal|"_FUNC_"
argument_list|,
name|funcName
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|desc
operator|.
name|isExtended
argument_list|()
condition|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|synonyms
init|=
name|FunctionRegistry
operator|.
name|getFunctionSynonyms
argument_list|(
name|funcName
argument_list|)
decl_stmt|;
if|if
condition|(
name|synonyms
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|outStream
operator|.
name|writeBytes
argument_list|(
literal|"\nSynonyms: "
operator|+
name|join
argument_list|(
name|synonyms
argument_list|,
literal|", "
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|description
operator|.
name|extended
argument_list|()
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|outStream
operator|.
name|writeBytes
argument_list|(
literal|"\n"
operator|+
name|description
operator|.
name|extended
argument_list|()
operator|.
name|replace
argument_list|(
literal|"_FUNC_"
argument_list|,
name|funcName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|printExtendedInfoIfRequested
parameter_list|(
name|DataOutputStream
name|outStream
parameter_list|,
name|FunctionInfo
name|functionInfo
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|funcClass
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|desc
operator|.
name|isExtended
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|funcClass
operator|!=
literal|null
condition|)
block|{
name|outStream
operator|.
name|writeBytes
argument_list|(
literal|"Function class:"
operator|+
name|funcClass
operator|.
name|getName
argument_list|()
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|functionInfo
operator|!=
literal|null
condition|)
block|{
name|outStream
operator|.
name|writeBytes
argument_list|(
literal|"Function type:"
operator|+
name|functionInfo
operator|.
name|getFunctionType
argument_list|()
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
name|FunctionResource
index|[]
name|resources
init|=
name|functionInfo
operator|.
name|getResources
argument_list|()
decl_stmt|;
if|if
condition|(
name|resources
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|FunctionResource
name|resource
range|:
name|resources
control|)
block|{
name|outStream
operator|.
name|writeBytes
argument_list|(
literal|"Resource:"
operator|+
name|resource
operator|.
name|getResourceURI
argument_list|()
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

