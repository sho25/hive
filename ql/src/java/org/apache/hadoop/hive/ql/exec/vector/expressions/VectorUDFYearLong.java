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
name|exec
operator|.
name|vector
operator|.
name|expressions
package|;
end_package

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
name|Calendar
import|;
end_import

begin_class
specifier|public
specifier|final
class|class
name|VectorUDFYearLong
extends|extends
name|VectorUDFTimestampFieldLong
block|{
comment|/* year boundaries in nanoseconds */
specifier|static
specifier|final
name|long
index|[]
name|yearBoundaries
decl_stmt|;
specifier|static
specifier|final
name|int
name|minYear
init|=
literal|1901
decl_stmt|;
specifier|static
specifier|final
name|int
name|maxYear
init|=
literal|2038
decl_stmt|;
static|static
block|{
name|yearBoundaries
operator|=
operator|new
name|long
index|[
name|maxYear
operator|-
name|minYear
index|]
expr_stmt|;
name|Calendar
name|c
init|=
name|Calendar
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|c
operator|.
name|setTimeInMillis
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// c.set doesn't reset millis
comment|/* 1901 Jan is not with in range */
for|for
control|(
name|int
name|year
init|=
name|minYear
operator|+
literal|1
init|;
name|year
operator|<=
literal|2038
condition|;
name|year
operator|++
control|)
block|{
name|c
operator|.
name|set
argument_list|(
name|year
argument_list|,
name|Calendar
operator|.
name|JANUARY
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|yearBoundaries
index|[
name|year
operator|-
name|minYear
operator|-
literal|1
index|]
operator|=
name|c
operator|.
name|getTimeInMillis
argument_list|()
operator|*
literal|1000
operator|*
literal|1000
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|long
name|getField
parameter_list|(
name|long
name|time
parameter_list|)
block|{
comment|/* binarySearch is faster than a loop doing a[i] (no array out of bounds checks) */
name|int
name|year
init|=
name|Arrays
operator|.
name|binarySearch
argument_list|(
name|yearBoundaries
argument_list|,
name|time
argument_list|)
decl_stmt|;
if|if
condition|(
name|year
operator|>=
literal|0
condition|)
block|{
comment|/* 0 == 1902 etc */
return|return
name|minYear
operator|+
literal|1
operator|+
name|year
return|;
block|}
else|else
block|{
comment|/* -1 == 1901, -2 == 1902 */
return|return
name|minYear
operator|-
literal|1
operator|-
name|year
return|;
block|}
block|}
specifier|public
name|VectorUDFYearLong
parameter_list|(
name|int
name|colNum
parameter_list|,
name|int
name|outputColumn
parameter_list|)
block|{
name|super
argument_list|(
name|Calendar
operator|.
name|YEAR
argument_list|,
name|colNum
argument_list|,
name|outputColumn
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

