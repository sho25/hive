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
name|common
package|;
end_package

begin_comment
comment|/**  * Collection of Java class loading/reflection related utilities common across  * Hive.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|JavaUtils
block|{
comment|/**    * Standard way of getting classloader in Hive code (outside of Hadoop).    *     * Uses the context loader to get access to classpaths to auxiliary and jars    * added with 'add jar' command. Falls back to current classloader.    *     * In Hadoop-related code, we use Configuration.getClassLoader().    */
specifier|public
specifier|static
name|ClassLoader
name|getClassLoader
parameter_list|()
block|{
name|ClassLoader
name|classLoader
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getContextClassLoader
argument_list|()
decl_stmt|;
if|if
condition|(
name|classLoader
operator|==
literal|null
condition|)
block|{
name|classLoader
operator|=
name|JavaUtils
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
expr_stmt|;
block|}
return|return
name|classLoader
return|;
block|}
specifier|private
name|JavaUtils
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit

