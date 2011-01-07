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
name|index
operator|.
name|compact
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
name|Iterator
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
name|exec
operator|.
name|Utilities
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
name|HiveFileFormatUtils
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
name|HiveInputFormat
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
name|IOPrepareCache
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
name|PartitionDesc
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
name|SequenceFile
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
name|FileInputFormat
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
name|FileSplit
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
name|InputFormat
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
name|InputSplit
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

begin_class
specifier|public
class|class
name|HiveCompactIndexInputFormat
extends|extends
name|HiveInputFormat
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|l4j
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"HiveIndexInputFormat"
argument_list|)
decl_stmt|;
specifier|public
name|HiveCompactIndexInputFormat
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|InputSplit
index|[]
name|doGetSplits
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|int
name|numSplits
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|init
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|Path
index|[]
name|dirs
init|=
name|FileInputFormat
operator|.
name|getInputPaths
argument_list|(
name|job
argument_list|)
decl_stmt|;
if|if
condition|(
name|dirs
operator|.
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"No input paths specified in job"
argument_list|)
throw|;
block|}
name|JobConf
name|newjob
init|=
operator|new
name|JobConf
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|InputSplit
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|InputSplit
argument_list|>
argument_list|()
decl_stmt|;
comment|// for each dir, get the InputFormat, and do getSplits.
for|for
control|(
name|Path
name|dir
range|:
name|dirs
control|)
block|{
name|PartitionDesc
name|part
init|=
name|HiveFileFormatUtils
operator|.
name|getPartitionDescFromPathRecursively
argument_list|(
name|pathToPartitionInfo
argument_list|,
name|dir
argument_list|,
name|IOPrepareCache
operator|.
name|get
argument_list|()
operator|.
name|allocatePartitionDescMap
argument_list|()
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|// create a new InputFormat instance if this is the first time to see this
comment|// class
name|Class
name|inputFormatClass
init|=
name|part
operator|.
name|getInputFileFormatClass
argument_list|()
decl_stmt|;
name|InputFormat
name|inputFormat
init|=
name|getInputFormatFromCache
argument_list|(
name|inputFormatClass
argument_list|,
name|job
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|copyTableJobPropertiesToConf
argument_list|(
name|part
operator|.
name|getTableDesc
argument_list|()
argument_list|,
name|newjob
argument_list|)
expr_stmt|;
name|FileInputFormat
operator|.
name|setInputPaths
argument_list|(
name|newjob
argument_list|,
name|dir
argument_list|)
expr_stmt|;
name|newjob
operator|.
name|setInputFormat
argument_list|(
name|inputFormat
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|InputSplit
index|[]
name|iss
init|=
name|inputFormat
operator|.
name|getSplits
argument_list|(
name|newjob
argument_list|,
name|numSplits
operator|/
name|dirs
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|InputSplit
name|is
range|:
name|iss
control|)
block|{
name|result
operator|.
name|add
argument_list|(
operator|new
name|HiveInputSplit
argument_list|(
name|is
argument_list|,
name|inputFormatClass
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
operator|.
name|toArray
argument_list|(
operator|new
name|HiveInputSplit
index|[
name|result
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|InputSplit
index|[]
name|getSplits
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|int
name|numSplits
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|indexFileStr
init|=
name|job
operator|.
name|get
argument_list|(
literal|"hive.index.compact.file"
argument_list|)
decl_stmt|;
name|l4j
operator|.
name|info
argument_list|(
literal|"index_file is "
operator|+
name|indexFileStr
argument_list|)
expr_stmt|;
name|HiveCompactIndexResult
name|hiveIndexResult
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|indexFileStr
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|hiveIndexResult
operator|=
operator|new
name|HiveCompactIndexResult
argument_list|(
name|indexFileStr
argument_list|,
name|job
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|l4j
operator|.
name|error
argument_list|(
literal|"Unable to read index.."
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|inputFiles
init|=
name|hiveIndexResult
operator|.
name|buckets
operator|.
name|keySet
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|String
argument_list|>
name|iter
init|=
name|inputFiles
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
name|StringBuilder
name|newInputPaths
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|String
name|path
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|path
operator|.
name|trim
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
literal|""
argument_list|)
condition|)
continue|continue;
if|if
condition|(
operator|!
name|first
condition|)
block|{
name|newInputPaths
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|first
operator|=
literal|false
expr_stmt|;
block|}
name|newInputPaths
operator|.
name|append
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
name|FileInputFormat
operator|.
name|setInputPaths
argument_list|(
name|job
argument_list|,
name|newInputPaths
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
return|return
name|super
operator|.
name|getSplits
argument_list|(
name|job
argument_list|,
name|numSplits
argument_list|)
return|;
block|}
name|HiveInputSplit
index|[]
name|splits
init|=
operator|(
name|HiveInputSplit
index|[]
operator|)
name|this
operator|.
name|doGetSplits
argument_list|(
name|job
argument_list|,
name|numSplits
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|HiveInputSplit
argument_list|>
name|newSplits
init|=
operator|new
name|ArrayList
argument_list|<
name|HiveInputSplit
argument_list|>
argument_list|(
name|numSplits
argument_list|)
decl_stmt|;
for|for
control|(
name|HiveInputSplit
name|split
range|:
name|splits
control|)
block|{
name|l4j
operator|.
name|info
argument_list|(
literal|"split start : "
operator|+
name|split
operator|.
name|getStart
argument_list|()
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|info
argument_list|(
literal|"split end : "
operator|+
operator|(
name|split
operator|.
name|getStart
argument_list|()
operator|+
name|split
operator|.
name|getLength
argument_list|()
operator|)
argument_list|)
expr_stmt|;
try|try
block|{
if|if
condition|(
name|hiveIndexResult
operator|.
name|contains
argument_list|(
name|split
argument_list|)
condition|)
block|{
comment|// we may miss a sync here
name|HiveInputSplit
name|newSplit
init|=
name|split
decl_stmt|;
if|if
condition|(
name|split
operator|.
name|inputFormatClassName
argument_list|()
operator|.
name|contains
argument_list|(
literal|"RCFile"
argument_list|)
operator|||
name|split
operator|.
name|inputFormatClassName
argument_list|()
operator|.
name|contains
argument_list|(
literal|"SequenceFile"
argument_list|)
condition|)
block|{
if|if
condition|(
name|split
operator|.
name|getStart
argument_list|()
operator|>
name|SequenceFile
operator|.
name|SYNC_INTERVAL
condition|)
block|{
name|newSplit
operator|=
operator|new
name|HiveInputSplit
argument_list|(
operator|new
name|FileSplit
argument_list|(
name|split
operator|.
name|getPath
argument_list|()
argument_list|,
name|split
operator|.
name|getStart
argument_list|()
operator|-
name|SequenceFile
operator|.
name|SYNC_INTERVAL
argument_list|,
name|split
operator|.
name|getLength
argument_list|()
operator|+
name|SequenceFile
operator|.
name|SYNC_INTERVAL
argument_list|,
name|split
operator|.
name|getLocations
argument_list|()
argument_list|)
argument_list|,
name|split
operator|.
name|inputFormatClassName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|newSplits
operator|.
name|add
argument_list|(
name|newSplit
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unable to get metadata for input table split"
operator|+
name|split
operator|.
name|getPath
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|InputSplit
name|retA
index|[]
init|=
name|newSplits
operator|.
name|toArray
argument_list|(
operator|(
operator|new
name|FileSplit
index|[
name|newSplits
operator|.
name|size
argument_list|()
index|]
operator|)
argument_list|)
decl_stmt|;
name|l4j
operator|.
name|info
argument_list|(
literal|"Number of input splits: "
operator|+
name|splits
operator|.
name|length
operator|+
literal|" new input splits: "
operator|+
name|retA
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|retA
return|;
block|}
block|}
end_class

end_unit

