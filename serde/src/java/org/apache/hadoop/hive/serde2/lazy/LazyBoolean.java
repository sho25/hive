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
name|serde2
operator|.
name|lazy
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyBooleanObjectInspector
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
name|BooleanWritable
import|;
end_import

begin_comment
comment|/**  * LazyObject for storing a value of boolean.  *   *<p>  * Part of the code is adapted from Apache Harmony Project.  *   * As with the specification, this implementation relied on code laid out in<a  * href="http://www.hackersdelight.org/">Henry S. Warren, Jr.'s Hacker's  * Delight, (Addison Wesley, 2002)</a> as well as<a  * href="http://aggregate.org/MAGIC/">The Aggregate's Magic Algorithms</a>.  *</p>  *   */
end_comment

begin_class
specifier|public
class|class
name|LazyBoolean
extends|extends
name|LazyPrimitive
argument_list|<
name|LazyBooleanObjectInspector
argument_list|,
name|BooleanWritable
argument_list|>
block|{
specifier|public
name|LazyBoolean
parameter_list|(
name|LazyBooleanObjectInspector
name|oi
parameter_list|)
block|{
name|super
argument_list|(
name|oi
argument_list|)
expr_stmt|;
name|data
operator|=
operator|new
name|BooleanWritable
argument_list|()
expr_stmt|;
block|}
specifier|public
name|LazyBoolean
parameter_list|(
name|LazyBoolean
name|copy
parameter_list|)
block|{
name|super
argument_list|(
name|copy
argument_list|)
expr_stmt|;
name|data
operator|=
operator|new
name|BooleanWritable
argument_list|(
name|copy
operator|.
name|data
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|ByteArrayRef
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
block|{
if|if
condition|(
name|length
operator|==
literal|4
operator|&&
name|Character
operator|.
name|toUpperCase
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
index|[
name|start
index|]
argument_list|)
operator|==
literal|'T'
operator|&&
name|Character
operator|.
name|toUpperCase
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
index|[
name|start
operator|+
literal|1
index|]
argument_list|)
operator|==
literal|'R'
operator|&&
name|Character
operator|.
name|toUpperCase
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
index|[
name|start
operator|+
literal|2
index|]
argument_list|)
operator|==
literal|'U'
operator|&&
name|Character
operator|.
name|toUpperCase
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
index|[
name|start
operator|+
literal|3
index|]
argument_list|)
operator|==
literal|'E'
condition|)
block|{
name|data
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|isNull
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|length
operator|==
literal|5
operator|&&
name|Character
operator|.
name|toUpperCase
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
index|[
name|start
index|]
argument_list|)
operator|==
literal|'F'
operator|&&
name|Character
operator|.
name|toUpperCase
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
index|[
name|start
operator|+
literal|1
index|]
argument_list|)
operator|==
literal|'A'
operator|&&
name|Character
operator|.
name|toUpperCase
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
index|[
name|start
operator|+
literal|2
index|]
argument_list|)
operator|==
literal|'L'
operator|&&
name|Character
operator|.
name|toUpperCase
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
index|[
name|start
operator|+
literal|3
index|]
argument_list|)
operator|==
literal|'S'
operator|&&
name|Character
operator|.
name|toUpperCase
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
index|[
name|start
operator|+
literal|4
index|]
argument_list|)
operator|==
literal|'E'
condition|)
block|{
name|data
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|isNull
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|oi
operator|.
name|isExtendedLiteral
argument_list|()
condition|)
block|{
if|if
condition|(
name|length
operator|==
literal|1
condition|)
block|{
name|byte
name|c
init|=
name|bytes
operator|.
name|getData
argument_list|()
index|[
name|start
index|]
decl_stmt|;
if|if
condition|(
name|c
operator|==
literal|'1'
operator|||
name|c
operator|==
literal|'t'
operator|||
name|c
operator|==
literal|'T'
condition|)
block|{
name|data
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|isNull
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|c
operator|==
literal|'0'
operator|||
name|c
operator|==
literal|'f'
operator|||
name|c
operator|==
literal|'F'
condition|)
block|{
name|data
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|isNull
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|isNull
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|isNull
operator|=
literal|true
expr_stmt|;
name|logExceptionMessage
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|,
literal|"BOOLEAN"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

