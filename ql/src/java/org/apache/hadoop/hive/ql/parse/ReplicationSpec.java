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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Predicate
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
name|repl
operator|.
name|ReplConst
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
name|metadata
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
name|ql
operator|.
name|plan
operator|.
name|PlanUtils
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|Collator
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

begin_comment
comment|/**  * Statements executed to handle replication have some additional  * information relevant to the replication subsystem - this class  * captures those bits of information.  *  * Typically, this corresponds to the replicationClause definition  * in the parser.  */
end_comment

begin_class
specifier|public
class|class
name|ReplicationSpec
block|{
specifier|private
name|boolean
name|isInReplicationScope
init|=
literal|false
decl_stmt|;
comment|// default is that it's not in a repl scope
specifier|private
name|boolean
name|isMetadataOnly
init|=
literal|false
decl_stmt|;
comment|// default is full export/import, not metadata-only
specifier|private
name|String
name|eventId
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|currStateId
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|isNoop
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|isReplace
init|=
literal|true
decl_stmt|;
comment|// default is that the import mode is insert overwrite
specifier|private
name|String
name|validWriteIdList
init|=
literal|null
decl_stmt|;
comment|// WriteIds snapshot for replicating ACID/MM tables.
comment|//TxnIds snapshot
specifier|private
name|String
name|validTxnList
init|=
literal|null
decl_stmt|;
specifier|private
name|Type
name|specType
init|=
name|Type
operator|.
name|DEFAULT
decl_stmt|;
comment|// DEFAULT means REPL_LOAD or BOOTSTRAP_DUMP or EXPORT
specifier|private
name|boolean
name|isMigratingToTxnTable
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|isMigratingToExternalTable
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|needDupCopyCheck
init|=
literal|false
decl_stmt|;
comment|//Determine if replication is done using repl or export-import
specifier|private
name|boolean
name|isRepl
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|isMetadataOnlyForExternalTables
init|=
literal|false
decl_stmt|;
comment|// Key definitions related to replication.
specifier|public
enum|enum
name|KEY
block|{
name|REPL_SCOPE
argument_list|(
literal|"repl.scope"
argument_list|)
block|,
name|EVENT_ID
argument_list|(
literal|"repl.event.id"
argument_list|)
block|,
name|CURR_STATE_ID
parameter_list|(
name|ReplConst
operator|.
name|REPL_TARGET_TABLE_PROPERTY
parameter_list|)
operator|,
constructor|NOOP("repl.noop"
block|)
enum|,
name|IS_REPLACE
argument_list|(
literal|"repl.is.replace"
argument_list|)
operator|,
name|VALID_WRITEID_LIST
argument_list|(
literal|"repl.valid.writeid.list"
argument_list|)
operator|,
name|VALID_TXN_LIST
argument_list|(
literal|"repl.valid.txnid.list"
argument_list|)
enum|;
specifier|private
specifier|final
name|String
name|keyName
decl_stmt|;
name|KEY
parameter_list|(
name|String
name|s
parameter_list|)
block|{
name|this
operator|.
name|keyName
operator|=
name|s
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
name|keyName
return|;
block|}
block|}
end_class

begin_enum
specifier|public
enum|enum
name|SCOPE
block|{
name|NO_REPL
block|,
name|MD_ONLY
block|,
name|REPL
block|}
end_enum

begin_enum
specifier|public
enum|enum
name|Type
block|{
name|DEFAULT
block|,
name|INCREMENTAL_DUMP
block|,
name|IMPORT
block|}
end_enum

begin_comment
comment|/**    * Constructor to construct spec based on either the ASTNode that    * corresponds to the replication clause itself, or corresponds to    * the parent node, and will scan through the children to instantiate    * itself.    * @param node replicationClause node, or parent of replicationClause node    */
end_comment

begin_constructor
specifier|public
name|ReplicationSpec
parameter_list|(
name|ASTNode
name|node
parameter_list|)
block|{
if|if
condition|(
name|node
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|isApplicable
argument_list|(
name|node
argument_list|)
condition|)
block|{
name|init
argument_list|(
name|node
argument_list|)
expr_stmt|;
return|return;
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|node
operator|.
name|getChildCount
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|ASTNode
name|child
init|=
operator|(
name|ASTNode
operator|)
name|node
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|isApplicable
argument_list|(
name|child
argument_list|)
condition|)
block|{
name|init
argument_list|(
name|child
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
block|}
block|}
comment|// If we reached here, we did not find a replication
comment|// spec in the node or its immediate children. Defaults
comment|// are to pretend replication is not happening, and the
comment|// statement above is running as-is.
block|}
end_constructor

begin_comment
comment|/**    * Default ctor that is useful for determining default states    */
end_comment

begin_constructor
specifier|public
name|ReplicationSpec
parameter_list|()
block|{
name|this
argument_list|(
operator|(
name|ASTNode
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
end_constructor

begin_constructor
specifier|public
name|ReplicationSpec
parameter_list|(
name|String
name|fromId
parameter_list|,
name|String
name|toId
parameter_list|)
block|{
name|this
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|,
name|fromId
argument_list|,
name|toId
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
end_constructor

begin_constructor
specifier|public
name|ReplicationSpec
parameter_list|(
name|boolean
name|isInReplicationScope
parameter_list|,
name|boolean
name|isMetadataOnly
parameter_list|,
name|String
name|eventReplicationState
parameter_list|,
name|String
name|currentReplicationState
parameter_list|,
name|boolean
name|isNoop
parameter_list|,
name|boolean
name|isReplace
parameter_list|)
block|{
name|this
operator|.
name|isInReplicationScope
operator|=
name|isInReplicationScope
expr_stmt|;
name|this
operator|.
name|isMetadataOnly
operator|=
name|isMetadataOnly
expr_stmt|;
name|this
operator|.
name|eventId
operator|=
name|eventReplicationState
expr_stmt|;
name|this
operator|.
name|currStateId
operator|=
name|currentReplicationState
expr_stmt|;
name|this
operator|.
name|isNoop
operator|=
name|isNoop
expr_stmt|;
name|this
operator|.
name|isReplace
operator|=
name|isReplace
expr_stmt|;
name|this
operator|.
name|specType
operator|=
name|Type
operator|.
name|DEFAULT
expr_stmt|;
block|}
end_constructor

begin_constructor
specifier|public
name|ReplicationSpec
parameter_list|(
name|Function
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|keyFetcher
parameter_list|)
block|{
name|String
name|scope
init|=
name|keyFetcher
operator|.
name|apply
argument_list|(
name|ReplicationSpec
operator|.
name|KEY
operator|.
name|REPL_SCOPE
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|isInReplicationScope
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|isMetadataOnly
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|specType
operator|=
name|Type
operator|.
name|DEFAULT
expr_stmt|;
if|if
condition|(
name|scope
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|scope
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"metadata"
argument_list|)
condition|)
block|{
name|this
operator|.
name|isMetadataOnly
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|isInReplicationScope
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|scope
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"all"
argument_list|)
condition|)
block|{
name|this
operator|.
name|isInReplicationScope
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|this
operator|.
name|eventId
operator|=
name|keyFetcher
operator|.
name|apply
argument_list|(
name|ReplicationSpec
operator|.
name|KEY
operator|.
name|EVENT_ID
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|currStateId
operator|=
name|keyFetcher
operator|.
name|apply
argument_list|(
name|ReplicationSpec
operator|.
name|KEY
operator|.
name|CURR_STATE_ID
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|isNoop
operator|=
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|keyFetcher
operator|.
name|apply
argument_list|(
name|ReplicationSpec
operator|.
name|KEY
operator|.
name|NOOP
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|isReplace
operator|=
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|keyFetcher
operator|.
name|apply
argument_list|(
name|ReplicationSpec
operator|.
name|KEY
operator|.
name|IS_REPLACE
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|validWriteIdList
operator|=
name|keyFetcher
operator|.
name|apply
argument_list|(
name|ReplicationSpec
operator|.
name|KEY
operator|.
name|VALID_WRITEID_LIST
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|validTxnList
operator|=
name|keyFetcher
operator|.
name|apply
argument_list|(
name|KEY
operator|.
name|VALID_TXN_LIST
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
end_constructor

begin_comment
comment|/**    * Tests if an ASTNode is a Replication Specification    */
end_comment

begin_function
specifier|public
specifier|static
name|boolean
name|isApplicable
parameter_list|(
name|ASTNode
name|node
parameter_list|)
block|{
return|return
operator|(
name|node
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_REPLICATION
operator|)
return|;
block|}
end_function

begin_comment
comment|/**    * @param currReplState Current object state    * @param replacementReplState Replacement-candidate state    * @return whether or not a provided replacement candidate is newer(or equal) to the existing object state or not    */
end_comment

begin_function
specifier|public
name|boolean
name|allowReplacement
parameter_list|(
name|String
name|currReplState
parameter_list|,
name|String
name|replacementReplState
parameter_list|)
block|{
if|if
condition|(
operator|(
name|currReplState
operator|==
literal|null
operator|)
operator|||
operator|(
name|currReplState
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
comment|// if we have no replication state on record for the obj, allow replacement.
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|(
name|replacementReplState
operator|==
literal|null
operator|)
operator|||
operator|(
name|replacementReplState
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
comment|// if we reached this condition, we had replication state on record for the
comment|// object, but its replacement has no state. Disallow replacement
return|return
literal|false
return|;
block|}
comment|// First try to extract a long value from the strings, and compare them.
comment|// If oldReplState is less-than newReplState, allow.
name|long
name|currReplStateLong
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|currReplState
operator|.
name|replaceAll
argument_list|(
literal|"\\D"
argument_list|,
literal|""
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|replacementReplStateLong
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|replacementReplState
operator|.
name|replaceAll
argument_list|(
literal|"\\D"
argument_list|,
literal|""
argument_list|)
argument_list|)
decl_stmt|;
comment|// Failure handling of IMPORT command and REPL LOAD commands are different.
comment|// IMPORT will set the last repl ID before copying data files and hence need to allow
comment|// replacement if loaded from same dump twice after failing to copy in previous attempt.
comment|// But, REPL LOAD will set the last repl ID only after the successful copy of data files and
comment|// hence need not allow if same event is applied twice.
if|if
condition|(
name|specType
operator|==
name|Type
operator|.
name|IMPORT
condition|)
block|{
return|return
operator|(
name|currReplStateLong
operator|<=
name|replacementReplStateLong
operator|)
return|;
block|}
else|else
block|{
return|return
operator|(
name|currReplStateLong
operator|<
name|replacementReplStateLong
operator|)
return|;
block|}
block|}
end_function

begin_comment
comment|/**    * Determines if a current replication object (current state of dump) is allowed to    * replicate-replace-into a given metastore object (based on state_id stored in their parameters)    */
end_comment

begin_function
specifier|public
name|boolean
name|allowReplacementInto
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
block|{
return|return
name|allowReplacement
argument_list|(
name|getLastReplicatedStateFromParameters
argument_list|(
name|params
argument_list|)
argument_list|,
name|getCurrentReplicationState
argument_list|()
argument_list|)
return|;
block|}
end_function

begin_comment
comment|/**    * Determines if a current replication event (based on event id) is allowed to    * replicate-replace-into a given metastore object (based on state_id stored in their parameters)    */
end_comment

begin_function
specifier|public
name|boolean
name|allowEventReplacementInto
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
block|{
return|return
name|allowReplacement
argument_list|(
name|getLastReplicatedStateFromParameters
argument_list|(
name|params
argument_list|)
argument_list|,
name|getReplicationState
argument_list|()
argument_list|)
return|;
block|}
end_function

begin_comment
comment|/**    * Returns a predicate filter to filter an Iterable&lt;Partition&gt; to return all partitions    * that the current replication event specification is allowed to replicate-replace-into    */
end_comment

begin_function
specifier|public
name|Predicate
argument_list|<
name|Partition
argument_list|>
name|allowEventReplacementInto
parameter_list|()
block|{
return|return
operator|new
name|Predicate
argument_list|<
name|Partition
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
annotation|@
name|Nullable
name|Partition
name|partition
parameter_list|)
block|{
if|if
condition|(
name|partition
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
operator|(
name|allowEventReplacementInto
argument_list|(
name|partition
operator|.
name|getParameters
argument_list|()
argument_list|)
operator|)
return|;
block|}
block|}
return|;
block|}
end_function

begin_function
specifier|private
name|void
name|init
parameter_list|(
name|ASTNode
name|node
parameter_list|)
block|{
comment|// -> ^(TOK_REPLICATION $replId $isMetadataOnly)
name|isInReplicationScope
operator|=
literal|true
expr_stmt|;
name|eventId
operator|=
name|PlanUtils
operator|.
name|stripQuotes
argument_list|(
name|node
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|node
operator|.
name|getChildCount
argument_list|()
operator|>
literal|1
operator|)
operator|&&
name|node
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|.
name|getText
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
literal|"metadata"
argument_list|)
condition|)
block|{
name|isMetadataOnly
operator|=
literal|true
expr_stmt|;
try|try
block|{
if|if
condition|(
name|Long
operator|.
name|parseLong
argument_list|(
name|eventId
argument_list|)
operator|>=
literal|0
condition|)
block|{
comment|// If metadata-only dump, then the state of the dump shouldn't be the latest event id as
comment|// the data is not yet dumped and shall be dumped in future export.
name|currStateId
operator|=
name|eventId
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
comment|// Ignore the exception and fall through the default currentStateId
block|}
block|}
block|}
end_function

begin_function
specifier|public
specifier|static
name|String
name|getLastReplicatedStateFromParameters
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|m
parameter_list|)
block|{
if|if
condition|(
operator|(
name|m
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|m
operator|.
name|containsKey
argument_list|(
name|KEY
operator|.
name|CURR_STATE_ID
operator|.
name|toString
argument_list|()
argument_list|)
operator|)
condition|)
block|{
return|return
name|m
operator|.
name|get
argument_list|(
name|KEY
operator|.
name|CURR_STATE_ID
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
end_function

begin_comment
comment|/**    * @return true if this statement refers to incremental dump operation.    */
end_comment

begin_function
specifier|public
name|Type
name|getReplSpecType
parameter_list|()
block|{
return|return
name|this
operator|.
name|specType
return|;
block|}
end_function

begin_function
specifier|public
name|void
name|setReplSpecType
parameter_list|(
name|Type
name|specType
parameter_list|)
block|{
name|this
operator|.
name|specType
operator|=
name|specType
expr_stmt|;
block|}
end_function

begin_comment
comment|/**    * @return true if this statement is being run for the purposes of replication    */
end_comment

begin_function
specifier|public
name|boolean
name|isInReplicationScope
parameter_list|()
block|{
return|return
name|isInReplicationScope
return|;
block|}
end_function

begin_comment
comment|/**    * @return true if this statement refers to metadata-only operation.    */
end_comment

begin_function
specifier|public
name|boolean
name|isMetadataOnly
parameter_list|()
block|{
return|return
name|isMetadataOnly
return|;
block|}
end_function

begin_function
specifier|public
name|void
name|setIsMetadataOnly
parameter_list|(
name|boolean
name|isMetadataOnly
parameter_list|)
block|{
name|this
operator|.
name|isMetadataOnly
operator|=
name|isMetadataOnly
expr_stmt|;
block|}
end_function

begin_comment
comment|/**    * @return true if this statement refers to metadata-only operation.    */
end_comment

begin_function
specifier|public
name|boolean
name|isMetadataOnlyForExternalTables
parameter_list|()
block|{
return|return
name|isMetadataOnlyForExternalTables
return|;
block|}
end_function

begin_function
specifier|public
name|void
name|setMetadataOnlyForExternalTables
parameter_list|(
name|boolean
name|metadataOnlyForExternalTables
parameter_list|)
block|{
name|isMetadataOnlyForExternalTables
operator|=
name|metadataOnlyForExternalTables
expr_stmt|;
block|}
end_function

begin_comment
comment|/**    * @return true if this statement refers to insert-into or insert-overwrite operation.    */
end_comment

begin_function
specifier|public
name|boolean
name|isReplace
parameter_list|()
block|{
return|return
name|isReplace
return|;
block|}
end_function

begin_function
specifier|public
name|void
name|setIsReplace
parameter_list|(
name|boolean
name|isReplace
parameter_list|)
block|{
name|this
operator|.
name|isReplace
operator|=
name|isReplace
expr_stmt|;
block|}
end_function

begin_comment
comment|/**    * @return the replication state of the event that spawned this statement    */
end_comment

begin_function
specifier|public
name|String
name|getReplicationState
parameter_list|()
block|{
return|return
name|eventId
return|;
block|}
end_function

begin_comment
comment|/**    * @return the current replication state of the wh    */
end_comment

begin_function
specifier|public
name|String
name|getCurrentReplicationState
parameter_list|()
block|{
return|return
name|currStateId
return|;
block|}
end_function

begin_function
specifier|public
name|void
name|setCurrentReplicationState
parameter_list|(
name|String
name|currStateId
parameter_list|)
block|{
name|this
operator|.
name|currStateId
operator|=
name|currStateId
expr_stmt|;
block|}
end_function

begin_comment
comment|/**    * @return whether or not the current replication action should be a noop    */
end_comment

begin_function
specifier|public
name|boolean
name|isNoop
parameter_list|()
block|{
return|return
name|isNoop
return|;
block|}
end_function

begin_comment
comment|/**    * @param isNoop whether or not the current replication action should be a noop    */
end_comment

begin_function
specifier|public
name|void
name|setNoop
parameter_list|(
name|boolean
name|isNoop
parameter_list|)
block|{
name|this
operator|.
name|isNoop
operator|=
name|isNoop
expr_stmt|;
block|}
end_function

begin_comment
comment|/**    * @return the WriteIds snapshot for the current ACID/MM table being replicated    */
end_comment

begin_function
specifier|public
name|String
name|getValidWriteIdList
parameter_list|()
block|{
return|return
name|validWriteIdList
return|;
block|}
end_function

begin_comment
comment|/**    * @param validWriteIdList WriteIds snapshot for the current ACID/MM table being replicated    */
end_comment

begin_function
specifier|public
name|void
name|setValidWriteIdList
parameter_list|(
name|String
name|validWriteIdList
parameter_list|)
block|{
name|this
operator|.
name|validWriteIdList
operator|=
name|validWriteIdList
expr_stmt|;
block|}
end_function

begin_function
specifier|public
name|String
name|getValidTxnList
parameter_list|()
block|{
return|return
name|validTxnList
return|;
block|}
end_function

begin_function
specifier|public
name|void
name|setValidTxnList
parameter_list|(
name|String
name|validTxnList
parameter_list|)
block|{
name|this
operator|.
name|validTxnList
operator|=
name|validTxnList
expr_stmt|;
block|}
end_function

begin_comment
comment|/**    * @return whether the current replication dumped object related to ACID/Mm table    */
end_comment

begin_function
specifier|public
name|boolean
name|isTransactionalTableDump
parameter_list|()
block|{
return|return
operator|(
name|validWriteIdList
operator|!=
literal|null
operator|)
return|;
block|}
end_function

begin_function
specifier|public
name|String
name|get
parameter_list|(
name|KEY
name|key
parameter_list|)
block|{
switch|switch
condition|(
name|key
condition|)
block|{
case|case
name|REPL_SCOPE
case|:
switch|switch
condition|(
name|getScope
argument_list|()
condition|)
block|{
case|case
name|MD_ONLY
case|:
return|return
literal|"metadata"
return|;
case|case
name|REPL
case|:
return|return
literal|"all"
return|;
case|case
name|NO_REPL
case|:
return|return
literal|"none"
return|;
block|}
case|case
name|EVENT_ID
case|:
return|return
name|getReplicationState
argument_list|()
return|;
case|case
name|CURR_STATE_ID
case|:
return|return
name|getCurrentReplicationState
argument_list|()
return|;
case|case
name|NOOP
case|:
return|return
name|String
operator|.
name|valueOf
argument_list|(
name|isNoop
argument_list|()
argument_list|)
return|;
case|case
name|IS_REPLACE
case|:
return|return
name|String
operator|.
name|valueOf
argument_list|(
name|isReplace
argument_list|()
argument_list|)
return|;
case|case
name|VALID_WRITEID_LIST
case|:
return|return
name|getValidWriteIdList
argument_list|()
return|;
case|case
name|VALID_TXN_LIST
case|:
return|return
name|getValidTxnList
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
end_function

begin_function
specifier|public
name|SCOPE
name|getScope
parameter_list|()
block|{
if|if
condition|(
name|isInReplicationScope
argument_list|()
condition|)
block|{
if|if
condition|(
name|isMetadataOnly
argument_list|()
condition|)
block|{
return|return
name|SCOPE
operator|.
name|MD_ONLY
return|;
block|}
else|else
block|{
return|return
name|SCOPE
operator|.
name|REPL
return|;
block|}
block|}
else|else
block|{
return|return
name|SCOPE
operator|.
name|NO_REPL
return|;
block|}
block|}
end_function

begin_function
specifier|public
name|boolean
name|isMigratingToTxnTable
parameter_list|()
block|{
return|return
name|isMigratingToTxnTable
return|;
block|}
end_function

begin_function
specifier|public
name|void
name|setMigratingToTxnTable
parameter_list|()
block|{
name|isMigratingToTxnTable
operator|=
literal|true
expr_stmt|;
block|}
end_function

begin_function
specifier|public
name|boolean
name|isMigratingToExternalTable
parameter_list|()
block|{
return|return
name|isMigratingToExternalTable
return|;
block|}
end_function

begin_function
specifier|public
name|void
name|setMigratingToExternalTable
parameter_list|()
block|{
name|isMigratingToExternalTable
operator|=
literal|true
expr_stmt|;
block|}
end_function

begin_function
specifier|public
specifier|static
name|void
name|copyLastReplId
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|srcParameter
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|destParameter
parameter_list|)
block|{
name|String
name|lastReplId
init|=
name|srcParameter
operator|.
name|get
argument_list|(
name|ReplicationSpec
operator|.
name|KEY
operator|.
name|CURR_STATE_ID
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|lastReplId
operator|!=
literal|null
condition|)
block|{
name|destParameter
operator|.
name|put
argument_list|(
name|ReplicationSpec
operator|.
name|KEY
operator|.
name|CURR_STATE_ID
operator|.
name|toString
argument_list|()
argument_list|,
name|lastReplId
argument_list|)
expr_stmt|;
block|}
block|}
end_function

begin_function
specifier|public
name|boolean
name|needDupCopyCheck
parameter_list|()
block|{
return|return
name|needDupCopyCheck
return|;
block|}
end_function

begin_function
specifier|public
name|void
name|setNeedDupCopyCheck
parameter_list|(
name|boolean
name|isFirstIncPending
parameter_list|)
block|{
comment|// Duplicate file check during copy is required until after first successful incremental load.
comment|// Check HIVE-21197 for more detail.
name|this
operator|.
name|needDupCopyCheck
operator|=
name|isFirstIncPending
expr_stmt|;
block|}
end_function

begin_function
specifier|public
name|boolean
name|isRepl
parameter_list|()
block|{
return|return
name|isRepl
return|;
block|}
end_function

begin_function
specifier|public
name|void
name|setRepl
parameter_list|(
name|boolean
name|repl
parameter_list|)
block|{
name|isRepl
operator|=
name|repl
expr_stmt|;
block|}
end_function

unit|}
end_unit

