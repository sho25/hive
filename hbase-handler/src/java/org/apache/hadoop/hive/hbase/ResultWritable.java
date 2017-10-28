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
name|hbase
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

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
name|hbase
operator|.
name|Cell
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
name|hbase
operator|.
name|CellUtil
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
name|hbase
operator|.
name|KeyValue
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
name|hbase
operator|.
name|KeyValueUtil
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
name|hbase
operator|.
name|client
operator|.
name|Result
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
name|hbase
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
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

begin_class
specifier|public
class|class
name|ResultWritable
implements|implements
name|Writable
block|{
specifier|private
name|Result
name|result
decl_stmt|;
specifier|public
name|ResultWritable
parameter_list|()
block|{    }
specifier|public
name|ResultWritable
parameter_list|(
name|Result
name|result
parameter_list|)
block|{
name|this
operator|.
name|result
operator|=
name|result
expr_stmt|;
block|}
specifier|public
name|Result
name|getResult
parameter_list|()
block|{
return|return
name|result
return|;
block|}
specifier|public
name|void
name|setResult
parameter_list|(
name|Result
name|result
parameter_list|)
block|{
name|this
operator|.
name|result
operator|=
name|result
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
specifier|final
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|ClientProtos
operator|.
name|Result
name|protosResult
init|=
name|ClientProtos
operator|.
name|Result
operator|.
name|parseDelimitedFrom
argument_list|(
name|DataInputInputStream
operator|.
name|from
argument_list|(
name|in
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Invalid size "
operator|+
name|size
argument_list|)
throw|;
block|}
name|Cell
index|[]
name|kvs
init|=
operator|new
name|Cell
index|[
name|size
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|kvs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|kvs
index|[
name|i
index|]
operator|=
name|KeyValue
operator|.
name|create
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|result
operator|=
name|ProtobufUtil
operator|.
name|toResult
argument_list|(
name|protosResult
argument_list|,
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|kvs
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
specifier|final
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|ProtobufUtil
operator|.
name|toResultNoData
argument_list|(
name|result
argument_list|)
operator|.
name|writeDelimitedTo
argument_list|(
name|DataOutputOutputStream
operator|.
name|from
argument_list|(
name|out
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|result
operator|.
name|listCells
argument_list|()
control|)
block|{
name|KeyValue
name|kv
init|=
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|cell
argument_list|)
decl_stmt|;
name|KeyValue
operator|.
name|write
argument_list|(
name|kv
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

