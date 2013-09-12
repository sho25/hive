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
name|mapreduce
package|;
end_package

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
name|common
operator|.
name|FileUtils
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
name|HiveMetaHook
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
name|RCFile
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
name|HiveException
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
name|DefaultStorageHandler
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
name|plan
operator|.
name|TableDesc
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
name|security
operator|.
name|authorization
operator|.
name|DefaultHiveAuthorizationProvider
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
name|security
operator|.
name|authorization
operator|.
name|HiveAuthorizationProvider
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
name|SerDe
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
name|mapred
operator|.
name|JobConf
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
name|HCatUtil
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

begin_comment
comment|/**  *  This class is used to encapsulate the InputFormat, OutputFormat and SerDe  *  artifacts of tables which don't define a SerDe. This StorageHandler assumes  *  the supplied storage artifacts are for a file-based storage system.  */
end_comment

begin_class
specifier|public
class|class
name|FosterStorageHandler
extends|extends
name|DefaultStorageHandler
block|{
specifier|public
name|Configuration
name|conf
decl_stmt|;
comment|/** The directory under which data is initially written for a partitioned table */
specifier|protected
specifier|static
specifier|final
name|String
name|DYNTEMP_DIR_NAME
init|=
literal|"_DYN"
decl_stmt|;
comment|/** The directory under which data is initially written for a non partitioned table */
specifier|protected
specifier|static
specifier|final
name|String
name|TEMP_DIR_NAME
init|=
literal|"_TEMP"
decl_stmt|;
specifier|private
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|ifClass
decl_stmt|;
specifier|private
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
name|ofClass
decl_stmt|;
specifier|private
name|Class
argument_list|<
name|?
extends|extends
name|SerDe
argument_list|>
name|serDeClass
decl_stmt|;
specifier|public
name|FosterStorageHandler
parameter_list|(
name|String
name|ifName
parameter_list|,
name|String
name|ofName
parameter_list|,
name|String
name|serdeName
parameter_list|)
throws|throws
name|ClassNotFoundException
block|{
name|this
argument_list|(
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|ifName
argument_list|)
argument_list|,
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|ofName
argument_list|)
argument_list|,
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|SerDe
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|serdeName
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|FosterStorageHandler
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|ifClass
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
name|ofClass
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|SerDe
argument_list|>
name|serDeClass
parameter_list|)
block|{
name|this
operator|.
name|ifClass
operator|=
name|ifClass
expr_stmt|;
name|this
operator|.
name|ofClass
operator|=
name|ofClass
expr_stmt|;
name|this
operator|.
name|serDeClass
operator|=
name|serDeClass
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|getInputFormatClass
parameter_list|()
block|{
return|return
name|ifClass
return|;
comment|//To change body of overridden methods use File | Settings | File Templates.
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
name|getOutputFormatClass
parameter_list|()
block|{
return|return
name|ofClass
return|;
comment|//To change body of overridden methods use File | Settings | File Templates.
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|SerDe
argument_list|>
name|getSerDeClass
parameter_list|()
block|{
return|return
name|serDeClass
return|;
comment|//To change body of implemented methods use File | Settings | File Templates.
block|}
annotation|@
name|Override
specifier|public
name|HiveMetaHook
name|getMetaHook
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|configureJobConf
parameter_list|(
name|TableDesc
name|tableDesc
parameter_list|,
name|JobConf
name|jobConf
parameter_list|)
block|{
comment|//do nothing currently
block|}
annotation|@
name|Override
specifier|public
name|void
name|configureInputJobProperties
parameter_list|(
name|TableDesc
name|tableDesc
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
parameter_list|)
block|{    }
annotation|@
name|Override
specifier|public
name|void
name|configureOutputJobProperties
parameter_list|(
name|TableDesc
name|tableDesc
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
parameter_list|)
block|{
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
name|tableDesc
operator|.
name|getJobProperties
argument_list|()
operator|.
name|get
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_OUTPUT_INFO
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|parentPath
init|=
name|jobInfo
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getTableLocation
argument_list|()
decl_stmt|;
name|String
name|dynHash
init|=
name|tableDesc
operator|.
name|getJobProperties
argument_list|()
operator|.
name|get
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DYNAMIC_PTN_JOBID
argument_list|)
decl_stmt|;
comment|// For dynamic partitioned writes without all keyvalues specified,
comment|// we create a temp dir for the associated write job
if|if
condition|(
name|dynHash
operator|!=
literal|null
condition|)
block|{
name|parentPath
operator|=
operator|new
name|Path
argument_list|(
name|parentPath
argument_list|,
name|DYNTEMP_DIR_NAME
operator|+
name|dynHash
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
name|String
name|outputLocation
decl_stmt|;
if|if
condition|(
operator|(
name|dynHash
operator|==
literal|null
operator|)
operator|&&
name|Boolean
operator|.
name|valueOf
argument_list|(
operator|(
name|String
operator|)
name|tableDesc
operator|.
name|getProperties
argument_list|()
operator|.
name|get
argument_list|(
literal|"EXTERNAL"
argument_list|)
argument_list|)
operator|&&
name|jobInfo
operator|.
name|getLocation
argument_list|()
operator|!=
literal|null
operator|&&
name|jobInfo
operator|.
name|getLocation
argument_list|()
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// honor custom location for external table apart from what metadata specifies
comment|// only if we're not using dynamic partitioning - see HIVE-5011
name|outputLocation
operator|=
name|jobInfo
operator|.
name|getLocation
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|dynHash
operator|==
literal|null
operator|&&
name|jobInfo
operator|.
name|getPartitionValues
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// For non-partitioned tables, we send them to the temp dir
name|outputLocation
operator|=
name|TEMP_DIR_NAME
expr_stmt|;
block|}
else|else
block|{
name|List
argument_list|<
name|String
argument_list|>
name|cols
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|//Get the output location in the order partition keys are defined for the table.
for|for
control|(
name|String
name|name
range|:
name|jobInfo
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getPartitionColumns
argument_list|()
operator|.
name|getFieldNames
argument_list|()
control|)
block|{
name|String
name|value
init|=
name|jobInfo
operator|.
name|getPartitionValues
argument_list|()
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|cols
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|values
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|outputLocation
operator|=
name|FileUtils
operator|.
name|makePartName
argument_list|(
name|cols
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
name|jobInfo
operator|.
name|setLocation
argument_list|(
operator|new
name|Path
argument_list|(
name|parentPath
argument_list|,
name|outputLocation
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|//only set output dir if partition is fully materialized
if|if
condition|(
name|jobInfo
operator|.
name|getPartitionValues
argument_list|()
operator|.
name|size
argument_list|()
operator|==
name|jobInfo
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getPartitionColumns
argument_list|()
operator|.
name|size
argument_list|()
condition|)
block|{
name|jobProperties
operator|.
name|put
argument_list|(
literal|"mapred.output.dir"
argument_list|,
name|jobInfo
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|//TODO find a better home for this, RCFile specifc
name|jobProperties
operator|.
name|put
argument_list|(
name|RCFile
operator|.
name|COLUMN_NUMBER_CONF_STR
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
name|jobProperties
operator|.
name|put
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_OUTPUT_INFO
argument_list|,
name|HCatUtil
operator|.
name|serialize
argument_list|(
name|jobInfo
argument_list|)
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
name|IllegalStateException
argument_list|(
literal|"Failed to set output path"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|configureTableJobProperties
parameter_list|(
name|TableDesc
name|tableDesc
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
parameter_list|)
block|{
return|return;
block|}
name|OutputFormatContainer
name|getOutputFormatContainer
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|OutputFormat
name|outputFormat
parameter_list|)
block|{
return|return
operator|new
name|FileOutputFormatContainer
argument_list|(
name|outputFormat
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|HiveAuthorizationProvider
name|getAuthorizationProvider
parameter_list|()
throws|throws
name|HiveException
block|{
return|return
operator|new
name|DefaultHiveAuthorizationProvider
argument_list|()
return|;
block|}
block|}
end_class

end_unit

