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
name|udf
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
name|Inherited
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
name|common
operator|.
name|classification
operator|.
name|InterfaceAudience
operator|.
name|Public
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
name|classification
operator|.
name|InterfaceStability
operator|.
name|Evolving
import|;
end_import

begin_comment
comment|/**  * UDFType annotations are used to describe properties of a UDF. This gives  * important information to the optimizer.  * If the UDF is not deterministic, or if it is stateful, it is necessary to  * annotate it as such for correctness.  *  */
end_comment

begin_annotation_defn
annotation|@
name|Public
annotation|@
name|Evolving
annotation|@
name|Target
argument_list|(
name|ElementType
operator|.
name|TYPE
argument_list|)
annotation|@
name|Retention
argument_list|(
name|RetentionPolicy
operator|.
name|RUNTIME
argument_list|)
annotation|@
name|Inherited
specifier|public
annotation_defn|@interface
name|UDFType
block|{
comment|/**    * Certain optimizations should not be applied if UDF is not deterministic.    * Deterministic UDF returns same result each time it is invoked with a    * particular input. This determinism just needs to hold within the context of    * a query.    *    * @return true if the UDF is deterministic    */
name|boolean
name|deterministic
parameter_list|()
default|default
literal|true
function_decl|;
comment|/**    * If a UDF stores state based on the sequence of records it has processed, it    * is stateful. A stateful UDF cannot be used in certain expressions such as    * case statement and certain optimizations such as AND/OR short circuiting    * don't apply for such UDFs, as they need to be invoked for each record.    * row_sequence is an example of stateful UDF. A stateful UDF is considered to    * be non-deterministic, irrespective of what deterministic() returns.    *    * @return true    */
name|boolean
name|stateful
parameter_list|()
default|default
literal|false
function_decl|;
comment|/**    * A UDF is considered distinctLike if the UDF can be evaluated on just the    * distinct values of a column. Examples include min and max UDFs. This    * information is used by metadata-only optimizer.    *    * @return true if UDF is distinctLike    */
name|boolean
name|distinctLike
parameter_list|()
default|default
literal|false
function_decl|;
comment|/**    * Using in analytical functions to specify that UDF implies an ordering    *    * @return true if the function implies order    */
name|boolean
name|impliesOrder
parameter_list|()
default|default
literal|false
function_decl|;
block|}
end_annotation_defn

end_unit

