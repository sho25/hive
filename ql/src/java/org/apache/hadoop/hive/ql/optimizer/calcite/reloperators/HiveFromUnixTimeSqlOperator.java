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
name|reloperators
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|fun
operator|.
name|SqlAbstractTimeFunction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|type
operator|.
name|SqlTypeName
import|;
end_import

begin_comment
comment|/**  * Calcite SQL operator mapping to FROM_UNIXTIME Hive UDF  */
end_comment

begin_class
specifier|public
class|class
name|HiveFromUnixTimeSqlOperator
extends|extends
name|SqlAbstractTimeFunction
block|{
specifier|public
specifier|static
specifier|final
name|HiveFromUnixTimeSqlOperator
name|INSTANCE
init|=
operator|new
name|HiveFromUnixTimeSqlOperator
argument_list|()
decl_stmt|;
specifier|protected
name|HiveFromUnixTimeSqlOperator
parameter_list|()
block|{
name|super
argument_list|(
literal|"FROM_UNIXTIME"
argument_list|,
name|SqlTypeName
operator|.
name|TIMESTAMP
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

