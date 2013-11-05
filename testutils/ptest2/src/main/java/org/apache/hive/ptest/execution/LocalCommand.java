begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|ptest
operator|.
name|execution
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedReader
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
name|java
operator|.
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStreamReader
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

begin_class
specifier|public
class|class
name|LocalCommand
block|{
specifier|private
specifier|final
name|Process
name|process
decl_stmt|;
specifier|private
specifier|final
name|StreamReader
name|streamReader
decl_stmt|;
specifier|private
name|Integer
name|exitCode
decl_stmt|;
specifier|public
name|LocalCommand
parameter_list|(
name|Logger
name|logger
parameter_list|,
name|OutputPolicy
name|outputPolicy
parameter_list|,
name|String
name|command
parameter_list|)
throws|throws
name|IOException
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Starting "
operator|+
name|command
argument_list|)
expr_stmt|;
name|process
operator|=
operator|new
name|ProcessBuilder
argument_list|()
operator|.
name|command
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"bash"
block|,
literal|"-c"
block|,
name|command
block|}
argument_list|)
operator|.
name|redirectErrorStream
argument_list|(
literal|true
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|streamReader
operator|=
operator|new
name|StreamReader
argument_list|(
name|outputPolicy
argument_list|,
name|process
operator|.
name|getInputStream
argument_list|()
argument_list|)
expr_stmt|;
name|streamReader
operator|.
name|setName
argument_list|(
literal|"StreamReader-["
operator|+
name|command
operator|+
literal|"]"
argument_list|)
expr_stmt|;
name|streamReader
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|streamReader
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
specifier|public
name|int
name|getExitCode
parameter_list|()
throws|throws
name|InterruptedException
block|{
synchronized|synchronized
init|(
name|process
init|)
block|{
if|if
condition|(
name|exitCode
operator|==
literal|null
condition|)
block|{
name|exitCode
operator|=
name|process
operator|.
name|waitFor
argument_list|()
expr_stmt|;
block|}
return|return
name|exitCode
return|;
block|}
block|}
specifier|public
name|void
name|kill
parameter_list|()
block|{
synchronized|synchronized
init|(
name|process
init|)
block|{
name|process
operator|.
name|destroy
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
interface|interface
name|OutputPolicy
block|{
specifier|public
name|void
name|handleOutput
parameter_list|(
name|String
name|line
parameter_list|)
function_decl|;
specifier|public
name|void
name|handleThrowable
parameter_list|(
name|Throwable
name|throwable
parameter_list|)
function_decl|;
block|}
specifier|public
specifier|static
class|class
name|CollectLogPolicy
extends|extends
name|CollectPolicy
block|{
specifier|private
specifier|final
name|Logger
name|logger
decl_stmt|;
specifier|public
name|CollectLogPolicy
parameter_list|(
name|Logger
name|logger
parameter_list|)
block|{
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleOutput
parameter_list|(
name|String
name|line
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
name|line
argument_list|)
expr_stmt|;
name|output
operator|.
name|append
argument_list|(
name|line
argument_list|)
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|CollectPolicy
implements|implements
name|OutputPolicy
block|{
specifier|protected
specifier|final
name|StringBuilder
name|output
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
specifier|protected
name|Throwable
name|throwable
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|handleOutput
parameter_list|(
name|String
name|line
parameter_list|)
block|{
name|output
operator|.
name|append
argument_list|(
name|line
argument_list|)
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleThrowable
parameter_list|(
name|Throwable
name|throwable
parameter_list|)
block|{
if|if
condition|(
name|throwable
operator|instanceof
name|IOException
operator|&&
literal|"Stream closed"
operator|.
name|equals
argument_list|(
name|throwable
operator|.
name|getMessage
argument_list|()
argument_list|)
condition|)
block|{
return|return;
block|}
name|this
operator|.
name|throwable
operator|=
name|throwable
expr_stmt|;
block|}
specifier|public
name|String
name|getOutput
parameter_list|()
block|{
name|String
name|result
init|=
name|output
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|throwable
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|result
argument_list|,
name|throwable
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|StreamReader
extends|extends
name|Thread
block|{
specifier|private
specifier|final
name|BufferedReader
name|input
decl_stmt|;
specifier|private
specifier|final
name|OutputPolicy
name|outputPolicy
decl_stmt|;
specifier|public
name|StreamReader
parameter_list|(
name|OutputPolicy
name|outputPolicy
parameter_list|,
name|InputStream
name|in
parameter_list|)
block|{
name|this
operator|.
name|outputPolicy
operator|=
name|outputPolicy
expr_stmt|;
name|this
operator|.
name|input
operator|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|String
name|line
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|input
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|outputPolicy
operator|.
name|handleOutput
argument_list|(
name|line
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
name|outputPolicy
operator|.
name|handleThrowable
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
try|try
block|{
name|input
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ignored
parameter_list|)
block|{          }
block|}
block|}
block|}
block|}
end_class

end_unit

