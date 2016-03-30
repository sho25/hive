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
name|hive
operator|.
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
import|;
end_import

begin_class
specifier|public
class|class
name|LlapUtil
block|{
specifier|public
specifier|static
name|String
name|getDaemonLocalDirList
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|localDirList
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_WORK_DIRS
argument_list|)
decl_stmt|;
if|if
condition|(
name|localDirList
operator|!=
literal|null
operator|&&
operator|!
name|localDirList
operator|.
name|isEmpty
argument_list|()
condition|)
return|return
name|localDirList
return|;
return|return
name|conf
operator|.
name|get
argument_list|(
literal|"yarn.nodemanager.local-dirs"
argument_list|)
return|;
block|}
block|}
end_class

end_unit

