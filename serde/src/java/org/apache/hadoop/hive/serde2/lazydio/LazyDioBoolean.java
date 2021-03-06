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
name|lazydio
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
import|;
end_import

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
name|ByteStream
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
name|ByteArrayRef
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
name|LazyBoolean
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
name|LazyBooleanObjectInspector
import|;
end_import

begin_comment
comment|/**  * LazyBooleanBinary for storing a boolean value as an BooleanWritable. This class complements class  * LazyBoolean. It's primary difference is the {@link #init(ByteArrayRef, int, int)} method, which  * reads the boolean value stored from the default binary format.  */
end_comment

begin_class
specifier|public
class|class
name|LazyDioBoolean
extends|extends
name|LazyBoolean
block|{
specifier|private
name|ByteStream
operator|.
name|Input
name|in
decl_stmt|;
specifier|private
name|DataInputStream
name|din
decl_stmt|;
specifier|public
name|LazyDioBoolean
parameter_list|(
name|LazyBooleanObjectInspector
name|oi
parameter_list|)
block|{
name|super
argument_list|(
name|oi
argument_list|)
expr_stmt|;
block|}
specifier|public
name|LazyDioBoolean
parameter_list|(
name|LazyDioBoolean
name|copy
parameter_list|)
block|{
name|super
argument_list|(
name|copy
argument_list|)
expr_stmt|;
block|}
comment|/* (non-Javadoc)    * This provides a LazyBoolean like class which can be initialized from data stored in a    * binary format.    *    * @see org.apache.hadoop.hive.serde2.lazy.LazyObject#init    *        (org.apache.hadoop.hive.serde2.lazy.ByteArrayRef, int, int)    */
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
name|boolean
name|value
init|=
literal|false
decl_stmt|;
try|try
block|{
name|in
operator|=
operator|new
name|ByteStream
operator|.
name|Input
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
name|din
operator|=
operator|new
name|DataInputStream
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|value
operator|=
name|din
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|data
operator|.
name|set
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|isNull
operator|=
literal|false
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|isNull
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
try|try
block|{
name|din
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// swallow exception
block|}
try|try
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// swallow exception
block|}
block|}
block|}
block|}
end_class

end_unit

