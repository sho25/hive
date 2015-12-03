begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|streaming
operator|.
name|mutate
operator|.
name|worker
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|ql
operator|.
name|io
operator|.
name|AcidOutputFormat
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
name|RecordUpdater
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
name|orc
operator|.
name|OrcRecordUpdater
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
import|;
end_import

begin_comment
comment|/** Base {@link Mutator} implementation. Creates a suitable {@link RecordUpdater} and delegates mutation events. */
end_comment

begin_class
specifier|public
class|class
name|MutatorImpl
implements|implements
name|Mutator
block|{
specifier|private
specifier|final
name|long
name|transactionId
decl_stmt|;
specifier|private
specifier|final
name|Path
name|partitionPath
decl_stmt|;
specifier|private
specifier|final
name|int
name|bucketId
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|configuration
decl_stmt|;
specifier|private
specifier|final
name|int
name|recordIdColumn
decl_stmt|;
specifier|private
specifier|final
name|ObjectInspector
name|objectInspector
decl_stmt|;
specifier|private
name|RecordUpdater
name|updater
decl_stmt|;
specifier|public
name|MutatorImpl
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|int
name|recordIdColumn
parameter_list|,
name|ObjectInspector
name|objectInspector
parameter_list|,
name|AcidOutputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|outputFormat
parameter_list|,
name|long
name|transactionId
parameter_list|,
name|Path
name|partitionPath
parameter_list|,
name|int
name|bucketId
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|configuration
operator|=
name|configuration
expr_stmt|;
name|this
operator|.
name|recordIdColumn
operator|=
name|recordIdColumn
expr_stmt|;
name|this
operator|.
name|objectInspector
operator|=
name|objectInspector
expr_stmt|;
name|this
operator|.
name|transactionId
operator|=
name|transactionId
expr_stmt|;
name|this
operator|.
name|partitionPath
operator|=
name|partitionPath
expr_stmt|;
name|this
operator|.
name|bucketId
operator|=
name|bucketId
expr_stmt|;
name|updater
operator|=
name|createRecordUpdater
argument_list|(
name|outputFormat
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|insert
parameter_list|(
name|Object
name|record
parameter_list|)
throws|throws
name|IOException
block|{
name|updater
operator|.
name|insert
argument_list|(
name|transactionId
argument_list|,
name|record
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|update
parameter_list|(
name|Object
name|record
parameter_list|)
throws|throws
name|IOException
block|{
name|updater
operator|.
name|update
argument_list|(
name|transactionId
argument_list|,
name|record
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|delete
parameter_list|(
name|Object
name|record
parameter_list|)
throws|throws
name|IOException
block|{
name|updater
operator|.
name|delete
argument_list|(
name|transactionId
argument_list|,
name|record
argument_list|)
expr_stmt|;
block|}
comment|/**    * This implementation does intentionally nothing at this time. We only use a single transaction and    * {@link OrcRecordUpdater#flush()} will purposefully throw and exception in this instance. We keep this here in the    * event that we support multiple transactions and to make it clear that the omission of an invocation of    * {@link OrcRecordUpdater#flush()} was not a mistake.    */
annotation|@
name|Override
specifier|public
name|void
name|flush
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Intentionally do nothing
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|updater
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|updater
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"ObjectInspectorMutator [transactionId="
operator|+
name|transactionId
operator|+
literal|", partitionPath="
operator|+
name|partitionPath
operator|+
literal|", bucketId="
operator|+
name|bucketId
operator|+
literal|"]"
return|;
block|}
specifier|protected
name|RecordUpdater
name|createRecordUpdater
parameter_list|(
name|AcidOutputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|outputFormat
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|outputFormat
operator|.
name|getRecordUpdater
argument_list|(
name|partitionPath
argument_list|,
operator|new
name|AcidOutputFormat
operator|.
name|Options
argument_list|(
name|configuration
argument_list|)
operator|.
name|inspector
argument_list|(
name|objectInspector
argument_list|)
operator|.
name|bucket
argument_list|(
name|bucketId
argument_list|)
operator|.
name|minimumTransactionId
argument_list|(
name|transactionId
argument_list|)
operator|.
name|maximumTransactionId
argument_list|(
name|transactionId
argument_list|)
operator|.
name|recordIdColumn
argument_list|(
name|recordIdColumn
argument_list|)
operator|.
name|finalDestination
argument_list|(
name|partitionPath
argument_list|)
operator|.
name|statementId
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

