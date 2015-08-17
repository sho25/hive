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
name|javax
operator|.
name|crypto
operator|.
name|Cipher
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

begin_comment
comment|/**  * GenericUDFAesEncrypt.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"aes_encrypt"
argument_list|,
name|value
operator|=
literal|"_FUNC_(input string/binary, key string/binary) - Encrypt input using AES."
argument_list|,
name|extended
operator|=
literal|"AES (Advanced Encryption Standard) algorithm. "
operator|+
literal|"Key lengths of 128, 192 or 256 bits can be used. 192 and 256 bits keys can be used if "
operator|+
literal|"Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files are installed. "
operator|+
literal|"If either argument is NULL or the key length is not one of the permitted values, the return value is NULL.\n"
operator|+
literal|"Example:> SELECT base64(_FUNC_('ABC', '1234567890123456'));\n 'y6Ss+zCYObpCbgfWfyNWTw=='"
argument_list|)
specifier|public
class|class
name|GenericUDFAesEncrypt
extends|extends
name|GenericUDFAesBase
block|{
annotation|@
name|Override
specifier|protected
name|int
name|getCipherMode
parameter_list|()
block|{
return|return
name|Cipher
operator|.
name|ENCRYPT_MODE
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|canParam0BeStr
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|getFuncName
parameter_list|()
block|{
return|return
literal|"aes_encrypt"
return|;
block|}
block|}
end_class

end_unit

