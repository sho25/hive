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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|StripeInformation
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
name|orc
operator|.
name|encoded
operator|.
name|Reader
operator|.
name|OrcEncodedColumnBatch
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|impl
operator|.
name|OrcIndex
import|;
end_import

begin_interface
specifier|public
interface|interface
name|EncodedReader
block|{
comment|/**    * Reads encoded data from ORC file.    * @param stripeIx Index of the stripe to read.    * @param stripe Externally provided metadata (from metadata reader or external cache).    * @param index Externally provided metadata (from metadata reader or external cache).    * @param encodings Externally provided metadata (from metadata reader or external cache).    * @param streams Externally provided metadata (from metadata reader or external cache).    * @param physicalFileIncludes The array of booleans indicating whether each column should be read.    * @param rgs Arrays of rgs, per column set to true in included, that are to be read.    *               null in each respective position means all rgs for this column need to be read.    * @param consumer The sink for data that has been read.    */
name|void
name|readEncodedColumns
parameter_list|(
name|int
name|stripeIx
parameter_list|,
name|StripeInformation
name|stripe
parameter_list|,
name|OrcProto
operator|.
name|RowIndex
index|[]
name|index
parameter_list|,
name|List
argument_list|<
name|OrcProto
operator|.
name|ColumnEncoding
argument_list|>
name|encodings
parameter_list|,
name|List
argument_list|<
name|OrcProto
operator|.
name|Stream
argument_list|>
name|streams
parameter_list|,
name|boolean
index|[]
name|physicalFileIncludes
parameter_list|,
name|boolean
index|[]
name|rgs
parameter_list|,
name|Consumer
argument_list|<
name|OrcEncodedColumnBatch
argument_list|>
name|consumer
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Closes the reader.    */
name|void
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Controls the low-level debug tracing. (Hopefully) allows for optimization where tracing    * checks are entirely eliminated because this method is called with constant value, similar    * to just checking the constant in the first place.    */
name|void
name|setTracing
parameter_list|(
name|boolean
name|isEnabled
parameter_list|)
function_decl|;
comment|/**    * Read the indexes from ORC file.    * @param index The destination with pre-allocated arrays to put index data into.    * @param stripe Externally provided metadata (from metadata reader or external cache).    * @param streams Externally provided metadata (from metadata reader or external cache).    * @param included The array of booleans indicating whether each column should be read.     * @param sargColumns The array of booleans indicating whether each column's    *                    bloom filters should be read.    */
name|void
name|readIndexStreams
parameter_list|(
name|OrcIndex
name|index
parameter_list|,
name|StripeInformation
name|stripe
parameter_list|,
name|List
argument_list|<
name|OrcProto
operator|.
name|Stream
argument_list|>
name|streams
parameter_list|,
name|boolean
index|[]
name|included
parameter_list|,
name|boolean
index|[]
name|sargColumns
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|void
name|setStopped
parameter_list|(
name|AtomicBoolean
name|isStopped
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

