begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|common
operator|.
name|ndv
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
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
name|java
operator|.
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|codec
operator|.
name|binary
operator|.
name|Base64
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
name|common
operator|.
name|ndv
operator|.
name|fm
operator|.
name|FMSketch
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
name|common
operator|.
name|ndv
operator|.
name|fm
operator|.
name|FMSketchUtils
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
name|common
operator|.
name|ndv
operator|.
name|hll
operator|.
name|HyperLogLog
import|;
end_import

begin_class
specifier|public
class|class
name|NumDistinctValueEstimatorFactory
block|{
specifier|private
name|NumDistinctValueEstimatorFactory
parameter_list|()
block|{   }
specifier|private
specifier|static
name|boolean
name|isFMSketch
parameter_list|(
name|String
name|s
parameter_list|)
throws|throws
name|IOException
block|{
name|InputStream
name|in
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|Base64
operator|.
name|decodeBase64
argument_list|(
name|s
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|magic
init|=
operator|new
name|byte
index|[
literal|2
index|]
decl_stmt|;
name|magic
index|[
literal|0
index|]
operator|=
operator|(
name|byte
operator|)
name|in
operator|.
name|read
argument_list|()
expr_stmt|;
name|magic
index|[
literal|1
index|]
operator|=
operator|(
name|byte
operator|)
name|in
operator|.
name|read
argument_list|()
expr_stmt|;
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|Arrays
operator|.
name|equals
argument_list|(
name|magic
argument_list|,
name|FMSketchUtils
operator|.
name|MAGIC
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|NumDistinctValueEstimator
name|getNumDistinctValueEstimator
parameter_list|(
name|String
name|s
parameter_list|)
block|{
comment|// Right now we assume only FM and HLL are available.
try|try
block|{
if|if
condition|(
name|isFMSketch
argument_list|(
name|s
argument_list|)
condition|)
block|{
return|return
name|FMSketchUtils
operator|.
name|deserializeFM
argument_list|(
name|s
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
operator|.
name|deserialize
argument_list|(
name|s
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|NumDistinctValueEstimator
name|getEmptyNumDistinctValueEstimator
parameter_list|(
name|NumDistinctValueEstimator
name|n
parameter_list|)
block|{
if|if
condition|(
name|n
operator|instanceof
name|FMSketch
condition|)
block|{
return|return
operator|new
name|FMSketch
argument_list|(
operator|(
operator|(
name|FMSketch
operator|)
name|n
operator|)
operator|.
name|getnumBitVectors
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
return|;
block|}
block|}
specifier|public
specifier|static
name|NumDistinctValueEstimator
name|getEmptyNumDistinctValueEstimator
parameter_list|(
name|String
name|func
parameter_list|,
name|int
name|numBitVectors
parameter_list|)
block|{
if|if
condition|(
literal|"fm"
operator|.
name|equals
argument_list|(
name|func
operator|.
name|toLowerCase
argument_list|()
argument_list|)
condition|)
block|{
return|return
operator|new
name|FMSketch
argument_list|(
name|numBitVectors
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"hll"
operator|.
name|equals
argument_list|(
name|func
operator|.
name|toLowerCase
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Can not recognize "
operator|+
name|func
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

