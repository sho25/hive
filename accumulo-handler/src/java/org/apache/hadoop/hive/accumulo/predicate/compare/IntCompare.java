begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|accumulo
operator|.
name|predicate
operator|.
name|compare
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_comment
comment|/**  * Set of comparison operations over a integer constant. Used for Hive predicates involving int  * comparison.  *  * Used by {@link org.apache.hadoop.hive.accumulo.predicate.PrimitiveComparisonFilter}  */
end_comment

begin_class
specifier|public
class|class
name|IntCompare
implements|implements
name|PrimitiveComparison
block|{
specifier|private
name|int
name|constant
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|byte
index|[]
name|constant
parameter_list|)
block|{
name|this
operator|.
name|constant
operator|=
name|serialize
argument_list|(
name|constant
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isEqual
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|serialize
argument_list|(
name|value
argument_list|)
operator|==
name|constant
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isNotEqual
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|serialize
argument_list|(
name|value
argument_list|)
operator|!=
name|constant
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|greaterThanOrEqual
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|serialize
argument_list|(
name|value
argument_list|)
operator|>=
name|constant
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|greaterThan
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|serialize
argument_list|(
name|value
argument_list|)
operator|>
name|constant
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|lessThanOrEqual
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|serialize
argument_list|(
name|value
argument_list|)
operator|<=
name|constant
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|lessThan
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|serialize
argument_list|(
name|value
argument_list|)
operator|<
name|constant
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|like
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Like not supported for "
operator|+
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
specifier|public
name|Integer
name|serialize
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
try|try
block|{
return|return
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|value
argument_list|)
operator|.
name|asIntBuffer
argument_list|()
operator|.
name|get
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
operator|.
name|toString
argument_list|()
operator|+
literal|" occurred trying to build int value. "
operator|+
literal|"Make sure the value type for the byte[] is int "
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

