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
name|org
operator|.
name|antlr
operator|.
name|runtime
operator|.
name|tree
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * List of error messages thrown by the parser  **/
end_comment

begin_enum
specifier|public
enum|enum
name|ErrorMsg
block|{
name|GENERIC_ERROR
argument_list|(
literal|"Exception while processing"
argument_list|)
block|,
name|INVALID_TABLE
argument_list|(
literal|"Table not found"
argument_list|)
block|,
name|INVALID_COLUMN
argument_list|(
literal|"Invalid Column Reference"
argument_list|)
block|,
name|INVALID_PARTITION
argument_list|(
literal|"Partition not found"
argument_list|)
block|,
name|AMBIGOUS_COLUMN
argument_list|(
literal|"Ambigous Column Reference"
argument_list|)
block|,
name|AMBIGOUS_TABLE_ALIAS
argument_list|(
literal|"Ambigous Table Alias"
argument_list|)
block|,
name|INVALID_TABLE_ALIAS
argument_list|(
literal|"Invalid Table Alias"
argument_list|)
block|,
name|NO_TABLE_ALIAS
argument_list|(
literal|"No Table Alias"
argument_list|)
block|,
name|INVALID_FUNCTION
argument_list|(
literal|"Invalid Function"
argument_list|)
block|,
name|INVALID_FUNCTION_SIGNATURE
argument_list|(
literal|"Function Argument Type Mismatch"
argument_list|)
block|,
name|INVALID_OPERATOR_SIGNATURE
argument_list|(
literal|"Operator Argument Type Mismatch"
argument_list|)
block|,
name|INVALID_JOIN_CONDITION_1
argument_list|(
literal|"Both Left and Right Aliases Encountered in Join"
argument_list|)
block|,
name|INVALID_JOIN_CONDITION_2
argument_list|(
literal|"Neither Left nor Right Aliases Encountered in Join"
argument_list|)
block|,
name|INVALID_JOIN_CONDITION_3
argument_list|(
literal|"OR not supported in Join currently"
argument_list|)
block|,
name|INVALID_TRANSFORM
argument_list|(
literal|"TRANSFORM with Other Select Columns not Supported"
argument_list|)
block|,
name|DUPLICATE_GROUPBY_KEY
argument_list|(
literal|"Repeated Key in Group By"
argument_list|)
block|,
name|UNSUPPORTED_MULTIPLE_DISTINCTS
argument_list|(
literal|"DISTINCT on Different Columns not Supported"
argument_list|)
block|,
name|NO_SUBQUERY_ALIAS
argument_list|(
literal|"No Alias For Subquery"
argument_list|)
block|,
name|NO_INSERT_INSUBQUERY
argument_list|(
literal|"Cannot insert in a Subquery. Inserting to table "
argument_list|)
block|,
name|NON_KEY_EXPR_IN_GROUPBY
argument_list|(
literal|"Expression Not In Group By Key"
argument_list|)
block|,
name|INVALID_XPATH
argument_list|(
literal|"General . and [] Operators are Not Supported"
argument_list|)
block|,
name|INVALID_PATH
argument_list|(
literal|"Invalid Path"
argument_list|)
block|,
name|ILLEGAL_PATH
argument_list|(
literal|"Path is not legal"
argument_list|)
block|,
name|INVALID_NUMERICAL_CONSTANT
argument_list|(
literal|"Invalid Numerical Constant"
argument_list|)
block|,
name|INVALID_ARRAYINDEX_CONSTANT
argument_list|(
literal|"Non Constant Expressions for Array Indexes not Supported"
argument_list|)
block|,
name|INVALID_MAPINDEX_CONSTANT
argument_list|(
literal|"Non Constant Expression for Map Indexes not Supported"
argument_list|)
block|,
name|INVALID_MAPINDEX_TYPE
argument_list|(
literal|"Map Key Type does not Match Index Expression Type"
argument_list|)
block|,
name|NON_COLLECTION_TYPE
argument_list|(
literal|"[] not Valid on Non Collection Types"
argument_list|)
block|,
name|SELECT_DISTINCT_WITH_GROUPBY
argument_list|(
literal|"SELECT DISTINCT and GROUP BY can not be in the same query"
argument_list|)
block|,
name|COLUMN_REPEATED_IN_PARTITIONING_COLS
argument_list|(
literal|"Column repeated in partitioning columns"
argument_list|)
block|,
name|DUPLICATE_COLUMN_NAMES
argument_list|(
literal|"Duplicate column names"
argument_list|)
block|,
name|COLUMN_REPEATED_IN_CLUSTER_SORT
argument_list|(
literal|"Same column cannot appear in cluster and sort by"
argument_list|)
block|,
name|SAMPLE_RESTRICTION
argument_list|(
literal|"Cannot Sample on More Than Two Columns"
argument_list|)
block|,
name|SAMPLE_COLUMN_NOT_FOUND
argument_list|(
literal|"Sample Column Not Found"
argument_list|)
block|,
name|NO_PARTITION_PREDICATE
argument_list|(
literal|"No Partition Predicate Found"
argument_list|)
block|,
name|INVALID_DOT
argument_list|(
literal|". operator is only supported on struct or list of struct types"
argument_list|)
block|,
name|INVALID_TBL_DDL_SERDE
argument_list|(
literal|"Either list of columns or a custom serializer should be specified"
argument_list|)
block|,
name|TARGET_TABLE_COLUMN_MISMATCH
argument_list|(
literal|"Cannot insert into target table because column number/types are different"
argument_list|)
block|,
name|TABLE_ALIAS_NOT_ALLOWED
argument_list|(
literal|"Table Alias not Allowed in Sampling Clause"
argument_list|)
block|,
name|CLUSTERBY_DISTRIBUTEBY_CONFLICT
argument_list|(
literal|"Cannot have both Cluster By and Distribute By Clauses"
argument_list|)
block|,
name|CLUSTERBY_SORTBY_CONFLICT
argument_list|(
literal|"Cannot have both Cluster By and Sort By Clauses"
argument_list|)
block|,
name|UNION_NOTIN_SUBQ
argument_list|(
literal|"Top level Union is not supported currently; use a subquery for the union"
argument_list|)
block|,
name|NON_BUCKETED_TABLE
argument_list|(
literal|"Sampling Expression Needed for Non-Bucketed Table"
argument_list|)
block|;
specifier|private
name|String
name|mesg
decl_stmt|;
name|ErrorMsg
parameter_list|(
name|String
name|mesg
parameter_list|)
block|{
name|this
operator|.
name|mesg
operator|=
name|mesg
expr_stmt|;
block|}
specifier|private
name|int
name|getLine
parameter_list|(
name|CommonTree
name|tree
parameter_list|)
block|{
if|if
condition|(
name|tree
operator|.
name|getChildCount
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
name|tree
operator|.
name|getToken
argument_list|()
operator|.
name|getLine
argument_list|()
return|;
block|}
return|return
name|getLine
argument_list|(
operator|(
name|CommonTree
operator|)
name|tree
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|int
name|getCharPositionInLine
parameter_list|(
name|CommonTree
name|tree
parameter_list|)
block|{
if|if
condition|(
name|tree
operator|.
name|getChildCount
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
name|tree
operator|.
name|getToken
argument_list|()
operator|.
name|getCharPositionInLine
argument_list|()
return|;
block|}
return|return
name|getCharPositionInLine
argument_list|(
operator|(
name|CommonTree
operator|)
name|tree
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
return|;
block|}
comment|// Dirty hack as this will throw away spaces and other things - find a better way!
specifier|private
name|String
name|getText
parameter_list|(
name|CommonTree
name|tree
parameter_list|)
block|{
if|if
condition|(
name|tree
operator|.
name|getChildCount
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
name|tree
operator|.
name|getText
argument_list|()
return|;
block|}
return|return
name|getText
argument_list|(
operator|(
name|CommonTree
operator|)
name|tree
operator|.
name|getChild
argument_list|(
name|tree
operator|.
name|getChildCount
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
return|;
block|}
name|String
name|getMsg
parameter_list|(
name|CommonTree
name|tree
parameter_list|)
block|{
return|return
literal|"line "
operator|+
name|getLine
argument_list|(
name|tree
argument_list|)
operator|+
literal|":"
operator|+
name|getCharPositionInLine
argument_list|(
name|tree
argument_list|)
operator|+
literal|" "
operator|+
name|mesg
operator|+
literal|" "
operator|+
name|getText
argument_list|(
name|tree
argument_list|)
return|;
block|}
name|String
name|getMsg
parameter_list|(
name|Tree
name|tree
parameter_list|)
block|{
return|return
name|getMsg
argument_list|(
operator|(
name|CommonTree
operator|)
name|tree
argument_list|)
return|;
block|}
name|String
name|getMsg
parameter_list|(
name|CommonTree
name|tree
parameter_list|,
name|String
name|reason
parameter_list|)
block|{
return|return
literal|"line "
operator|+
name|getLine
argument_list|(
name|tree
argument_list|)
operator|+
literal|":"
operator|+
name|getCharPositionInLine
argument_list|(
name|tree
argument_list|)
operator|+
literal|" "
operator|+
name|mesg
operator|+
literal|" "
operator|+
name|getText
argument_list|(
name|tree
argument_list|)
operator|+
literal|": "
operator|+
name|reason
return|;
block|}
name|String
name|getMsg
parameter_list|(
name|Tree
name|tree
parameter_list|,
name|String
name|reason
parameter_list|)
block|{
return|return
name|getMsg
argument_list|(
operator|(
name|CommonTree
operator|)
name|tree
argument_list|,
name|reason
argument_list|)
return|;
block|}
name|String
name|getMsg
parameter_list|()
block|{
return|return
name|mesg
return|;
block|}
block|}
end_enum

end_unit

