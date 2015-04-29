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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|parse
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|Context
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
name|ErrorMsg
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
name|InvalidTableException
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
name|serde
operator|.
name|serdeConstants
import|;
end_import

begin_comment
comment|/**  * ColumnStatsSemanticAnalyzer.  * Handles semantic analysis and rewrite for gathering column statistics both at the level of a  * partition and a table. Note that table statistics are implemented in SemanticAnalyzer.  *  */
end_comment

begin_class
specifier|public
class|class
name|ColumnStatsSemanticAnalyzer
extends|extends
name|SemanticAnalyzer
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ColumnStatsSemanticAnalyzer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ASTNode
name|originalTree
decl_stmt|;
specifier|private
name|ASTNode
name|rewrittenTree
decl_stmt|;
specifier|private
name|String
name|rewrittenQuery
decl_stmt|;
specifier|private
name|Context
name|ctx
decl_stmt|;
specifier|private
name|boolean
name|isRewritten
decl_stmt|;
specifier|private
name|boolean
name|isTableLevel
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|colNames
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|colType
decl_stmt|;
specifier|private
name|Table
name|tbl
decl_stmt|;
specifier|public
name|ColumnStatsSemanticAnalyzer
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|SemanticException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|shouldRewrite
parameter_list|(
name|ASTNode
name|tree
parameter_list|)
block|{
name|boolean
name|rwt
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|tree
operator|.
name|getChildCount
argument_list|()
operator|>
literal|1
condition|)
block|{
name|ASTNode
name|child0
init|=
operator|(
name|ASTNode
operator|)
name|tree
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ASTNode
name|child1
decl_stmt|;
if|if
condition|(
name|child0
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_TAB
condition|)
block|{
name|child0
operator|=
operator|(
name|ASTNode
operator|)
name|child0
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
if|if
condition|(
name|child0
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_TABNAME
condition|)
block|{
name|child1
operator|=
operator|(
name|ASTNode
operator|)
name|tree
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|child1
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|KW_COLUMNS
condition|)
block|{
name|rwt
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|rwt
return|;
block|}
specifier|private
name|boolean
name|isPartitionLevelStats
parameter_list|(
name|ASTNode
name|tree
parameter_list|)
block|{
name|boolean
name|isPartitioned
init|=
literal|false
decl_stmt|;
name|ASTNode
name|child
init|=
operator|(
name|ASTNode
operator|)
name|tree
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|child
operator|.
name|getChildCount
argument_list|()
operator|>
literal|1
condition|)
block|{
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|child
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|child
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_PARTSPEC
condition|)
block|{
name|isPartitioned
operator|=
literal|true
expr_stmt|;
block|}
block|}
return|return
name|isPartitioned
return|;
block|}
specifier|private
name|Table
name|getTable
parameter_list|(
name|ASTNode
name|tree
parameter_list|)
throws|throws
name|SemanticException
block|{
name|String
name|tableName
init|=
name|getUnescapedName
argument_list|(
operator|(
name|ASTNode
operator|)
name|tree
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|currentDb
init|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getCurrentDatabase
argument_list|()
decl_stmt|;
name|String
index|[]
name|names
init|=
name|Utilities
operator|.
name|getDbTableName
argument_list|(
name|currentDb
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
return|return
name|getTable
argument_list|(
name|names
index|[
literal|0
index|]
argument_list|,
name|names
index|[
literal|1
index|]
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getPartKeyValuePairsFromAST
parameter_list|(
name|Table
name|tbl
parameter_list|,
name|ASTNode
name|tree
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ASTNode
name|child
init|=
operator|(
operator|(
name|ASTNode
operator|)
name|tree
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|child
operator|!=
literal|null
condition|)
block|{
name|partSpec
operator|=
name|DDLSemanticAnalyzer
operator|.
name|getValidatedPartSpec
argument_list|(
name|tbl
argument_list|,
name|child
argument_list|,
name|hiveConf
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|//otherwise, it is the case of analyze table T compute statistics for columns;
return|return
name|partSpec
return|;
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|getColumnName
parameter_list|(
name|ASTNode
name|tree
parameter_list|)
throws|throws
name|SemanticException
block|{
switch|switch
condition|(
name|tree
operator|.
name|getChildCount
argument_list|()
condition|)
block|{
case|case
literal|2
case|:
return|return
name|Utilities
operator|.
name|getColumnNamesFromFieldSchema
argument_list|(
name|tbl
operator|.
name|getCols
argument_list|()
argument_list|)
return|;
case|case
literal|3
case|:
name|int
name|numCols
init|=
name|tree
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
operator|.
name|getChildCount
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|colName
init|=
operator|new
name|LinkedList
argument_list|<
name|String
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
name|numCols
condition|;
name|i
operator|++
control|)
block|{
name|colName
operator|.
name|add
argument_list|(
name|i
argument_list|,
operator|new
name|String
argument_list|(
name|getUnescapedName
argument_list|(
operator|(
name|ASTNode
operator|)
name|tree
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|colName
return|;
default|default:
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Internal error. Expected number of children of ASTNode to be"
operator|+
literal|" either 2 or 3. Found : "
operator|+
name|tree
operator|.
name|getChildCount
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|handlePartialPartitionSpec
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// If user has fully specified partition, validate that partition exists
name|int
name|partValsSpecified
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|partKey
range|:
name|partSpec
operator|.
name|keySet
argument_list|()
control|)
block|{
name|partValsSpecified
operator|+=
name|partSpec
operator|.
name|get
argument_list|(
name|partKey
argument_list|)
operator|==
literal|null
condition|?
literal|0
else|:
literal|1
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
operator|(
name|partValsSpecified
operator|==
name|tbl
operator|.
name|getPartitionKeys
argument_list|()
operator|.
name|size
argument_list|()
operator|)
operator|&&
operator|(
name|db
operator|.
name|getPartition
argument_list|(
name|tbl
argument_list|,
name|partSpec
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
operator|==
literal|null
operator|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|COLUMNSTATSCOLLECTOR_INVALID_PARTITION
operator|.
name|getMsg
argument_list|()
operator|+
literal|" : "
operator|+
name|partSpec
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|he
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|COLUMNSTATSCOLLECTOR_INVALID_PARTITION
operator|.
name|getMsg
argument_list|()
operator|+
literal|" : "
operator|+
name|partSpec
argument_list|)
throw|;
block|}
comment|// User might have only specified partial list of partition keys, in which case add other partition keys in partSpec
name|List
argument_list|<
name|String
argument_list|>
name|partKeys
init|=
name|Utilities
operator|.
name|getColumnNamesFromFieldSchema
argument_list|(
name|tbl
operator|.
name|getPartitionKeys
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|partKey
range|:
name|partKeys
control|)
block|{
if|if
condition|(
operator|!
name|partSpec
operator|.
name|containsKey
argument_list|(
name|partKey
argument_list|)
condition|)
block|{
name|partSpec
operator|.
name|put
argument_list|(
name|partKey
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Check if user have erroneously specified non-existent partitioning columns
for|for
control|(
name|String
name|partKey
range|:
name|partSpec
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|partKeys
operator|.
name|contains
argument_list|(
name|partKey
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|COLUMNSTATSCOLLECTOR_INVALID_PART_KEY
operator|.
name|getMsg
argument_list|()
operator|+
literal|" : "
operator|+
name|partKey
argument_list|)
throw|;
block|}
block|}
block|}
specifier|private
name|StringBuilder
name|genPartitionClause
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
throws|throws
name|SemanticException
block|{
name|StringBuilder
name|whereClause
init|=
operator|new
name|StringBuilder
argument_list|(
literal|" where "
argument_list|)
decl_stmt|;
name|boolean
name|predPresent
init|=
literal|false
decl_stmt|;
name|StringBuilder
name|groupByClause
init|=
operator|new
name|StringBuilder
argument_list|(
literal|" group by "
argument_list|)
decl_stmt|;
name|boolean
name|aggPresent
init|=
literal|false
decl_stmt|;
for|for
control|(
name|String
name|partKey
range|:
name|partSpec
operator|.
name|keySet
argument_list|()
control|)
block|{
name|String
name|value
decl_stmt|;
if|if
condition|(
operator|(
name|value
operator|=
name|partSpec
operator|.
name|get
argument_list|(
name|partKey
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|predPresent
condition|)
block|{
name|predPresent
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|whereClause
operator|.
name|append
argument_list|(
literal|" and "
argument_list|)
expr_stmt|;
block|}
name|whereClause
operator|.
name|append
argument_list|(
name|partKey
argument_list|)
operator|.
name|append
argument_list|(
literal|" = "
argument_list|)
operator|.
name|append
argument_list|(
name|genPartValueString
argument_list|(
name|partKey
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|FieldSchema
name|fs
range|:
name|tbl
operator|.
name|getPartitionKeys
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|aggPresent
condition|)
block|{
name|aggPresent
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|groupByClause
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|groupByClause
operator|.
name|append
argument_list|(
name|fs
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// attach the predicate and group by to the return clause
return|return
name|predPresent
condition|?
name|whereClause
operator|.
name|append
argument_list|(
name|groupByClause
argument_list|)
else|:
name|groupByClause
return|;
block|}
specifier|private
name|String
name|genPartValueString
parameter_list|(
name|String
name|partKey
parameter_list|,
name|String
name|partVal
parameter_list|)
throws|throws
name|SemanticException
block|{
name|String
name|returnVal
init|=
name|partVal
decl_stmt|;
name|String
name|partColType
init|=
name|getColTypeOf
argument_list|(
name|partKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|partColType
operator|.
name|equals
argument_list|(
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|)
operator|||
name|partColType
operator|.
name|contains
argument_list|(
name|serdeConstants
operator|.
name|VARCHAR_TYPE_NAME
argument_list|)
operator|||
name|partColType
operator|.
name|contains
argument_list|(
name|serdeConstants
operator|.
name|CHAR_TYPE_NAME
argument_list|)
condition|)
block|{
name|returnVal
operator|=
literal|"'"
operator|+
name|partVal
operator|+
literal|"'"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|partColType
operator|.
name|equals
argument_list|(
name|serdeConstants
operator|.
name|TINYINT_TYPE_NAME
argument_list|)
condition|)
block|{
name|returnVal
operator|=
name|partVal
operator|+
literal|"Y"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|partColType
operator|.
name|equals
argument_list|(
name|serdeConstants
operator|.
name|SMALLINT_TYPE_NAME
argument_list|)
condition|)
block|{
name|returnVal
operator|=
name|partVal
operator|+
literal|"S"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|partColType
operator|.
name|equals
argument_list|(
name|serdeConstants
operator|.
name|INT_TYPE_NAME
argument_list|)
condition|)
block|{
name|returnVal
operator|=
name|partVal
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|partColType
operator|.
name|equals
argument_list|(
name|serdeConstants
operator|.
name|BIGINT_TYPE_NAME
argument_list|)
condition|)
block|{
name|returnVal
operator|=
name|partVal
operator|+
literal|"L"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|partColType
operator|.
name|contains
argument_list|(
name|serdeConstants
operator|.
name|DECIMAL_TYPE_NAME
argument_list|)
condition|)
block|{
name|returnVal
operator|=
name|partVal
operator|+
literal|"BD"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|partColType
operator|.
name|equals
argument_list|(
name|serdeConstants
operator|.
name|DATE_TYPE_NAME
argument_list|)
operator|||
name|partColType
operator|.
name|equals
argument_list|(
name|serdeConstants
operator|.
name|TIMESTAMP_TYPE_NAME
argument_list|)
condition|)
block|{
name|returnVal
operator|=
name|partColType
operator|+
literal|" '"
operator|+
name|partVal
operator|+
literal|"'"
expr_stmt|;
block|}
else|else
block|{
comment|//for other usually not used types, just quote the value
name|returnVal
operator|=
literal|"'"
operator|+
name|partVal
operator|+
literal|"'"
expr_stmt|;
block|}
return|return
name|returnVal
return|;
block|}
specifier|private
name|String
name|getColTypeOf
parameter_list|(
name|String
name|partKey
parameter_list|)
throws|throws
name|SemanticException
block|{
for|for
control|(
name|FieldSchema
name|fs
range|:
name|tbl
operator|.
name|getPartitionKeys
argument_list|()
control|)
block|{
if|if
condition|(
name|partKey
operator|.
name|equalsIgnoreCase
argument_list|(
name|fs
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|fs
operator|.
name|getType
argument_list|()
operator|.
name|toLowerCase
argument_list|()
return|;
block|}
block|}
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Unknown partition key : "
operator|+
name|partKey
argument_list|)
throw|;
block|}
specifier|private
name|int
name|getNumBitVectorsForNDVEstimation
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|SemanticException
block|{
name|int
name|numBitVectors
decl_stmt|;
name|float
name|percentageError
init|=
name|HiveConf
operator|.
name|getFloatVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_STATS_NDV_ERROR
argument_list|)
decl_stmt|;
if|if
condition|(
name|percentageError
operator|<
literal|0.0
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"hive.stats.ndv.error can't be negative"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|percentageError
operator|<=
literal|2.4
condition|)
block|{
name|numBitVectors
operator|=
literal|1024
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Lowest error achievable is 2.4% but error requested is "
operator|+
name|percentageError
operator|+
literal|"%"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Choosing 1024 bit vectors.."
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|percentageError
operator|<=
literal|3.4
condition|)
block|{
name|numBitVectors
operator|=
literal|1024
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Error requested is "
operator|+
name|percentageError
operator|+
literal|"%"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Choosing 1024 bit vectors.."
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|percentageError
operator|<=
literal|4.8
condition|)
block|{
name|numBitVectors
operator|=
literal|512
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Error requested is "
operator|+
name|percentageError
operator|+
literal|"%"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Choosing 512 bit vectors.."
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|percentageError
operator|<=
literal|6.8
condition|)
block|{
name|numBitVectors
operator|=
literal|256
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Error requested is "
operator|+
name|percentageError
operator|+
literal|"%"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Choosing 256 bit vectors.."
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|percentageError
operator|<=
literal|9.7
condition|)
block|{
name|numBitVectors
operator|=
literal|128
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Error requested is "
operator|+
name|percentageError
operator|+
literal|"%"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Choosing 128 bit vectors.."
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|percentageError
operator|<=
literal|13.8
condition|)
block|{
name|numBitVectors
operator|=
literal|64
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Error requested is "
operator|+
name|percentageError
operator|+
literal|"%"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Choosing 64 bit vectors.."
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|percentageError
operator|<=
literal|19.6
condition|)
block|{
name|numBitVectors
operator|=
literal|32
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Error requested is "
operator|+
name|percentageError
operator|+
literal|"%"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Choosing 32 bit vectors.."
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|percentageError
operator|<=
literal|28.2
condition|)
block|{
name|numBitVectors
operator|=
literal|16
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Error requested is "
operator|+
name|percentageError
operator|+
literal|"%"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Choosing 16 bit vectors.."
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|percentageError
operator|<=
literal|40.9
condition|)
block|{
name|numBitVectors
operator|=
literal|8
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Error requested is "
operator|+
name|percentageError
operator|+
literal|"%"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Choosing 8 bit vectors.."
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|percentageError
operator|<=
literal|61.0
condition|)
block|{
name|numBitVectors
operator|=
literal|4
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Error requested is "
operator|+
name|percentageError
operator|+
literal|"%"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Choosing 4 bit vectors.."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|numBitVectors
operator|=
literal|2
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Error requested is "
operator|+
name|percentageError
operator|+
literal|"%"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Choosing 2 bit vectors.."
argument_list|)
expr_stmt|;
block|}
return|return
name|numBitVectors
return|;
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|getColumnTypes
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|colNames
parameter_list|)
throws|throws
name|SemanticException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|colTypes
init|=
operator|new
name|LinkedList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
init|=
name|tbl
operator|.
name|getCols
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|colName
range|:
name|colNames
control|)
block|{
for|for
control|(
name|FieldSchema
name|col
range|:
name|cols
control|)
block|{
if|if
condition|(
name|colName
operator|.
name|equalsIgnoreCase
argument_list|(
name|col
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|colTypes
operator|.
name|add
argument_list|(
operator|new
name|String
argument_list|(
name|col
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|colTypes
return|;
block|}
specifier|private
name|String
name|genRewrittenQuery
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|colNames
parameter_list|,
name|int
name|numBitVectors
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|,
name|boolean
name|isPartitionStats
parameter_list|)
throws|throws
name|SemanticException
block|{
name|StringBuilder
name|rewrittenQueryBuilder
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"select "
argument_list|)
decl_stmt|;
name|String
name|rewrittenQuery
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
name|colNames
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|rewrittenQueryBuilder
operator|.
name|append
argument_list|(
literal|" , "
argument_list|)
expr_stmt|;
block|}
name|rewrittenQueryBuilder
operator|.
name|append
argument_list|(
literal|"compute_stats("
argument_list|)
expr_stmt|;
name|rewrittenQueryBuilder
operator|.
name|append
argument_list|(
name|colNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|rewrittenQueryBuilder
operator|.
name|append
argument_list|(
literal|" , "
argument_list|)
expr_stmt|;
name|rewrittenQueryBuilder
operator|.
name|append
argument_list|(
name|numBitVectors
argument_list|)
expr_stmt|;
name|rewrittenQueryBuilder
operator|.
name|append
argument_list|(
literal|" )"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isPartitionStats
condition|)
block|{
for|for
control|(
name|FieldSchema
name|fs
range|:
name|tbl
operator|.
name|getPartCols
argument_list|()
control|)
block|{
name|rewrittenQueryBuilder
operator|.
name|append
argument_list|(
literal|" , "
operator|+
name|fs
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|rewrittenQueryBuilder
operator|.
name|append
argument_list|(
literal|" from "
argument_list|)
expr_stmt|;
name|rewrittenQueryBuilder
operator|.
name|append
argument_list|(
name|tbl
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|rewrittenQueryBuilder
operator|.
name|append
argument_list|(
literal|"."
argument_list|)
expr_stmt|;
name|rewrittenQueryBuilder
operator|.
name|append
argument_list|(
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|isRewritten
operator|=
literal|true
expr_stmt|;
comment|// If partition level statistics is requested, add predicate and group by as needed to rewritten
comment|// query
if|if
condition|(
name|isPartitionStats
condition|)
block|{
name|rewrittenQueryBuilder
operator|.
name|append
argument_list|(
name|genPartitionClause
argument_list|(
name|partSpec
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|rewrittenQuery
operator|=
name|rewrittenQueryBuilder
operator|.
name|toString
argument_list|()
expr_stmt|;
name|rewrittenQuery
operator|=
operator|new
name|VariableSubstitution
argument_list|()
operator|.
name|substitute
argument_list|(
name|conf
argument_list|,
name|rewrittenQuery
argument_list|)
expr_stmt|;
return|return
name|rewrittenQuery
return|;
block|}
specifier|private
name|ASTNode
name|genRewrittenTree
parameter_list|(
name|String
name|rewrittenQuery
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ASTNode
name|rewrittenTree
decl_stmt|;
comment|// Parse the rewritten query string
try|try
block|{
name|ctx
operator|=
operator|new
name|Context
argument_list|(
name|conf
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
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|COLUMNSTATSCOLLECTOR_IO_ERROR
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
name|ctx
operator|.
name|setCmd
argument_list|(
name|rewrittenQuery
argument_list|)
expr_stmt|;
name|ParseDriver
name|pd
init|=
operator|new
name|ParseDriver
argument_list|()
decl_stmt|;
try|try
block|{
name|rewrittenTree
operator|=
name|pd
operator|.
name|parse
argument_list|(
name|rewrittenQuery
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|COLUMNSTATSCOLLECTOR_PARSE_ERROR
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
name|rewrittenTree
operator|=
name|ParseUtils
operator|.
name|findRootNonNullToken
argument_list|(
name|rewrittenTree
argument_list|)
expr_stmt|;
return|return
name|rewrittenTree
return|;
block|}
comment|// fail early if the columns specified for column statistics are not valid
specifier|private
name|void
name|validateSpecifiedColumnNames
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|specifiedCols
parameter_list|)
throws|throws
name|SemanticException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|tableCols
init|=
name|Utilities
operator|.
name|getColumnNamesFromFieldSchema
argument_list|(
name|tbl
operator|.
name|getCols
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|sc
range|:
name|specifiedCols
control|)
block|{
if|if
condition|(
operator|!
name|tableCols
operator|.
name|contains
argument_list|(
name|sc
operator|.
name|toLowerCase
argument_list|()
argument_list|)
condition|)
block|{
name|String
name|msg
init|=
literal|"'"
operator|+
name|sc
operator|+
literal|"' (possible columns are "
operator|+
name|tableCols
operator|.
name|toString
argument_list|()
operator|+
literal|")"
decl_stmt|;
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_COLUMN
operator|.
name|getMsg
argument_list|(
name|msg
argument_list|)
argument_list|)
throw|;
block|}
block|}
block|}
specifier|private
name|void
name|checkForPartitionColumns
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|specifiedCols
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partCols
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// Raise error if user has specified partition column for stats
for|for
control|(
name|String
name|pc
range|:
name|partCols
control|)
block|{
for|for
control|(
name|String
name|sc
range|:
name|specifiedCols
control|)
block|{
if|if
condition|(
name|pc
operator|.
name|equalsIgnoreCase
argument_list|(
name|sc
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|COLUMNSTATSCOLLECTOR_INVALID_COLUMN
operator|.
name|getMsg
argument_list|()
operator|+
literal|" [Try removing column '"
operator|+
name|sc
operator|+
literal|"' from column list]"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|analyze
parameter_list|(
name|ASTNode
name|ast
parameter_list|,
name|Context
name|origCtx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|QB
name|qb
decl_stmt|;
name|QBParseInfo
name|qbp
decl_stmt|;
comment|// initialize QB
name|init
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// check if it is no scan. grammar prevents coexit noscan/columns
name|super
operator|.
name|processNoScanCommand
argument_list|(
name|ast
argument_list|)
expr_stmt|;
comment|// check if it is partial scan. grammar prevents coexit partialscan/columns
name|super
operator|.
name|processPartialScanCommand
argument_list|(
name|ast
argument_list|)
expr_stmt|;
comment|/* Rewrite only analyze table<> column<> compute statistics; Don't rewrite analyze table      * command - table stats are collected by the table scan operator and is not rewritten to      * an aggregation.      */
if|if
condition|(
name|shouldRewrite
argument_list|(
name|ast
argument_list|)
condition|)
block|{
name|tbl
operator|=
name|getTable
argument_list|(
name|ast
argument_list|)
expr_stmt|;
name|colNames
operator|=
name|getColumnName
argument_list|(
name|ast
argument_list|)
expr_stmt|;
comment|// Save away the original AST
name|originalTree
operator|=
name|ast
expr_stmt|;
name|boolean
name|isPartitionStats
init|=
name|isPartitionLevelStats
argument_list|(
name|ast
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
literal|null
decl_stmt|;
name|checkForPartitionColumns
argument_list|(
name|colNames
argument_list|,
name|Utilities
operator|.
name|getColumnNamesFromFieldSchema
argument_list|(
name|tbl
operator|.
name|getPartitionKeys
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|validateSpecifiedColumnNames
argument_list|(
name|colNames
argument_list|)
expr_stmt|;
if|if
condition|(
name|conf
operator|.
name|getBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_STATS_COLLECT_PART_LEVEL_STATS
argument_list|)
operator|&&
name|tbl
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
name|isPartitionStats
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|isPartitionStats
condition|)
block|{
name|isTableLevel
operator|=
literal|false
expr_stmt|;
name|partSpec
operator|=
name|getPartKeyValuePairsFromAST
argument_list|(
name|tbl
argument_list|,
name|ast
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|handlePartialPartitionSpec
argument_list|(
name|partSpec
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|isTableLevel
operator|=
literal|true
expr_stmt|;
block|}
name|colType
operator|=
name|getColumnTypes
argument_list|(
name|colNames
argument_list|)
expr_stmt|;
name|int
name|numBitVectors
init|=
name|getNumBitVectorsForNDVEstimation
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|rewrittenQuery
operator|=
name|genRewrittenQuery
argument_list|(
name|colNames
argument_list|,
name|numBitVectors
argument_list|,
name|partSpec
argument_list|,
name|isPartitionStats
argument_list|)
expr_stmt|;
name|rewrittenTree
operator|=
name|genRewrittenTree
argument_list|(
name|rewrittenQuery
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Not an analyze table column compute statistics statement - don't do any rewrites
name|originalTree
operator|=
name|rewrittenTree
operator|=
name|ast
expr_stmt|;
name|rewrittenQuery
operator|=
literal|null
expr_stmt|;
name|isRewritten
operator|=
literal|false
expr_stmt|;
block|}
comment|// Setup the necessary metadata if originating from analyze rewrite
if|if
condition|(
name|isRewritten
condition|)
block|{
name|qb
operator|=
name|getQB
argument_list|()
expr_stmt|;
name|qb
operator|.
name|setAnalyzeRewrite
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|qbp
operator|=
name|qb
operator|.
name|getParseInfo
argument_list|()
expr_stmt|;
name|analyzeRewrite
operator|=
operator|new
name|AnalyzeRewriteContext
argument_list|()
expr_stmt|;
name|analyzeRewrite
operator|.
name|setTableName
argument_list|(
name|tbl
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|analyzeRewrite
operator|.
name|setTblLvl
argument_list|(
name|isTableLevel
argument_list|)
expr_stmt|;
name|analyzeRewrite
operator|.
name|setColName
argument_list|(
name|colNames
argument_list|)
expr_stmt|;
name|analyzeRewrite
operator|.
name|setColType
argument_list|(
name|colType
argument_list|)
expr_stmt|;
name|qbp
operator|.
name|setAnalyzeRewrite
argument_list|(
name|analyzeRewrite
argument_list|)
expr_stmt|;
name|initCtx
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Invoking analyze on rewritten query"
argument_list|)
expr_stmt|;
name|analyzeInternal
argument_list|(
name|rewrittenTree
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|initCtx
argument_list|(
name|origCtx
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Invoking analyze on original query"
argument_list|)
expr_stmt|;
name|analyzeInternal
argument_list|(
name|originalTree
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

