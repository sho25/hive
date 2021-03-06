begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|mapreduce
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
name|ql
operator|.
name|metadata
operator|.
name|HiveStorageHandler
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
name|JobContext
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
name|util
operator|.
name|ReflectionUtils
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
name|ErrorType
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
name|HCatException
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
name|HCatRecord
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

begin_class
specifier|public
specifier|abstract
class|class
name|HCatBaseOutputFormat
extends|extends
name|OutputFormat
argument_list|<
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|HCatRecord
argument_list|>
block|{
comment|/**    * Gets the table schema for the table specified in the HCatOutputFormat.setOutput call    * on the specified job context.    * Note: This is the record-schema for the table. It does not include the table's partition columns.    * @param conf the Configuration object    * @return the table schema, excluding partition columns    * @throws IOException if HCatOutputFormat.setOutput has not been called for the passed context    */
specifier|public
specifier|static
name|HCatSchema
name|getTableSchema
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|OutputJobInfo
name|jobInfo
init|=
name|getJobInfo
argument_list|(
name|conf
argument_list|)
decl_stmt|;
return|return
name|jobInfo
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getDataColumns
argument_list|()
return|;
block|}
comment|/**    * Gets the table schema for the table specified in the HCatOutputFormat.setOutput call    * on the specified job context.    * Note: This is the complete table-schema, including the record-schema *and* the partitioning schema.    * @param conf the Configuration object    * @return the table schema, including the record-schema and partitioning schema.    * @throws IOException if HCatOutputFormat.setOutput has not been called for the passed context    */
specifier|public
specifier|static
name|HCatSchema
name|getTableSchemaWithPartitionColumns
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getJobInfo
argument_list|(
name|conf
argument_list|)
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getAllColumns
argument_list|()
return|;
block|}
comment|/**    * Check for validity of the output-specification for the job.    * @param context information about the job    * @throws IOException when output should not be attempted    */
annotation|@
name|Override
specifier|public
name|void
name|checkOutputSpecs
parameter_list|(
name|JobContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|getOutputFormat
argument_list|(
name|context
argument_list|)
operator|.
name|checkOutputSpecs
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
comment|/**    * Gets the output format instance.    * @param context the job context    * @return the output format instance    * @throws IOException    */
specifier|protected
name|OutputFormat
argument_list|<
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|HCatRecord
argument_list|>
name|getOutputFormat
parameter_list|(
name|JobContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|OutputJobInfo
name|jobInfo
init|=
name|getJobInfo
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|HiveStorageHandler
name|storageHandler
init|=
name|HCatUtil
operator|.
name|getStorageHandler
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|jobInfo
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getStorerInfo
argument_list|()
argument_list|)
decl_stmt|;
comment|// Always configure storage handler with jobproperties/jobconf before calling any methods on it
name|configureOutputStorageHandler
argument_list|(
name|context
argument_list|)
expr_stmt|;
if|if
condition|(
name|storageHandler
operator|instanceof
name|FosterStorageHandler
condition|)
block|{
return|return
operator|new
name|FileOutputFormatContainer
argument_list|(
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|storageHandler
operator|.
name|getOutputFormatClass
argument_list|()
argument_list|,
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|DefaultOutputFormatContainer
argument_list|(
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|storageHandler
operator|.
name|getOutputFormatClass
argument_list|()
argument_list|,
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
block|}
comment|/**    * Gets the HCatOuputJobInfo object by reading the Configuration and deserializing    * the string. If InputJobInfo is not present in the configuration, throws an    * exception since that means HCatOutputFormat.setOutput has not been called.    * @param conf the job Configuration object    * @return the OutputJobInfo object    * @throws IOException the IO exception    */
specifier|public
specifier|static
name|OutputJobInfo
name|getJobInfo
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|jobString
init|=
name|conf
operator|.
name|get
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_OUTPUT_INFO
argument_list|)
decl_stmt|;
if|if
condition|(
name|jobString
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_NOT_INITIALIZED
argument_list|)
throw|;
block|}
return|return
operator|(
name|OutputJobInfo
operator|)
name|HCatUtil
operator|.
name|deserialize
argument_list|(
name|jobString
argument_list|)
return|;
block|}
comment|/**    * Configure the output storage handler    * @param jobContext the job context    * @throws IOException    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|static
name|void
name|configureOutputStorageHandler
parameter_list|(
name|JobContext
name|jobContext
parameter_list|)
throws|throws
name|IOException
block|{
name|configureOutputStorageHandler
argument_list|(
name|jobContext
argument_list|,
operator|(
name|List
argument_list|<
name|String
argument_list|>
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Configure the output storage handler with allowing specification of missing dynamic partvals    * @param jobContext the job context    * @param dynamicPartVals    * @throws IOException    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|static
name|void
name|configureOutputStorageHandler
parameter_list|(
name|JobContext
name|jobContext
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|dynamicPartVals
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
name|jobContext
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
try|try
block|{
name|OutputJobInfo
name|jobInfo
init|=
operator|(
name|OutputJobInfo
operator|)
name|HCatUtil
operator|.
name|deserialize
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_OUTPUT_INFO
argument_list|)
argument_list|)
decl_stmt|;
name|HiveStorageHandler
name|storageHandler
init|=
name|HCatUtil
operator|.
name|getStorageHandler
argument_list|(
name|jobContext
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|jobInfo
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getStorerInfo
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionValues
init|=
name|jobInfo
operator|.
name|getPartitionValues
argument_list|()
decl_stmt|;
name|String
name|location
init|=
name|jobInfo
operator|.
name|getLocation
argument_list|()
decl_stmt|;
if|if
condition|(
name|dynamicPartVals
operator|!=
literal|null
condition|)
block|{
comment|// dynamic part vals specified
name|List
argument_list|<
name|String
argument_list|>
name|dynamicPartKeys
init|=
name|jobInfo
operator|.
name|getDynamicPartitioningKeys
argument_list|()
decl_stmt|;
if|if
condition|(
name|dynamicPartVals
operator|.
name|size
argument_list|()
operator|!=
name|dynamicPartKeys
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_INVALID_PARTITION_VALUES
argument_list|,
literal|"Unable to configure dynamic partitioning for storage handler, mismatch between"
operator|+
literal|" number of partition values obtained["
operator|+
name|dynamicPartVals
operator|.
name|size
argument_list|()
operator|+
literal|"] and number of partition values required["
operator|+
name|dynamicPartKeys
operator|.
name|size
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|dynamicPartKeys
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|partitionValues
operator|.
name|put
argument_list|(
name|dynamicPartKeys
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|dynamicPartVals
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//            // re-home location, now that we know the rest of the partvals
comment|//            Table table = jobInfo.getTableInfo().getTable();
comment|//
comment|//            List<String> partitionCols = new ArrayList<String>();
comment|//            for(FieldSchema schema : table.getPartitionKeys()) {
comment|//              partitionCols.add(schema.getName());
comment|//            }
name|jobInfo
operator|.
name|setPartitionValues
argument_list|(
name|partitionValues
argument_list|)
expr_stmt|;
block|}
name|HCatUtil
operator|.
name|configureOutputStorageHandler
argument_list|(
name|storageHandler
argument_list|,
name|conf
argument_list|,
name|jobInfo
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|HCatException
condition|)
block|{
throw|throw
operator|(
name|HCatException
operator|)
name|e
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_INIT_STORAGE_HANDLER
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**    * Configure the output storage handler, with allowing specification    * of partvals from which it picks the dynamic partvals    * @param context the job context    * @param jobInfo the output job info    * @param fullPartSpec    * @throws IOException    */
specifier|protected
specifier|static
name|void
name|configureOutputStorageHandler
parameter_list|(
name|JobContext
name|context
parameter_list|,
name|OutputJobInfo
name|jobInfo
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|fullPartSpec
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|dynamicPartKeys
init|=
name|jobInfo
operator|.
name|getDynamicPartitioningKeys
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|dynamicPartKeys
operator|==
literal|null
operator|)
operator|||
operator|(
name|dynamicPartKeys
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|configureOutputStorageHandler
argument_list|(
name|context
argument_list|,
operator|(
name|List
argument_list|<
name|String
argument_list|>
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|List
argument_list|<
name|String
argument_list|>
name|dynKeyVals
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|dynamicPartKey
range|:
name|dynamicPartKeys
control|)
block|{
name|dynKeyVals
operator|.
name|add
argument_list|(
name|fullPartSpec
operator|.
name|get
argument_list|(
name|dynamicPartKey
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|configureOutputStorageHandler
argument_list|(
name|context
argument_list|,
name|dynKeyVals
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
specifier|static
name|void
name|setPartDetails
parameter_list|(
name|OutputJobInfo
name|jobInfo
parameter_list|,
specifier|final
name|HCatSchema
name|schema
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partMap
parameter_list|)
throws|throws
name|HCatException
throws|,
name|IOException
block|{
name|List
argument_list|<
name|Integer
argument_list|>
name|posOfPartCols
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|posOfDynPartCols
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
comment|// If partition columns occur in data, we want to remove them.
comment|// So, find out positions of partition columns in schema provided by user.
comment|// We also need to update the output Schema with these deletions.
comment|// Note that, output storage handlers never sees partition columns in data
comment|// or schema.
name|HCatSchema
name|schemaWithoutParts
init|=
operator|new
name|HCatSchema
argument_list|(
name|schema
operator|.
name|getFields
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|partKey
range|:
name|partMap
operator|.
name|keySet
argument_list|()
control|)
block|{
name|Integer
name|idx
decl_stmt|;
if|if
condition|(
operator|(
name|idx
operator|=
name|schema
operator|.
name|getPosition
argument_list|(
name|partKey
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
name|posOfPartCols
operator|.
name|add
argument_list|(
name|idx
argument_list|)
expr_stmt|;
name|schemaWithoutParts
operator|.
name|remove
argument_list|(
name|schema
operator|.
name|get
argument_list|(
name|partKey
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Also, if dynamic partitioning is being used, we want to
comment|// set appropriate list of columns for the columns to be dynamically specified.
comment|// These would be partition keys too, so would also need to be removed from
comment|// output schema and partcols
if|if
condition|(
name|jobInfo
operator|.
name|isDynamicPartitioningUsed
argument_list|()
condition|)
block|{
for|for
control|(
name|String
name|partKey
range|:
name|jobInfo
operator|.
name|getDynamicPartitioningKeys
argument_list|()
control|)
block|{
name|Integer
name|idx
decl_stmt|;
if|if
condition|(
operator|(
name|idx
operator|=
name|schema
operator|.
name|getPosition
argument_list|(
name|partKey
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
name|posOfPartCols
operator|.
name|add
argument_list|(
name|idx
argument_list|)
expr_stmt|;
name|posOfDynPartCols
operator|.
name|add
argument_list|(
name|idx
argument_list|)
expr_stmt|;
name|schemaWithoutParts
operator|.
name|remove
argument_list|(
name|schema
operator|.
name|get
argument_list|(
name|partKey
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|HCatUtil
operator|.
name|validatePartitionSchema
argument_list|(
operator|new
name|Table
argument_list|(
name|jobInfo
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getTable
argument_list|()
argument_list|)
argument_list|,
name|schemaWithoutParts
argument_list|)
expr_stmt|;
name|jobInfo
operator|.
name|setPosOfPartCols
argument_list|(
name|posOfPartCols
argument_list|)
expr_stmt|;
name|jobInfo
operator|.
name|setPosOfDynPartCols
argument_list|(
name|posOfDynPartCols
argument_list|)
expr_stmt|;
name|jobInfo
operator|.
name|setOutputSchema
argument_list|(
name|schemaWithoutParts
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

