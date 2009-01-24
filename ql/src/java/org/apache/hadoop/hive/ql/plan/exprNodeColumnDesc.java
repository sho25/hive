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
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

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
name|typeinfo
operator|.
name|TypeInfo
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
name|typeinfo
operator|.
name|TypeInfoFactory
import|;
end_import

begin_class
specifier|public
class|class
name|exprNodeColumnDesc
extends|extends
name|exprNodeDesc
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
name|column
decl_stmt|;
specifier|public
name|exprNodeColumnDesc
parameter_list|()
block|{}
specifier|public
name|exprNodeColumnDesc
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|,
name|String
name|column
parameter_list|)
block|{
name|super
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|column
operator|=
name|column
expr_stmt|;
block|}
specifier|public
name|exprNodeColumnDesc
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|c
parameter_list|,
name|String
name|column
parameter_list|)
block|{
name|super
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|c
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|column
operator|=
name|column
expr_stmt|;
block|}
specifier|public
name|String
name|getColumn
parameter_list|()
block|{
return|return
name|this
operator|.
name|column
return|;
block|}
specifier|public
name|void
name|setColumn
parameter_list|(
name|String
name|column
parameter_list|)
block|{
name|this
operator|.
name|column
operator|=
name|column
expr_stmt|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"Column["
operator|+
name|column
operator|+
literal|"]"
return|;
block|}
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"expr"
argument_list|)
annotation|@
name|Override
specifier|public
name|String
name|getExprString
parameter_list|()
block|{
return|return
name|getColumn
argument_list|()
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getCols
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|lst
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|lst
operator|.
name|add
argument_list|(
name|column
argument_list|)
expr_stmt|;
return|return
name|lst
return|;
block|}
block|}
end_class

end_unit

