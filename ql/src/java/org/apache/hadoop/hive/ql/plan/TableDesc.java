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

begin_comment
comment|/**  * TableDesc.  *  */
end_comment

begin_class
specifier|public
class|class
name|TableDesc
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
literal|1L
decl_stmt|;
specifier|private
name|Class
argument_list|<
name|?
extends|extends
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
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
decl_stmt|;
specifier|public
name|TableDesc
parameter_list|()
block|{   }
specifier|public
name|TableDesc
parameter_list|(
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
name|class1
parameter_list|,
specifier|final
name|java
operator|.
name|util
operator|.
name|Properties
name|properties
parameter_list|)
block|{
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
name|outputFileFormatClass
operator|=
name|HiveFileFormatUtils
operator|.
name|getOutputFormatSubstitute
argument_list|(
name|class1
argument_list|)
expr_stmt|;
name|this
operator|.
name|properties
operator|=
name|properties
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
name|Class
argument_list|<
name|?
extends|extends
name|Deserializer
argument_list|>
name|getDeserializerClass
parameter_list|()
block|{
return|return
name|deserializerClass
return|;
block|}
specifier|public
name|void
name|setDeserializerClass
parameter_list|(
specifier|final
name|Class
argument_list|<
name|?
extends|extends
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
specifier|public
name|void
name|setJobProperties
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
parameter_list|)
block|{
name|this
operator|.
name|jobProperties
operator|=
name|jobProperties
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"jobProperties"
argument_list|,
name|normalExplain
operator|=
literal|false
argument_list|)
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getJobProperties
parameter_list|()
block|{
return|return
name|jobProperties
return|;
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
specifier|public
name|boolean
name|isNonNative
parameter_list|()
block|{
return|return
operator|(
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
name|metastore
operator|.
name|api
operator|.
name|hive_metastoreConstants
operator|.
name|META_TABLE_STORAGE
argument_list|)
operator|!=
literal|null
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|clone
parameter_list|()
block|{
name|TableDesc
name|ret
init|=
operator|new
name|TableDesc
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
name|setInputFileFormatClass
argument_list|(
name|inputFileFormatClass
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setOutputFileFormatClass
argument_list|(
name|outputFileFormatClass
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|jobProperties
operator|!=
literal|null
condition|)
block|{
name|ret
operator|.
name|jobProperties
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|jobProperties
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
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
name|prime
operator|*
name|result
operator|+
operator|(
operator|(
name|deserializerClass
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|deserializerClass
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
operator|(
operator|(
name|inputFileFormatClass
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|inputFileFormatClass
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
operator|(
operator|(
name|outputFileFormatClass
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|outputFileFormatClass
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
operator|(
operator|(
name|properties
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|properties
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
operator|(
operator|(
name|serdeClassName
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|serdeClassName
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
operator|(
operator|(
name|jobProperties
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|jobProperties
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
return|return
name|result
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
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|TableDesc
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|TableDesc
name|target
init|=
operator|(
name|TableDesc
operator|)
name|o
decl_stmt|;
name|boolean
name|ret
init|=
literal|true
decl_stmt|;
name|ret
operator|=
name|ret
operator|&&
operator|(
name|deserializerClass
operator|==
literal|null
condition|?
name|target
operator|.
name|deserializerClass
operator|==
literal|null
else|:
name|deserializerClass
operator|.
name|equals
argument_list|(
name|target
operator|.
name|deserializerClass
argument_list|)
operator|)
expr_stmt|;
name|ret
operator|=
name|ret
operator|&&
operator|(
name|inputFileFormatClass
operator|==
literal|null
condition|?
name|target
operator|.
name|inputFileFormatClass
operator|==
literal|null
else|:
name|inputFileFormatClass
operator|.
name|equals
argument_list|(
name|target
operator|.
name|inputFileFormatClass
argument_list|)
operator|)
expr_stmt|;
name|ret
operator|=
name|ret
operator|&&
operator|(
name|outputFileFormatClass
operator|==
literal|null
condition|?
name|target
operator|.
name|outputFileFormatClass
operator|==
literal|null
else|:
name|outputFileFormatClass
operator|.
name|equals
argument_list|(
name|target
operator|.
name|outputFileFormatClass
argument_list|)
operator|)
expr_stmt|;
name|ret
operator|=
name|ret
operator|&&
operator|(
name|properties
operator|==
literal|null
condition|?
name|target
operator|.
name|properties
operator|==
literal|null
else|:
name|properties
operator|.
name|equals
argument_list|(
name|target
operator|.
name|properties
argument_list|)
operator|)
expr_stmt|;
name|ret
operator|=
name|ret
operator|&&
operator|(
name|serdeClassName
operator|==
literal|null
condition|?
name|target
operator|.
name|serdeClassName
operator|==
literal|null
else|:
name|serdeClassName
operator|.
name|equals
argument_list|(
name|target
operator|.
name|serdeClassName
argument_list|)
operator|)
expr_stmt|;
name|ret
operator|=
name|ret
operator|&&
operator|(
name|jobProperties
operator|==
literal|null
condition|?
name|target
operator|.
name|jobProperties
operator|==
literal|null
else|:
name|jobProperties
operator|.
name|equals
argument_list|(
name|target
operator|.
name|jobProperties
argument_list|)
operator|)
expr_stmt|;
return|return
name|ret
return|;
block|}
block|}
end_class

end_unit

