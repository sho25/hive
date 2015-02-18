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
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|MapredContext
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
name|exec
operator|.
name|FunctionRegistry
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
name|exec
operator|.
name|UDFArgumentException
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
name|ConstantObjectInspector
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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorUtils
import|;
end_import

begin_comment
comment|/**  * A Generic User-defined function (GenericUDF) for the use with Hive.  *  * New GenericUDF classes need to inherit from this GenericUDF class.  *  * The GenericUDF are superior to normal UDFs in the following ways: 1. It can  * accept arguments of complex types, and return complex types. 2. It can accept  * variable length of arguments. 3. It can accept an infinite number of function  * signature - for example, it's easy to write a GenericUDF that accepts  * array<int>, array<array<int>> and so on (arbitrary levels of nesting). 4. It  * can do short-circuit evaluations using DeferedObject.  */
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
implements|implements
name|Closeable
block|{
comment|/**    * A Defered Object allows us to do lazy-evaluation and short-circuiting.    * GenericUDF use DeferedObject to pass arguments.    */
specifier|public
specifier|static
interface|interface
name|DeferredObject
block|{
name|void
name|prepare
parameter_list|(
name|int
name|version
parameter_list|)
throws|throws
name|HiveException
function_decl|;
name|Object
name|get
parameter_list|()
throws|throws
name|HiveException
function_decl|;
block|}
empty_stmt|;
comment|/**    * A basic dummy implementation of DeferredObject which just stores a Java    * Object reference.    */
specifier|public
specifier|static
class|class
name|DeferredJavaObject
implements|implements
name|DeferredObject
block|{
specifier|private
name|Object
name|value
decl_stmt|;
specifier|public
name|DeferredJavaObject
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|prepare
parameter_list|(
name|int
name|version
parameter_list|)
throws|throws
name|HiveException
block|{     }
annotation|@
name|Override
specifier|public
name|Object
name|get
parameter_list|()
throws|throws
name|HiveException
block|{
return|return
name|value
return|;
block|}
block|}
comment|/**    * The constructor.    */
specifier|public
name|GenericUDF
parameter_list|()
block|{   }
comment|/**    * Initialize this GenericUDF. This will be called once and only once per    * GenericUDF instance.    *    * @param arguments    *          The ObjectInspector for the arguments    * @throws UDFArgumentException    *           Thrown when arguments have wrong types, wrong length, etc.    * @return The ObjectInspector for the return value    */
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
name|UDFArgumentException
function_decl|;
comment|/**    * Additionally setup GenericUDF with MapredContext before initializing.    * This is only called in runtime of MapRedTask.    *    * @param context context    */
specifier|public
name|void
name|configure
parameter_list|(
name|MapredContext
name|context
parameter_list|)
block|{   }
comment|/**    * Initialize this GenericUDF.  Additionally, if the arguments are constant    * and the function is eligible to be folded, then the constant value    * returned by this UDF will be computed and stored in the    * ConstantObjectInspector returned.  Otherwise, the function behaves exactly    * like initialize().    */
specifier|public
name|ObjectInspector
name|initializeAndFoldConstants
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
name|ObjectInspector
name|oi
init|=
name|initialize
argument_list|(
name|arguments
argument_list|)
decl_stmt|;
comment|// If the UDF depends on any external resources, we can't fold because the
comment|// resources may not be available at compile time.
if|if
condition|(
name|getRequiredFiles
argument_list|()
operator|!=
literal|null
operator|||
name|getRequiredJars
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|oi
return|;
block|}
name|boolean
name|allConstant
init|=
literal|true
decl_stmt|;
for|for
control|(
name|int
name|ii
init|=
literal|0
init|;
name|ii
operator|<
name|arguments
operator|.
name|length
condition|;
operator|++
name|ii
control|)
block|{
if|if
condition|(
operator|!
name|ObjectInspectorUtils
operator|.
name|isConstantObjectInspector
argument_list|(
name|arguments
index|[
name|ii
index|]
argument_list|)
condition|)
block|{
name|allConstant
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|allConstant
operator|&&
operator|!
name|ObjectInspectorUtils
operator|.
name|isConstantObjectInspector
argument_list|(
name|oi
argument_list|)
operator|&&
name|FunctionRegistry
operator|.
name|isDeterministic
argument_list|(
name|this
argument_list|)
operator|&&
operator|!
name|FunctionRegistry
operator|.
name|isStateful
argument_list|(
name|this
argument_list|)
operator|&&
name|ObjectInspectorUtils
operator|.
name|supportsConstantObjectInspector
argument_list|(
name|oi
argument_list|)
condition|)
block|{
name|DeferredObject
index|[]
name|argumentValues
init|=
operator|new
name|DeferredJavaObject
index|[
name|arguments
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|ii
init|=
literal|0
init|;
name|ii
operator|<
name|arguments
operator|.
name|length
condition|;
operator|++
name|ii
control|)
block|{
name|argumentValues
index|[
name|ii
index|]
operator|=
operator|new
name|DeferredJavaObject
argument_list|(
operator|(
operator|(
name|ConstantObjectInspector
operator|)
name|arguments
index|[
name|ii
index|]
operator|)
operator|.
name|getWritableConstantValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|Object
name|constantValue
init|=
name|evaluate
argument_list|(
name|argumentValues
argument_list|)
decl_stmt|;
name|oi
operator|=
name|ObjectInspectorUtils
operator|.
name|getConstantObjectInspector
argument_list|(
name|oi
argument_list|,
name|constantValue
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|oi
return|;
block|}
comment|/**    * The following two functions can be overridden to automatically include    * additional resources required by this UDF.  The return types should be    * arrays of paths.    */
specifier|public
name|String
index|[]
name|getRequiredJars
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
specifier|public
name|String
index|[]
name|getRequiredFiles
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
comment|/**    * Evaluate the GenericUDF with the arguments.    *    * @param arguments    *          The arguments as DeferedObject, use DeferedObject.get() to get the    *          actual argument Object. The Objects can be inspected by the    *          ObjectInspectors passed in the initialize call.    * @return The    */
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
comment|/**    * Close GenericUDF.    * This is only called in runtime of MapRedTask.    */
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{   }
comment|/**    * Some functions are affected by appearing order of arguments (comparisons, for example)    */
specifier|public
name|GenericUDF
name|flip
parameter_list|()
block|{
return|return
name|this
return|;
block|}
specifier|public
name|String
name|getUdfName
parameter_list|()
block|{
return|return
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
return|;
block|}
comment|/**    * Some information may be set during initialize() which needs to be saved when the UDF is copied.    * This will be called by FunctionRegistry.cloneGenericUDF()    */
specifier|public
name|void
name|copyToNewInstance
parameter_list|(
name|Object
name|newInstance
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
comment|// newInstance should always be the same type of object as this
if|if
condition|(
name|this
operator|.
name|getClass
argument_list|()
operator|!=
name|newInstance
operator|.
name|getClass
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|"Invalid copy between "
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" and "
operator|+
name|newInstance
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|String
name|getStandardDisplayString
parameter_list|(
name|String
name|name
parameter_list|,
name|String
index|[]
name|children
parameter_list|)
block|{
return|return
name|getStandardDisplayString
argument_list|(
name|name
argument_list|,
name|children
argument_list|,
literal|", "
argument_list|)
return|;
block|}
specifier|protected
name|String
name|getStandardDisplayString
parameter_list|(
name|String
name|name
parameter_list|,
name|String
index|[]
name|children
parameter_list|,
name|String
name|delim
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"("
argument_list|)
expr_stmt|;
if|if
condition|(
name|children
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|children
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|children
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|delim
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|children
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|sb
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

