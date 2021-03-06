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
name|misc
operator|.
name|conf
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
name|Utilities
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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

begin_comment
comment|/**  * Operation process of showing some configuration.  */
end_comment

begin_class
specifier|public
class|class
name|ShowConfOperation
extends|extends
name|DDLOperation
argument_list|<
name|ShowConfDesc
argument_list|>
block|{
specifier|public
name|ShowConfOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|ShowConfDesc
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
throws|,
name|IOException
block|{
name|ConfVars
name|conf
init|=
name|HiveConf
operator|.
name|getConfVars
argument_list|(
name|desc
operator|.
name|getConfName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"invalid configuration name "
operator|+
name|desc
operator|.
name|getConfName
argument_list|()
argument_list|)
throw|;
block|}
name|String
name|description
init|=
name|conf
operator|.
name|getDescription
argument_list|()
decl_stmt|;
name|String
name|defaultValue
init|=
name|conf
operator|.
name|getDefaultValue
argument_list|()
decl_stmt|;
try|try
init|(
name|DataOutputStream
name|output
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
if|if
condition|(
name|defaultValue
operator|!=
literal|null
condition|)
block|{
name|output
operator|.
name|write
argument_list|(
name|defaultValue
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|output
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|output
operator|.
name|write
argument_list|(
name|conf
operator|.
name|typeString
argument_list|()
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
name|output
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
if|if
condition|(
name|description
operator|!=
literal|null
condition|)
block|{
name|output
operator|.
name|write
argument_list|(
name|description
operator|.
name|replaceAll
argument_list|(
literal|" *\n *"
argument_list|,
literal|" "
argument_list|)
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|output
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|newLineCode
argument_list|)
expr_stmt|;
block|}
return|return
literal|0
return|;
block|}
block|}
end_class

end_unit

