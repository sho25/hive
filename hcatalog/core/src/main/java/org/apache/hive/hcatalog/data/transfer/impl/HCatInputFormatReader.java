begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|transfer
operator|.
name|impl
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
name|Iterator
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
name|hive
operator|.
name|shims
operator|.
name|ShimLoader
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
name|WritableComparable
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
name|mapreduce
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
name|mapreduce
operator|.
name|Job
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
name|mapreduce
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
name|mapreduce
operator|.
name|TaskAttemptContext
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
name|mapreduce
operator|.
name|TaskAttemptID
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
name|hcatalog
operator|.
name|common
operator|.
name|ErrorType
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
name|hcatalog
operator|.
name|common
operator|.
name|HCatException
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
name|hcatalog
operator|.
name|data
operator|.
name|HCatRecord
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
name|hcatalog
operator|.
name|data
operator|.
name|transfer
operator|.
name|HCatReader
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
name|hcatalog
operator|.
name|data
operator|.
name|transfer
operator|.
name|ReadEntity
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
name|hcatalog
operator|.
name|data
operator|.
name|transfer
operator|.
name|ReaderContext
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
name|hcatalog
operator|.
name|data
operator|.
name|transfer
operator|.
name|state
operator|.
name|StateProvider
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
name|hcatalog
operator|.
name|mapreduce
operator|.
name|HCatInputFormat
import|;
end_import

begin_comment
comment|/**  * This reader reads via {@link HCatInputFormat}  *  */
end_comment

begin_class
specifier|public
class|class
name|HCatInputFormatReader
extends|extends
name|HCatReader
block|{
specifier|private
name|InputSplit
name|split
decl_stmt|;
specifier|public
name|HCatInputFormatReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|Configuration
name|config
parameter_list|,
name|StateProvider
name|sp
parameter_list|)
block|{
name|super
argument_list|(
name|config
argument_list|,
name|sp
argument_list|)
expr_stmt|;
name|this
operator|.
name|split
operator|=
name|split
expr_stmt|;
block|}
specifier|public
name|HCatInputFormatReader
parameter_list|(
name|ReadEntity
name|info
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|config
parameter_list|)
block|{
name|super
argument_list|(
name|info
argument_list|,
name|config
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ReaderContext
name|prepareRead
parameter_list|()
throws|throws
name|HCatException
block|{
try|try
block|{
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|HCatInputFormat
name|hcif
init|=
name|HCatInputFormat
operator|.
name|setInput
argument_list|(
name|job
argument_list|,
name|re
operator|.
name|getDbName
argument_list|()
argument_list|,
name|re
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|.
name|setFilter
argument_list|(
name|re
operator|.
name|getFilterString
argument_list|()
argument_list|)
decl_stmt|;
name|ReaderContext
name|cntxt
init|=
operator|new
name|ReaderContext
argument_list|()
decl_stmt|;
name|cntxt
operator|.
name|setInputSplits
argument_list|(
name|hcif
operator|.
name|getSplits
argument_list|(
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHCatShim
argument_list|()
operator|.
name|createJobContext
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|cntxt
operator|.
name|setConf
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|cntxt
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_NOT_INITIALIZED
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_NOT_INITIALIZED
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|HCatRecord
argument_list|>
name|read
parameter_list|()
throws|throws
name|HCatException
block|{
name|HCatInputFormat
name|inpFmt
init|=
operator|new
name|HCatInputFormat
argument_list|()
decl_stmt|;
name|RecordReader
argument_list|<
name|WritableComparable
argument_list|,
name|HCatRecord
argument_list|>
name|rr
decl_stmt|;
try|try
block|{
name|TaskAttemptContext
name|cntxt
init|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHCatShim
argument_list|()
operator|.
name|createTaskAttemptContext
argument_list|(
name|conf
argument_list|,
operator|new
name|TaskAttemptID
argument_list|()
argument_list|)
decl_stmt|;
name|rr
operator|=
name|inpFmt
operator|.
name|createRecordReader
argument_list|(
name|split
argument_list|,
name|cntxt
argument_list|)
expr_stmt|;
name|rr
operator|.
name|initialize
argument_list|(
name|split
argument_list|,
name|cntxt
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_NOT_INITIALIZED
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_NOT_INITIALIZED
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
operator|new
name|HCatRecordItr
argument_list|(
name|rr
argument_list|)
return|;
block|}
specifier|private
specifier|static
class|class
name|HCatRecordItr
implements|implements
name|Iterator
argument_list|<
name|HCatRecord
argument_list|>
block|{
specifier|private
name|RecordReader
argument_list|<
name|WritableComparable
argument_list|,
name|HCatRecord
argument_list|>
name|curRecReader
decl_stmt|;
name|HCatRecordItr
parameter_list|(
name|RecordReader
argument_list|<
name|WritableComparable
argument_list|,
name|HCatRecord
argument_list|>
name|rr
parameter_list|)
block|{
name|curRecReader
operator|=
name|rr
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
try|try
block|{
name|boolean
name|retVal
init|=
name|curRecReader
operator|.
name|nextKeyValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|retVal
condition|)
block|{
return|return
literal|true
return|;
block|}
comment|// if its false, we need to close recordReader.
name|curRecReader
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|HCatRecord
name|next
parameter_list|()
block|{
try|try
block|{
return|return
name|curRecReader
operator|.
name|getCurrentValue
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not allowed"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

