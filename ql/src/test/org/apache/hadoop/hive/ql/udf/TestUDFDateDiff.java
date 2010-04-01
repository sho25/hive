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
name|udf
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
name|io
operator|.
name|IntWritable
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

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TimeZone
import|;
end_import

begin_comment
comment|/**  * JUnit test for UDFDateDiff.  */
end_comment

begin_class
specifier|public
class|class
name|TestUDFDateDiff
extends|extends
name|TestCase
block|{
comment|/**      * Verify differences of dates crossing a daylight savings time change      * are correct.  The timezone tested is west coast US (PDT/PST) with a       * 1 hour shift back in time at 02:00 AM on 2009-10-31 and a      * 1 hour shift forward in time at 02:00 AM on 2010-03-14.      */
specifier|public
name|void
name|testDaylightChange
parameter_list|()
throws|throws
name|Exception
block|{
name|TimeZone
operator|.
name|setDefault
argument_list|(
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
literal|"America/Los_Angeles"
argument_list|)
argument_list|)
expr_stmt|;
comment|// time moves ahead an hour at 02:00 on 2009-10-31
comment|// which results in a 23 hour long day
name|Text
name|date1
init|=
operator|new
name|Text
argument_list|(
literal|"2009-11-01"
argument_list|)
decl_stmt|;
name|Text
name|date2
init|=
operator|new
name|Text
argument_list|(
literal|"2009-10-25"
argument_list|)
decl_stmt|;
name|IntWritable
name|result
init|=
operator|new
name|UDFDateDiff
argument_list|()
operator|.
name|evaluate
argument_list|(
name|date1
argument_list|,
name|date2
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|7
argument_list|,
name|result
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|=
operator|new
name|UDFDateDiff
argument_list|()
operator|.
name|evaluate
argument_list|(
name|date2
argument_list|,
name|date1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|7
argument_list|,
name|result
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// time moves back an hour at 02:00 on 2010-03-14
comment|// which results in a 25 hour long day
name|date1
operator|=
operator|new
name|Text
argument_list|(
literal|"2010-03-15"
argument_list|)
expr_stmt|;
name|date2
operator|=
operator|new
name|Text
argument_list|(
literal|"2010-03-08"
argument_list|)
expr_stmt|;
name|result
operator|=
operator|new
name|UDFDateDiff
argument_list|()
operator|.
name|evaluate
argument_list|(
name|date1
argument_list|,
name|date2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|7
argument_list|,
name|result
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|=
operator|new
name|UDFDateDiff
argument_list|()
operator|.
name|evaluate
argument_list|(
name|date2
argument_list|,
name|date1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|7
argument_list|,
name|result
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

