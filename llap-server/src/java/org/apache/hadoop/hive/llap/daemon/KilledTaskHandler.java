begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|daemon
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
name|security
operator|.
name|token
operator|.
name|Token
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
name|common
operator|.
name|security
operator|.
name|JobTokenIdentifier
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

begin_interface
specifier|public
interface|interface
name|KilledTaskHandler
block|{
comment|// TODO Ideally, this should only need to send in the TaskAttemptId. Everything else should be
comment|// inferred from this.
comment|// Passing in parameters until there's some dag information stored and tracked in the daemon.
name|void
name|taskKilled
parameter_list|(
name|String
name|amLocation
parameter_list|,
name|int
name|port
parameter_list|,
name|String
name|user
parameter_list|,
name|Token
argument_list|<
name|JobTokenIdentifier
argument_list|>
name|jobToken
parameter_list|,
name|String
name|queryId
parameter_list|,
name|String
name|dagName
parameter_list|,
name|TezTaskAttemptID
name|taskAttemptId
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

