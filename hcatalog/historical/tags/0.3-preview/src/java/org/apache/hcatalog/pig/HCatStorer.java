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
name|OutputCommitter
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
name|OutputFormat
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
name|TaskAttemptContext
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
name|TaskAttemptID
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
name|HCatConstants
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
name|HCatException
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
name|HCatOutputFormat
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
name|HCatOutputStorageDriver
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
name|OutputJobInfo
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
name|ResourceSchema
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
name|logicalLayer
operator|.
name|schema
operator|.
name|Schema
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
name|ObjectSerializer
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
comment|/**  * HCatStorer.  *  */
end_comment

begin_class
specifier|public
class|class
name|HCatStorer
extends|extends
name|HCatBaseStorer
block|{
comment|// Signature for wrapped storer, see comments in LoadFuncBasedInputDriver.initialize
specifier|final
specifier|public
specifier|static
name|String
name|INNER_SIGNATURE
init|=
literal|"hcatstorer.inner.signature"
decl_stmt|;
specifier|final
specifier|public
specifier|static
name|String
name|INNER_SIGNATURE_PREFIX
init|=
literal|"hcatstorer_inner_signature"
decl_stmt|;
specifier|public
name|HCatStorer
parameter_list|(
name|String
name|partSpecs
parameter_list|,
name|String
name|schema
parameter_list|)
throws|throws
name|Exception
block|{
name|super
argument_list|(
name|partSpecs
argument_list|,
name|schema
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HCatStorer
parameter_list|(
name|String
name|partSpecs
parameter_list|)
throws|throws
name|Exception
block|{
name|this
argument_list|(
name|partSpecs
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HCatStorer
parameter_list|()
throws|throws
name|Exception
block|{
name|this
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|OutputFormat
name|getOutputFormat
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|HCatOutputFormat
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setStoreLocation
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
name|job
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|INNER_SIGNATURE
argument_list|,
name|INNER_SIGNATURE_PREFIX
operator|+
literal|"_"
operator|+
name|sign
argument_list|)
expr_stmt|;
name|Properties
name|p
init|=
name|UDFContext
operator|.
name|getUDFContext
argument_list|()
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
name|sign
block|}
argument_list|)
decl_stmt|;
name|String
index|[]
name|userStr
init|=
name|location
operator|.
name|split
argument_list|(
literal|"\\."
argument_list|)
decl_stmt|;
name|OutputJobInfo
name|outputJobInfo
decl_stmt|;
if|if
condition|(
name|userStr
operator|.
name|length
operator|==
literal|2
condition|)
block|{
name|outputJobInfo
operator|=
name|OutputJobInfo
operator|.
name|create
argument_list|(
name|userStr
index|[
literal|0
index|]
argument_list|,
name|userStr
index|[
literal|1
index|]
argument_list|,
name|partitions
argument_list|,
name|PigHCatUtil
operator|.
name|getHCatServerUri
argument_list|(
name|job
argument_list|)
argument_list|,
name|PigHCatUtil
operator|.
name|getHCatServerPrincipal
argument_list|(
name|job
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outputJobInfo
operator|=
name|OutputJobInfo
operator|.
name|create
argument_list|(
literal|null
argument_list|,
name|userStr
index|[
literal|0
index|]
argument_list|,
name|partitions
argument_list|,
name|PigHCatUtil
operator|.
name|getHCatServerUri
argument_list|(
name|job
argument_list|)
argument_list|,
name|PigHCatUtil
operator|.
name|getHCatServerPrincipal
argument_list|(
name|job
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Configuration
name|config
init|=
name|job
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|HCatUtil
operator|.
name|checkJobContextIfRunningFromBackend
argument_list|(
name|job
argument_list|)
condition|)
block|{
name|Schema
name|schema
init|=
operator|(
name|Schema
operator|)
name|ObjectSerializer
operator|.
name|deserialize
argument_list|(
name|p
operator|.
name|getProperty
argument_list|(
name|PIG_SCHEMA
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|schema
operator|!=
literal|null
condition|)
block|{
name|pigSchema
operator|=
name|schema
expr_stmt|;
block|}
if|if
condition|(
name|pigSchema
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|FrontendException
argument_list|(
literal|"Schema for data cannot be determined."
argument_list|,
name|PigHCatUtil
operator|.
name|PIG_EXCEPTION_CODE
argument_list|)
throw|;
block|}
try|try
block|{
name|HCatOutputFormat
operator|.
name|setOutput
argument_list|(
name|job
argument_list|,
name|outputJobInfo
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HCatException
name|he
parameter_list|)
block|{
comment|// pass the message to the user - essentially something about the table
comment|// information passed to HCatOutputFormat was not right
throw|throw
operator|new
name|PigException
argument_list|(
name|he
operator|.
name|getMessage
argument_list|()
argument_list|,
name|PigHCatUtil
operator|.
name|PIG_EXCEPTION_CODE
argument_list|,
name|he
argument_list|)
throw|;
block|}
name|HCatSchema
name|hcatTblSchema
init|=
name|HCatOutputFormat
operator|.
name|getTableSchema
argument_list|(
name|job
argument_list|)
decl_stmt|;
try|try
block|{
name|doSchemaValidations
argument_list|(
name|pigSchema
argument_list|,
name|hcatTblSchema
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HCatException
name|he
parameter_list|)
block|{
throw|throw
operator|new
name|FrontendException
argument_list|(
name|he
operator|.
name|getMessage
argument_list|()
argument_list|,
name|PigHCatUtil
operator|.
name|PIG_EXCEPTION_CODE
argument_list|,
name|he
argument_list|)
throw|;
block|}
name|computedSchema
operator|=
name|convertPigSchemaToHCatSchema
argument_list|(
name|pigSchema
argument_list|,
name|hcatTblSchema
argument_list|)
expr_stmt|;
name|HCatOutputFormat
operator|.
name|setSchema
argument_list|(
name|job
argument_list|,
name|computedSchema
argument_list|)
expr_stmt|;
name|p
operator|.
name|setProperty
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_OUTPUT_INFO
argument_list|,
name|config
operator|.
name|get
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_OUTPUT_INFO
argument_list|)
argument_list|)
expr_stmt|;
name|PigHCatUtil
operator|.
name|saveConfigIntoUDFProperties
argument_list|(
name|p
argument_list|,
name|config
argument_list|,
name|HCatConstants
operator|.
name|HCAT_KEY_HIVE_CONF
argument_list|)
expr_stmt|;
name|PigHCatUtil
operator|.
name|saveConfigIntoUDFProperties
argument_list|(
name|p
argument_list|,
name|config
argument_list|,
name|HCatConstants
operator|.
name|HCAT_DYNAMIC_PTN_JOBID
argument_list|)
expr_stmt|;
name|PigHCatUtil
operator|.
name|saveConfigIntoUDFProperties
argument_list|(
name|p
argument_list|,
name|config
argument_list|,
name|HCatConstants
operator|.
name|HCAT_KEY_TOKEN_SIGNATURE
argument_list|)
expr_stmt|;
name|PigHCatUtil
operator|.
name|saveConfigIntoUDFProperties
argument_list|(
name|p
argument_list|,
name|config
argument_list|,
name|HCatConstants
operator|.
name|HCAT_KEY_JOBCLIENT_TOKEN_SIGNATURE
argument_list|)
expr_stmt|;
name|PigHCatUtil
operator|.
name|saveConfigIntoUDFProperties
argument_list|(
name|p
argument_list|,
name|config
argument_list|,
name|HCatConstants
operator|.
name|HCAT_KEY_JOBCLIENT_TOKEN_STRFORM
argument_list|)
expr_stmt|;
name|p
operator|.
name|setProperty
argument_list|(
name|COMPUTED_OUTPUT_SCHEMA
argument_list|,
name|ObjectSerializer
operator|.
name|serialize
argument_list|(
name|computedSchema
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|config
operator|.
name|set
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_OUTPUT_INFO
argument_list|,
name|p
operator|.
name|getProperty
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_OUTPUT_INFO
argument_list|)
argument_list|)
expr_stmt|;
name|PigHCatUtil
operator|.
name|getConfigFromUDFProperties
argument_list|(
name|p
argument_list|,
name|config
argument_list|,
name|HCatConstants
operator|.
name|HCAT_KEY_HIVE_CONF
argument_list|)
expr_stmt|;
name|PigHCatUtil
operator|.
name|getConfigFromUDFProperties
argument_list|(
name|p
argument_list|,
name|config
argument_list|,
name|HCatConstants
operator|.
name|HCAT_DYNAMIC_PTN_JOBID
argument_list|)
expr_stmt|;
name|PigHCatUtil
operator|.
name|getConfigFromUDFProperties
argument_list|(
name|p
argument_list|,
name|config
argument_list|,
name|HCatConstants
operator|.
name|HCAT_KEY_TOKEN_SIGNATURE
argument_list|)
expr_stmt|;
name|PigHCatUtil
operator|.
name|getConfigFromUDFProperties
argument_list|(
name|p
argument_list|,
name|config
argument_list|,
name|HCatConstants
operator|.
name|HCAT_KEY_JOBCLIENT_TOKEN_SIGNATURE
argument_list|)
expr_stmt|;
name|PigHCatUtil
operator|.
name|getConfigFromUDFProperties
argument_list|(
name|p
argument_list|,
name|config
argument_list|,
name|HCatConstants
operator|.
name|HCAT_KEY_JOBCLIENT_TOKEN_STRFORM
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|storeSchema
parameter_list|(
name|ResourceSchema
name|schema
parameter_list|,
name|String
name|arg1
parameter_list|,
name|Job
name|job
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|job
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
literal|"mapred.job.tracker"
argument_list|,
literal|""
argument_list|)
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"local"
argument_list|)
condition|)
block|{
try|try
block|{
comment|//In local mode, mapreduce will not call OutputCommitter.cleanupJob.
comment|//Calling it from here so that the partition publish happens.
comment|//This call needs to be removed after MAPREDUCE-1447 is fixed.
name|getOutputFormat
argument_list|()
operator|.
name|getOutputCommitter
argument_list|(
operator|new
name|TaskAttemptContext
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
operator|new
name|TaskAttemptID
argument_list|()
argument_list|)
argument_list|)
operator|.
name|cleanupJob
argument_list|(
name|job
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to cleanup job"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to cleanup job"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

