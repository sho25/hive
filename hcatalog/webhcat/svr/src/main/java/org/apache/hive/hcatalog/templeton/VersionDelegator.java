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
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
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
name|util
operator|.
name|VersionInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|HiveVersionInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|http
operator|.
name|HttpStatus
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|core
operator|.
name|Response
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

begin_comment
comment|/**  * Find the version of Hive, Hadoop, or Pig that is being used in this  * interface.  */
end_comment

begin_class
specifier|public
class|class
name|VersionDelegator
extends|extends
name|TempletonDelegator
block|{
specifier|public
name|VersionDelegator
parameter_list|(
name|AppConfig
name|appConf
parameter_list|)
block|{
name|super
argument_list|(
name|appConf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Response
name|getVersion
parameter_list|(
name|String
name|module
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|module
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
literal|"hadoop"
argument_list|)
condition|)
block|{
return|return
name|getHadoopVersion
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|module
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
literal|"hive"
argument_list|)
condition|)
block|{
return|return
name|getHiveVersion
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|module
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
literal|"sqoop"
argument_list|)
condition|)
block|{
return|return
name|getSqoopVersion
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|module
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
literal|"pig"
argument_list|)
condition|)
block|{
return|return
name|getPigVersion
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|SimpleWebException
operator|.
name|buildMessage
argument_list|(
name|HttpStatus
operator|.
name|NOT_FOUND_404
argument_list|,
literal|null
argument_list|,
literal|"Unknown module "
operator|+
name|module
argument_list|)
return|;
block|}
block|}
specifier|private
name|Response
name|getHadoopVersion
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|version
init|=
name|VersionInfo
operator|.
name|getVersion
argument_list|()
decl_stmt|;
return|return
name|JsonBuilder
operator|.
name|create
argument_list|()
operator|.
name|put
argument_list|(
literal|"module"
argument_list|,
literal|"hadoop"
argument_list|)
operator|.
name|put
argument_list|(
literal|"version"
argument_list|,
name|version
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
name|Response
name|getHiveVersion
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|version
init|=
name|HiveVersionInfo
operator|.
name|getVersion
argument_list|()
decl_stmt|;
return|return
name|JsonBuilder
operator|.
name|create
argument_list|()
operator|.
name|put
argument_list|(
literal|"module"
argument_list|,
literal|"hive"
argument_list|)
operator|.
name|put
argument_list|(
literal|"version"
argument_list|,
name|version
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
name|Response
name|getSqoopVersion
parameter_list|()
block|{
return|return
name|SimpleWebException
operator|.
name|buildMessage
argument_list|(
name|HttpStatus
operator|.
name|NOT_IMPLEMENTED_501
argument_list|,
literal|null
argument_list|,
literal|"Sqoop version request not yet implemented"
argument_list|)
return|;
block|}
specifier|private
name|Response
name|getPigVersion
parameter_list|()
block|{
return|return
name|SimpleWebException
operator|.
name|buildMessage
argument_list|(
name|HttpStatus
operator|.
name|NOT_IMPLEMENTED_501
argument_list|,
literal|null
argument_list|,
literal|"Pig version request not yet implemented"
argument_list|)
return|;
block|}
block|}
end_class

end_unit

