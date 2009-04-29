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
name|ql
operator|.
name|udf
operator|.
name|generic
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
name|ql
operator|.
name|exec
operator|.
name|UDFArgumentTypeException
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
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
name|ql
operator|.
name|udf
operator|.
name|UDFType
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

begin_comment
comment|/**  * A Generic User-defined function (GenericUDF) for the use with Hive.  *   * New GenericUDF classes need to inherit from this GenericUDF class.  *   * The GenericUDF are superior to normal UDFs in the following ways:  * 1. It can accept arguments of complex types, and return complex types.  * 2. It can accept variable length of arguments.  * 3. It can accept an infinite number of function signature - for example,   *    it's easy to write a GenericUDF that accepts array<int>,   *    array<array<int>> and so on (arbitrary levels of nesting).  * 4. It can do short-circuit evaluations using DeferedObject.    */
end_comment

begin_class
annotation|@
name|UDFType
argument_list|(
name|deterministic
operator|=
literal|true
argument_list|)
specifier|public
specifier|abstract
class|class
name|GenericUDF
block|{
comment|/**    * A Defered Object allows us to do lazy-evaluation    * and short-circuiting.    * GenericUDF use DeferedObject to pass arguments.    */
specifier|public
specifier|static
interface|interface
name|DeferredObject
block|{
specifier|public
name|Object
name|get
parameter_list|()
throws|throws
name|HiveException
function_decl|;
block|}
empty_stmt|;
comment|/**    * The constructor    */
specifier|public
name|GenericUDF
parameter_list|()
block|{   }
comment|/**    * Initialize this GenericUDF. This will be called once and only once per    * GenericUDF instance.    *     * @param arguments     The ObjectInspector for the arguments    * @throws UDFArgumentTypeException    *                      Thrown when arguments have wrong types    * @return              The ObjectInspector for the return value    */
specifier|public
specifier|abstract
name|ObjectInspector
name|initialize
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|)
throws|throws
name|UDFArgumentTypeException
function_decl|;
comment|/**    * Evaluate the GenericUDF with the arguments.    * @param arguments  The arguments as DeferedObject, use DeferedObject.get() to    *                   get the actual argument Object.  The Objects can be inspected    *                   by the ObjectInspectors passed in the initialize call.    * @return The     */
specifier|public
specifier|abstract
name|Object
name|evaluate
parameter_list|(
name|DeferredObject
index|[]
name|arguments
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * Get the String to be displayed in explain.    */
specifier|public
specifier|abstract
name|String
name|getDisplayString
parameter_list|(
name|String
index|[]
name|children
parameter_list|)
function_decl|;
block|}
end_class

end_unit

