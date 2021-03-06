begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/**  * This file is back-ported from hadoop-0.19, to make sure hive can run  * with hadoop-0.17.  */
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
name|io
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
name|io
operator|.
name|WritableComparator
import|;
end_import

begin_comment
comment|/**  * Writable for Double values.  * This class was created before the Hadoop version of this class was available, and needs to  * be kept around for backward compatibility of third-party UDFs/SerDes. We should consider  * removing this class in favor of directly using the Hadoop one in the next major release.  */
end_comment

begin_class
specifier|public
class|class
name|DoubleWritable
extends|extends
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|DoubleWritable
block|{
specifier|public
name|DoubleWritable
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|DoubleWritable
parameter_list|(
name|double
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
static|static
block|{
comment|// register this comparator
name|WritableComparator
operator|.
name|define
argument_list|(
name|DoubleWritable
operator|.
name|class
argument_list|,
operator|new
name|Comparator
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

