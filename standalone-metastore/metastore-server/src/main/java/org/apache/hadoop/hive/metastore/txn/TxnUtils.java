begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hive
operator|.
name|common
operator|.
name|ValidCompactorWriteIdList
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
name|ValidReaderWriteIdList
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
name|common
operator|.
name|ValidTxnWriteIdList
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
name|ValidWriteIdList
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
name|TransactionalValidationListener
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
name|*
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
name|conf
operator|.
name|MetastoreConf
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
name|conf
operator|.
name|MetastoreConf
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
name|utils
operator|.
name|JavaUtils
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
name|Collections
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
comment|// Transactional stats states
specifier|static
specifier|final
specifier|public
name|char
name|STAT_OPEN
init|=
literal|'o'
decl_stmt|;
specifier|static
specifier|final
specifier|public
name|char
name|STAT_INVALID
init|=
literal|'i'
decl_stmt|;
specifier|static
specifier|final
specifier|public
name|char
name|STAT_COMMITTED
init|=
literal|'c'
decl_stmt|;
specifier|static
specifier|final
specifier|public
name|char
name|STAT_OBSOLETE
init|=
literal|'s'
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
comment|/*      * The highWaterMark should be min(currentTxn,txns.getTxn_high_water_mark()) assuming currentTxn>0      * otherwise if currentTxn=7 and 8 commits before 7, then 7 will see result of 8 which      * doesn't make sense for Snapshot Isolation. Of course for Read Committed, the list should      * include the latest committed set.      */
name|long
name|highWaterMark
init|=
operator|(
name|currentTxn
operator|>
literal|0
operator|)
condition|?
name|Math
operator|.
name|min
argument_list|(
name|currentTxn
argument_list|,
name|txns
operator|.
name|getTxn_high_water_mark
argument_list|()
argument_list|)
else|:
name|txns
operator|.
name|getTxn_high_water_mark
argument_list|()
decl_stmt|;
comment|// Open txns are already sorted in ascending order. This list may or may not include HWM
comment|// but it is guaranteed that list won't have txn> HWM. But, if we overwrite the HWM with currentTxn
comment|// then need to truncate the exceptions list accordingly.
name|List
argument_list|<
name|Long
argument_list|>
name|openTxns
init|=
name|txns
operator|.
name|getOpen_txns
argument_list|()
decl_stmt|;
comment|// We care only about open/aborted txns below currentTxn and hence the size should be determined
comment|// for the exceptions list. The currentTxn will be missing in openTxns list only in rare case like
comment|// txn is aborted by AcidHouseKeeperService and compactor actually cleans up the aborted txns.
comment|// So, for such cases, we get negative value for sizeToHwm with found position for currentTxn, and so,
comment|// we just negate it to get the size.
name|int
name|sizeToHwm
init|=
operator|(
name|currentTxn
operator|>
literal|0
operator|)
condition|?
name|Collections
operator|.
name|binarySearch
argument_list|(
name|openTxns
argument_list|,
name|currentTxn
argument_list|)
else|:
name|openTxns
operator|.
name|size
argument_list|()
decl_stmt|;
name|sizeToHwm
operator|=
operator|(
name|sizeToHwm
operator|<
literal|0
operator|)
condition|?
operator|(
operator|-
name|sizeToHwm
operator|)
else|:
name|sizeToHwm
expr_stmt|;
name|long
index|[]
name|exceptions
init|=
operator|new
name|long
index|[
name|sizeToHwm
index|]
decl_stmt|;
name|BitSet
name|inAbortedBits
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
name|BitSet
name|outAbortedBits
init|=
operator|new
name|BitSet
argument_list|()
decl_stmt|;
name|long
name|minOpenTxnId
init|=
name|Long
operator|.
name|MAX_VALUE
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
name|openTxns
control|)
block|{
comment|// For snapshot isolation, we don't care about txns greater than current txn and so stop here.
comment|// Also, we need not include current txn to exceptions list.
if|if
condition|(
operator|(
name|currentTxn
operator|>
literal|0
operator|)
operator|&&
operator|(
name|txn
operator|>=
name|currentTxn
operator|)
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|inAbortedBits
operator|.
name|get
argument_list|(
name|i
argument_list|)
condition|)
block|{
name|outAbortedBits
operator|.
name|set
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|minOpenTxnId
operator|==
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
name|minOpenTxnId
operator|=
name|txn
expr_stmt|;
block|}
name|exceptions
index|[
name|i
operator|++
index|]
operator|=
name|txn
expr_stmt|;
block|}
return|return
operator|new
name|ValidReadTxnList
argument_list|(
name|exceptions
argument_list|,
name|outAbortedBits
argument_list|,
name|highWaterMark
argument_list|,
name|minOpenTxnId
argument_list|)
return|;
block|}
comment|/**    * Transform a {@link org.apache.hadoop.hive.metastore.api.GetValidWriteIdsResponse} to a    * {@link org.apache.hadoop.hive.common.ValidTxnWriteIdList}.  This assumes that the caller intends to    * read the files, and thus treats both open and aborted transactions as invalid.    * @param currentTxnId current txn ID for which we get the valid write ids list    * @param list valid write ids list from the metastore    * @return a valid write IDs list for the whole transaction.    */
specifier|public
specifier|static
name|ValidTxnWriteIdList
name|createValidTxnWriteIdList
parameter_list|(
name|Long
name|currentTxnId
parameter_list|,
name|List
argument_list|<
name|TableValidWriteIds
argument_list|>
name|validIds
parameter_list|)
block|{
name|ValidTxnWriteIdList
name|validTxnWriteIdList
init|=
operator|new
name|ValidTxnWriteIdList
argument_list|(
name|currentTxnId
argument_list|)
decl_stmt|;
for|for
control|(
name|TableValidWriteIds
name|tableWriteIds
range|:
name|validIds
control|)
block|{
name|validTxnWriteIdList
operator|.
name|addTableValidWriteIdList
argument_list|(
name|createValidReaderWriteIdList
argument_list|(
name|tableWriteIds
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|validTxnWriteIdList
return|;
block|}
comment|/**    * Transform a {@link org.apache.hadoop.hive.metastore.api.TableValidWriteIds} to a    * {@link org.apache.hadoop.hive.common.ValidReaderWriteIdList}.  This assumes that the caller intends to    * read the files, and thus treats both open and aborted write ids as invalid.    * @param tableWriteIds valid write ids for the given table from the metastore    * @return a valid write IDs list for the input table    */
specifier|public
specifier|static
name|ValidReaderWriteIdList
name|createValidReaderWriteIdList
parameter_list|(
name|TableValidWriteIds
name|tableWriteIds
parameter_list|)
block|{
name|String
name|fullTableName
init|=
name|tableWriteIds
operator|.
name|getFullTableName
argument_list|()
decl_stmt|;
name|long
name|highWater
init|=
name|tableWriteIds
operator|.
name|getWriteIdHighWaterMark
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Long
argument_list|>
name|invalids
init|=
name|tableWriteIds
operator|.
name|getInvalidWriteIds
argument_list|()
decl_stmt|;
name|BitSet
name|abortedBits
init|=
name|BitSet
operator|.
name|valueOf
argument_list|(
name|tableWriteIds
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
name|invalids
operator|.
name|size
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
name|long
name|writeId
range|:
name|invalids
control|)
block|{
name|exceptions
index|[
name|i
operator|++
index|]
operator|=
name|writeId
expr_stmt|;
block|}
if|if
condition|(
name|tableWriteIds
operator|.
name|isSetMinOpenWriteId
argument_list|()
condition|)
block|{
return|return
operator|new
name|ValidReaderWriteIdList
argument_list|(
name|fullTableName
argument_list|,
name|exceptions
argument_list|,
name|abortedBits
argument_list|,
name|highWater
argument_list|,
name|tableWriteIds
operator|.
name|getMinOpenWriteId
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|ValidReaderWriteIdList
argument_list|(
name|fullTableName
argument_list|,
name|exceptions
argument_list|,
name|abortedBits
argument_list|,
name|highWater
argument_list|)
return|;
block|}
block|}
comment|/**    * Transform a {@link org.apache.hadoop.hive.metastore.api.TableValidWriteIds} to a    * {@link org.apache.hadoop.hive.common.ValidCompactorWriteIdList}.  This assumes that the caller intends to    * compact the files, and thus treats only open transactions/write ids as invalid.  Additionally any    * writeId&gt; highestOpenWriteId is also invalid.  This is to avoid creating something like    * delta_17_120 where writeId 80, for example, is still open.    * @param tableValidWriteIds table write id list from the metastore    * @return a valid write id list.    */
specifier|public
specifier|static
name|ValidCompactorWriteIdList
name|createValidCompactWriteIdList
parameter_list|(
name|TableValidWriteIds
name|tableValidWriteIds
parameter_list|)
block|{
name|String
name|fullTableName
init|=
name|tableValidWriteIds
operator|.
name|getFullTableName
argument_list|()
decl_stmt|;
name|long
name|highWater
init|=
name|tableValidWriteIds
operator|.
name|getWriteIdHighWaterMark
argument_list|()
decl_stmt|;
name|long
name|minOpenWriteId
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|List
argument_list|<
name|Long
argument_list|>
name|invalids
init|=
name|tableValidWriteIds
operator|.
name|getInvalidWriteIds
argument_list|()
decl_stmt|;
name|BitSet
name|abortedBits
init|=
name|BitSet
operator|.
name|valueOf
argument_list|(
name|tableValidWriteIds
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
name|invalids
operator|.
name|size
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
name|long
name|writeId
range|:
name|invalids
control|)
block|{
if|if
condition|(
name|abortedBits
operator|.
name|get
argument_list|(
name|i
argument_list|)
condition|)
block|{
comment|// Only need aborted since we don't consider anything above minOpenWriteId
name|exceptions
index|[
name|i
operator|++
index|]
operator|=
name|writeId
expr_stmt|;
block|}
else|else
block|{
name|minOpenWriteId
operator|=
name|Math
operator|.
name|min
argument_list|(
name|minOpenWriteId
argument_list|,
name|writeId
argument_list|)
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
name|minOpenWriteId
operator|==
name|Long
operator|.
name|MAX_VALUE
condition|?
name|highWater
else|:
name|minOpenWriteId
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
comment|// for ValidCompactorWriteIdList, everything in exceptions are aborted
if|if
condition|(
name|minOpenWriteId
operator|==
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
return|return
operator|new
name|ValidCompactorWriteIdList
argument_list|(
name|fullTableName
argument_list|,
name|exceptions
argument_list|,
name|bitSet
argument_list|,
name|highWater
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|ValidCompactorWriteIdList
argument_list|(
name|fullTableName
argument_list|,
name|exceptions
argument_list|,
name|bitSet
argument_list|,
name|highWater
argument_list|,
name|minOpenWriteId
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
name|ValidReaderWriteIdList
name|updateForCompactionQuery
parameter_list|(
name|ValidReaderWriteIdList
name|ids
parameter_list|)
block|{
comment|// This is based on the existing valid write ID list that was built for a select query;
comment|// therefore we assume all the aborted txns, etc. were already accounted for.
comment|// All we do is adjust the high watermark to only include contiguous txns.
name|Long
name|minOpenWriteId
init|=
name|ids
operator|.
name|getMinOpenWriteId
argument_list|()
decl_stmt|;
if|if
condition|(
name|minOpenWriteId
operator|!=
literal|null
operator|&&
name|minOpenWriteId
operator|!=
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
return|return
name|ids
operator|.
name|updateHighWatermark
argument_list|(
name|ids
operator|.
name|getMinOpenWriteId
argument_list|()
operator|-
literal|1
argument_list|)
return|;
block|}
return|return
name|ids
return|;
block|}
comment|/**    * Get an instance of the TxnStore that is appropriate for this store    * @param conf configuration    * @return txn store    */
specifier|public
specifier|static
name|TxnStore
name|getTxnStore
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|className
init|=
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|TXN_STORE_IMPL
argument_list|)
decl_stmt|;
try|try
block|{
name|TxnStore
name|handler
init|=
name|JavaUtils
operator|.
name|getClass
argument_list|(
name|className
argument_list|,
name|TxnStore
operator|.
name|class
argument_list|)
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
comment|/**    * Note, users are responsible for using the correct TxnManager. We do not look at    * SessionState.get().getTxnMgr().supportsAcid() here    * Should produce the same result as    * {@link org.apache.hadoop.hive.ql.io.AcidUtils#isTransactionalTable(org.apache.hadoop.hive.ql.metadata.Table)}.    * @return true if table is a transactional table, false otherwise    */
specifier|public
specifier|static
name|boolean
name|isTransactionalTable
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
if|if
condition|(
name|parameters
operator|==
literal|null
condition|)
return|return
literal|false
return|;
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
specifier|public
specifier|static
name|boolean
name|isTransactionalTable
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
parameter_list|)
block|{
if|if
condition|(
name|parameters
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
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
comment|/**    * Should produce the same result as    * {@link org.apache.hadoop.hive.ql.io.AcidUtils#isAcidTable(org.apache.hadoop.hive.ql.metadata.Table)}.    */
specifier|public
specifier|static
name|boolean
name|isAcidTable
parameter_list|(
name|Table
name|table
parameter_list|)
block|{
return|return
name|TxnUtils
operator|.
name|isTransactionalTable
argument_list|(
name|table
argument_list|)
operator|&&
name|TransactionalValidationListener
operator|.
name|DEFAULT_TRANSACTIONAL_PROPERTY
operator|.
name|equals
argument_list|(
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|hive_metastoreConstants
operator|.
name|TABLE_TRANSACTIONAL_PROPERTIES
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Should produce the result as<dbName>.<tableName>.    */
specifier|public
specifier|static
name|String
name|getFullTableName
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
return|return
name|dbName
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|"."
operator|+
name|tableName
operator|.
name|toLowerCase
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|String
index|[]
name|getDbTableName
parameter_list|(
name|String
name|fullTableName
parameter_list|)
block|{
return|return
name|fullTableName
operator|.
name|split
argument_list|(
literal|"\\."
argument_list|)
return|;
block|}
comment|/**    * Build a query (or queries if one query is too big but only for the case of 'IN'    * composite clause. For the case of 'NOT IN' clauses, multiple queries change    * the semantics of the intended query.    * E.g., Let's assume that input "inList" parameter has [5, 6] and that    * _DIRECT_SQL_MAX_QUERY_LENGTH_ configuration parameter only allows one value in a 'NOT IN' clause,    * Then having two delete statements changes the semantics of the inteneded SQL statement.    * I.e. 'delete from T where a not in (5)' and 'delete from T where a not in (6)' sequence    * is not equal to 'delete from T where a not in (5, 6)'.)    * with one or multiple 'IN' or 'NOT IN' clauses with the given input parameters.    *    * Note that this method currently support only single column for    * IN/NOT IN clauses and that only covers OR-based composite 'IN' clause and    * AND-based composite 'NOT IN' clause.    * For example, for 'IN' clause case, the method will build a query with OR.    * E.g., "id in (1,2,3) OR id in (4,5,6)".    * For 'NOT IN' case, NOT IN list is broken into multiple 'NOT IN" clauses connected by AND.    *    * Note that, in this method, "a composite 'IN' clause" is defined as "a list of multiple 'IN'    * clauses in a query".    *    * @param queries   OUT: Array of query strings    * @param prefix    IN:  Part of the query that comes before IN list    * @param suffix    IN:  Part of the query that comes after IN list    * @param inList    IN:  the list with IN list values    * @param inColumn  IN:  single column name of IN list operator    * @param addParens IN:  add a pair of parenthesis outside the IN lists    *                       e.g. "(id in (1,2,3) OR id in (4,5,6))"    * @param notIn     IN:  is this for building a 'NOT IN' composite clause?    * @return          OUT: a list of the count of IN list values that are in each of the corresponding queries    */
specifier|public
specifier|static
name|List
argument_list|<
name|Integer
argument_list|>
name|buildQueryWithINClause
parameter_list|(
name|Configuration
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
name|List
argument_list|<
name|String
argument_list|>
name|inListStrings
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|inList
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Long
name|aLong
range|:
name|inList
control|)
block|{
name|inListStrings
operator|.
name|add
argument_list|(
name|aLong
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|buildQueryWithINClauseStrings
argument_list|(
name|conf
argument_list|,
name|queries
argument_list|,
name|prefix
argument_list|,
name|suffix
argument_list|,
name|inListStrings
argument_list|,
name|inColumn
argument_list|,
name|addParens
argument_list|,
name|notIn
argument_list|)
return|;
block|}
comment|/**    * Build a query (or queries if one query is too big but only for the case of 'IN'    * composite clause. For the case of 'NOT IN' clauses, multiple queries change    * the semantics of the intended query.    * E.g., Let's assume that input "inList" parameter has [5, 6] and that    * _DIRECT_SQL_MAX_QUERY_LENGTH_ configuration parameter only allows one value in a 'NOT IN' clause,    * Then having two delete statements changes the semantics of the inteneded SQL statement.    * I.e. 'delete from T where a not in (5)' and 'delete from T where a not in (6)' sequence    * is not equal to 'delete from T where a not in (5, 6)'.)    * with one or multiple 'IN' or 'NOT IN' clauses with the given input parameters.    *    * Note that this method currently support only single column for    * IN/NOT IN clauses and that only covers OR-based composite 'IN' clause and    * AND-based composite 'NOT IN' clause.    * For example, for 'IN' clause case, the method will build a query with OR.    * E.g., "id in (1,2,3) OR id in (4,5,6)".    * For 'NOT IN' case, NOT IN list is broken into multiple 'NOT IN" clauses connected by AND.    *    * Note that, in this method, "a composite 'IN' clause" is defined as "a list of multiple 'IN'    * clauses in a query".    *    * @param queries   OUT: Array of query strings    * @param prefix    IN:  Part of the query that comes before IN list    * @param suffix    IN:  Part of the query that comes after IN list    * @param inList    IN:  the list with IN list values    * @param inColumn  IN:  single column name of IN list operator    * @param addParens IN:  add a pair of parenthesis outside the IN lists    *                       e.g. "(id in (1,2,3) OR id in (4,5,6))"    * @param notIn     IN:  is this for building a 'NOT IN' composite clause?    * @return          OUT: a list of the count of IN list values that are in each of the corresponding queries    */
specifier|public
specifier|static
name|List
argument_list|<
name|Integer
argument_list|>
name|buildQueryWithINClauseStrings
parameter_list|(
name|Configuration
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
name|String
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
comment|// Get configuration parameters
name|int
name|maxQueryLength
init|=
name|MetastoreConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|DIRECT_SQL_MAX_QUERY_LENGTH
argument_list|)
decl_stmt|;
name|int
name|batchSize
init|=
name|MetastoreConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|DIRECT_SQL_MAX_ELEMENTS_IN_CLAUSE
argument_list|)
decl_stmt|;
comment|// Check parameter set validity as a public method.
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
operator|||
name|maxQueryLength
operator|<=
literal|0
operator|||
name|batchSize
operator|<=
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
comment|// Define constants and local variables.
name|int
name|inListSize
init|=
name|inList
operator|.
name|size
argument_list|()
decl_stmt|;
name|StringBuilder
name|buf
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|int
name|cursor4InListArray
init|=
literal|0
decl_stmt|,
comment|// cursor for the "inList" array.
name|cursor4InClauseElements
init|=
literal|0
decl_stmt|,
comment|// cursor for an element list per an 'IN'/'NOT IN'-clause.
name|cursor4queryOfInClauses
init|=
literal|0
decl_stmt|;
comment|// cursor for in-clause lists per a query.
name|boolean
name|nextItemNeeded
init|=
literal|true
decl_stmt|;
name|boolean
name|newInclausePrefixJustAppended
init|=
literal|false
decl_stmt|;
name|StringBuilder
name|nextValue
init|=
operator|new
name|StringBuilder
argument_list|(
literal|""
argument_list|)
decl_stmt|;
name|StringBuilder
name|newInclausePrefix
init|=
operator|new
name|StringBuilder
argument_list|(
name|notIn
condition|?
literal|" and "
operator|+
name|inColumn
operator|+
literal|" not in ("
else|:
literal|" or "
operator|+
name|inColumn
operator|+
literal|" in ("
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|ret
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|currentCount
init|=
literal|0
decl_stmt|;
comment|// Loop over the given inList elements.
while|while
condition|(
name|cursor4InListArray
operator|<
name|inListSize
operator|||
operator|!
name|nextItemNeeded
condition|)
block|{
if|if
condition|(
name|cursor4queryOfInClauses
operator|==
literal|0
condition|)
block|{
comment|// Append prefix
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
name|cursor4queryOfInClauses
operator|++
expr_stmt|;
name|newInclausePrefixJustAppended
operator|=
literal|false
expr_stmt|;
block|}
comment|// Get the next "inList" value element if needed.
if|if
condition|(
name|nextItemNeeded
condition|)
block|{
name|nextValue
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|nextValue
operator|.
name|append
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|inList
operator|.
name|get
argument_list|(
name|cursor4InListArray
operator|++
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|nextItemNeeded
operator|=
literal|false
expr_stmt|;
block|}
comment|// Compute the size of a query when the 'nextValue' is added to the current query.
name|int
name|querySize
init|=
name|querySizeExpected
argument_list|(
name|buf
operator|.
name|length
argument_list|()
argument_list|,
name|nextValue
operator|.
name|length
argument_list|()
argument_list|,
name|suffix
operator|.
name|length
argument_list|()
argument_list|,
name|addParens
argument_list|)
decl_stmt|;
if|if
condition|(
name|querySize
operator|>
name|maxQueryLength
operator|*
literal|1024
condition|)
block|{
comment|// Check an edge case where the DIRECT_SQL_MAX_QUERY_LENGTH does not allow one 'IN' clause with single value.
if|if
condition|(
name|cursor4queryOfInClauses
operator|==
literal|1
operator|&&
name|cursor4InClauseElements
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"The current "
operator|+
name|ConfVars
operator|.
name|DIRECT_SQL_MAX_QUERY_LENGTH
operator|.
name|getVarname
argument_list|()
operator|+
literal|" is set too small to have one IN clause with single value!"
argument_list|)
throw|;
block|}
comment|// Check en edge case to throw Exception if we can not build a single query for 'NOT IN' clause cases as mentioned at the method comments.
if|if
condition|(
name|notIn
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"The NOT IN list has too many elements for the current "
operator|+
name|ConfVars
operator|.
name|DIRECT_SQL_MAX_QUERY_LENGTH
operator|.
name|getVarname
argument_list|()
operator|+
literal|"!"
argument_list|)
throw|;
block|}
comment|// Wrap up the current query string since we can not add another "inList" element value.
if|if
condition|(
name|newInclausePrefixJustAppended
condition|)
block|{
name|buf
operator|.
name|delete
argument_list|(
name|buf
operator|.
name|length
argument_list|()
operator|-
name|newInclausePrefix
operator|.
name|length
argument_list|()
argument_list|,
name|buf
operator|.
name|length
argument_list|()
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
comment|// replace the "commar" to finish a 'IN' clause string.
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
name|ret
operator|.
name|add
argument_list|(
name|currentCount
argument_list|)
expr_stmt|;
comment|// Prepare a new query string.
name|buf
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|currentCount
operator|=
literal|0
expr_stmt|;
name|cursor4queryOfInClauses
operator|=
name|cursor4InClauseElements
operator|=
literal|0
expr_stmt|;
name|querySize
operator|=
literal|0
expr_stmt|;
name|newInclausePrefixJustAppended
operator|=
literal|false
expr_stmt|;
continue|continue;
block|}
elseif|else
if|if
condition|(
name|cursor4InClauseElements
operator|>=
name|batchSize
operator|-
literal|1
operator|&&
name|cursor4InClauseElements
operator|!=
literal|0
condition|)
block|{
comment|// Finish the current 'IN'/'NOT IN' clause and start a new clause.
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
comment|// replace the "commar".
name|buf
operator|.
name|append
argument_list|(
name|newInclausePrefix
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|newInclausePrefixJustAppended
operator|=
literal|true
expr_stmt|;
comment|// increment cursor for per-query IN-clause list
name|cursor4queryOfInClauses
operator|++
expr_stmt|;
name|cursor4InClauseElements
operator|=
literal|0
expr_stmt|;
block|}
else|else
block|{
name|buf
operator|.
name|append
argument_list|(
name|nextValue
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|currentCount
operator|++
expr_stmt|;
name|nextItemNeeded
operator|=
literal|true
expr_stmt|;
name|newInclausePrefixJustAppended
operator|=
literal|false
expr_stmt|;
comment|// increment cursor for elements per 'IN'/'NOT IN' clause.
name|cursor4InClauseElements
operator|++
expr_stmt|;
block|}
block|}
comment|// Finish the last query.
if|if
condition|(
name|newInclausePrefixJustAppended
condition|)
block|{
name|buf
operator|.
name|delete
argument_list|(
name|buf
operator|.
name|length
argument_list|()
operator|-
name|newInclausePrefix
operator|.
name|length
argument_list|()
argument_list|,
name|buf
operator|.
name|length
argument_list|()
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
comment|// replace the commar.
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
name|ret
operator|.
name|add
argument_list|(
name|currentCount
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
comment|/**    * Compute and return the size of a query statement with the given parameters as input variables.    *    * @param sizeSoFar     size of the current contents of the buf    * @param sizeNextItem      size of the next 'IN' clause element value.    * @param suffixSize    size of the suffix for a quey statement    * @param addParens     Do we add an additional parenthesis?    */
specifier|private
specifier|static
name|int
name|querySizeExpected
parameter_list|(
name|int
name|sizeSoFar
parameter_list|,
name|int
name|sizeNextItem
parameter_list|,
name|int
name|suffixSize
parameter_list|,
name|boolean
name|addParens
parameter_list|)
block|{
name|int
name|size
init|=
name|sizeSoFar
operator|+
name|sizeNextItem
operator|+
name|suffixSize
decl_stmt|;
if|if
condition|(
name|addParens
condition|)
block|{
name|size
operator|++
expr_stmt|;
block|}
return|return
name|size
return|;
block|}
block|}
end_class

end_unit

