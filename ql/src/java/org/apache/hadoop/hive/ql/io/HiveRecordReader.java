begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|mr
operator|.
name|ExecMapper
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
name|JobConf
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
name|RecordReader
import|;
end_import

begin_comment
comment|/**  * HiveRecordReader is a simple wrapper on RecordReader. It allows us to stop  * reading the data when some global flag ExecMapper.getDone() is set.  */
end_comment

begin_class
specifier|public
class|class
name|HiveRecordReader
parameter_list|<
name|K
extends|extends
name|WritableComparable
parameter_list|,
name|V
extends|extends
name|Writable
parameter_list|>
extends|extends
name|HiveContextAwareRecordReader
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
specifier|public
name|HiveRecordReader
parameter_list|(
name|RecordReader
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|recordReader
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|recordReader
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveRecordReader
parameter_list|(
name|RecordReader
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|recordReader
parameter_list|,
name|JobConf
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|recordReader
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|doClose
parameter_list|()
throws|throws
name|IOException
block|{
name|recordReader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|K
name|createKey
parameter_list|()
block|{
return|return
name|recordReader
operator|.
name|createKey
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|V
name|createValue
parameter_list|()
block|{
return|return
name|recordReader
operator|.
name|createValue
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getPos
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|recordReader
operator|.
name|getPos
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|float
name|getProgress
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|isSorted
condition|)
block|{
return|return
name|super
operator|.
name|getProgress
argument_list|()
return|;
block|}
return|return
name|recordReader
operator|.
name|getProgress
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|doNext
parameter_list|(
name|K
name|key
parameter_list|,
name|V
name|value
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|ExecMapper
operator|.
name|getDone
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|super
operator|.
name|doNext
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
return|;
block|}
block|}
end_class

end_unit

