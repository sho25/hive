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
name|exec
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Documented
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|ElementType
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Retention
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|RetentionPolicy
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Target
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
name|GenericUDAFResolver2
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
name|ptf
operator|.
name|WindowingTableFunction
import|;
end_import

begin_annotation_defn
annotation|@
name|Retention
argument_list|(
name|RetentionPolicy
operator|.
name|RUNTIME
argument_list|)
annotation|@
name|Target
argument_list|(
name|ElementType
operator|.
name|TYPE
argument_list|)
annotation|@
name|Documented
specifier|public
annotation_defn|@interface
name|WindowFunctionDescription
block|{
name|Description
name|description
parameter_list|()
function_decl|;
comment|/**    * controls whether this function can be applied to a Window.    *<p>    * Ranking function: Rank, Dense_Rank, Percent_Rank and Cume_Dist don't operate on Windows.    * Why? a window specification implies a row specific range i.e. every row gets its own set of rows to process the UDAF on.    * For ranking defining a set of rows for every row makes no sense.    *<p>    * All other UDAFs can be computed for a Window.    */
name|boolean
name|supportsWindow
parameter_list|()
default|default
literal|true
function_decl|;
comment|/**    * A WindowFunc is implemented as {@link GenericUDAFResolver2}. It returns only one value.    * If this is true then the function must return a List which is taken to be the column for this function in the Output table returned by the    * {@link WindowingTableFunction}. Otherwise the output is assumed to be a single value, the column of the Output will contain the same value    * for all the rows.    */
name|boolean
name|pivotResult
parameter_list|()
default|default
literal|false
function_decl|;
comment|/**    * Used in translations process to validate arguments    * @return true if ranking function    */
name|boolean
name|rankingFunction
parameter_list|()
default|default
literal|false
function_decl|;
comment|/**     * Using in analytical functions to specify that UDF implies an ordering     * @return true if the function implies order     */
name|boolean
name|impliesOrder
parameter_list|()
default|default
literal|false
function_decl|;
block|}
end_annotation_defn

end_unit

