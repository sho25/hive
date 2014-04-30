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
name|Context
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
name|ASTNode
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
name|QB
import|;
end_import

begin_class
specifier|public
class|class
name|ExplainSQRewriteWork
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
name|resFile
decl_stmt|;
specifier|private
name|QB
name|qb
decl_stmt|;
specifier|private
name|ASTNode
name|ast
decl_stmt|;
specifier|private
name|Context
name|ctx
decl_stmt|;
specifier|public
name|ExplainSQRewriteWork
parameter_list|()
block|{   }
specifier|public
name|ExplainSQRewriteWork
parameter_list|(
name|String
name|resFile
parameter_list|,
name|QB
name|qb
parameter_list|,
name|ASTNode
name|ast
parameter_list|,
name|Context
name|ctx
parameter_list|)
block|{
name|this
operator|.
name|resFile
operator|=
name|resFile
expr_stmt|;
name|this
operator|.
name|qb
operator|=
name|qb
expr_stmt|;
name|this
operator|.
name|ast
operator|=
name|ast
expr_stmt|;
name|this
operator|.
name|ctx
operator|=
name|ctx
expr_stmt|;
block|}
specifier|public
name|String
name|getResFile
parameter_list|()
block|{
return|return
name|resFile
return|;
block|}
specifier|public
name|QB
name|getQb
parameter_list|()
block|{
return|return
name|qb
return|;
block|}
specifier|public
name|ASTNode
name|getAst
parameter_list|()
block|{
return|return
name|ast
return|;
block|}
specifier|public
name|Context
name|getCtx
parameter_list|()
block|{
return|return
name|ctx
return|;
block|}
block|}
end_class

end_unit

