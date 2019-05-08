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
name|serde2
operator|.
name|json
package|;
end_package

begin_comment
comment|/**  * Enums describing the available String->Bytes encoding available for JSON  * parsing. This base-64 variant is what most people would think of "the  * standard" Base64 encoding for JSON: the specific MIME content transfer  * encoding. The Raw String encoding produces an array of bytes by reading the  * JSON value as a String and transforming it using Java's String getBytes()  * method.  *  * @see String#getBytes(java.nio.charset.Charset)  */
end_comment

begin_enum
specifier|public
enum|enum
name|BinaryEncoding
block|{
name|RAWSTRING
block|,
name|BASE64
block|}
end_enum

end_unit

