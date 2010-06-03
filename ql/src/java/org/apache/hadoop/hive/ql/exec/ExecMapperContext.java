begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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
name|Serializable
import|;
end_import

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
name|MapredLocalWork
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
name|MapredLocalWork
operator|.
name|BucketMapJoinContext
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
name|InspectableObject
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
name|mapred
operator|.
name|JobConf
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_class
specifier|public
class|class
name|ExecMapperContext
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|l4j
init|=
name|ExecMapper
operator|.
name|l4j
decl_stmt|;
comment|// lastInputFile should be changed by the root of the operator tree ExecMapper.map()
comment|// but kept unchanged throughout the operator tree for one row
specifier|private
name|String
name|lastInputFile
init|=
literal|null
decl_stmt|;
comment|// currentInputFile will be updated only by inputFileChanged(). If inputFileChanged()
comment|// is not called throughout the opertor tree, currentInputFile won't be used anyways
comment|// so it won't be updated.
specifier|private
name|String
name|currentInputFile
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|inputFileChecked
init|=
literal|false
decl_stmt|;
specifier|private
name|Integer
name|fileId
init|=
operator|new
name|Integer
argument_list|(
operator|-
literal|1
argument_list|)
decl_stmt|;
specifier|private
name|MapredLocalWork
name|localWork
init|=
literal|null
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|FetchOperator
argument_list|>
name|fetchOperators
decl_stmt|;
specifier|private
name|JobConf
name|jc
decl_stmt|;
specifier|public
name|ExecMapperContext
parameter_list|()
block|{   }
specifier|public
name|void
name|processInputFileChangeForLocalWork
parameter_list|()
throws|throws
name|HiveException
block|{
comment|// put inputFileChanged() after localWork check
if|if
condition|(
name|this
operator|.
name|localWork
operator|!=
literal|null
operator|&&
name|inputFileChanged
argument_list|()
condition|)
block|{
name|processMapLocalWork
argument_list|(
name|localWork
operator|.
name|getInputFileChangeSensitive
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * For CompbineFileInputFormat, the mapper's input file will be changed on the    * fly, and the input file name is passed to jobConf by shims/initNextRecordReader.    * If the map local work has any mapping depending on the current    * mapper's input file, the work need to clear context and re-initialization    * after the input file changed. This is first introduced to process bucket    * map join.    *    * @return    */
specifier|public
name|boolean
name|inputFileChanged
parameter_list|()
block|{
if|if
condition|(
operator|!
name|inputFileChecked
condition|)
block|{
name|currentInputFile
operator|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|jc
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPMAPFILENAME
argument_list|)
expr_stmt|;
name|inputFileChecked
operator|=
literal|true
expr_stmt|;
block|}
return|return
name|lastInputFile
operator|==
literal|null
operator|||
operator|!
name|lastInputFile
operator|.
name|equals
argument_list|(
name|currentInputFile
argument_list|)
return|;
block|}
comment|/**    * Reset the execution context for each new row. This function should be called only    * once at the root of the operator tree -- ExecMapper.map().    * Note: this function should be kept minimum since it is called for each input row.    */
specifier|public
name|void
name|resetRow
parameter_list|()
block|{
comment|// Update the lastInputFile with the currentInputFile.
name|lastInputFile
operator|=
name|currentInputFile
expr_stmt|;
name|inputFileChecked
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|String
name|getLastInputFile
parameter_list|()
block|{
return|return
name|lastInputFile
return|;
block|}
specifier|public
name|void
name|setLastInputFile
parameter_list|(
name|String
name|lastInputFile
parameter_list|)
block|{
name|this
operator|.
name|lastInputFile
operator|=
name|lastInputFile
expr_stmt|;
block|}
specifier|private
name|void
name|processMapLocalWork
parameter_list|(
name|boolean
name|inputFileChangeSenstive
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// process map local operators
if|if
condition|(
name|fetchOperators
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|int
name|fetchOpNum
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|FetchOperator
argument_list|>
name|entry
range|:
name|fetchOperators
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|int
name|fetchOpRows
init|=
literal|0
decl_stmt|;
name|String
name|alias
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|FetchOperator
name|fetchOp
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|inputFileChangeSenstive
condition|)
block|{
name|fetchOp
operator|.
name|clearFetchContext
argument_list|()
expr_stmt|;
name|setUpFetchOpContext
argument_list|(
name|fetchOp
argument_list|,
name|alias
argument_list|)
expr_stmt|;
block|}
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|forwardOp
init|=
name|localWork
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|InspectableObject
name|row
init|=
name|fetchOp
operator|.
name|getNextRow
argument_list|()
decl_stmt|;
if|if
condition|(
name|row
operator|==
literal|null
condition|)
block|{
name|forwardOp
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
break|break;
block|}
name|fetchOpRows
operator|++
expr_stmt|;
name|forwardOp
operator|.
name|process
argument_list|(
name|row
operator|.
name|o
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// check if any operator had a fatal error or early exit during
comment|// execution
if|if
condition|(
name|forwardOp
operator|.
name|getDone
argument_list|()
condition|)
block|{
name|ExecMapper
operator|.
name|setDone
argument_list|(
literal|true
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|l4j
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|l4j
operator|.
name|info
argument_list|(
literal|"fetch "
operator|+
name|fetchOpNum
operator|++
operator|+
literal|" processed "
operator|+
name|fetchOpRows
operator|+
literal|" used mem: "
operator|+
name|ExecMapper
operator|.
name|memoryMXBean
operator|.
name|getHeapMemoryUsage
argument_list|()
operator|.
name|getUsed
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|OutOfMemoryError
condition|)
block|{
comment|// Don't create a new object if we are already out of memory
throw|throw
operator|(
name|OutOfMemoryError
operator|)
name|e
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Hive Runtime Error: Map local work failed"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|setUpFetchOpContext
parameter_list|(
name|FetchOperator
name|fetchOp
parameter_list|,
name|String
name|alias
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|currentInputFile
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|jc
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPMAPFILENAME
argument_list|)
decl_stmt|;
name|BucketMapJoinContext
name|bucketMatcherCxt
init|=
name|this
operator|.
name|localWork
operator|.
name|getBucketMapjoinContext
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|BucketMatcher
argument_list|>
name|bucketMatcherCls
init|=
name|bucketMatcherCxt
operator|.
name|getBucketMatcherClass
argument_list|()
decl_stmt|;
name|BucketMatcher
name|bucketMatcher
init|=
operator|(
name|BucketMatcher
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|bucketMatcherCls
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|bucketMatcher
operator|.
name|setAliasBucketFileNameMapping
argument_list|(
name|bucketMatcherCxt
operator|.
name|getAliasBucketFileNameMapping
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|aliasFiles
init|=
name|bucketMatcher
operator|.
name|getAliasBucketFiles
argument_list|(
name|currentInputFile
argument_list|,
name|bucketMatcherCxt
operator|.
name|getMapJoinBigTableAlias
argument_list|()
argument_list|,
name|alias
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|Path
argument_list|>
name|iter
init|=
name|aliasFiles
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|fetchOp
operator|.
name|setupContext
argument_list|(
name|iter
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getCurrentInputFile
parameter_list|()
block|{
return|return
name|currentInputFile
return|;
block|}
specifier|public
name|void
name|setCurrentInputFile
parameter_list|(
name|String
name|currentInputFile
parameter_list|)
block|{
name|this
operator|.
name|currentInputFile
operator|=
name|currentInputFile
expr_stmt|;
block|}
specifier|public
name|JobConf
name|getJc
parameter_list|()
block|{
return|return
name|jc
return|;
block|}
specifier|public
name|void
name|setJc
parameter_list|(
name|JobConf
name|jc
parameter_list|)
block|{
name|this
operator|.
name|jc
operator|=
name|jc
expr_stmt|;
block|}
specifier|public
name|MapredLocalWork
name|getLocalWork
parameter_list|()
block|{
return|return
name|localWork
return|;
block|}
specifier|public
name|void
name|setLocalWork
parameter_list|(
name|MapredLocalWork
name|localWork
parameter_list|)
block|{
name|this
operator|.
name|localWork
operator|=
name|localWork
expr_stmt|;
block|}
specifier|public
name|Integer
name|getFileId
parameter_list|()
block|{
return|return
name|fileId
return|;
block|}
specifier|public
name|void
name|setFileId
parameter_list|(
name|Integer
name|fileId
parameter_list|)
block|{
name|this
operator|.
name|fileId
operator|=
name|fileId
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|FetchOperator
argument_list|>
name|getFetchOperators
parameter_list|()
block|{
return|return
name|fetchOperators
return|;
block|}
specifier|public
name|void
name|setFetchOperators
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|FetchOperator
argument_list|>
name|fetchOperators
parameter_list|)
block|{
name|this
operator|.
name|fetchOperators
operator|=
name|fetchOperators
expr_stmt|;
block|}
block|}
end_class

end_unit

