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
name|exec
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
name|List
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
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|fs
operator|.
name|FileStatus
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|HiveException
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
name|JoinDesc
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
name|api
operator|.
name|OperatorType
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
name|serde2
operator|.
name|SerDeUtils
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
name|serde2
operator|.
name|objectinspector
operator|.
name|StandardStructObjectInspector
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
name|serde2
operator|.
name|objectinspector
operator|.
name|StructField
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
name|serde2
operator|.
name|objectinspector
operator|.
name|StructObjectInspector
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
name|LongWritable
import|;
end_import

begin_comment
comment|/**  * Join operator implementation.  */
end_comment

begin_class
specifier|public
class|class
name|JoinOperator
extends|extends
name|CommonJoinOperator
argument_list|<
name|JoinDesc
argument_list|>
implements|implements
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
specifier|private
specifier|transient
name|SkewJoinHandler
name|skewJoinKeyContext
init|=
literal|null
decl_stmt|;
comment|/**    * SkewkeyTableCounter.    *    */
specifier|public
specifier|static
enum|enum
name|SkewkeyTableCounter
block|{
name|SKEWJOINFOLLOWUPJOBS
block|}
specifier|private
specifier|final
specifier|transient
name|LongWritable
name|skewjoin_followup_jobs
init|=
operator|new
name|LongWritable
argument_list|(
literal|0
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|initializeOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|initializeOp
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|initializeChildren
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
if|if
condition|(
name|handleSkewJoin
condition|)
block|{
name|skewJoinKeyContext
operator|=
operator|new
name|SkewJoinHandler
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|skewJoinKeyContext
operator|.
name|initiliaze
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|skewJoinKeyContext
operator|.
name|setSkewJoinJobCounter
argument_list|(
name|skewjoin_followup_jobs
argument_list|)
expr_stmt|;
block|}
name|statsMap
operator|.
name|put
argument_list|(
name|SkewkeyTableCounter
operator|.
name|SKEWJOINFOLLOWUPJOBS
argument_list|,
name|skewjoin_followup_jobs
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|processOp
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|reportProgress
argument_list|()
expr_stmt|;
comment|// get alias
name|alias
operator|=
operator|(
name|byte
operator|)
name|tag
expr_stmt|;
if|if
condition|(
operator|(
name|lastAlias
operator|==
literal|null
operator|)
operator|||
operator|(
operator|!
name|lastAlias
operator|.
name|equals
argument_list|(
name|alias
argument_list|)
operator|)
condition|)
block|{
name|nextSz
operator|=
name|joinEmitInterval
expr_stmt|;
block|}
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|nr
init|=
name|getFilteredValue
argument_list|(
name|alias
argument_list|,
name|row
argument_list|)
decl_stmt|;
if|if
condition|(
name|handleSkewJoin
condition|)
block|{
name|skewJoinKeyContext
operator|.
name|handleSkew
argument_list|(
name|tag
argument_list|)
expr_stmt|;
block|}
comment|// number of rows for the key in the given table
name|int
name|sz
init|=
name|storage
index|[
name|alias
index|]
operator|.
name|size
argument_list|()
decl_stmt|;
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|inputObjInspectors
index|[
name|tag
index|]
decl_stmt|;
name|StructField
name|sf
init|=
name|soi
operator|.
name|getStructFieldRef
argument_list|(
name|Utilities
operator|.
name|ReduceField
operator|.
name|KEY
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|List
name|keyObject
init|=
operator|(
name|List
operator|)
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|row
argument_list|,
name|sf
argument_list|)
decl_stmt|;
comment|// Are we consuming too much memory
if|if
condition|(
name|alias
operator|==
name|numAliases
operator|-
literal|1
operator|&&
operator|!
operator|(
name|handleSkewJoin
operator|&&
name|skewJoinKeyContext
operator|.
name|currBigKeyTag
operator|>=
literal|0
operator|)
operator|&&
operator|!
name|hasLeftSemiJoin
condition|)
block|{
if|if
condition|(
name|sz
operator|==
name|joinEmitInterval
condition|)
block|{
comment|// The input is sorted by alias, so if we are already in the last join
comment|// operand,
comment|// we can emit some results now.
comment|// Note this has to be done before adding the current row to the
comment|// storage,
comment|// to preserve the correctness for outer joins.
name|checkAndGenObject
argument_list|()
expr_stmt|;
name|storage
index|[
name|alias
index|]
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|sz
operator|==
name|nextSz
condition|)
block|{
comment|// Print a message if we reached at least 1000 rows for a join operand
comment|// We won't print a message for the last join operand since the size
comment|// will never goes to joinEmitInterval.
name|LOG
operator|.
name|info
argument_list|(
literal|"table "
operator|+
name|alias
operator|+
literal|" has "
operator|+
name|sz
operator|+
literal|" rows for join key "
operator|+
name|keyObject
argument_list|)
expr_stmt|;
name|nextSz
operator|=
name|getNextSize
argument_list|(
name|nextSz
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Add the value to the vector
comment|// if join-key is null, process each row in different group.
name|StandardStructObjectInspector
name|inspector
init|=
operator|(
name|StandardStructObjectInspector
operator|)
name|sf
operator|.
name|getFieldObjectInspector
argument_list|()
decl_stmt|;
if|if
condition|(
name|SerDeUtils
operator|.
name|hasAnyNullObject
argument_list|(
name|keyObject
argument_list|,
name|inspector
argument_list|,
name|nullsafes
argument_list|)
condition|)
block|{
name|endGroup
argument_list|()
expr_stmt|;
name|startGroup
argument_list|()
expr_stmt|;
block|}
name|storage
index|[
name|alias
index|]
operator|.
name|add
argument_list|(
name|nr
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|OperatorType
name|getType
parameter_list|()
block|{
return|return
name|OperatorType
operator|.
name|JOIN
return|;
block|}
comment|/**    * All done.    *    */
annotation|@
name|Override
specifier|public
name|void
name|closeOp
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|handleSkewJoin
condition|)
block|{
name|skewJoinKeyContext
operator|.
name|close
argument_list|(
name|abort
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|closeOp
argument_list|(
name|abort
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|jobCloseOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|,
name|boolean
name|success
parameter_list|,
name|JobCloseFeedBack
name|feedBack
parameter_list|)
throws|throws
name|HiveException
block|{
name|int
name|numAliases
init|=
name|conf
operator|.
name|getExprs
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|conf
operator|.
name|getHandleSkewJoin
argument_list|()
condition|)
block|{
try|try
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numAliases
condition|;
name|i
operator|++
control|)
block|{
name|String
name|specPath
init|=
name|conf
operator|.
name|getBigKeysDirMap
argument_list|()
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|)
decl_stmt|;
name|mvFileToFinalPath
argument_list|(
name|specPath
argument_list|,
name|hconf
argument_list|,
name|success
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numAliases
condition|;
name|j
operator|++
control|)
block|{
if|if
condition|(
name|j
operator|==
name|i
condition|)
block|{
continue|continue;
block|}
name|specPath
operator|=
name|getConf
argument_list|()
operator|.
name|getSmallKeysDirMap
argument_list|()
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|)
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|j
argument_list|)
expr_stmt|;
name|mvFileToFinalPath
argument_list|(
name|specPath
argument_list|,
name|hconf
argument_list|,
name|success
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|success
condition|)
block|{
comment|// move up files
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numAliases
condition|;
name|i
operator|++
control|)
block|{
name|String
name|specPath
init|=
name|conf
operator|.
name|getBigKeysDirMap
argument_list|()
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|)
decl_stmt|;
name|moveUpFiles
argument_list|(
name|specPath
argument_list|,
name|hconf
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numAliases
condition|;
name|j
operator|++
control|)
block|{
if|if
condition|(
name|j
operator|==
name|i
condition|)
block|{
continue|continue;
block|}
name|specPath
operator|=
name|getConf
argument_list|()
operator|.
name|getSmallKeysDirMap
argument_list|()
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|)
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|j
argument_list|)
expr_stmt|;
name|moveUpFiles
argument_list|(
name|specPath
argument_list|,
name|hconf
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
name|super
operator|.
name|jobCloseOp
argument_list|(
name|hconf
argument_list|,
name|success
argument_list|,
name|feedBack
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|moveUpFiles
parameter_list|(
name|String
name|specPath
parameter_list|,
name|Configuration
name|hconf
parameter_list|,
name|Log
name|log
parameter_list|)
throws|throws
name|IOException
throws|,
name|HiveException
block|{
name|FileSystem
name|fs
init|=
operator|(
operator|new
name|Path
argument_list|(
name|specPath
argument_list|)
operator|)
operator|.
name|getFileSystem
argument_list|(
name|hconf
argument_list|)
decl_stmt|;
name|Path
name|finalPath
init|=
operator|new
name|Path
argument_list|(
name|specPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|finalPath
argument_list|)
condition|)
block|{
name|FileStatus
index|[]
name|taskOutputDirs
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|finalPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|taskOutputDirs
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|FileStatus
name|dir
range|:
name|taskOutputDirs
control|)
block|{
name|Utilities
operator|.
name|renameOrMoveFiles
argument_list|(
name|fs
argument_list|,
name|dir
operator|.
name|getPath
argument_list|()
argument_list|,
name|finalPath
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|dir
operator|.
name|getPath
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * This is a similar implementation of FileSinkOperator.moveFileToFinalPath.    * @param specPath    * @param hconf    * @param success    * @param log    * @param dpCtx    * @throws IOException    * @throws HiveException    */
specifier|private
name|void
name|mvFileToFinalPath
parameter_list|(
name|String
name|specPath
parameter_list|,
name|Configuration
name|hconf
parameter_list|,
name|boolean
name|success
parameter_list|,
name|Log
name|log
parameter_list|)
throws|throws
name|IOException
throws|,
name|HiveException
block|{
name|FileSystem
name|fs
init|=
operator|(
operator|new
name|Path
argument_list|(
name|specPath
argument_list|)
operator|)
operator|.
name|getFileSystem
argument_list|(
name|hconf
argument_list|)
decl_stmt|;
name|Path
name|tmpPath
init|=
name|Utilities
operator|.
name|toTempPath
argument_list|(
name|specPath
argument_list|)
decl_stmt|;
name|Path
name|intermediatePath
init|=
operator|new
name|Path
argument_list|(
name|tmpPath
operator|.
name|getParent
argument_list|()
argument_list|,
name|tmpPath
operator|.
name|getName
argument_list|()
operator|+
literal|".intermediate"
argument_list|)
decl_stmt|;
name|Path
name|finalPath
init|=
operator|new
name|Path
argument_list|(
name|specPath
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|emptyBuckets
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|success
condition|)
block|{
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|tmpPath
argument_list|)
condition|)
block|{
comment|// Step1: rename tmp output folder to intermediate path. After this
comment|// point, updates from speculative tasks still writing to tmpPath
comment|// will not appear in finalPath.
name|log
operator|.
name|info
argument_list|(
literal|"Moving tmp dir: "
operator|+
name|tmpPath
operator|+
literal|" to: "
operator|+
name|intermediatePath
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|rename
argument_list|(
name|fs
argument_list|,
name|tmpPath
argument_list|,
name|intermediatePath
argument_list|)
expr_stmt|;
comment|// Step2: remove any tmp file or double-committed output files
name|Utilities
operator|.
name|removeTempOrDuplicateFiles
argument_list|(
name|fs
argument_list|,
name|intermediatePath
argument_list|)
expr_stmt|;
comment|// Step3: move to the file destination
name|log
operator|.
name|info
argument_list|(
literal|"Moving tmp dir: "
operator|+
name|intermediatePath
operator|+
literal|" to: "
operator|+
name|finalPath
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|renameOrMoveFiles
argument_list|(
name|fs
argument_list|,
name|intermediatePath
argument_list|,
name|finalPath
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|fs
operator|.
name|delete
argument_list|(
name|tmpPath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Forward a record of join results.    *    * @throws HiveException    */
annotation|@
name|Override
specifier|public
name|void
name|endGroup
parameter_list|()
throws|throws
name|HiveException
block|{
comment|// if this is a skew key, we need to handle it in a separate map reduce job.
if|if
condition|(
name|handleSkewJoin
operator|&&
name|skewJoinKeyContext
operator|.
name|currBigKeyTag
operator|>=
literal|0
condition|)
block|{
try|try
block|{
name|skewJoinKeyContext
operator|.
name|endGroup
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return;
block|}
else|else
block|{
name|checkAndGenObject
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|supportSkewJoinOptimization
parameter_list|()
block|{
comment|// Since skew join optimization makes a copy of the tree above joins, and
comment|// there is no multi-query optimization in place, let us not use skew join
comment|// optimizations for now.
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|opAllowedBeforeSortMergeJoin
parameter_list|()
block|{
comment|// If a join occurs before the sort-merge join, it is not useful to convert the the sort-merge
comment|// join to a mapjoin. It might be simpler to perform the join and then a sort-merge join
comment|// join. By converting the sort-merge join to a map-join, the job will be executed in 2
comment|// mapjoins in the best case. The number of inputs for the join is more than 1 so it would
comment|// be difficult to figure out the big table for the mapjoin.
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

