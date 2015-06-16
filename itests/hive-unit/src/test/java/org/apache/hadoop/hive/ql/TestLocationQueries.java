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
name|BufferedReader
import|;
end_import

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
name|FileReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|QTestUtil
operator|.
name|MiniClusterType
import|;
end_import

begin_comment
comment|/**  * Suite for testing location. e.g. if "alter table alter partition  * location" is run, do the partitions end up in the correct location.  *  *  This is a special case of the regular queries as paths are typically  *  ignored.  */
end_comment

begin_class
specifier|public
class|class
name|TestLocationQueries
extends|extends
name|BaseTestQueries
block|{
specifier|public
name|TestLocationQueries
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
comment|/**    * Our own checker - validate the location of the partition.    */
specifier|public
specifier|static
class|class
name|CheckResults
extends|extends
name|QTestUtil
block|{
specifier|private
specifier|final
name|String
name|locationSubdir
decl_stmt|;
comment|/**      * Validate only that the location is correct.      * @return non-zero if it failed      */
annotation|@
name|Override
specifier|public
name|int
name|checkCliDriverResults
parameter_list|(
name|String
name|tname
parameter_list|)
throws|throws
name|Exception
block|{
name|File
name|logFile
init|=
operator|new
name|File
argument_list|(
name|logDir
argument_list|,
name|tname
operator|+
literal|".out"
argument_list|)
decl_stmt|;
name|int
name|failedCount
init|=
literal|0
decl_stmt|;
name|FileReader
name|fr
init|=
operator|new
name|FileReader
argument_list|(
name|logFile
argument_list|)
decl_stmt|;
name|BufferedReader
name|in
init|=
operator|new
name|BufferedReader
argument_list|(
name|fr
argument_list|)
decl_stmt|;
try|try
block|{
name|String
name|line
decl_stmt|;
name|int
name|locationCount
init|=
literal|0
decl_stmt|;
name|Pattern
name|p
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"location:([^,)]+)"
argument_list|)
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|in
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|Matcher
name|m
init|=
name|p
operator|.
name|matcher
argument_list|(
name|line
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|.
name|find
argument_list|()
condition|)
block|{
name|File
name|f
init|=
operator|new
name|File
argument_list|(
name|m
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|f
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|locationSubdir
argument_list|)
condition|)
block|{
name|failedCount
operator|++
expr_stmt|;
block|}
name|locationCount
operator|++
expr_stmt|;
block|}
block|}
comment|// we always have to find at least one location, otw the test is useless
if|if
condition|(
name|locationCount
operator|==
literal|0
condition|)
block|{
return|return
name|Integer
operator|.
name|MAX_VALUE
return|;
block|}
block|}
finally|finally
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|failedCount
return|;
block|}
specifier|public
name|CheckResults
parameter_list|(
name|String
name|outDir
parameter_list|,
name|String
name|logDir
parameter_list|,
name|MiniClusterType
name|miniMr
parameter_list|,
name|String
name|hadoopVer
parameter_list|,
name|String
name|locationSubdir
parameter_list|)
throws|throws
name|Exception
block|{
name|super
argument_list|(
name|outDir
argument_list|,
name|logDir
argument_list|,
name|miniMr
argument_list|,
literal|null
argument_list|,
name|hadoopVer
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|locationSubdir
operator|=
name|locationSubdir
expr_stmt|;
block|}
block|}
comment|/**    * Verify that the location of the partition is valid. In this case    * the path should end in "parta" and not "dt=a" (the default).    *    */
specifier|public
name|void
name|testAlterTablePartitionLocation_alter5
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
literal|"alter5.q"
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
name|qt
init|=
operator|new
name|QTestUtil
index|[
name|qfiles
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|qfiles
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|qt
index|[
name|i
index|]
operator|=
operator|new
name|CheckResults
argument_list|(
name|resDir
argument_list|,
name|logDir
argument_list|,
name|MiniClusterType
operator|.
name|none
argument_list|,
literal|"0.20"
argument_list|,
literal|"parta"
argument_list|)
expr_stmt|;
name|qt
index|[
name|i
index|]
operator|.
name|addFile
argument_list|(
name|qfiles
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|qt
index|[
name|i
index|]
operator|.
name|clearTestSideEffects
argument_list|()
expr_stmt|;
block|}
name|boolean
name|success
init|=
name|QTestUtil
operator|.
name|queryListRunnerSingleThreaded
argument_list|(
name|qfiles
argument_list|,
name|qt
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

