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
name|io
operator|.
name|orc
package|;
end_package

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
name|vector
operator|.
name|VectorizedRowBatch
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
name|objectinspector
operator|.
name|ObjectInspector
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
name|ObjectWritable
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

begin_comment
comment|/**  * A serde class for ORC.  * It transparently passes the object to/from the ORC file reader/writer.  */
end_comment

begin_class
specifier|public
class|class
name|VectorizedOrcSerde
extends|extends
name|OrcSerde
block|{
specifier|private
specifier|final
name|OrcStruct
index|[]
name|orcStructArray
init|=
operator|new
name|OrcStruct
index|[
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
index|]
decl_stmt|;
specifier|private
specifier|final
name|Writable
index|[]
name|orcRowArray
init|=
operator|new
name|Writable
index|[
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
index|]
decl_stmt|;
specifier|private
specifier|final
name|ObjectWritable
name|ow
init|=
operator|new
name|ObjectWritable
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ObjectInspector
name|inspector
init|=
literal|null
decl_stmt|;
specifier|public
name|VectorizedOrcSerde
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|orcStructArray
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|orcRowArray
index|[
name|i
index|]
operator|=
operator|new
name|OrcSerdeRow
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Writable
name|serialize
parameter_list|(
name|Object
name|obj
parameter_list|,
name|ObjectInspector
name|inspector
parameter_list|)
block|{
name|VectorizedRowBatch
name|batch
init|=
operator|(
name|VectorizedRowBatch
operator|)
name|obj
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
name|batch
operator|.
name|size
condition|;
name|i
operator|++
control|)
block|{
name|OrcStruct
name|ost
init|=
name|orcStructArray
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|ost
operator|==
literal|null
condition|)
block|{
name|ost
operator|=
operator|new
name|OrcStruct
argument_list|(
name|batch
operator|.
name|numCols
argument_list|)
expr_stmt|;
name|orcStructArray
index|[
name|i
index|]
operator|=
name|ost
expr_stmt|;
block|}
name|int
name|index
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
name|index
operator|=
name|batch
operator|.
name|selected
index|[
name|i
index|]
expr_stmt|;
block|}
else|else
block|{
name|index
operator|=
name|i
expr_stmt|;
block|}
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|batch
operator|.
name|numCols
condition|;
name|k
operator|++
control|)
block|{
name|Writable
name|w
decl_stmt|;
if|if
condition|(
name|batch
operator|.
name|cols
index|[
name|k
index|]
operator|.
name|isRepeating
condition|)
block|{
name|w
operator|=
name|batch
operator|.
name|cols
index|[
name|k
index|]
operator|.
name|getWritableObject
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|w
operator|=
name|batch
operator|.
name|cols
index|[
name|k
index|]
operator|.
name|getWritableObject
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
name|ost
operator|.
name|setFieldValue
argument_list|(
name|k
argument_list|,
name|w
argument_list|)
expr_stmt|;
block|}
name|OrcSerdeRow
name|row
init|=
operator|(
name|OrcSerdeRow
operator|)
name|orcRowArray
index|[
name|i
index|]
decl_stmt|;
name|row
operator|.
name|realRow
operator|=
name|ost
expr_stmt|;
name|row
operator|.
name|inspector
operator|=
name|inspector
expr_stmt|;
block|}
name|ow
operator|.
name|set
argument_list|(
name|orcRowArray
argument_list|)
expr_stmt|;
return|return
name|ow
return|;
block|}
block|}
end_class

end_unit

