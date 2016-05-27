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
name|util
operator|.
name|UUID
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
name|Description
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
name|UDF
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * UDFUUID.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"uuid"
argument_list|,
name|value
operator|=
literal|"_FUNC_() - Returns a universally unique identifier (UUID) string."
argument_list|,
name|extended
operator|=
literal|"The value is returned as a canonical UUID 36-character string.\n"
operator|+
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_();\n"
operator|+
literal|"  '0baf1f52-53df-487f-8292-99a03716b688'\n"
operator|+
literal|"> SELECT _FUNC_();\n"
operator|+
literal|"  '36718a53-84f5-45d6-8796-4f79983ad49d'"
argument_list|)
specifier|public
class|class
name|UDFUUID
extends|extends
name|UDF
block|{
specifier|private
specifier|final
name|Text
name|result
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
comment|/**    * Returns a universally unique identifier (UUID) string (36 characters).    *    * @return Text    */
specifier|public
name|Text
name|evaluate
parameter_list|()
block|{
name|result
operator|.
name|set
argument_list|(
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

