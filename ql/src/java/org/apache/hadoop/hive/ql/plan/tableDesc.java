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
name|hadoop
operator|.
name|hive
operator|.
name|serde
operator|.
name|SerDe
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

begin_class
specifier|public
class|class
name|tableDesc
implements|implements
name|Serializable
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
name|SerDe
argument_list|>
name|serdeClass
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
name|tableDesc
parameter_list|()
block|{ }
specifier|public
name|tableDesc
parameter_list|(
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|SerDe
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
extends|extends
name|OutputFormat
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
name|this
operator|.
name|serdeClass
operator|=
name|serdeClass
expr_stmt|;
name|this
operator|.
name|inputFileFormatClass
operator|=
name|inputFileFormatClass
expr_stmt|;
name|this
operator|.
name|outputFileFormatClass
operator|=
name|class1
expr_stmt|;
name|this
operator|.
name|properties
operator|=
name|properties
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
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|SerDe
argument_list|>
name|getSerdeClass
parameter_list|()
block|{
return|return
name|this
operator|.
name|serdeClass
return|;
block|}
specifier|public
name|void
name|setSerdeClass
parameter_list|(
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|SerDe
argument_list|>
name|serdeClass
parameter_list|)
block|{
name|this
operator|.
name|serdeClass
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
name|this
operator|.
name|inputFileFormatClass
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
name|OutputFormat
argument_list|>
name|getOutputFileFormatClass
parameter_list|()
block|{
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
extends|extends
name|OutputFormat
argument_list|>
name|outputFileFormatClass
parameter_list|)
block|{
name|this
operator|.
name|outputFileFormatClass
operator|=
name|outputFileFormatClass
expr_stmt|;
block|}
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
specifier|public
name|String
name|getSerdeClassName
parameter_list|()
block|{
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
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|this
operator|.
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
name|Constants
operator|.
name|META_TABLE_NAME
argument_list|)
return|;
block|}
block|}
end_class

end_unit

