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
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
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
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
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
name|cli
operator|.
name|CliSessionState
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
name|ql
operator|.
name|lockmgr
operator|.
name|HiveTxnManager
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
name|ASTNode
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
name|ParseDriver
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
name|processors
operator|.
name|AddResourceProcessor
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
name|processors
operator|.
name|CommandProcessor
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
name|processors
operator|.
name|CommandProcessorFactory
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
name|processors
operator|.
name|CompileProcessor
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
name|processors
operator|.
name|CryptoProcessor
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
name|processors
operator|.
name|DfsProcessor
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
name|processors
operator|.
name|ListResourceProcessor
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
name|processors
operator|.
name|LlapCacheResourceProcessor
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
name|processors
operator|.
name|LlapClusterResourceProcessor
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
name|processors
operator|.
name|ReloadProcessor
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
name|processors
operator|.
name|ResetProcessor
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
name|processors
operator|.
name|SetProcessor
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
name|session
operator|.
name|SessionState
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
name|HiveStringUtils
import|;
end_import

begin_comment
comment|/**  * Q File Syntax Checker Utility.  *  */
end_comment

begin_class
specifier|public
class|class
name|QTestSyntaxUtil
block|{
specifier|private
name|QTestUtil
name|qTestUtil
decl_stmt|;
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
name|ParseDriver
name|pd
decl_stmt|;
specifier|public
name|QTestSyntaxUtil
parameter_list|(
name|QTestUtil
name|qTestUtil
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|ParseDriver
name|pd
parameter_list|)
block|{
name|this
operator|.
name|qTestUtil
operator|=
name|qTestUtil
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|pd
operator|=
name|pd
expr_stmt|;
block|}
specifier|public
name|void
name|checkQFileSyntax
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|cmds
parameter_list|)
block|{
name|String
name|command
init|=
literal|""
decl_stmt|;
if|if
condition|(
name|QTestSystemProperties
operator|.
name|shouldCheckSyntax
argument_list|()
condition|)
block|{
comment|//check syntax first
for|for
control|(
name|String
name|oneCmd
range|:
name|cmds
control|)
block|{
if|if
condition|(
name|StringUtils
operator|.
name|endsWith
argument_list|(
name|oneCmd
argument_list|,
literal|"\\"
argument_list|)
condition|)
block|{
name|command
operator|+=
name|StringUtils
operator|.
name|chop
argument_list|(
name|oneCmd
argument_list|)
operator|+
literal|"\\;"
expr_stmt|;
continue|continue;
block|}
else|else
block|{
if|if
condition|(
name|qTestUtil
operator|.
name|isHiveCommand
argument_list|(
name|oneCmd
argument_list|)
condition|)
block|{
name|command
operator|=
name|oneCmd
expr_stmt|;
block|}
else|else
block|{
name|command
operator|+=
name|oneCmd
expr_stmt|;
block|}
block|}
if|if
condition|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|command
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|assertTrue
argument_list|(
literal|"Syntax error in command: "
operator|+
name|command
argument_list|,
name|checkSyntax
argument_list|(
name|command
argument_list|)
argument_list|)
expr_stmt|;
name|command
operator|=
literal|""
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|boolean
name|checkSyntax
parameter_list|(
name|String
name|cmd
parameter_list|)
block|{
name|ASTNode
name|tree
decl_stmt|;
name|int
name|ret
init|=
literal|0
decl_stmt|;
name|CliSessionState
name|ss
init|=
operator|(
name|CliSessionState
operator|)
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
name|String
name|cmdTrimmed
init|=
name|HiveStringUtils
operator|.
name|removeComments
argument_list|(
name|cmd
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
name|String
index|[]
name|tokens
init|=
name|cmdTrimmed
operator|.
name|split
argument_list|(
literal|"\\s+"
argument_list|)
decl_stmt|;
if|if
condition|(
name|tokens
index|[
literal|0
index|]
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"source"
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|cmdTrimmed
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
literal|"quit"
argument_list|)
operator|||
name|cmdTrimmed
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
literal|"exit"
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|cmdTrimmed
operator|.
name|startsWith
argument_list|(
literal|"!"
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
try|try
block|{
name|CommandProcessor
name|proc
init|=
name|CommandProcessorFactory
operator|.
name|get
argument_list|(
name|tokens
argument_list|,
operator|(
name|HiveConf
operator|)
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|proc
operator|instanceof
name|IDriver
condition|)
block|{
try|try
block|{
name|Context
name|ctx
init|=
operator|new
name|Context
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|HiveTxnManager
name|queryTxnMgr
init|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|initTxnMgr
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|ctx
operator|.
name|setHiveTxnManager
argument_list|(
name|queryTxnMgr
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setCmd
argument_list|(
name|cmd
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setHDFSCleanup
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|tree
operator|=
name|pd
operator|.
name|parse
argument_list|(
name|cmd
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
name|qTestUtil
operator|.
name|analyzeAST
argument_list|(
name|tree
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
else|else
block|{
name|ret
operator|=
name|processLocalCmd
argument_list|(
name|cmdTrimmed
argument_list|,
name|proc
argument_list|,
name|ss
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
name|ret
operator|!=
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
specifier|private
specifier|static
name|int
name|processLocalCmd
parameter_list|(
name|String
name|cmd
parameter_list|,
name|CommandProcessor
name|proc
parameter_list|,
name|CliSessionState
name|ss
parameter_list|)
block|{
name|int
name|ret
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|proc
operator|!=
literal|null
condition|)
block|{
name|String
name|firstToken
init|=
name|cmd
operator|.
name|trim
argument_list|()
operator|.
name|split
argument_list|(
literal|"\\s+"
argument_list|)
index|[
literal|0
index|]
decl_stmt|;
name|String
name|cmd1
init|=
name|cmd
operator|.
name|trim
argument_list|()
operator|.
name|substring
argument_list|(
name|firstToken
operator|.
name|length
argument_list|()
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
comment|//CommandProcessorResponse res = proc.run(cmd_1);
if|if
condition|(
name|proc
operator|instanceof
name|ResetProcessor
operator|||
name|proc
operator|instanceof
name|CompileProcessor
operator|||
name|proc
operator|instanceof
name|ReloadProcessor
operator|||
name|proc
operator|instanceof
name|CryptoProcessor
operator|||
name|proc
operator|instanceof
name|AddResourceProcessor
operator|||
name|proc
operator|instanceof
name|ListResourceProcessor
operator|||
name|proc
operator|instanceof
name|LlapClusterResourceProcessor
operator|||
name|proc
operator|instanceof
name|LlapCacheResourceProcessor
condition|)
block|{
if|if
condition|(
name|cmd1
operator|.
name|trim
argument_list|()
operator|.
name|split
argument_list|(
literal|"\\s+"
argument_list|)
operator|.
name|length
operator|<
literal|1
condition|)
block|{
name|ret
operator|=
operator|-
literal|1
expr_stmt|;
block|}
block|}
if|if
condition|(
name|proc
operator|instanceof
name|SetProcessor
condition|)
block|{
if|if
condition|(
operator|!
name|cmd1
operator|.
name|contains
argument_list|(
literal|"="
argument_list|)
condition|)
block|{
name|ret
operator|=
operator|-
literal|1
expr_stmt|;
block|}
block|}
if|if
condition|(
name|proc
operator|instanceof
name|DfsProcessor
condition|)
block|{
name|String
index|[]
name|argv
init|=
name|cmd1
operator|.
name|trim
argument_list|()
operator|.
name|split
argument_list|(
literal|"\\s+"
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"-put"
operator|.
name|equals
argument_list|(
name|firstToken
argument_list|)
operator|||
literal|"-test"
operator|.
name|equals
argument_list|(
name|firstToken
argument_list|)
operator|||
literal|"-copyFromLocal"
operator|.
name|equals
argument_list|(
name|firstToken
argument_list|)
operator|||
literal|"-moveFromLocal"
operator|.
name|equals
argument_list|(
name|firstToken
argument_list|)
condition|)
block|{
if|if
condition|(
name|argv
operator|.
name|length
operator|<
literal|3
condition|)
block|{
name|ret
operator|=
operator|-
literal|1
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
literal|"-get"
operator|.
name|equals
argument_list|(
name|firstToken
argument_list|)
operator|||
literal|"-copyToLocal"
operator|.
name|equals
argument_list|(
name|firstToken
argument_list|)
operator|||
literal|"-moveToLocal"
operator|.
name|equals
argument_list|(
name|firstToken
argument_list|)
condition|)
block|{
if|if
condition|(
name|argv
operator|.
name|length
operator|<
literal|3
condition|)
block|{
name|ret
operator|=
operator|-
literal|1
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
literal|"-mv"
operator|.
name|equals
argument_list|(
name|firstToken
argument_list|)
operator|||
literal|"-cp"
operator|.
name|equals
argument_list|(
name|firstToken
argument_list|)
condition|)
block|{
if|if
condition|(
name|argv
operator|.
name|length
operator|<
literal|3
condition|)
block|{
name|ret
operator|=
operator|-
literal|1
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
literal|"-rm"
operator|.
name|equals
argument_list|(
name|firstToken
argument_list|)
operator|||
literal|"-rmr"
operator|.
name|equals
argument_list|(
name|firstToken
argument_list|)
operator|||
literal|"-cat"
operator|.
name|equals
argument_list|(
name|firstToken
argument_list|)
operator|||
literal|"-mkdir"
operator|.
name|equals
argument_list|(
name|firstToken
argument_list|)
operator|||
literal|"-touchz"
operator|.
name|equals
argument_list|(
name|firstToken
argument_list|)
operator|||
literal|"-stat"
operator|.
name|equals
argument_list|(
name|firstToken
argument_list|)
operator|||
literal|"-text"
operator|.
name|equals
argument_list|(
name|firstToken
argument_list|)
condition|)
block|{
if|if
condition|(
name|argv
operator|.
name|length
operator|<
literal|2
condition|)
block|{
name|ret
operator|=
operator|-
literal|1
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|ret
return|;
block|}
block|}
end_class

end_unit

