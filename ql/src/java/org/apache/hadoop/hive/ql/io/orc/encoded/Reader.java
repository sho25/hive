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
operator|.
name|orc
operator|.
name|encoded
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
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|common
operator|.
name|Pool
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
name|common
operator|.
name|Pool
operator|.
name|PoolObjectHelper
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
name|common
operator|.
name|io
operator|.
name|DataCache
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
name|common
operator|.
name|io
operator|.
name|encoded
operator|.
name|EncodedColumnBatch
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
name|common
operator|.
name|io
operator|.
name|encoded
operator|.
name|EncodedColumnBatch
operator|.
name|ColumnStreamData
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
name|ColumnVector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|CompressionCodec
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|DataReader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|OrcProto
import|;
end_import

begin_comment
comment|/**  * The interface for reading encoded data from ORC files.  */
end_comment

begin_interface
specifier|public
interface|interface
name|Reader
extends|extends
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
operator|.
name|Reader
block|{
comment|/**    * Creates the encoded reader.    * @param fileKey File ID to read, to use for cache lookups and such.    * @param dataCache Data cache to use for cache lookups.    * @param dataReader Data reader to read data not found in cache (from disk, HDFS, and such).    * @param pf Pool factory to create object pools.    * @return The reader.    */
name|EncodedReader
name|encodedReader
parameter_list|(
name|Object
name|fileKey
parameter_list|,
name|DataCache
name|dataCache
parameter_list|,
name|DataReader
name|dataReader
parameter_list|,
name|PoolFactory
name|pf
parameter_list|,
name|IoTrace
name|trace
parameter_list|,
name|boolean
name|useCodecPool
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/** The factory that can create (or return) the pools used by encoded reader. */
specifier|public
interface|interface
name|PoolFactory
block|{
parameter_list|<
name|T
parameter_list|>
name|Pool
argument_list|<
name|T
argument_list|>
name|createPool
parameter_list|(
name|int
name|size
parameter_list|,
name|PoolObjectHelper
argument_list|<
name|T
argument_list|>
name|helper
parameter_list|)
function_decl|;
name|Pool
argument_list|<
name|OrcEncodedColumnBatch
argument_list|>
name|createEncodedColumnBatchPool
parameter_list|()
function_decl|;
name|Pool
argument_list|<
name|ColumnStreamData
argument_list|>
name|createColumnStreamDataPool
parameter_list|()
function_decl|;
block|}
comment|/** Implementation of EncodedColumnBatch for ORC. */
specifier|public
specifier|static
specifier|final
class|class
name|OrcEncodedColumnBatch
extends|extends
name|EncodedColumnBatch
argument_list|<
name|OrcBatchKey
argument_list|>
block|{
comment|/** RG index indicating the data applies for all RGs (e.g. a string dictionary). */
specifier|public
specifier|static
specifier|final
name|int
name|ALL_RGS
init|=
operator|-
literal|1
decl_stmt|;
comment|/**      * All the previous streams are data streams, this and the next ones are index streams.      * We assume the order will stay the same for backward compat.      */
specifier|public
specifier|static
specifier|final
name|int
name|MAX_DATA_STREAMS
init|=
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
operator|.
name|ROW_INDEX
operator|.
name|getNumber
argument_list|()
decl_stmt|;
specifier|public
name|void
name|init
parameter_list|(
name|Object
name|fileKey
parameter_list|,
name|int
name|stripeIx
parameter_list|,
name|int
name|rgIx
parameter_list|,
name|int
name|columnCount
parameter_list|)
block|{
if|if
condition|(
name|batchKey
operator|==
literal|null
condition|)
block|{
name|batchKey
operator|=
operator|new
name|OrcBatchKey
argument_list|(
name|fileKey
argument_list|,
name|stripeIx
argument_list|,
name|rgIx
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|batchKey
operator|.
name|set
argument_list|(
name|fileKey
argument_list|,
name|stripeIx
argument_list|,
name|rgIx
argument_list|)
expr_stmt|;
block|}
name|resetColumnArrays
argument_list|(
name|columnCount
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|initOrcColumn
parameter_list|(
name|int
name|colIx
parameter_list|)
block|{
name|super
operator|.
name|initColumn
argument_list|(
name|colIx
argument_list|,
name|MAX_DATA_STREAMS
argument_list|)
expr_stmt|;
block|}
comment|/**      * Same as columnData, but for the data that already comes as VRBs.      * The combination of the two contains all the necessary data,      */
specifier|protected
name|List
argument_list|<
name|ColumnVector
argument_list|>
index|[]
name|columnVectors
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|super
operator|.
name|reset
argument_list|()
expr_stmt|;
if|if
condition|(
name|columnVectors
operator|==
literal|null
condition|)
return|return;
name|Arrays
operator|.
name|fill
argument_list|(
name|columnVectors
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|void
name|initColumnWithVectors
parameter_list|(
name|int
name|colIx
parameter_list|,
name|List
argument_list|<
name|ColumnVector
argument_list|>
name|data
parameter_list|)
block|{
if|if
condition|(
name|columnVectors
operator|==
literal|null
condition|)
block|{
name|columnVectors
operator|=
operator|new
name|List
index|[
name|columnData
operator|.
name|length
index|]
expr_stmt|;
block|}
name|columnVectors
index|[
name|colIx
index|]
operator|=
name|data
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
specifier|protected
name|void
name|resetColumnArrays
parameter_list|(
name|int
name|columnCount
parameter_list|)
block|{
name|super
operator|.
name|resetColumnArrays
argument_list|(
name|columnCount
argument_list|)
expr_stmt|;
if|if
condition|(
name|columnVectors
operator|!=
literal|null
operator|&&
name|columnCount
operator|==
name|columnVectors
operator|.
name|length
condition|)
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|columnVectors
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|columnVectors
operator|!=
literal|null
condition|)
block|{
name|columnVectors
operator|=
operator|new
name|List
index|[
name|columnCount
index|]
expr_stmt|;
block|}
else|else
block|{
name|columnVectors
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|public
name|boolean
name|hasVectors
parameter_list|(
name|int
name|colIx
parameter_list|)
block|{
return|return
name|columnVectors
operator|!=
literal|null
operator|&&
name|columnVectors
index|[
name|colIx
index|]
operator|!=
literal|null
return|;
block|}
specifier|public
name|List
argument_list|<
name|ColumnVector
argument_list|>
name|getColumnVectors
parameter_list|(
name|int
name|colIx
parameter_list|)
block|{
if|if
condition|(
operator|!
name|hasVectors
argument_list|(
name|colIx
argument_list|)
condition|)
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"No data for column "
operator|+
name|colIx
argument_list|)
throw|;
return|return
name|columnVectors
index|[
name|colIx
index|]
return|;
block|}
block|}
block|}
end_interface

end_unit

