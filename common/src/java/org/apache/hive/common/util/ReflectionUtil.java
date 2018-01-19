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
name|hive
operator|.
name|common
operator|.
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|Configurable
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|cache
operator|.
name|Cache
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|cache
operator|.
name|CacheBuilder
import|;
end_import

begin_comment
comment|/**  * Same as Hadoop ReflectionUtils, but (1) does not leak classloaders (or shouldn't anyway, we  * rely on Guava cache, and could fix it otherwise); (2) does not have a hidden epic lock.  */
end_comment

begin_class
specifier|public
class|class
name|ReflectionUtil
block|{
comment|// TODO: expireAfterAccess locks cache segments on put and expired get. It doesn't look too bad,
comment|//       but if we find some perf issues it might be a good idea to remove this - we are probably
comment|//       not caching that many constructors.
comment|// Note that weakKeys causes "==" to be used for key compare; this will only work
comment|// for classes in the same classloader. Should be ok in this case.
specifier|private
specifier|static
specifier|final
name|Cache
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|,
name|Constructor
argument_list|<
name|?
argument_list|>
argument_list|>
name|CONSTRUCTOR_CACHE
init|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|expireAfterAccess
argument_list|(
literal|15
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
operator|.
name|concurrencyLevel
argument_list|(
literal|64
argument_list|)
operator|.
name|weakKeys
argument_list|()
operator|.
name|weakValues
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|EMPTY_ARRAY
init|=
operator|new
name|Class
index|[]
block|{}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|jobConfClass
decl_stmt|,
name|jobConfigurableClass
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Method
name|configureMethod
decl_stmt|;
static|static
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|jobConfClassLocal
decl_stmt|,
name|jobConfigurableClassLocal
decl_stmt|;
name|Method
name|configureMethodLocal
decl_stmt|;
try|try
block|{
name|jobConfClassLocal
operator|=
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hadoop.mapred.JobConf"
argument_list|)
expr_stmt|;
name|jobConfigurableClassLocal
operator|=
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hadoop.mapred.JobConfigurable"
argument_list|)
expr_stmt|;
name|configureMethodLocal
operator|=
name|jobConfigurableClassLocal
operator|.
name|getMethod
argument_list|(
literal|"configure"
argument_list|,
name|jobConfClassLocal
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
comment|// Meh.
name|jobConfClassLocal
operator|=
name|jobConfigurableClassLocal
operator|=
literal|null
expr_stmt|;
name|configureMethodLocal
operator|=
literal|null
expr_stmt|;
block|}
name|jobConfClass
operator|=
name|jobConfClassLocal
expr_stmt|;
name|jobConfigurableClass
operator|=
name|jobConfigurableClassLocal
expr_stmt|;
name|configureMethod
operator|=
name|configureMethodLocal
expr_stmt|;
block|}
comment|/**    * Create an object for the given class and initialize it from conf    * @param theClass class of which an object is created    * @param conf Configuration    * @return a new object    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|newInstance
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|theClass
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|T
name|result
decl_stmt|;
try|try
block|{
name|Constructor
argument_list|<
name|?
argument_list|>
name|ctor
init|=
name|CONSTRUCTOR_CACHE
operator|.
name|getIfPresent
argument_list|(
name|theClass
argument_list|)
decl_stmt|;
if|if
condition|(
name|ctor
operator|==
literal|null
condition|)
block|{
name|ctor
operator|=
name|theClass
operator|.
name|getDeclaredConstructor
argument_list|(
name|EMPTY_ARRAY
argument_list|)
expr_stmt|;
name|ctor
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|CONSTRUCTOR_CACHE
operator|.
name|put
argument_list|(
name|theClass
argument_list|,
name|ctor
argument_list|)
expr_stmt|;
block|}
name|result
operator|=
operator|(
name|T
operator|)
name|ctor
operator|.
name|newInstance
argument_list|()
expr_stmt|;
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
argument_list|)
throw|;
block|}
name|setConf
argument_list|(
name|result
argument_list|,
name|conf
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * Check and set 'configuration' if necessary.    *     * @param theObject object for which to set configuration    * @param conf Configuration    */
specifier|public
specifier|static
name|void
name|setConf
parameter_list|(
name|Object
name|theObject
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|conf
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|theObject
operator|instanceof
name|Configurable
condition|)
block|{
operator|(
operator|(
name|Configurable
operator|)
name|theObject
operator|)
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
name|setJobConf
argument_list|(
name|theObject
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|setJobConf
parameter_list|(
name|Object
name|theObject
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|configureMethod
operator|==
literal|null
condition|)
return|return;
try|try
block|{
if|if
condition|(
name|jobConfClass
operator|.
name|isAssignableFrom
argument_list|(
name|conf
operator|.
name|getClass
argument_list|()
argument_list|)
operator|&&
name|jobConfigurableClass
operator|.
name|isAssignableFrom
argument_list|(
name|theObject
operator|.
name|getClass
argument_list|()
argument_list|)
condition|)
block|{
name|configureMethod
operator|.
name|invoke
argument_list|(
name|theObject
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
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
literal|"Error in configuring object"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

