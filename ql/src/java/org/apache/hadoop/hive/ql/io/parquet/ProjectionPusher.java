begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|parquet
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
name|io
operator|.
name|Serializable
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
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|Set
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
name|StringInternUtils
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
name|SerializationUtilities
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
name|ql
operator|.
name|exec
operator|.
name|Operator
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
name|RowSchema
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
name|TableScanOperator
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
name|UDFArgumentException
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
name|io
operator|.
name|HiveFileFormatUtils
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
name|ExprNodeGenericFuncDesc
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
name|MapWork
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
name|PartitionDesc
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
name|TableScanDesc
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFOPOr
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
name|ColumnProjectionUtils
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

begin_class
specifier|public
class|class
name|ProjectionPusher
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
name|ProjectionPusher
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|Path
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartitionInfo
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * MapWork is the Hive object which describes input files,    * columns projections, and filters.    */
specifier|private
name|MapWork
name|mapWork
decl_stmt|;
comment|/**    * Sets the mapWork variable based on the current JobConf in order to get all partitions.    *    * @param job    */
specifier|private
name|void
name|updateMrWork
parameter_list|(
specifier|final
name|JobConf
name|job
parameter_list|)
block|{
specifier|final
name|String
name|plan
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|job
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PLAN
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapWork
operator|==
literal|null
operator|&&
name|plan
operator|!=
literal|null
operator|&&
name|plan
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|mapWork
operator|=
name|Utilities
operator|.
name|getMapWork
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|pathToPartitionInfo
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
specifier|final
name|Map
operator|.
name|Entry
argument_list|<
name|Path
argument_list|,
name|PartitionDesc
argument_list|>
name|entry
range|:
name|mapWork
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|// key contains scheme (such as pfile://) and we want only the path portion fix in HIVE-6366
name|pathToPartitionInfo
operator|.
name|put
argument_list|(
name|StringInternUtils
operator|.
name|internUriStringsInPath
argument_list|(
name|Path
operator|.
name|getPathWithoutSchemeAndAuthority
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|pushProjectionsAndFilters
parameter_list|(
specifier|final
name|JobConf
name|jobConf
parameter_list|,
specifier|final
name|String
name|splitPath
parameter_list|,
specifier|final
name|String
name|splitPathWithNoSchema
parameter_list|)
block|{
if|if
condition|(
name|mapWork
operator|==
literal|null
condition|)
block|{
return|return;
block|}
elseif|else
if|if
condition|(
name|mapWork
operator|.
name|getPathToAliases
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return;
block|}
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|aliases
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
name|List
argument_list|<
name|String
argument_list|>
name|a
init|=
name|HiveFileFormatUtils
operator|.
name|getFromPathRecursively
argument_list|(
name|mapWork
operator|.
name|getPathToAliases
argument_list|()
argument_list|,
operator|new
name|Path
argument_list|(
name|splitPath
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|a
operator|!=
literal|null
condition|)
block|{
name|aliases
operator|.
name|addAll
argument_list|(
name|a
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|a
operator|==
literal|null
operator|||
name|a
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// TODO: not having aliases for path usually means some bug. Should it give up?
name|LOG
operator|.
name|warn
argument_list|(
literal|"Couldn't find aliases for "
operator|+
name|splitPath
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
decl||
name|IOException
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
comment|// Collect the needed columns from all the aliases and create ORed filter
comment|// expression for the table.
name|boolean
name|allColumnsNeeded
init|=
literal|false
decl_stmt|;
name|boolean
name|noFilters
init|=
literal|false
decl_stmt|;
name|Set
argument_list|<
name|Integer
argument_list|>
name|neededColumnIDs
init|=
operator|new
name|HashSet
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
comment|// To support nested column pruning, we need to track the path from the top to the nested
comment|// fields
name|Set
argument_list|<
name|String
argument_list|>
name|neededNestedColumnPaths
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ExprNodeGenericFuncDesc
argument_list|>
name|filterExprs
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeGenericFuncDesc
argument_list|>
argument_list|()
decl_stmt|;
name|RowSchema
name|rowSchema
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|alias
range|:
name|aliases
control|)
block|{
specifier|final
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
init|=
name|mapWork
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
if|if
condition|(
name|op
operator|!=
literal|null
operator|&&
name|op
operator|instanceof
name|TableScanOperator
condition|)
block|{
specifier|final
name|TableScanOperator
name|ts
init|=
operator|(
name|TableScanOperator
operator|)
name|op
decl_stmt|;
if|if
condition|(
name|ts
operator|.
name|getNeededColumnIDs
argument_list|()
operator|==
literal|null
condition|)
block|{
name|allColumnsNeeded
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|neededColumnIDs
operator|.
name|addAll
argument_list|(
name|ts
operator|.
name|getNeededColumnIDs
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|ts
operator|.
name|getNeededNestedColumnPaths
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|neededNestedColumnPaths
operator|.
name|addAll
argument_list|(
name|ts
operator|.
name|getNeededNestedColumnPaths
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|rowSchema
operator|=
name|ts
operator|.
name|getSchema
argument_list|()
expr_stmt|;
name|ExprNodeGenericFuncDesc
name|filterExpr
init|=
name|ts
operator|.
name|getConf
argument_list|()
operator|==
literal|null
condition|?
literal|null
else|:
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|getFilterExpr
argument_list|()
decl_stmt|;
name|noFilters
operator|=
name|filterExpr
operator|==
literal|null
expr_stmt|;
comment|// No filter if any TS has no filter expression
name|filterExprs
operator|.
name|add
argument_list|(
name|filterExpr
argument_list|)
expr_stmt|;
block|}
block|}
name|ExprNodeGenericFuncDesc
name|tableFilterExpr
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|noFilters
condition|)
block|{
try|try
block|{
for|for
control|(
name|ExprNodeGenericFuncDesc
name|filterExpr
range|:
name|filterExprs
control|)
block|{
if|if
condition|(
name|tableFilterExpr
operator|==
literal|null
condition|)
block|{
name|tableFilterExpr
operator|=
name|filterExpr
expr_stmt|;
block|}
else|else
block|{
name|tableFilterExpr
operator|=
name|ExprNodeGenericFuncDesc
operator|.
name|newInstance
argument_list|(
operator|new
name|GenericUDFOPOr
argument_list|()
argument_list|,
name|Arrays
operator|.
expr|<
name|ExprNodeDesc
operator|>
name|asList
argument_list|(
name|tableFilterExpr
argument_list|,
name|filterExpr
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|UDFArgumentException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Turn off filtering due to "
operator|+
name|ex
argument_list|)
expr_stmt|;
name|tableFilterExpr
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|// push down projections
if|if
condition|(
operator|!
name|allColumnsNeeded
condition|)
block|{
if|if
condition|(
operator|!
name|neededColumnIDs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ColumnProjectionUtils
operator|.
name|appendReadColumns
argument_list|(
name|jobConf
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|neededColumnIDs
argument_list|)
argument_list|)
expr_stmt|;
name|ColumnProjectionUtils
operator|.
name|appendNestedColumnPaths
argument_list|(
name|jobConf
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|neededNestedColumnPaths
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|ColumnProjectionUtils
operator|.
name|setReadAllColumns
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
block|}
name|pushFilters
argument_list|(
name|jobConf
argument_list|,
name|rowSchema
argument_list|,
name|tableFilterExpr
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|pushFilters
parameter_list|(
specifier|final
name|JobConf
name|jobConf
parameter_list|,
name|RowSchema
name|rowSchema
parameter_list|,
name|ExprNodeGenericFuncDesc
name|filterExpr
parameter_list|)
block|{
comment|// construct column name list for reference by filter push down
name|Utilities
operator|.
name|setColumnNameList
argument_list|(
name|jobConf
argument_list|,
name|rowSchema
argument_list|)
expr_stmt|;
comment|// push down filters
if|if
condition|(
name|filterExpr
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Not pushing filters because FilterExpr is null"
argument_list|)
expr_stmt|;
return|return;
block|}
specifier|final
name|String
name|filterText
init|=
name|filterExpr
operator|.
name|getExprString
argument_list|()
decl_stmt|;
specifier|final
name|String
name|filterExprSerialized
init|=
name|SerializationUtilities
operator|.
name|serializeExpression
argument_list|(
name|filterExpr
argument_list|)
decl_stmt|;
name|jobConf
operator|.
name|set
argument_list|(
name|TableScanDesc
operator|.
name|FILTER_TEXT_CONF_STR
argument_list|,
name|filterText
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|set
argument_list|(
name|TableScanDesc
operator|.
name|FILTER_EXPR_CONF_STR
argument_list|,
name|filterExprSerialized
argument_list|)
expr_stmt|;
block|}
specifier|public
name|JobConf
name|pushProjectionsAndFilters
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|updateMrWork
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
comment|// TODO: refactor this in HIVE-6366
specifier|final
name|JobConf
name|cloneJobConf
init|=
operator|new
name|JobConf
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
specifier|final
name|PartitionDesc
name|part
init|=
name|HiveFileFormatUtils
operator|.
name|getFromPathRecursively
argument_list|(
name|pathToPartitionInfo
argument_list|,
name|path
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|(
name|part
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|part
operator|.
name|getTableDesc
argument_list|()
operator|!=
literal|null
operator|)
condition|)
block|{
name|Utilities
operator|.
name|copyTableJobPropertiesToConf
argument_list|(
name|part
operator|.
name|getTableDesc
argument_list|()
argument_list|,
name|cloneJobConf
argument_list|)
expr_stmt|;
block|}
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
name|pushProjectionsAndFilters
argument_list|(
name|cloneJobConf
argument_list|,
name|path
operator|.
name|toString
argument_list|()
argument_list|,
name|path
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|cloneJobConf
return|;
block|}
block|}
end_class

end_unit

