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
name|llap
operator|.
name|cli
operator|.
name|service
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Callable
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
name|FileSystem
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
name|common
operator|.
name|CompressionUtils
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
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|TezConfiguration
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

begin_comment
comment|/** Download tez related jars for the tarball. */
end_comment

begin_class
class|class
name|AsyncTaskDownloadTezJars
implements|implements
name|Callable
argument_list|<
name|Void
argument_list|>
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
name|AsyncTaskDownloadTezJars
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|rawFs
decl_stmt|;
specifier|private
specifier|final
name|Path
name|libDir
decl_stmt|;
specifier|private
specifier|final
name|Path
name|tezDir
decl_stmt|;
name|AsyncTaskDownloadTezJars
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|FileSystem
name|rawFs
parameter_list|,
name|Path
name|libDir
parameter_list|,
name|Path
name|tezDir
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|rawFs
operator|=
name|rawFs
expr_stmt|;
name|this
operator|.
name|libDir
operator|=
name|libDir
expr_stmt|;
name|this
operator|.
name|tezDir
operator|=
name|tezDir
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
synchronized|synchronized
init|(
name|fs
init|)
block|{
name|String
name|tezLibs
init|=
name|conf
operator|.
name|get
argument_list|(
name|TezConfiguration
operator|.
name|TEZ_LIB_URIS
argument_list|)
decl_stmt|;
if|if
condition|(
name|tezLibs
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Missing tez.lib.uris in tez-site.xml"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Copying tez libs from "
operator|+
name|tezLibs
argument_list|)
expr_stmt|;
block|}
name|rawFs
operator|.
name|mkdirs
argument_list|(
name|tezDir
argument_list|)
expr_stmt|;
name|fs
operator|.
name|copyToLocalFile
argument_list|(
operator|new
name|Path
argument_list|(
name|tezLibs
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|libDir
argument_list|,
literal|"tez.tar.gz"
argument_list|)
argument_list|)
expr_stmt|;
name|CompressionUtils
operator|.
name|unTar
argument_list|(
operator|new
name|Path
argument_list|(
name|libDir
argument_list|,
literal|"tez.tar.gz"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|tezDir
operator|.
name|toString
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|rawFs
operator|.
name|delete
argument_list|(
operator|new
name|Path
argument_list|(
name|libDir
argument_list|,
literal|"tez.tar.gz"
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

