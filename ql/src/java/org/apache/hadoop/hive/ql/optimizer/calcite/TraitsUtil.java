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
name|RelTraitSet
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
name|RelCollation
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveRelNode
import|;
end_import

begin_class
specifier|public
class|class
name|TraitsUtil
block|{
specifier|public
specifier|static
name|RelTraitSet
name|getSortTraitSet
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|RelTraitSet
name|traitSet
parameter_list|,
name|RelCollation
name|collation
parameter_list|)
block|{
return|return
name|traitSet
operator|.
name|plus
argument_list|(
name|collation
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|RelTraitSet
name|getDefaultTraitSet
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|)
block|{
return|return
name|cluster
operator|.
name|traitSetOf
argument_list|(
name|HiveRelNode
operator|.
name|CONVENTION
argument_list|,
name|RelCollations
operator|.
name|EMPTY
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|RelTraitSet
name|getDefaultTraitSet
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|RelTraitSet
name|traitsFromInput
parameter_list|)
block|{
return|return
name|RelTraitSet
operator|.
name|createEmpty
argument_list|()
operator|.
name|merge
argument_list|(
name|traitsFromInput
argument_list|)
operator|.
name|merge
argument_list|(
name|getDefaultTraitSet
argument_list|(
name|cluster
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

