begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|jdbc
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
name|net
operator|.
name|URL
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
name|List
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
name|concurrent
operator|.
name|TimeUnit
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|tez
operator|.
name|WorkloadManager
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
name|wm
operator|.
name|Trigger
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
name|jdbc
operator|.
name|miniHS2
operator|.
name|MiniHS2
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
name|jdbc
operator|.
name|miniHS2
operator|.
name|MiniHS2
operator|.
name|MiniClusterType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_class
specifier|public
class|class
name|TestTriggersWorkloadManager
extends|extends
name|TestTriggersTezSessionPoolManager
block|{
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeTest
parameter_list|()
throws|throws
name|Exception
block|{
name|Class
operator|.
name|forName
argument_list|(
name|MiniHS2
operator|.
name|getJdbcDriverName
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|confDir
init|=
literal|"../../data/conf/llap/"
decl_stmt|;
if|if
condition|(
name|confDir
operator|!=
literal|null
operator|&&
operator|!
name|confDir
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|HiveConf
operator|.
name|setHiveSiteLocation
argument_list|(
operator|new
name|URL
argument_list|(
literal|"file://"
operator|+
operator|new
name|File
argument_list|(
name|confDir
argument_list|)
operator|.
name|toURI
argument_list|()
operator|.
name|getPath
argument_list|()
operator|+
literal|"/hive-site.xml"
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Setting hive-site: "
operator|+
name|HiveConf
operator|.
name|getHiveSiteLocation
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|conf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setTimeVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_TRIGGER_VALIDATION_INTERVAL_MS
argument_list|,
literal|100
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_TEZ_INTERACTIVE_QUEUE
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hive.test.workload.management"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|TEZ_EXEC_SUMMARY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_STRICT_CHECKS_CARTESIAN
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// don't want cache hits from llap io for testing filesystem bytes read counters
name|conf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|LLAP_IO_MEMORY_MODE
argument_list|,
literal|"none"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|addResource
argument_list|(
operator|new
name|URL
argument_list|(
literal|"file://"
operator|+
operator|new
name|File
argument_list|(
name|confDir
argument_list|)
operator|.
name|toURI
argument_list|()
operator|.
name|getPath
argument_list|()
operator|+
literal|"/tez-site.xml"
argument_list|)
argument_list|)
expr_stmt|;
name|miniHS2
operator|=
operator|new
name|MiniHS2
argument_list|(
name|conf
argument_list|,
name|MiniClusterType
operator|.
name|LLAP
argument_list|)
expr_stmt|;
name|dataFileDir
operator|=
name|conf
operator|.
name|get
argument_list|(
literal|"test.data.files"
argument_list|)
operator|.
name|replace
argument_list|(
literal|'\\'
argument_list|,
literal|'/'
argument_list|)
operator|.
name|replace
argument_list|(
literal|"c:"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|kvDataFilePath
operator|=
operator|new
name|Path
argument_list|(
name|dataFileDir
argument_list|,
literal|"kv1.txt"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|miniHS2
operator|.
name|start
argument_list|(
name|confOverlay
argument_list|)
expr_stmt|;
name|miniHS2
operator|.
name|getDFS
argument_list|()
operator|.
name|getFileSystem
argument_list|()
operator|.
name|mkdirs
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/apps_staging_dir/anonymous"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setupTriggers
parameter_list|(
specifier|final
name|List
argument_list|<
name|Trigger
argument_list|>
name|triggers
parameter_list|)
throws|throws
name|Exception
block|{
name|WorkloadManager
name|wm
init|=
name|WorkloadManager
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|wm
operator|.
name|getPools
argument_list|()
operator|.
name|get
argument_list|(
literal|"llap"
argument_list|)
operator|.
name|setTriggers
argument_list|(
name|triggers
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

