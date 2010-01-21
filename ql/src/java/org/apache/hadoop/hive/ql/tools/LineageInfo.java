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
name|tools
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
name|java
operator|.
name|util
operator|.
name|Stack
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|NodeProcessorCtx
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
name|parse
operator|.
name|ASTNode
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
name|HiveParser
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
name|ParseDriver
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
name|ParseException
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
comment|/**  *   * This class prints out the lineage info. It takes sql as input and prints  * lineage info. Currently this prints only input and output tables for a given  * sql. Later we can expand to add join tables etc.  *   */
end_comment

begin_class
specifier|public
class|class
name|LineageInfo
implements|implements
name|NodeProcessor
block|{
comment|/**    * Stores input tables in sql    */
name|TreeSet
argument_list|<
name|String
argument_list|>
name|inputTableList
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Stores output tables in sql    */
name|TreeSet
argument_list|<
name|String
argument_list|>
name|OutputTableList
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    *     * @return java.util.TreeSet    */
specifier|public
name|TreeSet
argument_list|<
name|String
argument_list|>
name|getInputTableList
parameter_list|()
block|{
return|return
name|inputTableList
return|;
block|}
comment|/**    * @return java.util.TreeSet    */
specifier|public
name|TreeSet
argument_list|<
name|String
argument_list|>
name|getOutputTableList
parameter_list|()
block|{
return|return
name|OutputTableList
return|;
block|}
comment|/**    * Implements the process method for the NodeProcessor interface.    */
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ASTNode
name|pt
init|=
operator|(
name|ASTNode
operator|)
name|nd
decl_stmt|;
switch|switch
condition|(
name|pt
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|HiveParser
operator|.
name|TOK_TAB
case|:
name|OutputTableList
operator|.
name|add
argument_list|(
name|pt
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
break|break;
case|case
name|HiveParser
operator|.
name|TOK_TABREF
case|:
name|String
name|table_name
init|=
operator|(
operator|(
name|ASTNode
operator|)
name|pt
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|getText
argument_list|()
decl_stmt|;
name|inputTableList
operator|.
name|add
argument_list|(
name|table_name
argument_list|)
expr_stmt|;
break|break;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * parses given query and gets the lineage info.    *     * @param query    * @throws ParseException    */
specifier|public
name|void
name|getLineageInfo
parameter_list|(
name|String
name|query
parameter_list|)
throws|throws
name|ParseException
throws|,
name|SemanticException
block|{
comment|/*      * Get the AST tree      */
name|ParseDriver
name|pd
init|=
operator|new
name|ParseDriver
argument_list|()
decl_stmt|;
name|ASTNode
name|tree
init|=
name|pd
operator|.
name|parse
argument_list|(
name|query
argument_list|)
decl_stmt|;
while|while
condition|(
operator|(
name|tree
operator|.
name|getToken
argument_list|()
operator|==
literal|null
operator|)
operator|&&
operator|(
name|tree
operator|.
name|getChildCount
argument_list|()
operator|>
literal|0
operator|)
condition|)
block|{
name|tree
operator|=
operator|(
name|ASTNode
operator|)
name|tree
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/*      * initialize Event Processor and dispatcher.      */
name|inputTableList
operator|.
name|clear
argument_list|()
expr_stmt|;
name|OutputTableList
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// create a walker which walks the tree in a DFS manner while maintaining
comment|// the operator stack. The dispatcher
comment|// generates the plan from the operator tree
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|rules
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
comment|// The dispatcher fires the processor corresponding to the closest matching
comment|// rule and passes the context along
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
name|this
argument_list|,
name|rules
argument_list|,
literal|null
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
name|add
argument_list|(
name|tree
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
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
throws|,
name|ParseException
throws|,
name|SemanticException
block|{
name|String
name|query
init|=
name|args
index|[
literal|0
index|]
decl_stmt|;
name|LineageInfo
name|lep
init|=
operator|new
name|LineageInfo
argument_list|()
decl_stmt|;
name|lep
operator|.
name|getLineageInfo
argument_list|(
name|query
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|tab
range|:
name|lep
operator|.
name|getInputTableList
argument_list|()
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"InputTable="
operator|+
name|tab
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|tab
range|:
name|lep
operator|.
name|getOutputTableList
argument_list|()
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"OutputTable="
operator|+
name|tab
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

