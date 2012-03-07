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
name|index
package|;
end_package

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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configurable
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
name|Index
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
name|Task
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
name|hooks
operator|.
name|ReadEntity
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
name|hooks
operator|.
name|WriteEntity
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
name|parse
operator|.
name|ParseContext
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

begin_comment
comment|/**  * HiveIndexHandler defines a pluggable interface for adding new index handlers  * to Hive.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HiveIndexHandler
extends|extends
name|Configurable
block|{
comment|/**    * Determines whether this handler implements indexes by creating an index    * table.    *    * @return true if index creation implies creation of an index table in Hive;    *         false if the index representation is not stored in a Hive table    */
name|boolean
name|usesIndexTable
parameter_list|()
function_decl|;
comment|/**    * Requests that the handler validate an index definition and fill in    * additional information about its stored representation.    *    * @param baseTable    *          the definition of the table being indexed    *    * @param index    *          the definition of the index being created    *    * @param indexTable    *          a partial definition of the index table to be used for storing the    *          index representation, or null if usesIndexTable() returns false;    *          the handler can augment the index's storage descriptor (e.g. with    *          information about input/output format) and/or the index table's    *          definition (typically with additional columns containing the index    *          representation, e.g. pointers into HDFS).    *    * @throws HiveException if the index definition is invalid with respect to    *         either the base table or the supplied index table definition    */
name|void
name|analyzeIndexDefinition
parameter_list|(
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
name|baseTable
parameter_list|,
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
name|Index
name|index
parameter_list|,
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
name|indexTable
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * Requests that the handler generate a plan for building the index; the plan    * should read the base table and write out the index representation.    *    * @param baseTbl    *          the definition of the table being indexed    *    * @param index    *          the definition of the index    *    * @param baseTblPartitions    *          list of base table partitions with each element mirrors to the    *          corresponding one in indexTblPartitions    *    * @param indexTbl    *          the definition of the index table, or null if usesIndexTable()    *          returns null    *    * @param inputs    *          inputs for hooks, supplemental outputs going    *          along with the return value    *    * @param outputs    *          outputs for hooks, supplemental outputs going    *          along with the return value    *    * @return list of tasks to be executed in parallel for building the index    *    * @throws HiveException if plan generation fails    */
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|generateIndexBuildTaskList
parameter_list|(
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
name|baseTbl
parameter_list|,
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
name|Index
name|index
parameter_list|,
name|List
argument_list|<
name|Partition
argument_list|>
name|indexTblPartitions
parameter_list|,
name|List
argument_list|<
name|Partition
argument_list|>
name|baseTblPartitions
parameter_list|,
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
name|indexTbl
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * Generate the list of tasks required to run an index optimized sub-query for the    * given predicate, using the given indexes. If multiple indexes are    * provided, it is up to the handler whether to use none, one, some or all of    * them. The supplied predicate may reference any of the columns from any of    * the indexes. If the handler decides to use more than one index, it is    * responsible for generating tasks to combine their search results    * (e.g. performing a JOIN on the result).    * @param indexes    * @param predicate    * @param pctx    * @param queryContext contains results, such as query tasks and input configuration    */
name|void
name|generateIndexQuery
parameter_list|(
name|List
argument_list|<
name|Index
argument_list|>
name|indexes
parameter_list|,
name|ExprNodeDesc
name|predicate
parameter_list|,
name|ParseContext
name|pctx
parameter_list|,
name|HiveIndexQueryContext
name|queryContext
parameter_list|)
function_decl|;
comment|/**    * Check the size of an input query to make sure it fits within the bounds    *    * @param inputSize size (in bytes) of the query in question    * @param conf    * @return true if query is within the bounds    */
name|boolean
name|checkQuerySize
parameter_list|(
name|long
name|inputSize
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

