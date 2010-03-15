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
name|xml
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

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"xpath_boolean"
argument_list|,
name|value
operator|=
literal|"_FUNC_(xml, xpath) - Evaluates a boolean xpath expression"
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_('<a><b>1</b></a>','a/b') FROM src LIMIT 1;\n"
operator|+
literal|"  true\n"
operator|+
literal|"> SELECT _FUNC_('<a><b>1</b></a>','a/b = 2') FROM src LIMIT 1;\n"
operator|+
literal|"  false"
argument_list|)
specifier|public
class|class
name|UDFXPathBoolean
extends|extends
name|UDF
block|{
specifier|private
specifier|final
name|UDFXPathUtil
name|xpath
init|=
operator|new
name|UDFXPathUtil
argument_list|()
decl_stmt|;
specifier|public
name|boolean
name|evaluate
parameter_list|(
name|String
name|xml
parameter_list|,
name|String
name|path
parameter_list|)
block|{
return|return
name|xpath
operator|.
name|evalBoolean
argument_list|(
name|xml
argument_list|,
name|path
argument_list|)
operator|.
name|booleanValue
argument_list|()
return|;
block|}
block|}
end_class

end_unit

