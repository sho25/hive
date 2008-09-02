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
name|serde
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
name|io
operator|.
name|Writable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
comment|/**  * Generic Interface to be exported by all serialization/deserialization libraries  * In addition to the interface below, all implementations are assume to have a ctor  * that takes a single 'Table' object as argument.  *  */
end_comment

begin_interface
specifier|public
interface|interface
name|SerDe
block|{
comment|// name of serialization scheme.
comment|//  public static final String SERIALIZATION_LIB = "serialization.lib";
comment|// what class structure is serialized
comment|//  public static final String SERIALIZATION_CLASS = "serialization.class";
comment|// additional info about serialization format
comment|//  public static final String SERIALIZATION_FORMAT = "serialization.format";
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|job
parameter_list|,
name|Properties
name|tbl
parameter_list|)
throws|throws
name|SerDeException
function_decl|;
comment|/**    * Deserialize an object out of a Writable blob    *    * SerDe's can choose a serialization format of their choosing as long as it is of    * type Writable. Two obvious examples are BytesWritable (binary serialized) and    * Text.    * @param blob The Writable object containing a serialized object    * @return A Java object representing the contents in the blob.    */
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
comment|/**    * Serialize an object. Currently this is not required to be implemented    */
specifier|public
name|Writable
name|serialize
parameter_list|(
name|Object
name|obj
parameter_list|)
throws|throws
name|SerDeException
function_decl|;
comment|/**    * Emit as JSON for easy processing in scripting languages    * @param obj The object to be emitted as JSON    * @param hf  The field that the object corresponds to    * @return a valid JSON string representing the contents of the object    */
specifier|public
name|String
name|toJSONString
parameter_list|(
name|Object
name|obj
parameter_list|,
name|SerDeField
name|hf
parameter_list|)
throws|throws
name|SerDeException
function_decl|;
comment|/**    * Get a collection of top level SerDeFields.    * A SerDeFields allows Hive to extract a subcomponent of an object that may    * be returned by this SerDe (from the deseriaize method).    *    * @param parentField the Field relative to which we want to get the subfields    * If parentField is null - then the top level subfields are returned    */
specifier|public
name|List
argument_list|<
name|SerDeField
argument_list|>
name|getFields
parameter_list|(
name|SerDeField
name|parentField
parameter_list|)
throws|throws
name|SerDeException
function_decl|;
comment|/**    * Get a Field Handler that corresponds to the field expression    *    * @param parentField The Field relative to which the expression is defined.    * parentField is null if there is no parent field (evaluating expression    * relative to root object)    * @param fieldExpression A FieldExpression can be a fieldname with some    * associated modifiers. Each SerDe can define it's own expression syntax    * for getting access to fields. Hive provides helper class called ComplexSerDeField    * that provides support for a default field expression syntax that is XPath    * oriented.    * @return A SerDeField that extract a subcomponent of an object denoted by    * the fieldExpression    */
specifier|public
name|SerDeField
name|getFieldFromExpression
parameter_list|(
name|SerDeField
name|parentField
parameter_list|,
name|String
name|fieldExpression
parameter_list|)
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

