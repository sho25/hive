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
name|plan
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_comment
comment|/**  * LoadDesc.  *  */
end_comment

begin_class
specifier|public
class|class
name|LoadDesc
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|String
name|sourceDir
decl_stmt|;
specifier|public
name|LoadDesc
parameter_list|()
block|{   }
specifier|public
name|LoadDesc
parameter_list|(
specifier|final
name|String
name|sourceDir
parameter_list|)
block|{
name|this
operator|.
name|sourceDir
operator|=
name|sourceDir
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"source"
argument_list|,
name|normalExplain
operator|=
literal|false
argument_list|)
specifier|public
name|String
name|getSourceDir
parameter_list|()
block|{
return|return
name|sourceDir
return|;
block|}
specifier|public
name|void
name|setSourceDir
parameter_list|(
specifier|final
name|String
name|source
parameter_list|)
block|{
name|sourceDir
operator|=
name|source
expr_stmt|;
block|}
block|}
end_class

end_unit

