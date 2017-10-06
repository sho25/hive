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
name|metadata
operator|.
name|formatting
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
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
name|api
operator|.
name|ColumnStatisticsObj
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
name|metadata
operator|.
name|ForeignKeyInfo
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
name|Hive
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
name|NotNullConstraint
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
name|ql
operator|.
name|metadata
operator|.
name|PrimaryKeyInfo
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
name|metadata
operator|.
name|UniqueConstraint
import|;
end_import

begin_comment
comment|/**  * Interface to format table and index information.  We can format it  * for human readability (lines of text) or for machine readability  * (json).  */
end_comment

begin_interface
specifier|public
interface|interface
name|MetaDataFormatter
block|{
comment|/**    * Write an error message.    * @param sqlState if {@code null}, will be ignored    */
specifier|public
name|void
name|error
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|String
name|msg
parameter_list|,
name|int
name|errorCode
parameter_list|,
name|String
name|sqlState
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * @param sqlState if {@code null}, will be skipped in output    * @param errorDetail usually string version of some Exception, if {@code null}, will be ignored    */
specifier|public
name|void
name|error
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|String
name|errorMessage
parameter_list|,
name|int
name|errorCode
parameter_list|,
name|String
name|sqlState
parameter_list|,
name|String
name|errorDetail
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * Show a list of tables.    */
specifier|public
name|void
name|showTables
parameter_list|(
name|DataOutputStream
name|out
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|tables
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * Describe table.    * @param out    * @param colPath    * @param tableName    * @param tbl    * @param part    * @param cols    * @param isFormatted - describe with formatted keyword    * @param isExt    * @param isPretty    * @param isOutputPadded - if true, add spacing and indentation    * @param colStats    * @param fkInfo  foreign keys information    * @param pkInfo  primary key information    * @param ukInfo  unique constraint information    * @param nnInfo  not null constraint information    * @throws HiveException    */
specifier|public
name|void
name|describeTable
parameter_list|(
name|DataOutputStream
name|out
parameter_list|,
name|String
name|colPath
parameter_list|,
name|String
name|tableName
parameter_list|,
name|Table
name|tbl
parameter_list|,
name|Partition
name|part
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
parameter_list|,
name|boolean
name|isFormatted
parameter_list|,
name|boolean
name|isExt
parameter_list|,
name|boolean
name|isOutputPadded
parameter_list|,
name|List
argument_list|<
name|ColumnStatisticsObj
argument_list|>
name|colStats
parameter_list|,
name|PrimaryKeyInfo
name|pkInfo
parameter_list|,
name|ForeignKeyInfo
name|fkInfo
parameter_list|,
name|UniqueConstraint
name|ukInfo
parameter_list|,
name|NotNullConstraint
name|nnInfo
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * Show the table status.    */
specifier|public
name|void
name|showTableStatus
parameter_list|(
name|DataOutputStream
name|out
parameter_list|,
name|Hive
name|db
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|List
argument_list|<
name|Table
argument_list|>
name|tbls
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|part
parameter_list|,
name|Partition
name|par
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * Show the table partitions.    */
specifier|public
name|void
name|showTablePartitions
parameter_list|(
name|DataOutputStream
name|out
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|parts
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * Show the databases    */
specifier|public
name|void
name|showDatabases
parameter_list|(
name|DataOutputStream
name|out
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|databases
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * Describe a database.    */
specifier|public
name|void
name|showDatabaseDescription
parameter_list|(
name|DataOutputStream
name|out
parameter_list|,
name|String
name|database
parameter_list|,
name|String
name|comment
parameter_list|,
name|String
name|location
parameter_list|,
name|String
name|ownerName
parameter_list|,
name|String
name|ownerType
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
throws|throws
name|HiveException
function_decl|;
block|}
end_interface

end_unit

