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
operator|.
name|generic
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
name|UDFArgumentException
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
name|metadata
operator|.
name|HiveException
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
name|session
operator|.
name|SessionState
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
name|udf
operator|.
name|UDFType
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
name|LongWritable
import|;
end_import

begin_class
annotation|@
name|UDFType
argument_list|(
name|deterministic
operator|=
literal|true
argument_list|)
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"unix_timestamp"
argument_list|,
name|value
operator|=
literal|"_FUNC_(date[, pattern]) - Converts the time to a number"
argument_list|,
name|extended
operator|=
literal|"Converts the specified time to number of seconds "
operator|+
literal|"since 1970-01-01. The _FUNC_(void) overload is deprecated, use current_timestamp."
argument_list|)
specifier|public
class|class
name|GenericUDFUnixTimeStamp
extends|extends
name|GenericUDFToUnixTimeStamp
block|{
specifier|private
name|LongWritable
name|currentInstant
decl_stmt|;
comment|// retValue is transient so store this separately.
annotation|@
name|Override
specifier|protected
name|void
name|initializeInput
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
if|if
condition|(
name|arguments
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|super
operator|.
name|initializeInput
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|currentInstant
operator|==
literal|null
condition|)
block|{
name|currentInstant
operator|=
operator|new
name|LongWritable
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|currentInstant
operator|.
name|set
argument_list|(
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getQueryCurrentTimestamp
argument_list|()
operator|.
name|toEpochMilli
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|msg
init|=
literal|"unix_timestamp(void) is deprecated. Use current_timestamp instead."
decl_stmt|;
name|SessionState
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
name|msg
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|protected
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"unix_timestamp"
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|evaluate
parameter_list|(
name|DeferredObject
index|[]
name|arguments
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
operator|(
name|arguments
operator|.
name|length
operator|==
literal|0
operator|)
condition|?
name|currentInstant
else|:
name|super
operator|.
name|evaluate
argument_list|(
name|arguments
argument_list|)
return|;
block|}
block|}
end_class

end_unit

