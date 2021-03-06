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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|Charset
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
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
name|conf
operator|.
name|Configuration
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
name|Writable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Charsets
import|;
end_import

begin_comment
comment|/**  * AbstractEncodingAwareSerDe aware the encoding from table properties,  * transform data from specified charset to UTF-8 during serialize, and  * transform data from UTF-8 to specified charset during deserialize.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractEncodingAwareSerDe
extends|extends
name|AbstractSerDe
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|AbstractEncodingAwareSerDe
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|Charset
name|charset
decl_stmt|;
annotation|@
name|Override
annotation|@
name|Deprecated
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Properties
name|tbl
parameter_list|)
throws|throws
name|SerDeException
block|{
name|charset
operator|=
name|Charset
operator|.
name|forName
argument_list|(
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_ENCODING
argument_list|,
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|charset
operator|.
name|equals
argument_list|(
name|Charsets
operator|.
name|ISO_8859_1
argument_list|)
operator|||
name|this
operator|.
name|charset
operator|.
name|equals
argument_list|(
name|Charsets
operator|.
name|US_ASCII
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"The data may not be properly converted to target charset "
operator|+
name|charset
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|Writable
name|serialize
parameter_list|(
name|Object
name|obj
parameter_list|,
name|ObjectInspector
name|objInspector
parameter_list|)
throws|throws
name|SerDeException
block|{
name|Writable
name|result
init|=
name|doSerialize
argument_list|(
name|obj
argument_list|,
name|objInspector
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|charset
operator|.
name|equals
argument_list|(
name|Charsets
operator|.
name|UTF_8
argument_list|)
condition|)
block|{
name|result
operator|=
name|transformFromUTF8
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * transform Writable data from UTF-8 to charset before serialize.    * @param blob    * @return    */
specifier|protected
specifier|abstract
name|Writable
name|transformFromUTF8
parameter_list|(
name|Writable
name|blob
parameter_list|)
function_decl|;
specifier|protected
specifier|abstract
name|Writable
name|doSerialize
parameter_list|(
name|Object
name|obj
parameter_list|,
name|ObjectInspector
name|objInspector
parameter_list|)
throws|throws
name|SerDeException
function_decl|;
annotation|@
name|Override
specifier|public
specifier|final
name|Object
name|deserialize
parameter_list|(
name|Writable
name|blob
parameter_list|)
throws|throws
name|SerDeException
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|charset
operator|.
name|equals
argument_list|(
name|Charsets
operator|.
name|UTF_8
argument_list|)
condition|)
block|{
name|blob
operator|=
name|transformToUTF8
argument_list|(
name|blob
argument_list|)
expr_stmt|;
block|}
return|return
name|doDeserialize
argument_list|(
name|blob
argument_list|)
return|;
block|}
comment|/**    * transform Writable data from charset to UTF-8 before doDeserialize.    * @param blob    * @return    */
specifier|protected
specifier|abstract
name|Writable
name|transformToUTF8
parameter_list|(
name|Writable
name|blob
parameter_list|)
function_decl|;
specifier|protected
specifier|abstract
name|Object
name|doDeserialize
parameter_list|(
name|Writable
name|blob
parameter_list|)
throws|throws
name|SerDeException
function_decl|;
block|}
end_class

end_unit

