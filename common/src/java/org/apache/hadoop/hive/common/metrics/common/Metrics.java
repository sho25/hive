begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|common
operator|.
name|metrics
operator|.
name|common
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Generic Metics interface.  */
end_comment

begin_interface
specifier|public
interface|interface
name|Metrics
block|{
comment|//Must declare CTOR taking in HiveConf.
comment|/**    * Deinitializes the Metrics system.    */
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|Exception
function_decl|;
comment|/**    * @param name    * @throws IOException    */
specifier|public
name|void
name|startScope
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|void
name|endScope
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|//Counter-related methods
comment|/**    * Increments a counter of the given name by 1.    * @param name    * @return    * @throws IOException    */
specifier|public
name|Long
name|incrementCounter
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Increments a counter of the given name by "increment"    * @param name    * @param increment    * @return    * @throws IOException    */
specifier|public
name|Long
name|incrementCounter
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|increment
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Decrements a counter of the given name by 1.    * @param name    * @return    * @throws IOException    */
specifier|public
name|Long
name|decrementCounter
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Decrements a counter of the given name by "decrement"    * @param name    * @param decrement    * @return    * @throws IOException    */
specifier|public
name|Long
name|decrementCounter
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|decrement
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Adds a metrics-gauge to track variable.  For example, number of open database connections.    * @param name name of gauge    * @param variable variable to track.    * @throws IOException    */
specifier|public
name|void
name|addGauge
parameter_list|(
name|String
name|name
parameter_list|,
specifier|final
name|MetricsVariable
name|variable
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

