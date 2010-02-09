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
name|hwi
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|history
operator|.
name|HiveHistoryViewer
import|;
end_import

begin_comment
comment|/**  * TestHWISessionManager.  *  */
end_comment

begin_class
specifier|public
class|class
name|TestHWISessionManager
extends|extends
name|TestCase
block|{
specifier|private
specifier|static
name|String
name|tableName
init|=
literal|"test_hwi_table"
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|final
name|Path
name|dataFilePath
decl_stmt|;
specifier|private
name|HWISessionManager
name|hsm
decl_stmt|;
specifier|public
name|TestHWISessionManager
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|conf
operator|=
operator|new
name|HiveConf
argument_list|(
name|TestHWISessionManager
operator|.
name|class
argument_list|)
expr_stmt|;
name|String
name|dataFileDir
init|=
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
decl_stmt|;
name|dataFilePath
operator|=
operator|new
name|Path
argument_list|(
name|dataFileDir
argument_list|,
literal|"kv1.txt"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|hsm
operator|=
operator|new
name|HWISessionManager
argument_list|()
expr_stmt|;
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|(
name|hsm
argument_list|)
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
name|hsm
operator|.
name|setGoOn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|final
name|void
name|testHiveDriver
parameter_list|()
throws|throws
name|Exception
block|{
comment|// create a user
name|HWIAuth
name|user1
init|=
operator|new
name|HWIAuth
argument_list|()
decl_stmt|;
name|user1
operator|.
name|setUser
argument_list|(
literal|"hadoop"
argument_list|)
expr_stmt|;
name|user1
operator|.
name|setGroups
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"hadoop"
block|}
argument_list|)
expr_stmt|;
comment|// create two sessions for user
name|HWISessionItem
name|user1_item1
init|=
name|hsm
operator|.
name|createSession
argument_list|(
name|user1
argument_list|,
literal|"session1"
argument_list|)
decl_stmt|;
name|HWISessionItem
name|user1_item2
init|=
name|hsm
operator|.
name|createSession
argument_list|(
name|user1
argument_list|,
literal|"session2"
argument_list|)
decl_stmt|;
comment|// create second user
name|HWIAuth
name|user2
init|=
operator|new
name|HWIAuth
argument_list|()
decl_stmt|;
name|user2
operator|.
name|setUser
argument_list|(
literal|"user2"
argument_list|)
expr_stmt|;
name|user2
operator|.
name|setGroups
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"user2"
block|}
argument_list|)
expr_stmt|;
comment|// create one session for this user
name|HWISessionItem
name|user2_item1
init|=
name|hsm
operator|.
name|createSession
argument_list|(
name|user2
argument_list|,
literal|"session1"
argument_list|)
decl_stmt|;
comment|// testing storage of sessions in HWISessionManager
name|assertEquals
argument_list|(
name|hsm
operator|.
name|findAllSessionsForUser
argument_list|(
name|user1
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hsm
operator|.
name|findAllSessionsForUser
argument_list|(
name|user2
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hsm
operator|.
name|findAllSessionItems
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|HWISessionItem
name|searchItem
init|=
name|hsm
operator|.
name|findSessionItemByName
argument_list|(
name|user1
argument_list|,
literal|"session1"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|searchItem
argument_list|,
name|user1_item1
argument_list|)
expr_stmt|;
name|searchItem
operator|.
name|addQuery
argument_list|(
literal|"create table "
operator|+
name|tableName
operator|+
literal|" (key int, value string)"
argument_list|)
expr_stmt|;
name|searchItem
operator|.
name|addQuery
argument_list|(
literal|"describe "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|searchItem
operator|.
name|clientStart
argument_list|()
expr_stmt|;
comment|// wait for the session manager to make the table. It is non blocking API.
synchronized|synchronized
init|(
name|searchItem
operator|.
name|runnable
init|)
block|{
while|while
condition|(
name|searchItem
operator|.
name|getStatus
argument_list|()
operator|!=
name|HWISessionItem
operator|.
name|WebSessionItemStatus
operator|.
name|READY
condition|)
block|{
name|searchItem
operator|.
name|runnable
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
block|}
name|ArrayList
argument_list|<
name|Integer
argument_list|>
name|zero
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|zero
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|zero
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|zero
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|zero
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|Integer
argument_list|>
name|zero3
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|zero3
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|zero3
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|zero3
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|Integer
argument_list|>
name|zero1
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|zero1
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|zero
argument_list|,
name|searchItem
operator|.
name|getQueryRet
argument_list|()
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|searchBlockRes
init|=
name|searchItem
operator|.
name|getResultBucket
argument_list|()
decl_stmt|;
name|String
name|resLine
init|=
name|searchBlockRes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|resLine
operator|.
name|contains
argument_list|(
literal|"key"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|resLine
operator|.
name|contains
argument_list|(
literal|"int"
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|resLine2
init|=
name|searchBlockRes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|resLine2
operator|.
name|contains
argument_list|(
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|resLine2
operator|.
name|contains
argument_list|(
literal|"string"
argument_list|)
argument_list|)
expr_stmt|;
comment|// load data into table
name|searchItem
operator|.
name|clientRenew
argument_list|()
expr_stmt|;
name|searchItem
operator|.
name|addQuery
argument_list|(
literal|"load data local inpath '"
operator|+
name|dataFilePath
operator|.
name|toString
argument_list|()
operator|+
literal|"' into table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|searchItem
operator|.
name|clientStart
argument_list|()
expr_stmt|;
while|while
condition|(
name|searchItem
operator|.
name|getStatus
argument_list|()
operator|!=
name|HWISessionItem
operator|.
name|WebSessionItemStatus
operator|.
name|READY
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|zero1
argument_list|,
name|searchItem
operator|.
name|getQueryRet
argument_list|()
argument_list|)
expr_stmt|;
comment|// start two queries simultaniously
name|user1_item2
operator|.
name|addQuery
argument_list|(
literal|"select distinct(test_hwi_table.key) from "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|user2_item1
operator|.
name|addQuery
argument_list|(
literal|"select distinct(test_hwi_table.key) from "
operator|+
name|tableName
argument_list|)
expr_stmt|;
comment|// set result files to compare results
name|File
name|tmpdir
init|=
operator|new
name|File
argument_list|(
literal|"/tmp/"
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
operator|+
literal|"/"
argument_list|)
decl_stmt|;
if|if
condition|(
name|tmpdir
operator|.
name|exists
argument_list|()
operator|&&
operator|!
name|tmpdir
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|tmpdir
operator|+
literal|" exists but is not a directory"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|tmpdir
operator|.
name|exists
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|tmpdir
operator|.
name|mkdirs
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Could not make scratch directory "
operator|+
name|tmpdir
argument_list|)
throw|;
block|}
block|}
name|File
name|result1
init|=
operator|new
name|File
argument_list|(
name|tmpdir
argument_list|,
literal|"user1_item2"
argument_list|)
decl_stmt|;
name|File
name|result2
init|=
operator|new
name|File
argument_list|(
name|tmpdir
argument_list|,
literal|"user2_item1"
argument_list|)
decl_stmt|;
name|user1_item2
operator|.
name|setResultFile
argument_list|(
name|result1
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|user2_item1
operator|.
name|setResultFile
argument_list|(
name|result2
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|user1_item2
operator|.
name|setSSIsSilent
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|user2_item1
operator|.
name|setSSIsSilent
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|user1_item2
operator|.
name|clientStart
argument_list|()
expr_stmt|;
name|user2_item1
operator|.
name|clientStart
argument_list|()
expr_stmt|;
synchronized|synchronized
init|(
name|user1_item2
operator|.
name|runnable
init|)
block|{
while|while
condition|(
name|user1_item2
operator|.
name|getStatus
argument_list|()
operator|!=
name|HWISessionItem
operator|.
name|WebSessionItemStatus
operator|.
name|READY
condition|)
block|{
name|user1_item2
operator|.
name|runnable
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
block|}
synchronized|synchronized
init|(
name|user2_item1
operator|.
name|runnable
init|)
block|{
while|while
condition|(
name|user2_item1
operator|.
name|getStatus
argument_list|()
operator|!=
name|HWISessionItem
operator|.
name|WebSessionItemStatus
operator|.
name|READY
condition|)
block|{
name|user2_item1
operator|.
name|runnable
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
name|zero3
argument_list|,
name|user1_item2
operator|.
name|getQueryRet
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|zero3
argument_list|,
name|user2_item1
operator|.
name|getQueryRet
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|isFileContentEqual
argument_list|(
name|result1
argument_list|,
name|result2
argument_list|)
argument_list|)
expr_stmt|;
comment|// clean up the files
name|result1
operator|.
name|delete
argument_list|()
expr_stmt|;
name|result2
operator|.
name|delete
argument_list|()
expr_stmt|;
comment|// test a session renew/refresh
name|user2_item1
operator|.
name|clientRenew
argument_list|()
expr_stmt|;
name|user2_item1
operator|.
name|addQuery
argument_list|(
literal|"select distinct(test_hwi_table.key) from "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|user2_item1
operator|.
name|clientStart
argument_list|()
expr_stmt|;
synchronized|synchronized
init|(
name|user2_item1
operator|.
name|runnable
init|)
block|{
while|while
condition|(
name|user2_item1
operator|.
name|getStatus
argument_list|()
operator|!=
name|HWISessionItem
operator|.
name|WebSessionItemStatus
operator|.
name|READY
condition|)
block|{
name|user2_item1
operator|.
name|runnable
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
block|}
comment|// cleanup
name|HWISessionItem
name|cleanup
init|=
name|hsm
operator|.
name|createSession
argument_list|(
name|user1
argument_list|,
literal|"cleanup"
argument_list|)
decl_stmt|;
name|cleanup
operator|.
name|addQuery
argument_list|(
literal|"drop table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|cleanup
operator|.
name|clientStart
argument_list|()
expr_stmt|;
synchronized|synchronized
init|(
name|cleanup
operator|.
name|runnable
init|)
block|{
while|while
condition|(
name|cleanup
operator|.
name|getStatus
argument_list|()
operator|!=
name|HWISessionItem
operator|.
name|WebSessionItemStatus
operator|.
name|READY
condition|)
block|{
name|cleanup
operator|.
name|runnable
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
block|}
comment|// test the history is non null object.
name|HiveHistoryViewer
name|hhv
init|=
name|cleanup
operator|.
name|getHistoryViewer
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|hhv
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|zero3
argument_list|,
name|cleanup
operator|.
name|getQueryRet
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isFileContentEqual
parameter_list|(
name|File
name|one
parameter_list|,
name|File
name|two
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|one
operator|.
name|exists
argument_list|()
operator|&&
name|two
operator|.
name|exists
argument_list|()
condition|)
block|{
if|if
condition|(
name|one
operator|.
name|isFile
argument_list|()
operator|&&
name|two
operator|.
name|isFile
argument_list|()
condition|)
block|{
if|if
condition|(
name|one
operator|.
name|length
argument_list|()
operator|==
name|two
operator|.
name|length
argument_list|()
condition|)
block|{
name|BufferedReader
name|br1
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|FileReader
argument_list|(
name|one
argument_list|)
argument_list|)
decl_stmt|;
name|BufferedReader
name|br2
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|FileReader
argument_list|(
name|one
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|line1
init|=
literal|null
decl_stmt|;
name|String
name|line2
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|line1
operator|=
name|br1
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|line2
operator|=
name|br2
operator|.
name|readLine
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|line1
operator|.
name|equals
argument_list|(
name|line2
argument_list|)
condition|)
block|{
name|br1
operator|.
name|close
argument_list|()
expr_stmt|;
name|br2
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
name|br1
operator|.
name|close
argument_list|()
expr_stmt|;
name|br2
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

