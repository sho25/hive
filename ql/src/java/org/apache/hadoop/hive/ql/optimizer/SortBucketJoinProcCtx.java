begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Map
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
name|conf
operator|.
name|HiveConf
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
name|ExprNodeDesc
import|;
end_import

begin_class
specifier|public
class|class
name|SortBucketJoinProcCtx
extends|extends
name|BucketJoinProcCtx
block|{
specifier|private
name|String
index|[]
name|srcs
decl_stmt|;
specifier|private
name|int
name|bigTablePosition
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|keyExprMap
decl_stmt|;
specifier|public
name|SortBucketJoinProcCtx
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
index|[]
name|getSrcs
parameter_list|()
block|{
return|return
name|srcs
return|;
block|}
specifier|public
name|void
name|setSrcs
parameter_list|(
name|String
index|[]
name|srcs
parameter_list|)
block|{
name|this
operator|.
name|srcs
operator|=
name|srcs
expr_stmt|;
block|}
specifier|public
name|int
name|getBigTablePosition
parameter_list|()
block|{
return|return
name|bigTablePosition
return|;
block|}
specifier|public
name|void
name|setBigTablePosition
parameter_list|(
name|int
name|bigTablePosition
parameter_list|)
block|{
name|this
operator|.
name|bigTablePosition
operator|=
name|bigTablePosition
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|getKeyExprMap
parameter_list|()
block|{
return|return
name|keyExprMap
return|;
block|}
specifier|public
name|void
name|setKeyExprMap
parameter_list|(
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|keyExprMap
parameter_list|)
block|{
name|this
operator|.
name|keyExprMap
operator|=
name|keyExprMap
expr_stmt|;
block|}
block|}
end_class

end_unit

