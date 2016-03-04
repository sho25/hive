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
name|physical
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
name|plan
operator|.
name|OperatorDesc
import|;
end_import

begin_comment
comment|/**  * This class contains the bucketing sorting context that is passed  * while walking the operator tree in inferring bucket/sort columns. The context  * contains the mappings from operators and files to the columns their output is  * bucketed/sorted on.  */
end_comment

begin_class
specifier|public
class|class
name|BucketingSortingCtx
implements|implements
name|NodeProcessorCtx
block|{
specifier|private
name|boolean
name|disableBucketing
decl_stmt|;
comment|// A mapping from an operator to the columns by which it's output is bucketed
specifier|private
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|,
name|List
argument_list|<
name|BucketCol
argument_list|>
argument_list|>
name|bucketedColsByOp
decl_stmt|;
comment|// A mapping from a directory which a FileSinkOperator writes into to the columns by which that
comment|// output is bucketed
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|BucketCol
argument_list|>
argument_list|>
name|bucketedColsByDirectory
decl_stmt|;
comment|// A mapping from an operator to the columns by which it's output is sorted
specifier|private
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|,
name|List
argument_list|<
name|SortCol
argument_list|>
argument_list|>
name|sortedColsByOp
decl_stmt|;
comment|// A mapping from a directory which a FileSinkOperator writes into to the columns by which that
comment|// output is sorted
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|SortCol
argument_list|>
argument_list|>
name|sortedColsByDirectory
decl_stmt|;
specifier|public
name|BucketingSortingCtx
parameter_list|(
name|boolean
name|disableBucketing
parameter_list|)
block|{
name|this
operator|.
name|disableBucketing
operator|=
name|disableBucketing
expr_stmt|;
name|this
operator|.
name|bucketedColsByOp
operator|=
operator|new
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|,
name|List
argument_list|<
name|BucketCol
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|bucketedColsByDirectory
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|BucketCol
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|sortedColsByOp
operator|=
operator|new
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|,
name|List
argument_list|<
name|SortCol
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|sortedColsByDirectory
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|SortCol
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|BucketCol
argument_list|>
name|getBucketedCols
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|)
block|{
return|return
name|disableBucketing
condition|?
literal|null
else|:
name|bucketedColsByOp
operator|.
name|get
argument_list|(
name|op
argument_list|)
return|;
block|}
specifier|public
name|void
name|setBucketedCols
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|,
name|List
argument_list|<
name|BucketCol
argument_list|>
name|bucketCols
parameter_list|)
block|{
if|if
condition|(
operator|!
name|disableBucketing
condition|)
block|{
name|bucketedColsByOp
operator|.
name|put
argument_list|(
name|op
argument_list|,
name|bucketCols
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|BucketCol
argument_list|>
argument_list|>
name|getBucketedColsByDirectory
parameter_list|()
block|{
return|return
name|disableBucketing
condition|?
literal|null
else|:
name|bucketedColsByDirectory
return|;
block|}
specifier|public
name|void
name|setBucketedColsByDirectory
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|BucketCol
argument_list|>
argument_list|>
name|bucketedColsByDirectory
parameter_list|)
block|{
if|if
condition|(
operator|!
name|disableBucketing
condition|)
block|{
name|this
operator|.
name|bucketedColsByDirectory
operator|=
name|bucketedColsByDirectory
expr_stmt|;
block|}
block|}
specifier|public
name|List
argument_list|<
name|SortCol
argument_list|>
name|getSortedCols
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|)
block|{
return|return
name|sortedColsByOp
operator|.
name|get
argument_list|(
name|op
argument_list|)
return|;
block|}
specifier|public
name|void
name|setSortedCols
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|,
name|List
argument_list|<
name|SortCol
argument_list|>
name|sortedCols
parameter_list|)
block|{
name|this
operator|.
name|sortedColsByOp
operator|.
name|put
argument_list|(
name|op
argument_list|,
name|sortedCols
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|SortCol
argument_list|>
argument_list|>
name|getSortedColsByDirectory
parameter_list|()
block|{
return|return
name|sortedColsByDirectory
return|;
block|}
specifier|public
name|void
name|setSortedColsByDirectory
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|SortCol
argument_list|>
argument_list|>
name|sortedColsByDirectory
parameter_list|)
block|{
name|this
operator|.
name|sortedColsByDirectory
operator|=
name|sortedColsByDirectory
expr_stmt|;
block|}
comment|/**    *    * BucketSortCol.    *    * Classes that implement this interface provide a way to store information about equivalent    * columns as their names and indexes in the schema change going into and out of operators.  The    * definition of equivalent columns is up to the class which uses these classes, e.g.    * BucketingSortingOpProcFactory.  For example, two columns are equivalent if they    * contain exactly the same data.  Though, it's possible that two columns contain exactly the    * same data and are not known to be equivalent.    *    * E.g. SELECT key a, key b FROM (SELECT key, count(*) c FROM src GROUP BY key) s;    * In this case, assuming this is done in a single map reduce job with the group by operator    * processed in the reducer, the data coming out of the group by operator will be bucketed    * by key, which would be at index 0 in the schema, after the outer select operator, the output    * can be viewed as bucketed by either the column with alias a or the column with alias b.  To    * represent this, there could be a single BucketSortCol implementation instance whose names    * include both a and b, and whose indexes include both 0 and 1.    *    * Implementations of this interface should maintain the restriction that the alias    * getNames().get(i) should have index getIndexes().get(i) in the schema.    */
specifier|public
specifier|static
interface|interface
name|BucketSortCol
block|{
comment|// Get a list of aliases for the same column
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getNames
parameter_list|()
function_decl|;
comment|// Get a list of indexes for which the columns in the schema are the same
specifier|public
name|List
argument_list|<
name|Integer
argument_list|>
name|getIndexes
parameter_list|()
function_decl|;
comment|// Add an alternative alias for the column this instance represents, and its index in the
comment|// schema.
specifier|public
name|void
name|addAlias
parameter_list|(
name|String
name|name
parameter_list|,
name|Integer
name|index
parameter_list|)
function_decl|;
block|}
comment|/**    *    * BucketCol.    *    * An implementation of BucketSortCol which contains known aliases/indexes of equivalent columns    * which data is determined to be bucketed on.    */
specifier|public
specifier|static
specifier|final
class|class
name|BucketCol
implements|implements
name|BucketSortCol
implements|,
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|// Equivalent aliases for the column
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|names
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// Indexes of those equivalent columns
specifier|private
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|indexes
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|BucketCol
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|index
parameter_list|)
block|{
name|addAlias
argument_list|(
name|name
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
specifier|public
name|BucketCol
parameter_list|()
block|{      }
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getNames
parameter_list|()
block|{
return|return
name|names
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Integer
argument_list|>
name|getIndexes
parameter_list|()
block|{
return|return
name|indexes
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addAlias
parameter_list|(
name|String
name|name
parameter_list|,
name|Integer
name|index
parameter_list|)
block|{
name|names
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|indexes
operator|.
name|add
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
comment|// Chooses a representative alias and index to use as the String, the first is used because
comment|// it is set in the constructor
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"name: "
operator|+
name|names
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|+
literal|" index: "
operator|+
name|indexes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
block|}
comment|/**    *    * SortCol.    *    * An implementation of BucketSortCol which contains known aliases/indexes of equivalent columns    * which data is determined to be sorted on.  Unlike aliases, and indexes the sort order is known    * to be constant for all equivalent columns.    */
specifier|public
specifier|static
specifier|final
class|class
name|SortCol
implements|implements
name|BucketSortCol
implements|,
name|Serializable
block|{
specifier|public
name|SortCol
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|// Equivalent aliases for the column
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|names
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// Indexes of those equivalent columns
specifier|private
name|List
argument_list|<
name|Integer
argument_list|>
name|indexes
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
comment|// Sort order (+|-)
specifier|private
name|char
name|sortOrder
decl_stmt|;
specifier|public
name|SortCol
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|index
parameter_list|,
name|char
name|sortOrder
parameter_list|)
block|{
name|this
argument_list|(
name|sortOrder
argument_list|)
expr_stmt|;
name|addAlias
argument_list|(
name|name
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SortCol
parameter_list|(
name|char
name|sortOrder
parameter_list|)
block|{
name|this
operator|.
name|sortOrder
operator|=
name|sortOrder
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getNames
parameter_list|()
block|{
return|return
name|names
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Integer
argument_list|>
name|getIndexes
parameter_list|()
block|{
return|return
name|indexes
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addAlias
parameter_list|(
name|String
name|name
parameter_list|,
name|Integer
name|index
parameter_list|)
block|{
name|names
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|indexes
operator|.
name|add
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
specifier|public
name|char
name|getSortOrder
parameter_list|()
block|{
return|return
name|sortOrder
return|;
block|}
annotation|@
name|Override
comment|// Chooses a representative alias, index, and order to use as the String, the first is used
comment|// because it is set in the constructor
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"name: "
operator|+
name|names
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|+
literal|" index: "
operator|+
name|indexes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|+
literal|" order: "
operator|+
name|sortOrder
return|;
block|}
block|}
block|}
end_class

end_unit

