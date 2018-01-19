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
name|optimizer
operator|.
name|calcite
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|plan
operator|.
name|Context
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|plan
operator|.
name|Contexts
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|plan
operator|.
name|RelOptCluster
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|plan
operator|.
name|RelOptSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|RelCollations
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
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
name|apache
operator|.
name|calcite
operator|.
name|rex
operator|.
name|RexNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rex
operator|.
name|RexUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|schema
operator|.
name|SchemaPlus
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|server
operator|.
name|CalciteServerStatement
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|tools
operator|.
name|FrameworkConfig
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|tools
operator|.
name|Frameworks
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|tools
operator|.
name|RelBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|tools
operator|.
name|RelBuilderFactory
import|;
end_import

begin_comment
comment|/**  * Builder for relational expressions in Hive.  *  *<p>{@code RelBuilder} does not make possible anything that you could not  * also accomplish by calling the factory methods of the particular relational  * expression. But it makes common tasks more straightforward and concise.  *  *<p>It is not thread-safe.  */
end_comment

begin_class
specifier|public
class|class
name|HiveRelBuilder
extends|extends
name|RelBuilder
block|{
specifier|private
name|HiveRelBuilder
parameter_list|(
name|Context
name|context
parameter_list|,
name|RelOptCluster
name|cluster
parameter_list|,
name|RelOptSchema
name|relOptSchema
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|,
name|cluster
argument_list|,
name|relOptSchema
argument_list|)
expr_stmt|;
block|}
comment|/** Creates a RelBuilder. */
specifier|public
specifier|static
name|RelBuilder
name|create
parameter_list|(
name|FrameworkConfig
name|config
parameter_list|)
block|{
specifier|final
name|RelOptCluster
index|[]
name|clusters
init|=
block|{
literal|null
block|}
decl_stmt|;
specifier|final
name|RelOptSchema
index|[]
name|relOptSchemas
init|=
block|{
literal|null
block|}
decl_stmt|;
name|Frameworks
operator|.
name|withPrepare
argument_list|(
operator|new
name|Frameworks
operator|.
name|PrepareAction
argument_list|<
name|Void
argument_list|>
argument_list|(
name|config
argument_list|)
block|{
specifier|public
name|Void
name|apply
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|RelOptSchema
name|relOptSchema
parameter_list|,
name|SchemaPlus
name|rootSchema
parameter_list|,
name|CalciteServerStatement
name|statement
parameter_list|)
block|{
name|clusters
index|[
literal|0
index|]
operator|=
name|cluster
expr_stmt|;
name|relOptSchemas
index|[
literal|0
index|]
operator|=
name|relOptSchema
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
operator|new
name|HiveRelBuilder
argument_list|(
name|config
operator|.
name|getContext
argument_list|()
argument_list|,
name|clusters
index|[
literal|0
index|]
argument_list|,
name|relOptSchemas
index|[
literal|0
index|]
argument_list|)
return|;
block|}
comment|/** Creates a {@link RelBuilderFactory}, a partially-created RelBuilder.    * Just add a {@link RelOptCluster} and a {@link RelOptSchema} */
specifier|public
specifier|static
name|RelBuilderFactory
name|proto
parameter_list|(
specifier|final
name|Context
name|context
parameter_list|)
block|{
return|return
operator|new
name|RelBuilderFactory
argument_list|()
block|{
specifier|public
name|RelBuilder
name|create
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|RelOptSchema
name|schema
parameter_list|)
block|{
return|return
operator|new
name|HiveRelBuilder
argument_list|(
name|context
argument_list|,
name|cluster
argument_list|,
name|schema
argument_list|)
return|;
block|}
block|}
return|;
block|}
comment|/** Creates a {@link RelBuilderFactory} that uses a given set of factories. */
specifier|public
specifier|static
name|RelBuilderFactory
name|proto
parameter_list|(
name|Object
modifier|...
name|factories
parameter_list|)
block|{
return|return
name|proto
argument_list|(
name|Contexts
operator|.
name|of
argument_list|(
name|factories
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RelBuilder
name|filter
parameter_list|(
name|Iterable
argument_list|<
name|?
extends|extends
name|RexNode
argument_list|>
name|predicates
parameter_list|)
block|{
specifier|final
name|RexNode
name|x
init|=
name|RexUtil
operator|.
name|simplify
argument_list|(
name|cluster
operator|.
name|getRexBuilder
argument_list|()
argument_list|,
name|RexUtil
operator|.
name|composeConjunction
argument_list|(
name|cluster
operator|.
name|getRexBuilder
argument_list|()
argument_list|,
name|predicates
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|x
operator|.
name|isAlwaysTrue
argument_list|()
condition|)
block|{
specifier|final
name|RelNode
name|input
init|=
name|build
argument_list|()
decl_stmt|;
specifier|final
name|RelNode
name|filter
init|=
name|HiveRelFactories
operator|.
name|HIVE_FILTER_FACTORY
operator|.
name|createFilter
argument_list|(
name|input
argument_list|,
name|x
argument_list|)
decl_stmt|;
return|return
name|this
operator|.
name|push
argument_list|(
name|filter
argument_list|)
return|;
block|}
return|return
name|this
return|;
block|}
comment|/**    * Empty relationship can be expressed in many different ways, e.g.,    * filter(cond=false), empty LogicalValues(), etc. Calcite default implementation    * uses empty LogicalValues(); however, currently there is not an equivalent to    * this expression in Hive. Thus, we use limit 0, since Hive already includes    * optimizations that will do early pruning of the result tree when it is found,    * e.g., GlobalLimitOptimizer.    */
annotation|@
name|Override
specifier|public
name|RelBuilder
name|empty
parameter_list|()
block|{
specifier|final
name|RelNode
name|input
init|=
name|build
argument_list|()
decl_stmt|;
specifier|final
name|RelNode
name|sort
init|=
name|HiveRelFactories
operator|.
name|HIVE_SORT_FACTORY
operator|.
name|createSort
argument_list|(
name|input
argument_list|,
name|RelCollations
operator|.
name|of
argument_list|()
argument_list|,
literal|null
argument_list|,
name|literal
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|this
operator|.
name|push
argument_list|(
name|sort
argument_list|)
return|;
block|}
block|}
end_class

end_unit

