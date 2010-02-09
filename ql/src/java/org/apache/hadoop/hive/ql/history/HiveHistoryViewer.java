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
name|history
package|;
end_package

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
name|HashMap
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
name|history
operator|.
name|HiveHistory
operator|.
name|Keys
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
name|history
operator|.
name|HiveHistory
operator|.
name|Listener
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
name|history
operator|.
name|HiveHistory
operator|.
name|QueryInfo
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
name|history
operator|.
name|HiveHistory
operator|.
name|RecordTypes
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
name|history
operator|.
name|HiveHistory
operator|.
name|TaskInfo
import|;
end_import

begin_comment
comment|/**  * HiveHistoryViewer.  *  */
end_comment

begin_class
specifier|public
class|class
name|HiveHistoryViewer
implements|implements
name|Listener
block|{
name|String
name|historyFile
decl_stmt|;
name|String
name|sessionId
decl_stmt|;
comment|// Job Hash Map
specifier|private
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|QueryInfo
argument_list|>
name|jobInfoMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|QueryInfo
argument_list|>
argument_list|()
decl_stmt|;
comment|// Task Hash Map
specifier|private
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|TaskInfo
argument_list|>
name|taskInfoMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|TaskInfo
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|HiveHistoryViewer
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|historyFile
operator|=
name|path
expr_stmt|;
name|init
argument_list|()
expr_stmt|;
block|}
specifier|public
name|String
name|getSessionId
parameter_list|()
block|{
return|return
name|sessionId
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|QueryInfo
argument_list|>
name|getJobInfoMap
parameter_list|()
block|{
return|return
name|jobInfoMap
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|TaskInfo
argument_list|>
name|getTaskInfoMap
parameter_list|()
block|{
return|return
name|taskInfoMap
return|;
block|}
comment|/**    * Parse history files.    */
name|void
name|init
parameter_list|()
block|{
try|try
block|{
name|HiveHistory
operator|.
name|parseHiveHistory
argument_list|(
name|historyFile
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// TODO Auto-generated catch block
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Implementation Listner interface function.    *     * @see org.apache.hadoop.hive.ql.history.HiveHistory.Listener#handle(org.apache.hadoop.hive.ql.history.HiveHistory.RecordTypes,    *      java.util.Map)    */
specifier|public
name|void
name|handle
parameter_list|(
name|RecordTypes
name|recType
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|values
parameter_list|)
block|{
if|if
condition|(
name|recType
operator|==
name|RecordTypes
operator|.
name|SessionStart
condition|)
block|{
name|sessionId
operator|=
name|values
operator|.
name|get
argument_list|(
name|Keys
operator|.
name|SESSION_ID
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|recType
operator|==
name|RecordTypes
operator|.
name|QueryStart
operator|||
name|recType
operator|==
name|RecordTypes
operator|.
name|QueryEnd
condition|)
block|{
name|String
name|key
init|=
name|values
operator|.
name|get
argument_list|(
name|Keys
operator|.
name|QUERY_ID
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
name|QueryInfo
name|ji
decl_stmt|;
if|if
condition|(
name|jobInfoMap
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|ji
operator|=
name|jobInfoMap
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|ji
operator|.
name|hm
operator|.
name|putAll
argument_list|(
name|values
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ji
operator|=
operator|new
name|QueryInfo
argument_list|()
expr_stmt|;
name|ji
operator|.
name|hm
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|ji
operator|.
name|hm
operator|.
name|putAll
argument_list|(
name|values
argument_list|)
expr_stmt|;
name|jobInfoMap
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|ji
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|recType
operator|==
name|RecordTypes
operator|.
name|TaskStart
operator|||
name|recType
operator|==
name|RecordTypes
operator|.
name|TaskEnd
operator|||
name|recType
operator|==
name|RecordTypes
operator|.
name|TaskProgress
condition|)
block|{
name|String
name|jobid
init|=
name|values
operator|.
name|get
argument_list|(
name|Keys
operator|.
name|QUERY_ID
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|taskid
init|=
name|values
operator|.
name|get
argument_list|(
name|Keys
operator|.
name|TASK_ID
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|key
init|=
name|jobid
operator|+
literal|":"
operator|+
name|taskid
decl_stmt|;
name|TaskInfo
name|ti
decl_stmt|;
if|if
condition|(
name|taskInfoMap
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|ti
operator|=
name|taskInfoMap
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|ti
operator|.
name|hm
operator|.
name|putAll
argument_list|(
name|values
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ti
operator|=
operator|new
name|TaskInfo
argument_list|()
expr_stmt|;
name|ti
operator|.
name|hm
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|ti
operator|.
name|hm
operator|.
name|putAll
argument_list|(
name|values
argument_list|)
expr_stmt|;
name|taskInfoMap
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|ti
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

