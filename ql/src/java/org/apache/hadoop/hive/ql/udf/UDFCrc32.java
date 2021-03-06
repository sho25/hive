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
name|udf
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|zip
operator|.
name|CRC32
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
name|Description
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
name|UDF
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
name|BytesWritable
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
name|LongWritable
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
name|Text
import|;
end_import

begin_comment
comment|/**  * UDFCrc32.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"crc32"
argument_list|,
name|value
operator|=
literal|"_FUNC_(str or bin) - Computes a cyclic redundancy check value "
operator|+
literal|"for string or binary argument and returns bigint value."
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_('ABC');\n"
operator|+
literal|"  2743272264\n"
operator|+
literal|"> SELECT _FUNC_(binary('ABC'));\n"
operator|+
literal|"  2743272264"
argument_list|)
specifier|public
class|class
name|UDFCrc32
extends|extends
name|UDF
block|{
specifier|private
specifier|final
name|LongWritable
name|result
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|CRC32
name|crc32
init|=
operator|new
name|CRC32
argument_list|()
decl_stmt|;
comment|/**    * CRC32 for string    */
specifier|public
name|LongWritable
name|evaluate
parameter_list|(
name|Text
name|n
parameter_list|)
block|{
if|if
condition|(
name|n
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|crc32
operator|.
name|reset
argument_list|()
expr_stmt|;
name|crc32
operator|.
name|update
argument_list|(
name|n
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|n
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|set
argument_list|(
name|crc32
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * CRC32 for binary    */
specifier|public
name|LongWritable
name|evaluate
parameter_list|(
name|BytesWritable
name|b
parameter_list|)
block|{
if|if
condition|(
name|b
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|crc32
operator|.
name|reset
argument_list|()
expr_stmt|;
name|crc32
operator|.
name|update
argument_list|(
name|b
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|set
argument_list|(
name|crc32
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

