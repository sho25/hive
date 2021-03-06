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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|io
operator|.
name|HiveIntervalDayTimeWritable
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
name|lazybinary
operator|.
name|LazyBinaryUtils
operator|.
name|VLong
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
name|WritableHiveIntervalDayTimeObjectInspector
import|;
end_import

begin_comment
comment|/**  * LazyBinaryHiveIntervalDayTime  * A LazyBinaryObject that encodes a HiveIntervalDayTime  */
end_comment

begin_class
specifier|public
class|class
name|LazyBinaryHiveIntervalDayTime
extends|extends
name|LazyBinaryPrimitive
argument_list|<
name|WritableHiveIntervalDayTimeObjectInspector
argument_list|,
name|HiveIntervalDayTimeWritable
argument_list|>
block|{
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|LazyBinaryHiveIntervalDayTime
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Reusable member for decoding integer.    */
name|VInt
name|vInt
init|=
operator|new
name|LazyBinaryUtils
operator|.
name|VInt
argument_list|()
decl_stmt|;
name|VLong
name|vLong
init|=
operator|new
name|LazyBinaryUtils
operator|.
name|VLong
argument_list|()
decl_stmt|;
name|LazyBinaryHiveIntervalDayTime
parameter_list|(
name|WritableHiveIntervalDayTimeObjectInspector
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
name|HiveIntervalDayTimeWritable
argument_list|()
expr_stmt|;
block|}
name|LazyBinaryHiveIntervalDayTime
parameter_list|(
name|LazyBinaryHiveIntervalDayTime
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
name|HiveIntervalDayTimeWritable
argument_list|(
name|copy
operator|.
name|data
argument_list|)
expr_stmt|;
block|}
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
name|data
operator|.
name|setFromBytes
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
argument_list|,
name|start
argument_list|,
name|length
argument_list|,
name|vInt
argument_list|,
name|vLong
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

