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
package|;
end_package

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
name|Matcher
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
name|org
operator|.
name|antlr
operator|.
name|runtime
operator|.
name|tree
operator|.
name|Tree
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
name|HiveUtils
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
name|ASTNode
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
name|ASTNodeOrigin
import|;
end_import

begin_comment
comment|/**  * List of all error messages.  * This list contains both compile time and run-time errors.  **/
end_comment

begin_enum
specifier|public
enum|enum
name|ErrorMsg
block|{
comment|// The error codes are Hive-specific and partitioned into the following ranges:
comment|// 10000 to 19999: Errors occuring during semantic analysis and compilation of the query.
comment|// 20000 to 29999: Runtime errors where Hive believes that retries are unlikely to succeed.
comment|// 30000 to 39999: Runtime errors which Hive thinks may be transient and retrying may succeed.
comment|// 40000 to 49999: Errors where Hive is unable to advise about retries.
comment|// In addition to the error code, ErrorMsg also has a SQLState field.
comment|// SQLStates are taken from Section 12.5 of ISO-9075.
comment|// See http://www.contrib.andrew.cmu.edu/~shadow/sql/sql1992.txt
comment|// Most will just rollup to the generic syntax error state of 42000, but
comment|// specific errors can override the that state.
comment|// See this page for how MySQL uses SQLState codes:
comment|// http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-error-sqlstates.html
name|GENERIC_ERROR
argument_list|(
literal|40000
argument_list|,
literal|"Exception while processing"
argument_list|)
block|,
name|INVALID_TABLE
argument_list|(
literal|10001
argument_list|,
literal|"Table not found"
argument_list|,
literal|"42S02"
argument_list|)
block|,
name|INVALID_COLUMN
argument_list|(
literal|10002
argument_list|,
literal|"Invalid column reference"
argument_list|)
block|,
name|INVALID_INDEX
argument_list|(
literal|10003
argument_list|,
literal|"Invalid index"
argument_list|)
block|,
name|INVALID_TABLE_OR_COLUMN
argument_list|(
literal|10004
argument_list|,
literal|"Invalid table alias or column reference"
argument_list|)
block|,
name|AMBIGUOUS_TABLE_OR_COLUMN
argument_list|(
literal|10005
argument_list|,
literal|"Ambiguous table alias or column reference"
argument_list|)
block|,
name|INVALID_PARTITION
argument_list|(
literal|10006
argument_list|,
literal|"Partition not found"
argument_list|)
block|,
name|AMBIGUOUS_COLUMN
argument_list|(
literal|10007
argument_list|,
literal|"Ambiguous column reference"
argument_list|)
block|,
name|AMBIGUOUS_TABLE_ALIAS
argument_list|(
literal|10008
argument_list|,
literal|"Ambiguous table alias"
argument_list|)
block|,
name|INVALID_TABLE_ALIAS
argument_list|(
literal|10009
argument_list|,
literal|"Invalid table alias"
argument_list|)
block|,
name|NO_TABLE_ALIAS
argument_list|(
literal|10010
argument_list|,
literal|"No table alias"
argument_list|)
block|,
name|INVALID_FUNCTION
argument_list|(
literal|10011
argument_list|,
literal|"Invalid function"
argument_list|)
block|,
name|INVALID_FUNCTION_SIGNATURE
argument_list|(
literal|10012
argument_list|,
literal|"Function argument type mismatch"
argument_list|)
block|,
name|INVALID_OPERATOR_SIGNATURE
argument_list|(
literal|10013
argument_list|,
literal|"Operator argument type mismatch"
argument_list|)
block|,
name|INVALID_ARGUMENT
argument_list|(
literal|10014
argument_list|,
literal|"Wrong arguments"
argument_list|)
block|,
name|INVALID_ARGUMENT_LENGTH
argument_list|(
literal|10015
argument_list|,
literal|"Arguments length mismatch"
argument_list|,
literal|"21000"
argument_list|)
block|,
name|INVALID_ARGUMENT_TYPE
argument_list|(
literal|10016
argument_list|,
literal|"Argument type mismatch"
argument_list|)
block|,
name|INVALID_JOIN_CONDITION_1
argument_list|(
literal|10017
argument_list|,
literal|"Both left and right aliases encountered in JOIN"
argument_list|)
block|,
name|INVALID_JOIN_CONDITION_2
argument_list|(
literal|10018
argument_list|,
literal|"Neither left nor right aliases encountered in JOIN"
argument_list|)
block|,
name|INVALID_JOIN_CONDITION_3
argument_list|(
literal|10019
argument_list|,
literal|"OR not supported in JOIN currently"
argument_list|)
block|,
name|INVALID_TRANSFORM
argument_list|(
literal|10020
argument_list|,
literal|"TRANSFORM with other SELECT columns not supported"
argument_list|)
block|,
name|DUPLICATE_GROUPBY_KEY
argument_list|(
literal|10021
argument_list|,
literal|"Repeated key in GROUP BY"
argument_list|)
block|,
name|UNSUPPORTED_MULTIPLE_DISTINCTS
argument_list|(
literal|10022
argument_list|,
literal|"DISTINCT on different columns not supported"
operator|+
literal|" with skew in data"
argument_list|)
block|,
name|NO_SUBQUERY_ALIAS
argument_list|(
literal|10023
argument_list|,
literal|"No alias for subquery"
argument_list|)
block|,
name|NO_INSERT_INSUBQUERY
argument_list|(
literal|10024
argument_list|,
literal|"Cannot insert in a subquery. Inserting to table "
argument_list|)
block|,
name|NON_KEY_EXPR_IN_GROUPBY
argument_list|(
literal|10025
argument_list|,
literal|"Expression not in GROUP BY key"
argument_list|)
block|,
name|INVALID_XPATH
argument_list|(
literal|10026
argument_list|,
literal|"General . and [] operators are not supported"
argument_list|)
block|,
name|INVALID_PATH
argument_list|(
literal|10027
argument_list|,
literal|"Invalid path"
argument_list|)
block|,
name|ILLEGAL_PATH
argument_list|(
literal|10028
argument_list|,
literal|"Path is not legal"
argument_list|)
block|,
name|INVALID_NUMERICAL_CONSTANT
argument_list|(
literal|10029
argument_list|,
literal|"Invalid numerical constant"
argument_list|)
block|,
name|INVALID_ARRAYINDEX_CONSTANT
argument_list|(
literal|10030
argument_list|,
literal|"Non-constant expressions for array indexes not supported"
argument_list|)
block|,
name|INVALID_MAPINDEX_CONSTANT
argument_list|(
literal|10031
argument_list|,
literal|"Non-constant expression for map indexes not supported"
argument_list|)
block|,
name|INVALID_MAPINDEX_TYPE
argument_list|(
literal|10032
argument_list|,
literal|"MAP key type does not match index expression type"
argument_list|)
block|,
name|NON_COLLECTION_TYPE
argument_list|(
literal|10033
argument_list|,
literal|"[] not valid on non-collection types"
argument_list|)
block|,
name|SELECT_DISTINCT_WITH_GROUPBY
argument_list|(
literal|10034
argument_list|,
literal|"SELECT DISTINCT and GROUP BY can not be in the same query"
argument_list|)
block|,
name|COLUMN_REPEATED_IN_PARTITIONING_COLS
argument_list|(
literal|10035
argument_list|,
literal|"Column repeated in partitioning columns"
argument_list|)
block|,
name|DUPLICATE_COLUMN_NAMES
argument_list|(
literal|10036
argument_list|,
literal|"Duplicate column name:"
argument_list|)
block|,
name|INVALID_BUCKET_NUMBER
argument_list|(
literal|10037
argument_list|,
literal|"Bucket number should be bigger than zero"
argument_list|)
block|,
name|COLUMN_REPEATED_IN_CLUSTER_SORT
argument_list|(
literal|10038
argument_list|,
literal|"Same column cannot appear in CLUSTER BY and SORT BY"
argument_list|)
block|,
name|SAMPLE_RESTRICTION
argument_list|(
literal|10039
argument_list|,
literal|"Cannot SAMPLE on more than two columns"
argument_list|)
block|,
name|SAMPLE_COLUMN_NOT_FOUND
argument_list|(
literal|10040
argument_list|,
literal|"SAMPLE column not found"
argument_list|)
block|,
name|NO_PARTITION_PREDICATE
argument_list|(
literal|10041
argument_list|,
literal|"No partition predicate found"
argument_list|)
block|,
name|INVALID_DOT
argument_list|(
literal|10042
argument_list|,
literal|". Operator is only supported on struct or list of struct types"
argument_list|)
block|,
name|INVALID_TBL_DDL_SERDE
argument_list|(
literal|10043
argument_list|,
literal|"Either list of columns or a custom serializer should be specified"
argument_list|)
block|,
name|TARGET_TABLE_COLUMN_MISMATCH
argument_list|(
literal|10044
argument_list|,
literal|"Cannot insert into target table because column number/types are different"
argument_list|)
block|,
name|TABLE_ALIAS_NOT_ALLOWED
argument_list|(
literal|10045
argument_list|,
literal|"Table alias not allowed in sampling clause"
argument_list|)
block|,
name|CLUSTERBY_DISTRIBUTEBY_CONFLICT
argument_list|(
literal|10046
argument_list|,
literal|"Cannot have both CLUSTER BY and DISTRIBUTE BY clauses"
argument_list|)
block|,
name|ORDERBY_DISTRIBUTEBY_CONFLICT
argument_list|(
literal|10047
argument_list|,
literal|"Cannot have both ORDER BY and DISTRIBUTE BY clauses"
argument_list|)
block|,
name|CLUSTERBY_SORTBY_CONFLICT
argument_list|(
literal|10048
argument_list|,
literal|"Cannot have both CLUSTER BY and SORT BY clauses"
argument_list|)
block|,
name|ORDERBY_SORTBY_CONFLICT
argument_list|(
literal|10049
argument_list|,
literal|"Cannot have both ORDER BY and SORT BY clauses"
argument_list|)
block|,
name|CLUSTERBY_ORDERBY_CONFLICT
argument_list|(
literal|10050
argument_list|,
literal|"Cannot have both CLUSTER BY and ORDER BY clauses"
argument_list|)
block|,
name|NO_LIMIT_WITH_ORDERBY
argument_list|(
literal|10051
argument_list|,
literal|"In strict mode, if ORDER BY is specified, "
operator|+
literal|"LIMIT must also be specified"
argument_list|)
block|,
name|NO_CARTESIAN_PRODUCT
argument_list|(
literal|10052
argument_list|,
literal|"In strict mode, cartesian product is not allowed. "
operator|+
literal|"If you really want to perform the operation, set hive.mapred.mode=nonstrict"
argument_list|)
block|,
name|UNION_NOTIN_SUBQ
argument_list|(
literal|10053
argument_list|,
literal|"Top level UNION is not supported currently; "
operator|+
literal|"use a subquery for the UNION"
argument_list|)
block|,
name|INVALID_INPUT_FORMAT_TYPE
argument_list|(
literal|10054
argument_list|,
literal|"Input format must implement InputFormat"
argument_list|)
block|,
name|INVALID_OUTPUT_FORMAT_TYPE
argument_list|(
literal|10055
argument_list|,
literal|"Output Format must implement HiveOutputFormat, "
operator|+
literal|"otherwise it should be either IgnoreKeyTextOutputFormat or SequenceFileOutputFormat"
argument_list|)
block|,
name|NO_VALID_PARTN
argument_list|(
literal|10056
argument_list|,
literal|"The query does not reference any valid partition. "
operator|+
literal|"To run this query, set hive.mapred.mode=nonstrict"
argument_list|)
block|,
name|NO_OUTER_MAPJOIN
argument_list|(
literal|10057
argument_list|,
literal|"MAPJOIN cannot be performed with OUTER JOIN"
argument_list|)
block|,
name|INVALID_MAPJOIN_HINT
argument_list|(
literal|10058
argument_list|,
literal|"Neither table specified as map-table"
argument_list|)
block|,
name|INVALID_MAPJOIN_TABLE
argument_list|(
literal|10059
argument_list|,
literal|"Result of a union cannot be a map table"
argument_list|)
block|,
name|NON_BUCKETED_TABLE
argument_list|(
literal|10060
argument_list|,
literal|"Sampling expression needed for non-bucketed table"
argument_list|)
block|,
name|BUCKETED_NUMERATOR_BIGGER_DENOMINATOR
argument_list|(
literal|10061
argument_list|,
literal|"Numerator should not be bigger than "
operator|+
literal|"denominator in sample clause for table"
argument_list|)
block|,
name|NEED_PARTITION_ERROR
argument_list|(
literal|10062
argument_list|,
literal|"Need to specify partition columns because the destination "
operator|+
literal|"table is partitioned"
argument_list|)
block|,
name|CTAS_CTLT_COEXISTENCE
argument_list|(
literal|10063
argument_list|,
literal|"Create table command does not allow LIKE and AS-SELECT in "
operator|+
literal|"the same command"
argument_list|)
block|,
name|LINES_TERMINATED_BY_NON_NEWLINE
argument_list|(
literal|10064
argument_list|,
literal|"LINES TERMINATED BY only supports "
operator|+
literal|"newline '\\n' right now"
argument_list|)
block|,
name|CTAS_COLLST_COEXISTENCE
argument_list|(
literal|10065
argument_list|,
literal|"CREATE TABLE AS SELECT command cannot specify "
operator|+
literal|"the list of columns "
operator|+
literal|"for the target table"
argument_list|)
block|,
name|CTLT_COLLST_COEXISTENCE
argument_list|(
literal|10066
argument_list|,
literal|"CREATE TABLE LIKE command cannot specify the list of columns for "
operator|+
literal|"the target table"
argument_list|)
block|,
name|INVALID_SELECT_SCHEMA
argument_list|(
literal|10067
argument_list|,
literal|"Cannot derive schema from the select-clause"
argument_list|)
block|,
name|CTAS_PARCOL_COEXISTENCE
argument_list|(
literal|10068
argument_list|,
literal|"CREATE-TABLE-AS-SELECT does not support "
operator|+
literal|"partitioning in the target table "
argument_list|)
block|,
name|CTAS_MULTI_LOADFILE
argument_list|(
literal|10069
argument_list|,
literal|"CREATE-TABLE-AS-SELECT results in multiple file load"
argument_list|)
block|,
name|CTAS_EXTTBL_COEXISTENCE
argument_list|(
literal|10070
argument_list|,
literal|"CREATE-TABLE-AS-SELECT cannot create external table"
argument_list|)
block|,
name|INSERT_EXTERNAL_TABLE
argument_list|(
literal|10071
argument_list|,
literal|"Inserting into a external table is not allowed"
argument_list|)
block|,
name|DATABASE_NOT_EXISTS
argument_list|(
literal|10072
argument_list|,
literal|"Database does not exist:"
argument_list|)
block|,
name|TABLE_ALREADY_EXISTS
argument_list|(
literal|10073
argument_list|,
literal|"Table already exists:"
argument_list|,
literal|"42S02"
argument_list|)
block|,
name|COLUMN_ALIAS_ALREADY_EXISTS
argument_list|(
literal|10074
argument_list|,
literal|"Column alias already exists:"
argument_list|,
literal|"42S02"
argument_list|)
block|,
name|UDTF_MULTIPLE_EXPR
argument_list|(
literal|10075
argument_list|,
literal|"Only a single expression in the SELECT clause is "
operator|+
literal|"supported with UDTF's"
argument_list|)
block|,
name|UDTF_REQUIRE_AS
argument_list|(
literal|10076
argument_list|,
literal|"UDTF's require an AS clause"
argument_list|)
block|,
name|UDTF_NO_GROUP_BY
argument_list|(
literal|10077
argument_list|,
literal|"GROUP BY is not supported with a UDTF in the SELECT clause"
argument_list|)
block|,
name|UDTF_NO_SORT_BY
argument_list|(
literal|10078
argument_list|,
literal|"SORT BY is not supported with a UDTF in the SELECT clause"
argument_list|)
block|,
name|UDTF_NO_CLUSTER_BY
argument_list|(
literal|10079
argument_list|,
literal|"CLUSTER BY is not supported with a UDTF in the SELECT clause"
argument_list|)
block|,
name|UDTF_NO_DISTRIBUTE_BY
argument_list|(
literal|10080
argument_list|,
literal|"DISTRUBTE BY is not supported with a UDTF in the SELECT clause"
argument_list|)
block|,
name|UDTF_INVALID_LOCATION
argument_list|(
literal|10081
argument_list|,
literal|"UDTF's are not supported outside the SELECT clause, nor nested "
operator|+
literal|"in expressions"
argument_list|)
block|,
name|UDTF_LATERAL_VIEW
argument_list|(
literal|10082
argument_list|,
literal|"UDTF's cannot be in a select expression when there is a lateral view"
argument_list|)
block|,
name|UDTF_ALIAS_MISMATCH
argument_list|(
literal|10083
argument_list|,
literal|"The number of aliases supplied in the AS clause does not match the "
operator|+
literal|"number of columns output by the UDTF"
argument_list|)
block|,
name|UDF_STATEFUL_INVALID_LOCATION
argument_list|(
literal|10084
argument_list|,
literal|"Stateful UDF's can only be invoked in the SELECT list"
argument_list|)
block|,
name|LATERAL_VIEW_WITH_JOIN
argument_list|(
literal|10085
argument_list|,
literal|"JOIN with a LATERAL VIEW is not supported"
argument_list|)
block|,
name|LATERAL_VIEW_INVALID_CHILD
argument_list|(
literal|10086
argument_list|,
literal|"LATERAL VIEW AST with invalid child"
argument_list|)
block|,
name|OUTPUT_SPECIFIED_MULTIPLE_TIMES
argument_list|(
literal|10087
argument_list|,
literal|"The same output cannot be present multiple times: "
argument_list|)
block|,
name|INVALID_AS
argument_list|(
literal|10088
argument_list|,
literal|"AS clause has an invalid number of aliases"
argument_list|)
block|,
name|VIEW_COL_MISMATCH
argument_list|(
literal|10089
argument_list|,
literal|"The number of columns produced by the SELECT clause does not match the "
operator|+
literal|"number of column names specified by CREATE VIEW"
argument_list|)
block|,
name|DML_AGAINST_VIEW
argument_list|(
literal|10090
argument_list|,
literal|"A view cannot be used as target table for LOAD or INSERT"
argument_list|)
block|,
name|ANALYZE_VIEW
argument_list|(
literal|10091
argument_list|,
literal|"ANALYZE is not supported for views"
argument_list|)
block|,
name|VIEW_PARTITION_TOTAL
argument_list|(
literal|10092
argument_list|,
literal|"At least one non-partitioning column must be present in view"
argument_list|)
block|,
name|VIEW_PARTITION_MISMATCH
argument_list|(
literal|10093
argument_list|,
literal|"Rightmost columns in view output do not match "
operator|+
literal|"PARTITIONED ON clause"
argument_list|)
block|,
name|PARTITION_DYN_STA_ORDER
argument_list|(
literal|10094
argument_list|,
literal|"Dynamic partition cannot be the parent of a static partition"
argument_list|)
block|,
name|DYNAMIC_PARTITION_DISABLED
argument_list|(
literal|10095
argument_list|,
literal|"Dynamic partition is disabled. Either enable it by setting "
operator|+
literal|"hive.exec.dynamic.partition=true or specify partition column values"
argument_list|)
block|,
name|DYNAMIC_PARTITION_STRICT_MODE
argument_list|(
literal|10096
argument_list|,
literal|"Dynamic partition strict mode requires at least one "
operator|+
literal|"static partition column. To turn this off set hive.exec.dynamic.partition.mode=nonstrict"
argument_list|)
block|,
name|DYNAMIC_PARTITION_MERGE
argument_list|(
literal|10097
argument_list|,
literal|"Dynamic partition does not support merging using "
operator|+
literal|"non-CombineHiveInputFormat. Please check your hive.input.format setting and "
operator|+
literal|"make sure your Hadoop version support CombineFileInputFormat"
argument_list|)
block|,
name|NONEXISTPARTCOL
argument_list|(
literal|10098
argument_list|,
literal|"Non-Partition column appears in the partition specification: "
argument_list|)
block|,
name|UNSUPPORTED_TYPE
argument_list|(
literal|10099
argument_list|,
literal|"DATE and DATETIME types aren't supported yet. Please use "
operator|+
literal|"TIMESTAMP instead"
argument_list|)
block|,
name|CREATE_NON_NATIVE_AS
argument_list|(
literal|10100
argument_list|,
literal|"CREATE TABLE AS SELECT cannot be used for a non-native table"
argument_list|)
block|,
name|LOAD_INTO_NON_NATIVE
argument_list|(
literal|10101
argument_list|,
literal|"A non-native table cannot be used as target for LOAD"
argument_list|)
block|,
name|LOCKMGR_NOT_SPECIFIED
argument_list|(
literal|10102
argument_list|,
literal|"Lock manager not specified correctly, set hive.lock.manager"
argument_list|)
block|,
name|LOCKMGR_NOT_INITIALIZED
argument_list|(
literal|10103
argument_list|,
literal|"Lock manager could not be initialized, check hive.lock.manager "
argument_list|)
block|,
name|LOCK_CANNOT_BE_ACQUIRED
argument_list|(
literal|10104
argument_list|,
literal|"Locks on the underlying objects cannot be acquired. "
operator|+
literal|"retry after some time"
argument_list|)
block|,
name|ZOOKEEPER_CLIENT_COULD_NOT_BE_INITIALIZED
argument_list|(
literal|10105
argument_list|,
literal|"Check hive.zookeeper.quorum "
operator|+
literal|"and hive.zookeeper.client.port"
argument_list|)
block|,
name|OVERWRITE_ARCHIVED_PART
argument_list|(
literal|10106
argument_list|,
literal|"Cannot overwrite an archived partition. "
operator|+
literal|"Unarchive before running this command"
argument_list|)
block|,
name|ARCHIVE_METHODS_DISABLED
argument_list|(
literal|10107
argument_list|,
literal|"Archiving methods are currently disabled. "
operator|+
literal|"Please see the Hive wiki for more information about enabling archiving"
argument_list|)
block|,
name|ARCHIVE_ON_MULI_PARTS
argument_list|(
literal|10108
argument_list|,
literal|"ARCHIVE can only be run on a single partition"
argument_list|)
block|,
name|UNARCHIVE_ON_MULI_PARTS
argument_list|(
literal|10109
argument_list|,
literal|"ARCHIVE can only be run on a single partition"
argument_list|)
block|,
name|ARCHIVE_ON_TABLE
argument_list|(
literal|10110
argument_list|,
literal|"ARCHIVE can only be run on partitions"
argument_list|)
block|,
name|RESERVED_PART_VAL
argument_list|(
literal|10111
argument_list|,
literal|"Partition value contains a reserved substring"
argument_list|)
block|,
name|HOLD_DDLTIME_ON_NONEXIST_PARTITIONS
argument_list|(
literal|10112
argument_list|,
literal|"HOLD_DDLTIME hint cannot be applied to dynamic "
operator|+
literal|"partitions or non-existent partitions"
argument_list|)
block|,
name|OFFLINE_TABLE_OR_PARTITION
argument_list|(
literal|10113
argument_list|,
literal|"Query against an offline table or partition"
argument_list|)
block|,
name|OUTERJOIN_USES_FILTERS
argument_list|(
literal|10114
argument_list|,
literal|"The query results could be wrong. "
operator|+
literal|"Turn on hive.outerjoin.supports.filters"
argument_list|)
block|,
name|NEED_PARTITION_SPECIFICATION
argument_list|(
literal|10115
argument_list|,
literal|"Table is partitioned and partition specification is needed"
argument_list|)
block|,
name|INVALID_METADATA
argument_list|(
literal|10116
argument_list|,
literal|"The metadata file could not be parsed "
argument_list|)
block|,
name|NEED_TABLE_SPECIFICATION
argument_list|(
literal|10117
argument_list|,
literal|"Table name could be determined; It should be specified "
argument_list|)
block|,
name|PARTITION_EXISTS
argument_list|(
literal|10118
argument_list|,
literal|"Partition already exists"
argument_list|)
block|,
name|TABLE_DATA_EXISTS
argument_list|(
literal|10119
argument_list|,
literal|"Table exists and contains data files"
argument_list|)
block|,
name|INCOMPATIBLE_SCHEMA
argument_list|(
literal|10120
argument_list|,
literal|"The existing table is not compatible with the import spec. "
argument_list|)
block|,
name|EXIM_FOR_NON_NATIVE
argument_list|(
literal|10121
argument_list|,
literal|"Export/Import cannot be done for a non-native table. "
argument_list|)
block|,
name|INSERT_INTO_BUCKETIZED_TABLE
argument_list|(
literal|10122
argument_list|,
literal|"Bucketized tables do not support INSERT INTO:"
argument_list|)
block|,
name|NO_COMPARE_BIGINT_STRING
argument_list|(
literal|10123
argument_list|,
literal|"In strict mode, comparing bigints and strings is not allowed, "
operator|+
literal|"it may result in a loss of precision. "
operator|+
literal|"If you really want to perform the operation, set hive.mapred.mode=nonstrict"
argument_list|)
block|,
name|NO_COMPARE_BIGINT_DOUBLE
argument_list|(
literal|10124
argument_list|,
literal|"In strict mode, comparing bigints and doubles is not allowed, "
operator|+
literal|"it may result in a loss of precision. "
operator|+
literal|"If you really want to perform the operation, set hive.mapred.mode=nonstrict"
argument_list|)
block|,
name|PARTSPEC_DIFFER_FROM_SCHEMA
argument_list|(
literal|10125
argument_list|,
literal|"Partition columns in partition specification are "
operator|+
literal|"not the same as that defined in the table schema. "
operator|+
literal|"The names and orders have to be exactly the same."
argument_list|)
block|,
name|PARTITION_COLUMN_NON_PRIMITIVE
argument_list|(
literal|10126
argument_list|,
literal|"Partition column must be of primitive type."
argument_list|)
block|,
name|INSERT_INTO_DYNAMICPARTITION_IFNOTEXISTS
argument_list|(
literal|10127
argument_list|,
literal|"Dynamic partitions do not support IF NOT EXISTS. Specified partitions with value :"
argument_list|)
block|,
name|UDAF_INVALID_LOCATION
argument_list|(
literal|10128
argument_list|,
literal|"Not yet supported place for UDAF"
argument_list|)
block|,
name|DROP_PARTITION_NON_STRING_PARTCOLS_NONEQUALITY
argument_list|(
literal|10129
argument_list|,
literal|"Drop partitions for a non string partition columns is not allowed using non-equality"
argument_list|)
block|,
name|ALTER_COMMAND_FOR_VIEWS
argument_list|(
literal|10131
argument_list|,
literal|"To alter a view you need to use the ALTER VIEW command."
argument_list|)
block|,
name|ALTER_COMMAND_FOR_TABLES
argument_list|(
literal|10132
argument_list|,
literal|"To alter a base table you need to use the ALTER TABLE command."
argument_list|)
block|,
name|ALTER_VIEW_DISALLOWED_OP
argument_list|(
literal|10133
argument_list|,
literal|"Cannot use this form of ALTER on a view"
argument_list|)
block|,
name|ALTER_TABLE_NON_NATIVE
argument_list|(
literal|10134
argument_list|,
literal|"ALTER TABLE cannot be used for a non-native table"
argument_list|)
block|,
name|SORTMERGE_MAPJOIN_FAILED
argument_list|(
literal|10135
argument_list|,
literal|"Sort merge bucketed join could not be performed. "
operator|+
literal|"If you really want to perform the operation, either set "
operator|+
literal|"hive.optimize.bucketmapjoin.sortedmerge=false, or set "
operator|+
literal|"hive.enforce.sortmergebucketmapjoin=false."
argument_list|)
block|,
name|BUCKET_MAPJOIN_NOT_POSSIBLE
argument_list|(
literal|10136
argument_list|,
literal|"Bucketed mapjoin cannot be performed. "
operator|+
literal|"This can be due to multiple reasons: "
operator|+
literal|" . Join columns dont match bucketed columns. "
operator|+
literal|" . Number of buckets are not a multiple of each other. "
operator|+
literal|"If you really want to perform the operation, either remove the "
operator|+
literal|"mapjoin hint from your query or set hive.enforce.bucketmapjoin to false."
argument_list|)
block|,
name|EXPRESSIONS_NOT_ALLOWED_CLUSTERBY
argument_list|(
literal|10137
argument_list|,
literal|"Expressions are not allowed in a cluster by clause. Use a column alias instead"
argument_list|)
block|,
name|EXPRESSIONS_NOT_ALLOWED_DISTRIBUTEBY
argument_list|(
literal|10138
argument_list|,
literal|"Expressions are not allowed in a distribute by clause. Use a column alias instead"
argument_list|)
block|,
name|EXPRESSIONS_NOT_ALLOWED_ORDERBY
argument_list|(
literal|10139
argument_list|,
literal|"Expressions are not allowed in an order by clause. Use a column alias instead"
argument_list|)
block|,
name|EXPRESSIONS_NOT_ALLOWED_SORTBY
argument_list|(
literal|10140
argument_list|,
literal|"Expressions are not allowed in a sort by clause. Use a column alias instead"
argument_list|)
block|,
name|BUCKETED_TABLE_METADATA_INCORRECT
argument_list|(
literal|10141
argument_list|,
literal|"Bucketed table metadata is not correct. "
operator|+
literal|"Fix the metadata or don't use bucketed mapjoin, by setting "
operator|+
literal|"hive.enforce.bucketmapjoin to false."
argument_list|)
block|,
name|JOINNODE_OUTERJOIN_MORETHAN_8
argument_list|(
literal|10142
argument_list|,
literal|"Single join node containing outer join(s) "
operator|+
literal|"cannot have more than 8 aliases"
argument_list|)
block|,
name|CREATE_SKEWED_TABLE_NO_COLUMN_NAME
argument_list|(
literal|10200
argument_list|,
literal|"No skewed column name."
argument_list|)
block|,
name|CREATE_SKEWED_TABLE_NO_COLUMN_VALUE
argument_list|(
literal|10201
argument_list|,
literal|"No skewed values."
argument_list|)
block|,
name|CREATE_SKEWED_TABLE_DUPLICATE_COLUMN_NAMES
argument_list|(
literal|10202
argument_list|,
literal|"Duplicate skewed column name:"
argument_list|)
block|,
name|CREATE_SKEWED_TABLE_INVALID_COLUMN
argument_list|(
literal|10203
argument_list|,
literal|"Invalid skewed column name:"
argument_list|)
block|,
name|CREATE_SKEWED_TABLE_SKEWED_COL_NAME_VALUE_MISMATCH_1
argument_list|(
literal|10204
argument_list|,
literal|"Skewed column name is empty but skewed value is not."
argument_list|)
block|,
name|CREATE_SKEWED_TABLE_SKEWED_COL_NAME_VALUE_MISMATCH_2
argument_list|(
literal|10205
argument_list|,
literal|"Skewed column value is empty but skewed name is not."
argument_list|)
block|,
name|CREATE_SKEWED_TABLE_SKEWED_COL_NAME_VALUE_MISMATCH_3
argument_list|(
literal|10206
argument_list|,
literal|"The number of skewed column names and the number of "
operator|+
literal|"skewed column values are different: "
argument_list|)
block|,
name|ALTER_TABLE_NOT_ALLOWED_RENAME_SKEWED_COLUMN
argument_list|(
literal|10207
argument_list|,
literal|" is a skewed column. It's not allowed to rename skewed column."
argument_list|)
block|,
name|HIVE_INTERNAL_DDL_LIST_BUCKETING_DISABLED
argument_list|(
literal|10208
argument_list|,
literal|"List Bucketing DDL is not allowed to use since feature is not completed yet."
argument_list|)
block|,
name|SCRIPT_INIT_ERROR
argument_list|(
literal|20000
argument_list|,
literal|"Unable to initialize custom script."
argument_list|)
block|,
name|SCRIPT_IO_ERROR
argument_list|(
literal|20001
argument_list|,
literal|"An error occurred while reading or writing to your custom script. "
operator|+
literal|"It may have crashed with an error."
argument_list|)
block|,
name|SCRIPT_GENERIC_ERROR
argument_list|(
literal|20002
argument_list|,
literal|"Hive encountered some unknown error while "
operator|+
literal|"running your custom script."
argument_list|)
block|,
name|SCRIPT_CLOSING_ERROR
argument_list|(
literal|20003
argument_list|,
literal|"An error occurred when trying to close the Operator "
operator|+
literal|"running your custom script."
argument_list|)
block|,
name|STATSPUBLISHER_NOT_OBTAINED
argument_list|(
literal|30000
argument_list|,
literal|"StatsPublisher cannot be obtained. "
operator|+
literal|"There was a error to retrieve the StatsPublisher, and retrying "
operator|+
literal|"might help. If you dont want the query to fail because accurate statistics "
operator|+
literal|"could not be collected, set hive.stats.reliable=false"
argument_list|)
block|,
name|STATSPUBLISHER_INITIALIZATION_ERROR
argument_list|(
literal|30001
argument_list|,
literal|"StatsPublisher cannot be initialized. "
operator|+
literal|"There was a error in the initialization of StatsPublisher, and retrying "
operator|+
literal|"might help. If you dont want the query to fail because accurate statistics "
operator|+
literal|"could not be collected, set hive.stats.reliable=false"
argument_list|)
block|,
name|STATSPUBLISHER_CONNECTION_ERROR
argument_list|(
literal|30002
argument_list|,
literal|"StatsPublisher cannot be connected to."
operator|+
literal|"There was a error while connecting to the StatsPublisher, and retrying "
operator|+
literal|"might help. If you dont want the query to fail because accurate statistics "
operator|+
literal|"could not be collected, set hive.stats.reliable=false"
argument_list|)
block|,
name|STATSPUBLISHER_PUBLISHING_ERROR
argument_list|(
literal|30003
argument_list|,
literal|"Error in publishing stats. There was an "
operator|+
literal|"error in publishing stats via StatsPublisher, and retrying "
operator|+
literal|"might help. If you dont want the query to fail because accurate statistics "
operator|+
literal|"could not be collected, set hive.stats.reliable=false"
argument_list|)
block|,
name|STATSPUBLISHER_CLOSING_ERROR
argument_list|(
literal|30004
argument_list|,
literal|"StatsPublisher cannot be closed."
operator|+
literal|"There was a error while closing the StatsPublisher, and retrying "
operator|+
literal|"might help. If you dont want the query to fail because accurate statistics "
operator|+
literal|"could not be collected, set hive.stats.reliable=false"
argument_list|)
block|,      ;
specifier|private
name|int
name|errorCode
decl_stmt|;
specifier|private
name|String
name|mesg
decl_stmt|;
specifier|private
name|String
name|sqlState
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|char
name|SPACE
init|=
literal|' '
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Pattern
name|ERROR_MESSAGE_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|".*Line [0-9]+:[0-9]+ (.*)"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Pattern
name|ERROR_CODE_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"HiveException:\\s+\\[Error ([0-9]+)\\]: (.*)"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|ErrorMsg
argument_list|>
name|mesgToErrorMsgMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|ErrorMsg
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|int
name|minMesgLength
init|=
operator|-
literal|1
decl_stmt|;
static|static
block|{
for|for
control|(
name|ErrorMsg
name|errorMsg
range|:
name|values
argument_list|()
control|)
block|{
name|mesgToErrorMsgMap
operator|.
name|put
argument_list|(
name|errorMsg
operator|.
name|getMsg
argument_list|()
operator|.
name|trim
argument_list|()
argument_list|,
name|errorMsg
argument_list|)
expr_stmt|;
name|int
name|length
init|=
name|errorMsg
operator|.
name|getMsg
argument_list|()
operator|.
name|trim
argument_list|()
operator|.
name|length
argument_list|()
decl_stmt|;
if|if
condition|(
name|minMesgLength
operator|==
operator|-
literal|1
operator|||
name|length
operator|<
name|minMesgLength
condition|)
block|{
name|minMesgLength
operator|=
name|length
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Given an error message string, returns the ErrorMsg object associated with it.    * @param mesg An error message string    * @return ErrorMsg    */
specifier|public
specifier|static
name|ErrorMsg
name|getErrorMsg
parameter_list|(
name|String
name|mesg
parameter_list|)
block|{
if|if
condition|(
name|mesg
operator|==
literal|null
condition|)
block|{
return|return
name|GENERIC_ERROR
return|;
block|}
comment|// first see if there is a direct match
name|ErrorMsg
name|errorMsg
init|=
name|mesgToErrorMsgMap
operator|.
name|get
argument_list|(
name|mesg
argument_list|)
decl_stmt|;
if|if
condition|(
name|errorMsg
operator|!=
literal|null
condition|)
block|{
return|return
name|errorMsg
return|;
block|}
comment|// if not see if the mesg follows type of format, which is typically the
comment|// case:
comment|// line 1:14 Table not found table_name
name|String
name|truncatedMesg
init|=
name|mesg
operator|.
name|trim
argument_list|()
decl_stmt|;
name|Matcher
name|match
init|=
name|ERROR_MESSAGE_PATTERN
operator|.
name|matcher
argument_list|(
name|mesg
argument_list|)
decl_stmt|;
if|if
condition|(
name|match
operator|.
name|matches
argument_list|()
condition|)
block|{
name|truncatedMesg
operator|=
name|match
operator|.
name|group
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// appends might exist after the root message, so strip tokens off until we
comment|// match
while|while
condition|(
name|truncatedMesg
operator|.
name|length
argument_list|()
operator|>
name|minMesgLength
condition|)
block|{
name|errorMsg
operator|=
name|mesgToErrorMsgMap
operator|.
name|get
argument_list|(
name|truncatedMesg
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|errorMsg
operator|!=
literal|null
condition|)
block|{
return|return
name|errorMsg
return|;
block|}
name|int
name|lastSpace
init|=
name|truncatedMesg
operator|.
name|lastIndexOf
argument_list|(
name|SPACE
argument_list|)
decl_stmt|;
if|if
condition|(
name|lastSpace
operator|==
operator|-
literal|1
condition|)
block|{
break|break;
block|}
comment|// hack off the last word and try again
name|truncatedMesg
operator|=
name|truncatedMesg
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|lastSpace
argument_list|)
operator|.
name|trim
argument_list|()
expr_stmt|;
block|}
return|return
name|GENERIC_ERROR
return|;
block|}
comment|/**    * Given an error code, returns the ErrorMsg object associated with it.    * @param errorCode An error code    * @return ErrorMsg    */
specifier|public
specifier|static
name|ErrorMsg
name|getErrorMsg
parameter_list|(
name|int
name|errorCode
parameter_list|)
block|{
for|for
control|(
name|ErrorMsg
name|errorMsg
range|:
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|errorMsg
operator|.
name|getErrorCode
argument_list|()
operator|==
name|errorCode
condition|)
block|{
return|return
name|errorMsg
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * For a given error message string, searches for a<code>ErrorMsg</code> enum    * that appears to be a match. If an match is found, returns the    *<code>SQLState</code> associated with the<code>ErrorMsg</code>. If a match    * is not found or<code>ErrorMsg</code> has no<code>SQLState</code>, returns    * the<code>SQLState</code> bound to the<code>GENERIC_ERROR</code>    *<code>ErrorMsg</code>.    *    * @param mesg    *          An error message string    * @return SQLState    */
specifier|public
specifier|static
name|String
name|findSQLState
parameter_list|(
name|String
name|mesg
parameter_list|)
block|{
name|ErrorMsg
name|error
init|=
name|getErrorMsg
argument_list|(
name|mesg
argument_list|)
decl_stmt|;
return|return
name|error
operator|.
name|getSQLState
argument_list|()
return|;
block|}
specifier|private
name|ErrorMsg
parameter_list|(
name|int
name|errorCode
parameter_list|,
name|String
name|mesg
parameter_list|)
block|{
comment|// 42000 is the generic SQLState for syntax error.
name|this
argument_list|(
name|errorCode
argument_list|,
name|mesg
argument_list|,
literal|"42000"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ErrorMsg
parameter_list|(
name|int
name|errorCode
parameter_list|,
name|String
name|mesg
parameter_list|,
name|String
name|sqlState
parameter_list|)
block|{
name|this
operator|.
name|errorCode
operator|=
name|errorCode
expr_stmt|;
name|this
operator|.
name|mesg
operator|=
name|mesg
expr_stmt|;
name|this
operator|.
name|sqlState
operator|=
name|sqlState
expr_stmt|;
block|}
specifier|private
specifier|static
name|int
name|getLine
parameter_list|(
name|ASTNode
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
name|ASTNode
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
specifier|static
name|int
name|getCharPositionInLine
parameter_list|(
name|ASTNode
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
name|ASTNode
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
comment|// Dirty hack as this will throw away spaces and other things - find a better
comment|// way!
specifier|public
specifier|static
name|String
name|getText
parameter_list|(
name|ASTNode
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
name|ASTNode
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
specifier|public
name|String
name|getMsg
parameter_list|(
name|ASTNode
name|tree
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|renderPosition
argument_list|(
name|sb
argument_list|,
name|tree
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|mesg
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" '"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|getText
argument_list|(
name|tree
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"'"
argument_list|)
expr_stmt|;
name|renderOrigin
argument_list|(
name|sb
argument_list|,
name|tree
operator|.
name|getOrigin
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|void
name|renderOrigin
parameter_list|(
name|StringBuilder
name|sb
parameter_list|,
name|ASTNodeOrigin
name|origin
parameter_list|)
block|{
while|while
condition|(
name|origin
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" in definition of "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|origin
operator|.
name|getObjectType
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|origin
operator|.
name|getObjectName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" ["
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|HiveUtils
operator|.
name|LINE_SEP
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|origin
operator|.
name|getObjectDefinition
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|HiveUtils
operator|.
name|LINE_SEP
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"] used as "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|origin
operator|.
name|getUsageAlias
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" at "
argument_list|)
expr_stmt|;
name|ASTNode
name|usageNode
init|=
name|origin
operator|.
name|getUsageNode
argument_list|()
decl_stmt|;
name|renderPosition
argument_list|(
name|sb
argument_list|,
name|usageNode
argument_list|)
expr_stmt|;
name|origin
operator|=
name|usageNode
operator|.
name|getOrigin
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|renderPosition
parameter_list|(
name|StringBuilder
name|sb
parameter_list|,
name|ASTNode
name|tree
parameter_list|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"Line "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|getLine
argument_list|(
name|tree
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|getCharPositionInLine
argument_list|(
name|tree
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
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
name|ASTNode
operator|)
name|tree
argument_list|)
return|;
block|}
specifier|public
name|String
name|getMsg
parameter_list|(
name|ASTNode
name|tree
parameter_list|,
name|String
name|reason
parameter_list|)
block|{
return|return
name|getMsg
argument_list|(
name|tree
argument_list|)
operator|+
literal|": "
operator|+
name|reason
return|;
block|}
specifier|public
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
name|ASTNode
operator|)
name|tree
argument_list|,
name|reason
argument_list|)
return|;
block|}
specifier|public
name|String
name|getMsg
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
return|return
name|mesg
operator|+
literal|" "
operator|+
name|reason
return|;
block|}
specifier|public
name|String
name|getErrorCodedMsg
parameter_list|()
block|{
return|return
literal|"[Error "
operator|+
name|errorCode
operator|+
literal|"]: "
operator|+
name|mesg
return|;
block|}
specifier|public
specifier|static
name|Pattern
name|getErrorCodePattern
parameter_list|()
block|{
return|return
name|ERROR_CODE_PATTERN
return|;
block|}
specifier|public
name|String
name|getMsg
parameter_list|()
block|{
return|return
name|mesg
return|;
block|}
specifier|public
name|String
name|getSQLState
parameter_list|()
block|{
return|return
name|sqlState
return|;
block|}
specifier|public
name|int
name|getErrorCode
parameter_list|()
block|{
return|return
name|errorCode
return|;
block|}
block|}
end_enum

end_unit

