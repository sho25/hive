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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
import|;
end_import

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"mask_last_n"
argument_list|,
name|value
operator|=
literal|"masks the last n characters of the value"
argument_list|,
name|extended
operator|=
literal|"Examples:\n "
operator|+
literal|"  mask_last_n(ccn, 8)\n "
operator|+
literal|"  mask_last_n(ccn, 8, 'x', 'x', 'x')\n "
operator|+
literal|"Arguments:\n "
operator|+
literal|"  mask_last_n(value, charCount, upperChar, lowerChar, digitChar, otherChar, numberChar)\n "
operator|+
literal|"    value      - value to mask. Supported types: TINYINT, SMALLINT, INT, BIGINT, STRING, VARCHAR, CHAR\n "
operator|+
literal|"    charCount  - number of characters. Default value: 4\n "
operator|+
literal|"    upperChar  - character to replace upper-case characters with. Specify -1 to retain original character. Default value: 'X'\n "
operator|+
literal|"    lowerChar  - character to replace lower-case characters with. Specify -1 to retain original character. Default value: 'x'\n "
operator|+
literal|"    digitChar  - character to replace digit characters with. Specify -1 to retain original character. Default value: 'n'\n "
operator|+
literal|"    otherChar  - character to replace all other characters with. Specify -1 to retain original character. Default value: -1\n "
operator|+
literal|"     numberChar - character to replace digits in a number with. Valid values: 0-9. Default value: '1'\n "
argument_list|)
specifier|public
class|class
name|GenericUDFMaskLastN
extends|extends
name|BaseMaskUDF
block|{
specifier|public
specifier|static
specifier|final
name|String
name|UDF_NAME
init|=
literal|"mask_last_n"
decl_stmt|;
specifier|public
name|GenericUDFMaskLastN
parameter_list|()
block|{
name|super
argument_list|(
operator|new
name|MaskLastNTransformer
argument_list|()
argument_list|,
name|UDF_NAME
argument_list|)
expr_stmt|;
block|}
block|}
end_class

begin_class
class|class
name|MaskLastNTransformer
extends|extends
name|MaskTransformer
block|{
name|int
name|charCount
init|=
literal|4
decl_stmt|;
specifier|public
name|MaskLastNTransformer
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|,
name|int
name|argsStartIdx
parameter_list|)
block|{
name|super
operator|.
name|init
argument_list|(
name|arguments
argument_list|,
name|argsStartIdx
operator|+
literal|1
argument_list|)
expr_stmt|;
comment|// first argument is charCount, which is consumed in this method below
name|charCount
operator|=
name|getIntArg
argument_list|(
name|arguments
argument_list|,
name|argsStartIdx
argument_list|,
literal|4
argument_list|)
expr_stmt|;
if|if
condition|(
name|charCount
operator|<
literal|0
condition|)
block|{
name|charCount
operator|=
literal|0
expr_stmt|;
block|}
block|}
annotation|@
name|Override
name|String
name|transform
parameter_list|(
specifier|final
name|String
name|value
parameter_list|)
block|{
specifier|final
name|StringBuilder
name|ret
init|=
operator|new
name|StringBuilder
argument_list|(
name|value
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|int
name|startIdx
init|=
name|value
operator|.
name|length
argument_list|()
operator|<=
name|charCount
condition|?
literal|0
else|:
operator|(
name|value
operator|.
name|length
argument_list|()
operator|-
name|charCount
operator|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|startIdx
condition|;
name|i
operator|++
control|)
block|{
name|ret
operator|.
name|appendCodePoint
argument_list|(
name|value
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
name|startIdx
init|;
name|i
operator|<
name|value
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ret
operator|.
name|appendCodePoint
argument_list|(
name|transformChar
argument_list|(
name|value
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
name|Byte
name|transform
parameter_list|(
specifier|final
name|Byte
name|value
parameter_list|)
block|{
name|byte
name|val
init|=
name|value
decl_stmt|;
if|if
condition|(
name|value
operator|<
literal|0
condition|)
block|{
name|val
operator|*=
operator|-
literal|1
expr_stmt|;
block|}
name|byte
name|ret
init|=
literal|0
decl_stmt|;
name|int
name|pos
init|=
literal|1
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|val
operator|!=
literal|0
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|<
name|charCount
condition|)
block|{
comment|// mask this digit
name|ret
operator|+=
name|maskedNumber
operator|*
name|pos
expr_stmt|;
block|}
else|else
block|{
comment|//retain this digit
name|ret
operator|+=
operator|(
name|val
operator|%
literal|10
operator|)
operator|*
name|pos
expr_stmt|;
block|}
name|val
operator|/=
literal|10
expr_stmt|;
name|pos
operator|*=
literal|10
expr_stmt|;
block|}
if|if
condition|(
name|value
operator|<
literal|0
condition|)
block|{
name|ret
operator|*=
operator|-
literal|1
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
annotation|@
name|Override
name|Short
name|transform
parameter_list|(
specifier|final
name|Short
name|value
parameter_list|)
block|{
name|short
name|val
init|=
name|value
decl_stmt|;
if|if
condition|(
name|value
operator|<
literal|0
condition|)
block|{
name|val
operator|*=
operator|-
literal|1
expr_stmt|;
block|}
name|short
name|ret
init|=
literal|0
decl_stmt|;
name|int
name|pos
init|=
literal|1
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|val
operator|!=
literal|0
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|<
name|charCount
condition|)
block|{
comment|// mask this digit
name|ret
operator|+=
name|maskedNumber
operator|*
name|pos
expr_stmt|;
block|}
else|else
block|{
comment|// retain this digit
name|ret
operator|+=
operator|(
name|val
operator|%
literal|10
operator|)
operator|*
name|pos
expr_stmt|;
block|}
name|val
operator|/=
literal|10
expr_stmt|;
name|pos
operator|*=
literal|10
expr_stmt|;
block|}
if|if
condition|(
name|value
operator|<
literal|0
condition|)
block|{
name|ret
operator|*=
operator|-
literal|1
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
annotation|@
name|Override
name|Integer
name|transform
parameter_list|(
specifier|final
name|Integer
name|value
parameter_list|)
block|{
name|int
name|val
init|=
name|value
decl_stmt|;
if|if
condition|(
name|value
operator|<
literal|0
condition|)
block|{
name|val
operator|*=
operator|-
literal|1
expr_stmt|;
block|}
name|int
name|ret
init|=
literal|0
decl_stmt|;
name|int
name|pos
init|=
literal|1
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|val
operator|!=
literal|0
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|<
name|charCount
condition|)
block|{
comment|// mask this digit
name|ret
operator|+=
name|maskedNumber
operator|*
name|pos
expr_stmt|;
block|}
else|else
block|{
comment|// retain this digit
name|ret
operator|+=
operator|(
name|val
operator|%
literal|10
operator|)
operator|*
name|pos
expr_stmt|;
block|}
name|val
operator|/=
literal|10
expr_stmt|;
name|pos
operator|*=
literal|10
expr_stmt|;
block|}
if|if
condition|(
name|value
operator|<
literal|0
condition|)
block|{
name|ret
operator|*=
operator|-
literal|1
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
annotation|@
name|Override
name|Long
name|transform
parameter_list|(
specifier|final
name|Long
name|value
parameter_list|)
block|{
name|long
name|val
init|=
name|value
decl_stmt|;
if|if
condition|(
name|value
operator|<
literal|0
condition|)
block|{
name|val
operator|*=
operator|-
literal|1
expr_stmt|;
block|}
name|long
name|ret
init|=
literal|0
decl_stmt|;
name|long
name|pos
init|=
literal|1
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|val
operator|!=
literal|0
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|<
name|charCount
condition|)
block|{
comment|// mask this digit
name|ret
operator|+=
name|maskedNumber
operator|*
name|pos
expr_stmt|;
block|}
else|else
block|{
comment|// retain this digit
name|ret
operator|+=
operator|(
name|val
operator|%
literal|10
operator|)
operator|*
name|pos
expr_stmt|;
block|}
name|val
operator|/=
literal|10
expr_stmt|;
name|pos
operator|*=
literal|10
expr_stmt|;
block|}
if|if
condition|(
name|value
operator|<
literal|0
condition|)
block|{
name|ret
operator|*=
operator|-
literal|1
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
block|}
end_class

end_unit

