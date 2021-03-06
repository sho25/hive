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
operator|.
name|tez
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
name|HashMap
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
name|metadata
operator|.
name|HiveException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_class
class|class
name|RestrictedConfigChecker
block|{
specifier|private
specifier|final
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|RestrictedConfigChecker
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|ConfVars
argument_list|>
name|restrictedHiveConf
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|restrictedNonHiveConf
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|initConf
decl_stmt|;
name|RestrictedConfigChecker
parameter_list|(
name|HiveConf
name|initConf
parameter_list|)
block|{
name|this
operator|.
name|initConf
operator|=
name|initConf
expr_stmt|;
name|String
index|[]
name|restrictedConfigs
init|=
name|HiveConf
operator|.
name|getTrimmedStringsVar
argument_list|(
name|initConf
argument_list|,
name|ConfVars
operator|.
name|HIVE_SERVER2_TEZ_SESSION_RESTRICTED_CONFIGS
argument_list|)
decl_stmt|;
if|if
condition|(
name|restrictedConfigs
operator|==
literal|null
operator|||
name|restrictedConfigs
operator|.
name|length
operator|==
literal|0
condition|)
return|return;
name|HashMap
argument_list|<
name|String
argument_list|,
name|ConfVars
argument_list|>
name|confVars
init|=
name|HiveConf
operator|.
name|getOrCreateReverseMap
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|confName
range|:
name|restrictedConfigs
control|)
block|{
if|if
condition|(
name|confName
operator|==
literal|null
operator|||
name|confName
operator|.
name|isEmpty
argument_list|()
condition|)
continue|continue;
name|confName
operator|=
name|confName
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
name|ConfVars
name|cv
init|=
name|confVars
operator|.
name|get
argument_list|(
name|confName
argument_list|)
decl_stmt|;
if|if
condition|(
name|cv
operator|!=
literal|null
condition|)
block|{
name|restrictedHiveConf
operator|.
name|add
argument_list|(
name|cv
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"A restricted config "
operator|+
name|confName
operator|+
literal|" is not recognized as a Hive setting."
argument_list|)
expr_stmt|;
name|restrictedNonHiveConf
operator|.
name|add
argument_list|(
name|confName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|validate
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{
for|for
control|(
name|ConfVars
name|var
range|:
name|restrictedHiveConf
control|)
block|{
name|String
name|userValue
init|=
name|HiveConf
operator|.
name|getVarWithoutType
argument_list|(
name|conf
argument_list|,
name|var
argument_list|)
decl_stmt|,
name|serverValue
init|=
name|HiveConf
operator|.
name|getVarWithoutType
argument_list|(
name|initConf
argument_list|,
name|var
argument_list|)
decl_stmt|;
comment|// Note: with some trickery, we could add logic for each type in ConfVars; for now the
comment|// potential spurious mismatches (e.g. 0 and 0.0 for float) should be easy to work around.
name|validateRestrictedConfigValues
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|userValue
argument_list|,
name|serverValue
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|var
range|:
name|restrictedNonHiveConf
control|)
block|{
name|String
name|userValue
init|=
name|conf
operator|.
name|get
argument_list|(
name|var
argument_list|)
decl_stmt|,
name|serverValue
init|=
name|initConf
operator|.
name|get
argument_list|(
name|var
argument_list|)
decl_stmt|;
name|validateRestrictedConfigValues
argument_list|(
name|var
argument_list|,
name|userValue
argument_list|,
name|serverValue
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|validateRestrictedConfigValues
parameter_list|(
name|String
name|var
parameter_list|,
name|String
name|userValue
parameter_list|,
name|String
name|serverValue
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
operator|(
name|userValue
operator|==
literal|null
operator|)
operator|!=
operator|(
name|serverValue
operator|==
literal|null
operator|)
operator|||
operator|(
name|userValue
operator|!=
literal|null
operator|&&
operator|!
name|userValue
operator|.
name|equals
argument_list|(
name|serverValue
argument_list|)
operator|)
condition|)
block|{
name|String
name|logValue
init|=
name|initConf
operator|.
name|isHiddenConfig
argument_list|(
name|var
argument_list|)
condition|?
literal|"(hidden)"
else|:
name|serverValue
decl_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|var
operator|+
literal|" is restricted from being set; server is configured"
operator|+
literal|" to use "
operator|+
name|logValue
operator|+
literal|", but the query configuration specifies "
operator|+
name|userValue
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

