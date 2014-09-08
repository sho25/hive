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
name|ql
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

begin_comment
comment|/**  * Suite for testing running of queries in multi-threaded mode.  */
end_comment

begin_class
specifier|public
class|class
name|TestMTQueries
extends|extends
name|BaseTestQueries
block|{
specifier|public
name|TestMTQueries
parameter_list|()
block|{
name|File
name|logDirFile
init|=
operator|new
name|File
argument_list|(
name|logDir
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|logDirFile
operator|.
name|exists
argument_list|()
operator|||
name|logDirFile
operator|.
name|mkdirs
argument_list|()
operator|)
condition|)
block|{
name|fail
argument_list|(
literal|"Could not create "
operator|+
name|logDir
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testMTQueries1
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|testNames
init|=
operator|new
name|String
index|[]
block|{
literal|"join1.q"
block|,
literal|"join2.q"
block|,
literal|"groupby1.q"
block|,
literal|"groupby2.q"
block|,
literal|"join3.q"
block|,
literal|"input1.q"
block|,
literal|"input19.q"
block|}
decl_stmt|;
name|File
index|[]
name|qfiles
init|=
name|setupQFiles
argument_list|(
name|testNames
argument_list|)
decl_stmt|;
name|QTestUtil
index|[]
name|qts
init|=
name|QTestUtil
operator|.
name|queryListRunnerSetup
argument_list|(
name|qfiles
argument_list|,
name|resDir
argument_list|,
name|logDir
argument_list|)
decl_stmt|;
for|for
control|(
name|QTestUtil
name|util
range|:
name|qts
control|)
block|{
comment|// derby fails creating multiple stats aggregator concurrently
name|util
operator|.
name|getConf
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hive.exec.submitviachild"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|util
operator|.
name|getConf
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hive.exec.submit.local.task.via.child"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|util
operator|.
name|getConf
argument_list|()
operator|.
name|set
argument_list|(
literal|"hive.stats.dbclass"
argument_list|,
literal|"fs"
argument_list|)
expr_stmt|;
block|}
name|boolean
name|success
init|=
name|QTestUtil
operator|.
name|queryListRunnerMultiThreaded
argument_list|(
name|qfiles
argument_list|,
name|qts
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|success
condition|)
block|{
name|fail
argument_list|(
literal|"One or more queries failed"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

