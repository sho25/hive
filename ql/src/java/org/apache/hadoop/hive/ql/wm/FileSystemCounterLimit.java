begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|wm
package|;
end_package

begin_comment
comment|/**  * File system specific counters with defined limits  */
end_comment

begin_class
specifier|public
class|class
name|FileSystemCounterLimit
implements|implements
name|CounterLimit
block|{
specifier|public
enum|enum
name|FSCounter
block|{
name|BYTES_READ
block|,
name|BYTES_WRITTEN
block|,
name|SHUFFLE_BYTES
block|}
specifier|private
name|String
name|scheme
decl_stmt|;
specifier|private
name|FSCounter
name|fsCounter
decl_stmt|;
specifier|private
name|long
name|limit
decl_stmt|;
name|FileSystemCounterLimit
parameter_list|(
specifier|final
name|String
name|scheme
parameter_list|,
specifier|final
name|FSCounter
name|fsCounter
parameter_list|,
specifier|final
name|long
name|limit
parameter_list|)
block|{
name|this
operator|.
name|scheme
operator|=
name|scheme
operator|==
literal|null
operator|||
name|scheme
operator|.
name|isEmpty
argument_list|()
condition|?
literal|""
else|:
name|scheme
operator|.
name|toUpperCase
argument_list|()
expr_stmt|;
name|this
operator|.
name|fsCounter
operator|=
name|fsCounter
expr_stmt|;
name|this
operator|.
name|limit
operator|=
name|limit
expr_stmt|;
block|}
specifier|static
name|FileSystemCounterLimit
name|fromName
parameter_list|(
specifier|final
name|String
name|counterName
parameter_list|,
specifier|final
name|long
name|limit
parameter_list|)
block|{
name|String
name|counterNameStr
init|=
name|counterName
operator|.
name|toUpperCase
argument_list|()
decl_stmt|;
for|for
control|(
name|FSCounter
name|fsCounter
range|:
name|FSCounter
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|counterNameStr
operator|.
name|endsWith
argument_list|(
name|fsCounter
operator|.
name|name
argument_list|()
argument_list|)
condition|)
block|{
name|int
name|startIdx
init|=
name|counterNameStr
operator|.
name|indexOf
argument_list|(
name|fsCounter
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|startIdx
operator|==
literal|0
condition|)
block|{
comment|// exact match
return|return
operator|new
name|FileSystemCounterLimit
argument_list|(
literal|null
argument_list|,
name|FSCounter
operator|.
name|valueOf
argument_list|(
name|counterName
argument_list|)
argument_list|,
name|limit
argument_list|)
return|;
block|}
else|else
block|{
name|String
name|scheme
init|=
name|counterNameStr
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|startIdx
operator|-
literal|1
argument_list|)
decl_stmt|;
comment|// schema/counter name validation will be done in grammar as part of HIVE-17622
return|return
operator|new
name|FileSystemCounterLimit
argument_list|(
name|scheme
argument_list|,
name|FSCounter
operator|.
name|valueOf
argument_list|(
name|fsCounter
operator|.
name|name
argument_list|()
argument_list|)
argument_list|,
name|limit
argument_list|)
return|;
block|}
block|}
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid counter name specified "
operator|+
name|counterName
operator|.
name|toUpperCase
argument_list|()
operator|+
literal|""
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|scheme
operator|.
name|isEmpty
argument_list|()
condition|?
name|fsCounter
operator|.
name|name
argument_list|()
else|:
name|scheme
operator|.
name|toUpperCase
argument_list|()
operator|+
literal|"_"
operator|+
name|fsCounter
operator|.
name|name
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLimit
parameter_list|()
block|{
return|return
name|limit
return|;
block|}
annotation|@
name|Override
specifier|public
name|CounterLimit
name|clone
parameter_list|()
block|{
return|return
operator|new
name|FileSystemCounterLimit
argument_list|(
name|scheme
argument_list|,
name|fsCounter
argument_list|,
name|limit
argument_list|)
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
literal|"counter: "
operator|+
name|getName
argument_list|()
operator|+
literal|" limit: "
operator|+
name|limit
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|hash
init|=
literal|31
operator|*
name|scheme
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|hash
operator|+=
literal|31
operator|*
name|fsCounter
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|hash
operator|+=
literal|31
operator|*
name|limit
expr_stmt|;
return|return
literal|31
operator|*
name|hash
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
specifier|final
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|==
literal|null
operator|||
operator|!
operator|(
name|other
operator|instanceof
name|FileSystemCounterLimit
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|other
operator|==
name|this
condition|)
block|{
return|return
literal|true
return|;
block|}
name|FileSystemCounterLimit
name|otherFscl
init|=
operator|(
name|FileSystemCounterLimit
operator|)
name|other
decl_stmt|;
return|return
name|scheme
operator|.
name|equals
argument_list|(
name|otherFscl
operator|.
name|scheme
argument_list|)
operator|&&
name|fsCounter
operator|.
name|equals
argument_list|(
name|otherFscl
operator|.
name|fsCounter
argument_list|)
operator|&&
name|limit
operator|==
name|otherFscl
operator|.
name|limit
return|;
block|}
block|}
end_class

end_unit

