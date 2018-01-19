begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|conf
operator|.
name|Configuration
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
name|mapreduce
operator|.
name|MRJobConfig
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Strings
import|;
end_import

begin_class
specifier|public
class|class
name|DagUtils
block|{
specifier|public
specifier|static
specifier|final
name|String
name|MAPREDUCE_WORKFLOW_NODE_NAME
init|=
literal|"mapreduce.workflow.node.name"
decl_stmt|;
specifier|public
specifier|static
name|String
name|getQueryName
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|name
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYNAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|isNullOrEmpty
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
name|conf
operator|.
name|get
argument_list|(
name|MRJobConfig
operator|.
name|JOB_NAME
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|name
operator|+
literal|" ("
operator|+
name|conf
operator|.
name|get
argument_list|(
name|DagUtils
operator|.
name|MAPREDUCE_WORKFLOW_NODE_NAME
argument_list|)
operator|+
literal|")"
return|;
block|}
block|}
block|}
end_class

end_unit

