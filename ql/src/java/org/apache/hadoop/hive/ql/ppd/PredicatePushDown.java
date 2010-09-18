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
name|ppd
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|exec
operator|.
name|Operator
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
name|lib
operator|.
name|DefaultGraphWalker
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
name|lib
operator|.
name|DefaultRuleDispatcher
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
name|lib
operator|.
name|Dispatcher
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
name|lib
operator|.
name|GraphWalker
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
name|lib
operator|.
name|Node
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
name|lib
operator|.
name|NodeProcessor
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
name|lib
operator|.
name|Rule
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
name|lib
operator|.
name|RuleRegExp
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
name|Transform
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
name|parse
operator|.
name|OpParseContext
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
name|parse
operator|.
name|ParseContext
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
name|parse
operator|.
name|SemanticException
import|;
end_import

begin_comment
comment|/**  * Implements predicate pushdown. Predicate pushdown is a term borrowed from  * relational databases even though for Hive it is predicate pushup. The basic  * idea is to process expressions as early in the plan as possible. The default  * plan generation adds filters where they are seen but in some instances some  * of the filter expressions can be pushed nearer to the operator that sees this  * particular data for the first time. e.g. select a.*, b.* from a join b on  * (a.col1 = b.col1) where a.col1> 20 and b.col2> 40  *  * For the above query, the predicates (a.col1> 20) and (b.col2> 40), without  * predicate pushdown, would be evaluated after the join processing has been  * done. Suppose the two predicates filter out most of the rows from a and b,  * the join is unnecessarily processing these rows. With predicate pushdown,  * these two predicates will be processed before the join.  *  * Predicate pushdown is enabled by setting hive.optimize.ppd to true. It is  * disable by default.  *  * The high-level algorithm is describe here - An operator is processed after  * all its children have been processed - An operator processes its own  * predicates and then merges (conjunction) with the processed predicates of its  * children. In case of multiple children, there are combined using disjunction  * (OR). - A predicate expression is processed for an operator using the  * following steps - If the expr is a constant then it is a candidate for  * predicate pushdown - If the expr is a col reference then it is a candidate  * and its alias is noted - If the expr is an index and both the array and index  * expr are treated as children - If the all child expr are candidates for  * pushdown and all of the expression reference only one alias from the  * operator's RowResolver then the current expression is also a candidate One  * key thing to note is that some operators (Select, ReduceSink, GroupBy, Join  * etc) change the columns as data flows through them. In such cases the column  * references are replaced by the corresponding expression in the input data.  */
end_comment

begin_class
specifier|public
class|class
name|PredicatePushDown
implements|implements
name|Transform
block|{
specifier|private
name|ParseContext
name|pGraphContext
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|OpParseContext
argument_list|>
name|opToParseCtxMap
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ParseContext
name|transform
parameter_list|(
name|ParseContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|pGraphContext
operator|=
name|pctx
expr_stmt|;
name|opToParseCtxMap
operator|=
name|pGraphContext
operator|.
name|getOpParseCtx
argument_list|()
expr_stmt|;
comment|// create a the context for walking operators
name|OpWalkerInfo
name|opWalkerInfo
init|=
operator|new
name|OpWalkerInfo
argument_list|(
name|pGraphContext
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|opRules
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
argument_list|()
decl_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R1"
argument_list|,
literal|"FIL%"
argument_list|)
argument_list|,
name|OpProcFactory
operator|.
name|getFilterProc
argument_list|()
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R3"
argument_list|,
literal|"JOIN%"
argument_list|)
argument_list|,
name|OpProcFactory
operator|.
name|getJoinProc
argument_list|()
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R4"
argument_list|,
literal|"RS%"
argument_list|)
argument_list|,
name|OpProcFactory
operator|.
name|getRSProc
argument_list|()
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R5"
argument_list|,
literal|"TS%"
argument_list|)
argument_list|,
name|OpProcFactory
operator|.
name|getTSProc
argument_list|()
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R6"
argument_list|,
literal|"SCR%"
argument_list|)
argument_list|,
name|OpProcFactory
operator|.
name|getSCRProc
argument_list|()
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R6"
argument_list|,
literal|"LIM%"
argument_list|)
argument_list|,
name|OpProcFactory
operator|.
name|getLIMProc
argument_list|()
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R7"
argument_list|,
literal|"UDTF%"
argument_list|)
argument_list|,
name|OpProcFactory
operator|.
name|getUDTFProc
argument_list|()
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R8"
argument_list|,
literal|"LVF%"
argument_list|)
argument_list|,
name|OpProcFactory
operator|.
name|getLVFProc
argument_list|()
argument_list|)
expr_stmt|;
comment|// The dispatcher fires the processor corresponding to the closest matching
comment|// rule and passes the context along
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
name|OpProcFactory
operator|.
name|getDefaultProc
argument_list|()
argument_list|,
name|opRules
argument_list|,
name|opWalkerInfo
argument_list|)
decl_stmt|;
name|GraphWalker
name|ogw
init|=
operator|new
name|DefaultGraphWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
comment|// Create a list of topop nodes
name|ArrayList
argument_list|<
name|Node
argument_list|>
name|topNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
name|topNodes
operator|.
name|addAll
argument_list|(
name|pGraphContext
operator|.
name|getTopOps
argument_list|()
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|ogw
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|pGraphContext
return|;
block|}
block|}
end_class

end_unit

