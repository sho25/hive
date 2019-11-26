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
name|optimizer
operator|.
name|calcite
operator|.
name|translator
operator|.
name|opconventer
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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveAggregate
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
name|optimizer
operator|.
name|calcite
operator|.
name|translator
operator|.
name|opconventer
operator|.
name|HiveOpConverter
operator|.
name|OpAttr
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
name|SemanticException
import|;
end_import

begin_class
class|class
name|HiveAggregateVisitor
extends|extends
name|HiveRelNodeVisitor
argument_list|<
name|HiveAggregate
argument_list|>
block|{
name|HiveAggregateVisitor
parameter_list|(
name|HiveOpConverter
name|hiveOpConverter
parameter_list|)
block|{
name|super
argument_list|(
name|hiveOpConverter
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|OpAttr
name|visit
parameter_list|(
name|HiveAggregate
name|aggRel
parameter_list|)
throws|throws
name|SemanticException
block|{
name|OpAttr
name|inputOpAf
init|=
name|hiveOpConverter
operator|.
name|dispatch
argument_list|(
name|aggRel
operator|.
name|getInput
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|HiveGBOpConvUtil
operator|.
name|translateGB
argument_list|(
name|inputOpAf
argument_list|,
name|aggRel
argument_list|,
name|hiveOpConverter
operator|.
name|getHiveConf
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

