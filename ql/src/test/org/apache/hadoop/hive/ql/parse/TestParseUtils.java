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
operator|.
name|parse
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
name|metastore
operator|.
name|api
operator|.
name|TxnType
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
name|AcidUtils
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
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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

begin_comment
comment|/**  * Transaction type derived from the original query test.  */
end_comment

begin_class
annotation|@
name|RunWith
argument_list|(
name|value
operator|=
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestParseUtils
block|{
specifier|private
name|String
name|query
decl_stmt|;
specifier|private
name|TxnType
name|txnType
decl_stmt|;
specifier|public
name|TestParseUtils
parameter_list|(
name|String
name|query
parameter_list|,
name|TxnType
name|txnType
parameter_list|)
block|{
name|this
operator|.
name|query
operator|=
name|query
expr_stmt|;
name|this
operator|.
name|txnType
operator|=
name|txnType
expr_stmt|;
block|}
annotation|@
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|data
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Object
index|[]
index|[]
block|{
block|{
literal|"SELECT current_timestamp()"
block|,
name|TxnType
operator|.
name|READ_ONLY
block|}
block|,
block|{
literal|"SELECT count(*) FROM a"
block|,
name|TxnType
operator|.
name|READ_ONLY
block|}
block|,
block|{
literal|"SELECT count(*) FROM a JOIN b ON a.id = b.id"
block|,
name|TxnType
operator|.
name|READ_ONLY
block|}
block|,
block|{
literal|"WITH a AS (SELECT current_timestamp()) "
operator|+
literal|"  SELECT * FROM a"
block|,
name|TxnType
operator|.
name|READ_ONLY
block|}
block|,
block|{
literal|"INSERT INTO a VALUES (1, 2)"
block|,
name|TxnType
operator|.
name|DEFAULT
block|}
block|,
block|{
literal|"INSERT INTO a SELECT * FROM b"
block|,
name|TxnType
operator|.
name|DEFAULT
block|}
block|,
block|{
literal|"INSERT OVERWRITE TABLE a SELECT * FROM b"
block|,
name|TxnType
operator|.
name|DEFAULT
block|}
block|,
block|{
literal|"FROM b INSERT OVERWRITE TABLE a SELECT *"
block|,
name|TxnType
operator|.
name|DEFAULT
block|}
block|,
block|{
literal|"WITH a AS (SELECT current_timestamp()) "
operator|+
literal|"  INSERT INTO b SELECT * FROM a"
block|,
name|TxnType
operator|.
name|DEFAULT
block|}
block|,
block|{
literal|"UPDATE a SET col_b = 1"
block|,
name|TxnType
operator|.
name|DEFAULT
block|}
block|,
block|{
literal|"DELETE FROM a WHERE col_b = 1"
block|,
name|TxnType
operator|.
name|DEFAULT
block|}
block|,
block|{
literal|"CREATE TABLE a (col_b int)"
block|,
name|TxnType
operator|.
name|DEFAULT
block|}
block|,
block|{
literal|"CREATE TABLE a AS SELECT * FROM b"
block|,
name|TxnType
operator|.
name|DEFAULT
block|}
block|,
block|{
literal|"DROP TABLE a"
block|,
name|TxnType
operator|.
name|DEFAULT
block|}
block|,
block|{
literal|"LOAD DATA LOCAL INPATH './examples/files/kv.txt' "
operator|+
literal|"  OVERWRITE INTO TABLE a"
block|,
name|TxnType
operator|.
name|DEFAULT
block|}
block|,
block|{
literal|"REPL LOAD a from './examples/files/kv.txt'"
block|,
name|TxnType
operator|.
name|DEFAULT
block|}
block|,
block|{
literal|"REPL DUMP a"
block|,
name|TxnType
operator|.
name|DEFAULT
block|}
block|,
block|{
literal|"REPL STATUS a"
block|,
name|TxnType
operator|.
name|DEFAULT
block|}
block|,
block|{
literal|"MERGE INTO a trg using b src "
operator|+
literal|"  ON src.col_a = trg.col_a "
operator|+
literal|"WHEN MATCHED THEN "
operator|+
literal|"  UPDATE SET col_b = src.col_b "
operator|+
literal|"WHEN NOT MATCHED THEN "
operator|+
literal|"  INSERT VALUES (src.col_a, src.col_b)"
block|,
name|TxnType
operator|.
name|DEFAULT
block|}
block|,       }
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTxnType
parameter_list|()
throws|throws
name|ParseException
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|AcidUtils
operator|.
name|getTxnType
argument_list|(
name|ParseUtils
operator|.
name|parse
argument_list|(
name|query
argument_list|)
argument_list|)
argument_list|,
name|txnType
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

