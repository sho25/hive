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
name|io
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
name|Random
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|serde2
operator|.
name|ColumnProjectionUtils
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
name|columnar
operator|.
name|BytesRefArrayWritable
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
name|columnar
operator|.
name|BytesRefWritable
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
name|io
operator|.
name|ByteWritable
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
name|io
operator|.
name|SequenceFile
operator|.
name|CompressionType
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
name|compress
operator|.
name|CompressionCodec
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
name|compress
operator|.
name|DefaultCodec
import|;
end_import

begin_class
specifier|public
class|class
name|PerformTestRCFileAndSeqFile
extends|extends
name|TestCase
block|{
specifier|private
specifier|final
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
specifier|private
name|Path
name|testRCFile
decl_stmt|;
specifier|private
name|Path
name|testSeqFile
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
name|int
name|columnMaxSize
init|=
literal|30
decl_stmt|;
name|Random
name|randomCharGenerator
init|=
operator|new
name|Random
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|Random
name|randColLenGenerator
init|=
operator|new
name|Random
argument_list|(
literal|20
argument_list|)
decl_stmt|;
specifier|public
name|PerformTestRCFileAndSeqFile
parameter_list|(
name|boolean
name|local
parameter_list|,
name|String
name|file
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|local
condition|)
block|{
name|fs
operator|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
name|conf
operator|.
name|setInt
argument_list|(
name|RCFile
operator|.
name|Writer
operator|.
name|COLUMNS_BUFFER_SIZE_CONF_STR
argument_list|,
literal|1
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
if|if
condition|(
name|file
operator|==
literal|null
condition|)
block|{
name|Path
name|dir
init|=
operator|new
name|Path
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.data.dir"
argument_list|,
literal|"."
argument_list|)
operator|+
literal|"/mapred"
argument_list|)
decl_stmt|;
name|testRCFile
operator|=
operator|new
name|Path
argument_list|(
name|dir
argument_list|,
literal|"test_rcfile"
argument_list|)
expr_stmt|;
name|testSeqFile
operator|=
operator|new
name|Path
argument_list|(
name|dir
argument_list|,
literal|"test_seqfile"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|testRCFile
operator|=
operator|new
name|Path
argument_list|(
name|file
operator|+
literal|"-rcfile"
argument_list|)
expr_stmt|;
name|testSeqFile
operator|=
operator|new
name|Path
argument_list|(
name|file
operator|+
literal|"-seqfile"
argument_list|)
expr_stmt|;
block|}
name|fs
operator|.
name|delete
argument_list|(
name|testRCFile
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|testSeqFile
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"RCFile:"
operator|+
name|testRCFile
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"SequenceFile:"
operator|+
name|testSeqFile
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|writeSeqenceFileTest
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|int
name|rowCount
parameter_list|,
name|Path
name|file
parameter_list|,
name|int
name|columnNum
parameter_list|,
name|CompressionCodec
name|codec
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
index|[]
name|columnRandom
decl_stmt|;
name|resetRandomGenerators
argument_list|()
expr_stmt|;
name|BytesRefArrayWritable
name|bytes
init|=
operator|new
name|BytesRefArrayWritable
argument_list|(
name|columnNum
argument_list|)
decl_stmt|;
name|columnRandom
operator|=
operator|new
name|byte
index|[
name|columnNum
index|]
index|[]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|columnNum
condition|;
name|i
operator|++
control|)
block|{
name|BytesRefWritable
name|cu
init|=
operator|new
name|BytesRefWritable
argument_list|()
decl_stmt|;
name|bytes
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|cu
argument_list|)
expr_stmt|;
block|}
comment|// zero length key is not allowed by block compress writer, so we use a byte
comment|// writable
name|ByteWritable
name|key
init|=
operator|new
name|ByteWritable
argument_list|()
decl_stmt|;
name|SequenceFile
operator|.
name|Writer
name|seqWriter
init|=
name|SequenceFile
operator|.
name|createWriter
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|file
argument_list|,
name|ByteWritable
operator|.
name|class
argument_list|,
name|BytesRefArrayWritable
operator|.
name|class
argument_list|,
name|CompressionType
operator|.
name|BLOCK
argument_list|,
name|codec
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|rowCount
condition|;
name|i
operator|++
control|)
block|{
name|nextRandomRow
argument_list|(
name|columnRandom
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
name|seqWriter
operator|.
name|append
argument_list|(
name|key
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
block|}
name|seqWriter
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|resetRandomGenerators
parameter_list|()
block|{
name|randomCharGenerator
operator|=
operator|new
name|Random
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|randColLenGenerator
operator|=
operator|new
name|Random
argument_list|(
literal|20
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|writeRCFileTest
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|int
name|rowCount
parameter_list|,
name|Path
name|file
parameter_list|,
name|int
name|columnNum
parameter_list|,
name|CompressionCodec
name|codec
parameter_list|)
throws|throws
name|IOException
block|{
name|fs
operator|.
name|delete
argument_list|(
name|file
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|resetRandomGenerators
argument_list|()
expr_stmt|;
name|RCFileOutputFormat
operator|.
name|setColumnNumber
argument_list|(
name|conf
argument_list|,
name|columnNum
argument_list|)
expr_stmt|;
name|RCFile
operator|.
name|Writer
name|writer
init|=
operator|new
name|RCFile
operator|.
name|Writer
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|file
argument_list|,
literal|null
argument_list|,
name|codec
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|columnRandom
decl_stmt|;
name|BytesRefArrayWritable
name|bytes
init|=
operator|new
name|BytesRefArrayWritable
argument_list|(
name|columnNum
argument_list|)
decl_stmt|;
name|columnRandom
operator|=
operator|new
name|byte
index|[
name|columnNum
index|]
index|[]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|columnNum
condition|;
name|i
operator|++
control|)
block|{
name|BytesRefWritable
name|cu
init|=
operator|new
name|BytesRefWritable
argument_list|()
decl_stmt|;
name|bytes
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|cu
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|rowCount
condition|;
name|i
operator|++
control|)
block|{
name|nextRandomRow
argument_list|(
name|columnRandom
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|nextRandomRow
parameter_list|(
name|byte
index|[]
index|[]
name|row
parameter_list|,
name|BytesRefArrayWritable
name|bytes
parameter_list|)
block|{
name|bytes
operator|.
name|resetValid
argument_list|(
name|row
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|row
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|int
name|len
init|=
name|Math
operator|.
name|abs
argument_list|(
name|randColLenGenerator
operator|.
name|nextInt
argument_list|(
name|columnMaxSize
argument_list|)
argument_list|)
decl_stmt|;
name|row
index|[
name|i
index|]
operator|=
operator|new
name|byte
index|[
name|len
index|]
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
name|len
condition|;
name|j
operator|++
control|)
block|{
name|row
index|[
name|i
index|]
index|[
name|j
index|]
operator|=
name|getRandomChar
argument_list|(
name|randomCharGenerator
argument_list|)
expr_stmt|;
block|}
name|bytes
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|set
argument_list|(
name|row
index|[
name|i
index|]
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|int
name|CHAR_END
init|=
literal|122
operator|-
literal|7
decl_stmt|;
specifier|private
name|byte
name|getRandomChar
parameter_list|(
name|Random
name|random
parameter_list|)
block|{
name|byte
name|b
init|=
literal|0
decl_stmt|;
do|do
block|{
name|b
operator|=
operator|(
name|byte
operator|)
name|random
operator|.
name|nextInt
argument_list|(
name|CHAR_END
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
operator|(
name|b
operator|<
literal|65
operator|)
condition|)
do|;
if|if
condition|(
name|b
operator|>
literal|90
condition|)
block|{
name|b
operator|+=
literal|7
expr_stmt|;
block|}
return|return
name|b
return|;
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
name|Exception
block|{
name|int
name|count
init|=
literal|1000
decl_stmt|;
name|String
name|file
init|=
literal|null
decl_stmt|;
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
name|args
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
comment|// parse command line
if|if
condition|(
name|args
index|[
name|i
index|]
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
elseif|else
if|if
condition|(
name|args
index|[
name|i
index|]
operator|.
name|equals
argument_list|(
literal|"-count"
argument_list|)
condition|)
block|{
name|count
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// file is required parameter
name|file
operator|=
name|args
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
comment|// change it to choose the appropriate file system
name|boolean
name|isLocalFS
init|=
literal|true
decl_stmt|;
name|PerformTestRCFileAndSeqFile
name|testcase
init|=
operator|new
name|PerformTestRCFileAndSeqFile
argument_list|(
name|isLocalFS
argument_list|,
name|file
argument_list|)
decl_stmt|;
comment|// change these parameters
name|boolean
name|checkCorrect
init|=
literal|true
decl_stmt|;
name|CompressionCodec
name|codec
init|=
operator|new
name|DefaultCodec
argument_list|()
decl_stmt|;
name|testcase
operator|.
name|columnMaxSize
operator|=
literal|30
expr_stmt|;
comment|// testcase.testWithColumnNumber(count, 2, checkCorrect, codec);
comment|// testcase.testWithColumnNumber(count, 10, checkCorrect, codec);
comment|// testcase.testWithColumnNumber(count, 25, checkCorrect, codec);
name|testcase
operator|.
name|testWithColumnNumber
argument_list|(
name|count
argument_list|,
literal|40
argument_list|,
name|checkCorrect
argument_list|,
name|codec
argument_list|)
expr_stmt|;
comment|// testcase.testWithColumnNumber(count, 50, checkCorrect, codec);
comment|// testcase.testWithColumnNumber(count, 80, checkCorrect, codec);
block|}
finally|finally
block|{     }
block|}
specifier|private
name|void
name|testWithColumnNumber
parameter_list|(
name|int
name|rowCount
parameter_list|,
name|int
name|columnNum
parameter_list|,
name|boolean
name|checkCorrect
parameter_list|,
name|CompressionCodec
name|codec
parameter_list|)
throws|throws
name|IOException
block|{
comment|// rcfile
comment|// rcfile write
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|writeRCFileTest
argument_list|(
name|fs
argument_list|,
name|rowCount
argument_list|,
name|testRCFile
argument_list|,
name|columnNum
argument_list|,
name|codec
argument_list|)
expr_stmt|;
name|long
name|cost
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
decl_stmt|;
name|long
name|fileLen
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|testRCFile
argument_list|)
operator|.
name|getLen
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Write RCFile with "
operator|+
name|columnNum
operator|+
literal|" random string columns and "
operator|+
name|rowCount
operator|+
literal|" rows cost "
operator|+
name|cost
operator|+
literal|" milliseconds. And the file's on disk size is "
operator|+
name|fileLen
argument_list|)
expr_stmt|;
comment|// sequence file write
name|start
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|writeSeqenceFileTest
argument_list|(
name|fs
argument_list|,
name|rowCount
argument_list|,
name|testSeqFile
argument_list|,
name|columnNum
argument_list|,
name|codec
argument_list|)
expr_stmt|;
name|cost
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
expr_stmt|;
name|fileLen
operator|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|testSeqFile
argument_list|)
operator|.
name|getLen
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Write SequenceFile with "
operator|+
name|columnNum
operator|+
literal|" random string columns and "
operator|+
name|rowCount
operator|+
literal|" rows cost "
operator|+
name|cost
operator|+
literal|" milliseconds. And the file's on disk size is "
operator|+
name|fileLen
argument_list|)
expr_stmt|;
comment|// rcfile read
name|start
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|int
name|readRows
init|=
name|performRCFileReadFirstColumnTest
argument_list|(
name|fs
argument_list|,
name|testRCFile
argument_list|,
name|columnNum
argument_list|,
name|checkCorrect
argument_list|)
decl_stmt|;
name|cost
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Read only one column of a RCFile with "
operator|+
name|columnNum
operator|+
literal|" random string columns and "
operator|+
name|rowCount
operator|+
literal|" rows cost "
operator|+
name|cost
operator|+
literal|" milliseconds."
argument_list|)
expr_stmt|;
if|if
condition|(
name|rowCount
operator|!=
name|readRows
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Compare read and write row count error."
argument_list|)
throw|;
block|}
name|assertEquals
argument_list|(
literal|""
argument_list|,
name|rowCount
argument_list|,
name|readRows
argument_list|)
expr_stmt|;
if|if
condition|(
name|isLocalFileSystem
argument_list|()
operator|&&
operator|!
name|checkCorrect
condition|)
block|{
comment|// make some noisy to avoid disk caches data.
name|performSequenceFileRead
argument_list|(
name|fs
argument_list|,
name|rowCount
argument_list|,
name|testSeqFile
argument_list|)
expr_stmt|;
block|}
name|start
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|readRows
operator|=
name|performRCFileReadFirstAndLastColumnTest
argument_list|(
name|fs
argument_list|,
name|testRCFile
argument_list|,
name|columnNum
argument_list|,
name|checkCorrect
argument_list|)
expr_stmt|;
name|cost
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Read only first and last columns of a RCFile with "
operator|+
name|columnNum
operator|+
literal|" random string columns and "
operator|+
name|rowCount
operator|+
literal|" rows cost "
operator|+
name|cost
operator|+
literal|" milliseconds."
argument_list|)
expr_stmt|;
if|if
condition|(
name|rowCount
operator|!=
name|readRows
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Compare read and write row count error."
argument_list|)
throw|;
block|}
name|assertEquals
argument_list|(
literal|""
argument_list|,
name|rowCount
argument_list|,
name|readRows
argument_list|)
expr_stmt|;
if|if
condition|(
name|isLocalFileSystem
argument_list|()
operator|&&
operator|!
name|checkCorrect
condition|)
block|{
comment|// make some noisy to avoid disk caches data.
name|performSequenceFileRead
argument_list|(
name|fs
argument_list|,
name|rowCount
argument_list|,
name|testSeqFile
argument_list|)
expr_stmt|;
block|}
name|start
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|performRCFileFullyReadColumnTest
argument_list|(
name|fs
argument_list|,
name|testRCFile
argument_list|,
name|columnNum
argument_list|,
name|checkCorrect
argument_list|)
expr_stmt|;
name|cost
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Read all columns of a RCFile with "
operator|+
name|columnNum
operator|+
literal|" random string columns and "
operator|+
name|rowCount
operator|+
literal|" rows cost "
operator|+
name|cost
operator|+
literal|" milliseconds."
argument_list|)
expr_stmt|;
if|if
condition|(
name|rowCount
operator|!=
name|readRows
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Compare read and write row count error."
argument_list|)
throw|;
block|}
name|assertEquals
argument_list|(
literal|""
argument_list|,
name|rowCount
argument_list|,
name|readRows
argument_list|)
expr_stmt|;
comment|// sequence file read
name|start
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|performSequenceFileRead
argument_list|(
name|fs
argument_list|,
name|rowCount
argument_list|,
name|testSeqFile
argument_list|)
expr_stmt|;
name|cost
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Read SequenceFile with "
operator|+
name|columnNum
operator|+
literal|"  random string columns and "
operator|+
name|rowCount
operator|+
literal|" rows cost "
operator|+
name|cost
operator|+
literal|" milliseconds."
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isLocalFileSystem
parameter_list|()
block|{
return|return
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"file://"
argument_list|)
return|;
block|}
specifier|public
name|void
name|performSequenceFileRead
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|int
name|count
parameter_list|,
name|Path
name|file
parameter_list|)
throws|throws
name|IOException
block|{
name|SequenceFile
operator|.
name|Reader
name|reader
init|=
operator|new
name|SequenceFile
operator|.
name|Reader
argument_list|(
name|fs
argument_list|,
name|file
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|ByteWritable
name|key
init|=
operator|new
name|ByteWritable
argument_list|()
decl_stmt|;
name|BytesRefArrayWritable
name|val
init|=
operator|new
name|BytesRefArrayWritable
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|count
condition|;
name|i
operator|++
control|)
block|{
name|reader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|int
name|performRCFileReadFirstColumnTest
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|file
parameter_list|,
name|int
name|allColumnsNumber
parameter_list|,
name|boolean
name|chechCorrect
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
index|[]
name|checkBytes
init|=
literal|null
decl_stmt|;
name|BytesRefArrayWritable
name|checkRow
init|=
operator|new
name|BytesRefArrayWritable
argument_list|(
name|allColumnsNumber
argument_list|)
decl_stmt|;
if|if
condition|(
name|chechCorrect
condition|)
block|{
name|resetRandomGenerators
argument_list|()
expr_stmt|;
name|checkBytes
operator|=
operator|new
name|byte
index|[
name|allColumnsNumber
index|]
index|[]
expr_stmt|;
block|}
name|int
name|actualReadCount
init|=
literal|0
decl_stmt|;
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|Integer
argument_list|>
name|readCols
init|=
operator|new
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|readCols
operator|.
name|add
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|ColumnProjectionUtils
operator|.
name|setReadColumnIDs
argument_list|(
name|conf
argument_list|,
name|readCols
argument_list|)
expr_stmt|;
name|RCFile
operator|.
name|Reader
name|reader
init|=
operator|new
name|RCFile
operator|.
name|Reader
argument_list|(
name|fs
argument_list|,
name|file
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|LongWritable
name|rowID
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
name|BytesRefArrayWritable
name|cols
init|=
operator|new
name|BytesRefArrayWritable
argument_list|()
decl_stmt|;
while|while
condition|(
name|reader
operator|.
name|next
argument_list|(
name|rowID
argument_list|)
condition|)
block|{
name|reader
operator|.
name|getCurrentRow
argument_list|(
name|cols
argument_list|)
expr_stmt|;
name|boolean
name|ok
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|chechCorrect
condition|)
block|{
name|nextRandomRow
argument_list|(
name|checkBytes
argument_list|,
name|checkRow
argument_list|)
expr_stmt|;
name|ok
operator|=
name|ok
operator|&&
operator|(
name|checkRow
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|equals
argument_list|(
name|cols
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|ok
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Compare read and write error."
argument_list|)
throw|;
block|}
name|actualReadCount
operator|++
expr_stmt|;
block|}
return|return
name|actualReadCount
return|;
block|}
specifier|public
name|int
name|performRCFileReadFirstAndLastColumnTest
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|file
parameter_list|,
name|int
name|allColumnsNumber
parameter_list|,
name|boolean
name|chechCorrect
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
index|[]
name|checkBytes
init|=
literal|null
decl_stmt|;
name|BytesRefArrayWritable
name|checkRow
init|=
operator|new
name|BytesRefArrayWritable
argument_list|(
name|allColumnsNumber
argument_list|)
decl_stmt|;
if|if
condition|(
name|chechCorrect
condition|)
block|{
name|resetRandomGenerators
argument_list|()
expr_stmt|;
name|checkBytes
operator|=
operator|new
name|byte
index|[
name|allColumnsNumber
index|]
index|[]
expr_stmt|;
block|}
name|int
name|actualReadCount
init|=
literal|0
decl_stmt|;
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|Integer
argument_list|>
name|readCols
init|=
operator|new
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|readCols
operator|.
name|add
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|readCols
operator|.
name|add
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|allColumnsNumber
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|ColumnProjectionUtils
operator|.
name|setReadColumnIDs
argument_list|(
name|conf
argument_list|,
name|readCols
argument_list|)
expr_stmt|;
name|RCFile
operator|.
name|Reader
name|reader
init|=
operator|new
name|RCFile
operator|.
name|Reader
argument_list|(
name|fs
argument_list|,
name|file
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|LongWritable
name|rowID
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
name|BytesRefArrayWritable
name|cols
init|=
operator|new
name|BytesRefArrayWritable
argument_list|()
decl_stmt|;
while|while
condition|(
name|reader
operator|.
name|next
argument_list|(
name|rowID
argument_list|)
condition|)
block|{
name|reader
operator|.
name|getCurrentRow
argument_list|(
name|cols
argument_list|)
expr_stmt|;
name|boolean
name|ok
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|chechCorrect
condition|)
block|{
name|nextRandomRow
argument_list|(
name|checkBytes
argument_list|,
name|checkRow
argument_list|)
expr_stmt|;
name|ok
operator|=
name|ok
operator|&&
operator|(
name|checkRow
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|equals
argument_list|(
name|cols
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|)
expr_stmt|;
name|ok
operator|=
name|ok
operator|&&
name|checkRow
operator|.
name|get
argument_list|(
name|allColumnsNumber
operator|-
literal|1
argument_list|)
operator|.
name|equals
argument_list|(
name|cols
operator|.
name|get
argument_list|(
name|allColumnsNumber
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|ok
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Compare read and write error."
argument_list|)
throw|;
block|}
name|actualReadCount
operator|++
expr_stmt|;
block|}
return|return
name|actualReadCount
return|;
block|}
specifier|public
name|int
name|performRCFileFullyReadColumnTest
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|file
parameter_list|,
name|int
name|allColumnsNumber
parameter_list|,
name|boolean
name|chechCorrect
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
index|[]
name|checkBytes
init|=
literal|null
decl_stmt|;
name|BytesRefArrayWritable
name|checkRow
init|=
operator|new
name|BytesRefArrayWritable
argument_list|(
name|allColumnsNumber
argument_list|)
decl_stmt|;
if|if
condition|(
name|chechCorrect
condition|)
block|{
name|resetRandomGenerators
argument_list|()
expr_stmt|;
name|checkBytes
operator|=
operator|new
name|byte
index|[
name|allColumnsNumber
index|]
index|[]
expr_stmt|;
block|}
name|int
name|actualReadCount
init|=
literal|0
decl_stmt|;
name|ColumnProjectionUtils
operator|.
name|setFullyReadColumns
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|RCFile
operator|.
name|Reader
name|reader
init|=
operator|new
name|RCFile
operator|.
name|Reader
argument_list|(
name|fs
argument_list|,
name|file
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|LongWritable
name|rowID
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
name|BytesRefArrayWritable
name|cols
init|=
operator|new
name|BytesRefArrayWritable
argument_list|()
decl_stmt|;
while|while
condition|(
name|reader
operator|.
name|next
argument_list|(
name|rowID
argument_list|)
condition|)
block|{
name|reader
operator|.
name|getCurrentRow
argument_list|(
name|cols
argument_list|)
expr_stmt|;
name|boolean
name|ok
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|chechCorrect
condition|)
block|{
name|nextRandomRow
argument_list|(
name|checkBytes
argument_list|,
name|checkRow
argument_list|)
expr_stmt|;
name|ok
operator|=
name|ok
operator|&&
name|checkRow
operator|.
name|equals
argument_list|(
name|cols
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|ok
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Compare read and write error."
argument_list|)
throw|;
block|}
name|actualReadCount
operator|++
expr_stmt|;
block|}
return|return
name|actualReadCount
return|;
block|}
block|}
end_class

end_unit

