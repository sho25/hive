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
name|hadoop
operator|.
name|hive
operator|.
name|kudu
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
name|HashSet
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
name|java
operator|.
name|util
operator|.
name|Set
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
name|base
operator|.
name|Preconditions
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
name|base
operator|.
name|Throwables
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
name|FileSystem
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
name|kudu
operator|.
name|KuduOutputFormat
operator|.
name|KuduRecordWriter
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|Utilities
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
name|HiveStoragePredicateHandler
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
name|StorageHandlerInfo
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
name|ExprNodeDesc
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
name|AbstractSerDe
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
name|Deserializer
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
name|hadoop
operator|.
name|security
operator|.
name|UserGroupInformation
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
name|StringUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kudu
operator|.
name|Schema
import|;
end_import

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

begin_comment
comment|/**  * Provides a HiveStorageHandler implementation for Apache Kudu.  */
end_comment

begin_class
specifier|public
class|class
name|KuduStorageHandler
extends|extends
name|DefaultStorageHandler
implements|implements
name|HiveStoragePredicateHandler
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|KuduStorageHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|KUDU_PROPERTY_PREFIX
init|=
literal|"kudu."
decl_stmt|;
comment|/** Table Properties. Used in the hive table definition when creating a new table. */
specifier|public
specifier|static
specifier|final
name|String
name|KUDU_TABLE_ID_KEY
init|=
name|KUDU_PROPERTY_PREFIX
operator|+
literal|"table_id"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|KUDU_TABLE_NAME_KEY
init|=
name|KUDU_PROPERTY_PREFIX
operator|+
literal|"table_name"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|KUDU_MASTER_ADDRS_KEY
init|=
name|KUDU_PROPERTY_PREFIX
operator|+
literal|"master_addresses"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|KUDU_TABLE_PROPERTIES
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|KUDU_TABLE_ID_KEY
argument_list|,
name|KUDU_TABLE_NAME_KEY
argument_list|,
name|KUDU_MASTER_ADDRS_KEY
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
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
name|KuduInputFormat
operator|.
name|class
return|;
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
name|KuduOutputFormat
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|AbstractSerDe
argument_list|>
name|getSerDeClass
parameter_list|()
block|{
return|return
name|KuduSerDe
operator|.
name|class
return|;
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
block|{
name|configureJobProperties
argument_list|(
name|tableDesc
argument_list|,
name|jobProperties
argument_list|)
expr_stmt|;
block|}
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
name|configureJobProperties
argument_list|(
name|tableDesc
argument_list|,
name|jobProperties
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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
name|configureJobProperties
argument_list|(
name|tableDesc
argument_list|,
name|jobProperties
argument_list|)
expr_stmt|;
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
comment|// Copied from the DruidStorageHandler.
if|if
condition|(
name|UserGroupInformation
operator|.
name|isSecurityEnabled
argument_list|()
condition|)
block|{
comment|// AM can not do Kerberos Auth so will do the input split generation in the HS2
name|LOG
operator|.
name|debug
argument_list|(
literal|"Setting {} to {} to enable split generation on HS2"
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AM_SPLIT_GENERATION
operator|.
name|toString
argument_list|()
argument_list|,
name|Boolean
operator|.
name|FALSE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AM_SPLIT_GENERATION
operator|.
name|toString
argument_list|()
argument_list|,
name|Boolean
operator|.
name|FALSE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|addDependencyJars
argument_list|(
name|jobConf
argument_list|,
name|KuduStorageHandler
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|Throwables
operator|.
name|propagate
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Copied from the DruidStorageHandler.
specifier|private
specifier|static
name|void
name|addDependencyJars
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
modifier|...
name|classes
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|localFs
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|jars
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|conf
operator|.
name|getStringCollection
argument_list|(
literal|"tmpjars"
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
range|:
name|classes
control|)
block|{
if|if
condition|(
name|clazz
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
specifier|final
name|String
name|path
init|=
name|Utilities
operator|.
name|jarFinderGetJar
argument_list|(
name|clazz
argument_list|)
decl_stmt|;
if|if
condition|(
name|path
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Could not find jar for class "
operator|+
name|clazz
operator|+
literal|" in order to ship it to the cluster."
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|localFs
operator|.
name|exists
argument_list|(
operator|new
name|Path
argument_list|(
name|path
argument_list|)
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Could not validate jar file "
operator|+
name|path
operator|+
literal|" for class "
operator|+
name|clazz
argument_list|)
throw|;
block|}
name|jars
operator|.
name|add
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|jars
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
comment|//noinspection ToArrayCallWithZeroLengthArrayArgument
name|conf
operator|.
name|set
argument_list|(
literal|"tmpjars"
argument_list|,
name|StringUtils
operator|.
name|arrayToString
argument_list|(
name|jars
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|jars
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|configureJobProperties
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
name|Properties
name|tblProps
init|=
name|tableDesc
operator|.
name|getProperties
argument_list|()
decl_stmt|;
name|copyPropertiesFromTable
argument_list|(
name|jobProperties
argument_list|,
name|tblProps
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|copyPropertiesFromTable
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
parameter_list|,
name|Properties
name|tblProps
parameter_list|)
block|{
for|for
control|(
name|String
name|propToCopy
range|:
name|KUDU_TABLE_PROPERTIES
control|)
block|{
if|if
condition|(
name|tblProps
operator|.
name|containsKey
argument_list|(
name|propToCopy
argument_list|)
condition|)
block|{
name|String
name|value
init|=
name|tblProps
operator|.
name|getProperty
argument_list|(
name|propToCopy
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|propToCopy
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|jobProperties
operator|.
name|put
argument_list|(
name|propToCopy
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Gives the storage handler a chance to decompose a predicate.    * The storage handler should analyze the predicate and return the portion of it which    * cannot be evaluated during table access.    *    * @param jobConf contains a job configuration matching the one that will later be passed    *               to getRecordReader and getSplits    * @param deserializer deserializer which will be used when fetching rows    * @param predicate predicate to be decomposed    * @return decomposed form of predicate, or null if no pushdown is possible at all    */
annotation|@
name|Override
specifier|public
name|DecomposedPredicate
name|decomposePredicate
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|Deserializer
name|deserializer
parameter_list|,
name|ExprNodeDesc
name|predicate
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|deserializer
operator|instanceof
name|KuduSerDe
argument_list|)
expr_stmt|;
name|KuduSerDe
name|serDe
init|=
operator|(
name|KuduSerDe
operator|)
name|deserializer
decl_stmt|;
name|Schema
name|schema
init|=
name|serDe
operator|.
name|getSchema
argument_list|()
decl_stmt|;
return|return
name|KuduPredicateHandler
operator|.
name|decompose
argument_list|(
name|predicate
argument_list|,
name|schema
argument_list|)
return|;
block|}
comment|/**    * Used to fetch runtime information about storage handler during DESCRIBE EXTENDED statement.    */
annotation|@
name|Override
specifier|public
name|StorageHandlerInfo
name|getStorageHandlerInfo
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

