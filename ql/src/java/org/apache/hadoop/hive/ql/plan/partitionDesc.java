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
name|mapred
operator|.
name|InputFormat
import|;
end_import

begin_class
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"Partition"
argument_list|)
specifier|public
class|class
name|partitionDesc
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
name|tableDesc
name|table
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
specifier|public
name|partitionDesc
parameter_list|()
block|{ }
specifier|public
name|partitionDesc
parameter_list|(
specifier|final
name|tableDesc
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
name|partitionDesc
parameter_list|(
specifier|final
name|tableDesc
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
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|partSpec
operator|=
name|partSpec
expr_stmt|;
name|this
operator|.
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
name|this
operator|.
name|outputFileFormatClass
operator|=
name|HiveFileFormatUtils
operator|.
name|getOutputFormatSubstitute
argument_list|(
name|outputFormat
argument_list|)
expr_stmt|;
name|this
operator|.
name|properties
operator|=
name|properties
expr_stmt|;
if|if
condition|(
name|properties
operator|!=
literal|null
condition|)
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
name|Constants
operator|.
name|SERIALIZATION_LIB
argument_list|)
expr_stmt|;
block|}
specifier|public
name|partitionDesc
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
name|this
operator|.
name|table
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
name|this
operator|.
name|partSpec
operator|=
name|part
operator|.
name|getSpec
argument_list|()
expr_stmt|;
name|this
operator|.
name|deserializerClass
operator|=
name|part
operator|.
name|getDeserializer
argument_list|()
operator|.
name|getClass
argument_list|()
expr_stmt|;
name|this
operator|.
name|inputFileFormatClass
operator|=
name|part
operator|.
name|getInputFormatClass
argument_list|()
expr_stmt|;
name|this
operator|.
name|outputFileFormatClass
operator|=
name|part
operator|.
name|getOutputFormatClass
argument_list|()
expr_stmt|;
name|this
operator|.
name|properties
operator|=
name|part
operator|.
name|getSchema
argument_list|()
expr_stmt|;
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
name|Constants
operator|.
name|SERIALIZATION_LIB
argument_list|)
expr_stmt|;
empty_stmt|;
block|}
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|""
argument_list|)
specifier|public
name|tableDesc
name|getTableDesc
parameter_list|()
block|{
return|return
name|this
operator|.
name|table
return|;
block|}
specifier|public
name|void
name|setTableDesc
parameter_list|(
specifier|final
name|tableDesc
name|table
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
block|}
annotation|@
name|explain
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
name|this
operator|.
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
name|this
operator|.
name|deserializerClass
operator|==
literal|null
operator|&&
name|this
operator|.
name|table
operator|!=
literal|null
condition|)
name|setDeserializerClass
argument_list|(
name|this
operator|.
name|table
operator|.
name|getDeserializerClass
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|this
operator|.
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
name|this
operator|.
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
name|this
operator|.
name|inputFileFormatClass
operator|==
literal|null
operator|&&
name|this
operator|.
name|table
operator|!=
literal|null
condition|)
name|setInputFileFormatClass
argument_list|(
name|this
operator|.
name|table
operator|.
name|getInputFileFormatClass
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|this
operator|.
name|inputFileFormatClass
return|;
block|}
comment|/**    * Return a deserializer object corresponding to the tableDesc    */
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
name|this
operator|.
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
name|this
operator|.
name|outputFileFormatClass
operator|==
literal|null
operator|&&
name|this
operator|.
name|table
operator|!=
literal|null
condition|)
name|setOutputFileFormatClass
argument_list|(
name|this
operator|.
name|table
operator|.
name|getOutputFileFormatClass
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|this
operator|.
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
name|explain
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
name|this
operator|.
name|serdeClassName
operator|==
literal|null
operator|&&
name|this
operator|.
name|properties
operator|==
literal|null
operator|&&
name|this
operator|.
name|table
operator|!=
literal|null
condition|)
name|setProperties
argument_list|(
name|this
operator|.
name|table
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|this
operator|.
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
name|explain
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
name|this
operator|.
name|serdeClassName
operator|==
literal|null
operator|&&
name|this
operator|.
name|table
operator|!=
literal|null
condition|)
name|setSerdeClassName
argument_list|(
name|this
operator|.
name|table
operator|.
name|getSerdeClassName
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|this
operator|.
name|serdeClassName
return|;
block|}
comment|/**    * @param serdeClassName the serde Class Name to set    */
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
name|explain
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
name|Constants
operator|.
name|META_TABLE_NAME
argument_list|)
return|;
block|}
annotation|@
name|explain
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
name|explain
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
specifier|public
name|partitionDesc
name|clone
parameter_list|()
block|{
name|partitionDesc
name|ret
init|=
operator|new
name|partitionDesc
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
name|this
operator|.
name|inputFileFormatClass
expr_stmt|;
name|ret
operator|.
name|outputFileFormatClass
operator|=
name|this
operator|.
name|outputFileFormatClass
expr_stmt|;
if|if
condition|(
name|this
operator|.
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
name|table
operator|=
operator|(
name|tableDesc
operator|)
name|this
operator|.
name|table
operator|.
name|clone
argument_list|()
expr_stmt|;
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
name|this
operator|.
name|partSpec
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
block|}
end_class

end_unit

