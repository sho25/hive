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
name|ql
operator|.
name|metadata
package|;
end_package

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
name|HashMap
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Executors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidQuery
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidTable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|interpreter
operator|.
name|BindableConvention
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|jdbc
operator|.
name|JavaTypeFactoryImpl
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|plan
operator|.
name|RelOptCluster
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|plan
operator|.
name|RelOptMaterialization
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|plan
operator|.
name|RelOptPlanner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|RelNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|core
operator|.
name|TableScan
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|type
operator|.
name|RelDataType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|type
operator|.
name|RelDataTypeFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|type
operator|.
name|RelDataTypeField
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|type
operator|.
name|RelDataTypeImpl
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rex
operator|.
name|RexBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|type
operator|.
name|SqlTypeName
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
name|Constants
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
name|metastore
operator|.
name|DefaultMetaStoreFilterHookImpl
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
name|conf
operator|.
name|MetastoreConf
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
name|ColumnInfo
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
name|optimizer
operator|.
name|calcite
operator|.
name|CalciteSemanticException
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
name|optimizer
operator|.
name|calcite
operator|.
name|HiveTypeSystemImpl
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
name|optimizer
operator|.
name|calcite
operator|.
name|RelOptHiveTable
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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveRelNode
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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveTableScan
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
name|optimizer
operator|.
name|calcite
operator|.
name|translator
operator|.
name|TypeConverter
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
name|parse
operator|.
name|CalcitePlanner
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
name|parse
operator|.
name|ParseUtils
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
name|parse
operator|.
name|RowResolver
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
name|session
operator|.
name|SessionState
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
name|SerDeException
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
name|objectinspector
operator|.
name|StructField
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
name|objectinspector
operator|.
name|StructObjectInspector
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
name|TypeInfoFactory
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
name|joda
operator|.
name|time
operator|.
name|Interval
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
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
name|ImmutableList
import|;
end_import

begin_comment
comment|/**  * Registry for materialized views. The goal of this cache is to avoid parsing and creating  * logical plans for the materialized views at query runtime. When a query arrives, we will  * just need to consult this cache and extract the logical plans for the views (which had  * already been parsed) from it. This cache lives in HS2.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|HiveMaterializedViewsRegistry
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
name|HiveMaterializedViewsRegistry
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/* Singleton */
specifier|private
specifier|static
specifier|final
name|HiveMaterializedViewsRegistry
name|SINGLETON
init|=
operator|new
name|HiveMaterializedViewsRegistry
argument_list|()
decl_stmt|;
comment|/* Key is the database name. Value a map from the qualified name to the view object. */
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|RelOptMaterialization
argument_list|>
argument_list|>
name|materializedViews
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|RelOptMaterialization
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|/* Whether the cache has been initialized or not. */
specifier|private
name|AtomicBoolean
name|initialized
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
name|HiveMaterializedViewsRegistry
parameter_list|()
block|{   }
comment|/**    * Get instance of HiveMaterializedViewsRegistry.    *    * @return the singleton    */
specifier|public
specifier|static
name|HiveMaterializedViewsRegistry
name|get
parameter_list|()
block|{
return|return
name|SINGLETON
return|;
block|}
comment|/**    * Initialize the registry for the given database. It will extract the materialized views    * that are enabled for rewriting from the metastore for the current user, parse them,    * and register them in this cache.    *    * The loading process runs on the background; the method returns in the moment that the    * runnable task is created, thus the views will still not be loaded in the cache when    * it returns.    */
specifier|public
name|void
name|init
parameter_list|()
block|{
try|try
block|{
comment|// Create a new conf object to bypass metastore authorization, as we need to
comment|// retrieve all materialized views from all databases
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|FILTER_HOOK
operator|.
name|getVarname
argument_list|()
argument_list|,
name|DefaultMetaStoreFilterHookImpl
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|init
argument_list|(
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Problem connecting to the metastore when initializing the view registry"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|init
parameter_list|(
name|Hive
name|db
parameter_list|)
block|{
specifier|final
name|boolean
name|dummy
init|=
name|db
operator|.
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_MATERIALIZED_VIEWS_REGISTRY_IMPL
operator|.
name|varname
argument_list|)
operator|.
name|equals
argument_list|(
literal|"DUMMY"
argument_list|)
decl_stmt|;
if|if
condition|(
name|dummy
condition|)
block|{
comment|// Dummy registry does not cache information and forwards all requests to metastore
name|initialized
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Using dummy materialized views registry"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// We initialize the cache
name|ExecutorService
name|pool
init|=
name|Executors
operator|.
name|newCachedThreadPool
argument_list|(
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|setNameFormat
argument_list|(
literal|"HiveMaterializedViewsRegistry-%d"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|pool
operator|.
name|submit
argument_list|(
operator|new
name|Loader
argument_list|(
name|db
argument_list|)
argument_list|)
expr_stmt|;
name|pool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
class|class
name|Loader
implements|implements
name|Runnable
block|{
specifier|private
specifier|final
name|Hive
name|db
decl_stmt|;
specifier|private
name|Loader
parameter_list|(
name|Hive
name|db
parameter_list|)
block|{
name|this
operator|.
name|db
operator|=
name|db
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|SessionState
name|ss
init|=
operator|new
name|SessionState
argument_list|(
name|db
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|ss
operator|.
name|setIsHiveServerQuery
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// All is served from HS2, we do not need e.g. Tez sessions
name|SessionState
operator|.
name|start
argument_list|(
name|ss
argument_list|)
expr_stmt|;
specifier|final
name|boolean
name|cache
init|=
operator|!
name|db
operator|.
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_MATERIALIZED_VIEWS_REGISTRY_IMPL
operator|.
name|varname
argument_list|)
operator|.
name|equals
argument_list|(
literal|"DUMMY"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|dbName
range|:
name|db
operator|.
name|getAllDatabases
argument_list|()
control|)
block|{
for|for
control|(
name|Table
name|mv
range|:
name|db
operator|.
name|getAllMaterializedViewObjects
argument_list|(
name|dbName
argument_list|)
control|)
block|{
name|addMaterializedView
argument_list|(
name|db
operator|.
name|getConf
argument_list|()
argument_list|,
name|mv
argument_list|,
name|OpType
operator|.
name|LOAD
argument_list|,
name|cache
argument_list|)
expr_stmt|;
block|}
block|}
name|initialized
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Materialized views registry has been initialized"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Problem connecting to the metastore when initializing the view registry"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|boolean
name|isInitialized
parameter_list|()
block|{
return|return
name|initialized
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * Adds a newly created materialized view to the cache.    *    * @param materializedViewTable the materialized view    */
specifier|public
name|RelOptMaterialization
name|createMaterializedView
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|Table
name|materializedViewTable
parameter_list|)
block|{
specifier|final
name|boolean
name|cache
init|=
operator|!
name|conf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_MATERIALIZED_VIEWS_REGISTRY_IMPL
operator|.
name|varname
argument_list|)
operator|.
name|equals
argument_list|(
literal|"DUMMY"
argument_list|)
decl_stmt|;
return|return
name|addMaterializedView
argument_list|(
name|conf
argument_list|,
name|materializedViewTable
argument_list|,
name|OpType
operator|.
name|CREATE
argument_list|,
name|cache
argument_list|)
return|;
block|}
comment|/**    * Adds the materialized view to the cache.    *    * @param materializedViewTable the materialized view    */
specifier|private
name|RelOptMaterialization
name|addMaterializedView
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|Table
name|materializedViewTable
parameter_list|,
name|OpType
name|opType
parameter_list|,
name|boolean
name|cache
parameter_list|)
block|{
comment|// Bail out if it is not enabled for rewriting
if|if
condition|(
operator|!
name|materializedViewTable
operator|.
name|isRewriteEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Materialized view "
operator|+
name|materializedViewTable
operator|.
name|getCompleteName
argument_list|()
operator|+
literal|" ignored; it is not rewrite enabled"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
comment|// We are going to create the map for each view in the given database
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|RelOptMaterialization
argument_list|>
name|cq
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|RelOptMaterialization
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|cache
condition|)
block|{
comment|// If we are caching the MV, we include it in the cache
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|RelOptMaterialization
argument_list|>
name|prevCq
init|=
name|materializedViews
operator|.
name|putIfAbsent
argument_list|(
name|materializedViewTable
operator|.
name|getDbName
argument_list|()
argument_list|,
name|cq
argument_list|)
decl_stmt|;
if|if
condition|(
name|prevCq
operator|!=
literal|null
condition|)
block|{
name|cq
operator|=
name|prevCq
expr_stmt|;
block|}
block|}
comment|// Start the process to add MV to the cache
comment|// First we parse the view query and create the materialization object
specifier|final
name|String
name|viewQuery
init|=
name|materializedViewTable
operator|.
name|getViewExpandedText
argument_list|()
decl_stmt|;
specifier|final
name|RelNode
name|viewScan
init|=
name|createMaterializedViewScan
argument_list|(
name|conf
argument_list|,
name|materializedViewTable
argument_list|)
decl_stmt|;
if|if
condition|(
name|viewScan
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Materialized view "
operator|+
name|materializedViewTable
operator|.
name|getCompleteName
argument_list|()
operator|+
literal|" ignored; error creating view replacement"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|final
name|RelNode
name|queryRel
decl_stmt|;
try|try
block|{
name|queryRel
operator|=
name|ParseUtils
operator|.
name|parseQuery
argument_list|(
name|conf
argument_list|,
name|viewQuery
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Materialized view "
operator|+
name|materializedViewTable
operator|.
name|getCompleteName
argument_list|()
operator|+
literal|" ignored; error parsing original query; "
operator|+
name|e
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|RelOptMaterialization
name|materialization
init|=
operator|new
name|RelOptMaterialization
argument_list|(
name|viewScan
argument_list|,
name|queryRel
argument_list|,
literal|null
argument_list|,
name|viewScan
operator|.
name|getTable
argument_list|()
operator|.
name|getQualifiedName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|opType
operator|==
name|OpType
operator|.
name|CREATE
condition|)
block|{
comment|// You store the materialized view
name|cq
operator|.
name|put
argument_list|(
name|materializedViewTable
operator|.
name|getTableName
argument_list|()
argument_list|,
name|materialization
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// For LOAD, you only add it if it does exist as you might be loading an outdated MV
name|cq
operator|.
name|putIfAbsent
argument_list|(
name|materializedViewTable
operator|.
name|getTableName
argument_list|()
argument_list|,
name|materialization
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Created materialized view for rewriting: "
operator|+
name|viewScan
operator|.
name|getTable
argument_list|()
operator|.
name|getQualifiedName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|materialization
return|;
block|}
comment|/**    * Removes the materialized view from the cache.    *    * @param materializedViewTable the materialized view to remove    */
specifier|public
name|void
name|dropMaterializedView
parameter_list|(
name|Table
name|materializedViewTable
parameter_list|)
block|{
name|dropMaterializedView
argument_list|(
name|materializedViewTable
operator|.
name|getDbName
argument_list|()
argument_list|,
name|materializedViewTable
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Removes the materialized view from the cache.    *    * @param dbName the db for the materialized view to remove    * @param tableName the name for the materialized view to remove    */
specifier|public
name|void
name|dropMaterializedView
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|RelOptMaterialization
argument_list|>
name|dbMap
init|=
name|materializedViews
operator|.
name|get
argument_list|(
name|dbName
argument_list|)
decl_stmt|;
if|if
condition|(
name|dbMap
operator|!=
literal|null
condition|)
block|{
name|dbMap
operator|.
name|remove
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Returns the materialized views in the cache for the given database.    *    * @param dbName the database    * @return the collection of materialized views, or the empty collection if none    */
name|RelOptMaterialization
name|getRewritingMaterializedView
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|viewName
parameter_list|)
block|{
if|if
condition|(
name|materializedViews
operator|.
name|get
argument_list|(
name|dbName
argument_list|)
operator|!=
literal|null
condition|)
block|{
return|return
name|materializedViews
operator|.
name|get
argument_list|(
name|dbName
argument_list|)
operator|.
name|get
argument_list|(
name|viewName
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
specifier|static
name|RelNode
name|createMaterializedViewScan
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|Table
name|viewTable
parameter_list|)
block|{
comment|// 0. Recreate cluster
specifier|final
name|RelOptPlanner
name|planner
init|=
name|CalcitePlanner
operator|.
name|createPlanner
argument_list|(
name|conf
argument_list|)
decl_stmt|;
specifier|final
name|RexBuilder
name|rexBuilder
init|=
operator|new
name|RexBuilder
argument_list|(
operator|new
name|JavaTypeFactoryImpl
argument_list|(
operator|new
name|HiveTypeSystemImpl
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|RelOptCluster
name|cluster
init|=
name|RelOptCluster
operator|.
name|create
argument_list|(
name|planner
argument_list|,
name|rexBuilder
argument_list|)
decl_stmt|;
comment|// 1. Create column schema
specifier|final
name|RowResolver
name|rr
init|=
operator|new
name|RowResolver
argument_list|()
decl_stmt|;
comment|// 1.1 Add Column info for non partion cols (Object Inspector fields)
name|StructObjectInspector
name|rowObjectInspector
decl_stmt|;
try|try
block|{
name|rowObjectInspector
operator|=
operator|(
name|StructObjectInspector
operator|)
name|viewTable
operator|.
name|getDeserializer
argument_list|()
operator|.
name|getObjectInspector
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
comment|// Bail out
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fields
init|=
name|rowObjectInspector
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|ColumnInfo
name|colInfo
decl_stmt|;
name|String
name|colName
decl_stmt|;
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
name|cInfoLst
init|=
operator|new
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
argument_list|()
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
name|fields
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|colName
operator|=
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldName
argument_list|()
expr_stmt|;
name|colInfo
operator|=
operator|new
name|ColumnInfo
argument_list|(
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldName
argument_list|()
argument_list|,
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|rr
operator|.
name|put
argument_list|(
literal|null
argument_list|,
name|colName
argument_list|,
name|colInfo
argument_list|)
expr_stmt|;
name|cInfoLst
operator|.
name|add
argument_list|(
name|colInfo
argument_list|)
expr_stmt|;
block|}
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
name|nonPartitionColumns
init|=
operator|new
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
argument_list|(
name|cInfoLst
argument_list|)
decl_stmt|;
comment|// 1.2 Add column info corresponding to partition columns
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
name|partitionColumns
init|=
operator|new
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FieldSchema
name|part_col
range|:
name|viewTable
operator|.
name|getPartCols
argument_list|()
control|)
block|{
name|colName
operator|=
name|part_col
operator|.
name|getName
argument_list|()
expr_stmt|;
name|colInfo
operator|=
operator|new
name|ColumnInfo
argument_list|(
name|colName
argument_list|,
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|part_col
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|rr
operator|.
name|put
argument_list|(
literal|null
argument_list|,
name|colName
argument_list|,
name|colInfo
argument_list|)
expr_stmt|;
name|cInfoLst
operator|.
name|add
argument_list|(
name|colInfo
argument_list|)
expr_stmt|;
name|partitionColumns
operator|.
name|add
argument_list|(
name|colInfo
argument_list|)
expr_stmt|;
block|}
comment|// 1.3 Build row type from field<type, name>
name|RelDataType
name|rowType
decl_stmt|;
try|try
block|{
name|rowType
operator|=
name|TypeConverter
operator|.
name|getType
argument_list|(
name|cluster
argument_list|,
name|rr
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CalciteSemanticException
name|e
parameter_list|)
block|{
comment|// Bail out
return|return
literal|null
return|;
block|}
comment|// 2. Build RelOptAbstractTable
name|List
argument_list|<
name|String
argument_list|>
name|fullyQualifiedTabName
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|viewTable
operator|.
name|getDbName
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|viewTable
operator|.
name|getDbName
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|fullyQualifiedTabName
operator|.
name|add
argument_list|(
name|viewTable
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|fullyQualifiedTabName
operator|.
name|add
argument_list|(
name|viewTable
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|RelNode
name|tableRel
decl_stmt|;
comment|// 3. Build operator
if|if
condition|(
name|obtainTableType
argument_list|(
name|viewTable
argument_list|)
operator|==
name|TableType
operator|.
name|DRUID
condition|)
block|{
comment|// Build Druid query
name|String
name|address
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_DRUID_BROKER_DEFAULT_ADDRESS
argument_list|)
decl_stmt|;
name|String
name|dataSource
init|=
name|viewTable
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|Constants
operator|.
name|DRUID_DATA_SOURCE
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|metrics
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RelDataType
argument_list|>
name|druidColTypes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|druidColNames
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|//@NOTE this code is very similar to the code at org/apache/hadoop/hive/ql/parse/CalcitePlanner.java:2362
comment|//@TODO it will be nice to refactor it
name|RelDataTypeFactory
name|dtFactory
init|=
name|cluster
operator|.
name|getRexBuilder
argument_list|()
operator|.
name|getTypeFactory
argument_list|()
decl_stmt|;
for|for
control|(
name|RelDataTypeField
name|field
range|:
name|rowType
operator|.
name|getFieldList
argument_list|()
control|)
block|{
if|if
condition|(
name|DruidTable
operator|.
name|DEFAULT_TIMESTAMP_COLUMN
operator|.
name|equals
argument_list|(
name|field
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
comment|// Druid's time column is always not null.
name|druidColTypes
operator|.
name|add
argument_list|(
name|dtFactory
operator|.
name|createTypeWithNullability
argument_list|(
name|field
operator|.
name|getType
argument_list|()
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|druidColTypes
operator|.
name|add
argument_list|(
name|field
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|druidColNames
operator|.
name|add
argument_list|(
name|field
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|field
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|DruidTable
operator|.
name|DEFAULT_TIMESTAMP_COLUMN
argument_list|)
condition|)
block|{
comment|// timestamp
continue|continue;
block|}
if|if
condition|(
name|field
operator|.
name|getType
argument_list|()
operator|.
name|getSqlTypeName
argument_list|()
operator|==
name|SqlTypeName
operator|.
name|VARCHAR
condition|)
block|{
comment|// dimension
continue|continue;
block|}
name|metrics
operator|.
name|add
argument_list|(
name|field
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Interval
argument_list|>
name|intervals
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|DruidTable
operator|.
name|DEFAULT_INTERVAL
argument_list|)
decl_stmt|;
name|rowType
operator|=
name|dtFactory
operator|.
name|createStructType
argument_list|(
name|druidColTypes
argument_list|,
name|druidColNames
argument_list|)
expr_stmt|;
name|RelOptHiveTable
name|optTable
init|=
operator|new
name|RelOptHiveTable
argument_list|(
literal|null
argument_list|,
name|cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|,
name|fullyQualifiedTabName
argument_list|,
name|rowType
argument_list|,
name|viewTable
argument_list|,
name|nonPartitionColumns
argument_list|,
name|partitionColumns
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|,
name|conf
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|,
operator|new
name|AtomicInteger
argument_list|()
argument_list|)
decl_stmt|;
name|DruidTable
name|druidTable
init|=
operator|new
name|DruidTable
argument_list|(
operator|new
name|DruidSchema
argument_list|(
name|address
argument_list|,
name|address
argument_list|,
literal|false
argument_list|)
argument_list|,
name|dataSource
argument_list|,
name|RelDataTypeImpl
operator|.
name|proto
argument_list|(
name|rowType
argument_list|)
argument_list|,
name|metrics
argument_list|,
name|DruidTable
operator|.
name|DEFAULT_TIMESTAMP_COLUMN
argument_list|,
name|intervals
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
specifier|final
name|TableScan
name|scan
init|=
operator|new
name|HiveTableScan
argument_list|(
name|cluster
argument_list|,
name|cluster
operator|.
name|traitSetOf
argument_list|(
name|HiveRelNode
operator|.
name|CONVENTION
argument_list|)
argument_list|,
name|optTable
argument_list|,
name|viewTable
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|tableRel
operator|=
name|DruidQuery
operator|.
name|create
argument_list|(
name|cluster
argument_list|,
name|cluster
operator|.
name|traitSetOf
argument_list|(
name|BindableConvention
operator|.
name|INSTANCE
argument_list|)
argument_list|,
name|optTable
argument_list|,
name|druidTable
argument_list|,
name|ImmutableList
operator|.
expr|<
name|RelNode
operator|>
name|of
argument_list|(
name|scan
argument_list|)
argument_list|,
name|ImmutableMap
operator|.
name|of
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Build Hive Table Scan Rel
name|RelOptHiveTable
name|optTable
init|=
operator|new
name|RelOptHiveTable
argument_list|(
literal|null
argument_list|,
name|cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|,
name|fullyQualifiedTabName
argument_list|,
name|rowType
argument_list|,
name|viewTable
argument_list|,
name|nonPartitionColumns
argument_list|,
name|partitionColumns
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|,
name|conf
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|,
operator|new
name|AtomicInteger
argument_list|()
argument_list|)
decl_stmt|;
name|tableRel
operator|=
operator|new
name|HiveTableScan
argument_list|(
name|cluster
argument_list|,
name|cluster
operator|.
name|traitSetOf
argument_list|(
name|HiveRelNode
operator|.
name|CONVENTION
argument_list|)
argument_list|,
name|optTable
argument_list|,
name|viewTable
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
return|return
name|tableRel
return|;
block|}
specifier|private
specifier|static
name|TableType
name|obtainTableType
parameter_list|(
name|Table
name|tabMetaData
parameter_list|)
block|{
if|if
condition|(
name|tabMetaData
operator|.
name|getStorageHandler
argument_list|()
operator|!=
literal|null
condition|)
block|{
specifier|final
name|String
name|storageHandlerStr
init|=
name|tabMetaData
operator|.
name|getStorageHandler
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|storageHandlerStr
operator|.
name|equals
argument_list|(
name|Constants
operator|.
name|DRUID_HIVE_STORAGE_HANDLER_ID
argument_list|)
condition|)
block|{
return|return
name|TableType
operator|.
name|DRUID
return|;
block|}
if|if
condition|(
name|storageHandlerStr
operator|.
name|equals
argument_list|(
name|Constants
operator|.
name|JDBC_HIVE_STORAGE_HANDLER_ID
argument_list|)
condition|)
block|{
return|return
name|TableType
operator|.
name|JDBC
return|;
block|}
block|}
return|return
name|TableType
operator|.
name|NATIVE
return|;
block|}
comment|//@TODO this seems to be the same as org.apache.hadoop.hive.ql.parse.CalcitePlanner.TableType.DRUID do we really need both
specifier|private
enum|enum
name|TableType
block|{
name|DRUID
block|,
name|NATIVE
block|,
name|JDBC
block|}
specifier|private
enum|enum
name|OpType
block|{
name|CREATE
block|,
comment|//view just being created
name|LOAD
comment|// already created view being loaded
block|}
block|}
end_class

end_unit

