begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetAddress
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
name|UnknownHostException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayDeque
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|java
operator|.
name|util
operator|.
name|Deque
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|GroupByOperator
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
name|OperatorUtils
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
name|SelectOperator
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
name|TableScanOperator
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
name|Task
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
name|parse
operator|.
name|spark
operator|.
name|OptimizeSparkProcContext
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
name|parse
operator|.
name|spark
operator|.
name|SparkPartitionPruningSinkOperator
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
name|hive
operator|.
name|spark
operator|.
name|client
operator|.
name|SparkClientUtilities
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
name|get
argument_list|()
operator|.
name|getSparkSession
argument_list|()
operator|.
name|getHDFSSessionDir
argument_list|()
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
name|remoteFile
operator|.
name|toUri
argument_list|()
argument_list|,
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
name|String
name|master
init|=
name|sparkConf
operator|.
name|get
argument_list|(
literal|"spark.master"
argument_list|)
decl_stmt|;
name|String
name|deployMode
init|=
name|sparkConf
operator|.
name|contains
argument_list|(
literal|"spark.submit.deployMode"
argument_list|)
condition|?
name|sparkConf
operator|.
name|get
argument_list|(
literal|"spark.submit.deployMode"
argument_list|)
else|:
literal|null
decl_stmt|;
return|return
name|SparkClientUtilities
operator|.
name|isYarnClusterMode
argument_list|(
name|master
argument_list|,
name|deployMode
argument_list|)
operator|&&
operator|!
operator|(
name|source
operator|.
name|getScheme
argument_list|()
operator|.
name|equals
argument_list|(
literal|"hdfs"
argument_list|)
operator|||
name|source
operator|.
name|getScheme
argument_list|()
operator|.
name|equals
argument_list|(
literal|"viewfs"
argument_list|)
operator|)
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
name|SparkClientUtilities
operator|.
name|isYarnMaster
argument_list|(
name|master
argument_list|)
operator|||
name|SparkClientUtilities
operator|.
name|isLocalMaster
argument_list|(
name|master
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
comment|/**    * Recursively find all operators under root, that are of class clazz or are the sub-class of clazz, and    * put them in result.    * @param result all operators under root that are of class clazz    * @param root the root operator under which all operators will be examined    * @param clazz clas to collect. Must NOT be null.    */
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
name|isAssignableFrom
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
comment|/**    * Collect operators of type T starting from root. Matching operators will be put into result.    * Set seen can be used to skip search in certain branches.    */
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Operator
argument_list|<
name|?
argument_list|>
parameter_list|>
name|void
name|collectOp
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|root
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|cls
parameter_list|,
name|Collection
argument_list|<
name|T
argument_list|>
name|result
parameter_list|,
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|seen
parameter_list|)
block|{
if|if
condition|(
name|seen
operator|.
name|contains
argument_list|(
name|root
argument_list|)
condition|)
block|{
return|return;
block|}
name|Deque
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|deque
init|=
operator|new
name|ArrayDeque
argument_list|<>
argument_list|()
decl_stmt|;
name|deque
operator|.
name|add
argument_list|(
name|root
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|deque
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|op
init|=
name|deque
operator|.
name|remove
argument_list|()
decl_stmt|;
name|seen
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
if|if
condition|(
name|cls
operator|.
name|isInstance
argument_list|(
name|op
argument_list|)
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
operator|(
name|T
operator|)
name|op
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|op
operator|.
name|getChildOperators
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|child
range|:
name|op
operator|.
name|getChildOperators
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|seen
operator|.
name|contains
argument_list|(
name|child
argument_list|)
condition|)
block|{
name|deque
operator|.
name|add
argument_list|(
name|child
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
comment|/**    * remove currTask from the children of its parentTask    * remove currTask from the parent of its childrenTask    * @param currTask    */
specifier|public
specifier|static
name|void
name|removeEmptySparkTask
parameter_list|(
name|SparkTask
name|currTask
parameter_list|)
block|{
comment|//remove currTask from parentTasks
name|ArrayList
argument_list|<
name|Task
argument_list|>
name|parTasks
init|=
operator|new
name|ArrayList
argument_list|<
name|Task
argument_list|>
argument_list|()
decl_stmt|;
name|parTasks
operator|.
name|addAll
argument_list|(
name|currTask
operator|.
name|getParentTasks
argument_list|()
argument_list|)
expr_stmt|;
name|Object
index|[]
name|parTaskArr
init|=
name|parTasks
operator|.
name|toArray
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
name|parTask
range|:
name|parTaskArr
control|)
block|{
operator|(
operator|(
name|Task
operator|)
name|parTask
operator|)
operator|.
name|removeDependentTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
block|}
comment|//remove currTask from childTasks
name|currTask
operator|.
name|removeFromChildrenTasks
argument_list|()
expr_stmt|;
block|}
comment|/**    * For DPP sinks w/ common join, we'll split the tree and what's above the branching    * operator is computed multiple times. Therefore it may not be good for performance to support    * nested DPP sinks, i.e. one DPP sink depends on other DPP sinks.    * The following is an example:    *    *             TS          TS    *             |           |    *            ...         FIL    *            |           |  \    *            RS         RS  SEL    *              \        /    |    *     TS          JOIN      GBY    *     |         /     \      |    *    RS        RS    SEL   DPP2    *     \       /       |    *       JOIN         GBY    *                     |    *                    DPP1    *    * where DPP1 depends on DPP2.    *    * To avoid such case, we'll visit all the branching operators. If a branching operator has any    * further away DPP branches w/ common join in its sub-tree, such branches will be removed.    * In the above example, the branch of DPP1 will be removed.    */
specifier|public
specifier|static
name|void
name|removeNestedDPP
parameter_list|(
name|OptimizeSparkProcContext
name|procContext
parameter_list|)
block|{
name|Set
argument_list|<
name|SparkPartitionPruningSinkOperator
argument_list|>
name|allDPPs
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|seen
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
comment|// collect all DPP sinks
for|for
control|(
name|TableScanOperator
name|root
range|:
name|procContext
operator|.
name|getParseContext
argument_list|()
operator|.
name|getTopOps
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|SparkUtilities
operator|.
name|collectOp
argument_list|(
name|root
argument_list|,
name|SparkPartitionPruningSinkOperator
operator|.
name|class
argument_list|,
name|allDPPs
argument_list|,
name|seen
argument_list|)
expr_stmt|;
block|}
comment|// collect all branching operators
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|branchingOps
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|SparkPartitionPruningSinkOperator
name|dpp
range|:
name|allDPPs
control|)
block|{
name|branchingOps
operator|.
name|add
argument_list|(
name|dpp
operator|.
name|getBranchingOp
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// remember the branching ops we have visited
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|visited
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|branchingOp
range|:
name|branchingOps
control|)
block|{
if|if
condition|(
operator|!
name|visited
operator|.
name|contains
argument_list|(
name|branchingOp
argument_list|)
condition|)
block|{
name|visited
operator|.
name|add
argument_list|(
name|branchingOp
argument_list|)
expr_stmt|;
name|seen
operator|.
name|clear
argument_list|()
expr_stmt|;
name|Set
argument_list|<
name|SparkPartitionPruningSinkOperator
argument_list|>
name|nestedDPPs
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|branch
range|:
name|branchingOp
operator|.
name|getChildOperators
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|isDirectDPPBranch
argument_list|(
name|branch
argument_list|)
condition|)
block|{
name|SparkUtilities
operator|.
name|collectOp
argument_list|(
name|branch
argument_list|,
name|SparkPartitionPruningSinkOperator
operator|.
name|class
argument_list|,
name|nestedDPPs
argument_list|,
name|seen
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|SparkPartitionPruningSinkOperator
name|nestedDPP
range|:
name|nestedDPPs
control|)
block|{
name|visited
operator|.
name|add
argument_list|(
name|nestedDPP
operator|.
name|getBranchingOp
argument_list|()
argument_list|)
expr_stmt|;
comment|// if a DPP is with MJ, the tree won't be split and so we don't have to remove it
if|if
condition|(
operator|!
name|nestedDPP
operator|.
name|isWithMapjoin
argument_list|()
condition|)
block|{
name|OperatorUtils
operator|.
name|removeBranch
argument_list|(
name|nestedDPP
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
comment|// whether of pattern "SEL - GBY - DPP"
specifier|private
specifier|static
name|boolean
name|isDirectDPPBranch
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
parameter_list|)
block|{
if|if
condition|(
name|op
operator|instanceof
name|SelectOperator
operator|&&
name|op
operator|.
name|getChildOperators
argument_list|()
operator|!=
literal|null
operator|&&
name|op
operator|.
name|getChildOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|op
operator|=
name|op
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
if|if
condition|(
name|op
operator|instanceof
name|GroupByOperator
operator|&&
name|op
operator|.
name|getChildOperators
argument_list|()
operator|!=
literal|null
operator|&&
name|op
operator|.
name|getChildOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|op
operator|=
name|op
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
return|return
name|op
operator|instanceof
name|SparkPartitionPruningSinkOperator
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|public
specifier|static
name|String
name|reverseDNSLookupURL
parameter_list|(
name|String
name|url
parameter_list|)
throws|throws
name|UnknownHostException
block|{
comment|// Run a reverse DNS lookup on the URL
name|URI
name|uri
init|=
name|URI
operator|.
name|create
argument_list|(
name|url
argument_list|)
decl_stmt|;
name|InetAddress
name|address
init|=
name|InetAddress
operator|.
name|getByName
argument_list|(
name|uri
operator|.
name|getHost
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|uri
operator|.
name|getScheme
argument_list|()
operator|+
literal|"://"
operator|+
name|address
operator|.
name|getCanonicalHostName
argument_list|()
operator|+
literal|":"
operator|+
name|uri
operator|.
name|getPort
argument_list|()
return|;
block|}
block|}
end_class

end_unit

