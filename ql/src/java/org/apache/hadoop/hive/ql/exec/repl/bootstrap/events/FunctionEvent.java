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
name|repl
operator|.
name|bootstrap
operator|.
name|events
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
name|fs
operator|.
name|Path
import|;
end_import

begin_comment
comment|/**  * Exposing the FileSystem implementation outside which is what it should NOT do.  *<p>  * Since the bootstrap and incremental for functions is handled similarly. There  * is additional work to make sure we pass the event object from both places.  *<p>  * FunctionDescBuilder in {@link org.apache.hadoop.hive.ql.parse.repl.load.message.CreateFunctionHandler}  * would be merged here mostly.  */
end_comment

begin_interface
specifier|public
interface|interface
name|FunctionEvent
extends|extends
name|BootstrapEvent
block|{
name|Path
name|rootDir
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

