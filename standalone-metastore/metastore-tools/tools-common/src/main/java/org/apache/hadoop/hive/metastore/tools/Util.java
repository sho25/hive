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
name|metastore
operator|.
name|tools
package|;
end_package

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
name|Joiner
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
name|net
operator|.
name|HostAndPort
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
name|TableType
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
name|Database
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
name|metastore
operator|.
name|api
operator|.
name|Partition
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
name|PrincipalType
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
name|SerDeInfo
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
name|StorageDescriptor
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
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jetbrains
operator|.
name|annotations
operator|.
name|NotNull
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jetbrains
operator|.
name|annotations
operator|.
name|Nullable
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

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|IntStream
import|;
end_import

begin_comment
comment|/**  * Helper utilities. The Util class is just a placeholder for static methods,  * it should be never instantiated.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|Util
block|{
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_TYPE
init|=
literal|"string"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TYPE_SEPARATOR
init|=
literal|":"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|THRIFT_SCHEMA
init|=
literal|"thrift"
decl_stmt|;
specifier|static
specifier|final
name|String
name|DEFAULT_HOST
init|=
literal|"localhost"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ENV_SERVER
init|=
literal|"HMS_HOST"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ENV_PORT
init|=
literal|"HMS_PORT"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|PROP_HOST
init|=
literal|"hms.host"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|PROP_PORT
init|=
literal|"hms.port"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HIVE_INPUT_FORMAT
init|=
literal|"org.apache.hadoop.hive.ql.io.HiveInputFormat"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HIVE_OUTPUT_FORMAT
init|=
literal|"org.apache.hadoop.hive.ql.io.HiveOutputFormat"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|LAZY_SIMPLE_SERDE
init|=
literal|"org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Pattern
index|[]
name|EMPTY_PATTERN
init|=
operator|new
name|Pattern
index|[]
block|{}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Pattern
index|[]
name|MATCH_ALL_PATTERN
init|=
operator|new
name|Pattern
index|[]
block|{
name|Pattern
operator|.
name|compile
argument_list|(
literal|".*"
argument_list|)
block|}
decl_stmt|;
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
name|Util
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Disable public constructor
specifier|private
name|Util
parameter_list|()
block|{   }
comment|/**    * Wrapper that moves all checked exceptions to RuntimeException.    *    * @param throwingSupplier Supplier that throws Exception    * @param<T>              Supplier return type    * @return Supplier that throws unchecked exception    */
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|throwingSupplierWrapper
parameter_list|(
name|ThrowingSupplier
argument_list|<
name|T
argument_list|,
name|Exception
argument_list|>
name|throwingSupplier
parameter_list|)
block|{
try|try
block|{
return|return
name|throwingSupplier
operator|.
name|get
argument_list|()
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Version of the Supplier that can throw exceptions.    *    * @param<T> Supplier return type    * @param<E> Exception type    */
annotation|@
name|FunctionalInterface
specifier|public
interface|interface
name|ThrowingSupplier
parameter_list|<
name|T
parameter_list|,
name|E
extends|extends
name|Exception
parameter_list|>
block|{
name|T
name|get
parameter_list|()
throws|throws
name|E
function_decl|;
block|}
comment|/**    * A builder for Database.  The name of the new database is required.  Everything else    * selects reasonable defaults.    * This is a modified version of Hive 3.0 DatabaseBuilder.    */
specifier|public
specifier|static
class|class
name|DatabaseBuilder
block|{
specifier|private
name|String
name|name
decl_stmt|;
specifier|private
name|String
name|description
decl_stmt|;
specifier|private
name|String
name|location
decl_stmt|;
specifier|private
name|String
name|ownerName
decl_stmt|;
specifier|private
name|PrincipalType
name|ownerType
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
init|=
literal|null
decl_stmt|;
comment|// Disable default constructor
specifier|private
name|DatabaseBuilder
parameter_list|()
block|{     }
comment|/**      * Constructor from database name.      *      * @param name Database name      */
specifier|public
name|DatabaseBuilder
parameter_list|(
annotation|@
name|NotNull
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|ownerType
operator|=
name|PrincipalType
operator|.
name|USER
expr_stmt|;
block|}
comment|/**      * Add database description.      *      * @param description Database description string.      * @return this      */
specifier|public
name|DatabaseBuilder
name|withDescription
parameter_list|(
annotation|@
name|NotNull
name|String
name|description
parameter_list|)
block|{
name|this
operator|.
name|description
operator|=
name|description
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Add database location      *      * @param location Database location string      * @return this      */
specifier|public
name|DatabaseBuilder
name|withLocation
parameter_list|(
annotation|@
name|NotNull
name|String
name|location
parameter_list|)
block|{
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Add Database parameters      *      * @param params database parameters      * @return this      */
specifier|public
name|DatabaseBuilder
name|withParams
parameter_list|(
annotation|@
name|NotNull
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
block|{
name|this
operator|.
name|params
operator|=
name|params
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Add a single database parameter.      *      * @param key parameter key      * @param val parameter value      * @return this      */
specifier|public
name|DatabaseBuilder
name|withParam
parameter_list|(
annotation|@
name|NotNull
name|String
name|key
parameter_list|,
annotation|@
name|NotNull
name|String
name|val
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|params
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|params
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|params
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Add database owner name      *      * @param ownerName new owner name      * @return this      */
specifier|public
name|DatabaseBuilder
name|withOwnerName
parameter_list|(
annotation|@
name|NotNull
name|String
name|ownerName
parameter_list|)
block|{
name|this
operator|.
name|ownerName
operator|=
name|ownerName
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Add owner tyoe      *      * @param ownerType database owner type (USER or GROUP)      * @return this      */
specifier|public
name|DatabaseBuilder
name|withOwnerType
parameter_list|(
name|PrincipalType
name|ownerType
parameter_list|)
block|{
name|this
operator|.
name|ownerType
operator|=
name|ownerType
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Build database object      *      * @return database      */
specifier|public
name|Database
name|build
parameter_list|()
block|{
name|Database
name|db
init|=
operator|new
name|Database
argument_list|(
name|name
argument_list|,
name|description
argument_list|,
name|location
argument_list|,
name|params
argument_list|)
decl_stmt|;
if|if
condition|(
name|ownerName
operator|!=
literal|null
condition|)
block|{
name|db
operator|.
name|setOwnerName
argument_list|(
name|ownerName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ownerType
operator|!=
literal|null
condition|)
block|{
name|db
operator|.
name|setOwnerType
argument_list|(
name|ownerType
argument_list|)
expr_stmt|;
block|}
return|return
name|db
return|;
block|}
block|}
comment|/**    * Builder for Table.    */
specifier|public
specifier|static
class|class
name|TableBuilder
block|{
specifier|private
specifier|final
name|String
name|dbName
decl_stmt|;
specifier|private
specifier|final
name|String
name|tableName
decl_stmt|;
specifier|private
name|TableType
name|tableType
init|=
name|TableType
operator|.
name|MANAGED_TABLE
decl_stmt|;
specifier|private
name|String
name|location
decl_stmt|;
specifier|private
name|String
name|serde
init|=
name|LAZY_SIMPLE_SERDE
decl_stmt|;
specifier|private
name|String
name|owner
decl_stmt|;
specifier|private
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|columns
decl_stmt|;
specifier|private
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partitionKeys
decl_stmt|;
specifier|private
name|String
name|inputFormat
init|=
name|HIVE_INPUT_FORMAT
decl_stmt|;
specifier|private
name|String
name|outputFormat
init|=
name|HIVE_OUTPUT_FORMAT
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|TableBuilder
parameter_list|()
block|{
name|dbName
operator|=
literal|null
expr_stmt|;
name|tableName
operator|=
literal|null
expr_stmt|;
block|}
name|TableBuilder
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
specifier|static
name|Table
name|buildDefaultTable
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
return|return
operator|new
name|TableBuilder
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
name|TableBuilder
name|withType
parameter_list|(
name|TableType
name|tabeType
parameter_list|)
block|{
name|this
operator|.
name|tableType
operator|=
name|tabeType
expr_stmt|;
return|return
name|this
return|;
block|}
name|TableBuilder
name|withOwner
parameter_list|(
name|String
name|owner
parameter_list|)
block|{
name|this
operator|.
name|owner
operator|=
name|owner
expr_stmt|;
return|return
name|this
return|;
block|}
name|TableBuilder
name|withColumns
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|columns
parameter_list|)
block|{
name|this
operator|.
name|columns
operator|=
name|columns
expr_stmt|;
return|return
name|this
return|;
block|}
name|TableBuilder
name|withPartitionKeys
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partitionKeys
parameter_list|)
block|{
name|this
operator|.
name|partitionKeys
operator|=
name|partitionKeys
expr_stmt|;
return|return
name|this
return|;
block|}
name|TableBuilder
name|withSerde
parameter_list|(
name|String
name|serde
parameter_list|)
block|{
name|this
operator|.
name|serde
operator|=
name|serde
expr_stmt|;
return|return
name|this
return|;
block|}
name|TableBuilder
name|withInputFormat
parameter_list|(
name|String
name|inputFormat
parameter_list|)
block|{
name|this
operator|.
name|inputFormat
operator|=
name|inputFormat
expr_stmt|;
return|return
name|this
return|;
block|}
name|TableBuilder
name|withOutputFormat
parameter_list|(
name|String
name|outputFormat
parameter_list|)
block|{
name|this
operator|.
name|outputFormat
operator|=
name|outputFormat
expr_stmt|;
return|return
name|this
return|;
block|}
name|TableBuilder
name|withParameter
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|parameters
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
name|TableBuilder
name|withLocation
parameter_list|(
name|String
name|location
parameter_list|)
block|{
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
return|return
name|this
return|;
block|}
name|Table
name|build
parameter_list|()
block|{
name|StorageDescriptor
name|sd
init|=
operator|new
name|StorageDescriptor
argument_list|()
decl_stmt|;
if|if
condition|(
name|columns
operator|==
literal|null
condition|)
block|{
name|sd
operator|.
name|setCols
argument_list|(
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sd
operator|.
name|setCols
argument_list|(
name|columns
argument_list|)
expr_stmt|;
block|}
name|SerDeInfo
name|serdeInfo
init|=
operator|new
name|SerDeInfo
argument_list|()
decl_stmt|;
name|serdeInfo
operator|.
name|setSerializationLib
argument_list|(
name|serde
argument_list|)
expr_stmt|;
name|serdeInfo
operator|.
name|setName
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setSerdeInfo
argument_list|(
name|serdeInfo
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setInputFormat
argument_list|(
name|inputFormat
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setOutputFormat
argument_list|(
name|outputFormat
argument_list|)
expr_stmt|;
if|if
condition|(
name|location
operator|!=
literal|null
condition|)
block|{
name|sd
operator|.
name|setLocation
argument_list|(
name|location
argument_list|)
expr_stmt|;
block|}
name|Table
name|table
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|table
operator|.
name|setDbName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|table
operator|.
name|setTableName
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|table
operator|.
name|setSd
argument_list|(
name|sd
argument_list|)
expr_stmt|;
name|table
operator|.
name|setParameters
argument_list|(
name|parameters
argument_list|)
expr_stmt|;
name|table
operator|.
name|setOwner
argument_list|(
name|owner
argument_list|)
expr_stmt|;
if|if
condition|(
name|partitionKeys
operator|!=
literal|null
condition|)
block|{
name|table
operator|.
name|setPartitionKeys
argument_list|(
name|partitionKeys
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|setTableType
argument_list|(
name|tableType
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|table
return|;
block|}
block|}
comment|/**    * Builder of partitions.    */
specifier|public
specifier|static
class|class
name|PartitionBuilder
block|{
specifier|private
specifier|final
name|Table
name|table
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|values
decl_stmt|;
specifier|private
name|String
name|location
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|PartitionBuilder
parameter_list|()
block|{
name|table
operator|=
literal|null
expr_stmt|;
block|}
name|PartitionBuilder
parameter_list|(
name|Table
name|table
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
block|}
name|PartitionBuilder
name|withValues
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|values
parameter_list|)
block|{
name|this
operator|.
name|values
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|values
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
name|PartitionBuilder
name|withLocation
parameter_list|(
name|String
name|location
parameter_list|)
block|{
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
return|return
name|this
return|;
block|}
name|PartitionBuilder
name|withParameter
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|parameters
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
name|PartitionBuilder
name|withParameters
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
block|{
name|parameters
operator|=
name|params
expr_stmt|;
return|return
name|this
return|;
block|}
name|Partition
name|build
parameter_list|()
block|{
name|Partition
name|partition
init|=
operator|new
name|Partition
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|partitionNames
init|=
name|table
operator|.
name|getPartitionKeys
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|FieldSchema
operator|::
name|getName
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|partitionNames
operator|.
name|size
argument_list|()
operator|!=
name|values
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Partition values do not match table schema"
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|spec
init|=
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|values
operator|.
name|size
argument_list|()
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|i
lambda|->
name|partitionNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|+
literal|"="
operator|+
name|values
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
name|partition
operator|.
name|setDbName
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|partition
operator|.
name|setTableName
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|partition
operator|.
name|setParameters
argument_list|(
name|parameters
argument_list|)
expr_stmt|;
name|partition
operator|.
name|setValues
argument_list|(
name|values
argument_list|)
expr_stmt|;
name|partition
operator|.
name|setSd
argument_list|(
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|deepCopy
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|location
operator|==
literal|null
condition|)
block|{
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|setLocation
argument_list|(
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
operator|+
literal|"/"
operator|+
name|Joiner
operator|.
name|on
argument_list|(
literal|"/"
argument_list|)
operator|.
name|join
argument_list|(
name|spec
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|setLocation
argument_list|(
name|location
argument_list|)
expr_stmt|;
block|}
return|return
name|partition
return|;
block|}
block|}
comment|/**    * Create table schema from parameters.    *    * @param params list of parameters. Each parameter can be either a simple name or    *               name:type for non-String types.    * @return table schema description    */
specifier|public
specifier|static
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|createSchema
parameter_list|(
annotation|@
name|Nullable
name|List
argument_list|<
name|String
argument_list|>
name|params
parameter_list|)
block|{
if|if
condition|(
name|params
operator|==
literal|null
operator|||
name|params
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
return|return
name|params
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|Util
operator|::
name|param2Schema
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Get server URI.<p>    * HMS host is obtained from    *<ol>    *<li>Argument</li>    *<li>HMS_HOST environment parameter</li>    *<li>hms.host Java property</li>    *<li>use 'localhost' if above fails</li>    *</ol>    * HMS Port is obtained from    *<ol>    *<li>Argument</li>    *<li>host:port string</li>    *<li>HMS_PORT environment variable</li>    *<li>hms.port Java property</li>    *<li>default port value</li>    *</ol>    *    * @param host       HMS host string.    * @param portString HMS port    * @return HMS URI    * @throws URISyntaxException if URI is is invalid    */
specifier|public
specifier|static
annotation|@
name|Nullable
name|URI
name|getServerUri
parameter_list|(
annotation|@
name|Nullable
name|String
name|host
parameter_list|,
annotation|@
name|Nullable
name|String
name|portString
parameter_list|)
throws|throws
name|URISyntaxException
block|{
if|if
condition|(
name|host
operator|==
literal|null
condition|)
block|{
name|host
operator|=
name|System
operator|.
name|getenv
argument_list|(
name|ENV_SERVER
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|host
operator|==
literal|null
condition|)
block|{
name|host
operator|=
name|System
operator|.
name|getProperty
argument_list|(
name|PROP_HOST
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|host
operator|==
literal|null
condition|)
block|{
name|host
operator|=
name|DEFAULT_HOST
expr_stmt|;
block|}
name|host
operator|=
name|host
operator|.
name|trim
argument_list|()
expr_stmt|;
if|if
condition|(
operator|(
name|portString
operator|==
literal|null
operator|||
name|portString
operator|.
name|isEmpty
argument_list|()
operator|||
name|portString
operator|.
name|equals
argument_list|(
literal|"0"
argument_list|)
operator|)
operator|&&
operator|!
name|host
operator|.
name|contains
argument_list|(
literal|":"
argument_list|)
condition|)
block|{
name|portString
operator|=
name|System
operator|.
name|getenv
argument_list|(
name|ENV_PORT
argument_list|)
expr_stmt|;
if|if
condition|(
name|portString
operator|==
literal|null
condition|)
block|{
name|portString
operator|=
name|System
operator|.
name|getProperty
argument_list|(
name|PROP_PORT
argument_list|)
expr_stmt|;
block|}
block|}
name|Integer
name|port
init|=
name|Constants
operator|.
name|HMS_DEFAULT_PORT
decl_stmt|;
if|if
condition|(
name|portString
operator|!=
literal|null
condition|)
block|{
name|port
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|portString
argument_list|)
expr_stmt|;
block|}
name|HostAndPort
name|hp
init|=
name|HostAndPort
operator|.
name|fromString
argument_list|(
name|host
argument_list|)
operator|.
name|withDefaultPort
argument_list|(
name|port
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Connecting to {}:{}"
argument_list|,
name|hp
operator|.
name|getHostText
argument_list|()
argument_list|,
name|hp
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|URI
argument_list|(
name|THRIFT_SCHEMA
argument_list|,
literal|null
argument_list|,
name|hp
operator|.
name|getHostText
argument_list|()
argument_list|,
name|hp
operator|.
name|getPort
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|FieldSchema
name|param2Schema
parameter_list|(
annotation|@
name|NotNull
name|String
name|param
parameter_list|)
block|{
name|String
name|colType
init|=
name|DEFAULT_TYPE
decl_stmt|;
name|String
name|name
init|=
name|param
decl_stmt|;
if|if
condition|(
name|param
operator|.
name|contains
argument_list|(
name|TYPE_SEPARATOR
argument_list|)
condition|)
block|{
name|String
index|[]
name|parts
init|=
name|param
operator|.
name|split
argument_list|(
name|TYPE_SEPARATOR
argument_list|)
decl_stmt|;
name|name
operator|=
name|parts
index|[
literal|0
index|]
expr_stmt|;
name|colType
operator|=
name|parts
index|[
literal|1
index|]
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|FieldSchema
argument_list|(
name|name
argument_list|,
name|colType
argument_list|,
literal|""
argument_list|)
return|;
block|}
comment|/**    * Create multiple partition objects.    *    * @param table    * @param arguments   - list of partition names.    * @param npartitions - Partition parameters    * @return List of created partitions    */
specifier|static
name|List
argument_list|<
name|Partition
argument_list|>
name|createManyPartitions
parameter_list|(
annotation|@
name|NotNull
name|Table
name|table
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
parameter_list|,
annotation|@
name|NotNull
name|List
argument_list|<
name|String
argument_list|>
name|arguments
parameter_list|,
name|int
name|npartitions
parameter_list|)
block|{
return|return
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|npartitions
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|i
lambda|->
operator|new
name|PartitionBuilder
argument_list|(
name|table
argument_list|)
operator|.
name|withParameters
argument_list|(
name|parameters
argument_list|)
operator|.
name|withValues
argument_list|(
name|arguments
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|a
lambda|->
name|a
operator|+
name|i
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Add many partitions in one HMS call    *    * @param client      HMS Client    * @param dbName      database name    * @param tableName   table name    * @param arguments   list of partition names    * @param npartitions number of partitions to create    * @throws TException if fails to create partitions    */
specifier|static
name|Object
name|addManyPartitions
parameter_list|(
annotation|@
name|NotNull
name|HMSClient
name|client
parameter_list|,
annotation|@
name|NotNull
name|String
name|dbName
parameter_list|,
annotation|@
name|NotNull
name|String
name|tableName
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
parameter_list|,
annotation|@
name|NotNull
name|List
argument_list|<
name|String
argument_list|>
name|arguments
parameter_list|,
name|int
name|npartitions
parameter_list|)
throws|throws
name|TException
block|{
name|Table
name|table
init|=
name|client
operator|.
name|getTable
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|client
operator|.
name|addPartitions
argument_list|(
name|createManyPartitions
argument_list|(
name|table
argument_list|,
name|parameters
argument_list|,
name|arguments
argument_list|,
name|npartitions
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|generatePartitionNames
parameter_list|(
annotation|@
name|NotNull
name|String
name|prefix
parameter_list|,
name|int
name|npartitions
parameter_list|)
block|{
return|return
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|npartitions
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|i
lambda|->
name|prefix
operator|+
name|i
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
return|;
block|}
specifier|static
name|void
name|addManyPartitionsNoException
parameter_list|(
annotation|@
name|NotNull
name|HMSClient
name|client
parameter_list|,
annotation|@
name|NotNull
name|String
name|dbName
parameter_list|,
annotation|@
name|NotNull
name|String
name|tableName
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|arguments
parameter_list|,
name|int
name|npartitions
parameter_list|)
block|{
name|throwingSupplierWrapper
argument_list|(
parameter_list|()
lambda|->
name|addManyPartitions
argument_list|(
name|client
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|,
name|parameters
argument_list|,
name|arguments
argument_list|,
name|npartitions
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Filter candidates - find all that match positive matches and do not match    * any negative matches.    *    * @param candidates       list of candidate strings. If null, return an empty list.    * @param positivePatterns list of regexp that should all match. If null, everything matches.    * @param negativePatterns list of regexp, none of these should match. If null, everything matches.    * @return list of filtered results.    */
specifier|public
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|filterMatches
parameter_list|(
annotation|@
name|Nullable
name|List
argument_list|<
name|String
argument_list|>
name|candidates
parameter_list|,
annotation|@
name|Nullable
name|Pattern
index|[]
name|positivePatterns
parameter_list|,
annotation|@
name|Nullable
name|Pattern
index|[]
name|negativePatterns
parameter_list|)
block|{
if|if
condition|(
name|candidates
operator|==
literal|null
operator|||
name|candidates
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
specifier|final
name|Pattern
index|[]
name|positive
init|=
operator|(
name|positivePatterns
operator|==
literal|null
operator|||
name|positivePatterns
operator|.
name|length
operator|==
literal|0
operator|)
condition|?
name|MATCH_ALL_PATTERN
else|:
name|positivePatterns
decl_stmt|;
specifier|final
name|Pattern
index|[]
name|negative
init|=
name|negativePatterns
operator|==
literal|null
condition|?
name|EMPTY_PATTERN
else|:
name|negativePatterns
decl_stmt|;
return|return
name|candidates
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|c
lambda|->
name|Arrays
operator|.
name|stream
argument_list|(
name|positive
argument_list|)
operator|.
name|anyMatch
argument_list|(
name|p
lambda|->
name|p
operator|.
name|matcher
argument_list|(
name|c
argument_list|)
operator|.
name|matches
argument_list|()
argument_list|)
argument_list|)
operator|.
name|filter
argument_list|(
name|c
lambda|->
name|Arrays
operator|.
name|stream
argument_list|(
name|negative
argument_list|)
operator|.
name|noneMatch
argument_list|(
name|p
lambda|->
name|p
operator|.
name|matcher
argument_list|(
name|c
argument_list|)
operator|.
name|matches
argument_list|()
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

