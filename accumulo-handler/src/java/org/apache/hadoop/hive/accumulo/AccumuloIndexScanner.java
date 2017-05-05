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
name|accumulo
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|data
operator|.
name|Range
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
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * Specification for implementing a AccumuloIndexScanner.  */
end_comment

begin_interface
specifier|public
interface|interface
name|AccumuloIndexScanner
block|{
comment|/**    * Initialize the index scanner implementation with the runtime configuration.    *    * @param conf  - the hadoop configuration    */
name|void
name|init
parameter_list|(
name|Configuration
name|conf
parameter_list|)
function_decl|;
comment|/**    * Check if column is defined as being indexed.    *    * @param columnName - the hive column name    * @return true if the column is indexed    */
name|boolean
name|isIndexed
parameter_list|(
name|String
name|columnName
parameter_list|)
function_decl|;
comment|/**    * Get a list of rowid ranges by scanning a column index.    *    * @param column     - the hive column name    * @param indexRange - Key range to scan on the index table    * @return List of matching rowid ranges or null if too many matches found    *    */
name|List
argument_list|<
name|Range
argument_list|>
name|getIndexRowRanges
parameter_list|(
name|String
name|column
parameter_list|,
name|Range
name|indexRange
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

