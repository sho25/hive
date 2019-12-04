begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|write
package|;
end_package

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
name|Map
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
name|serde2
operator|.
name|io
operator|.
name|ParquetHiveRecord
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
name|common
operator|.
name|util
operator|.
name|HiveVersionInfo
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
name|WriteSupport
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
name|io
operator|.
name|api
operator|.
name|RecordConsumer
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

begin_comment
comment|/**  *  * DataWritableWriteSupport is a WriteSupport for the DataWritableWriter  *  */
end_comment

begin_class
specifier|public
class|class
name|DataWritableWriteSupport
extends|extends
name|WriteSupport
argument_list|<
name|ParquetHiveRecord
argument_list|>
block|{
specifier|public
specifier|static
specifier|final
name|String
name|PARQUET_HIVE_SCHEMA
init|=
literal|"parquet.hive.schema"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|WRITER_TIMEZONE
init|=
literal|"writer.time.zone"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|WRITER_DATE_PROLEPTIC
init|=
literal|"writer.date.proleptic"
decl_stmt|;
specifier|private
name|DataWritableWriter
name|writer
decl_stmt|;
specifier|private
name|MessageType
name|schema
decl_stmt|;
specifier|private
name|boolean
name|defaultDateProleptic
decl_stmt|;
specifier|public
specifier|static
name|void
name|setSchema
parameter_list|(
specifier|final
name|MessageType
name|schema
parameter_list|,
specifier|final
name|Configuration
name|configuration
parameter_list|)
block|{
name|configuration
operator|.
name|set
argument_list|(
name|PARQUET_HIVE_SCHEMA
argument_list|,
name|schema
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|MessageType
name|getSchema
parameter_list|(
specifier|final
name|Configuration
name|configuration
parameter_list|)
block|{
return|return
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
name|configuration
operator|.
name|get
argument_list|(
name|PARQUET_HIVE_SCHEMA
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|WriteContext
name|init
parameter_list|(
specifier|final
name|Configuration
name|configuration
parameter_list|)
block|{
name|schema
operator|=
name|getSchema
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|metaData
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|metaData
operator|.
name|put
argument_list|(
name|WRITER_TIMEZONE
argument_list|,
name|TimeZone
operator|.
name|getDefault
argument_list|()
operator|.
name|toZoneId
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|defaultDateProleptic
operator|=
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
name|HIVE_PARQUET_DATE_PROLEPTIC_GREGORIAN
argument_list|)
expr_stmt|;
name|metaData
operator|.
name|put
argument_list|(
name|WRITER_DATE_PROLEPTIC
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|defaultDateProleptic
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|WriteContext
argument_list|(
name|schema
argument_list|,
name|metaData
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|prepareForWrite
parameter_list|(
specifier|final
name|RecordConsumer
name|recordConsumer
parameter_list|)
block|{
name|writer
operator|=
operator|new
name|DataWritableWriter
argument_list|(
name|recordConsumer
argument_list|,
name|schema
argument_list|,
name|defaultDateProleptic
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
specifier|final
name|ParquetHiveRecord
name|record
parameter_list|)
block|{
name|writer
operator|.
name|write
argument_list|(
name|record
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|HiveVersionInfo
operator|.
name|getVersion
argument_list|()
return|;
block|}
block|}
end_class

end_unit

