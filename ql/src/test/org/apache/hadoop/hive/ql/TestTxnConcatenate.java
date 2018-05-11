begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metastore
operator|.
name|api
operator|.
name|ShowCompactRequest
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
name|ShowCompactResponse
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
name|txn
operator|.
name|TxnStore
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
name|txn
operator|.
name|TxnUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TemporaryFolder
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
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_class
specifier|public
class|class
name|TestTxnConcatenate
extends|extends
name|TxnCommandsBaseForTests
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
name|TestTxnConcatenate
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEST_DATA_DIR
init|=
operator|new
name|File
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
operator|+
name|File
operator|.
name|separator
operator|+
name|TestTxnLoadData
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
operator|+
literal|"-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
operator|.
name|getPath
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"\\\\"
argument_list|,
literal|"/"
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TemporaryFolder
name|folder
init|=
operator|new
name|TemporaryFolder
argument_list|()
decl_stmt|;
annotation|@
name|Override
name|String
name|getTestDataDir
parameter_list|()
block|{
return|return
name|TEST_DATA_DIR
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConcatenate
parameter_list|()
throws|throws
name|Exception
block|{
name|runStatementOnDriver
argument_list|(
literal|"insert into "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|" values(1,2),(4,5)"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"update "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|" set b = 4"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|" values(5,6),(8,8)"
argument_list|)
expr_stmt|;
name|String
name|testQuery
init|=
literal|"select ROW__ID, a, b, INPUT__FILE__NAME from "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|" order by a, b"
decl_stmt|;
name|String
index|[]
index|[]
name|expected
init|=
operator|new
name|String
index|[]
index|[]
block|{
block|{
literal|"{\"writeid\":2,\"bucketid\":536936448,\"rowid\":1}\t1\t4"
block|,
literal|"acidtbl/delta_0000002_0000002_0000/bucket_00001"
block|}
block|,
block|{
literal|"{\"writeid\":2,\"bucketid\":536936448,\"rowid\":0}\t4\t4"
block|,
literal|"acidtbl/delta_0000002_0000002_0000/bucket_00001"
block|}
block|,
block|{
literal|"{\"writeid\":3,\"bucketid\":536936448,\"rowid\":1}\t5\t6"
block|,
literal|"acidtbl/delta_0000003_0000003_0000/bucket_00001"
block|}
block|,
block|{
literal|"{\"writeid\":3,\"bucketid\":536936448,\"rowid\":0}\t8\t8"
block|,
literal|"acidtbl/delta_0000003_0000003_0000/bucket_00001"
block|}
block|}
decl_stmt|;
name|checkResult
argument_list|(
name|expected
argument_list|,
name|testQuery
argument_list|,
literal|false
argument_list|,
literal|"check data"
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
comment|/*in UTs, there is no standalone HMS running to kick off compaction so it's done via runWorker()      but in normal usage 'concatenate' is blocking, */
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|TRANSACTIONAL_CONCATENATE_NOBLOCK
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"alter table "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|" concatenate"
argument_list|)
expr_stmt|;
name|TxnStore
name|txnStore
init|=
name|TxnUtils
operator|.
name|getTxnStore
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|ShowCompactResponse
name|rsp
init|=
name|txnStore
operator|.
name|showCompact
argument_list|(
operator|new
name|ShowCompactRequest
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rsp
operator|.
name|getCompactsSize
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TxnStore
operator|.
name|INITIATED_RESPONSE
argument_list|,
name|rsp
operator|.
name|getCompacts
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|runWorker
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|txnStore
operator|.
name|showCompact
argument_list|(
operator|new
name|ShowCompactRequest
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rsp
operator|.
name|getCompactsSize
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TxnStore
operator|.
name|CLEANING_RESPONSE
argument_list|,
name|rsp
operator|.
name|getCompacts
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|String
index|[]
index|[]
name|expected2
init|=
operator|new
name|String
index|[]
index|[]
block|{
block|{
literal|"{\"writeid\":2,\"bucketid\":536936448,\"rowid\":1}\t1\t4"
block|,
literal|"acidtbl/base_0000003/bucket_00001"
block|}
block|,
block|{
literal|"{\"writeid\":2,\"bucketid\":536936448,\"rowid\":0}\t4\t4"
block|,
literal|"acidtbl/base_0000003/bucket_00001"
block|}
block|,
block|{
literal|"{\"writeid\":3,\"bucketid\":536936448,\"rowid\":1}\t5\t6"
block|,
literal|"acidtbl/base_0000003/bucket_00001"
block|}
block|,
block|{
literal|"{\"writeid\":3,\"bucketid\":536936448,\"rowid\":0}\t8\t8"
block|,
literal|"acidtbl/base_0000003/bucket_00001"
block|}
block|}
decl_stmt|;
name|checkResult
argument_list|(
name|expected2
argument_list|,
name|testQuery
argument_list|,
literal|false
argument_list|,
literal|"check data after concatenate"
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConcatenatePart
parameter_list|()
throws|throws
name|Exception
block|{
name|runStatementOnDriver
argument_list|(
literal|"insert into "
operator|+
name|Table
operator|.
name|ACIDTBLPART
operator|+
literal|" values(1,2,'p1'),(4,5,'p2')"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"update "
operator|+
name|Table
operator|.
name|ACIDTBLPART
operator|+
literal|" set b = 4 where p='p1'"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into "
operator|+
name|Table
operator|.
name|ACIDTBLPART
operator|+
literal|" values(5,6,'p1'),(8,8,'p2')"
argument_list|)
expr_stmt|;
name|String
name|testQuery
init|=
literal|"select ROW__ID, a, b, INPUT__FILE__NAME from "
operator|+
name|Table
operator|.
name|ACIDTBLPART
operator|+
literal|" order by a, b"
decl_stmt|;
name|String
index|[]
index|[]
name|expected
init|=
operator|new
name|String
index|[]
index|[]
block|{
block|{
literal|"{\"writeid\":2,\"bucketid\":536936448,\"rowid\":0}\t1\t4"
block|,
literal|"acidtblpart/p=p1/delta_0000002_0000002_0000/bucket_00001"
block|}
block|,
block|{
literal|"{\"writeid\":1,\"bucketid\":536936448,\"rowid\":0}\t4\t5"
block|,
literal|"acidtblpart/p=p2/delta_0000001_0000001_0000/bucket_00001"
block|}
block|,
block|{
literal|"{\"writeid\":3,\"bucketid\":536936448,\"rowid\":0}\t5\t6"
block|,
literal|"acidtblpart/p=p1/delta_0000003_0000003_0000/bucket_00001"
block|}
block|,
block|{
literal|"{\"writeid\":3,\"bucketid\":536936448,\"rowid\":0}\t8\t8"
block|,
literal|"acidtblpart/p=p2/delta_0000003_0000003_0000/bucket_00001"
block|}
block|}
decl_stmt|;
name|checkResult
argument_list|(
name|expected
argument_list|,
name|testQuery
argument_list|,
literal|false
argument_list|,
literal|"check data"
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
comment|/*in UTs, there is no standalone HMS running to kick off compaction so it's done via runWorker()      but in normal usage 'concatenate' is blocking, */
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|TRANSACTIONAL_CONCATENATE_NOBLOCK
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"alter table "
operator|+
name|Table
operator|.
name|ACIDTBLPART
operator|+
literal|" PARTITION(p='p1') concatenate"
argument_list|)
expr_stmt|;
name|TxnStore
name|txnStore
init|=
name|TxnUtils
operator|.
name|getTxnStore
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|ShowCompactResponse
name|rsp
init|=
name|txnStore
operator|.
name|showCompact
argument_list|(
operator|new
name|ShowCompactRequest
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rsp
operator|.
name|getCompactsSize
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TxnStore
operator|.
name|INITIATED_RESPONSE
argument_list|,
name|rsp
operator|.
name|getCompacts
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|runWorker
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|txnStore
operator|.
name|showCompact
argument_list|(
operator|new
name|ShowCompactRequest
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rsp
operator|.
name|getCompactsSize
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TxnStore
operator|.
name|CLEANING_RESPONSE
argument_list|,
name|rsp
operator|.
name|getCompacts
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|String
index|[]
index|[]
name|expected2
init|=
operator|new
name|String
index|[]
index|[]
block|{
block|{
literal|"{\"writeid\":2,\"bucketid\":536936448,\"rowid\":0}\t1\t4"
block|,
literal|"acidtblpart/p=p1/base_0000003/bucket_00001"
block|}
block|,
block|{
literal|"{\"writeid\":1,\"bucketid\":536936448,\"rowid\":0}\t4\t5"
block|,
literal|"acidtblpart/p=p2/delta_0000001_0000001_0000/bucket_00001"
block|}
block|,
block|{
literal|"{\"writeid\":3,\"bucketid\":536936448,\"rowid\":0}\t5\t6"
block|,
literal|"acidtblpart/p=p1/base_0000003/bucket_00001"
block|}
block|,
block|{
literal|"{\"writeid\":3,\"bucketid\":536936448,\"rowid\":0}\t8\t8"
block|,
literal|"acidtblpart/p=p2/delta_0000003_0000003_0000/bucket_00001"
block|}
block|}
decl_stmt|;
name|checkResult
argument_list|(
name|expected2
argument_list|,
name|testQuery
argument_list|,
literal|false
argument_list|,
literal|"check data after concatenate"
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConcatenateMM
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
operator|.
name|setBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_CREATE_TABLES_AS_INSERT_ONLY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"drop table if exists T"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table T(a int, b int)"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into T values(1,2),(4,5)"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into T values(5,6),(8,8)"
argument_list|)
expr_stmt|;
name|String
name|testQuery
init|=
literal|"select a, b, INPUT__FILE__NAME from T order by a, b"
decl_stmt|;
name|String
index|[]
index|[]
name|expected
init|=
operator|new
name|String
index|[]
index|[]
block|{
block|{
literal|"1\t2"
block|,
literal|"t/delta_0000001_0000001_0000/000000_0"
block|}
block|,
block|{
literal|"4\t5"
block|,
literal|"t/delta_0000001_0000001_0000/000000_0"
block|}
block|,
block|{
literal|"5\t6"
block|,
literal|"t/delta_0000002_0000002_0000/000000_0"
block|}
block|,
block|{
literal|"8\t8"
block|,
literal|"t/delta_0000002_0000002_0000/000000_0"
block|}
block|}
decl_stmt|;
name|checkResult
argument_list|(
name|expected
argument_list|,
name|testQuery
argument_list|,
literal|false
argument_list|,
literal|"check data"
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
comment|/*in UTs, there is no standalone HMS running to kick off compaction so it's done via runWorker()      but in normal usage 'concatenate' is blocking, */
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|TRANSACTIONAL_CONCATENATE_NOBLOCK
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"alter table T concatenate"
argument_list|)
expr_stmt|;
name|TxnStore
name|txnStore
init|=
name|TxnUtils
operator|.
name|getTxnStore
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|ShowCompactResponse
name|rsp
init|=
name|txnStore
operator|.
name|showCompact
argument_list|(
operator|new
name|ShowCompactRequest
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rsp
operator|.
name|getCompactsSize
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TxnStore
operator|.
name|INITIATED_RESPONSE
argument_list|,
name|rsp
operator|.
name|getCompacts
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|runWorker
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|txnStore
operator|.
name|showCompact
argument_list|(
operator|new
name|ShowCompactRequest
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rsp
operator|.
name|getCompactsSize
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TxnStore
operator|.
name|CLEANING_RESPONSE
argument_list|,
name|rsp
operator|.
name|getCompacts
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|String
index|[]
index|[]
name|expected2
init|=
operator|new
name|String
index|[]
index|[]
block|{
block|{
literal|"1\t2"
block|,
literal|"t/base_0000002/000000_0"
block|}
block|,
block|{
literal|"4\t5"
block|,
literal|"t/base_0000002/000000_0"
block|}
block|,
block|{
literal|"5\t6"
block|,
literal|"t/base_0000002/000000_0"
block|}
block|,
block|{
literal|"8\t8"
block|,
literal|"t/base_0000002/000000_0"
block|}
block|}
decl_stmt|;
name|checkResult
argument_list|(
name|expected2
argument_list|,
name|testQuery
argument_list|,
literal|false
argument_list|,
literal|"check data after concatenate"
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

