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
name|serde2
operator|.
name|lazybinary
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
name|serde2
operator|.
name|lazy
operator|.
name|ByteArrayRef
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
name|lazybinary
operator|.
name|LazyBinaryUtils
operator|.
name|VInt
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
name|primitive
operator|.
name|WritableIntObjectInspector
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
name|IntWritable
import|;
end_import

begin_comment
comment|/**  * LazyBinaryObject for integer which is serialized as VInt.  *   * @see LazyBinaryUtils#readVInt(byte[], int, VInt)  */
end_comment

begin_class
specifier|public
class|class
name|LazyBinaryInteger
extends|extends
name|LazyBinaryPrimitive
argument_list|<
name|WritableIntObjectInspector
argument_list|,
name|IntWritable
argument_list|>
block|{
name|LazyBinaryInteger
parameter_list|(
name|WritableIntObjectInspector
name|oi
parameter_list|)
block|{
name|super
argument_list|(
name|oi
argument_list|)
expr_stmt|;
name|data
operator|=
operator|new
name|IntWritable
argument_list|()
expr_stmt|;
block|}
name|LazyBinaryInteger
parameter_list|(
name|LazyBinaryInteger
name|copy
parameter_list|)
block|{
name|super
argument_list|(
name|copy
argument_list|)
expr_stmt|;
name|data
operator|=
operator|new
name|IntWritable
argument_list|(
name|copy
operator|.
name|data
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * The reusable vInt for decoding the integer.    */
name|VInt
name|vInt
init|=
operator|new
name|LazyBinaryUtils
operator|.
name|VInt
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|ByteArrayRef
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|LazyBinaryUtils
operator|.
name|readVInt
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
argument_list|,
name|start
argument_list|,
name|vInt
argument_list|)
expr_stmt|;
assert|assert
operator|(
name|length
operator|==
name|vInt
operator|.
name|length
operator|)
assert|;
name|data
operator|.
name|set
argument_list|(
name|vInt
operator|.
name|value
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

