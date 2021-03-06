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
name|accumulo
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|client
operator|.
name|lexicoder
operator|.
name|BigIntegerLexicoder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|client
operator|.
name|lexicoder
operator|.
name|DoubleLexicoder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|client
operator|.
name|lexicoder
operator|.
name|IntegerLexicoder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|client
operator|.
name|lexicoder
operator|.
name|LongLexicoder
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
name|serde
operator|.
name|serdeConstants
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigInteger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
operator|.
name|UTF_8
import|;
end_import

begin_comment
comment|/**  * Utility class to encode index values for accumulo.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|AccumuloIndexLexicoder
block|{
specifier|private
specifier|static
specifier|final
name|IntegerLexicoder
name|INTEGER_LEXICODER
init|=
operator|new
name|IntegerLexicoder
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|DoubleLexicoder
name|DOUBLE_LEXICODER
init|=
operator|new
name|DoubleLexicoder
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|LongLexicoder
name|LONG_LEXICODER
init|=
operator|new
name|LongLexicoder
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|BigIntegerLexicoder
name|BIG_INTEGER_LEXICODER
init|=
operator|new
name|BigIntegerLexicoder
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DIM_PAT
init|=
literal|"[(]+.*"
decl_stmt|;
specifier|private
name|AccumuloIndexLexicoder
parameter_list|()
block|{
comment|// hide constructor
block|}
specifier|public
specifier|static
name|String
name|getRawType
parameter_list|(
name|String
name|hiveType
parameter_list|)
block|{
if|if
condition|(
name|hiveType
operator|!=
literal|null
condition|)
block|{
return|return
name|hiveType
operator|.
name|toLowerCase
argument_list|()
operator|.
name|replaceFirst
argument_list|(
name|DIM_PAT
argument_list|,
literal|""
argument_list|)
operator|.
name|trim
argument_list|()
return|;
block|}
return|return
name|hiveType
return|;
block|}
specifier|public
specifier|static
name|byte
index|[]
name|encodeValue
parameter_list|(
name|byte
index|[]
name|value
parameter_list|,
name|String
name|hiveType
parameter_list|,
name|boolean
name|stringEncoded
parameter_list|)
block|{
if|if
condition|(
name|stringEncoded
condition|)
block|{
return|return
name|encodeStringValue
argument_list|(
name|value
argument_list|,
name|hiveType
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|encodeBinaryValue
argument_list|(
name|value
argument_list|,
name|hiveType
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
name|byte
index|[]
name|encodeStringValue
parameter_list|(
name|byte
index|[]
name|value
parameter_list|,
name|String
name|hiveType
parameter_list|)
block|{
name|String
name|rawType
init|=
name|getRawType
argument_list|(
name|hiveType
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|rawType
condition|)
block|{
case|case
name|serdeConstants
operator|.
name|BOOLEAN_TYPE_NAME
case|:
return|return
name|Boolean
operator|.
name|valueOf
argument_list|(
operator|new
name|String
argument_list|(
name|value
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|(
name|UTF_8
argument_list|)
return|;
case|case
name|serdeConstants
operator|.
name|SMALLINT_TYPE_NAME
case|:
case|case
name|serdeConstants
operator|.
name|TINYINT_TYPE_NAME
case|:
case|case
name|serdeConstants
operator|.
name|INT_TYPE_NAME
case|:
return|return
name|INTEGER_LEXICODER
operator|.
name|encode
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
operator|new
name|String
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
return|;
case|case
name|serdeConstants
operator|.
name|FLOAT_TYPE_NAME
case|:
case|case
name|serdeConstants
operator|.
name|DOUBLE_TYPE_NAME
case|:
return|return
name|DOUBLE_LEXICODER
operator|.
name|encode
argument_list|(
name|Double
operator|.
name|valueOf
argument_list|(
operator|new
name|String
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
return|;
case|case
name|serdeConstants
operator|.
name|BIGINT_TYPE_NAME
case|:
return|return
name|BIG_INTEGER_LEXICODER
operator|.
name|encode
argument_list|(
operator|new
name|BigInteger
argument_list|(
operator|new
name|String
argument_list|(
name|value
argument_list|)
argument_list|,
literal|10
argument_list|)
argument_list|)
return|;
case|case
name|serdeConstants
operator|.
name|DECIMAL_TYPE_NAME
case|:
return|return
operator|new
name|String
argument_list|(
name|value
argument_list|)
operator|.
name|getBytes
argument_list|(
name|UTF_8
argument_list|)
return|;
default|default :
comment|// return the passed in string value
return|return
name|value
return|;
block|}
block|}
specifier|public
specifier|static
name|byte
index|[]
name|encodeBinaryValue
parameter_list|(
name|byte
index|[]
name|value
parameter_list|,
name|String
name|hiveType
parameter_list|)
block|{
name|String
name|rawType
init|=
name|getRawType
argument_list|(
name|hiveType
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|rawType
condition|)
block|{
case|case
name|serdeConstants
operator|.
name|BOOLEAN_TYPE_NAME
case|:
return|return
name|String
operator|.
name|valueOf
argument_list|(
name|value
index|[
literal|0
index|]
operator|==
literal|1
argument_list|)
operator|.
name|getBytes
argument_list|()
return|;
case|case
name|serdeConstants
operator|.
name|INT_TYPE_NAME
case|:
return|return
name|INTEGER_LEXICODER
operator|.
name|encode
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|value
argument_list|)
operator|.
name|asIntBuffer
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
return|;
case|case
name|serdeConstants
operator|.
name|SMALLINT_TYPE_NAME
case|:
return|return
name|INTEGER_LEXICODER
operator|.
name|encode
argument_list|(
call|(
name|int
call|)
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|value
argument_list|)
operator|.
name|asShortBuffer
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
return|;
case|case
name|serdeConstants
operator|.
name|TINYINT_TYPE_NAME
case|:
return|return
name|INTEGER_LEXICODER
operator|.
name|encode
argument_list|(
operator|(
name|int
operator|)
name|value
index|[
literal|0
index|]
argument_list|)
return|;
case|case
name|serdeConstants
operator|.
name|FLOAT_TYPE_NAME
case|:
return|return
name|DOUBLE_LEXICODER
operator|.
name|encode
argument_list|(
operator|(
name|double
operator|)
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|value
argument_list|)
operator|.
name|asFloatBuffer
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
return|;
case|case
name|serdeConstants
operator|.
name|DOUBLE_TYPE_NAME
case|:
return|return
name|DOUBLE_LEXICODER
operator|.
name|encode
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|value
argument_list|)
operator|.
name|asDoubleBuffer
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
return|;
case|case
name|serdeConstants
operator|.
name|BIGINT_TYPE_NAME
case|:
return|return
name|BIG_INTEGER_LEXICODER
operator|.
name|encode
argument_list|(
operator|new
name|BigInteger
argument_list|(
name|value
argument_list|)
argument_list|)
return|;
case|case
name|serdeConstants
operator|.
name|DECIMAL_TYPE_NAME
case|:
return|return
operator|new
name|String
argument_list|(
name|value
argument_list|)
operator|.
name|getBytes
argument_list|(
name|UTF_8
argument_list|)
return|;
default|default :
return|return
name|value
return|;
block|}
block|}
block|}
end_class

end_unit

