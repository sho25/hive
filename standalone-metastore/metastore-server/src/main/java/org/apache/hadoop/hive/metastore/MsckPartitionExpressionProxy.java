begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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
name|metastore
package|;
end_package

begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
name|FieldSchema
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
name|FileMetadataExprType
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
name|MetaException
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

begin_comment
comment|// This is added as part of moving MSCK code from ql to standalone-metastore. There is a metastore API to drop
end_comment

begin_comment
comment|// partitions by name but we cannot use it because msck typically will contain partition value (year=2014). We almost
end_comment

begin_comment
comment|// never drop partition by name (year). So we need to construct expression filters, the current
end_comment

begin_comment
comment|// PartitionExpressionProxy implementations (PartitionExpressionForMetastore and HCatClientHMSImpl.ExpressionBuilder)
end_comment

begin_comment
comment|// all depend on ql code to build ExprNodeDesc for the partition expressions. It also depends on kryo for serializing
end_comment

begin_comment
comment|// the expression objects to byte[]. For MSCK drop partition, we don't need complex expression generator. For now,
end_comment

begin_comment
comment|// all we do is split the partition spec (year=2014/month=24) into filter expression year='2014' and month='24' and
end_comment

begin_comment
comment|// rely on metastore database to deal with type conversions. Ideally, PartitionExpressionProxy default implementation
end_comment

begin_comment
comment|// should use SearchArgument (storage-api) to construct the filter expression and not depend on ql, but the usecase
end_comment

begin_comment
comment|// for msck is pretty simple and this specific implementation should suffice.
end_comment

begin_class
specifier|public
class|class
name|MsckPartitionExpressionProxy
implements|implements
name|PartitionExpressionProxy
block|{
annotation|@
name|Override
specifier|public
name|String
name|convertExprToFilter
parameter_list|(
specifier|final
name|byte
index|[]
name|exprBytes
parameter_list|,
specifier|final
name|String
name|defaultPartitionName
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
operator|new
name|String
argument_list|(
name|exprBytes
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterPartitionsByExpr
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partColumns
parameter_list|,
name|byte
index|[]
name|expr
parameter_list|,
name|String
name|defaultPartitionName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partitionNames
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileMetadataExprType
name|getMetadataType
parameter_list|(
name|String
name|inputFormat
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|FileFormatProxy
name|getFileFormatProxy
parameter_list|(
name|FileMetadataExprType
name|type
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|SearchArgument
name|createSarg
parameter_list|(
name|byte
index|[]
name|expr
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
end_class

end_unit

