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
name|hive
operator|.
name|ql
operator|.
name|exec
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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|RecordWriter
import|;
end_import

begin_comment
comment|/**  * ScriptDesc.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Transform Operator"
argument_list|)
specifier|public
class|class
name|ScriptDesc
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
name|String
name|scriptCmd
decl_stmt|;
comment|// Describe how to deserialize data back from user script
specifier|private
name|TableDesc
name|scriptOutputInfo
decl_stmt|;
specifier|private
name|Class
argument_list|<
name|?
extends|extends
name|RecordWriter
argument_list|>
name|inRecordWriterClass
decl_stmt|;
comment|// Describe how to serialize data out to user script
specifier|private
name|TableDesc
name|scriptInputInfo
decl_stmt|;
specifier|private
name|Class
argument_list|<
name|?
extends|extends
name|RecordReader
argument_list|>
name|outRecordReaderClass
decl_stmt|;
specifier|public
name|ScriptDesc
parameter_list|()
block|{   }
specifier|public
name|ScriptDesc
parameter_list|(
specifier|final
name|String
name|scriptCmd
parameter_list|,
specifier|final
name|TableDesc
name|scriptInputInfo
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|RecordWriter
argument_list|>
name|inRecordWriterClass
parameter_list|,
specifier|final
name|TableDesc
name|scriptOutputInfo
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|RecordReader
argument_list|>
name|outRecordReaderClass
parameter_list|)
block|{
name|this
operator|.
name|scriptCmd
operator|=
name|scriptCmd
expr_stmt|;
name|this
operator|.
name|scriptInputInfo
operator|=
name|scriptInputInfo
expr_stmt|;
name|this
operator|.
name|inRecordWriterClass
operator|=
name|inRecordWriterClass
expr_stmt|;
name|this
operator|.
name|scriptOutputInfo
operator|=
name|scriptOutputInfo
expr_stmt|;
name|this
operator|.
name|outRecordReaderClass
operator|=
name|outRecordReaderClass
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"command"
argument_list|)
specifier|public
name|String
name|getScriptCmd
parameter_list|()
block|{
return|return
name|scriptCmd
return|;
block|}
specifier|public
name|void
name|setScriptCmd
parameter_list|(
specifier|final
name|String
name|scriptCmd
parameter_list|)
block|{
name|this
operator|.
name|scriptCmd
operator|=
name|scriptCmd
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"output info"
argument_list|)
specifier|public
name|TableDesc
name|getScriptOutputInfo
parameter_list|()
block|{
return|return
name|scriptOutputInfo
return|;
block|}
specifier|public
name|void
name|setScriptOutputInfo
parameter_list|(
specifier|final
name|TableDesc
name|scriptOutputInfo
parameter_list|)
block|{
name|this
operator|.
name|scriptOutputInfo
operator|=
name|scriptOutputInfo
expr_stmt|;
block|}
specifier|public
name|TableDesc
name|getScriptInputInfo
parameter_list|()
block|{
return|return
name|scriptInputInfo
return|;
block|}
specifier|public
name|void
name|setScriptInputInfo
parameter_list|(
name|TableDesc
name|scriptInputInfo
parameter_list|)
block|{
name|this
operator|.
name|scriptInputInfo
operator|=
name|scriptInputInfo
expr_stmt|;
block|}
comment|/**    * @return the outRecordReaderClass    */
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|RecordReader
argument_list|>
name|getOutRecordReaderClass
parameter_list|()
block|{
return|return
name|outRecordReaderClass
return|;
block|}
comment|/**    * @param outRecordReaderClass    *          the outRecordReaderClass to set    */
specifier|public
name|void
name|setOutRecordReaderClass
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|RecordReader
argument_list|>
name|outRecordReaderClass
parameter_list|)
block|{
name|this
operator|.
name|outRecordReaderClass
operator|=
name|outRecordReaderClass
expr_stmt|;
block|}
comment|/**    * @return the inRecordWriterClass    */
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|RecordWriter
argument_list|>
name|getInRecordWriterClass
parameter_list|()
block|{
return|return
name|inRecordWriterClass
return|;
block|}
comment|/**    * @param inRecordWriterClass    *          the inRecordWriterClass to set    */
specifier|public
name|void
name|setInRecordWriterClass
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|RecordWriter
argument_list|>
name|inRecordWriterClass
parameter_list|)
block|{
name|this
operator|.
name|inRecordWriterClass
operator|=
name|inRecordWriterClass
expr_stmt|;
block|}
block|}
end_class

end_unit

