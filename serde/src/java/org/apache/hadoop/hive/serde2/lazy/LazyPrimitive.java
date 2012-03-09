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
operator|.
name|lazy
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|CharacterCodingException
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
name|Text
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
comment|/**  * LazyPrimitive stores a primitive Object in a LazyObject.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|LazyPrimitive
parameter_list|<
name|OI
extends|extends
name|ObjectInspector
parameter_list|,
name|T
extends|extends
name|Writable
parameter_list|>
extends|extends
name|LazyObject
argument_list|<
name|OI
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|LazyPrimitive
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|LazyPrimitive
parameter_list|(
name|OI
name|oi
parameter_list|)
block|{
name|super
argument_list|(
name|oi
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|LazyPrimitive
parameter_list|(
name|LazyPrimitive
argument_list|<
name|OI
argument_list|,
name|T
argument_list|>
name|copy
parameter_list|)
block|{
name|super
argument_list|(
name|copy
operator|.
name|oi
argument_list|)
expr_stmt|;
name|isNull
operator|=
name|copy
operator|.
name|isNull
expr_stmt|;
block|}
specifier|protected
name|T
name|data
decl_stmt|;
specifier|protected
name|boolean
name|isNull
init|=
literal|false
decl_stmt|;
comment|/**    * Returns the primitive object represented by this LazyObject. This is useful    * because it can make sure we have "null" for null objects.    */
annotation|@
name|Override
specifier|public
name|Object
name|getObject
parameter_list|()
block|{
return|return
name|isNull
condition|?
literal|null
else|:
name|this
return|;
block|}
specifier|public
name|T
name|getWritableObject
parameter_list|()
block|{
return|return
name|isNull
condition|?
literal|null
else|:
name|data
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|isNull
condition|?
literal|null
else|:
name|data
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|isNull
condition|?
literal|0
else|:
name|data
operator|.
name|hashCode
argument_list|()
return|;
block|}
specifier|public
name|void
name|logExceptionMessage
parameter_list|(
name|ByteArrayRef
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|,
name|String
name|dataType
parameter_list|)
block|{
try|try
block|{
name|String
name|byteData
init|=
name|Text
operator|.
name|decode
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Data not in the "
operator|+
name|dataType
operator|+
literal|" data type range so converted to null. Given data is :"
operator|+
name|byteData
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CharacterCodingException
name|e1
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Data not in the "
operator|+
name|dataType
operator|+
literal|" data type range so converted to null."
argument_list|,
name|e1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

