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
name|Set
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
name|long
name|highWater
init|=
name|txns
operator|.
name|getTxn_high_water_mark
argument_list|()
decl_stmt|;
name|Set
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
return|return
operator|new
name|ValidReadTxnList
argument_list|(
name|exceptions
argument_list|,
name|highWater
argument_list|)
return|;
block|}
comment|/**    * Transform a {@link org.apache.hadoop.hive.metastore.api.GetOpenTxnsInfoResponse} to a    * {@link org.apache.hadoop.hive.common.ValidTxnList}.  This assumes that the caller intends to    * compact the files, and thus treats only open transactions as invalid.  Additionally any    * txnId> highestOpenTxnId is also invalid.  This is avoid creating something like    * delta_17_120 where txnId 80, for example, is still open.    * @param txns txn list from the metastore    * @return a valid txn list.    */
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
return|return
operator|new
name|ValidCompactorTxnList
argument_list|(
name|exceptions
argument_list|,
operator|-
literal|1
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
block|}
end_class

end_unit

