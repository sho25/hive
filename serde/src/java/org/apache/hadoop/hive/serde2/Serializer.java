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
name|serde2
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|io
operator|.
name|Writable
import|;
end_import

begin_comment
comment|/**  * HiveSerializer is used to serialize data to a Hadoop Writable object. The  * serialize In addition to the interface below, all implementations are assume  * to have a ctor that takes a single 'Table' object as argument.  *  */
end_comment

begin_interface
specifier|public
interface|interface
name|Serializer
block|{
comment|/**    * Initialize the HiveSerializer.    *    * @param conf    *          System properties    * @param tbl    *          table properties    * @throws SerDeException    */
name|void
name|initialize
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Properties
name|tbl
parameter_list|)
throws|throws
name|SerDeException
function_decl|;
comment|/**    * Returns the Writable class that would be returned by the serialize method.    * This is used to initialize SequenceFile header.    */
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|getSerializedClass
parameter_list|()
function_decl|;
comment|/**    * Serialize an object by navigating inside the Object with the    * ObjectInspector. In most cases, the return value of this function will be    * constant since the function will reuse the Writable object. If the client    * wants to keep a copy of the Writable, the client needs to clone the    * returned value.    */
name|Writable
name|serialize
parameter_list|(
name|Object
name|obj
parameter_list|,
name|ObjectInspector
name|objInspector
parameter_list|)
throws|throws
name|SerDeException
function_decl|;
comment|/**    * Returns statistics collected when serializing    */
name|SerDeStats
name|getSerDeStats
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

