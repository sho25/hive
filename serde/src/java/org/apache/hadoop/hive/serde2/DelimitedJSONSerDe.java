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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|serde
operator|.
name|serdeConstants
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
name|lazy
operator|.
name|LazySimpleSerDe
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
name|ObjectInspector
operator|.
name|Category
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
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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
comment|/**  * DelimitedJSONSerDe.  *  * This serde can only serialize, because it is just intended for use by the FetchTask class and the  * TRANSFORM function.  */
end_comment

begin_class
specifier|public
class|class
name|DelimitedJSONSerDe
extends|extends
name|LazySimpleSerDe
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|DelimitedJSONSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|DelimitedJSONSerDe
parameter_list|()
throws|throws
name|SerDeException
block|{   }
comment|/**    * Not implemented.    */
annotation|@
name|Override
specifier|public
name|Object
name|deserialize
parameter_list|(
name|Writable
name|field
parameter_list|)
throws|throws
name|SerDeException
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"DelimitedJSONSerDe cannot deserialize."
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"DelimitedJSONSerDe cannot deserialize."
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|serializeField
parameter_list|(
name|ByteStream
operator|.
name|Output
name|out
parameter_list|,
name|Object
name|obj
parameter_list|,
name|ObjectInspector
name|objInspector
parameter_list|,
name|SerDeParameters
name|serdeParams
parameter_list|)
throws|throws
name|SerDeException
block|{
if|if
condition|(
operator|!
name|objInspector
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|Category
operator|.
name|PRIMITIVE
argument_list|)
operator|||
operator|(
name|objInspector
operator|.
name|getTypeName
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|serdeConstants
operator|.
name|BINARY_TYPE_NAME
argument_list|)
operator|)
condition|)
block|{
comment|//do this for all complex types and binary
try|try
block|{
name|serialize
argument_list|(
name|out
argument_list|,
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|obj
argument_list|,
name|objInspector
argument_list|,
name|serdeParams
operator|.
name|getNullSequence
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|,
name|serdeParams
operator|.
name|getSeparators
argument_list|()
argument_list|,
literal|1
argument_list|,
name|serdeParams
operator|.
name|getNullSequence
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|isEscaped
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getEscapeChar
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getNeedsEscape
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
else|else
block|{
comment|//primitives except binary
name|super
operator|.
name|serializeField
argument_list|(
name|out
argument_list|,
name|obj
argument_list|,
name|objInspector
argument_list|,
name|serdeParams
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

