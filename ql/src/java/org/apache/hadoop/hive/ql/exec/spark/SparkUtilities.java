begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ql
operator|.
name|exec
operator|.
name|spark
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
name|java
operator|.
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|UUID
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|io
operator|.
name|FilenameUtils
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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|spark
operator|.
name|session
operator|.
name|SparkSession
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
name|ql
operator|.
name|exec
operator|.
name|spark
operator|.
name|session
operator|.
name|SparkSessionManager
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
name|ql
operator|.
name|io
operator|.
name|HiveKey
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
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
name|ql
operator|.
name|session
operator|.
name|SessionState
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
name|BytesWritable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|SparkConf
import|;
end_import

begin_comment
comment|/**  * Contains utilities methods used as part of Spark tasks.  */
end_comment

begin_class
specifier|public
class|class
name|SparkUtilities
block|{
specifier|public
specifier|static
name|HiveKey
name|copyHiveKey
parameter_list|(
name|HiveKey
name|key
parameter_list|)
block|{
name|HiveKey
name|copy
init|=
operator|new
name|HiveKey
argument_list|()
decl_stmt|;
name|copy
operator|.
name|setDistKeyLength
argument_list|(
name|key
operator|.
name|getDistKeyLength
argument_list|()
argument_list|)
expr_stmt|;
name|copy
operator|.
name|setHashCode
argument_list|(
name|key
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|copy
operator|.
name|set
argument_list|(
name|key
argument_list|)
expr_stmt|;
return|return
name|copy
return|;
block|}
specifier|public
specifier|static
name|BytesWritable
name|copyBytesWritable
parameter_list|(
name|BytesWritable
name|bw
parameter_list|)
block|{
name|BytesWritable
name|copy
init|=
operator|new
name|BytesWritable
argument_list|()
decl_stmt|;
name|copy
operator|.
name|set
argument_list|(
name|bw
argument_list|)
expr_stmt|;
return|return
name|copy
return|;
block|}
specifier|public
specifier|static
name|URI
name|getURI
parameter_list|(
name|String
name|path
parameter_list|)
throws|throws
name|URISyntaxException
block|{
if|if
condition|(
name|path
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|URI
name|uri
init|=
operator|new
name|URI
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|uri
operator|.
name|getScheme
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// if no file schema in path, we assume it's file on local fs.
name|uri
operator|=
operator|new
name|File
argument_list|(
name|path
argument_list|)
operator|.
name|toURI
argument_list|()
expr_stmt|;
block|}
return|return
name|uri
return|;
block|}
comment|/**    * Uploads a local file to HDFS    *    * @param source    * @param conf    * @return    * @throws IOException    */
specifier|public
specifier|static
name|URI
name|uploadToHDFS
parameter_list|(
name|URI
name|source
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|localFile
init|=
operator|new
name|Path
argument_list|(
name|source
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
comment|// give the uploaded file a UUID
name|Path
name|remoteFile
init|=
operator|new
name|Path
argument_list|(
name|SessionState
operator|.
name|getHDFSSessionPath
argument_list|(
name|conf
argument_list|)
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|+
literal|"-"
operator|+
name|getFileName
argument_list|(
name|source
argument_list|)
argument_list|)
decl_stmt|;
name|FileSystem
name|fileSystem
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|fileSystem
operator|.
name|copyFromLocalFile
argument_list|(
name|localFile
argument_list|,
name|remoteFile
argument_list|)
expr_stmt|;
name|Path
name|fullPath
init|=
name|fileSystem
operator|.
name|getFileStatus
argument_list|(
name|remoteFile
argument_list|)
operator|.
name|getPath
argument_list|()
decl_stmt|;
return|return
name|fullPath
operator|.
name|toUri
argument_list|()
return|;
block|}
comment|// checks if a resource has to be uploaded to HDFS for yarn-cluster mode
specifier|public
specifier|static
name|boolean
name|needUploadToHDFS
parameter_list|(
name|URI
name|source
parameter_list|,
name|SparkConf
name|sparkConf
parameter_list|)
block|{
return|return
name|sparkConf
operator|.
name|get
argument_list|(
literal|"spark.master"
argument_list|)
operator|.
name|equals
argument_list|(
literal|"yarn-cluster"
argument_list|)
operator|&&
operator|!
name|source
operator|.
name|getScheme
argument_list|()
operator|.
name|equals
argument_list|(
literal|"hdfs"
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|String
name|getFileName
parameter_list|(
name|URI
name|uri
parameter_list|)
block|{
if|if
condition|(
name|uri
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|String
name|name
init|=
name|FilenameUtils
operator|.
name|getName
argument_list|(
name|uri
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|name
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isDedicatedCluster
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|master
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"spark.master"
argument_list|)
decl_stmt|;
return|return
name|master
operator|.
name|startsWith
argument_list|(
literal|"yarn-"
argument_list|)
operator|||
name|master
operator|.
name|startsWith
argument_list|(
literal|"local"
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|SparkSession
name|getSparkSession
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|SparkSessionManager
name|sparkSessionManager
parameter_list|)
throws|throws
name|HiveException
block|{
name|SparkSession
name|sparkSession
init|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getSparkSession
argument_list|()
decl_stmt|;
comment|// Spark configurations are updated close the existing session
if|if
condition|(
name|conf
operator|.
name|getSparkConfigUpdated
argument_list|()
condition|)
block|{
name|sparkSessionManager
operator|.
name|closeSession
argument_list|(
name|sparkSession
argument_list|)
expr_stmt|;
name|sparkSession
operator|=
literal|null
expr_stmt|;
name|conf
operator|.
name|setSparkConfigUpdated
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|sparkSession
operator|=
name|sparkSessionManager
operator|.
name|getSession
argument_list|(
name|sparkSession
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|setSparkSession
argument_list|(
name|sparkSession
argument_list|)
expr_stmt|;
return|return
name|sparkSession
return|;
block|}
block|}
end_class

end_unit

