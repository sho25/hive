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
name|contrib
operator|.
name|util
operator|.
name|typedbytes
package|;
end_package

begin_comment
comment|/**  * The possible type codes.  */
end_comment

begin_enum
specifier|public
enum|enum
name|Type
block|{
comment|// codes for supported types (< 50):
name|BYTES
argument_list|(
literal|0
argument_list|)
block|,
name|BYTE
argument_list|(
literal|1
argument_list|)
block|,
name|BOOL
argument_list|(
literal|2
argument_list|)
block|,
name|INT
argument_list|(
literal|3
argument_list|)
block|,
name|LONG
argument_list|(
literal|4
argument_list|)
block|,
name|FLOAT
argument_list|(
literal|5
argument_list|)
block|,
name|DOUBLE
argument_list|(
literal|6
argument_list|)
block|,
name|STRING
argument_list|(
literal|7
argument_list|)
block|,
name|VECTOR
argument_list|(
literal|8
argument_list|)
block|,
name|LIST
argument_list|(
literal|9
argument_list|)
block|,
name|MAP
argument_list|(
literal|10
argument_list|)
block|,
name|SHORT
argument_list|(
literal|11
argument_list|)
block|,
name|NULL
argument_list|(
literal|12
argument_list|)
block|,
comment|// application-specific codes (50-200):
name|WRITABLE
argument_list|(
literal|50
argument_list|)
block|,
name|ENDOFRECORD
argument_list|(
literal|177
argument_list|)
block|,
comment|// low-level codes (> 200):
name|MARKER
argument_list|(
literal|255
argument_list|)
block|;
specifier|final
name|int
name|code
decl_stmt|;
name|Type
parameter_list|(
name|int
name|code
parameter_list|)
block|{
name|this
operator|.
name|code
operator|=
name|code
expr_stmt|;
block|}
block|}
end_enum

end_unit

