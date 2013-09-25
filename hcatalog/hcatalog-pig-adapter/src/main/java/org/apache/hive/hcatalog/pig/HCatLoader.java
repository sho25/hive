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
name|hive
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
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|metadata
operator|.
name|Table
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
name|hive
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
name|hive
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
name|hive
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
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|Pair
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
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
name|hive
operator|.
name|hcatalog
operator|.
name|mapreduce
operator|.
name|HCatInputFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
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
name|pig
operator|.
name|Expression
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
name|Expression
operator|.
name|BinaryExpression
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
name|impl
operator|.
name|util
operator|.
name|UDFContext
import|;
end_import

begin_comment
comment|/**  * Pig {@link org.apache.pig.LoadFunc} to read data from HCat  */
end_comment

begin_class
specifier|public
class|class
name|HCatLoader
extends|extends
name|HCatBaseLoader
block|{
specifier|private
specifier|static
specifier|final
name|String
name|PARTITION_FILTER
init|=
literal|"partition.filter"
decl_stmt|;
comment|// for future use
specifier|private
name|HCatInputFormat
name|hcatInputFormat
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|dbName
decl_stmt|;
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|private
name|String
name|hcatServerUri
decl_stmt|;
specifier|private
name|String
name|partitionFilterString
decl_stmt|;
specifier|private
specifier|final
name|PigHCatUtil
name|phutil
init|=
operator|new
name|PigHCatUtil
argument_list|()
decl_stmt|;
comment|// Signature for wrapped loader, see comments in LoadFuncBasedInputDriver.initialize
specifier|final
specifier|public
specifier|static
name|String
name|INNER_SIGNATURE
init|=
literal|"hcatloader.inner.signature"
decl_stmt|;
specifier|final
specifier|public
specifier|static
name|String
name|INNER_SIGNATURE_PREFIX
init|=
literal|"hcatloader_inner_signature"
decl_stmt|;
comment|// A hash map which stores job credentials. The key is a signature passed by Pig, which is
comment|//unique to the load func and input file name (table, in our case).
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
annotation|@
name|Override
specifier|public
name|InputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|getInputFormat
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|hcatInputFormat
operator|==
literal|null
condition|)
block|{
name|hcatInputFormat
operator|=
operator|new
name|HCatInputFormat
argument_list|()
expr_stmt|;
block|}
return|return
name|hcatInputFormat
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|relativeToAbsolutePath
parameter_list|(
name|String
name|location
parameter_list|,
name|Path
name|curDir
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|location
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setLocation
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
literal|true
argument_list|)
expr_stmt|;
name|UDFContext
name|udfContext
init|=
name|UDFContext
operator|.
name|getUDFContext
argument_list|()
decl_stmt|;
name|Properties
name|udfProps
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
name|signature
argument_list|)
expr_stmt|;
name|Pair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|dbTablePair
init|=
name|PigHCatUtil
operator|.
name|getDBTableNames
argument_list|(
name|location
argument_list|)
decl_stmt|;
name|dbName
operator|=
name|dbTablePair
operator|.
name|first
expr_stmt|;
name|tableName
operator|=
name|dbTablePair
operator|.
name|second
expr_stmt|;
name|RequiredFieldList
name|requiredFieldsInfo
init|=
operator|(
name|RequiredFieldList
operator|)
name|udfProps
operator|.
name|get
argument_list|(
name|PRUNE_PROJECTION_INFO
argument_list|)
decl_stmt|;
comment|// get partitionFilterString stored in the UDFContext - it would have
comment|// been stored there by an earlier call to setPartitionFilter
comment|// call setInput on HCatInputFormat only in the frontend because internally
comment|// it makes calls to the hcat server - we don't want these to happen in
comment|// the backend
comment|// in the hadoop front end mapred.task.id property will not be set in
comment|// the Configuration
if|if
condition|(
name|udfProps
operator|.
name|containsKey
argument_list|(
name|HCatConstants
operator|.
name|HCAT_PIG_LOADER_LOCATION_SET
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
name|job
operator|.
name|getConfiguration
argument_list|()
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
comment|//Combine credentials and credentials from job takes precedence for freshness
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
name|signature
argument_list|)
decl_stmt|;
name|crd
operator|.
name|addAll
argument_list|(
name|job
operator|.
name|getCredentials
argument_list|()
argument_list|)
expr_stmt|;
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
name|HCatInputFormat
operator|.
name|setInput
argument_list|(
name|job
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|)
operator|.
name|setFilter
argument_list|(
name|getPartitionFilterString
argument_list|()
argument_list|)
expr_stmt|;
comment|// We will store all the new /changed properties in the job in the
comment|// udf context, so the the HCatInputFormat.setInput method need not
comment|//be called many times.
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
name|udfProps
operator|.
name|put
argument_list|(
name|HCatConstants
operator|.
name|HCAT_PIG_LOADER_LOCATION_SET
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|//Store credentials in a private hash map and not the udf context to
comment|// make sure they are not public.
name|Credentials
name|crd
init|=
operator|new
name|Credentials
argument_list|()
decl_stmt|;
name|crd
operator|.
name|addAll
argument_list|(
name|job
operator|.
name|getCredentials
argument_list|()
argument_list|)
expr_stmt|;
name|jobCredentials
operator|.
name|put
argument_list|(
name|INNER_SIGNATURE_PREFIX
operator|+
literal|"_"
operator|+
name|signature
argument_list|,
name|crd
argument_list|)
expr_stmt|;
block|}
comment|// Need to also push projections by calling setOutputSchema on
comment|// HCatInputFormat - we have to get the RequiredFields information
comment|// from the UdfContext, translate it to an Schema and then pass it
comment|// The reason we do this here is because setLocation() is called by
comment|// Pig runtime at InputFormat.getSplits() and
comment|// InputFormat.createRecordReader() time - we are not sure when
comment|// HCatInputFormat needs to know about pruned projections - so doing it
comment|// here will ensure we communicate to HCatInputFormat about pruned
comment|// projections at getSplits() and createRecordReader() time
if|if
condition|(
name|requiredFieldsInfo
operator|!=
literal|null
condition|)
block|{
comment|// convert to hcatschema and pass to HCatInputFormat
try|try
block|{
name|outputSchema
operator|=
name|phutil
operator|.
name|getHCatSchema
argument_list|(
name|requiredFieldsInfo
operator|.
name|getFields
argument_list|()
argument_list|,
name|signature
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|HCatInputFormat
operator|.
name|setOutputSchema
argument_list|(
name|job
argument_list|,
name|outputSchema
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
else|else
block|{
comment|// else - this means pig's optimizer never invoked the pushProjection
comment|// method - so we need all fields and hence we should not call the
comment|// setOutputSchema on HCatInputFormat
if|if
condition|(
name|HCatUtil
operator|.
name|checkJobContextIfRunningFromBackend
argument_list|(
name|job
argument_list|)
condition|)
block|{
try|try
block|{
name|HCatSchema
name|hcatTableSchema
init|=
operator|(
name|HCatSchema
operator|)
name|udfProps
operator|.
name|get
argument_list|(
name|HCatConstants
operator|.
name|HCAT_TABLE_SCHEMA
argument_list|)
decl_stmt|;
name|outputSchema
operator|=
name|hcatTableSchema
expr_stmt|;
name|HCatInputFormat
operator|.
name|setOutputSchema
argument_list|(
name|job
argument_list|,
name|outputSchema
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getPartitionKeys
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
name|Table
name|table
init|=
name|phutil
operator|.
name|getTable
argument_list|(
name|location
argument_list|,
name|hcatServerUri
operator|!=
literal|null
condition|?
name|hcatServerUri
else|:
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
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|tablePartitionKeys
init|=
name|table
operator|.
name|getPartitionKeys
argument_list|()
decl_stmt|;
name|String
index|[]
name|partitionKeys
init|=
operator|new
name|String
index|[
name|tablePartitionKeys
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|tablePartitionKeys
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|partitionKeys
index|[
name|i
index|]
operator|=
name|tablePartitionKeys
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
return|return
name|partitionKeys
return|;
block|}
annotation|@
name|Override
specifier|public
name|ResourceSchema
name|getSchema
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
literal|true
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|phutil
operator|.
name|getTable
argument_list|(
name|location
argument_list|,
name|hcatServerUri
operator|!=
literal|null
condition|?
name|hcatServerUri
else|:
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
decl_stmt|;
name|HCatSchema
name|hcatTableSchema
init|=
name|HCatUtil
operator|.
name|getTableSchemaWithPtnCols
argument_list|(
name|table
argument_list|)
decl_stmt|;
try|try
block|{
name|PigHCatUtil
operator|.
name|validateHCatTableSchemaFollowsPigRules
argument_list|(
name|hcatTableSchema
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
name|PigException
argument_list|(
literal|"Table schema incompatible for reading through HCatLoader :"
operator|+
name|e
operator|.
name|getMessage
argument_list|()
operator|+
literal|";[Table schema was "
operator|+
name|hcatTableSchema
operator|.
name|toString
argument_list|()
operator|+
literal|"]"
argument_list|,
name|PigHCatUtil
operator|.
name|PIG_EXCEPTION_CODE
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|storeInUDFContext
argument_list|(
name|signature
argument_list|,
name|HCatConstants
operator|.
name|HCAT_TABLE_SCHEMA
argument_list|,
name|hcatTableSchema
argument_list|)
expr_stmt|;
name|outputSchema
operator|=
name|hcatTableSchema
expr_stmt|;
return|return
name|PigHCatUtil
operator|.
name|getResourceSchema
argument_list|(
name|hcatTableSchema
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setPartitionFilter
parameter_list|(
name|Expression
name|partitionFilter
parameter_list|)
throws|throws
name|IOException
block|{
comment|// convert the partition filter expression into a string expected by
comment|// hcat and pass it in setLocation()
name|partitionFilterString
operator|=
name|getHCatComparisonString
argument_list|(
name|partitionFilter
argument_list|)
expr_stmt|;
comment|// store this in the udf context so we can get it later
name|storeInUDFContext
argument_list|(
name|signature
argument_list|,
name|PARTITION_FILTER
argument_list|,
name|partitionFilterString
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get statistics about the data to be loaded. Only input data size is implemented at this time.    */
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
try|try
block|{
name|ResourceStatistics
name|stats
init|=
operator|new
name|ResourceStatistics
argument_list|()
decl_stmt|;
name|InputJobInfo
name|inputJobInfo
init|=
operator|(
name|InputJobInfo
operator|)
name|HCatUtil
operator|.
name|deserialize
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_JOB_INFO
argument_list|)
argument_list|)
decl_stmt|;
name|stats
operator|.
name|setmBytes
argument_list|(
name|getSizeInBytes
argument_list|(
name|inputJobInfo
argument_list|)
operator|/
literal|1024
operator|/
literal|1024
argument_list|)
expr_stmt|;
return|return
name|stats
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|String
name|getPartitionFilterString
parameter_list|()
block|{
if|if
condition|(
name|partitionFilterString
operator|==
literal|null
condition|)
block|{
name|Properties
name|props
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
name|signature
block|}
argument_list|)
decl_stmt|;
name|partitionFilterString
operator|=
name|props
operator|.
name|getProperty
argument_list|(
name|PARTITION_FILTER
argument_list|)
expr_stmt|;
block|}
return|return
name|partitionFilterString
return|;
block|}
specifier|private
name|String
name|getHCatComparisonString
parameter_list|(
name|Expression
name|expr
parameter_list|)
block|{
if|if
condition|(
name|expr
operator|instanceof
name|BinaryExpression
condition|)
block|{
comment|// call getHCatComparisonString on lhs and rhs, and and join the
comment|// results with OpType string
comment|// we can just use OpType.toString() on all Expression types except
comment|// Equal, NotEqualt since Equal has '==' in toString() and
comment|// we need '='
name|String
name|opStr
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|expr
operator|.
name|getOpType
argument_list|()
condition|)
block|{
case|case
name|OP_EQ
case|:
name|opStr
operator|=
literal|" = "
expr_stmt|;
break|break;
default|default:
name|opStr
operator|=
name|expr
operator|.
name|getOpType
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
name|BinaryExpression
name|be
init|=
operator|(
name|BinaryExpression
operator|)
name|expr
decl_stmt|;
return|return
literal|"("
operator|+
name|getHCatComparisonString
argument_list|(
name|be
operator|.
name|getLhs
argument_list|()
argument_list|)
operator|+
name|opStr
operator|+
name|getHCatComparisonString
argument_list|(
name|be
operator|.
name|getRhs
argument_list|()
argument_list|)
operator|+
literal|")"
return|;
block|}
else|else
block|{
comment|// should be a constant or column
return|return
name|expr
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

