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
name|hcatalog
operator|.
name|pig
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
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|fs
operator|.
name|FileStatus
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
name|mapreduce
operator|.
name|Job
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
name|mapreduce
operator|.
name|RecordReader
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
name|data
operator|.
name|HCatRecord
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
name|data
operator|.
name|schema
operator|.
name|HCatSchema
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
name|InputJobInfo
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
name|PartInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|LoadFunc
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|LoadMetadata
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|LoadPushDown
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|PigException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|ResourceStatistics
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|backend
operator|.
name|executionengine
operator|.
name|ExecException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|backend
operator|.
name|hadoop
operator|.
name|executionengine
operator|.
name|mapReduceLayer
operator|.
name|PigSplit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|data
operator|.
name|Tuple
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|impl
operator|.
name|logicalLayer
operator|.
name|FrontendException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|impl
operator|.
name|util
operator|.
name|UDFContext
import|;
end_import

begin_comment
comment|/**  * Base class for HCatLoader and HCatEximLoader  */
end_comment

begin_class
specifier|abstract
class|class
name|HCatBaseLoader
extends|extends
name|LoadFunc
implements|implements
name|LoadMetadata
implements|,
name|LoadPushDown
block|{
specifier|protected
specifier|static
specifier|final
name|String
name|PRUNE_PROJECTION_INFO
init|=
literal|"prune.projection.info"
decl_stmt|;
specifier|private
name|RecordReader
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|reader
decl_stmt|;
specifier|protected
name|String
name|signature
decl_stmt|;
name|HCatSchema
name|outputSchema
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Tuple
name|getNext
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|HCatRecord
name|hr
init|=
call|(
name|HCatRecord
call|)
argument_list|(
name|reader
operator|.
name|nextKeyValue
argument_list|()
condition|?
name|reader
operator|.
name|getCurrentValue
argument_list|()
else|:
literal|null
argument_list|)
decl_stmt|;
name|Tuple
name|t
init|=
name|PigHCatUtil
operator|.
name|transformToTuple
argument_list|(
name|hr
argument_list|,
name|outputSchema
argument_list|)
decl_stmt|;
comment|// TODO : we were discussing an iter interface, and also a LazyTuple
comment|// change this when plans for that solidifies.
return|return
name|t
return|;
block|}
catch|catch
parameter_list|(
name|ExecException
name|e
parameter_list|)
block|{
name|int
name|errCode
init|=
literal|6018
decl_stmt|;
name|String
name|errMsg
init|=
literal|"Error while reading input"
decl_stmt|;
throw|throw
operator|new
name|ExecException
argument_list|(
name|errMsg
argument_list|,
name|errCode
argument_list|,
name|PigException
operator|.
name|REMOTE_ENVIRONMENT
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|eOther
parameter_list|)
block|{
name|int
name|errCode
init|=
literal|6018
decl_stmt|;
name|String
name|errMsg
init|=
literal|"Error converting read value to tuple"
decl_stmt|;
throw|throw
operator|new
name|ExecException
argument_list|(
name|errMsg
argument_list|,
name|errCode
argument_list|,
name|PigException
operator|.
name|REMOTE_ENVIRONMENT
argument_list|,
name|eOther
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|prepareToRead
parameter_list|(
name|RecordReader
name|reader
parameter_list|,
name|PigSplit
name|arg1
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ResourceStatistics
name|getStatistics
parameter_list|(
name|String
name|location
parameter_list|,
name|Job
name|job
parameter_list|)
throws|throws
name|IOException
block|{
comment|// statistics not implemented currently
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|OperatorSet
argument_list|>
name|getFeatures
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|LoadPushDown
operator|.
name|OperatorSet
operator|.
name|PROJECTION
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RequiredFieldResponse
name|pushProjection
parameter_list|(
name|RequiredFieldList
name|requiredFieldsInfo
parameter_list|)
throws|throws
name|FrontendException
block|{
comment|// Store the required fields information in the UDFContext so that we
comment|// can retrieve it later.
name|storeInUDFContext
argument_list|(
name|signature
argument_list|,
name|PRUNE_PROJECTION_INFO
argument_list|,
name|requiredFieldsInfo
argument_list|)
expr_stmt|;
comment|// HCat will always prune columns based on what we ask of it - so the
comment|// response is true
return|return
operator|new
name|RequiredFieldResponse
argument_list|(
literal|true
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setUDFContextSignature
parameter_list|(
name|String
name|signature
parameter_list|)
block|{
name|this
operator|.
name|signature
operator|=
name|signature
expr_stmt|;
block|}
comment|// helper methods
specifier|protected
name|void
name|storeInUDFContext
parameter_list|(
name|String
name|signature
parameter_list|,
name|String
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|UDFContext
name|udfContext
init|=
name|UDFContext
operator|.
name|getUDFContext
argument_list|()
decl_stmt|;
name|Properties
name|props
init|=
name|udfContext
operator|.
name|getUDFProperties
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|,
operator|new
name|String
index|[]
block|{
name|signature
block|}
argument_list|)
decl_stmt|;
name|props
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
comment|/**      * A utility method to get the size of inputs. This is accomplished by summing the      * size of all input paths on supported FileSystems. Locations whose size cannot be      * determined are ignored. Note non-FileSystem and unpartitioned locations will not      * report their input size by default.      */
specifier|protected
specifier|static
name|long
name|getSizeInBytes
parameter_list|(
name|InputJobInfo
name|inputJobInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|long
name|sizeInBytes
init|=
literal|0
decl_stmt|;
for|for
control|(
name|PartInfo
name|partInfo
range|:
name|inputJobInfo
operator|.
name|getPartitions
argument_list|()
control|)
block|{
try|try
block|{
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|partInfo
operator|.
name|getLocation
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|p
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|isFile
argument_list|(
name|p
argument_list|)
condition|)
block|{
name|sizeInBytes
operator|+=
name|p
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|getFileStatus
argument_list|(
name|p
argument_list|)
operator|.
name|getLen
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|FileStatus
index|[]
name|fileStatuses
init|=
name|p
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|listStatus
argument_list|(
name|p
argument_list|)
decl_stmt|;
if|if
condition|(
name|fileStatuses
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|FileStatus
name|child
range|:
name|fileStatuses
control|)
block|{
name|sizeInBytes
operator|+=
name|child
operator|.
name|getLen
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// Report size to the extent possible.
block|}
block|}
return|return
name|sizeInBytes
return|;
block|}
block|}
end_class

end_unit

