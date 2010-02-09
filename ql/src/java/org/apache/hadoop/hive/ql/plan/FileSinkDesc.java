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

begin_comment
comment|/**  * FileSinkDesc.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"File Output Operator"
argument_list|)
specifier|public
class|class
name|FileSinkDesc
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
name|dirName
decl_stmt|;
specifier|private
name|TableDesc
name|tableInfo
decl_stmt|;
specifier|private
name|boolean
name|compressed
decl_stmt|;
specifier|private
name|int
name|destTableId
decl_stmt|;
specifier|private
name|String
name|compressCodec
decl_stmt|;
specifier|private
name|String
name|compressType
decl_stmt|;
specifier|public
name|FileSinkDesc
parameter_list|()
block|{   }
specifier|public
name|FileSinkDesc
parameter_list|(
specifier|final
name|String
name|dirName
parameter_list|,
specifier|final
name|TableDesc
name|tableInfo
parameter_list|,
specifier|final
name|boolean
name|compressed
parameter_list|,
name|int
name|destTableId
parameter_list|)
block|{
name|this
operator|.
name|dirName
operator|=
name|dirName
expr_stmt|;
name|this
operator|.
name|tableInfo
operator|=
name|tableInfo
expr_stmt|;
name|this
operator|.
name|compressed
operator|=
name|compressed
expr_stmt|;
name|this
operator|.
name|destTableId
operator|=
name|destTableId
expr_stmt|;
block|}
specifier|public
name|FileSinkDesc
parameter_list|(
specifier|final
name|String
name|dirName
parameter_list|,
specifier|final
name|TableDesc
name|tableInfo
parameter_list|,
specifier|final
name|boolean
name|compressed
parameter_list|)
block|{
name|this
operator|.
name|dirName
operator|=
name|dirName
expr_stmt|;
name|this
operator|.
name|tableInfo
operator|=
name|tableInfo
expr_stmt|;
name|this
operator|.
name|compressed
operator|=
name|compressed
expr_stmt|;
name|destTableId
operator|=
literal|0
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"directory"
argument_list|,
name|normalExplain
operator|=
literal|false
argument_list|)
specifier|public
name|String
name|getDirName
parameter_list|()
block|{
return|return
name|dirName
return|;
block|}
specifier|public
name|void
name|setDirName
parameter_list|(
specifier|final
name|String
name|dirName
parameter_list|)
block|{
name|this
operator|.
name|dirName
operator|=
name|dirName
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"table"
argument_list|)
specifier|public
name|TableDesc
name|getTableInfo
parameter_list|()
block|{
return|return
name|tableInfo
return|;
block|}
specifier|public
name|void
name|setTableInfo
parameter_list|(
specifier|final
name|TableDesc
name|tableInfo
parameter_list|)
block|{
name|this
operator|.
name|tableInfo
operator|=
name|tableInfo
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"compressed"
argument_list|)
specifier|public
name|boolean
name|getCompressed
parameter_list|()
block|{
return|return
name|compressed
return|;
block|}
specifier|public
name|void
name|setCompressed
parameter_list|(
name|boolean
name|compressed
parameter_list|)
block|{
name|this
operator|.
name|compressed
operator|=
name|compressed
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"GlobalTableId"
argument_list|)
specifier|public
name|int
name|getDestTableId
parameter_list|()
block|{
return|return
name|destTableId
return|;
block|}
specifier|public
name|void
name|setDestTableId
parameter_list|(
name|int
name|destTableId
parameter_list|)
block|{
name|this
operator|.
name|destTableId
operator|=
name|destTableId
expr_stmt|;
block|}
specifier|public
name|String
name|getCompressCodec
parameter_list|()
block|{
return|return
name|compressCodec
return|;
block|}
specifier|public
name|void
name|setCompressCodec
parameter_list|(
name|String
name|intermediateCompressorCodec
parameter_list|)
block|{
name|compressCodec
operator|=
name|intermediateCompressorCodec
expr_stmt|;
block|}
specifier|public
name|String
name|getCompressType
parameter_list|()
block|{
return|return
name|compressType
return|;
block|}
specifier|public
name|void
name|setCompressType
parameter_list|(
name|String
name|intermediateCompressType
parameter_list|)
block|{
name|compressType
operator|=
name|intermediateCompressType
expr_stmt|;
block|}
block|}
end_class

end_unit

