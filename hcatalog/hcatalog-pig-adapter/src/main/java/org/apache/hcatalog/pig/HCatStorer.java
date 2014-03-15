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
name|Enumeration
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|Map
operator|.
name|Entry
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
name|security
operator|.
name|Credentials
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
name|HCatContext
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
name|OutputJobInfo
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
name|shims
operator|.
name|ShimLoader
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
comment|/**  * HCatStorer.  *  * @deprecated Use/modify {@link org.apache.hive.hcatalog.pig.HCatStorer} instead  */
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
comment|// A hash map which stores job credentials. The key is a signature passed by Pig, which is
comment|//unique to the store func and out file name (table, in our case).
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|Credentials
argument_list|>
name|jobCredentials
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Credentials
argument_list|>
argument_list|()
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
name|HCatContext
operator|.
name|INSTANCE
operator|.
name|setConf
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|getConf
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|setBoolean
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DATA_TINY_SMALL_INT_PROMOTION
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Configuration
name|config
init|=
name|job
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|config
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
name|udfProps
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
if|if
condition|(
name|udfProps
operator|.
name|containsKey
argument_list|(
name|HCatConstants
operator|.
name|HCAT_PIG_STORER_LOCATION_SET
argument_list|)
condition|)
block|{
for|for
control|(
name|Enumeration
argument_list|<
name|Object
argument_list|>
name|emr
init|=
name|udfProps
operator|.
name|keys
argument_list|()
init|;
name|emr
operator|.
name|hasMoreElements
argument_list|()
condition|;
control|)
block|{
name|PigHCatUtil
operator|.
name|getConfigFromUDFProperties
argument_list|(
name|udfProps
argument_list|,
name|config
argument_list|,
name|emr
operator|.
name|nextElement
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Credentials
name|crd
init|=
name|jobCredentials
operator|.
name|get
argument_list|(
name|INNER_SIGNATURE_PREFIX
operator|+
literal|"_"
operator|+
name|sign
argument_list|)
decl_stmt|;
if|if
condition|(
name|crd
operator|!=
literal|null
condition|)
block|{
name|job
operator|.
name|getCredentials
argument_list|()
operator|.
name|addAll
argument_list|(
name|crd
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|Job
name|clone
init|=
operator|new
name|Job
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
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
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|userStr
operator|.
name|length
operator|==
literal|1
condition|)
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
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|FrontendException
argument_list|(
literal|"location "
operator|+
name|location
operator|+
literal|" is invalid. It must be of the form [db.]table"
argument_list|,
name|PigHCatUtil
operator|.
name|PIG_EXCEPTION_CODE
argument_list|)
throw|;
block|}
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
name|udfProps
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
name|String
name|externalLocation
init|=
operator|(
name|String
operator|)
name|udfProps
operator|.
name|getProperty
argument_list|(
name|HCatConstants
operator|.
name|HCAT_PIG_STORER_EXTERNAL_LOCATION
argument_list|)
decl_stmt|;
if|if
condition|(
name|externalLocation
operator|!=
literal|null
condition|)
block|{
name|outputJobInfo
operator|.
name|setLocation
argument_list|(
name|externalLocation
argument_list|)
expr_stmt|;
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
comment|// pass the message to the user - essentially something about
comment|// the table
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
name|udfProps
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
comment|// We will store all the new /changed properties in the job in the
comment|// udf context, so the the HCatOutputFormat.setOutput and setSchema
comment|// methods need not be called many times.
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|keyValue
range|:
name|job
operator|.
name|getConfiguration
argument_list|()
control|)
block|{
name|String
name|oldValue
init|=
name|clone
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getRaw
argument_list|(
name|keyValue
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|oldValue
operator|==
literal|null
operator|)
operator|||
operator|(
name|keyValue
operator|.
name|getValue
argument_list|()
operator|.
name|equals
argument_list|(
name|oldValue
argument_list|)
operator|==
literal|false
operator|)
condition|)
block|{
name|udfProps
operator|.
name|put
argument_list|(
name|keyValue
operator|.
name|getKey
argument_list|()
argument_list|,
name|keyValue
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|//Store credentials in a private hash map and not the udf context to
comment|// make sure they are not public.
name|jobCredentials
operator|.
name|put
argument_list|(
name|INNER_SIGNATURE_PREFIX
operator|+
literal|"_"
operator|+
name|sign
argument_list|,
name|job
operator|.
name|getCredentials
argument_list|()
argument_list|)
expr_stmt|;
name|udfProps
operator|.
name|put
argument_list|(
name|HCatConstants
operator|.
name|HCAT_PIG_STORER_LOCATION_SET
argument_list|,
literal|true
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
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHCatShim
argument_list|()
operator|.
name|commitJob
argument_list|(
name|getOutputFormat
argument_list|()
argument_list|,
name|job
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cleanupOnFailure
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
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHCatShim
argument_list|()
operator|.
name|abortJob
argument_list|(
name|getOutputFormat
argument_list|()
argument_list|,
name|job
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

