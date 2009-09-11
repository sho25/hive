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
name|FileStatus
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
comment|/**  * In order to be compatible with multiple versions of Hadoop, all parts  * of the Hadoop interface that are not cross-version compatible are  * encapsulated in an implementation of this class. Users should use  * the ShimLoader class as a factory to obtain an implementation of  * HadoopShims corresponding to the version of Hadoop currently on the  * classpath.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HadoopShims
block|{
comment|/**    * Return true if the current version of Hadoop uses the JobShell for    * command line interpretation.    */
specifier|public
name|boolean
name|usesJobShell
parameter_list|()
function_decl|;
comment|/**    * Calls fs.deleteOnExit(path) if such a function exists.    *    * @return true if the call was successful    */
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
function_decl|;
comment|/**    * Calls fmt.validateInput(conf) if such a function exists.    */
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
function_decl|;
comment|/**    * If JobClient.getCommandLineConfig exists, sets the given    * property/value pair in that Configuration object.    *    * This applies for Hadoop 0.17 through 0.19    */
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
function_decl|;
comment|/**    * return the last access time of the given file.    * @param file    * @return last access time. -1 if not supported.    */
specifier|public
name|long
name|getAccessTime
parameter_list|(
name|FileStatus
name|file
parameter_list|)
function_decl|;
comment|/**    * Returns a shim to wrap MiniDFSCluster. This is necessary since this class    * was moved from org.apache.hadoop.dfs to org.apache.hadoop.hdfs    */
specifier|public
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
function_decl|;
comment|/**    * Shim around the functions in MiniDFSCluster that Hive uses.    */
specifier|public
interface|interface
name|MiniDFSShim
block|{
specifier|public
name|FileSystem
name|getFileSystem
parameter_list|()
throws|throws
name|IOException
function_decl|;
specifier|public
name|void
name|shutdown
parameter_list|()
throws|throws
name|IOException
function_decl|;
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
function_decl|;
block|}
end_interface

end_unit

