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
name|hooks
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
name|LinkedHashMap
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
name|lang
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
name|QueryPlan
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|session
operator|.
name|SessionState
operator|.
name|LogHelper
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
name|ColumnAccessInfo
import|;
end_import

begin_comment
comment|/*  * This hook is used for verifying the column access information  * that is generated and maintained in the QueryPlan object by the  * ColumnAccessAnalyzer. All the hook does is print out the columns  * accessed from each table as recorded in the ColumnAccessInfo  * in the QueryPlan.  */
end_comment

begin_class
specifier|public
class|class
name|CheckColumnAccessHook
implements|implements
name|ExecuteWithHookContext
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
name|HookContext
name|hookContext
parameter_list|)
block|{
name|HiveConf
name|conf
init|=
name|hookContext
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_STATS_COLLECT_SCANCOLS
argument_list|)
operator|==
literal|false
condition|)
block|{
return|return;
block|}
name|QueryPlan
name|plan
init|=
name|hookContext
operator|.
name|getQueryPlan
argument_list|()
decl_stmt|;
if|if
condition|(
name|plan
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|ColumnAccessInfo
name|columnAccessInfo
init|=
name|hookContext
operator|.
name|getQueryPlan
argument_list|()
operator|.
name|getColumnAccessInfo
argument_list|()
decl_stmt|;
if|if
condition|(
name|columnAccessInfo
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|LogHelper
name|console
init|=
name|SessionState
operator|.
name|getConsole
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableToColumnAccessMap
init|=
name|columnAccessInfo
operator|.
name|getTableToColumnAccessMap
argument_list|()
decl_stmt|;
comment|// Must be deterministic order map for consistent test output across Java versions
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|outputOrderedMap
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableAccess
range|:
name|tableToColumnAccessMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|StringBuilder
name|perTableInfo
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|perTableInfo
operator|.
name|append
argument_list|(
literal|"Table:"
argument_list|)
operator|.
name|append
argument_list|(
name|tableAccess
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
comment|// Sort columns to make output deterministic
name|String
index|[]
name|columns
init|=
operator|new
name|String
index|[
name|tableAccess
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|tableAccess
operator|.
name|getValue
argument_list|()
operator|.
name|toArray
argument_list|(
name|columns
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|columns
argument_list|)
expr_stmt|;
name|perTableInfo
operator|.
name|append
argument_list|(
literal|"Columns:"
argument_list|)
operator|.
name|append
argument_list|(
name|StringUtils
operator|.
name|join
argument_list|(
name|columns
argument_list|,
literal|','
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
name|outputOrderedMap
operator|.
name|put
argument_list|(
name|tableAccess
operator|.
name|getKey
argument_list|()
argument_list|,
name|perTableInfo
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|perOperatorInfo
range|:
name|outputOrderedMap
operator|.
name|values
argument_list|()
control|)
block|{
name|console
operator|.
name|printError
argument_list|(
name|perOperatorInfo
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

