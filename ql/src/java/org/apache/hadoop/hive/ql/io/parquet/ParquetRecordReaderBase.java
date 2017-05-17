begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|parquet
package|;
end_package

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
name|Strings
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
name|io
operator|.
name|parquet
operator|.
name|read
operator|.
name|DataWritableReadSupport
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
name|parquet
operator|.
name|read
operator|.
name|ParquetFilterPredicateConverter
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
name|parquet
operator|.
name|serde
operator|.
name|ParquetTableUtils
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
name|parquet
operator|.
name|timestamp
operator|.
name|NanoTimeUtils
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
name|sarg
operator|.
name|ConvertAstToSearchArg
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
name|sarg
operator|.
name|SearchArgument
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
name|SerDeStats
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
name|JobConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|filter2
operator|.
name|compat
operator|.
name|FilterCompat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|filter2
operator|.
name|compat
operator|.
name|RowGroupFilter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|filter2
operator|.
name|predicate
operator|.
name|FilterPredicate
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|format
operator|.
name|converter
operator|.
name|ParquetMetadataConverter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|hadoop
operator|.
name|ParquetFileReader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|hadoop
operator|.
name|ParquetInputFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|hadoop
operator|.
name|ParquetInputSplit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|hadoop
operator|.
name|api
operator|.
name|InitContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|hadoop
operator|.
name|api
operator|.
name|ReadSupport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|hadoop
operator|.
name|metadata
operator|.
name|BlockMetaData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|hadoop
operator|.
name|metadata
operator|.
name|FileMetaData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|hadoop
operator|.
name|metadata
operator|.
name|ParquetMetadata
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|MessageType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|MessageTypeParser
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TimeZone
import|;
end_import

begin_class
specifier|public
class|class
name|ParquetRecordReaderBase
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ParquetRecordReaderBase
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|Path
name|file
decl_stmt|;
specifier|protected
name|ProjectionPusher
name|projectionPusher
decl_stmt|;
specifier|protected
name|SerDeStats
name|serDeStats
decl_stmt|;
specifier|protected
name|JobConf
name|jobConf
decl_stmt|;
specifier|protected
name|int
name|schemaSize
decl_stmt|;
specifier|protected
name|List
argument_list|<
name|BlockMetaData
argument_list|>
name|filtedBlocks
decl_stmt|;
specifier|protected
name|ParquetFileReader
name|reader
decl_stmt|;
comment|/**    * gets a ParquetInputSplit corresponding to a split given by Hive    *    * @param oldSplit The split given by Hive    * @param conf The JobConf of the Hive job    * @return a ParquetInputSplit corresponding to the oldSplit    * @throws IOException if the config cannot be enhanced or if the footer cannot be read from the file    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|protected
name|ParquetInputSplit
name|getSplit
parameter_list|(
specifier|final
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputSplit
name|oldSplit
parameter_list|,
specifier|final
name|JobConf
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|ParquetInputSplit
name|split
decl_stmt|;
if|if
condition|(
name|oldSplit
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|oldSplit
operator|instanceof
name|FileSplit
condition|)
block|{
specifier|final
name|Path
name|finalPath
init|=
operator|(
operator|(
name|FileSplit
operator|)
name|oldSplit
operator|)
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|jobConf
operator|=
name|projectionPusher
operator|.
name|pushProjectionsAndFilters
argument_list|(
name|conf
argument_list|,
name|finalPath
operator|.
name|getParent
argument_list|()
argument_list|)
expr_stmt|;
comment|// TODO enable MetadataFilter by using readFooter(Configuration configuration, Path file,
comment|// MetadataFilter filter) API
specifier|final
name|ParquetMetadata
name|parquetMetadata
init|=
name|ParquetFileReader
operator|.
name|readFooter
argument_list|(
name|jobConf
argument_list|,
name|finalPath
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|BlockMetaData
argument_list|>
name|blocks
init|=
name|parquetMetadata
operator|.
name|getBlocks
argument_list|()
decl_stmt|;
specifier|final
name|FileMetaData
name|fileMetaData
init|=
name|parquetMetadata
operator|.
name|getFileMetaData
argument_list|()
decl_stmt|;
specifier|final
name|ReadSupport
operator|.
name|ReadContext
name|readContext
init|=
operator|new
name|DataWritableReadSupport
argument_list|()
operator|.
name|init
argument_list|(
operator|new
name|InitContext
argument_list|(
name|jobConf
argument_list|,
literal|null
argument_list|,
name|fileMetaData
operator|.
name|getSchema
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
comment|// Compute stats
for|for
control|(
name|BlockMetaData
name|bmd
range|:
name|blocks
control|)
block|{
name|serDeStats
operator|.
name|setRowCount
argument_list|(
name|serDeStats
operator|.
name|getRowCount
argument_list|()
operator|+
name|bmd
operator|.
name|getRowCount
argument_list|()
argument_list|)
expr_stmt|;
name|serDeStats
operator|.
name|setRawDataSize
argument_list|(
name|serDeStats
operator|.
name|getRawDataSize
argument_list|()
operator|+
name|bmd
operator|.
name|getTotalByteSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|schemaSize
operator|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
name|readContext
operator|.
name|getReadSupportMetadata
argument_list|()
operator|.
name|get
argument_list|(
name|DataWritableReadSupport
operator|.
name|HIVE_TABLE_AS_PARQUET_SCHEMA
argument_list|)
argument_list|)
operator|.
name|getFieldCount
argument_list|()
expr_stmt|;
specifier|final
name|List
argument_list|<
name|BlockMetaData
argument_list|>
name|splitGroup
init|=
operator|new
name|ArrayList
argument_list|<
name|BlockMetaData
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|long
name|splitStart
init|=
operator|(
operator|(
name|FileSplit
operator|)
name|oldSplit
operator|)
operator|.
name|getStart
argument_list|()
decl_stmt|;
specifier|final
name|long
name|splitLength
init|=
operator|(
operator|(
name|FileSplit
operator|)
name|oldSplit
operator|)
operator|.
name|getLength
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|BlockMetaData
name|block
range|:
name|blocks
control|)
block|{
specifier|final
name|long
name|firstDataPage
init|=
name|block
operator|.
name|getColumns
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFirstDataPageOffset
argument_list|()
decl_stmt|;
if|if
condition|(
name|firstDataPage
operator|>=
name|splitStart
operator|&&
name|firstDataPage
operator|<
name|splitStart
operator|+
name|splitLength
condition|)
block|{
name|splitGroup
operator|.
name|add
argument_list|(
name|block
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|splitGroup
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Skipping split, could not find row group in: "
operator|+
name|oldSplit
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|FilterCompat
operator|.
name|Filter
name|filter
init|=
name|setFilter
argument_list|(
name|jobConf
argument_list|,
name|fileMetaData
operator|.
name|getSchema
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|filter
operator|!=
literal|null
condition|)
block|{
name|filtedBlocks
operator|=
name|RowGroupFilter
operator|.
name|filterRowGroups
argument_list|(
name|filter
argument_list|,
name|splitGroup
argument_list|,
name|fileMetaData
operator|.
name|getSchema
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|filtedBlocks
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"All row groups are dropped due to filter predicates"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|long
name|droppedBlocks
init|=
name|splitGroup
operator|.
name|size
argument_list|()
operator|-
name|filtedBlocks
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|droppedBlocks
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Dropping "
operator|+
name|droppedBlocks
operator|+
literal|" row groups that do not pass filter predicate"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|filtedBlocks
operator|=
name|splitGroup
expr_stmt|;
block|}
name|split
operator|=
operator|new
name|ParquetInputSplit
argument_list|(
name|finalPath
argument_list|,
name|splitStart
argument_list|,
name|splitLength
argument_list|,
name|oldSplit
operator|.
name|getLocations
argument_list|()
argument_list|,
name|filtedBlocks
argument_list|,
name|readContext
operator|.
name|getRequestedSchema
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|fileMetaData
operator|.
name|getSchema
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|fileMetaData
operator|.
name|getKeyValueMetaData
argument_list|()
argument_list|,
name|readContext
operator|.
name|getReadSupportMetadata
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|split
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown split type: "
operator|+
name|oldSplit
argument_list|)
throw|;
block|}
block|}
comment|/**    * Sets the TimeZone conversion for Parquet timestamp columns.    *    * @param configuration Configuration object where to get and set the TimeZone conversion    * @param finalPath     path to the parquet file    */
specifier|protected
name|void
name|setTimeZoneConversion
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|Path
name|finalPath
parameter_list|)
block|{
name|ParquetMetadata
name|parquetMetadata
decl_stmt|;
name|String
name|timeZoneID
decl_stmt|;
try|try
block|{
name|parquetMetadata
operator|=
name|ParquetFileReader
operator|.
name|readFooter
argument_list|(
name|configuration
argument_list|,
name|finalPath
argument_list|,
name|ParquetMetadataConverter
operator|.
name|NO_FILTER
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// If an error occurred while reading the file, then we just skip the TimeZone setting.
comment|// This error will probably occur on any other part of the code.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Could not read parquet file footer at "
operator|+
name|finalPath
operator|+
literal|". Cannot determine "
operator|+
literal|"parquet file timezone"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
name|boolean
name|skipConversion
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|configuration
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_PARQUET_TIMESTAMP_SKIP_CONVERSION
argument_list|)
decl_stmt|;
name|FileMetaData
name|fileMetaData
init|=
name|parquetMetadata
operator|.
name|getFileMetaData
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|fileMetaData
operator|.
name|getCreatedBy
argument_list|()
argument_list|)
operator|.
name|startsWith
argument_list|(
literal|"parquet-mr"
argument_list|)
operator|&&
name|skipConversion
condition|)
block|{
comment|// Impala writes timestamp values using GMT only. We should not try to convert Impala
comment|// files to other type of timezones.
name|timeZoneID
operator|=
name|ParquetTableUtils
operator|.
name|PARQUET_INT96_NO_ADJUSTMENT_ZONE
expr_stmt|;
block|}
else|else
block|{
comment|// TABLE_PARQUET_INT96_TIMEZONE is a table property used to detect what timezone conversion
comment|// to use when reading Parquet timestamps.
name|timeZoneID
operator|=
name|configuration
operator|.
name|get
argument_list|(
name|ParquetTableUtils
operator|.
name|PARQUET_INT96_WRITE_ZONE_PROPERTY
argument_list|)
expr_stmt|;
name|NanoTimeUtils
operator|.
name|validateTimeZone
argument_list|(
name|timeZoneID
argument_list|)
expr_stmt|;
block|}
comment|// 'timeZoneID' should be valid, since we did not throw exception above
name|configuration
operator|.
name|set
argument_list|(
name|ParquetTableUtils
operator|.
name|PARQUET_INT96_WRITE_ZONE_PROPERTY
argument_list|,
name|timeZoneID
argument_list|)
expr_stmt|;
block|}
specifier|public
name|FilterCompat
operator|.
name|Filter
name|setFilter
parameter_list|(
specifier|final
name|JobConf
name|conf
parameter_list|,
name|MessageType
name|schema
parameter_list|)
block|{
name|SearchArgument
name|sarg
init|=
name|ConvertAstToSearchArg
operator|.
name|createFromConf
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|sarg
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// Create the Parquet FilterPredicate without including columns that do not exist
comment|// on the schema (such as partition columns).
name|FilterPredicate
name|p
init|=
name|ParquetFilterPredicateConverter
operator|.
name|toFilterPredicate
argument_list|(
name|sarg
argument_list|,
name|schema
argument_list|)
decl_stmt|;
if|if
condition|(
name|p
operator|!=
literal|null
condition|)
block|{
comment|// Filter may have sensitive information. Do not send to debug.
name|LOG
operator|.
name|debug
argument_list|(
literal|"PARQUET predicate push down generated."
argument_list|)
expr_stmt|;
name|ParquetInputFormat
operator|.
name|setFilterPredicate
argument_list|(
name|conf
argument_list|,
name|p
argument_list|)
expr_stmt|;
return|return
name|FilterCompat
operator|.
name|get
argument_list|(
name|p
argument_list|)
return|;
block|}
else|else
block|{
comment|// Filter may have sensitive information. Do not send to debug.
name|LOG
operator|.
name|debug
argument_list|(
literal|"No PARQUET predicate push down is generated."
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|public
name|List
argument_list|<
name|BlockMetaData
argument_list|>
name|getFiltedBlocks
parameter_list|()
block|{
return|return
name|filtedBlocks
return|;
block|}
specifier|public
name|SerDeStats
name|getStats
parameter_list|()
block|{
return|return
name|serDeStats
return|;
block|}
block|}
end_class

end_unit

