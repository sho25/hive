begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|api
operator|.
name|repl
package|;
end_package

begin_comment
comment|/**  * Interface for a client to provide a Staging Directory specification  */
end_comment

begin_interface
specifier|public
interface|interface
name|StagingDirectoryProvider
block|{
comment|/**    * Return a temporary staging directory for a given key    * @param key key for the directory, usually a name of a partition    * Note that when overriding this method, no guarantees are made about the    * contents of the key, other than that is unique per partition.    * @return A parth specification to use as a temporary staging directory    */
name|String
name|getStagingDirectory
parameter_list|(
name|String
name|key
parameter_list|)
function_decl|;
comment|/**    * Trivial implementation of this interface - creates    */
specifier|public
class|class
name|TrivialImpl
implements|implements
name|StagingDirectoryProvider
block|{
name|String
name|prefix
init|=
literal|null
decl_stmt|;
comment|/**      * Trivial implementation of StagingDirectoryProvider which takes a temporary directory      * and creates directories inside that for each key. Note that this is intended as a      * trivial implementation, and if any further "advanced" behaviour is desired,      * it is better that the user roll their own.      *      * @param base temp directory inside which other tmp dirs are created      * @param separator path separator. Usually should be "/"      */
specifier|public
name|TrivialImpl
parameter_list|(
name|String
name|base
parameter_list|,
name|String
name|separator
parameter_list|)
block|{
name|this
operator|.
name|prefix
operator|=
name|base
operator|+
name|separator
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getStagingDirectory
parameter_list|(
name|String
name|key
parameter_list|)
block|{
return|return
name|prefix
operator|+
name|key
return|;
block|}
block|}
block|}
end_interface

end_unit

