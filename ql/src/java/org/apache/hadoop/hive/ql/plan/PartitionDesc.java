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
name|plan
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
name|collect
operator|.
name|Interner
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
name|collect
operator|.
name|Interners
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
name|Enumeration
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|Properties
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
name|metastore
operator|.
name|MetaStoreUtils
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
name|metastore
operator|.
name|api
operator|.
name|hive_metastoreConstants
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
name|HiveOutputFormat
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
name|metadata
operator|.
name|Partition
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
name|Table
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
name|serde
operator|.
name|serdeConstants
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
name|Deserializer
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
name|OutputFormat
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
name|ReflectionUtil
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
name|HiveStringUtils
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
name|Explain
operator|.
name|Level
import|;
end_import

begin_comment
comment|/**  * PartitionDesc.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Partition"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|PartitionDesc
implements|implements
name|Serializable
implements|,
name|Cloneable
block|{
static|static
block|{
name|STRING_INTERNER
operator|=
name|Interners
operator|.
name|newWeakInterner
argument_list|()
expr_stmt|;
name|CLASS_INTERNER
operator|=
name|Interners
operator|.
name|newWeakInterner
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|Interner
argument_list|<
name|String
argument_list|>
name|STRING_INTERNER
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Interner
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|CLASS_INTERNER
decl_stmt|;
specifier|private
name|TableDesc
name|tableDesc
decl_stmt|;
specifier|private
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
decl_stmt|;
specifier|private
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|inputFileFormatClass
decl_stmt|;
specifier|private
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
name|outputFileFormatClass
decl_stmt|;
specifier|private
name|Properties
name|properties
decl_stmt|;
specifier|private
name|String
name|baseFileName
decl_stmt|;
specifier|private
name|VectorPartitionDesc
name|vectorPartitionDesc
decl_stmt|;
specifier|public
name|void
name|setBaseFileName
parameter_list|(
name|String
name|baseFileName
parameter_list|)
block|{
name|this
operator|.
name|baseFileName
operator|=
name|baseFileName
expr_stmt|;
block|}
specifier|public
name|PartitionDesc
parameter_list|()
block|{   }
specifier|private
specifier|final
specifier|static
name|org
operator|.
name|slf4j
operator|.
name|Logger
name|LOG
init|=
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|PartitionDesc
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|PartitionDesc
parameter_list|(
specifier|final
name|TableDesc
name|table
parameter_list|,
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
block|{
name|this
operator|.
name|tableDesc
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|partSpec
operator|=
name|partSpec
expr_stmt|;
block|}
specifier|public
name|PartitionDesc
parameter_list|(
specifier|final
name|Partition
name|part
parameter_list|)
throws|throws
name|HiveException
block|{
name|PartitionDescConstructorHelper
argument_list|(
name|part
argument_list|,
name|getTableDesc
argument_list|(
name|part
operator|.
name|getTable
argument_list|()
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|Utilities
operator|.
name|isInputFileFormatSelfDescribing
argument_list|(
name|this
argument_list|)
condition|)
block|{
comment|// if IF is self describing no need to send column info per partition, since its not used anyway.
name|Table
name|tbl
init|=
name|part
operator|.
name|getTable
argument_list|()
decl_stmt|;
name|setProperties
argument_list|(
name|MetaStoreUtils
operator|.
name|getSchemaWithoutCols
argument_list|(
name|part
operator|.
name|getTPartition
argument_list|()
operator|.
name|getSd
argument_list|()
argument_list|,
name|part
operator|.
name|getTPartition
argument_list|()
operator|.
name|getSd
argument_list|()
argument_list|,
name|part
operator|.
name|getParameters
argument_list|()
argument_list|,
name|tbl
operator|.
name|getDbName
argument_list|()
argument_list|,
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|,
name|tbl
operator|.
name|getPartitionKeys
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|setProperties
argument_list|(
name|part
operator|.
name|getMetadataFromPartitionSchema
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @param part Partition    * @param tblDesc Table Descriptor    * @param usePartSchemaProperties Use Partition Schema Properties to set the    * partition descriptor properties. This is usually set to true by the caller    * if the table is partitioned, i.e. if the table has partition columns.    * @throws HiveException    */
specifier|public
name|PartitionDesc
parameter_list|(
specifier|final
name|Partition
name|part
parameter_list|,
specifier|final
name|TableDesc
name|tblDesc
parameter_list|,
name|boolean
name|usePartSchemaProperties
parameter_list|)
throws|throws
name|HiveException
block|{
name|PartitionDescConstructorHelper
argument_list|(
name|part
argument_list|,
name|tblDesc
argument_list|,
name|usePartSchemaProperties
argument_list|)
expr_stmt|;
comment|//We use partition schema properties to set the partition descriptor properties
comment|// if usePartSchemaProperties is set to true.
if|if
condition|(
name|usePartSchemaProperties
condition|)
block|{
name|setProperties
argument_list|(
name|part
operator|.
name|getMetadataFromPartitionSchema
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// each partition maintains a large properties
name|setProperties
argument_list|(
name|part
operator|.
name|getSchemaFromTableSchema
argument_list|(
name|tblDesc
operator|.
name|getProperties
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|PartitionDescConstructorHelper
parameter_list|(
specifier|final
name|Partition
name|part
parameter_list|,
specifier|final
name|TableDesc
name|tblDesc
parameter_list|,
name|boolean
name|setInputFileFormat
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
operator|.
name|tableDesc
operator|=
name|tblDesc
expr_stmt|;
name|this
operator|.
name|partSpec
operator|=
name|part
operator|.
name|getSpec
argument_list|()
expr_stmt|;
if|if
condition|(
name|setInputFileFormat
condition|)
block|{
name|setInputFileFormatClass
argument_list|(
name|part
operator|.
name|getInputFormatClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|setOutputFileFormatClass
argument_list|(
name|part
operator|.
name|getInputFormatClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|setOutputFileFormatClass
argument_list|(
name|part
operator|.
name|getOutputFormatClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|""
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|TableDesc
name|getTableDesc
parameter_list|()
block|{
return|return
name|tableDesc
return|;
block|}
specifier|public
name|void
name|setTableDesc
parameter_list|(
name|TableDesc
name|tableDesc
parameter_list|)
block|{
name|this
operator|.
name|tableDesc
operator|=
name|tableDesc
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"partition values"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getPartSpec
parameter_list|()
block|{
return|return
name|partSpec
return|;
block|}
specifier|public
name|void
name|setPartSpec
parameter_list|(
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
block|{
name|this
operator|.
name|partSpec
operator|=
name|partSpec
expr_stmt|;
block|}
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|getInputFileFormatClass
parameter_list|()
block|{
if|if
condition|(
name|inputFileFormatClass
operator|==
literal|null
operator|&&
name|tableDesc
operator|!=
literal|null
condition|)
block|{
name|setInputFileFormatClass
argument_list|(
name|tableDesc
operator|.
name|getInputFileFormatClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|inputFileFormatClass
return|;
block|}
specifier|public
name|String
name|getDeserializerClassName
parameter_list|()
block|{
name|Properties
name|schema
init|=
name|getProperties
argument_list|()
decl_stmt|;
name|String
name|clazzName
init|=
name|schema
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_LIB
argument_list|)
decl_stmt|;
if|if
condition|(
name|clazzName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Property "
operator|+
name|serdeConstants
operator|.
name|SERIALIZATION_LIB
operator|+
literal|" cannot be null"
argument_list|)
throw|;
block|}
return|return
name|clazzName
return|;
block|}
comment|/**    * Return a deserializer object corresponding to the partitionDesc.    */
specifier|public
name|Deserializer
name|getDeserializer
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|Properties
name|schema
init|=
name|getProperties
argument_list|()
decl_stmt|;
name|String
name|clazzName
init|=
name|getDeserializerClassName
argument_list|()
decl_stmt|;
name|Deserializer
name|deserializer
init|=
name|ReflectionUtil
operator|.
name|newInstance
argument_list|(
name|conf
operator|.
name|getClassByName
argument_list|(
name|clazzName
argument_list|)
operator|.
name|asSubclass
argument_list|(
name|Deserializer
operator|.
name|class
argument_list|)
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|deserializer
argument_list|,
name|conf
argument_list|,
name|getTableDesc
argument_list|()
operator|.
name|getProperties
argument_list|()
argument_list|,
name|schema
argument_list|)
expr_stmt|;
return|return
name|deserializer
return|;
block|}
specifier|public
name|void
name|setInputFileFormatClass
parameter_list|(
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|inputFileFormatClass
parameter_list|)
block|{
if|if
condition|(
name|inputFileFormatClass
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|inputFileFormatClass
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|inputFileFormatClass
operator|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
operator|)
name|CLASS_INTERNER
operator|.
name|intern
argument_list|(
name|inputFileFormatClass
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
name|getOutputFileFormatClass
parameter_list|()
block|{
if|if
condition|(
name|outputFileFormatClass
operator|==
literal|null
operator|&&
name|tableDesc
operator|!=
literal|null
condition|)
block|{
name|setOutputFileFormatClass
argument_list|(
name|tableDesc
operator|.
name|getOutputFileFormatClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|outputFileFormatClass
return|;
block|}
specifier|public
name|void
name|setOutputFileFormatClass
parameter_list|(
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|outputFileFormatClass
parameter_list|)
block|{
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
name|outputClass
init|=
name|outputFileFormatClass
operator|==
literal|null
condition|?
literal|null
else|:
name|HiveFileFormatUtils
operator|.
name|getOutputFormatSubstitute
argument_list|(
name|outputFileFormatClass
argument_list|)
decl_stmt|;
if|if
condition|(
name|outputClass
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|outputFileFormatClass
operator|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|HiveOutputFormat
argument_list|>
operator|)
name|CLASS_INTERNER
operator|.
name|intern
argument_list|(
name|outputClass
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|outputFileFormatClass
operator|=
name|outputClass
expr_stmt|;
block|}
block|}
specifier|public
name|Properties
name|getProperties
parameter_list|()
block|{
if|if
condition|(
name|properties
operator|==
literal|null
operator|&&
name|tableDesc
operator|!=
literal|null
condition|)
block|{
return|return
name|tableDesc
operator|.
name|getProperties
argument_list|()
return|;
block|}
return|return
name|properties
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"properties"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|Map
name|getPropertiesExplain
parameter_list|()
block|{
return|return
name|HiveStringUtils
operator|.
name|getPropertiesExplain
argument_list|(
name|getProperties
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|void
name|setProperties
parameter_list|(
specifier|final
name|Properties
name|properties
parameter_list|)
block|{
name|internProperties
argument_list|(
name|properties
argument_list|)
expr_stmt|;
name|this
operator|.
name|properties
operator|=
name|properties
expr_stmt|;
block|}
specifier|private
specifier|static
name|TableDesc
name|getTableDesc
parameter_list|(
name|Table
name|table
parameter_list|)
block|{
name|TableDesc
name|tableDesc
init|=
name|Utilities
operator|.
name|getTableDesc
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|internProperties
argument_list|(
name|tableDesc
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|tableDesc
return|;
block|}
specifier|private
specifier|static
name|void
name|internProperties
parameter_list|(
name|Properties
name|properties
parameter_list|)
block|{
for|for
control|(
name|Enumeration
argument_list|<
name|?
argument_list|>
name|keys
init|=
name|properties
operator|.
name|propertyNames
argument_list|()
init|;
name|keys
operator|.
name|hasMoreElements
argument_list|()
condition|;
control|)
block|{
name|String
name|key
init|=
operator|(
name|String
operator|)
name|keys
operator|.
name|nextElement
argument_list|()
decl_stmt|;
name|String
name|oldValue
init|=
name|properties
operator|.
name|getProperty
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldValue
operator|!=
literal|null
condition|)
block|{
name|String
name|value
init|=
name|STRING_INTERNER
operator|.
name|intern
argument_list|(
name|oldValue
argument_list|)
decl_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * @return the serdeClassName    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"serde"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getSerdeClassName
parameter_list|()
block|{
return|return
name|getProperties
argument_list|()
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_LIB
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"name"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|getProperties
argument_list|()
operator|.
name|getProperty
argument_list|(
name|hive_metastoreConstants
operator|.
name|META_TABLE_NAME
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"input format"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getInputFileFormatClassName
parameter_list|()
block|{
return|return
name|getInputFileFormatClass
argument_list|()
operator|.
name|getName
argument_list|()
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"output format"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getOutputFileFormatClassName
parameter_list|()
block|{
return|return
name|getOutputFileFormatClass
argument_list|()
operator|.
name|getName
argument_list|()
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"base file name"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getBaseFileName
parameter_list|()
block|{
return|return
name|baseFileName
return|;
block|}
specifier|public
name|boolean
name|isPartitioned
parameter_list|()
block|{
return|return
name|partSpec
operator|!=
literal|null
operator|&&
operator|!
name|partSpec
operator|.
name|isEmpty
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|PartitionDesc
name|clone
parameter_list|()
block|{
name|PartitionDesc
name|ret
init|=
operator|new
name|PartitionDesc
argument_list|()
decl_stmt|;
name|ret
operator|.
name|inputFileFormatClass
operator|=
name|inputFileFormatClass
expr_stmt|;
name|ret
operator|.
name|outputFileFormatClass
operator|=
name|outputFileFormatClass
expr_stmt|;
if|if
condition|(
name|properties
operator|!=
literal|null
condition|)
block|{
name|Properties
name|newProp
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|Enumeration
argument_list|<
name|Object
argument_list|>
name|keysProp
init|=
name|properties
operator|.
name|keys
argument_list|()
decl_stmt|;
while|while
condition|(
name|keysProp
operator|.
name|hasMoreElements
argument_list|()
condition|)
block|{
name|Object
name|key
init|=
name|keysProp
operator|.
name|nextElement
argument_list|()
decl_stmt|;
name|newProp
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|properties
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ret
operator|.
name|setProperties
argument_list|(
name|newProp
argument_list|)
expr_stmt|;
block|}
name|ret
operator|.
name|tableDesc
operator|=
operator|(
name|TableDesc
operator|)
name|tableDesc
operator|.
name|clone
argument_list|()
expr_stmt|;
comment|// The partition spec is not present
if|if
condition|(
name|partSpec
operator|!=
literal|null
condition|)
block|{
name|ret
operator|.
name|partSpec
operator|=
operator|new
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|ret
operator|.
name|partSpec
operator|.
name|putAll
argument_list|(
name|partSpec
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|vectorPartitionDesc
operator|!=
literal|null
condition|)
block|{
name|ret
operator|.
name|vectorPartitionDesc
operator|=
name|vectorPartitionDesc
operator|.
name|clone
argument_list|()
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|boolean
name|cond
init|=
name|o
operator|instanceof
name|PartitionDesc
decl_stmt|;
if|if
condition|(
operator|!
name|cond
condition|)
block|{
return|return
literal|false
return|;
block|}
name|PartitionDesc
name|other
init|=
operator|(
name|PartitionDesc
operator|)
name|o
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|input1
init|=
name|getInputFileFormatClass
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|input2
init|=
name|other
operator|.
name|getInputFileFormatClass
argument_list|()
decl_stmt|;
name|cond
operator|=
operator|(
name|input1
operator|==
literal|null
operator|&&
name|input2
operator|==
literal|null
operator|)
operator|||
operator|(
name|input1
operator|!=
literal|null
operator|&&
name|input1
operator|.
name|equals
argument_list|(
name|input2
argument_list|)
operator|)
expr_stmt|;
if|if
condition|(
operator|!
name|cond
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
name|output1
init|=
name|getOutputFileFormatClass
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
name|output2
init|=
name|other
operator|.
name|getOutputFileFormatClass
argument_list|()
decl_stmt|;
name|cond
operator|=
operator|(
name|output1
operator|==
literal|null
operator|&&
name|output2
operator|==
literal|null
operator|)
operator|||
operator|(
name|output1
operator|!=
literal|null
operator|&&
name|output1
operator|.
name|equals
argument_list|(
name|output2
argument_list|)
operator|)
expr_stmt|;
if|if
condition|(
operator|!
name|cond
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Properties
name|properties1
init|=
name|getProperties
argument_list|()
decl_stmt|;
name|Properties
name|properties2
init|=
name|other
operator|.
name|getProperties
argument_list|()
decl_stmt|;
name|cond
operator|=
operator|(
name|properties1
operator|==
literal|null
operator|&&
name|properties2
operator|==
literal|null
operator|)
operator|||
operator|(
name|properties1
operator|!=
literal|null
operator|&&
name|properties1
operator|.
name|equals
argument_list|(
name|properties2
argument_list|)
operator|)
expr_stmt|;
if|if
condition|(
operator|!
name|cond
condition|)
block|{
return|return
literal|false
return|;
block|}
name|TableDesc
name|tableDesc1
init|=
name|getTableDesc
argument_list|()
decl_stmt|;
name|TableDesc
name|tableDesc2
init|=
name|other
operator|.
name|getTableDesc
argument_list|()
decl_stmt|;
name|cond
operator|=
operator|(
name|tableDesc1
operator|==
literal|null
operator|&&
name|tableDesc2
operator|==
literal|null
operator|)
operator|||
operator|(
name|tableDesc1
operator|!=
literal|null
operator|&&
name|tableDesc1
operator|.
name|equals
argument_list|(
name|tableDesc2
argument_list|)
operator|)
expr_stmt|;
if|if
condition|(
operator|!
name|cond
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec1
init|=
name|getPartSpec
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec2
init|=
name|other
operator|.
name|getPartSpec
argument_list|()
decl_stmt|;
name|cond
operator|=
operator|(
name|partSpec1
operator|==
literal|null
operator|&&
name|partSpec2
operator|==
literal|null
operator|)
operator|||
operator|(
name|partSpec1
operator|!=
literal|null
operator|&&
name|partSpec1
operator|.
name|equals
argument_list|(
name|partSpec2
argument_list|)
operator|)
expr_stmt|;
if|if
condition|(
operator|!
name|cond
condition|)
block|{
return|return
literal|false
return|;
block|}
name|VectorPartitionDesc
name|vecPartDesc1
init|=
name|getVectorPartitionDesc
argument_list|()
decl_stmt|;
name|VectorPartitionDesc
name|vecPartDesc2
init|=
name|other
operator|.
name|getVectorPartitionDesc
argument_list|()
decl_stmt|;
return|return
operator|(
name|vecPartDesc1
operator|==
literal|null
operator|&&
name|vecPartDesc2
operator|==
literal|null
operator|)
operator|||
operator|(
name|vecPartDesc1
operator|!=
literal|null
operator|&&
name|vecPartDesc1
operator|.
name|equals
argument_list|(
name|vecPartDesc2
argument_list|)
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
specifier|final
name|int
name|prime
init|=
literal|31
decl_stmt|;
name|int
name|result
init|=
literal|1
decl_stmt|;
name|result
operator|=
name|result
operator|*
name|prime
operator|+
operator|(
name|getInputFileFormatClass
argument_list|()
operator|==
literal|null
condition|?
literal|0
else|:
name|getInputFileFormatClass
argument_list|()
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
name|result
operator|=
name|result
operator|*
name|prime
operator|+
operator|(
name|getOutputFileFormatClass
argument_list|()
operator|==
literal|null
condition|?
literal|0
else|:
name|getOutputFileFormatClass
argument_list|()
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
name|result
operator|=
name|result
operator|*
name|prime
operator|+
operator|(
name|getProperties
argument_list|()
operator|==
literal|null
condition|?
literal|0
else|:
name|getProperties
argument_list|()
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
name|result
operator|=
name|result
operator|*
name|prime
operator|+
operator|(
name|getTableDesc
argument_list|()
operator|==
literal|null
condition|?
literal|0
else|:
name|getTableDesc
argument_list|()
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
name|result
operator|=
name|result
operator|*
name|prime
operator|+
operator|(
name|getPartSpec
argument_list|()
operator|==
literal|null
condition|?
literal|0
else|:
name|getPartSpec
argument_list|()
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
name|result
operator|=
name|result
operator|*
name|prime
operator|+
operator|(
name|getVectorPartitionDesc
argument_list|()
operator|==
literal|null
condition|?
literal|0
else|:
name|getVectorPartitionDesc
argument_list|()
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * Attempt to derive a virtual<code>base file name</code> property from the    * path. If path format is unrecognized, just use the full path.    *    * @param path    *          URI to the partition file    */
specifier|public
name|void
name|deriveBaseFileName
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
name|PlanUtils
operator|.
name|configureInputJobPropertiesForStorageHandler
argument_list|(
name|tableDesc
argument_list|)
expr_stmt|;
if|if
condition|(
name|path
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|baseFileName
operator|=
name|path
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|intern
parameter_list|(
name|Interner
argument_list|<
name|TableDesc
argument_list|>
name|interner
parameter_list|)
block|{
name|this
operator|.
name|tableDesc
operator|=
name|interner
operator|.
name|intern
argument_list|(
name|tableDesc
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setVectorPartitionDesc
parameter_list|(
name|VectorPartitionDesc
name|vectorPartitionDesc
parameter_list|)
block|{
name|this
operator|.
name|vectorPartitionDesc
operator|=
name|vectorPartitionDesc
expr_stmt|;
block|}
specifier|public
name|VectorPartitionDesc
name|getVectorPartitionDesc
parameter_list|()
block|{
return|return
name|vectorPartitionDesc
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"PartitionDesc [tableDesc="
operator|+
name|tableDesc
operator|+
literal|", partSpec="
operator|+
name|partSpec
operator|+
literal|", inputFileFormatClass="
operator|+
name|inputFileFormatClass
operator|+
literal|", outputFileFormatClass="
operator|+
name|outputFileFormatClass
operator|+
literal|", properties="
operator|+
name|properties
operator|+
literal|", baseFileName="
operator|+
name|baseFileName
operator|+
literal|", vectorPartitionDesc="
operator|+
name|vectorPartitionDesc
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

