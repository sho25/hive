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
name|HashMap
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
name|isLazy
init|=
literal|false
decl_stmt|;
comment|// lazy mode => we only list files, and expect that the eventual copy will pull data in.
specifier|private
name|boolean
name|isReplace
init|=
literal|true
decl_stmt|;
comment|// default is that the import mode is insert overwrite
comment|// Key definitions related to replication
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
argument_list|(
literal|"repl.last.id"
argument_list|)
block|,
name|NOOP
argument_list|(
literal|"repl.noop"
argument_list|)
block|,
name|LAZY
argument_list|(
literal|"repl.lazy"
argument_list|)
block|,
name|IS_REPLACE
argument_list|(
literal|"repl.is.replace"
argument_list|)
block|;
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
empty_stmt|;
specifier|static
specifier|private
name|Collator
name|collator
init|=
name|Collator
operator|.
name|getInstance
argument_list|()
decl_stmt|;
comment|/**    * Class that extends HashMap with a slightly different put semantic, where    * put behaves as follows:    *  a) If the key does not already exist, then retains existing HashMap.put behaviour    *  b) If the map already contains an entry for the given key, then will replace only    *     if the new value is "greater" than the old value.    *    * The primary goal for this is to track repl updates for dbs and tables, to replace state    * only if the state is newer.    */
specifier|public
specifier|static
class|class
name|ReplStateMap
parameter_list|<
name|K
parameter_list|,
name|V
extends|extends
name|Comparable
parameter_list|>
extends|extends
name|HashMap
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|V
name|put
parameter_list|(
name|K
name|k
parameter_list|,
name|V
name|v
parameter_list|)
block|{
if|if
condition|(
operator|!
name|containsKey
argument_list|(
name|k
argument_list|)
condition|)
block|{
return|return
name|super
operator|.
name|put
argument_list|(
name|k
argument_list|,
name|v
argument_list|)
return|;
block|}
name|V
name|oldValue
init|=
name|get
argument_list|(
name|k
argument_list|)
decl_stmt|;
if|if
condition|(
name|v
operator|.
name|compareTo
argument_list|(
name|oldValue
argument_list|)
operator|>
literal|0
condition|)
block|{
return|return
name|super
operator|.
name|put
argument_list|(
name|k
argument_list|,
name|v
argument_list|)
return|;
block|}
comment|// we did no replacement, but return the old value anyway. This
comment|// seems most consistent with HashMap behaviour, becuse the "put"
comment|// was effectively processed and consumed, although we threw away
comment|// the enw value.
return|return
name|oldValue
return|;
block|}
block|}
comment|/**    * Constructor to construct spec based on either the ASTNode that    * corresponds to the replication clause itself, or corresponds to    * the parent node, and will scan through the children to instantiate    * itself.    * @param node replicationClause node, or parent of replicationClause node    */
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
comment|/**    * Default ctor that is useful for determining default states    */
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
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
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
name|isLazy
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
name|isLazy
operator|=
name|isLazy
expr_stmt|;
name|this
operator|.
name|isReplace
operator|=
name|isReplace
expr_stmt|;
block|}
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
name|isMetadataOnly
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|isInReplicationScope
operator|=
literal|false
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
name|isLazy
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
name|LAZY
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
block|}
comment|/**    * Tests if an ASTNode is a Replication Specification    */
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
comment|/**    * @param currReplState Current object state    * @param replacementReplState Replacement-candidate state    * @return whether or not a provided replacement candidate is newer(or equal) to the existing object state or not    */
specifier|public
specifier|static
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
return|return
operator|(
operator|(
name|currReplStateLong
operator|-
name|replacementReplStateLong
operator|)
operator|<
literal|0
operator|)
return|;
block|}
comment|/**    * Determines if a current replication object (current state of dump) is allowed to    * replicate-replace-into a given metastore object (based on state_id stored in their parameters)    */
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
comment|/**    * Determines if a current replication event (based on event id) is allowed to    * replicate-replace-into a given metastore object (based on state_id stored in their parameters)    */
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
comment|/**    * Returns a predicate filter to filter an Iterable<Partition> to return all partitions    * that the current replication event specification is allowed to replicate-replace-into    */
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
specifier|private
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
name|node
operator|.
name|getChildCount
argument_list|()
operator|>
literal|1
condition|)
block|{
if|if
condition|(
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
block|}
block|}
block|}
comment|/**    * @return true if this statement is being run for the purposes of replication    */
specifier|public
name|boolean
name|isInReplicationScope
parameter_list|()
block|{
return|return
name|isInReplicationScope
return|;
block|}
comment|/**    * @return true if this statement refers to metadata-only operation.    */
specifier|public
name|boolean
name|isMetadataOnly
parameter_list|()
block|{
return|return
name|isMetadataOnly
return|;
block|}
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
comment|/**    * @return true if this statement refers to insert-into or insert-overwrite operation.    */
specifier|public
name|boolean
name|isReplace
parameter_list|()
block|{
return|return
name|isReplace
return|;
block|}
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
comment|/**    * @return the replication state of the event that spawned this statement    */
specifier|public
name|String
name|getReplicationState
parameter_list|()
block|{
return|return
name|eventId
return|;
block|}
comment|/**    * @return the current replication state of the wh    */
specifier|public
name|String
name|getCurrentReplicationState
parameter_list|()
block|{
return|return
name|currStateId
return|;
block|}
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
comment|/**    * @return whether or not the current replication action should be a noop    */
specifier|public
name|boolean
name|isNoop
parameter_list|()
block|{
return|return
name|isNoop
return|;
block|}
comment|/**    * @param isNoop whether or not the current replication action should be a noop    */
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
comment|/**    * @return whether or not the current replication action is set to be lazy    */
specifier|public
name|boolean
name|isLazy
parameter_list|()
block|{
return|return
name|isLazy
return|;
block|}
comment|/**    * @param isLazy whether or not the current replication action should be lazy    */
specifier|public
name|void
name|setLazy
parameter_list|(
name|boolean
name|isLazy
parameter_list|)
block|{
name|this
operator|.
name|isLazy
operator|=
name|isLazy
expr_stmt|;
block|}
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
name|LAZY
case|:
return|return
name|String
operator|.
name|valueOf
argument_list|(
name|isLazy
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
block|}
return|return
literal|null
return|;
block|}
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
block|}
end_class

end_unit

