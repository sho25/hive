begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|storage
operator|.
name|jdbc
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
name|Constants
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
name|MapWritable
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
name|RecordReader
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
name|Reporter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|storage
operator|.
name|jdbc
operator|.
name|dao
operator|.
name|DatabaseAccessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|storage
operator|.
name|jdbc
operator|.
name|dao
operator|.
name|DatabaseAccessorFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_class
specifier|public
class|class
name|JdbcInputFormat
extends|extends
name|HiveInputFormat
argument_list|<
name|LongWritable
argument_list|,
name|MapWritable
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOGGER
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|JdbcInputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|DatabaseAccessor
name|dbAccessor
init|=
literal|null
decl_stmt|;
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|RecordReader
argument_list|<
name|LongWritable
argument_list|,
name|MapWritable
argument_list|>
name|getRecordReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
operator|(
name|split
operator|instanceof
name|JdbcInputSplit
operator|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Incompatible split type "
operator|+
name|split
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|"."
argument_list|)
throw|;
block|}
return|return
operator|new
name|JdbcRecordReader
argument_list|(
name|job
argument_list|,
operator|(
name|JdbcInputSplit
operator|)
name|split
argument_list|)
return|;
block|}
comment|/**    * {@inheritDoc}    */
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
try|try
block|{
if|if
condition|(
operator|!
name|job
operator|.
name|getBoolean
argument_list|(
name|Constants
operator|.
name|JDBC_SPLIT_QUERY
argument_list|,
literal|true
argument_list|)
condition|)
block|{
comment|// We will not split this query
name|LOGGER
operator|.
name|debug
argument_list|(
literal|"Creating 1 input splits"
argument_list|)
expr_stmt|;
name|InputSplit
index|[]
name|splits
init|=
operator|new
name|InputSplit
index|[
literal|1
index|]
decl_stmt|;
name|splits
index|[
literal|0
index|]
operator|=
operator|new
name|JdbcInputSplit
argument_list|(
name|FileInputFormat
operator|.
name|getInputPaths
argument_list|(
name|job
argument_list|)
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
return|return
name|splits
return|;
block|}
comment|// We will split this query into n splits
name|LOGGER
operator|.
name|debug
argument_list|(
literal|"Creating {} input splits"
argument_list|,
name|numSplits
argument_list|)
expr_stmt|;
name|dbAccessor
operator|=
name|DatabaseAccessorFactory
operator|.
name|getAccessor
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|int
name|numRecords
init|=
name|numSplits
operator|<=
literal|1
condition|?
name|Integer
operator|.
name|MAX_VALUE
else|:
name|dbAccessor
operator|.
name|getTotalNumberOfRecords
argument_list|(
name|job
argument_list|)
decl_stmt|;
if|if
condition|(
name|numRecords
operator|<
name|numSplits
condition|)
block|{
name|numSplits
operator|=
name|numRecords
expr_stmt|;
block|}
if|if
condition|(
name|numSplits
operator|<=
literal|0
condition|)
block|{
name|numSplits
operator|=
literal|1
expr_stmt|;
block|}
name|int
name|numRecordsPerSplit
init|=
name|numRecords
operator|/
name|numSplits
decl_stmt|;
name|int
name|numSplitsWithExtraRecords
init|=
name|numRecords
operator|%
name|numSplits
decl_stmt|;
name|LOGGER
operator|.
name|debug
argument_list|(
literal|"Num records = {}"
argument_list|,
name|numRecords
argument_list|)
expr_stmt|;
name|InputSplit
index|[]
name|splits
init|=
operator|new
name|InputSplit
index|[
name|numSplits
index|]
decl_stmt|;
name|Path
index|[]
name|tablePaths
init|=
name|FileInputFormat
operator|.
name|getInputPaths
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|int
name|offset
init|=
literal|0
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
name|numSplits
condition|;
name|i
operator|++
control|)
block|{
name|int
name|numRecordsInThisSplit
init|=
name|numRecordsPerSplit
decl_stmt|;
if|if
condition|(
name|i
operator|<
name|numSplitsWithExtraRecords
condition|)
block|{
name|numRecordsInThisSplit
operator|++
expr_stmt|;
block|}
name|splits
index|[
name|i
index|]
operator|=
operator|new
name|JdbcInputSplit
argument_list|(
name|numRecordsInThisSplit
argument_list|,
name|offset
argument_list|,
name|tablePaths
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|offset
operator|+=
name|numRecordsInThisSplit
expr_stmt|;
block|}
name|dbAccessor
operator|=
literal|null
expr_stmt|;
return|return
name|splits
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOGGER
operator|.
name|error
argument_list|(
literal|"Error while splitting input data."
argument_list|,
name|e
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
block|}
comment|/**    * For testing purposes only    *    * @param dbAccessor    *            DatabaseAccessor object    */
specifier|public
name|void
name|setDbAccessor
parameter_list|(
name|DatabaseAccessor
name|dbAccessor
parameter_list|)
block|{
name|this
operator|.
name|dbAccessor
operator|=
name|dbAccessor
expr_stmt|;
block|}
block|}
end_class

end_unit

