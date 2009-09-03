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
name|shims
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
name|io
operator|.
name|Text
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
name|dfs
operator|.
name|MiniDFSCluster
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
name|mapred
operator|.
name|InputFormat
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
name|mapred
operator|.
name|JobClient
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
name|mapred
operator|.
name|JobConf
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
comment|/**  * Implemention of shims against Hadoop 0.18.0  */
end_comment

begin_class
specifier|public
class|class
name|Hadoop18Shims
implements|implements
name|HadoopShims
block|{
specifier|public
name|boolean
name|usesJobShell
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
specifier|public
name|boolean
name|fileSystemDeleteOnExit
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fs
operator|.
name|deleteOnExit
argument_list|(
name|path
argument_list|)
return|;
block|}
specifier|public
name|void
name|inputFormatValidateInput
parameter_list|(
name|InputFormat
name|fmt
parameter_list|,
name|JobConf
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
comment|// gone in 0.18+
block|}
comment|/**    * workaround for hadoop-17 - jobclient only looks at commandlineconfig    */
specifier|public
name|void
name|setTmpFiles
parameter_list|(
name|String
name|prop
parameter_list|,
name|String
name|files
parameter_list|)
block|{
name|Configuration
name|conf
init|=
name|JobClient
operator|.
name|getCommandLineConfig
argument_list|()
decl_stmt|;
if|if
condition|(
name|conf
operator|!=
literal|null
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|prop
argument_list|,
name|files
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|HadoopShims
operator|.
name|MiniDFSShim
name|getMiniDfs
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|int
name|numDataNodes
parameter_list|,
name|boolean
name|format
parameter_list|,
name|String
index|[]
name|racks
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|MiniDFSShim
argument_list|(
operator|new
name|MiniDFSCluster
argument_list|(
name|conf
argument_list|,
name|numDataNodes
argument_list|,
name|format
argument_list|,
name|racks
argument_list|)
argument_list|)
return|;
block|}
specifier|public
class|class
name|MiniDFSShim
implements|implements
name|HadoopShims
operator|.
name|MiniDFSShim
block|{
specifier|private
name|MiniDFSCluster
name|cluster
decl_stmt|;
specifier|public
name|MiniDFSShim
parameter_list|(
name|MiniDFSCluster
name|cluster
parameter_list|)
block|{
name|this
operator|.
name|cluster
operator|=
name|cluster
expr_stmt|;
block|}
specifier|public
name|FileSystem
name|getFileSystem
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|cluster
operator|.
name|getFileSystem
argument_list|()
return|;
block|}
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
name|cluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * We define this function here to make the code compatible between    * hadoop 0.17 and hadoop 0.20.    *    * Hive binary that compiled Text.compareTo(Text) with hadoop 0.20 won't    * work with hadoop 0.17 because in hadoop 0.20, Text.compareTo(Text) is    * implemented in org.apache.hadoop.io.BinaryComparable, and Java compiler    * references that class, which is not available in hadoop 0.17.    */
specifier|public
name|int
name|compareText
parameter_list|(
name|Text
name|a
parameter_list|,
name|Text
name|b
parameter_list|)
block|{
return|return
name|a
operator|.
name|compareTo
argument_list|(
name|b
argument_list|)
return|;
block|}
block|}
end_class

end_unit

