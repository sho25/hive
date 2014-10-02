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
name|cost
package|;
end_package

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
name|HiveRel
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
name|HiveTableScanRel
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
name|RelOptCost
import|;
end_import

begin_comment
comment|// Use this once we have Join Algorithm selection
end_comment

begin_class
specifier|public
class|class
name|HiveCostUtil
block|{
specifier|private
specifier|static
specifier|final
name|double
name|cpuCostInNanoSec
init|=
literal|1.0
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|double
name|netCostInNanoSec
init|=
literal|150
operator|*
name|cpuCostInNanoSec
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|double
name|localFSWriteCostInNanoSec
init|=
literal|4
operator|*
name|netCostInNanoSec
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|double
name|localFSReadCostInNanoSec
init|=
literal|4
operator|*
name|netCostInNanoSec
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|double
name|hDFSWriteCostInNanoSec
init|=
literal|10
operator|*
name|localFSWriteCostInNanoSec
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
comment|//Use this once we have Join Algorithm selection
specifier|private
specifier|static
specifier|final
name|double
name|hDFSReadCostInNanoSec
init|=
literal|1.5
operator|*
name|localFSReadCostInNanoSec
decl_stmt|;
specifier|public
specifier|static
name|RelOptCost
name|computCardinalityBasedCost
parameter_list|(
name|HiveRel
name|hr
parameter_list|)
block|{
return|return
operator|new
name|HiveCost
argument_list|(
name|hr
operator|.
name|getRows
argument_list|()
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|HiveCost
name|computeCost
parameter_list|(
name|HiveTableScanRel
name|t
parameter_list|)
block|{
name|double
name|cardinality
init|=
name|t
operator|.
name|getRows
argument_list|()
decl_stmt|;
return|return
operator|new
name|HiveCost
argument_list|(
name|cardinality
argument_list|,
literal|0
argument_list|,
name|hDFSWriteCostInNanoSec
operator|*
name|cardinality
operator|*
literal|0
argument_list|)
return|;
block|}
block|}
end_class

end_unit

