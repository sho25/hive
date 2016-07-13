begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  *  you may not use this file except in compliance with the License.  *  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  */
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
name|llap
operator|.
name|tezplugins
package|;
end_package

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|NumberFormat
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
name|classification
operator|.
name|InterfaceAudience
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
name|mapred
operator|.
name|JobConf
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
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|ApplicationId
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|records
operator|.
name|TezDAGID
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|records
operator|.
name|TezTaskAttemptID
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|records
operator|.
name|TezTaskID
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|records
operator|.
name|TezVertexID
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|mapreduce
operator|.
name|hadoop
operator|.
name|MRHelpers
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|mapreduce
operator|.
name|hadoop
operator|.
name|MRInputHelpers
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|mapreduce
operator|.
name|hadoop
operator|.
name|MRJobConfig
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|mapreduce
operator|.
name|input
operator|.
name|MRInput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|mapreduce
operator|.
name|input
operator|.
name|MRInputLegacy
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|mapreduce
operator|.
name|input
operator|.
name|MultiMRInput
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
name|Joiner
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|LlapTezUtils
block|{
specifier|public
specifier|static
name|boolean
name|isSourceOfInterest
parameter_list|(
name|String
name|inputClassName
parameter_list|)
block|{
comment|// MRInput is not of interest since it'll always be ready.
return|return
operator|!
operator|(
name|inputClassName
operator|.
name|equals
argument_list|(
name|MRInputLegacy
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|||
name|inputClassName
operator|.
name|equals
argument_list|(
name|MultiMRInput
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|||
name|inputClassName
operator|.
name|equals
argument_list|(
name|MRInput
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|)
return|;
block|}
specifier|public
specifier|static
name|String
name|getFragmentId
parameter_list|(
specifier|final
name|JobConf
name|job
parameter_list|)
block|{
name|String
name|taskAttemptId
init|=
name|job
operator|.
name|get
argument_list|(
name|MRInput
operator|.
name|TEZ_MAPREDUCE_TASK_ATTEMPT_ID
argument_list|)
decl_stmt|;
if|if
condition|(
name|taskAttemptId
operator|!=
literal|null
condition|)
block|{
return|return
name|stripAttemptPrefix
argument_list|(
name|taskAttemptId
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
specifier|static
name|String
name|stripAttemptPrefix
parameter_list|(
specifier|final
name|String
name|s
parameter_list|)
block|{
if|if
condition|(
name|s
operator|.
name|startsWith
argument_list|(
name|TezTaskAttemptID
operator|.
name|ATTEMPT
argument_list|)
condition|)
block|{
return|return
name|s
operator|.
name|substring
argument_list|(
name|TezTaskAttemptID
operator|.
name|ATTEMPT
operator|.
name|length
argument_list|()
operator|+
literal|1
argument_list|)
return|;
block|}
return|return
name|s
return|;
block|}
block|}
end_class

end_unit

