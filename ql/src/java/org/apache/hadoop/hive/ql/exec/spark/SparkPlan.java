begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  Licensed to the Apache Software Foundation (ASF) under one  *  or more contributor license agreements.  See the NOTICE file  *  distributed with this work for additional information  *  regarding copyright ownership.  The ASF licenses this file  *  to you under the Apache License, Version 2.0 (the  *  "License"); you may not use this file except in compliance  *  with the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  */
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
name|exec
operator|.
name|spark
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|io
operator|.
name|HiveKey
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
name|log
operator|.
name|PerfLogger
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
name|io
operator|.
name|BytesWritable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaPairRDD
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
name|Preconditions
import|;
end_import

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
specifier|public
class|class
name|SparkPlan
block|{
specifier|private
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
name|SparkPlan
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SparkPlan
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|PerfLogger
name|perfLogger
init|=
name|PerfLogger
operator|.
name|getPerfLogger
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|SparkTran
argument_list|>
name|rootTrans
init|=
operator|new
name|HashSet
argument_list|<
name|SparkTran
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|SparkTran
argument_list|>
name|leafTrans
init|=
operator|new
name|HashSet
argument_list|<
name|SparkTran
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|SparkTran
argument_list|,
name|List
argument_list|<
name|SparkTran
argument_list|>
argument_list|>
name|transGraph
init|=
operator|new
name|HashMap
argument_list|<
name|SparkTran
argument_list|,
name|List
argument_list|<
name|SparkTran
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|SparkTran
argument_list|,
name|List
argument_list|<
name|SparkTran
argument_list|>
argument_list|>
name|invertedTransGraph
init|=
operator|new
name|HashMap
argument_list|<
name|SparkTran
argument_list|,
name|List
argument_list|<
name|SparkTran
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|Integer
argument_list|>
name|cachedRDDIds
init|=
operator|new
name|HashSet
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|generateGraph
parameter_list|()
block|{
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|SPARK_BUILD_RDD_GRAPH
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|SparkTran
argument_list|,
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|>
name|tranToOutputRDDMap
init|=
operator|new
name|HashMap
argument_list|<
name|SparkTran
argument_list|,
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|SparkTran
name|tran
range|:
name|getAllTrans
argument_list|()
control|)
block|{
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|rdd
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|SparkTran
argument_list|>
name|parents
init|=
name|getParents
argument_list|(
name|tran
argument_list|)
decl_stmt|;
if|if
condition|(
name|parents
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// Root tran, it must be MapInput
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|tran
operator|instanceof
name|MapInput
argument_list|,
literal|"AssertionError: tran must be an instance of MapInput"
argument_list|)
expr_stmt|;
name|rdd
operator|=
name|tran
operator|.
name|transform
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|SparkTran
name|parent
range|:
name|parents
control|)
block|{
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|prevRDD
init|=
name|tranToOutputRDDMap
operator|.
name|get
argument_list|(
name|parent
argument_list|)
decl_stmt|;
if|if
condition|(
name|rdd
operator|==
literal|null
condition|)
block|{
name|rdd
operator|=
name|prevRDD
expr_stmt|;
block|}
else|else
block|{
name|rdd
operator|=
name|rdd
operator|.
name|union
argument_list|(
name|prevRDD
argument_list|)
expr_stmt|;
block|}
block|}
name|rdd
operator|=
name|tran
operator|.
name|transform
argument_list|(
name|rdd
argument_list|)
expr_stmt|;
block|}
name|tranToOutputRDDMap
operator|.
name|put
argument_list|(
name|tran
argument_list|,
name|rdd
argument_list|)
expr_stmt|;
block|}
name|logSparkPlan
argument_list|()
expr_stmt|;
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|finalRDD
init|=
literal|null
decl_stmt|;
for|for
control|(
name|SparkTran
name|leafTran
range|:
name|leafTrans
control|)
block|{
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|rdd
init|=
name|tranToOutputRDDMap
operator|.
name|get
argument_list|(
name|leafTran
argument_list|)
decl_stmt|;
if|if
condition|(
name|finalRDD
operator|==
literal|null
condition|)
block|{
name|finalRDD
operator|=
name|rdd
expr_stmt|;
block|}
else|else
block|{
name|finalRDD
operator|=
name|finalRDD
operator|.
name|union
argument_list|(
name|rdd
argument_list|)
expr_stmt|;
block|}
block|}
name|perfLogger
operator|.
name|PerfLogEnd
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|SPARK_BUILD_RDD_GRAPH
argument_list|)
expr_stmt|;
return|return
name|finalRDD
return|;
block|}
specifier|private
name|void
name|addNumberToTrans
parameter_list|()
block|{
name|int
name|i
init|=
literal|1
decl_stmt|;
name|String
name|name
init|=
literal|null
decl_stmt|;
comment|// Traverse leafTran& transGraph add numbers to trans
for|for
control|(
name|SparkTran
name|leaf
range|:
name|leafTrans
control|)
block|{
name|name
operator|=
name|leaf
operator|.
name|getName
argument_list|()
operator|+
literal|" "
operator|+
name|i
operator|++
expr_stmt|;
name|leaf
operator|.
name|setName
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
name|Set
argument_list|<
name|SparkTran
argument_list|>
name|sparkTrans
init|=
name|transGraph
operator|.
name|keySet
argument_list|()
decl_stmt|;
for|for
control|(
name|SparkTran
name|tran
range|:
name|sparkTrans
control|)
block|{
name|name
operator|=
name|tran
operator|.
name|getName
argument_list|()
operator|+
literal|" "
operator|+
name|i
operator|++
expr_stmt|;
name|tran
operator|.
name|setName
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|logSparkPlan
parameter_list|()
block|{
name|addNumberToTrans
argument_list|()
expr_stmt|;
name|ArrayList
argument_list|<
name|SparkTran
argument_list|>
name|leafTran
init|=
operator|new
name|ArrayList
argument_list|<
name|SparkTran
argument_list|>
argument_list|()
decl_stmt|;
name|leafTran
operator|.
name|addAll
argument_list|(
name|leafTrans
argument_list|)
expr_stmt|;
for|for
control|(
name|SparkTran
name|leaf
range|:
name|leafTrans
control|)
block|{
name|collectLeafTrans
argument_list|(
name|leaf
argument_list|,
name|leafTran
argument_list|)
expr_stmt|;
block|}
comment|// Start Traverse from the leafTrans and get parents of each leafTrans till
comment|// the end
name|StringBuilder
name|sparkPlan
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"\n\t!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Spark Plan !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! \n\n"
argument_list|)
decl_stmt|;
for|for
control|(
name|SparkTran
name|leaf
range|:
name|leafTran
control|)
block|{
name|sparkPlan
operator|.
name|append
argument_list|(
name|leaf
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|getSparkPlan
argument_list|(
name|leaf
argument_list|,
name|sparkPlan
argument_list|)
expr_stmt|;
name|sparkPlan
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
name|sparkPlan
operator|.
name|append
argument_list|(
literal|" \n\t!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Spark Plan !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|sparkPlan
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|collectLeafTrans
parameter_list|(
name|SparkTran
name|leaf
parameter_list|,
name|List
argument_list|<
name|SparkTran
argument_list|>
name|reduceTrans
parameter_list|)
block|{
name|List
argument_list|<
name|SparkTran
argument_list|>
name|parents
init|=
name|getParents
argument_list|(
name|leaf
argument_list|)
decl_stmt|;
if|if
condition|(
name|parents
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|SparkTran
name|nextLeaf
init|=
literal|null
decl_stmt|;
for|for
control|(
name|SparkTran
name|leafTran
range|:
name|parents
control|)
block|{
if|if
condition|(
name|leafTran
operator|instanceof
name|ReduceTran
condition|)
block|{
name|reduceTrans
operator|.
name|add
argument_list|(
name|leafTran
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|getParents
argument_list|(
name|leafTran
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
name|nextLeaf
operator|=
name|leafTran
expr_stmt|;
block|}
block|}
if|if
condition|(
name|nextLeaf
operator|!=
literal|null
condition|)
name|collectLeafTrans
argument_list|(
name|nextLeaf
argument_list|,
name|reduceTrans
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|getSparkPlan
parameter_list|(
name|SparkTran
name|tran
parameter_list|,
name|StringBuilder
name|sparkPlan
parameter_list|)
block|{
name|List
argument_list|<
name|SparkTran
argument_list|>
name|parents
init|=
name|getParents
argument_list|(
name|tran
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|SparkTran
argument_list|>
name|nextLeaf
init|=
operator|new
name|ArrayList
argument_list|<
name|SparkTran
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|parents
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|sparkPlan
operator|.
name|append
argument_list|(
literal|"<-- "
argument_list|)
expr_stmt|;
name|boolean
name|isFirst
init|=
literal|true
decl_stmt|;
for|for
control|(
name|SparkTran
name|leaf
range|:
name|parents
control|)
block|{
if|if
condition|(
name|isFirst
condition|)
block|{
name|sparkPlan
operator|.
name|append
argument_list|(
literal|"( "
operator|+
name|leaf
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|leaf
operator|instanceof
name|ShuffleTran
condition|)
block|{
name|logShuffleTranStatus
argument_list|(
operator|(
name|ShuffleTran
operator|)
name|leaf
argument_list|,
name|sparkPlan
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logCacheStatus
argument_list|(
name|leaf
argument_list|,
name|sparkPlan
argument_list|)
expr_stmt|;
block|}
name|isFirst
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|sparkPlan
operator|.
name|append
argument_list|(
literal|","
operator|+
name|leaf
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|leaf
operator|instanceof
name|ShuffleTran
condition|)
block|{
name|logShuffleTranStatus
argument_list|(
operator|(
name|ShuffleTran
operator|)
name|leaf
argument_list|,
name|sparkPlan
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logCacheStatus
argument_list|(
name|leaf
argument_list|,
name|sparkPlan
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Leave reduceTran it will be expanded in the next line
if|if
condition|(
name|getParents
argument_list|(
name|leaf
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|0
operator|&&
operator|!
operator|(
name|leaf
operator|instanceof
name|ReduceTran
operator|)
condition|)
block|{
name|nextLeaf
operator|.
name|add
argument_list|(
name|leaf
argument_list|)
expr_stmt|;
block|}
block|}
name|sparkPlan
operator|.
name|append
argument_list|(
literal|" ) "
argument_list|)
expr_stmt|;
if|if
condition|(
name|nextLeaf
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
name|logLeafTran
argument_list|(
name|nextLeaf
argument_list|,
name|sparkPlan
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|nextLeaf
operator|.
name|size
argument_list|()
operator|!=
literal|0
condition|)
name|getSparkPlan
argument_list|(
name|nextLeaf
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|sparkPlan
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|logLeafTran
parameter_list|(
name|List
argument_list|<
name|SparkTran
argument_list|>
name|parent
parameter_list|,
name|StringBuilder
name|sparkPlan
parameter_list|)
block|{
name|sparkPlan
operator|.
name|append
argument_list|(
literal|"<-- "
argument_list|)
expr_stmt|;
name|boolean
name|isFirst
init|=
literal|true
decl_stmt|;
for|for
control|(
name|SparkTran
name|sparkTran
range|:
name|parent
control|)
block|{
name|List
argument_list|<
name|SparkTran
argument_list|>
name|parents
init|=
name|getParents
argument_list|(
name|sparkTran
argument_list|)
decl_stmt|;
name|SparkTran
name|leaf
init|=
name|parents
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|isFirst
condition|)
block|{
name|sparkPlan
operator|.
name|append
argument_list|(
literal|"( "
operator|+
name|leaf
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|leaf
operator|instanceof
name|ShuffleTran
condition|)
block|{
name|logShuffleTranStatus
argument_list|(
operator|(
name|ShuffleTran
operator|)
name|leaf
argument_list|,
name|sparkPlan
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logCacheStatus
argument_list|(
name|leaf
argument_list|,
name|sparkPlan
argument_list|)
expr_stmt|;
block|}
name|isFirst
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|sparkPlan
operator|.
name|append
argument_list|(
literal|","
operator|+
name|leaf
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|leaf
operator|instanceof
name|ShuffleTran
condition|)
block|{
name|logShuffleTranStatus
argument_list|(
operator|(
name|ShuffleTran
operator|)
name|leaf
argument_list|,
name|sparkPlan
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logCacheStatus
argument_list|(
name|leaf
argument_list|,
name|sparkPlan
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|sparkPlan
operator|.
name|append
argument_list|(
literal|" ) "
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|logShuffleTranStatus
parameter_list|(
name|ShuffleTran
name|leaf
parameter_list|,
name|StringBuilder
name|sparkPlan
parameter_list|)
block|{
name|int
name|noOfPartitions
init|=
name|leaf
operator|.
name|getNoOfPartitions
argument_list|()
decl_stmt|;
name|sparkPlan
operator|.
name|append
argument_list|(
literal|" ( Partitions "
operator|+
name|noOfPartitions
argument_list|)
expr_stmt|;
name|SparkShuffler
name|shuffler
init|=
name|leaf
operator|.
name|getShuffler
argument_list|()
decl_stmt|;
name|sparkPlan
operator|.
name|append
argument_list|(
literal|", "
operator|+
name|shuffler
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|leaf
operator|.
name|isCacheEnable
argument_list|()
condition|)
block|{
name|sparkPlan
operator|.
name|append
argument_list|(
literal|", Cache on"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sparkPlan
operator|.
name|append
argument_list|(
literal|", Cache off"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|logCacheStatus
parameter_list|(
name|SparkTran
name|sparkTran
parameter_list|,
name|StringBuilder
name|sparkPlan
parameter_list|)
block|{
if|if
condition|(
name|sparkTran
operator|.
name|isCacheEnable
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|sparkTran
operator|.
name|isCacheEnable
argument_list|()
operator|.
name|booleanValue
argument_list|()
condition|)
block|{
name|sparkPlan
operator|.
name|append
argument_list|(
literal|" (cache on) "
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sparkPlan
operator|.
name|append
argument_list|(
literal|" (cache off) "
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|addTran
parameter_list|(
name|SparkTran
name|tran
parameter_list|)
block|{
name|rootTrans
operator|.
name|add
argument_list|(
name|tran
argument_list|)
expr_stmt|;
name|leafTrans
operator|.
name|add
argument_list|(
name|tran
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addCachedRDDId
parameter_list|(
name|int
name|rddId
parameter_list|)
block|{
name|cachedRDDIds
operator|.
name|add
argument_list|(
name|rddId
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Set
argument_list|<
name|Integer
argument_list|>
name|getCachedRDDIds
parameter_list|()
block|{
return|return
name|cachedRDDIds
return|;
block|}
comment|/**    * This method returns a topologically sorted list of SparkTran.    */
specifier|private
name|List
argument_list|<
name|SparkTran
argument_list|>
name|getAllTrans
parameter_list|()
block|{
name|List
argument_list|<
name|SparkTran
argument_list|>
name|result
init|=
operator|new
name|LinkedList
argument_list|<
name|SparkTran
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|SparkTran
argument_list|>
name|seen
init|=
operator|new
name|HashSet
argument_list|<
name|SparkTran
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|SparkTran
name|leaf
range|:
name|leafTrans
control|)
block|{
comment|// make sure all leaves are visited at least once
name|visit
argument_list|(
name|leaf
argument_list|,
name|seen
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|private
name|void
name|visit
parameter_list|(
name|SparkTran
name|child
parameter_list|,
name|Set
argument_list|<
name|SparkTran
argument_list|>
name|seen
parameter_list|,
name|List
argument_list|<
name|SparkTran
argument_list|>
name|result
parameter_list|)
block|{
if|if
condition|(
name|seen
operator|.
name|contains
argument_list|(
name|child
argument_list|)
condition|)
block|{
comment|// don't visit multiple times
return|return;
block|}
name|seen
operator|.
name|add
argument_list|(
name|child
argument_list|)
expr_stmt|;
for|for
control|(
name|SparkTran
name|parent
range|:
name|getParents
argument_list|(
name|child
argument_list|)
control|)
block|{
if|if
condition|(
operator|!
name|seen
operator|.
name|contains
argument_list|(
name|parent
argument_list|)
condition|)
block|{
name|visit
argument_list|(
name|parent
argument_list|,
name|seen
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
block|}
name|result
operator|.
name|add
argument_list|(
name|child
argument_list|)
expr_stmt|;
block|}
comment|/**    * Connects the two SparkTrans in the graph.  Does not allow multiple connections    * between the same pair of SparkTrans.    * @param parent    * @param child    */
specifier|public
name|void
name|connect
parameter_list|(
name|SparkTran
name|parent
parameter_list|,
name|SparkTran
name|child
parameter_list|)
block|{
if|if
condition|(
name|getChildren
argument_list|(
name|parent
argument_list|)
operator|.
name|contains
argument_list|(
name|child
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Connection already exists"
argument_list|)
throw|;
block|}
name|rootTrans
operator|.
name|remove
argument_list|(
name|child
argument_list|)
expr_stmt|;
name|leafTrans
operator|.
name|remove
argument_list|(
name|parent
argument_list|)
expr_stmt|;
if|if
condition|(
name|transGraph
operator|.
name|get
argument_list|(
name|parent
argument_list|)
operator|==
literal|null
condition|)
block|{
name|transGraph
operator|.
name|put
argument_list|(
name|parent
argument_list|,
operator|new
name|LinkedList
argument_list|<
name|SparkTran
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|invertedTransGraph
operator|.
name|get
argument_list|(
name|child
argument_list|)
operator|==
literal|null
condition|)
block|{
name|invertedTransGraph
operator|.
name|put
argument_list|(
name|child
argument_list|,
operator|new
name|LinkedList
argument_list|<
name|SparkTran
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|transGraph
operator|.
name|get
argument_list|(
name|parent
argument_list|)
operator|.
name|add
argument_list|(
name|child
argument_list|)
expr_stmt|;
name|invertedTransGraph
operator|.
name|get
argument_list|(
name|child
argument_list|)
operator|.
name|add
argument_list|(
name|parent
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|SparkTran
argument_list|>
name|getParents
parameter_list|(
name|SparkTran
name|tran
parameter_list|)
block|{
if|if
condition|(
operator|!
name|invertedTransGraph
operator|.
name|containsKey
argument_list|(
name|tran
argument_list|)
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|SparkTran
argument_list|>
argument_list|()
return|;
block|}
return|return
name|invertedTransGraph
operator|.
name|get
argument_list|(
name|tran
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|SparkTran
argument_list|>
name|getChildren
parameter_list|(
name|SparkTran
name|tran
parameter_list|)
block|{
if|if
condition|(
operator|!
name|transGraph
operator|.
name|containsKey
argument_list|(
name|tran
argument_list|)
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|SparkTran
argument_list|>
argument_list|()
return|;
block|}
return|return
name|transGraph
operator|.
name|get
argument_list|(
name|tran
argument_list|)
return|;
block|}
block|}
end_class

end_unit

