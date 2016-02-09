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
name|common
operator|.
name|type
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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
name|TimeUnit
import|;
end_import

begin_class
specifier|public
class|class
name|RandomTypeUtil
block|{
specifier|public
specifier|static
specifier|final
name|long
name|NANOSECONDS_PER_SECOND
init|=
name|TimeUnit
operator|.
name|SECONDS
operator|.
name|toNanos
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|Timestamp
name|getRandTimestamp
parameter_list|(
name|Random
name|r
parameter_list|)
block|{
name|String
name|optionalNanos
init|=
literal|""
decl_stmt|;
if|if
condition|(
name|r
operator|.
name|nextInt
argument_list|(
literal|2
argument_list|)
operator|==
literal|1
condition|)
block|{
name|optionalNanos
operator|=
name|String
operator|.
name|format
argument_list|(
literal|".%09d"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
literal|0
operator|+
name|r
operator|.
name|nextInt
argument_list|(
operator|(
name|int
operator|)
name|NANOSECONDS_PER_SECOND
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|timestampStr
init|=
name|String
operator|.
name|format
argument_list|(
literal|"%04d-%02d-%02d %02d:%02d:%02d%s"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
literal|0
operator|+
name|r
operator|.
name|nextInt
argument_list|(
literal|10000
argument_list|)
argument_list|)
argument_list|,
comment|// year
name|Integer
operator|.
name|valueOf
argument_list|(
literal|1
operator|+
name|r
operator|.
name|nextInt
argument_list|(
literal|12
argument_list|)
argument_list|)
argument_list|,
comment|// month
name|Integer
operator|.
name|valueOf
argument_list|(
literal|1
operator|+
name|r
operator|.
name|nextInt
argument_list|(
literal|28
argument_list|)
argument_list|)
argument_list|,
comment|// day
name|Integer
operator|.
name|valueOf
argument_list|(
literal|0
operator|+
name|r
operator|.
name|nextInt
argument_list|(
literal|24
argument_list|)
argument_list|)
argument_list|,
comment|// hour
name|Integer
operator|.
name|valueOf
argument_list|(
literal|0
operator|+
name|r
operator|.
name|nextInt
argument_list|(
literal|60
argument_list|)
argument_list|)
argument_list|,
comment|// minute
name|Integer
operator|.
name|valueOf
argument_list|(
literal|0
operator|+
name|r
operator|.
name|nextInt
argument_list|(
literal|60
argument_list|)
argument_list|)
argument_list|,
comment|// second
name|optionalNanos
argument_list|)
decl_stmt|;
name|Timestamp
name|timestampVal
decl_stmt|;
try|try
block|{
name|timestampVal
operator|=
name|Timestamp
operator|.
name|valueOf
argument_list|(
name|timestampStr
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Timestamp string "
operator|+
name|timestampStr
operator|+
literal|" did not parse"
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
return|return
name|timestampVal
return|;
block|}
block|}
end_class

end_unit

