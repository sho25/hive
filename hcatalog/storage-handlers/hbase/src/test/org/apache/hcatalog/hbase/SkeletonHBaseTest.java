begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|hbase
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
name|util
operator|.
name|HashMap
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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
name|hbase
operator|.
name|HBaseConfiguration
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
name|hbase
operator|.
name|HColumnDescriptor
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
name|hbase
operator|.
name|HTableDescriptor
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
name|hbase
operator|.
name|client
operator|.
name|HBaseAdmin
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
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_comment
comment|/**  * Base class for HBase Tests which need a mini cluster instance  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|SkeletonHBaseTest
block|{
specifier|protected
specifier|static
name|String
name|TEST_DIR
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|,
literal|"target/tmp/"
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|String
name|DEFAULT_CONTEXT_HANDLE
init|=
literal|"default"
decl_stmt|;
specifier|protected
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|Context
argument_list|>
name|contextMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Context
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|tableNames
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Allow tests to alter the default MiniCluster configuration.    * (requires static initializer block as all setup here is static)    */
specifier|protected
specifier|static
name|Configuration
name|testConf
init|=
literal|null
decl_stmt|;
specifier|protected
name|void
name|createTable
parameter_list|(
name|String
name|tableName
parameter_list|,
name|String
index|[]
name|families
parameter_list|)
throws|throws
name|IOException
block|{
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|getHbaseConf
argument_list|()
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|tableDesc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|family
range|:
name|families
control|)
block|{
name|HColumnDescriptor
name|columnDescriptor
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
decl_stmt|;
name|tableDesc
operator|.
name|addFamily
argument_list|(
name|columnDescriptor
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|createTable
argument_list|(
name|tableDesc
argument_list|)
expr_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|String
name|newTableName
parameter_list|(
name|String
name|prefix
parameter_list|)
block|{
name|String
name|name
init|=
literal|null
decl_stmt|;
name|int
name|tries
init|=
literal|100
decl_stmt|;
do|do
block|{
name|name
operator|=
name|prefix
operator|+
literal|"_"
operator|+
name|Math
operator|.
name|abs
argument_list|(
operator|new
name|Random
argument_list|()
operator|.
name|nextLong
argument_list|()
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|tableNames
operator|.
name|contains
argument_list|(
name|name
argument_list|)
operator|&&
operator|--
name|tries
operator|>
literal|0
condition|)
do|;
if|if
condition|(
name|tableNames
operator|.
name|contains
argument_list|(
name|name
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Couldn't find a unique table name, tableNames size: "
operator|+
name|tableNames
operator|.
name|size
argument_list|()
argument_list|)
throw|;
block|}
name|tableNames
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|name
return|;
block|}
comment|/**    * startup an hbase cluster instance before a test suite runs    */
specifier|public
specifier|static
name|void
name|setupSkeletonHBaseTest
parameter_list|()
block|{
if|if
condition|(
operator|!
name|contextMap
operator|.
name|containsKey
argument_list|(
name|getContextHandle
argument_list|()
argument_list|)
condition|)
block|{
name|contextMap
operator|.
name|put
argument_list|(
name|getContextHandle
argument_list|()
argument_list|,
operator|new
name|Context
argument_list|(
name|getContextHandle
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|contextMap
operator|.
name|get
argument_list|(
name|getContextHandle
argument_list|()
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
comment|/**    * shutdown an hbase cluster instance ant the end of the test suite    */
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
block|{
name|contextMap
operator|.
name|get
argument_list|(
name|getContextHandle
argument_list|()
argument_list|)
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
comment|/**    * override this with a different context handle if tests suites are run simultaneously    * and ManyMiniCluster instances shouldn't be shared    * @return    */
specifier|public
specifier|static
name|String
name|getContextHandle
parameter_list|()
block|{
return|return
name|DEFAULT_CONTEXT_HANDLE
return|;
block|}
comment|/**    * @return working directory for a given test context, which normally is a test suite    */
specifier|public
name|String
name|getTestDir
parameter_list|()
block|{
return|return
name|contextMap
operator|.
name|get
argument_list|(
name|getContextHandle
argument_list|()
argument_list|)
operator|.
name|getTestDir
argument_list|()
return|;
block|}
comment|/**    * @return ManyMiniCluster instance    */
specifier|public
name|ManyMiniCluster
name|getCluster
parameter_list|()
block|{
return|return
name|contextMap
operator|.
name|get
argument_list|(
name|getContextHandle
argument_list|()
argument_list|)
operator|.
name|getCluster
argument_list|()
return|;
block|}
comment|/**    * @return configuration of MiniHBaseCluster    */
specifier|public
name|Configuration
name|getHbaseConf
parameter_list|()
block|{
return|return
name|contextMap
operator|.
name|get
argument_list|(
name|getContextHandle
argument_list|()
argument_list|)
operator|.
name|getHbaseConf
argument_list|()
return|;
block|}
comment|/**    * @return configuration of MiniMRCluster    */
specifier|public
name|Configuration
name|getJobConf
parameter_list|()
block|{
return|return
name|contextMap
operator|.
name|get
argument_list|(
name|getContextHandle
argument_list|()
argument_list|)
operator|.
name|getJobConf
argument_list|()
return|;
block|}
comment|/**    * @return configuration of Hive Metastore    */
specifier|public
name|HiveConf
name|getHiveConf
parameter_list|()
block|{
return|return
name|contextMap
operator|.
name|get
argument_list|(
name|getContextHandle
argument_list|()
argument_list|)
operator|.
name|getHiveConf
argument_list|()
return|;
block|}
comment|/**    * @return filesystem used by ManyMiniCluster daemons    */
specifier|public
name|FileSystem
name|getFileSystem
parameter_list|()
block|{
return|return
name|contextMap
operator|.
name|get
argument_list|(
name|getContextHandle
argument_list|()
argument_list|)
operator|.
name|getFileSystem
argument_list|()
return|;
block|}
comment|/**    * class used to encapsulate a context which is normally used by    * a single TestSuite or across TestSuites when multi-threaded testing is turned on    */
specifier|public
specifier|static
class|class
name|Context
block|{
specifier|protected
name|String
name|testDir
decl_stmt|;
specifier|protected
name|ManyMiniCluster
name|cluster
decl_stmt|;
specifier|protected
name|Configuration
name|hbaseConf
decl_stmt|;
specifier|protected
name|Configuration
name|jobConf
decl_stmt|;
specifier|protected
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|protected
name|FileSystem
name|fileSystem
decl_stmt|;
specifier|protected
name|int
name|usageCount
init|=
literal|0
decl_stmt|;
specifier|public
name|Context
parameter_list|(
name|String
name|handle
parameter_list|)
block|{
try|try
block|{
name|testDir
operator|=
operator|new
name|File
argument_list|(
name|TEST_DIR
operator|+
literal|"/test_"
operator|+
name|handle
operator|+
literal|"_"
operator|+
name|Math
operator|.
name|abs
argument_list|(
operator|new
name|Random
argument_list|()
operator|.
name|nextLong
argument_list|()
argument_list|)
operator|+
literal|"/"
argument_list|)
operator|.
name|getCanonicalPath
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Cluster work directory: "
operator|+
name|testDir
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Failed to generate testDir"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|start
parameter_list|()
block|{
if|if
condition|(
name|usageCount
operator|++
operator|==
literal|0
condition|)
block|{
name|ManyMiniCluster
operator|.
name|Builder
name|b
init|=
name|ManyMiniCluster
operator|.
name|create
argument_list|(
operator|new
name|File
argument_list|(
name|testDir
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|testConf
operator|!=
literal|null
condition|)
block|{
name|b
operator|.
name|hbaseConf
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|testConf
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|cluster
operator|=
name|b
operator|.
name|build
argument_list|()
expr_stmt|;
name|cluster
operator|.
name|start
argument_list|()
expr_stmt|;
name|this
operator|.
name|hbaseConf
operator|=
name|cluster
operator|.
name|getHBaseConf
argument_list|()
expr_stmt|;
name|jobConf
operator|=
name|cluster
operator|.
name|getJobConf
argument_list|()
expr_stmt|;
name|fileSystem
operator|=
name|cluster
operator|.
name|getFileSystem
argument_list|()
expr_stmt|;
name|hiveConf
operator|=
name|cluster
operator|.
name|getHiveConf
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|stop
parameter_list|()
block|{
if|if
condition|(
operator|--
name|usageCount
operator|==
literal|0
condition|)
block|{
try|try
block|{
name|cluster
operator|.
name|stop
argument_list|()
expr_stmt|;
name|cluster
operator|=
literal|null
expr_stmt|;
block|}
finally|finally
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Trying to cleanup: "
operator|+
name|testDir
argument_list|)
expr_stmt|;
try|try
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
operator|new
name|Path
argument_list|(
name|testDir
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Failed to cleanup test dir"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
specifier|public
name|String
name|getTestDir
parameter_list|()
block|{
return|return
name|testDir
return|;
block|}
specifier|public
name|ManyMiniCluster
name|getCluster
parameter_list|()
block|{
return|return
name|cluster
return|;
block|}
specifier|public
name|Configuration
name|getHbaseConf
parameter_list|()
block|{
return|return
name|hbaseConf
return|;
block|}
specifier|public
name|Configuration
name|getJobConf
parameter_list|()
block|{
return|return
name|jobConf
return|;
block|}
specifier|public
name|HiveConf
name|getHiveConf
parameter_list|()
block|{
return|return
name|hiveConf
return|;
block|}
specifier|public
name|FileSystem
name|getFileSystem
parameter_list|()
block|{
return|return
name|fileSystem
return|;
block|}
block|}
block|}
end_class

end_unit

