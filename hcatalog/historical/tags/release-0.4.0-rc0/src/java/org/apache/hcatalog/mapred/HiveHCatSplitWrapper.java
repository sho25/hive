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
name|hcatalog
operator|.
name|mapred
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|fs
operator|.
name|Path
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
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|WritableUtils
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
name|mapred
operator|.
name|FileSplit
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
name|mapred
operator|.
name|InputSplit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|mapreduce
operator|.
name|HCatSplit
import|;
end_import

begin_comment
comment|/**  * Even though HiveInputSplit expects an InputSplit to wrap, it  * expects getPath() to work from the underlying split. And since  * that's populated by HiveInputSplit only if the underlying  * split is a FileSplit, the HCatSplit that goes to Hive needs  * to be a FileSplit. And since FileSplit is a class, and   * mapreduce.InputSplit is also a class, we can't do the trick  * where we implement mapred.inputSplit and extend mapred.InputSplit.  *   * Thus, we compose the other HCatSplit, and work with it.  *   * Also, this means that reading HCat through Hive will only work  * when the underlying InputFormat's InputSplit has implemented  * a getPath() - either by subclassing FileSplit, or by itself -  * we make a best effort attempt to call a getPath() via reflection,  * but if that doesn't work, this isn't going to work.  *  */
end_comment

begin_class
specifier|public
class|class
name|HiveHCatSplitWrapper
extends|extends
name|FileSplit
implements|implements
name|InputSplit
block|{
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HiveHCatSplitWrapper
operator|.
name|class
argument_list|)
decl_stmt|;
name|HCatSplit
name|hsplit
decl_stmt|;
specifier|public
name|HiveHCatSplitWrapper
parameter_list|()
block|{
name|super
argument_list|(
operator|(
name|Path
operator|)
literal|null
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveHCatSplitWrapper
parameter_list|(
name|HCatSplit
name|hsplit
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|this
operator|.
name|hsplit
operator|=
name|hsplit
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|input
parameter_list|)
throws|throws
name|IOException
block|{
name|hsplit
operator|=
operator|new
name|HCatSplit
argument_list|()
expr_stmt|;
name|hsplit
operator|.
name|readFields
argument_list|(
name|input
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|output
parameter_list|)
throws|throws
name|IOException
block|{
name|hsplit
operator|.
name|write
argument_list|(
name|output
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLength
parameter_list|()
block|{
return|return
name|hsplit
operator|.
name|getLength
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getLocations
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|hsplit
operator|.
name|getLocations
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Path
name|getPath
parameter_list|()
block|{
comment|/**      * This function is the reason this class exists at all.      * See class description for why.      */
if|if
condition|(
name|hsplit
operator|.
name|getBaseSplit
argument_list|()
operator|instanceof
name|FileSplit
condition|)
block|{
comment|// if baseSplit is a FileSplit, then return that.
return|return
operator|(
operator|(
name|FileSplit
operator|)
name|hsplit
operator|.
name|getBaseSplit
argument_list|()
operator|)
operator|.
name|getPath
argument_list|()
return|;
block|}
else|else
block|{
comment|// use reflection to try and determine if underlying class has a getPath() method that returns a path
name|Class
argument_list|<
name|?
argument_list|>
name|c
init|=
name|hsplit
operator|.
name|getBaseSplit
argument_list|()
operator|.
name|getClass
argument_list|()
decl_stmt|;
try|try
block|{
return|return
call|(
name|Path
call|)
argument_list|(
name|c
operator|.
name|getMethod
argument_list|(
literal|"getPath"
argument_list|)
argument_list|)
operator|.
name|invoke
argument_list|(
name|hsplit
operator|.
name|getBaseSplit
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|HCatUtil
operator|.
name|logStackTrace
argument_list|(
name|LOG
argument_list|)
expr_stmt|;
comment|// not much we can do - default exit will return null Path
block|}
block|}
name|LOG
operator|.
name|error
argument_list|(
literal|"Returning empty path from getPath(), Hive will not be happy."
argument_list|)
expr_stmt|;
return|return
operator|new
name|Path
argument_list|(
literal|""
argument_list|)
return|;
comment|// This will cause hive to error, but we can't do anything for that situation.
block|}
specifier|public
name|HCatSplit
name|getHCatSplit
parameter_list|()
block|{
return|return
name|hsplit
return|;
block|}
block|}
end_class

end_unit

