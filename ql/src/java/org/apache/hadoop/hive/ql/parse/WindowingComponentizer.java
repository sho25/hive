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
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|ql
operator|.
name|parse
operator|.
name|PTFInvocationSpec
operator|.
name|PartitioningSpec
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
name|WindowingSpec
operator|.
name|WindowExpressionSpec
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
name|WindowingSpec
operator|.
name|WindowFunctionSpec
import|;
end_import

begin_comment
comment|/*  * breakup the original WindowingSpec into a set of WindowingSpecs.  * Each WindowingSpec is executed in an instance of PTFOperator,  * preceded by ReduceSink and Extract.  * The logic to componentize is straightforward:  * - distribute Window Fn. Specs from original Window Spec into a set of WindowSpecs,  *   based on their Partitioning.  * - A Group of WindowSpecs, is a subset of the Window Fn Invocations in the QueryBlock that  *   have the same Partitioning(Partition + Order spec).  * - Each Group is put in a new WindowingSpec and is evaluated in its own PTFOperator instance.  * - the order of computation is then inferred based on the dependency between Groups.  *   If 2 groups have the same dependency, then the Group with the function that is  *   earliest in the SelectList is executed first.  */
end_comment

begin_class
specifier|public
class|class
name|WindowingComponentizer
block|{
name|WindowingSpec
name|originalSpec
decl_stmt|;
name|LinkedHashMap
argument_list|<
name|PartitioningSpec
argument_list|,
name|WindowingSpec
argument_list|>
name|groups
decl_stmt|;
specifier|public
name|WindowingComponentizer
parameter_list|(
name|WindowingSpec
name|originalSpec
parameter_list|)
throws|throws
name|SemanticException
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|originalSpec
operator|=
name|originalSpec
expr_stmt|;
name|groups
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|PartitioningSpec
argument_list|,
name|WindowingSpec
argument_list|>
argument_list|()
expr_stmt|;
name|groupFunctions
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|groupFunctions
parameter_list|()
throws|throws
name|SemanticException
block|{
for|for
control|(
name|WindowExpressionSpec
name|expr
range|:
name|originalSpec
operator|.
name|getWindowExpressions
argument_list|()
control|)
block|{
name|WindowFunctionSpec
name|wFn
init|=
operator|(
name|WindowFunctionSpec
operator|)
name|expr
decl_stmt|;
name|PartitioningSpec
name|wFnGrp
init|=
name|wFn
operator|.
name|getWindowSpec
argument_list|()
operator|.
name|getPartitioning
argument_list|()
decl_stmt|;
name|WindowingSpec
name|wSpec
init|=
name|groups
operator|.
name|get
argument_list|(
name|wFnGrp
argument_list|)
decl_stmt|;
if|if
condition|(
name|wSpec
operator|==
literal|null
condition|)
block|{
name|wSpec
operator|=
operator|new
name|WindowingSpec
argument_list|()
expr_stmt|;
name|groups
operator|.
name|put
argument_list|(
name|wFnGrp
argument_list|,
name|wSpec
argument_list|)
expr_stmt|;
block|}
name|wSpec
operator|.
name|addWindowFunction
argument_list|(
name|wFn
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
operator|!
name|groups
operator|.
name|isEmpty
argument_list|()
return|;
block|}
specifier|public
name|WindowingSpec
name|next
parameter_list|(
name|HiveConf
name|hCfg
parameter_list|,
name|SemanticAnalyzer
name|semAly
parameter_list|,
name|UnparseTranslator
name|unparseT
parameter_list|,
name|RowResolver
name|inputRR
parameter_list|)
throws|throws
name|SemanticException
block|{
name|SemanticException
name|originalException
init|=
literal|null
decl_stmt|;
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|PartitioningSpec
argument_list|,
name|WindowingSpec
argument_list|>
argument_list|>
name|grpIt
init|=
name|groups
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|grpIt
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|PartitioningSpec
argument_list|,
name|WindowingSpec
argument_list|>
name|entry
init|=
name|grpIt
operator|.
name|next
argument_list|()
decl_stmt|;
name|WindowingSpec
name|wSpec
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
try|try
block|{
name|PTFTranslator
name|t
init|=
operator|new
name|PTFTranslator
argument_list|()
decl_stmt|;
name|t
operator|.
name|translate
argument_list|(
name|wSpec
argument_list|,
name|semAly
argument_list|,
name|hCfg
argument_list|,
name|inputRR
argument_list|,
name|unparseT
argument_list|)
expr_stmt|;
name|groups
operator|.
name|remove
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|wSpec
return|;
block|}
catch|catch
parameter_list|(
name|SemanticException
name|se
parameter_list|)
block|{
name|originalException
operator|=
name|se
expr_stmt|;
block|}
block|}
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Failed to breakup Windowing invocations into Groups. "
operator|+
literal|"At least 1 group must only depend on input columns. "
operator|+
literal|"Also check for circular dependencies.\n"
operator|+
literal|"Underlying error: "
operator|+
name|originalException
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

