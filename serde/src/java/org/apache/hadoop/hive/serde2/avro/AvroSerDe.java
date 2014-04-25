begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|serde2
operator|.
name|avro
package|;
end_package

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
name|Properties
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|Schema
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
name|serde2
operator|.
name|AbstractSerDe
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
name|SerDeException
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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|typeinfo
operator|.
name|TypeInfo
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
name|Writable
import|;
end_import

begin_comment
comment|/**  * Read or write Avro data from Hive.  */
end_comment

begin_class
specifier|public
class|class
name|AvroSerDe
extends|extends
name|AbstractSerDe
block|{
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
name|AvroSerDe
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ObjectInspector
name|oi
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
decl_stmt|;
specifier|private
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
decl_stmt|;
specifier|private
name|Schema
name|schema
decl_stmt|;
specifier|private
name|AvroDeserializer
name|avroDeserializer
init|=
literal|null
decl_stmt|;
specifier|private
name|AvroSerializer
name|avroSerializer
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|badSchema
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|Properties
name|tableProperties
parameter_list|,
name|Properties
name|partitionProperties
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// Avro should always use the table properties for initialization (see HIVE-6835).
name|initialize
argument_list|(
name|configuration
argument_list|,
name|tableProperties
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|Properties
name|properties
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// Reset member variables so we don't get in a half-constructed state
if|if
condition|(
name|schema
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Resetting already initialized AvroSerDe"
argument_list|)
expr_stmt|;
block|}
name|schema
operator|=
literal|null
expr_stmt|;
name|oi
operator|=
literal|null
expr_stmt|;
name|columnNames
operator|=
literal|null
expr_stmt|;
name|columnTypes
operator|=
literal|null
expr_stmt|;
name|schema
operator|=
name|AvroSerdeUtils
operator|.
name|determineSchemaOrReturnErrorSchema
argument_list|(
name|properties
argument_list|)
expr_stmt|;
if|if
condition|(
name|configuration
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Configuration null, not inserting schema"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|configuration
operator|.
name|set
argument_list|(
name|AvroSerdeUtils
operator|.
name|AVRO_SERDE_SCHEMA
argument_list|,
name|schema
operator|.
name|toString
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|badSchema
operator|=
name|schema
operator|.
name|equals
argument_list|(
name|SchemaResolutionProblem
operator|.
name|SIGNAL_BAD_SCHEMA
argument_list|)
expr_stmt|;
name|AvroObjectInspectorGenerator
name|aoig
init|=
operator|new
name|AvroObjectInspectorGenerator
argument_list|(
name|schema
argument_list|)
decl_stmt|;
name|this
operator|.
name|columnNames
operator|=
name|aoig
operator|.
name|getColumnNames
argument_list|()
expr_stmt|;
name|this
operator|.
name|columnTypes
operator|=
name|aoig
operator|.
name|getColumnTypes
argument_list|()
expr_stmt|;
name|this
operator|.
name|oi
operator|=
name|aoig
operator|.
name|getObjectInspector
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|getSerializedClass
parameter_list|()
block|{
return|return
name|AvroGenericRecordWritable
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|Writable
name|serialize
parameter_list|(
name|Object
name|o
parameter_list|,
name|ObjectInspector
name|objectInspector
parameter_list|)
throws|throws
name|SerDeException
block|{
if|if
condition|(
name|badSchema
condition|)
block|{
throw|throw
operator|new
name|BadSchemaException
argument_list|()
throw|;
block|}
return|return
name|getSerializer
argument_list|()
operator|.
name|serialize
argument_list|(
name|o
argument_list|,
name|objectInspector
argument_list|,
name|columnNames
argument_list|,
name|columnTypes
argument_list|,
name|schema
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|deserialize
parameter_list|(
name|Writable
name|writable
parameter_list|)
throws|throws
name|SerDeException
block|{
if|if
condition|(
name|badSchema
condition|)
block|{
throw|throw
operator|new
name|BadSchemaException
argument_list|()
throw|;
block|}
return|return
name|getDeserializer
argument_list|()
operator|.
name|deserialize
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|,
name|writable
argument_list|,
name|schema
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|getObjectInspector
parameter_list|()
throws|throws
name|SerDeException
block|{
return|return
name|oi
return|;
block|}
annotation|@
name|Override
specifier|public
name|SerDeStats
name|getSerDeStats
parameter_list|()
block|{
comment|// No support for statistics. That seems to be a popular answer.
return|return
literal|null
return|;
block|}
specifier|private
name|AvroDeserializer
name|getDeserializer
parameter_list|()
block|{
if|if
condition|(
name|avroDeserializer
operator|==
literal|null
condition|)
block|{
name|avroDeserializer
operator|=
operator|new
name|AvroDeserializer
argument_list|()
expr_stmt|;
block|}
return|return
name|avroDeserializer
return|;
block|}
specifier|private
name|AvroSerializer
name|getSerializer
parameter_list|()
block|{
if|if
condition|(
name|avroSerializer
operator|==
literal|null
condition|)
block|{
name|avroSerializer
operator|=
operator|new
name|AvroSerializer
argument_list|()
expr_stmt|;
block|}
return|return
name|avroSerializer
return|;
block|}
block|}
end_class

end_unit

