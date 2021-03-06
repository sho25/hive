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
name|conf
operator|.
name|HiveConf
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
name|io
operator|.
name|RCFileOutputFormat
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
name|io
operator|.
name|avro
operator|.
name|AvroContainerOutputFormat
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
name|io
operator|.
name|parquet
operator|.
name|MapredParquetOutputFormat
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
name|io
operator|.
name|parquet
operator|.
name|convert
operator|.
name|HiveSchemaConverter
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
name|io
operator|.
name|parquet
operator|.
name|serde
operator|.
name|ParquetTableUtils
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
name|io
operator|.
name|parquet
operator|.
name|write
operator|.
name|DataWritableWriteSupport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|OrcConf
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
name|io
operator|.
name|orc
operator|.
name|OrcOutputFormat
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
name|avro
operator|.
name|AvroSerDe
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
name|avro
operator|.
name|AvroSerdeUtils
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
name|typeinfo
operator|.
name|TypeInfo
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
name|typeinfo
operator|.
name|TypeInfoUtils
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
name|OutputFormat
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
name|HCatFieldSchema
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
import|;
end_import

begin_comment
comment|/**  * This class is a place to put all the code associated with  * Special cases. If there is a corner case required to make  * a particular format work that is above and beyond the generic  * use, it belongs here, for example. Over time, the goal is to  * try to minimize usage of this, but it is a useful overflow  * class that allows us to still be as generic as possible  * in the main codeflow path, and call attention to the special  * cases here.  *  * Note : For all methods introduced here, please document why  * the special case is necessary, providing a jira number if  * possible.  */
end_comment

begin_class
specifier|public
class|class
name|SpecialCases
block|{
specifier|static
specifier|final
specifier|private
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SpecialCases
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Method to do any file-format specific special casing while    * instantiating a storage handler to write. We set any parameters    * we want to be visible to the job in jobProperties, and this will    * be available to the job via jobconf at run time.    *    * This is mostly intended to be used by StorageHandlers that wrap    * File-based OutputFormats such as FosterStorageHandler that wraps    * RCFile, ORC, etc.    *    * @param jobProperties : map to write to    * @param jobInfo : information about this output job to read from    * @param ofclass : the output format in use    */
specifier|public
specifier|static
name|void
name|addSpecialCasesParametersToOutputJobProperties
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
parameter_list|,
name|OutputJobInfo
name|jobInfo
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
name|ofclass
parameter_list|)
block|{
if|if
condition|(
name|ofclass
operator|==
name|RCFileOutputFormat
operator|.
name|class
condition|)
block|{
comment|// RCFile specific parameter
name|jobProperties
operator|.
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_RCFILE_COLUMN_NUMBER_CONF
operator|.
name|varname
argument_list|,
name|Integer
operator|.
name|toOctalString
argument_list|(
name|jobInfo
operator|.
name|getOutputSchema
argument_list|()
operator|.
name|getFields
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ofclass
operator|==
name|OrcOutputFormat
operator|.
name|class
condition|)
block|{
comment|// Special cases for ORC
comment|// We need to check table properties to see if a couple of parameters,
comment|// such as compression parameters are defined. If they are, then we copy
comment|// them to job properties, so that it will be available in jobconf at runtime
comment|// See HIVE-5504 for details
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tableProps
init|=
name|jobInfo
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getTable
argument_list|()
operator|.
name|getParameters
argument_list|()
decl_stmt|;
for|for
control|(
name|OrcConf
name|property
range|:
name|OrcConf
operator|.
name|values
argument_list|()
control|)
block|{
name|String
name|propName
init|=
name|property
operator|.
name|getAttribute
argument_list|()
decl_stmt|;
if|if
condition|(
name|tableProps
operator|.
name|containsKey
argument_list|(
name|propName
argument_list|)
condition|)
block|{
name|jobProperties
operator|.
name|put
argument_list|(
name|propName
argument_list|,
name|tableProps
operator|.
name|get
argument_list|(
name|propName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|ofclass
operator|==
name|AvroContainerOutputFormat
operator|.
name|class
condition|)
block|{
comment|// Special cases for Avro. As with ORC, we make table properties that
comment|// Avro is interested in available in jobconf at runtime
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tableProps
init|=
name|jobInfo
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getTable
argument_list|()
operator|.
name|getParameters
argument_list|()
decl_stmt|;
for|for
control|(
name|AvroSerdeUtils
operator|.
name|AvroTableProperties
name|property
range|:
name|AvroSerdeUtils
operator|.
name|AvroTableProperties
operator|.
name|values
argument_list|()
control|)
block|{
name|String
name|propName
init|=
name|property
operator|.
name|getPropName
argument_list|()
decl_stmt|;
if|if
condition|(
name|tableProps
operator|.
name|containsKey
argument_list|(
name|propName
argument_list|)
condition|)
block|{
name|String
name|propVal
init|=
name|tableProps
operator|.
name|get
argument_list|(
name|propName
argument_list|)
decl_stmt|;
name|jobProperties
operator|.
name|put
argument_list|(
name|propName
argument_list|,
name|tableProps
operator|.
name|get
argument_list|(
name|propName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|Properties
name|properties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|properties
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
name|jobInfo
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|colNames
init|=
name|jobInfo
operator|.
name|getOutputSchema
argument_list|()
operator|.
name|getFieldNames
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|colTypes
init|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|HCatFieldSchema
name|field
range|:
name|jobInfo
operator|.
name|getOutputSchema
argument_list|()
operator|.
name|getFields
argument_list|()
control|)
block|{
name|colTypes
operator|.
name|add
argument_list|(
name|TypeInfoUtils
operator|.
name|getTypeInfoFromTypeString
argument_list|(
name|field
operator|.
name|getTypeString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|jobProperties
operator|.
name|get
argument_list|(
name|AvroSerdeUtils
operator|.
name|AvroTableProperties
operator|.
name|SCHEMA_LITERAL
operator|.
name|getPropName
argument_list|()
argument_list|)
operator|==
literal|null
operator|||
name|jobProperties
operator|.
name|get
argument_list|(
name|AvroSerdeUtils
operator|.
name|AvroTableProperties
operator|.
name|SCHEMA_LITERAL
operator|.
name|getPropName
argument_list|()
argument_list|)
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|jobProperties
operator|.
name|put
argument_list|(
name|AvroSerdeUtils
operator|.
name|AvroTableProperties
operator|.
name|SCHEMA_LITERAL
operator|.
name|getPropName
argument_list|()
argument_list|,
name|AvroSerDe
operator|.
name|getSchemaFromCols
argument_list|(
name|properties
argument_list|,
name|colNames
argument_list|,
name|colTypes
argument_list|,
literal|null
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|ofclass
operator|==
name|MapredParquetOutputFormat
operator|.
name|class
condition|)
block|{
comment|//Handle table properties
name|Properties
name|tblProperties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tableProps
init|=
name|jobInfo
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getTable
argument_list|()
operator|.
name|getParameters
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|key
range|:
name|tableProps
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|ParquetTableUtils
operator|.
name|isParquetProperty
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|tblProperties
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|tableProps
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|//Handle table schema
name|List
argument_list|<
name|String
argument_list|>
name|colNames
init|=
name|jobInfo
operator|.
name|getOutputSchema
argument_list|()
operator|.
name|getFieldNames
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|colTypes
init|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|HCatFieldSchema
name|field
range|:
name|jobInfo
operator|.
name|getOutputSchema
argument_list|()
operator|.
name|getFields
argument_list|()
control|)
block|{
name|colTypes
operator|.
name|add
argument_list|(
name|TypeInfoUtils
operator|.
name|getTypeInfoFromTypeString
argument_list|(
name|field
operator|.
name|getTypeString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|parquetSchema
init|=
name|HiveSchemaConverter
operator|.
name|convert
argument_list|(
name|colNames
argument_list|,
name|colTypes
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|jobProperties
operator|.
name|put
argument_list|(
name|DataWritableWriteSupport
operator|.
name|PARQUET_HIVE_SCHEMA
argument_list|,
name|parquetSchema
argument_list|)
expr_stmt|;
name|jobProperties
operator|.
name|putAll
argument_list|(
name|Maps
operator|.
name|fromProperties
argument_list|(
name|tblProperties
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Method to do any storage-handler specific special casing while instantiating a    * HCatLoader    *    * @param conf : configuration to write to    * @param tableInfo : the table definition being used    */
specifier|public
specifier|static
name|void
name|addSpecialCasesParametersForHCatLoader
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HCatTableInfo
name|tableInfo
parameter_list|)
block|{
if|if
condition|(
operator|(
name|tableInfo
operator|==
literal|null
operator|)
operator|||
operator|(
name|tableInfo
operator|.
name|getStorerInfo
argument_list|()
operator|==
literal|null
operator|)
condition|)
block|{
return|return;
block|}
name|String
name|shClass
init|=
name|tableInfo
operator|.
name|getStorerInfo
argument_list|()
operator|.
name|getStorageHandlerClass
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|shClass
operator|!=
literal|null
operator|)
operator|&&
name|shClass
operator|.
name|equals
argument_list|(
literal|"org.apache.hadoop.hive.hbase.HBaseStorageHandler"
argument_list|)
condition|)
block|{
comment|// NOTE: The reason we use a string name of the hive hbase handler here is
comment|// because we do not want to introduce a compile-dependency on the hive-hbase-handler
comment|// module from within hive-hcatalog.
comment|// This parameter was added due to the requirement in HIVE-7072
name|conf
operator|.
name|set
argument_list|(
literal|"pig.noSplitCombination"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

