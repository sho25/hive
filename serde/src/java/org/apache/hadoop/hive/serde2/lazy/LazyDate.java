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
name|serde2
operator|.
name|lazy
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
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
import|import
name|java
operator|.
name|sql
operator|.
name|Date
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
name|io
operator|.
name|DateWritable
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyDateObjectInspector
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
comment|/**  *  * LazyDate.  * Serializes and deserializes a Date in the SQL date format  *  *    YYYY-MM-DD  *  */
end_comment

begin_class
specifier|public
class|class
name|LazyDate
extends|extends
name|LazyPrimitive
argument_list|<
name|LazyDateObjectInspector
argument_list|,
name|DateWritable
argument_list|>
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
name|LazyDate
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|LazyDate
parameter_list|(
name|LazyDateObjectInspector
name|oi
parameter_list|)
block|{
name|super
argument_list|(
name|oi
argument_list|)
expr_stmt|;
name|data
operator|=
operator|new
name|DateWritable
argument_list|()
expr_stmt|;
block|}
specifier|public
name|LazyDate
parameter_list|(
name|LazyDate
name|copy
parameter_list|)
block|{
name|super
argument_list|(
name|copy
argument_list|)
expr_stmt|;
name|data
operator|=
operator|new
name|DateWritable
argument_list|(
name|copy
operator|.
name|data
argument_list|)
expr_stmt|;
block|}
comment|/**    * Initializes LazyDate object by interpreting the input bytes as a SQL date string.    *    * @param bytes    * @param start    * @param length    */
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|ByteArrayRef
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|String
name|s
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|LazyUtils
operator|.
name|isDateMaybe
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
condition|)
block|{
name|isNull
operator|=
literal|true
expr_stmt|;
return|return;
block|}
try|try
block|{
name|s
operator|=
name|Text
operator|.
name|decode
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|data
operator|.
name|set
argument_list|(
name|Date
operator|.
name|valueOf
argument_list|(
name|s
argument_list|)
argument_list|)
expr_stmt|;
name|isNull
operator|=
literal|false
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|isNull
operator|=
literal|true
expr_stmt|;
name|logExceptionMessage
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|,
literal|"DATE"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Writes a Date in SQL date format to the output stream.    * @param out    *          The output stream    * @param i    *          The Date to write    * @throws IOException    */
specifier|public
specifier|static
name|void
name|writeUTF8
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|DateWritable
name|d
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteBuffer
name|b
init|=
name|Text
operator|.
name|encode
argument_list|(
name|d
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|out
operator|.
name|write
argument_list|(
name|b
operator|.
name|array
argument_list|()
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|limit
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

