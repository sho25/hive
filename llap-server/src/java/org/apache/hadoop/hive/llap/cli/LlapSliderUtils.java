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
name|cli
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|fs
operator|.
name|Path
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
name|conf
operator|.
name|HiveConf
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
name|yarn
operator|.
name|exceptions
operator|.
name|YarnException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|slider
operator|.
name|client
operator|.
name|SliderClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|slider
operator|.
name|common
operator|.
name|params
operator|.
name|ActionCreateArgs
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|slider
operator|.
name|common
operator|.
name|params
operator|.
name|ActionDestroyArgs
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|slider
operator|.
name|common
operator|.
name|params
operator|.
name|ActionFreezeArgs
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|slider
operator|.
name|common
operator|.
name|params
operator|.
name|ActionInstallPackageArgs
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|slider
operator|.
name|common
operator|.
name|tools
operator|.
name|SliderUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|slider
operator|.
name|core
operator|.
name|exceptions
operator|.
name|UnknownApplicationInstanceException
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|io
operator|.
name|Files
import|;
end_import

begin_class
specifier|public
class|class
name|LlapSliderUtils
block|{
specifier|private
specifier|static
specifier|final
name|String
name|SLIDER_GZ
init|=
literal|"slider-agent.tar.gz"
decl_stmt|;
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
name|LlapSliderUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|SliderClient
name|createSliderClient
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|SliderClient
name|sliderClient
init|=
operator|new
name|SliderClient
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|serviceInit
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|super
operator|.
name|serviceInit
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|initHadoopBinding
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|Configuration
name|sliderClientConf
init|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|sliderClientConf
operator|=
name|sliderClient
operator|.
name|bindArgs
argument_list|(
name|sliderClientConf
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"help"
block|}
argument_list|)
expr_stmt|;
name|sliderClient
operator|.
name|init
argument_list|(
name|sliderClientConf
argument_list|)
expr_stmt|;
name|sliderClient
operator|.
name|start
argument_list|()
expr_stmt|;
return|return
name|sliderClient
return|;
block|}
specifier|public
specifier|static
name|void
name|startCluster
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|packageName
parameter_list|,
name|Path
name|packageDir
parameter_list|,
name|String
name|queue
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting cluster with "
operator|+
name|name
operator|+
literal|", "
operator|+
name|packageName
operator|+
literal|", "
operator|+
name|queue
operator|+
literal|", "
operator|+
name|packageDir
argument_list|)
expr_stmt|;
name|SliderClient
name|sc
decl_stmt|;
try|try
block|{
name|sc
operator|=
name|createSliderClient
argument_list|(
name|conf
argument_list|)
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
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Executing the freeze command"
argument_list|)
expr_stmt|;
name|ActionFreezeArgs
name|freezeArgs
init|=
operator|new
name|ActionFreezeArgs
argument_list|()
decl_stmt|;
name|freezeArgs
operator|.
name|force
operator|=
literal|true
expr_stmt|;
name|freezeArgs
operator|.
name|setWaittime
argument_list|(
literal|3600
argument_list|)
expr_stmt|;
comment|// Wait forever (or at least for an hour).
try|try
block|{
name|sc
operator|.
name|actionFreeze
argument_list|(
name|name
argument_list|,
name|freezeArgs
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownApplicationInstanceException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"There was no old application instance to freeze"
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Executing the destroy command"
argument_list|)
expr_stmt|;
name|ActionDestroyArgs
name|destroyArg
init|=
operator|new
name|ActionDestroyArgs
argument_list|()
decl_stmt|;
name|destroyArg
operator|.
name|force
operator|=
literal|true
expr_stmt|;
try|try
block|{
name|sc
operator|.
name|actionDestroy
argument_list|(
name|name
argument_list|,
name|destroyArg
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownApplicationInstanceException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"There was no old application instance to destroy"
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Executing the install command"
argument_list|)
expr_stmt|;
name|ActionInstallPackageArgs
name|installArgs
init|=
operator|new
name|ActionInstallPackageArgs
argument_list|()
decl_stmt|;
name|installArgs
operator|.
name|name
operator|=
literal|"LLAP"
expr_stmt|;
name|installArgs
operator|.
name|packageURI
operator|=
operator|new
name|Path
argument_list|(
name|packageDir
argument_list|,
name|packageName
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
name|installArgs
operator|.
name|replacePkg
operator|=
literal|true
expr_stmt|;
name|sc
operator|.
name|actionInstallPkg
argument_list|(
name|installArgs
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Executing the create command"
argument_list|)
expr_stmt|;
name|ActionCreateArgs
name|createArgs
init|=
operator|new
name|ActionCreateArgs
argument_list|()
decl_stmt|;
name|createArgs
operator|.
name|resources
operator|=
operator|new
name|File
argument_list|(
operator|new
name|Path
argument_list|(
name|packageDir
argument_list|,
literal|"resources.json"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|createArgs
operator|.
name|template
operator|=
operator|new
name|File
argument_list|(
operator|new
name|Path
argument_list|(
name|packageDir
argument_list|,
literal|"appConfig.json"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|createArgs
operator|.
name|setWaittime
argument_list|(
literal|3600
argument_list|)
expr_stmt|;
if|if
condition|(
name|queue
operator|!=
literal|null
condition|)
block|{
name|createArgs
operator|.
name|queue
operator|=
name|queue
expr_stmt|;
block|}
comment|// See the comments in the method. SliderClient doesn't work in normal circumstances.
name|File
name|bogusSliderFile
init|=
name|startSetSliderLibDir
argument_list|()
decl_stmt|;
try|try
block|{
name|sc
operator|.
name|actionCreate
argument_list|(
name|name
argument_list|,
name|createArgs
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|endSetSliderLibDir
argument_list|(
name|bogusSliderFile
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Started the cluster via slider API"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|YarnException
decl||
name|IOException
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
finally|finally
block|{
try|try
block|{
name|sc
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed to close slider client"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|File
name|startSetSliderLibDir
parameter_list|()
throws|throws
name|IOException
block|{
comment|// TODO: this is currently required for the use of slider create API. Need SLIDER-1192.
name|File
name|sliderJarDir
init|=
name|SliderUtils
operator|.
name|findContainingJar
argument_list|(
name|SliderClient
operator|.
name|class
argument_list|)
operator|.
name|getParentFile
argument_list|()
decl_stmt|;
name|File
name|gz
init|=
operator|new
name|File
argument_list|(
name|sliderJarDir
argument_list|,
name|SLIDER_GZ
argument_list|)
decl_stmt|;
if|if
condition|(
name|gz
operator|.
name|exists
argument_list|()
condition|)
block|{
name|String
name|path
init|=
name|sliderJarDir
operator|.
name|getAbsolutePath
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting slider.libdir based on jar file location: "
operator|+
name|path
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"slider.libdir"
argument_list|,
name|path
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
comment|// There's no gz file next to slider jars. Due to the horror that is SliderClient, we'd have
comment|// to find it and copy it there. Let's try to find it. Also set slider.libdir.
name|String
name|path
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"slider.libdir"
argument_list|)
decl_stmt|;
name|gz
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|path
operator|!=
literal|null
operator|&&
operator|!
name|path
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"slider.libdir was already set: "
operator|+
name|path
argument_list|)
expr_stmt|;
name|gz
operator|=
operator|new
name|File
argument_list|(
name|path
argument_list|,
name|SLIDER_GZ
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|gz
operator|.
name|exists
argument_list|()
condition|)
block|{
name|gz
operator|=
literal|null
expr_stmt|;
block|}
block|}
if|if
condition|(
name|gz
operator|==
literal|null
condition|)
block|{
name|path
operator|=
name|System
operator|.
name|getenv
argument_list|(
literal|"SLIDER_HOME"
argument_list|)
expr_stmt|;
if|if
condition|(
name|path
operator|!=
literal|null
operator|&&
operator|!
name|path
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|gz
operator|=
operator|new
name|File
argument_list|(
operator|new
name|File
argument_list|(
name|path
argument_list|,
literal|"lib"
argument_list|)
argument_list|,
name|SLIDER_GZ
argument_list|)
expr_stmt|;
if|if
condition|(
name|gz
operator|.
name|exists
argument_list|()
condition|)
block|{
name|path
operator|=
name|gz
operator|.
name|getParentFile
argument_list|()
operator|.
name|getAbsolutePath
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting slider.libdir based on SLIDER_HOME: "
operator|+
name|path
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"slider.libdir"
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|gz
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|gz
operator|==
literal|null
condition|)
block|{
comment|// This is a terrible hack trying to find slider on a typical installation. Sigh...
name|File
name|rootDir
init|=
name|SliderUtils
operator|.
name|findContainingJar
argument_list|(
name|HiveConf
operator|.
name|class
argument_list|)
operator|.
name|getParentFile
argument_list|()
operator|.
name|getParentFile
argument_list|()
operator|.
name|getParentFile
argument_list|()
decl_stmt|;
name|File
name|sliderJarDir2
init|=
operator|new
name|File
argument_list|(
operator|new
name|File
argument_list|(
name|rootDir
argument_list|,
literal|"slider"
argument_list|)
argument_list|,
literal|"lib"
argument_list|)
decl_stmt|;
if|if
condition|(
name|sliderJarDir2
operator|.
name|exists
argument_list|()
condition|)
block|{
name|gz
operator|=
operator|new
name|File
argument_list|(
name|sliderJarDir2
argument_list|,
name|SLIDER_GZ
argument_list|)
expr_stmt|;
if|if
condition|(
name|gz
operator|.
name|exists
argument_list|()
condition|)
block|{
name|path
operator|=
name|sliderJarDir2
operator|.
name|getAbsolutePath
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting slider.libdir based on guesswork: "
operator|+
name|path
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"slider.libdir"
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|gz
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|gz
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot find "
operator|+
name|SLIDER_GZ
operator|+
literal|". Please ensure SLIDER_HOME is set."
argument_list|)
throw|;
block|}
name|File
name|newGz
init|=
operator|new
name|File
argument_list|(
name|sliderJarDir
argument_list|,
name|SLIDER_GZ
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Copying "
operator|+
name|gz
operator|+
literal|" to "
operator|+
name|newGz
argument_list|)
expr_stmt|;
name|Files
operator|.
name|copy
argument_list|(
name|gz
argument_list|,
name|newGz
argument_list|)
expr_stmt|;
name|newGz
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
return|return
name|newGz
return|;
block|}
specifier|public
specifier|static
name|void
name|endSetSliderLibDir
parameter_list|(
name|File
name|newGz
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|newGz
operator|==
literal|null
operator|||
operator|!
name|newGz
operator|.
name|exists
argument_list|()
condition|)
return|return;
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleting "
operator|+
name|newGz
argument_list|)
expr_stmt|;
name|newGz
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

