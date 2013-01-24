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
argument_list|)
specifier|public
class|class
name|PartitionDesc
implements|implements
name|Serializable
implements|,
name|Cloneable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|2L
decl_stmt|;
specifier|private
name|TableDesc
name|tableDesc
decl_stmt|;
specifier|private
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
name|partSpec
decl_stmt|;
specifier|private
name|java
operator|.
name|lang
operator|.
name|Class
argument_list|<
name|?
extends|extends
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
argument_list|>
name|deserializerClass
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
name|HiveOutputFormat
argument_list|>
name|outputFileFormatClass
decl_stmt|;
specifier|private
name|java
operator|.
name|util
operator|.
name|Properties
name|properties
decl_stmt|;
specifier|private
name|String
name|serdeClassName
decl_stmt|;
specifier|private
specifier|transient
name|String
name|baseFileName
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
specifier|public
name|PartitionDesc
parameter_list|(
specifier|final
name|TableDesc
name|table
parameter_list|,
specifier|final
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
name|partSpec
parameter_list|)
block|{
name|this
argument_list|(
name|table
argument_list|,
name|partSpec
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|PartitionDesc
parameter_list|(
specifier|final
name|TableDesc
name|table
parameter_list|,
specifier|final
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
name|partSpec
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|Deserializer
argument_list|>
name|serdeClass
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|inputFileFormatClass
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|outputFormat
parameter_list|,
specifier|final
name|java
operator|.
name|util
operator|.
name|Properties
name|properties
parameter_list|,
specifier|final
name|String
name|serdeClassName
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
name|properties
operator|=
name|properties
expr_stmt|;
name|this
operator|.
name|partSpec
operator|=
name|partSpec
expr_stmt|;
name|deserializerClass
operator|=
name|serdeClass
expr_stmt|;
name|this
operator|.
name|inputFileFormatClass
operator|=
name|inputFileFormatClass
expr_stmt|;
if|if
condition|(
name|outputFormat
operator|!=
literal|null
condition|)
block|{
name|outputFileFormatClass
operator|=
name|HiveFileFormatUtils
operator|.
name|getOutputFormatSubstitute
argument_list|(
name|outputFormat
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|serdeClassName
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|serdeClassName
operator|=
name|serdeClassName
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|properties
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|serdeClassName
operator|=
name|properties
operator|.
name|getProperty
argument_list|(
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
operator|.
name|SERIALIZATION_LIB
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|PartitionDesc
parameter_list|(
specifier|final
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
name|part
parameter_list|)
throws|throws
name|HiveException
block|{
name|tableDesc
operator|=
name|Utilities
operator|.
name|getTableDesc
argument_list|(
name|part
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|properties
operator|=
name|part
operator|.
name|getMetadataFromPartitionSchema
argument_list|()
expr_stmt|;
name|partSpec
operator|=
name|part
operator|.
name|getSpec
argument_list|()
expr_stmt|;
name|deserializerClass
operator|=
name|part
operator|.
name|getDeserializer
argument_list|(
name|properties
argument_list|)
operator|.
name|getClass
argument_list|()
expr_stmt|;
name|inputFileFormatClass
operator|=
name|part
operator|.
name|getInputFormatClass
argument_list|()
expr_stmt|;
name|outputFileFormatClass
operator|=
name|part
operator|.
name|getOutputFormatClass
argument_list|()
expr_stmt|;
name|serdeClassName
operator|=
name|properties
operator|.
name|getProperty
argument_list|(
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
operator|.
name|SERIALIZATION_LIB
argument_list|)
expr_stmt|;
empty_stmt|;
block|}
specifier|public
name|PartitionDesc
parameter_list|(
specifier|final
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
name|part
parameter_list|,
specifier|final
name|TableDesc
name|tblDesc
parameter_list|)
throws|throws
name|HiveException
block|{
name|tableDesc
operator|=
name|tblDesc
expr_stmt|;
name|properties
operator|=
name|part
operator|.
name|getSchemaFromTableSchema
argument_list|(
name|tblDesc
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
comment|// each partition maintains a large properties
name|partSpec
operator|=
name|part
operator|.
name|getSpec
argument_list|()
expr_stmt|;
comment|// deserializerClass = part.getDeserializer(properties).getClass();
name|Deserializer
name|deserializer
decl_stmt|;
try|try
block|{
name|deserializer
operator|=
name|SerDeUtils
operator|.
name|lookupDeserializer
argument_list|(
name|properties
operator|.
name|getProperty
argument_list|(
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
operator|.
name|SERIALIZATION_LIB
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
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
name|deserializerClass
operator|=
name|deserializer
operator|.
name|getClass
argument_list|()
expr_stmt|;
name|inputFileFormatClass
operator|=
name|part
operator|.
name|getInputFormatClass
argument_list|()
expr_stmt|;
name|outputFileFormatClass
operator|=
name|part
operator|.
name|getOutputFormatClass
argument_list|()
expr_stmt|;
name|serdeClassName
operator|=
name|properties
operator|.
name|getProperty
argument_list|(
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
operator|.
name|SERIALIZATION_LIB
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|""
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
argument_list|)
specifier|public
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
name|java
operator|.
name|lang
operator|.
name|Class
argument_list|<
name|?
extends|extends
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
argument_list|>
name|getDeserializerClass
parameter_list|()
block|{
if|if
condition|(
name|deserializerClass
operator|==
literal|null
operator|&&
name|tableDesc
operator|!=
literal|null
condition|)
block|{
name|setDeserializerClass
argument_list|(
name|tableDesc
operator|.
name|getDeserializerClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|deserializerClass
return|;
block|}
specifier|public
name|void
name|setDeserializerClass
parameter_list|(
specifier|final
name|java
operator|.
name|lang
operator|.
name|Class
argument_list|<
name|?
extends|extends
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
argument_list|>
name|serdeClass
parameter_list|)
block|{
name|deserializerClass
operator|=
name|serdeClass
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
comment|/**    * Return a deserializer object corresponding to the tableDesc.    */
specifier|public
name|Deserializer
name|getDeserializer
parameter_list|()
throws|throws
name|Exception
block|{
name|Deserializer
name|de
init|=
name|deserializerClass
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|de
operator|.
name|initialize
argument_list|(
literal|null
argument_list|,
name|properties
argument_list|)
expr_stmt|;
return|return
name|de
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
name|this
operator|.
name|inputFileFormatClass
operator|=
name|inputFileFormatClass
expr_stmt|;
block|}
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|HiveOutputFormat
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
name|this
operator|.
name|outputFileFormatClass
operator|=
name|HiveFileFormatUtils
operator|.
name|getOutputFormatSubstitute
argument_list|(
name|outputFileFormatClass
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"properties"
argument_list|,
name|normalExplain
operator|=
literal|false
argument_list|)
specifier|public
name|java
operator|.
name|util
operator|.
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
specifier|public
name|void
name|setProperties
parameter_list|(
specifier|final
name|java
operator|.
name|util
operator|.
name|Properties
name|properties
parameter_list|)
block|{
name|this
operator|.
name|properties
operator|=
name|properties
expr_stmt|;
block|}
comment|/**    * @return the serdeClassName    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"serde"
argument_list|)
specifier|public
name|String
name|getSerdeClassName
parameter_list|()
block|{
if|if
condition|(
name|serdeClassName
operator|==
literal|null
operator|&&
name|tableDesc
operator|!=
literal|null
condition|)
block|{
name|setSerdeClassName
argument_list|(
name|tableDesc
operator|.
name|getSerdeClassName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|serdeClassName
return|;
block|}
comment|/**    * @param serdeClassName    *          the serde Class Name to set    */
specifier|public
name|void
name|setSerdeClassName
parameter_list|(
name|String
name|serdeClassName
parameter_list|)
block|{
name|this
operator|.
name|serdeClassName
operator|=
name|serdeClassName
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"name"
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
name|normalExplain
operator|=
literal|false
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
name|setSerdeClassName
argument_list|(
name|serdeClassName
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setDeserializerClass
argument_list|(
name|deserializerClass
argument_list|)
expr_stmt|;
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
return|return
name|ret
return|;
block|}
comment|/**    * Attempt to derive a virtual<code>base file name</code> property from the    * path. If path format is unrecognized, just use the full path.    *    * @param path    *          URI to the partition file    */
name|void
name|deriveBaseFileName
parameter_list|(
name|String
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
try|try
block|{
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|baseFileName
operator|=
name|p
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
comment|// don't really care about the exception. the goal is to capture the
comment|// the last component at the minimum - so set to the complete path
name|baseFileName
operator|=
name|path
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

