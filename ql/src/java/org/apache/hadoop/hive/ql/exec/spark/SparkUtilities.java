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
name|Collection
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
name|base
operator|.
name|Preconditions
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
name|Operator
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
name|TaskFactory
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
name|plan
operator|.
name|BaseWork
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
name|plan
operator|.
name|SparkWork
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
name|Dependency
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaPairRDD
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
name|rdd
operator|.
name|RDD
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
name|rdd
operator|.
name|UnionRDD
import|;
end_import

begin_import
import|import
name|scala
operator|.
name|collection
operator|.
name|JavaConversions
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
comment|// Overwrite if the remote file already exists. Whether the file can be added
comment|// on executor is up to spark, i.e. spark.files.overwrite
name|fileSystem
operator|.
name|copyFromLocalFile
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|,
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
name|HiveConf
name|sessionConf
init|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getConf
argument_list|()
decl_stmt|;
comment|// Spark configurations are updated close the existing session
comment|// In case of async queries or confOverlay is not empty,
comment|// sessionConf and conf are different objects
if|if
condition|(
name|sessionConf
operator|.
name|getSparkConfigUpdated
argument_list|()
operator|||
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
name|sessionConf
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
specifier|public
specifier|static
name|String
name|rddGraphToString
parameter_list|(
name|JavaPairRDD
name|rdd
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|rddToString
argument_list|(
name|rdd
operator|.
name|rdd
argument_list|()
argument_list|,
name|sb
argument_list|,
literal|""
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|void
name|rddToString
parameter_list|(
name|RDD
name|rdd
parameter_list|,
name|StringBuilder
name|sb
parameter_list|,
name|String
name|offset
parameter_list|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|offset
argument_list|)
operator|.
name|append
argument_list|(
name|rdd
operator|.
name|getClass
argument_list|()
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"["
argument_list|)
operator|.
name|append
argument_list|(
name|rdd
operator|.
name|hashCode
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
if|if
condition|(
name|rdd
operator|.
name|getStorageLevel
argument_list|()
operator|.
name|useMemory
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"(cached)"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|Dependency
argument_list|>
name|dependencies
init|=
name|JavaConversions
operator|.
name|asJavaCollection
argument_list|(
name|rdd
operator|.
name|dependencies
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|dependencies
operator|!=
literal|null
condition|)
block|{
name|offset
operator|+=
literal|"\t"
expr_stmt|;
for|for
control|(
name|Dependency
name|dependency
range|:
name|dependencies
control|)
block|{
name|RDD
name|parentRdd
init|=
name|dependency
operator|.
name|rdd
argument_list|()
decl_stmt|;
name|rddToString
argument_list|(
name|parentRdd
argument_list|,
name|sb
argument_list|,
name|offset
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|rdd
operator|instanceof
name|UnionRDD
condition|)
block|{
name|UnionRDD
name|unionRDD
init|=
operator|(
name|UnionRDD
operator|)
name|rdd
decl_stmt|;
name|offset
operator|+=
literal|"\t"
expr_stmt|;
name|Collection
argument_list|<
name|RDD
argument_list|>
name|parentRdds
init|=
name|JavaConversions
operator|.
name|asJavaCollection
argument_list|(
name|unionRDD
operator|.
name|rdds
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|RDD
name|parentRdd
range|:
name|parentRdds
control|)
block|{
name|rddToString
argument_list|(
name|parentRdd
argument_list|,
name|sb
argument_list|,
name|offset
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Generate a temporary path for dynamic partition pruning in Spark branch    * TODO: no longer need this if we use accumulator!    * @param basePath    * @param id    * @return    */
specifier|public
specifier|static
name|Path
name|generateTmpPathForPartitionPruning
parameter_list|(
name|Path
name|basePath
parameter_list|,
name|String
name|id
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|basePath
argument_list|,
name|id
argument_list|)
return|;
block|}
comment|/**    * Return the ID for this BaseWork, in String form.    * @param work the input BaseWork    * @return the unique ID for this BaseWork    */
specifier|public
specifier|static
name|String
name|getWorkId
parameter_list|(
name|BaseWork
name|work
parameter_list|)
block|{
name|String
name|workName
init|=
name|work
operator|.
name|getName
argument_list|()
decl_stmt|;
return|return
name|workName
operator|.
name|substring
argument_list|(
name|workName
operator|.
name|indexOf
argument_list|(
literal|" "
argument_list|)
operator|+
literal|1
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|SparkTask
name|createSparkTask
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
return|return
operator|(
name|SparkTask
operator|)
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|SparkWork
argument_list|(
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYID
argument_list|)
argument_list|)
argument_list|,
name|conf
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|SparkTask
name|createSparkTask
parameter_list|(
name|SparkWork
name|work
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
block|{
return|return
operator|(
name|SparkTask
operator|)
name|TaskFactory
operator|.
name|get
argument_list|(
name|work
argument_list|,
name|conf
argument_list|)
return|;
block|}
comment|/**    * Recursively find all operators under root, that are of class clazz, and    * put them in result.    * @param result all operators under root that are of class clazz    * @param root the root operator under which all operators will be examined    * @param clazz clas to collect. Must NOT be null.    */
specifier|public
specifier|static
name|void
name|collectOp
parameter_list|(
name|Collection
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|result
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|root
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|clazz
operator|!=
literal|null
argument_list|,
literal|"AssertionError: clazz should not be null"
argument_list|)
expr_stmt|;
if|if
condition|(
name|root
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|clazz
operator|.
name|equals
argument_list|(
name|root
operator|.
name|getClass
argument_list|()
argument_list|)
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|root
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|child
range|:
name|root
operator|.
name|getChildOperators
argument_list|()
control|)
block|{
name|collectOp
argument_list|(
name|result
argument_list|,
name|child
argument_list|,
name|clazz
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

