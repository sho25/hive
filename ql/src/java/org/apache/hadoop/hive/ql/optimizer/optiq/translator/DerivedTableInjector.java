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
name|optimizer
operator|.
name|optiq
operator|.
name|translator
package|;
end_package

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
name|List
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
name|FieldSchema
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
name|optimizer
operator|.
name|optiq
operator|.
name|HiveOptiqUtil
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
name|optimizer
operator|.
name|optiq
operator|.
name|OptiqSemanticException
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
name|optimizer
operator|.
name|optiq
operator|.
name|reloperators
operator|.
name|HiveAggregateRel
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
name|optimizer
operator|.
name|optiq
operator|.
name|reloperators
operator|.
name|HiveProjectRel
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
name|optimizer
operator|.
name|optiq
operator|.
name|reloperators
operator|.
name|HiveSortRel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|AggregateRelBase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|EmptyRel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|FilterRelBase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|JoinRelBase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|OneRowRelBase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|ProjectRelBase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|RelNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|SetOpRel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|SingleRel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|SortRel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|TableAccessRelBase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|TableFunctionRelBase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|ValuesRelBase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|rules
operator|.
name|MultiJoinRel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|relopt
operator|.
name|hep
operator|.
name|HepRelVertex
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|relopt
operator|.
name|volcano
operator|.
name|RelSubset
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rex
operator|.
name|RexNode
import|;
end_import

begin_class
specifier|public
class|class
name|DerivedTableInjector
block|{
specifier|public
specifier|static
name|RelNode
name|convertOpTree
parameter_list|(
name|RelNode
name|rel
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|resultSchema
parameter_list|)
throws|throws
name|OptiqSemanticException
block|{
name|RelNode
name|newTopNode
init|=
name|rel
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|newTopNode
operator|instanceof
name|ProjectRelBase
operator|)
operator|&&
operator|!
operator|(
name|newTopNode
operator|instanceof
name|SortRel
operator|)
condition|)
block|{
name|newTopNode
operator|=
name|introduceDerivedTable
argument_list|(
name|newTopNode
argument_list|)
expr_stmt|;
block|}
name|convertOpTree
argument_list|(
name|newTopNode
argument_list|,
operator|(
name|RelNode
operator|)
literal|null
argument_list|)
expr_stmt|;
name|newTopNode
operator|=
name|renameTopLevelSelectInResultSchema
argument_list|(
name|newTopNode
argument_list|,
name|resultSchema
argument_list|)
expr_stmt|;
return|return
name|newTopNode
return|;
block|}
specifier|private
specifier|static
name|void
name|convertOpTree
parameter_list|(
name|RelNode
name|rel
parameter_list|,
name|RelNode
name|parent
parameter_list|)
block|{
if|if
condition|(
name|rel
operator|instanceof
name|EmptyRel
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Found Empty Rel"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|rel
operator|instanceof
name|HepRelVertex
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Found HepRelVertex"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|rel
operator|instanceof
name|JoinRelBase
condition|)
block|{
if|if
condition|(
operator|!
name|validJoinParent
argument_list|(
name|rel
argument_list|,
name|parent
argument_list|)
condition|)
block|{
name|introduceDerivedTable
argument_list|(
name|rel
argument_list|,
name|parent
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|rel
operator|instanceof
name|MultiJoinRel
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Found MultiJoinRel"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|rel
operator|instanceof
name|OneRowRelBase
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Found OneRowRelBase"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|rel
operator|instanceof
name|RelSubset
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Found RelSubset"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|rel
operator|instanceof
name|SetOpRel
condition|)
block|{
comment|// TODO: Handle more than 2 inputs for setop
if|if
condition|(
operator|!
name|validSetopParent
argument_list|(
name|rel
argument_list|,
name|parent
argument_list|)
condition|)
name|introduceDerivedTable
argument_list|(
name|rel
argument_list|,
name|parent
argument_list|)
expr_stmt|;
name|SetOpRel
name|setopRel
init|=
operator|(
name|SetOpRel
operator|)
name|rel
decl_stmt|;
for|for
control|(
name|RelNode
name|inputRel
range|:
name|setopRel
operator|.
name|getInputs
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|validSetopChild
argument_list|(
name|inputRel
argument_list|)
condition|)
block|{
name|introduceDerivedTable
argument_list|(
name|inputRel
argument_list|,
name|setopRel
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|rel
operator|instanceof
name|SingleRel
condition|)
block|{
if|if
condition|(
name|rel
operator|instanceof
name|FilterRelBase
condition|)
block|{
if|if
condition|(
operator|!
name|validFilterParent
argument_list|(
name|rel
argument_list|,
name|parent
argument_list|)
condition|)
block|{
name|introduceDerivedTable
argument_list|(
name|rel
argument_list|,
name|parent
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|rel
operator|instanceof
name|HiveSortRel
condition|)
block|{
if|if
condition|(
operator|!
name|validSortParent
argument_list|(
name|rel
argument_list|,
name|parent
argument_list|)
condition|)
block|{
name|introduceDerivedTable
argument_list|(
name|rel
argument_list|,
name|parent
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|validSortChild
argument_list|(
operator|(
name|HiveSortRel
operator|)
name|rel
argument_list|)
condition|)
block|{
name|introduceDerivedTable
argument_list|(
operator|(
operator|(
name|HiveSortRel
operator|)
name|rel
operator|)
operator|.
name|getChild
argument_list|()
argument_list|,
name|rel
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|rel
operator|instanceof
name|HiveAggregateRel
condition|)
block|{
if|if
condition|(
operator|!
name|validGBParent
argument_list|(
name|rel
argument_list|,
name|parent
argument_list|)
condition|)
block|{
name|introduceDerivedTable
argument_list|(
name|rel
argument_list|,
name|parent
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|List
argument_list|<
name|RelNode
argument_list|>
name|childNodes
init|=
name|rel
operator|.
name|getInputs
argument_list|()
decl_stmt|;
if|if
condition|(
name|childNodes
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|RelNode
name|r
range|:
name|childNodes
control|)
block|{
name|convertOpTree
argument_list|(
name|r
argument_list|,
name|rel
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
name|RelNode
name|renameTopLevelSelectInResultSchema
parameter_list|(
specifier|final
name|RelNode
name|rootRel
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|resultSchema
parameter_list|)
throws|throws
name|OptiqSemanticException
block|{
name|RelNode
name|tmpRel
init|=
name|rootRel
decl_stmt|;
name|RelNode
name|parentOforiginalProjRel
init|=
name|rootRel
decl_stmt|;
name|HiveProjectRel
name|originalProjRel
init|=
literal|null
decl_stmt|;
while|while
condition|(
name|tmpRel
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|tmpRel
operator|instanceof
name|HiveProjectRel
condition|)
block|{
name|originalProjRel
operator|=
operator|(
name|HiveProjectRel
operator|)
name|tmpRel
expr_stmt|;
break|break;
block|}
name|parentOforiginalProjRel
operator|=
name|tmpRel
expr_stmt|;
name|tmpRel
operator|=
name|tmpRel
operator|.
name|getInput
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
comment|// Assumption: top portion of tree could only be
comment|// (limit)?(OB)?(ProjectRelBase)....
name|List
argument_list|<
name|RexNode
argument_list|>
name|rootChildExps
init|=
name|originalProjRel
operator|.
name|getChildExps
argument_list|()
decl_stmt|;
if|if
condition|(
name|resultSchema
operator|.
name|size
argument_list|()
operator|!=
name|rootChildExps
operator|.
name|size
argument_list|()
condition|)
block|{
comment|// this is a bug in Hive where for queries like select key,value,value
comment|// convertRowSchemaToResultSetSchema() only returns schema containing key,value
comment|// Underlying issue is much deeper because it seems like RowResolver itself doesnt have
comment|// those mappings. see limit_pushdown.q& limit_pushdown_negative.q
comment|// Till Hive issue is fixed, disable CBO for such queries.
throw|throw
operator|new
name|OptiqSemanticException
argument_list|(
literal|"Result Schema didn't match Optiq Optimized Op Tree Schema"
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|newSelAliases
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|rootChildExps
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|newSelAliases
operator|.
name|add
argument_list|(
name|resultSchema
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|HiveProjectRel
name|replacementProjectRel
init|=
name|HiveProjectRel
operator|.
name|create
argument_list|(
name|originalProjRel
operator|.
name|getChild
argument_list|()
argument_list|,
name|originalProjRel
operator|.
name|getChildExps
argument_list|()
argument_list|,
name|newSelAliases
argument_list|)
decl_stmt|;
if|if
condition|(
name|rootRel
operator|==
name|originalProjRel
condition|)
block|{
return|return
name|replacementProjectRel
return|;
block|}
else|else
block|{
name|parentOforiginalProjRel
operator|.
name|replaceInput
argument_list|(
literal|0
argument_list|,
name|replacementProjectRel
argument_list|)
expr_stmt|;
return|return
name|rootRel
return|;
block|}
block|}
specifier|private
specifier|static
name|RelNode
name|introduceDerivedTable
parameter_list|(
specifier|final
name|RelNode
name|rel
parameter_list|)
block|{
name|List
argument_list|<
name|RexNode
argument_list|>
name|projectList
init|=
name|HiveOptiqUtil
operator|.
name|getProjsFromBelowAsInputRef
argument_list|(
name|rel
argument_list|)
decl_stmt|;
name|HiveProjectRel
name|select
init|=
name|HiveProjectRel
operator|.
name|create
argument_list|(
name|rel
operator|.
name|getCluster
argument_list|()
argument_list|,
name|rel
argument_list|,
name|projectList
argument_list|,
name|rel
operator|.
name|getRowType
argument_list|()
argument_list|,
name|rel
operator|.
name|getCollationList
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|select
return|;
block|}
specifier|private
specifier|static
name|void
name|introduceDerivedTable
parameter_list|(
specifier|final
name|RelNode
name|rel
parameter_list|,
name|RelNode
name|parent
parameter_list|)
block|{
name|int
name|i
init|=
literal|0
decl_stmt|;
name|int
name|pos
init|=
operator|-
literal|1
decl_stmt|;
name|List
argument_list|<
name|RelNode
argument_list|>
name|childList
init|=
name|parent
operator|.
name|getInputs
argument_list|()
decl_stmt|;
for|for
control|(
name|RelNode
name|child
range|:
name|childList
control|)
block|{
if|if
condition|(
name|child
operator|==
name|rel
condition|)
block|{
name|pos
operator|=
name|i
expr_stmt|;
break|break;
block|}
name|i
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|pos
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Couldn't find child node in parent's inputs"
argument_list|)
throw|;
block|}
name|RelNode
name|select
init|=
name|introduceDerivedTable
argument_list|(
name|rel
argument_list|)
decl_stmt|;
name|parent
operator|.
name|replaceInput
argument_list|(
name|pos
argument_list|,
name|select
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|boolean
name|validJoinParent
parameter_list|(
name|RelNode
name|joinNode
parameter_list|,
name|RelNode
name|parent
parameter_list|)
block|{
name|boolean
name|validParent
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|parent
operator|instanceof
name|JoinRelBase
condition|)
block|{
if|if
condition|(
operator|(
operator|(
name|JoinRelBase
operator|)
name|parent
operator|)
operator|.
name|getRight
argument_list|()
operator|==
name|joinNode
condition|)
block|{
name|validParent
operator|=
literal|false
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|parent
operator|instanceof
name|SetOpRel
condition|)
block|{
name|validParent
operator|=
literal|false
expr_stmt|;
block|}
return|return
name|validParent
return|;
block|}
specifier|private
specifier|static
name|boolean
name|validFilterParent
parameter_list|(
name|RelNode
name|filterNode
parameter_list|,
name|RelNode
name|parent
parameter_list|)
block|{
name|boolean
name|validParent
init|=
literal|true
decl_stmt|;
comment|// TOODO: Verify GB having is not a seperate filter (if so we shouldn't
comment|// introduce derived table)
if|if
condition|(
name|parent
operator|instanceof
name|FilterRelBase
operator|||
name|parent
operator|instanceof
name|JoinRelBase
operator|||
name|parent
operator|instanceof
name|SetOpRel
condition|)
block|{
name|validParent
operator|=
literal|false
expr_stmt|;
block|}
return|return
name|validParent
return|;
block|}
specifier|private
specifier|static
name|boolean
name|validGBParent
parameter_list|(
name|RelNode
name|gbNode
parameter_list|,
name|RelNode
name|parent
parameter_list|)
block|{
name|boolean
name|validParent
init|=
literal|true
decl_stmt|;
comment|// TOODO: Verify GB having is not a seperate filter (if so we shouldn't
comment|// introduce derived table)
if|if
condition|(
name|parent
operator|instanceof
name|JoinRelBase
operator|||
name|parent
operator|instanceof
name|SetOpRel
operator|||
name|parent
operator|instanceof
name|AggregateRelBase
operator|||
operator|(
name|parent
operator|instanceof
name|FilterRelBase
operator|&&
operator|(
operator|(
name|AggregateRelBase
operator|)
name|gbNode
operator|)
operator|.
name|getGroupSet
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|validParent
operator|=
literal|false
expr_stmt|;
block|}
return|return
name|validParent
return|;
block|}
specifier|private
specifier|static
name|boolean
name|validSortParent
parameter_list|(
name|RelNode
name|sortNode
parameter_list|,
name|RelNode
name|parent
parameter_list|)
block|{
name|boolean
name|validParent
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|parent
operator|!=
literal|null
operator|&&
operator|!
operator|(
name|parent
operator|instanceof
name|ProjectRelBase
operator|)
condition|)
block|{
name|validParent
operator|=
literal|false
expr_stmt|;
block|}
return|return
name|validParent
return|;
block|}
specifier|private
specifier|static
name|boolean
name|validSortChild
parameter_list|(
name|HiveSortRel
name|sortNode
parameter_list|)
block|{
name|boolean
name|validChild
init|=
literal|true
decl_stmt|;
name|RelNode
name|child
init|=
name|sortNode
operator|.
name|getChild
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|child
operator|instanceof
name|ProjectRelBase
operator|)
condition|)
block|{
name|validChild
operator|=
literal|false
expr_stmt|;
block|}
return|return
name|validChild
return|;
block|}
specifier|private
specifier|static
name|boolean
name|validSetopParent
parameter_list|(
name|RelNode
name|setop
parameter_list|,
name|RelNode
name|parent
parameter_list|)
block|{
name|boolean
name|validChild
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|parent
operator|!=
literal|null
operator|&&
operator|!
operator|(
name|parent
operator|instanceof
name|ProjectRelBase
operator|)
condition|)
block|{
name|validChild
operator|=
literal|false
expr_stmt|;
block|}
return|return
name|validChild
return|;
block|}
specifier|private
specifier|static
name|boolean
name|validSetopChild
parameter_list|(
name|RelNode
name|setopChild
parameter_list|)
block|{
name|boolean
name|validChild
init|=
literal|true
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|setopChild
operator|instanceof
name|ProjectRelBase
operator|)
condition|)
block|{
name|validChild
operator|=
literal|false
expr_stmt|;
block|}
return|return
name|validChild
return|;
block|}
block|}
end_class

end_unit

