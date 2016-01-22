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
operator|.
name|txn
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Connection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Driver
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|PreparedStatement
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|ResultSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLTransactionRollbackException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Statement
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|shims
operator|.
name|ShimLoader
import|;
end_import

begin_comment
comment|/**  * Utility methods for creating and destroying txn database/schema.  * Placed here in a separate class so it can be shared across unit tests.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|TxnDbUtil
block|{
specifier|static
specifier|final
specifier|private
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TxnDbUtil
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TXN_MANAGER
init|=
literal|"org.apache.hadoop.hive.ql.lockmgr.DbTxnManager"
decl_stmt|;
specifier|private
specifier|static
name|int
name|deadlockCnt
init|=
literal|0
decl_stmt|;
specifier|private
name|TxnDbUtil
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Can't initialize class"
argument_list|)
throw|;
block|}
comment|/**    * Set up the configuration so it will use the DbTxnManager, concurrency will be set to true,    * and the JDBC configs will be set for putting the transaction and lock info in the embedded    * metastore.    *    * @param conf HiveConf to add these values to    */
specifier|public
specifier|static
name|void
name|setConfValues
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TXN_MANAGER
argument_list|,
name|TXN_MANAGER
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|prepDb
parameter_list|()
throws|throws
name|Exception
block|{
comment|// This is a bogus hack because it copies the contents of the SQL file
comment|// intended for creating derby databases, and thus will inexorably get
comment|// out of date with it.  I'm open to any suggestions on how to make this
comment|// read the file in a build friendly way.
name|Connection
name|conn
init|=
literal|null
decl_stmt|;
name|Statement
name|stmt
init|=
literal|null
decl_stmt|;
try|try
block|{
name|conn
operator|=
name|getConnection
argument_list|()
expr_stmt|;
name|stmt
operator|=
name|conn
operator|.
name|createStatement
argument_list|()
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"CREATE TABLE TXNS ("
operator|+
literal|"  TXN_ID bigint PRIMARY KEY,"
operator|+
literal|"  TXN_STATE char(1) NOT NULL,"
operator|+
literal|"  TXN_STARTED bigint NOT NULL,"
operator|+
literal|"  TXN_LAST_HEARTBEAT bigint NOT NULL,"
operator|+
literal|"  TXN_USER varchar(128) NOT NULL,"
operator|+
literal|"  TXN_HOST varchar(128) NOT NULL)"
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"CREATE TABLE TXN_COMPONENTS ("
operator|+
literal|"  TC_TXNID bigint REFERENCES TXNS (TXN_ID),"
operator|+
literal|"  TC_DATABASE varchar(128) NOT NULL,"
operator|+
literal|"  TC_TABLE varchar(128),"
operator|+
literal|"  TC_PARTITION varchar(767))"
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"CREATE TABLE COMPLETED_TXN_COMPONENTS ("
operator|+
literal|"  CTC_TXNID bigint,"
operator|+
literal|"  CTC_DATABASE varchar(128) NOT NULL,"
operator|+
literal|"  CTC_TABLE varchar(128),"
operator|+
literal|"  CTC_PARTITION varchar(767))"
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"CREATE TABLE NEXT_TXN_ID ("
operator|+
literal|"  NTXN_NEXT bigint NOT NULL)"
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"INSERT INTO NEXT_TXN_ID VALUES(1)"
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"CREATE TABLE HIVE_LOCKS ("
operator|+
literal|" HL_LOCK_EXT_ID bigint NOT NULL,"
operator|+
literal|" HL_LOCK_INT_ID bigint NOT NULL,"
operator|+
literal|" HL_TXNID bigint,"
operator|+
literal|" HL_DB varchar(128) NOT NULL,"
operator|+
literal|" HL_TABLE varchar(128),"
operator|+
literal|" HL_PARTITION varchar(767),"
operator|+
literal|" HL_LOCK_STATE char(1) NOT NULL,"
operator|+
literal|" HL_LOCK_TYPE char(1) NOT NULL,"
operator|+
literal|" HL_LAST_HEARTBEAT bigint NOT NULL,"
operator|+
literal|" HL_ACQUIRED_AT bigint,"
operator|+
literal|" HL_USER varchar(128) NOT NULL,"
operator|+
literal|" HL_HOST varchar(128) NOT NULL,"
operator|+
literal|" PRIMARY KEY(HL_LOCK_EXT_ID, HL_LOCK_INT_ID))"
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"CREATE INDEX HL_TXNID_INDEX ON HIVE_LOCKS (HL_TXNID)"
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"CREATE TABLE NEXT_LOCK_ID ("
operator|+
literal|" NL_NEXT bigint NOT NULL)"
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"INSERT INTO NEXT_LOCK_ID VALUES(1)"
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"CREATE TABLE COMPACTION_QUEUE ("
operator|+
literal|" CQ_ID bigint PRIMARY KEY,"
operator|+
literal|" CQ_DATABASE varchar(128) NOT NULL,"
operator|+
literal|" CQ_TABLE varchar(128) NOT NULL,"
operator|+
literal|" CQ_PARTITION varchar(767),"
operator|+
literal|" CQ_STATE char(1) NOT NULL,"
operator|+
literal|" CQ_TYPE char(1) NOT NULL,"
operator|+
literal|" CQ_WORKER_ID varchar(128),"
operator|+
literal|" CQ_START bigint,"
operator|+
literal|" CQ_RUN_AS varchar(128),"
operator|+
literal|" CQ_HIGHEST_TXN_ID bigint,"
operator|+
literal|" CQ_META_INFO varchar(2048) for bit data,"
operator|+
literal|" CQ_HADOOP_JOB_ID varchar(32))"
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"CREATE TABLE NEXT_COMPACTION_QUEUE_ID (NCQ_NEXT bigint NOT NULL)"
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"INSERT INTO NEXT_COMPACTION_QUEUE_ID VALUES(1)"
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"CREATE TABLE COMPLETED_COMPACTIONS ("
operator|+
literal|" CC_ID bigint PRIMARY KEY,"
operator|+
literal|" CC_DATABASE varchar(128) NOT NULL,"
operator|+
literal|" CC_TABLE varchar(128) NOT NULL,"
operator|+
literal|" CC_PARTITION varchar(767),"
operator|+
literal|" CC_STATE char(1) NOT NULL,"
operator|+
literal|" CC_TYPE char(1) NOT NULL,"
operator|+
literal|" CC_WORKER_ID varchar(128),"
operator|+
literal|" CC_START bigint,"
operator|+
literal|" CC_END bigint,"
operator|+
literal|" CC_RUN_AS varchar(128),"
operator|+
literal|" CC_HIGHEST_TXN_ID bigint,"
operator|+
literal|" CC_META_INFO varchar(2048) for bit data,"
operator|+
literal|" CC_HADOOP_JOB_ID varchar(32))"
argument_list|)
expr_stmt|;
name|conn
operator|.
name|commit
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
comment|// This might be a deadlock, if so, let's retry
name|conn
operator|.
name|rollback
argument_list|()
expr_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|SQLTransactionRollbackException
operator|&&
name|deadlockCnt
operator|++
operator|<
literal|5
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Caught deadlock, retrying db creation"
argument_list|)
expr_stmt|;
name|prepDb
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
name|e
throw|;
block|}
block|}
finally|finally
block|{
name|deadlockCnt
operator|=
literal|0
expr_stmt|;
name|closeResources
argument_list|(
name|conn
argument_list|,
name|stmt
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|cleanDb
parameter_list|()
throws|throws
name|Exception
block|{
name|Connection
name|conn
init|=
literal|null
decl_stmt|;
name|Statement
name|stmt
init|=
literal|null
decl_stmt|;
try|try
block|{
name|conn
operator|=
name|getConnection
argument_list|()
expr_stmt|;
name|stmt
operator|=
name|conn
operator|.
name|createStatement
argument_list|()
expr_stmt|;
comment|// We want to try these, whether they succeed or fail.
try|try
block|{
name|stmt
operator|.
name|execute
argument_list|(
literal|"DROP INDEX HL_TXNID_INDEX"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Unable to drop index HL_TXNID_INDEX "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|dropTable
argument_list|(
name|stmt
argument_list|,
literal|"TXN_COMPONENTS"
argument_list|)
expr_stmt|;
name|dropTable
argument_list|(
name|stmt
argument_list|,
literal|"COMPLETED_TXN_COMPONENTS"
argument_list|)
expr_stmt|;
name|dropTable
argument_list|(
name|stmt
argument_list|,
literal|"TXNS"
argument_list|)
expr_stmt|;
name|dropTable
argument_list|(
name|stmt
argument_list|,
literal|"NEXT_TXN_ID"
argument_list|)
expr_stmt|;
name|dropTable
argument_list|(
name|stmt
argument_list|,
literal|"HIVE_LOCKS"
argument_list|)
expr_stmt|;
name|dropTable
argument_list|(
name|stmt
argument_list|,
literal|"NEXT_LOCK_ID"
argument_list|)
expr_stmt|;
name|dropTable
argument_list|(
name|stmt
argument_list|,
literal|"COMPACTION_QUEUE"
argument_list|)
expr_stmt|;
name|dropTable
argument_list|(
name|stmt
argument_list|,
literal|"NEXT_COMPACTION_QUEUE_ID"
argument_list|)
expr_stmt|;
name|dropTable
argument_list|(
name|stmt
argument_list|,
literal|"COMPLETED_COMPACTIONS"
argument_list|)
expr_stmt|;
name|conn
operator|.
name|commit
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|closeResources
argument_list|(
name|conn
argument_list|,
name|stmt
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|dropTable
parameter_list|(
name|Statement
name|stmt
parameter_list|,
name|String
name|name
parameter_list|)
block|{
try|try
block|{
name|stmt
operator|.
name|execute
argument_list|(
literal|"DROP TABLE "
operator|+
name|name
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Unable to drop table "
operator|+
name|name
operator|+
literal|": "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * A tool to count the number of partitions, tables,    * and databases locked by a particular lockId.    *    * @param lockId lock id to look for lock components    *    * @return number of components, or 0 if there is no lock    */
specifier|public
specifier|static
name|int
name|countLockComponents
parameter_list|(
name|long
name|lockId
parameter_list|)
throws|throws
name|Exception
block|{
name|Connection
name|conn
init|=
literal|null
decl_stmt|;
name|PreparedStatement
name|stmt
init|=
literal|null
decl_stmt|;
name|ResultSet
name|rs
init|=
literal|null
decl_stmt|;
try|try
block|{
name|conn
operator|=
name|getConnection
argument_list|()
expr_stmt|;
name|stmt
operator|=
name|conn
operator|.
name|prepareStatement
argument_list|(
literal|"SELECT count(*) FROM hive_locks WHERE hl_lock_ext_id = ?"
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|setLong
argument_list|(
literal|1
argument_list|,
name|lockId
argument_list|)
expr_stmt|;
name|rs
operator|=
name|stmt
operator|.
name|executeQuery
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|rs
operator|.
name|next
argument_list|()
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|rs
operator|.
name|getInt
argument_list|(
literal|1
argument_list|)
return|;
block|}
finally|finally
block|{
name|closeResources
argument_list|(
name|conn
argument_list|,
name|stmt
argument_list|,
name|rs
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|int
name|findNumCurrentLocks
parameter_list|()
throws|throws
name|Exception
block|{
name|Connection
name|conn
init|=
literal|null
decl_stmt|;
name|Statement
name|stmt
init|=
literal|null
decl_stmt|;
name|ResultSet
name|rs
init|=
literal|null
decl_stmt|;
try|try
block|{
name|conn
operator|=
name|getConnection
argument_list|()
expr_stmt|;
name|stmt
operator|=
name|conn
operator|.
name|createStatement
argument_list|()
expr_stmt|;
name|rs
operator|=
name|stmt
operator|.
name|executeQuery
argument_list|(
literal|"select count(*) from hive_locks"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|rs
operator|.
name|next
argument_list|()
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|rs
operator|.
name|getInt
argument_list|(
literal|1
argument_list|)
return|;
block|}
finally|finally
block|{
name|closeResources
argument_list|(
name|conn
argument_list|,
name|stmt
argument_list|,
name|rs
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|Connection
name|getConnection
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|String
name|jdbcDriver
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CONNECTION_DRIVER
argument_list|)
decl_stmt|;
name|Driver
name|driver
init|=
operator|(
name|Driver
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|jdbcDriver
argument_list|)
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|Properties
name|prop
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|String
name|driverUrl
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORECONNECTURLKEY
argument_list|)
decl_stmt|;
name|String
name|user
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CONNECTION_USER_NAME
argument_list|)
decl_stmt|;
name|String
name|passwd
init|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getPassword
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREPWD
operator|.
name|varname
argument_list|)
decl_stmt|;
name|prop
operator|.
name|setProperty
argument_list|(
literal|"user"
argument_list|,
name|user
argument_list|)
expr_stmt|;
name|prop
operator|.
name|setProperty
argument_list|(
literal|"password"
argument_list|,
name|passwd
argument_list|)
expr_stmt|;
name|Connection
name|conn
init|=
name|driver
operator|.
name|connect
argument_list|(
name|driverUrl
argument_list|,
name|prop
argument_list|)
decl_stmt|;
name|conn
operator|.
name|setAutoCommit
argument_list|(
literal|false
argument_list|)
expr_stmt|;
return|return
name|conn
return|;
block|}
specifier|private
specifier|static
name|void
name|closeResources
parameter_list|(
name|Connection
name|conn
parameter_list|,
name|Statement
name|stmt
parameter_list|,
name|ResultSet
name|rs
parameter_list|)
block|{
if|if
condition|(
name|rs
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|rs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Error closing ResultSet: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|stmt
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|stmt
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Error closing Statement: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|conn
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|conn
operator|.
name|rollback
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Error rolling back: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Error closing Connection: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

