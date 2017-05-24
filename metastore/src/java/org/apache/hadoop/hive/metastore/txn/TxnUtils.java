begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|common
operator|.
name|ValidCompactorTxnList
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
name|common
operator|.
name|ValidReadTxnList
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
name|common
operator|.
name|ValidTxnList
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
name|metastore
operator|.
name|MetaStoreUtils
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
name|GetOpenTxnsInfoResponse
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
name|GetOpenTxnsResponse
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
name|api
operator|.
name|TxnInfo
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
name|TxnState
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
name|hive_metastoreConstants
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
name|BitSet
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

begin_class
specifier|public
class|class
name|TxnUtils
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TxnUtils
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Transform a {@link org.apache.hadoop.hive.metastore.api.GetOpenTxnsResponse} to a    * {@link org.apache.hadoop.hive.common.ValidTxnList}.  This assumes that the caller intends to    * read the files, and thus treats both open and aborted transactions as invalid.    * @param txns txn list from the metastore    * @param currentTxn Current transaction that the user has open.  If this is greater than 0 it    *                   will be removed from the exceptions list so that the user sees his own    *                   transaction as valid.    * @return a valid txn list.    */
specifier|public
specifier|static
name|ValidTxnList
name|createValidReadTxnList
parameter_list|(
name|GetOpenTxnsResponse
name|txns
parameter_list|,
name|long
name|currentTxn
parameter_list|)
block|{
comment|/*todo: should highWater be min(currentTxn,txns.getTxn_high_water_mark()) assuming currentTxn>0      * otherwise if currentTxn=7 and 8 commits before 7, then 7 will see result of 8 which      * doesn't make sense for Snapshot Isolation.  Of course for Read Committed, the list should      * inlude the latest committed set.*/
name|long
name|highWater
init|=
name|txns
operator|.
name|getTxn_high_water_mark
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Long
argument_list|>
name|open
init|=
name|txns
operator|.
name|getOpen_txns
argument_list|()
decl_stmt|;
name|BitSet
name|abortedBits
init|=
name|BitSet
operator|.
name|valueOf
argument_list|(
name|txns
operator|.
name|getAbortedBits
argument_list|()
argument_list|)
decl_stmt|;
name|long
index|[]
name|exceptions
init|=
operator|new
name|long
index|[
name|open
operator|.
name|size
argument_list|()
operator|-
operator|(
name|currentTxn
operator|>
literal|0
condition|?
literal|1
else|:
literal|0
operator|)
index|]
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|long
name|txn
range|:
name|open
control|)
block|{
if|if
condition|(
name|currentTxn
operator|>
literal|0
operator|&&
name|currentTxn
operator|==
name|txn
condition|)
continue|continue;
name|exceptions
index|[
name|i
operator|++
index|]
operator|=
name|txn
expr_stmt|;
block|}
if|if
condition|(
name|txns
operator|.
name|isSetMin_open_txn
argument_list|()
condition|)
block|{
return|return
operator|new
name|ValidReadTxnList
argument_list|(
name|exceptions
argument_list|,
name|abortedBits
argument_list|,
name|highWater
argument_list|,
name|txns
operator|.
name|getMin_open_txn
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|ValidReadTxnList
argument_list|(
name|exceptions
argument_list|,
name|abortedBits
argument_list|,
name|highWater
argument_list|)
return|;
block|}
block|}
comment|/**    * Transform a {@link org.apache.hadoop.hive.metastore.api.GetOpenTxnsInfoResponse} to a    * {@link org.apache.hadoop.hive.common.ValidTxnList}.  This assumes that the caller intends to    * compact the files, and thus treats only open transactions as invalid.  Additionally any    * txnId> highestOpenTxnId is also invalid.  This is to avoid creating something like    * delta_17_120 where txnId 80, for example, is still open.    * @param txns txn list from the metastore    * @return a valid txn list.    */
specifier|public
specifier|static
name|ValidTxnList
name|createValidCompactTxnList
parameter_list|(
name|GetOpenTxnsInfoResponse
name|txns
parameter_list|)
block|{
name|long
name|highWater
init|=
name|txns
operator|.
name|getTxn_high_water_mark
argument_list|()
decl_stmt|;
name|long
name|minOpenTxn
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|long
index|[]
name|exceptions
init|=
operator|new
name|long
index|[
name|txns
operator|.
name|getOpen_txnsSize
argument_list|()
index|]
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|TxnInfo
name|txn
range|:
name|txns
operator|.
name|getOpen_txns
argument_list|()
control|)
block|{
if|if
condition|(
name|txn
operator|.
name|getState
argument_list|()
operator|==
name|TxnState
operator|.
name|OPEN
condition|)
block|{
name|minOpenTxn
operator|=
name|Math
operator|.
name|min
argument_list|(
name|minOpenTxn
argument_list|,
name|txn
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|//only need aborted since we don't consider anything above minOpenTxn
name|exceptions
index|[
name|i
operator|++
index|]
operator|=
name|txn
operator|.
name|getId
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|i
operator|<
name|exceptions
operator|.
name|length
condition|)
block|{
name|exceptions
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|exceptions
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
name|highWater
operator|=
name|minOpenTxn
operator|==
name|Long
operator|.
name|MAX_VALUE
condition|?
name|highWater
else|:
name|minOpenTxn
operator|-
literal|1
expr_stmt|;
name|BitSet
name|bitSet
init|=
operator|new
name|BitSet
argument_list|(
name|exceptions
operator|.
name|length
argument_list|)
decl_stmt|;
name|bitSet
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|exceptions
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// for ValidCompactorTxnList, everything in exceptions are aborted
return|return
operator|new
name|ValidCompactorTxnList
argument_list|(
name|exceptions
argument_list|,
name|bitSet
argument_list|,
name|highWater
argument_list|)
return|;
block|}
comment|/**    * Get an instance of the TxnStore that is appropriate for this store    * @param conf configuration    * @return txn store    */
specifier|public
specifier|static
name|TxnStore
name|getTxnStore
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|String
name|className
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_TXN_STORE_IMPL
argument_list|)
decl_stmt|;
try|try
block|{
name|TxnStore
name|handler
init|=
operator|(
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|TxnHandler
argument_list|>
operator|)
name|MetaStoreUtils
operator|.
name|getClass
argument_list|(
name|className
argument_list|)
operator|)
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|handler
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
return|return
name|handler
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to instantiate raw store directly in fastpath mode"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/** Checks if a table is a valid ACID table.    * Note, users are responsible for using the correct TxnManager. We do not look at    * SessionState.get().getTxnMgr().supportsAcid() here    * @param table table    * @return true if table is a legit ACID table, false otherwise    */
specifier|public
specifier|static
name|boolean
name|isAcidTable
parameter_list|(
name|Table
name|table
parameter_list|)
block|{
if|if
condition|(
name|table
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
init|=
name|table
operator|.
name|getParameters
argument_list|()
decl_stmt|;
name|String
name|tableIsTransactional
init|=
name|parameters
operator|.
name|get
argument_list|(
name|hive_metastoreConstants
operator|.
name|TABLE_IS_TRANSACTIONAL
argument_list|)
decl_stmt|;
return|return
name|tableIsTransactional
operator|!=
literal|null
operator|&&
name|tableIsTransactional
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"true"
argument_list|)
return|;
block|}
comment|/**    * Build a query (or queries if one query is too big) with specified "prefix" and "suffix",    * while populating the IN list into multiple OR clauses, e.g. id in (1,2,3) OR id in (4,5,6)    * For NOT IN case, NOT IN list is broken into multiple AND clauses.    * @param queries array of complete query strings    * @param prefix part of the query that comes before IN list    * @param suffix part of the query that comes after IN list    * @param inList the list containing IN list values    * @param inColumn column name of IN list operator    * @param addParens add a pair of parenthesis outside the IN lists    *                  e.g. ( id in (1,2,3) OR id in (4,5,6) )    * @param notIn clause to be broken up is NOT IN    */
specifier|public
specifier|static
name|void
name|buildQueryWithINClause
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|queries
parameter_list|,
name|StringBuilder
name|prefix
parameter_list|,
name|StringBuilder
name|suffix
parameter_list|,
name|List
argument_list|<
name|Long
argument_list|>
name|inList
parameter_list|,
name|String
name|inColumn
parameter_list|,
name|boolean
name|addParens
parameter_list|,
name|boolean
name|notIn
parameter_list|)
block|{
if|if
condition|(
name|inList
operator|==
literal|null
operator|||
name|inList
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"The IN list is empty!"
argument_list|)
throw|;
block|}
name|int
name|batchSize
init|=
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_DIRECT_SQL_MAX_ELEMENTS_IN_CLAUSE
argument_list|)
decl_stmt|;
name|int
name|numWholeBatches
init|=
name|inList
operator|.
name|size
argument_list|()
operator|/
name|batchSize
decl_stmt|;
name|StringBuilder
name|buf
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|prefix
argument_list|)
expr_stmt|;
if|if
condition|(
name|addParens
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|"("
argument_list|)
expr_stmt|;
block|}
name|buf
operator|.
name|append
argument_list|(
name|inColumn
argument_list|)
expr_stmt|;
if|if
condition|(
name|notIn
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|" not in ("
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|buf
operator|.
name|append
argument_list|(
literal|" in ("
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
name|numWholeBatches
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|*
name|batchSize
operator|==
name|inList
operator|.
name|size
argument_list|()
condition|)
block|{
comment|// At this point we just realized we don't need another query
break|break;
block|}
if|if
condition|(
name|needNewQuery
argument_list|(
name|conf
argument_list|,
name|buf
argument_list|)
condition|)
block|{
comment|// Wrap up current query string
if|if
condition|(
name|addParens
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
block|}
name|buf
operator|.
name|append
argument_list|(
name|suffix
argument_list|)
expr_stmt|;
name|queries
operator|.
name|add
argument_list|(
name|buf
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Prepare a new query string
name|buf
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|notIn
condition|)
block|{
if|if
condition|(
name|buf
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
name|prefix
argument_list|)
expr_stmt|;
if|if
condition|(
name|addParens
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|"("
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|buf
operator|.
name|append
argument_list|(
literal|" and "
argument_list|)
expr_stmt|;
block|}
name|buf
operator|.
name|append
argument_list|(
name|inColumn
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|" not in ("
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|buf
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
name|prefix
argument_list|)
expr_stmt|;
if|if
condition|(
name|addParens
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|"("
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|buf
operator|.
name|append
argument_list|(
literal|" or "
argument_list|)
expr_stmt|;
block|}
name|buf
operator|.
name|append
argument_list|(
name|inColumn
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|" in ("
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|int
name|j
init|=
name|i
operator|*
name|batchSize
init|;
name|j
operator|<
operator|(
name|i
operator|+
literal|1
operator|)
operator|*
name|batchSize
operator|&&
name|j
operator|<
name|inList
operator|.
name|size
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|buf
operator|.
name|append
argument_list|(
name|inList
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|buf
operator|.
name|setCharAt
argument_list|(
name|buf
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|,
literal|')'
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|addParens
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
block|}
name|buf
operator|.
name|append
argument_list|(
name|suffix
argument_list|)
expr_stmt|;
name|queries
operator|.
name|add
argument_list|(
name|buf
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/** Estimate if the size of a string will exceed certain limit */
specifier|private
specifier|static
name|boolean
name|needNewQuery
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|StringBuilder
name|sb
parameter_list|)
block|{
name|int
name|queryMemoryLimit
init|=
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_DIRECT_SQL_MAX_QUERY_LENGTH
argument_list|)
decl_stmt|;
comment|// http://www.javamex.com/tutorials/memory/string_memory_usage.shtml
name|long
name|sizeInBytes
init|=
literal|8
operator|*
operator|(
operator|(
operator|(
name|sb
operator|.
name|length
argument_list|()
operator|*
literal|2
operator|)
operator|+
literal|45
operator|)
operator|/
literal|8
operator|)
decl_stmt|;
return|return
name|sizeInBytes
operator|/
literal|1024
operator|>
name|queryMemoryLimit
return|;
block|}
block|}
end_class

end_unit

