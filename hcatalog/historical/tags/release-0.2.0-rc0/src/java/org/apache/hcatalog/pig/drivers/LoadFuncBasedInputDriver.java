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
name|pig
operator|.
name|drivers
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
name|ArrayList
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
name|Map
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
name|WritableComparable
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
name|InputFormat
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
name|JobContext
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
name|DefaultHCatRecord
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
name|HCatInputStorageDriver
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
name|pig
operator|.
name|PigHCatUtil
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
name|data
operator|.
name|Tuple
import|;
end_import

begin_comment
comment|/**  * This is a base class which wraps a Load func in HCatInputStorageDriver.  * If you already have a LoadFunc, then this class along with LoadFuncBasedInputFormat  * is doing all the heavy lifting. For a new HCat Input Storage Driver just extend it  * and override the initialize(). {@link PigStorageInputDriver} illustrates  * that well.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|LoadFuncBasedInputDriver
extends|extends
name|HCatInputStorageDriver
block|{
specifier|private
name|LoadFuncBasedInputFormat
name|inputFormat
decl_stmt|;
specifier|private
name|HCatSchema
name|dataSchema
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partVals
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|desiredColNames
decl_stmt|;
specifier|protected
name|LoadFunc
name|lf
decl_stmt|;
annotation|@
name|Override
specifier|public
name|HCatRecord
name|convertToHCatRecord
parameter_list|(
name|WritableComparable
name|baseKey
parameter_list|,
name|Writable
name|baseValue
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|data
init|=
operator|(
operator|(
name|Tuple
operator|)
name|baseValue
operator|)
operator|.
name|getAll
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|hcatRecord
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|desiredColNames
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
comment|/* Iterate through columns asked for in output schema, look them up in      * original data schema. If found, put it. Else look up in partition columns      * if found, put it. Else, its a new column, so need to put null. Map lookup      * on partition map will return null, if column is not found.      */
for|for
control|(
name|String
name|colName
range|:
name|desiredColNames
control|)
block|{
name|Integer
name|idx
init|=
name|dataSchema
operator|.
name|getPosition
argument_list|(
name|colName
argument_list|)
decl_stmt|;
name|hcatRecord
operator|.
name|add
argument_list|(
name|idx
operator|!=
literal|null
condition|?
name|data
operator|.
name|get
argument_list|(
name|idx
argument_list|)
else|:
name|partVals
operator|.
name|get
argument_list|(
name|colName
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|DefaultHCatRecord
argument_list|(
name|hcatRecord
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|InputFormat
argument_list|<
name|?
extends|extends
name|WritableComparable
argument_list|,
name|?
extends|extends
name|Writable
argument_list|>
name|getInputFormat
parameter_list|(
name|Properties
name|hcatProperties
parameter_list|)
block|{
return|return
name|inputFormat
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setOriginalSchema
parameter_list|(
name|JobContext
name|jobContext
parameter_list|,
name|HCatSchema
name|hcatSchema
parameter_list|)
throws|throws
name|IOException
block|{
name|dataSchema
operator|=
name|hcatSchema
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setOutputSchema
parameter_list|(
name|JobContext
name|jobContext
parameter_list|,
name|HCatSchema
name|hcatSchema
parameter_list|)
throws|throws
name|IOException
block|{
name|desiredColNames
operator|=
name|hcatSchema
operator|.
name|getFieldNames
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setPartitionValues
parameter_list|(
name|JobContext
name|jobContext
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionValues
parameter_list|)
throws|throws
name|IOException
block|{
name|partVals
operator|=
name|partitionValues
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|JobContext
name|context
parameter_list|,
name|Properties
name|storageDriverArgs
parameter_list|)
throws|throws
name|IOException
block|{
name|lf
operator|.
name|setLocation
argument_list|(
name|location
argument_list|,
operator|new
name|Job
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|inputFormat
operator|=
operator|new
name|LoadFuncBasedInputFormat
argument_list|(
name|lf
argument_list|,
name|PigHCatUtil
operator|.
name|getResourceSchema
argument_list|(
name|dataSchema
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|location
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setInputPath
parameter_list|(
name|JobContext
name|jobContext
parameter_list|,
name|String
name|location
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
name|super
operator|.
name|setInputPath
argument_list|(
name|jobContext
argument_list|,
name|location
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

