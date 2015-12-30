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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * UDFRpad.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"rpad"
argument_list|,
name|value
operator|=
literal|"_FUNC_(str, len, pad) - "
operator|+
literal|"Returns str, right-padded with pad to a length of len"
argument_list|,
name|extended
operator|=
literal|"If str is longer than len, the return value is shortened to "
operator|+
literal|"len characters.\n"
operator|+
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_('hi', 5, '??') FROM src LIMIT 1;\n"
operator|+
literal|"  'hi???'"
operator|+
literal|"> SELECT _FUNC_('hi', 1, '??') FROM src LIMIT 1;\n"
operator|+
literal|"  'h'"
argument_list|)
specifier|public
class|class
name|GenericUDFRpad
extends|extends
name|GenericUDFBasePad
block|{
specifier|public
name|GenericUDFRpad
parameter_list|()
block|{
name|super
argument_list|(
literal|"rpad"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|performOp
parameter_list|(
name|StringBuilder
name|builder
parameter_list|,
name|int
name|len
parameter_list|,
name|String
name|str
parameter_list|,
name|String
name|pad
parameter_list|)
block|{
name|int
name|pos
init|=
name|str
operator|.
name|length
argument_list|()
decl_stmt|;
comment|// Copy the text
name|builder
operator|.
name|append
argument_list|(
name|str
argument_list|,
literal|0
argument_list|,
name|pos
argument_list|)
expr_stmt|;
comment|// Copy the padding
while|while
condition|(
name|pos
operator|<
name|len
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
name|pad
argument_list|)
expr_stmt|;
name|pos
operator|+=
name|pad
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|setLength
argument_list|(
name|len
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

