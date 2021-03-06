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
name|common
operator|.
name|metrics
package|;
end_package

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|DynamicMBean
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|JMException
import|;
end_import

begin_comment
comment|/**  * MBean definition for metrics tracking from jmx  */
end_comment

begin_interface
specifier|public
interface|interface
name|MetricsMBean
extends|extends
name|DynamicMBean
block|{
comment|/**      * Check if we're tracking a certain named key/metric      */
specifier|public
specifier|abstract
name|boolean
name|hasKey
parameter_list|(
name|String
name|name
parameter_list|)
function_decl|;
comment|/**      * Add a key/metric and its value to track      * @param name Name of the key/metric      * @param value value associated with the key      */
specifier|public
specifier|abstract
name|void
name|put
parameter_list|(
name|String
name|name
parameter_list|,
name|Object
name|value
parameter_list|)
function_decl|;
comment|/**      *      * @param name      * @return value associated with the key      * @throws JMException      */
specifier|public
specifier|abstract
name|Object
name|get
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|JMException
function_decl|;
comment|/**      * Removes all the keys and values from this MetricsMBean.      */
name|void
name|clear
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

