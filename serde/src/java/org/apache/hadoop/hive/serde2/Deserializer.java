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
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_comment
comment|/**  * HiveDeserializer is used to deserialize the data from hadoop Writable to a   * custom java object that can be of any type that the developer wants.  *   * HiveDeserializer also provides the ObjectInspector which can be used to inspect   * the internal structure of the object (that is returned by deserialize function).  *  */
end_comment

begin_interface
specifier|public
interface|interface
name|Deserializer
block|{
comment|/**    * Initialize the HiveDeserializer.    * @param conf System properties    * @param tbl  table properties    * @throws SerDeException    */
specifier|public
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
comment|/**    * Deserialize an object out of a Writable blob.    * In most cases, the return value of this function will be constant since the function    * will reuse the returned object.    * If the client wants to keep a copy of the object, the client needs to clone the    * returned value by calling ObjectInspectorUtils.getStandardObject().    * @param blob The Writable object containing a serialized object    * @return A Java object representing the contents in the blob.    */
specifier|public
name|Object
name|deserialize
parameter_list|(
name|Writable
name|blob
parameter_list|)
throws|throws
name|SerDeException
function_decl|;
comment|/**    * Get the object inspector that can be used to navigate through the internal    * structure of the Object returned from deserialize(...).    */
specifier|public
name|ObjectInspector
name|getObjectInspector
parameter_list|()
throws|throws
name|SerDeException
function_decl|;
specifier|public
name|String
name|getShortName
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

