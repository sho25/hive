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
operator|.
name|generic
package|;
end_package

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
name|UDTFOperator
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

begin_comment
comment|/**  * UDTFCollector collects data from a GenericUDTF and passes the data to a  * UDTFOperator  */
end_comment

begin_class
specifier|public
class|class
name|UDTFCollector
implements|implements
name|Collector
block|{
comment|/*    * (non-Javadoc)    *     * @see    * org.apache.hadoop.hive.ql.udf.generic.Collector#collect(java.lang.Object)    */
name|UDTFOperator
name|op
init|=
literal|null
decl_stmt|;
specifier|public
name|UDTFCollector
parameter_list|(
name|UDTFOperator
name|op
parameter_list|)
block|{
name|this
operator|.
name|op
operator|=
name|op
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|collect
parameter_list|(
name|Object
name|input
parameter_list|)
throws|throws
name|HiveException
block|{
name|op
operator|.
name|forwardUDTFOutput
argument_list|(
name|input
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

