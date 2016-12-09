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
name|parquet
operator|.
name|vector
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
name|ColumnVector
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
name|vector
operator|.
name|StructColumnVector
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
name|typeinfo
operator|.
name|StructTypeInfo
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
name|typeinfo
operator|.
name|TypeInfo
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
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_class
specifier|public
class|class
name|VectorizedStructColumnReader
implements|implements
name|VectorizedColumnReader
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|VectorizedColumnReader
argument_list|>
name|fieldReaders
decl_stmt|;
specifier|public
name|VectorizedStructColumnReader
parameter_list|(
name|List
argument_list|<
name|VectorizedColumnReader
argument_list|>
name|fieldReaders
parameter_list|)
block|{
name|this
operator|.
name|fieldReaders
operator|=
name|fieldReaders
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readBatch
parameter_list|(
name|int
name|total
parameter_list|,
name|ColumnVector
name|column
parameter_list|,
name|TypeInfo
name|columnType
parameter_list|)
throws|throws
name|IOException
block|{
name|StructColumnVector
name|structColumnVector
init|=
operator|(
name|StructColumnVector
operator|)
name|column
decl_stmt|;
name|StructTypeInfo
name|structTypeInfo
init|=
operator|(
name|StructTypeInfo
operator|)
name|columnType
decl_stmt|;
name|ColumnVector
index|[]
name|vectors
init|=
name|structColumnVector
operator|.
name|fields
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
name|vectors
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|fieldReaders
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|readBatch
argument_list|(
name|total
argument_list|,
name|vectors
index|[
name|i
index|]
argument_list|,
name|structTypeInfo
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|structColumnVector
operator|.
name|isRepeating
operator|=
name|structColumnVector
operator|.
name|isRepeating
operator|&&
name|vectors
index|[
name|i
index|]
operator|.
name|isRepeating
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|vectors
index|[
name|i
index|]
operator|.
name|isNull
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|structColumnVector
operator|.
name|isNull
index|[
name|j
index|]
operator|=
operator|(
name|i
operator|==
literal|0
operator|)
condition|?
name|vectors
index|[
name|i
index|]
operator|.
name|isNull
index|[
name|j
index|]
else|:
name|structColumnVector
operator|.
name|isNull
index|[
name|j
index|]
operator|&&
name|vectors
index|[
name|i
index|]
operator|.
name|isNull
index|[
name|j
index|]
expr_stmt|;
block|}
name|structColumnVector
operator|.
name|noNulls
operator|=
operator|(
name|i
operator|==
literal|0
operator|)
condition|?
name|vectors
index|[
name|i
index|]
operator|.
name|noNulls
else|:
name|structColumnVector
operator|.
name|noNulls
operator|&&
name|vectors
index|[
name|i
index|]
operator|.
name|noNulls
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

