begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|metastore
operator|.
name|messaging
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
name|conf
operator|.
name|Configuration
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
name|metastore
operator|.
name|conf
operator|.
name|MetastoreConf
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
name|metastore
operator|.
name|messaging
operator|.
name|json
operator|.
name|JSONMessageEncoder
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
name|metastore
operator|.
name|messaging
operator|.
name|json
operator|.
name|gzip
operator|.
name|GzipJSONMessageEncoder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|InvocationTargetException
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
name|lang
operator|.
name|reflect
operator|.
name|Modifier
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * Abstract Factory for the construction of HCatalog message instances.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|MessageFactory
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|MessageFactory
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|Configuration
name|conf
init|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Method
argument_list|>
name|registry
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|void
name|register
parameter_list|(
name|String
name|messageFormat
parameter_list|,
name|Class
name|clazz
parameter_list|)
block|{
name|Method
name|method
init|=
name|requiredMethod
argument_list|(
name|clazz
argument_list|)
decl_stmt|;
name|registry
operator|.
name|put
argument_list|(
name|messageFormat
argument_list|,
name|method
argument_list|)
expr_stmt|;
block|}
static|static
block|{
name|register
argument_list|(
name|GzipJSONMessageEncoder
operator|.
name|FORMAT
argument_list|,
name|GzipJSONMessageEncoder
operator|.
name|class
argument_list|)
expr_stmt|;
name|register
argument_list|(
name|JSONMessageEncoder
operator|.
name|FORMAT
argument_list|,
name|JSONMessageEncoder
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|Method
name|requiredMethod
parameter_list|(
name|Class
name|clazz
parameter_list|)
block|{
if|if
condition|(
name|MessageEncoder
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|clazz
argument_list|)
condition|)
block|{
try|try
block|{
name|Method
name|methodInstance
init|=
name|clazz
operator|.
name|getMethod
argument_list|(
literal|"getInstance"
argument_list|)
decl_stmt|;
if|if
condition|(
name|MessageEncoder
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|methodInstance
operator|.
name|getReturnType
argument_list|()
argument_list|)
condition|)
block|{
name|int
name|modifiers
init|=
name|methodInstance
operator|.
name|getModifiers
argument_list|()
decl_stmt|;
if|if
condition|(
name|Modifier
operator|.
name|isStatic
argument_list|(
name|modifiers
argument_list|)
operator|&&
name|Modifier
operator|.
name|isPublic
argument_list|(
name|modifiers
argument_list|)
condition|)
block|{
return|return
name|methodInstance
return|;
block|}
throw|throw
operator|new
name|NoSuchMethodException
argument_list|(
literal|"modifier for getInstance() method is not 'public static' in "
operator|+
name|clazz
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
throw|;
block|}
throw|throw
operator|new
name|NoSuchMethodException
argument_list|(
literal|"return type is not assignable to "
operator|+
name|MessageEncoder
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|e
parameter_list|)
block|{
name|String
name|message
init|=
name|clazz
operator|.
name|getCanonicalName
argument_list|()
operator|+
literal|" does not implement the required 'public static MessageEncoder getInstance()' method "
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|message
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|message
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
name|String
name|message
init|=
name|clazz
operator|.
name|getCanonicalName
argument_list|()
operator|+
literal|" is not assignable to "
operator|+
name|MessageEncoder
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|message
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|message
argument_list|)
throw|;
block|}
specifier|public
specifier|static
name|MessageEncoder
name|getInstance
parameter_list|(
name|String
name|messageFormat
parameter_list|)
throws|throws
name|InvocationTargetException
throws|,
name|IllegalAccessException
block|{
name|Method
name|methodInstance
init|=
name|registry
operator|.
name|get
argument_list|(
name|messageFormat
argument_list|)
decl_stmt|;
if|if
condition|(
name|methodInstance
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"received incorrect MessageFormat "
operator|+
name|messageFormat
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"messageFormat: "
operator|+
name|messageFormat
operator|+
literal|" is not supported "
argument_list|)
throw|;
block|}
return|return
operator|(
name|MessageEncoder
operator|)
name|methodInstance
operator|.
name|invoke
argument_list|(
literal|null
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|MessageEncoder
name|getDefaultInstance
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|clazz
init|=
name|MetastoreConf
operator|.
name|get
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|EVENT_MESSAGE_FACTORY
operator|.
name|getVarname
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|clazzObject
init|=
name|MessageFactory
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
operator|.
name|loadClass
argument_list|(
name|clazz
argument_list|)
decl_stmt|;
return|return
operator|(
name|MessageEncoder
operator|)
name|requiredMethod
argument_list|(
name|clazzObject
argument_list|)
operator|.
name|invoke
argument_list|(
literal|null
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|String
name|message
init|=
literal|"could not load the configured class "
operator|+
name|clazz
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|message
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|message
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

