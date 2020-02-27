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
name|stats
operator|.
name|estimator
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
name|Optional
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
name|ColStatistics
import|;
end_import

begin_comment
comment|/**  * Enables statistics related computation on UDFs  */
end_comment

begin_interface
specifier|public
interface|interface
name|StatEstimator
block|{
comment|/**    * Computes the output statistics of the actual UDF.    *    * The estimator should return with a prefereably overestimated {@link ColStatistics} object if possible.    * The actual estimation logic may decide to not give an estimation; it should return with {@link Optional#empty()}.    *    * Note: at the time of the call there will be {@link ColStatistics} for all the arguments; if that is not available - the estimation is skipped.    *    * @param argStats the statistics for every argument of the UDF    * @return {@link ColStatistics} estimate for the actual UDF.    */
specifier|public
name|Optional
argument_list|<
name|ColStatistics
argument_list|>
name|estimate
parameter_list|(
name|List
argument_list|<
name|ColStatistics
argument_list|>
name|argStats
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

