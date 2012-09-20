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
name|metastore
package|;
end_package

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
name|hive
operator|.
name|cli
operator|.
name|CliSessionState
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
name|metastore
operator|.
name|api
operator|.
name|Database
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
name|metastore
operator|.
name|api
operator|.
name|Partition
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
name|metastore
operator|.
name|api
operator|.
name|PartitionEventType
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
name|metastore
operator|.
name|api
operator|.
name|Table
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
name|metastore
operator|.
name|events
operator|.
name|AddPartitionEvent
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
name|metastore
operator|.
name|events
operator|.
name|AlterPartitionEvent
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
name|metastore
operator|.
name|events
operator|.
name|AlterTableEvent
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
name|metastore
operator|.
name|events
operator|.
name|CreateDatabaseEvent
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
name|metastore
operator|.
name|events
operator|.
name|CreateTableEvent
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
name|metastore
operator|.
name|events
operator|.
name|DropDatabaseEvent
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
name|metastore
operator|.
name|events
operator|.
name|DropPartitionEvent
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
name|metastore
operator|.
name|events
operator|.
name|DropTableEvent
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
name|metastore
operator|.
name|events
operator|.
name|ListenerEvent
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
name|metastore
operator|.
name|events
operator|.
name|LoadPartitionDoneEvent
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
name|metastore
operator|.
name|events
operator|.
name|PreAddPartitionEvent
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
name|metastore
operator|.
name|events
operator|.
name|PreAlterPartitionEvent
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
name|metastore
operator|.
name|events
operator|.
name|PreAlterTableEvent
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
name|metastore
operator|.
name|events
operator|.
name|PreCreateDatabaseEvent
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
name|metastore
operator|.
name|events
operator|.
name|PreCreateTableEvent
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
name|metastore
operator|.
name|events
operator|.
name|PreDropDatabaseEvent
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
name|metastore
operator|.
name|events
operator|.
name|PreDropPartitionEvent
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
name|metastore
operator|.
name|events
operator|.
name|PreDropTableEvent
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
name|metastore
operator|.
name|events
operator|.
name|PreEventContext
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
name|metastore
operator|.
name|events
operator|.
name|PreLoadPartitionDoneEvent
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
name|Driver
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

begin_comment
comment|/**  * TestMetaStoreEventListener. Test case for  * {@link org.apache.hadoop.hive.metastore.MetaStoreEventListener} and  * {@link org.apache.hadoop.hive.metastore.MetaStorePreEventListener}  */
end_comment

begin_class
specifier|public
class|class
name|TestMetaStoreEventListener
extends|extends
name|TestCase
block|{
specifier|private
specifier|static
specifier|final
name|String
name|msPort
init|=
literal|"20001"
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
name|HiveMetaStoreClient
name|msc
decl_stmt|;
specifier|private
name|Driver
name|driver
decl_stmt|;
specifier|private
specifier|static
class|class
name|RunMS
implements|implements
name|Runnable
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|HiveMetaStore
operator|.
name|main
argument_list|(
operator|new
name|String
index|[]
block|{
name|msPort
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|(
name|System
operator|.
name|err
argument_list|)
expr_stmt|;
assert|assert
literal|false
assert|;
block|}
block|}
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
name|System
operator|.
name|setProperty
argument_list|(
literal|"hive.metastore.event.listeners"
argument_list|,
name|DummyListener
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"hive.metastore.pre.event.listeners"
argument_list|,
name|DummyPreListener
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|(
operator|new
name|RunMS
argument_list|()
argument_list|)
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|40000
argument_list|)
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
argument_list|,
literal|"thrift://localhost:"
operator|+
name|msPort
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORETHRIFTRETRIES
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PREEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|POSTEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|msc
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|hiveConf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|driver
operator|=
operator|new
name|Driver
argument_list|(
name|hiveConf
argument_list|)
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
block|}
specifier|private
name|void
name|validateCreateDb
parameter_list|(
name|Database
name|expectedDb
parameter_list|,
name|Database
name|actualDb
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|expectedDb
operator|.
name|getName
argument_list|()
argument_list|,
name|actualDb
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedDb
operator|.
name|getLocationUri
argument_list|()
argument_list|,
name|actualDb
operator|.
name|getLocationUri
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|validateTable
parameter_list|(
name|Table
name|expectedTable
parameter_list|,
name|Table
name|actualTable
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|expectedTable
operator|.
name|getTableName
argument_list|()
argument_list|,
name|actualTable
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedTable
operator|.
name|getDbName
argument_list|()
argument_list|,
name|actualTable
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedTable
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|,
name|actualTable
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|validateCreateTable
parameter_list|(
name|Table
name|expectedTable
parameter_list|,
name|Table
name|actualTable
parameter_list|)
block|{
name|validateTable
argument_list|(
name|expectedTable
argument_list|,
name|actualTable
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|validateAddPartition
parameter_list|(
name|Partition
name|expectedPartition
parameter_list|,
name|Partition
name|actualPartition
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|expectedPartition
argument_list|,
name|actualPartition
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|validatePartition
parameter_list|(
name|Partition
name|expectedPartition
parameter_list|,
name|Partition
name|actualPartition
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|expectedPartition
operator|.
name|getValues
argument_list|()
argument_list|,
name|actualPartition
operator|.
name|getValues
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedPartition
operator|.
name|getDbName
argument_list|()
argument_list|,
name|actualPartition
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedPartition
operator|.
name|getTableName
argument_list|()
argument_list|,
name|actualPartition
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|validateAlterPartition
parameter_list|(
name|Partition
name|expectedOldPartition
parameter_list|,
name|Partition
name|expectedNewPartition
parameter_list|,
name|String
name|actualOldPartitionDbName
parameter_list|,
name|String
name|actualOldPartitionTblName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|actualOldPartitionValues
parameter_list|,
name|Partition
name|actualNewPartition
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|expectedOldPartition
operator|.
name|getValues
argument_list|()
argument_list|,
name|actualOldPartitionValues
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedOldPartition
operator|.
name|getDbName
argument_list|()
argument_list|,
name|actualOldPartitionDbName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedOldPartition
operator|.
name|getTableName
argument_list|()
argument_list|,
name|actualOldPartitionTblName
argument_list|)
expr_stmt|;
name|validatePartition
argument_list|(
name|expectedNewPartition
argument_list|,
name|actualNewPartition
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|validateAlterTable
parameter_list|(
name|Table
name|expectedOldTable
parameter_list|,
name|Table
name|expectedNewTable
parameter_list|,
name|Table
name|actualOldTable
parameter_list|,
name|Table
name|actualNewTable
parameter_list|)
block|{
name|validateTable
argument_list|(
name|expectedOldTable
argument_list|,
name|actualOldTable
argument_list|)
expr_stmt|;
name|validateTable
argument_list|(
name|expectedNewTable
argument_list|,
name|actualNewTable
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|validateAlterTableColumns
parameter_list|(
name|Table
name|expectedOldTable
parameter_list|,
name|Table
name|expectedNewTable
parameter_list|,
name|Table
name|actualOldTable
parameter_list|,
name|Table
name|actualNewTable
parameter_list|)
block|{
name|validateAlterTable
argument_list|(
name|expectedOldTable
argument_list|,
name|expectedNewTable
argument_list|,
name|actualOldTable
argument_list|,
name|actualNewTable
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedOldTable
operator|.
name|getSd
argument_list|()
operator|.
name|getCols
argument_list|()
argument_list|,
name|actualOldTable
operator|.
name|getSd
argument_list|()
operator|.
name|getCols
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedNewTable
operator|.
name|getSd
argument_list|()
operator|.
name|getCols
argument_list|()
argument_list|,
name|actualNewTable
operator|.
name|getSd
argument_list|()
operator|.
name|getCols
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|validateLoadPartitionDone
parameter_list|(
name|String
name|expectedTableName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|expectedPartitionName
parameter_list|,
name|String
name|actualTableName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|actualPartitionName
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|expectedPartitionName
argument_list|,
name|actualPartitionName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedTableName
argument_list|,
name|actualTableName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|validateDropPartition
parameter_list|(
name|Partition
name|expectedPartition
parameter_list|,
name|Partition
name|actualPartition
parameter_list|)
block|{
name|validatePartition
argument_list|(
name|expectedPartition
argument_list|,
name|actualPartition
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|validateDropTable
parameter_list|(
name|Table
name|expectedTable
parameter_list|,
name|Table
name|actualTable
parameter_list|)
block|{
name|validateTable
argument_list|(
name|expectedTable
argument_list|,
name|actualTable
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|validateDropDb
parameter_list|(
name|Database
name|expectedDb
parameter_list|,
name|Database
name|actualDb
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|expectedDb
argument_list|,
name|actualDb
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testListener
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|dbName
init|=
literal|"tmpdb"
decl_stmt|;
name|String
name|tblName
init|=
literal|"tmptbl"
decl_stmt|;
name|String
name|renamed
init|=
literal|"tmptbl2"
decl_stmt|;
name|int
name|listSize
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|ListenerEvent
argument_list|>
name|notifyList
init|=
name|DummyListener
operator|.
name|notifyList
decl_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|PreEventContext
argument_list|>
name|preNotifyList
init|=
name|DummyPreListener
operator|.
name|notifyList
decl_stmt|;
name|assertEquals
argument_list|(
name|preNotifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"create database "
operator|+
name|dbName
argument_list|)
expr_stmt|;
name|listSize
operator|++
expr_stmt|;
name|Database
name|db
init|=
name|msc
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|listSize
argument_list|,
name|notifyList
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|listSize
argument_list|,
name|preNotifyList
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|CreateDatabaseEvent
name|dbEvent
init|=
call|(
name|CreateDatabaseEvent
call|)
argument_list|(
name|notifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
assert|assert
name|dbEvent
operator|.
name|getStatus
argument_list|()
assert|;
name|validateCreateDb
argument_list|(
name|db
argument_list|,
name|dbEvent
operator|.
name|getDatabase
argument_list|()
argument_list|)
expr_stmt|;
name|PreCreateDatabaseEvent
name|preDbEvent
init|=
call|(
name|PreCreateDatabaseEvent
call|)
argument_list|(
name|preNotifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|validateCreateDb
argument_list|(
name|db
argument_list|,
name|preDbEvent
operator|.
name|getDatabase
argument_list|()
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|dbName
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"create table %s (a string) partitioned by (b string)"
argument_list|,
name|tblName
argument_list|)
argument_list|)
expr_stmt|;
name|listSize
operator|++
expr_stmt|;
name|Table
name|tbl
init|=
name|msc
operator|.
name|getTable
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|preNotifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|CreateTableEvent
name|tblEvent
init|=
call|(
name|CreateTableEvent
call|)
argument_list|(
name|notifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
assert|assert
name|tblEvent
operator|.
name|getStatus
argument_list|()
assert|;
name|validateCreateTable
argument_list|(
name|tbl
argument_list|,
name|tblEvent
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|PreCreateTableEvent
name|preTblEvent
init|=
call|(
name|PreCreateTableEvent
call|)
argument_list|(
name|preNotifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|validateCreateTable
argument_list|(
name|tbl
argument_list|,
name|preTblEvent
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"alter table tmptbl add partition (b='2011')"
argument_list|)
expr_stmt|;
name|listSize
operator|++
expr_stmt|;
name|Partition
name|part
init|=
name|msc
operator|.
name|getPartition
argument_list|(
literal|"tmpdb"
argument_list|,
literal|"tmptbl"
argument_list|,
literal|"b=2011"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|preNotifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|AddPartitionEvent
name|partEvent
init|=
call|(
name|AddPartitionEvent
call|)
argument_list|(
name|notifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
assert|assert
name|partEvent
operator|.
name|getStatus
argument_list|()
assert|;
name|validateAddPartition
argument_list|(
name|part
argument_list|,
name|partEvent
operator|.
name|getPartition
argument_list|()
argument_list|)
expr_stmt|;
name|PreAddPartitionEvent
name|prePartEvent
init|=
call|(
name|PreAddPartitionEvent
call|)
argument_list|(
name|preNotifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|validateAddPartition
argument_list|(
name|part
argument_list|,
name|prePartEvent
operator|.
name|getPartition
argument_list|()
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"alter table %s touch partition (%s)"
argument_list|,
name|tblName
argument_list|,
literal|"b='2011'"
argument_list|)
argument_list|)
expr_stmt|;
name|listSize
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|preNotifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
comment|//the partition did not change,
comment|// so the new partition should be similar to the original partition
name|Partition
name|origP
init|=
name|msc
operator|.
name|getPartition
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|,
literal|"b=2011"
argument_list|)
decl_stmt|;
name|AlterPartitionEvent
name|alterPartEvent
init|=
operator|(
name|AlterPartitionEvent
operator|)
name|notifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
decl_stmt|;
assert|assert
name|alterPartEvent
operator|.
name|getStatus
argument_list|()
assert|;
name|validateAlterPartition
argument_list|(
name|origP
argument_list|,
name|origP
argument_list|,
name|alterPartEvent
operator|.
name|getOldPartition
argument_list|()
operator|.
name|getDbName
argument_list|()
argument_list|,
name|alterPartEvent
operator|.
name|getOldPartition
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|,
name|alterPartEvent
operator|.
name|getOldPartition
argument_list|()
operator|.
name|getValues
argument_list|()
argument_list|,
name|alterPartEvent
operator|.
name|getNewPartition
argument_list|()
argument_list|)
expr_stmt|;
name|PreAlterPartitionEvent
name|preAlterPartEvent
init|=
operator|(
name|PreAlterPartitionEvent
operator|)
name|preNotifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
decl_stmt|;
name|validateAlterPartition
argument_list|(
name|origP
argument_list|,
name|origP
argument_list|,
name|preAlterPartEvent
operator|.
name|getDbName
argument_list|()
argument_list|,
name|preAlterPartEvent
operator|.
name|getTableName
argument_list|()
argument_list|,
name|preAlterPartEvent
operator|.
name|getNewPartition
argument_list|()
operator|.
name|getValues
argument_list|()
argument_list|,
name|preAlterPartEvent
operator|.
name|getNewPartition
argument_list|()
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"alter table %s rename to %s"
argument_list|,
name|tblName
argument_list|,
name|renamed
argument_list|)
argument_list|)
expr_stmt|;
name|listSize
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|preNotifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|Table
name|renamedTable
init|=
name|msc
operator|.
name|getTable
argument_list|(
name|dbName
argument_list|,
name|renamed
argument_list|)
decl_stmt|;
name|AlterTableEvent
name|alterTableE
init|=
operator|(
name|AlterTableEvent
operator|)
name|notifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
decl_stmt|;
assert|assert
name|alterTableE
operator|.
name|getStatus
argument_list|()
assert|;
name|validateAlterTable
argument_list|(
name|tbl
argument_list|,
name|renamedTable
argument_list|,
name|alterTableE
operator|.
name|getOldTable
argument_list|()
argument_list|,
name|alterTableE
operator|.
name|getNewTable
argument_list|()
argument_list|)
expr_stmt|;
name|PreAlterTableEvent
name|preAlterTableE
init|=
operator|(
name|PreAlterTableEvent
operator|)
name|preNotifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
decl_stmt|;
name|validateAlterTable
argument_list|(
name|tbl
argument_list|,
name|renamedTable
argument_list|,
name|preAlterTableE
operator|.
name|getOldTable
argument_list|()
argument_list|,
name|preAlterTableE
operator|.
name|getNewTable
argument_list|()
argument_list|)
expr_stmt|;
comment|//change the table name back
name|driver
operator|.
name|run
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"alter table %s rename to %s"
argument_list|,
name|renamed
argument_list|,
name|tblName
argument_list|)
argument_list|)
expr_stmt|;
name|listSize
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|preNotifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"alter table %s ADD COLUMNS (c int)"
argument_list|,
name|tblName
argument_list|)
argument_list|)
expr_stmt|;
name|listSize
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|preNotifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|Table
name|altTable
init|=
name|msc
operator|.
name|getTable
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|)
decl_stmt|;
name|alterTableE
operator|=
operator|(
name|AlterTableEvent
operator|)
name|notifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
expr_stmt|;
assert|assert
name|alterTableE
operator|.
name|getStatus
argument_list|()
assert|;
name|validateAlterTableColumns
argument_list|(
name|tbl
argument_list|,
name|altTable
argument_list|,
name|alterTableE
operator|.
name|getOldTable
argument_list|()
argument_list|,
name|alterTableE
operator|.
name|getNewTable
argument_list|()
argument_list|)
expr_stmt|;
name|preAlterTableE
operator|=
operator|(
name|PreAlterTableEvent
operator|)
name|preNotifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
expr_stmt|;
name|validateAlterTableColumns
argument_list|(
name|tbl
argument_list|,
name|altTable
argument_list|,
name|preAlterTableE
operator|.
name|getOldTable
argument_list|()
argument_list|,
name|preAlterTableE
operator|.
name|getNewTable
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|kvs
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|kvs
operator|.
name|put
argument_list|(
literal|"b"
argument_list|,
literal|"2011"
argument_list|)
expr_stmt|;
name|msc
operator|.
name|markPartitionForEvent
argument_list|(
literal|"tmpdb"
argument_list|,
literal|"tmptbl"
argument_list|,
name|kvs
argument_list|,
name|PartitionEventType
operator|.
name|LOAD_DONE
argument_list|)
expr_stmt|;
name|listSize
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|preNotifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|LoadPartitionDoneEvent
name|partMarkEvent
init|=
operator|(
name|LoadPartitionDoneEvent
operator|)
name|notifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
decl_stmt|;
assert|assert
name|partMarkEvent
operator|.
name|getStatus
argument_list|()
assert|;
name|validateLoadPartitionDone
argument_list|(
literal|"tmptbl"
argument_list|,
name|kvs
argument_list|,
name|partMarkEvent
operator|.
name|getTable
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|,
name|partMarkEvent
operator|.
name|getPartitionName
argument_list|()
argument_list|)
expr_stmt|;
name|PreLoadPartitionDoneEvent
name|prePartMarkEvent
init|=
operator|(
name|PreLoadPartitionDoneEvent
operator|)
name|preNotifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
decl_stmt|;
name|validateLoadPartitionDone
argument_list|(
literal|"tmptbl"
argument_list|,
name|kvs
argument_list|,
name|prePartMarkEvent
operator|.
name|getTableName
argument_list|()
argument_list|,
name|prePartMarkEvent
operator|.
name|getPartitionName
argument_list|()
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"alter table %s drop partition (b='2011')"
argument_list|,
name|tblName
argument_list|)
argument_list|)
expr_stmt|;
name|listSize
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|preNotifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|DropPartitionEvent
name|dropPart
init|=
operator|(
name|DropPartitionEvent
operator|)
name|notifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
decl_stmt|;
assert|assert
name|dropPart
operator|.
name|getStatus
argument_list|()
assert|;
name|validateDropPartition
argument_list|(
name|part
argument_list|,
name|dropPart
operator|.
name|getPartition
argument_list|()
argument_list|)
expr_stmt|;
name|PreDropPartitionEvent
name|preDropPart
init|=
operator|(
name|PreDropPartitionEvent
operator|)
name|preNotifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
decl_stmt|;
name|validateDropPartition
argument_list|(
name|part
argument_list|,
name|preDropPart
operator|.
name|getPartition
argument_list|()
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop table "
operator|+
name|tblName
argument_list|)
expr_stmt|;
name|listSize
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|preNotifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|DropTableEvent
name|dropTbl
init|=
operator|(
name|DropTableEvent
operator|)
name|notifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
decl_stmt|;
assert|assert
name|dropTbl
operator|.
name|getStatus
argument_list|()
assert|;
name|validateDropTable
argument_list|(
name|tbl
argument_list|,
name|dropTbl
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|PreDropTableEvent
name|preDropTbl
init|=
operator|(
name|PreDropTableEvent
operator|)
name|preNotifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
decl_stmt|;
name|validateDropTable
argument_list|(
name|tbl
argument_list|,
name|preDropTbl
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop database "
operator|+
name|dbName
argument_list|)
expr_stmt|;
name|listSize
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|preNotifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|DropDatabaseEvent
name|dropDB
init|=
operator|(
name|DropDatabaseEvent
operator|)
name|notifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
decl_stmt|;
assert|assert
name|dropDB
operator|.
name|getStatus
argument_list|()
assert|;
name|validateDropDb
argument_list|(
name|db
argument_list|,
name|dropDB
operator|.
name|getDatabase
argument_list|()
argument_list|)
expr_stmt|;
name|PreDropDatabaseEvent
name|preDropDB
init|=
operator|(
name|PreDropDatabaseEvent
operator|)
name|preNotifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
decl_stmt|;
assert|assert
name|dropDB
operator|.
name|getStatus
argument_list|()
assert|;
name|validateDropDb
argument_list|(
name|db
argument_list|,
name|preDropDB
operator|.
name|getDatabase
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

