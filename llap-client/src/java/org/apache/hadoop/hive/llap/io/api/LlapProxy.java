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
name|llap
operator|.
name|io
operator|.
name|api
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
name|llap
operator|.
name|coordinator
operator|.
name|LlapCoordinator
import|;
end_import

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
specifier|public
class|class
name|LlapProxy
block|{
specifier|private
specifier|final
specifier|static
name|String
name|IO_IMPL_CLASS
init|=
literal|"org.apache.hadoop.hive.llap.io.api.impl.LlapIoImpl"
decl_stmt|;
comment|// Llap server depends on Hive execution, so the reverse cannot be true. We create the I/O
comment|// singleton once (on daemon startup); the said singleton serves as the IO interface.
specifier|private
specifier|static
name|LlapIo
name|io
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|isDaemon
init|=
literal|false
decl_stmt|;
specifier|public
specifier|static
name|void
name|setDaemon
parameter_list|(
name|boolean
name|isDaemon
parameter_list|)
block|{
name|LlapProxy
operator|.
name|isDaemon
operator|=
name|isDaemon
expr_stmt|;
block|}
specifier|public
specifier|static
name|boolean
name|isDaemon
parameter_list|()
block|{
return|return
name|isDaemon
return|;
block|}
specifier|public
specifier|static
name|LlapIo
name|getIo
parameter_list|()
block|{
return|return
name|io
return|;
block|}
specifier|public
specifier|static
name|void
name|initializeLlapIo
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|io
operator|!=
literal|null
condition|)
block|{
return|return;
comment|// already initialized
block|}
name|io
operator|=
name|createInstance
argument_list|(
name|IO_IMPL_CLASS
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|createInstance
parameter_list|(
name|String
name|className
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
try|try
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Class
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|clazz
init|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|T
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|)
decl_stmt|;
name|Constructor
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|ctor
init|=
name|clazz
operator|.
name|getDeclaredConstructor
argument_list|(
name|Configuration
operator|.
name|class
argument_list|)
decl_stmt|;
name|ctor
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|ctor
operator|.
name|newInstance
argument_list|(
name|conf
argument_list|)
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
literal|"Failed to create "
operator|+
name|className
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|io
operator|!=
literal|null
condition|)
block|{
name|io
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

