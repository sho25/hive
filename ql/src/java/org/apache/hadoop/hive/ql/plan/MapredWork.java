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
name|ByteArrayOutputStream
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
name|exec
operator|.
name|Operator
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
name|Utilities
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
name|mapred
operator|.
name|JobConf
import|;
end_import

begin_comment
comment|/**  * MapredWork.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Map Reduce"
argument_list|)
specifier|public
class|class
name|MapredWork
extends|extends
name|AbstractOperatorDesc
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
name|MapWork
name|mapWork
init|=
operator|new
name|MapWork
argument_list|()
decl_stmt|;
specifier|private
name|ReduceWork
name|reduceWork
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|finalMapRed
decl_stmt|;
annotation|@
name|Explain
argument_list|(
name|skipHeader
operator|=
literal|true
argument_list|,
name|displayName
operator|=
literal|"Map"
argument_list|)
specifier|public
name|MapWork
name|getMapWork
parameter_list|()
block|{
return|return
name|mapWork
return|;
block|}
specifier|public
name|void
name|setMapWork
parameter_list|(
name|MapWork
name|mapWork
parameter_list|)
block|{
name|this
operator|.
name|mapWork
operator|=
name|mapWork
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|skipHeader
operator|=
literal|true
argument_list|,
name|displayName
operator|=
literal|"Reduce"
argument_list|)
specifier|public
name|ReduceWork
name|getReduceWork
parameter_list|()
block|{
return|return
name|reduceWork
return|;
block|}
specifier|public
name|void
name|setReduceWork
parameter_list|(
name|ReduceWork
name|reduceWork
parameter_list|)
block|{
name|this
operator|.
name|reduceWork
operator|=
name|reduceWork
expr_stmt|;
block|}
specifier|public
name|boolean
name|isFinalMapRed
parameter_list|()
block|{
return|return
name|finalMapRed
return|;
block|}
specifier|public
name|void
name|setFinalMapRed
parameter_list|(
name|boolean
name|finalMapRed
parameter_list|)
block|{
name|this
operator|.
name|finalMapRed
operator|=
name|finalMapRed
expr_stmt|;
block|}
specifier|public
name|void
name|configureJobConf
parameter_list|(
name|JobConf
name|job
parameter_list|)
block|{
name|mapWork
operator|.
name|configureJobConf
argument_list|(
name|job
argument_list|)
expr_stmt|;
if|if
condition|(
name|reduceWork
operator|!=
literal|null
condition|)
block|{
name|reduceWork
operator|.
name|configureJobConf
argument_list|(
name|job
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|getAllOperators
parameter_list|()
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|ops
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|ops
operator|.
name|addAll
argument_list|(
name|mapWork
operator|.
name|getAllOperators
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|reduceWork
operator|!=
literal|null
condition|)
block|{
name|ops
operator|.
name|addAll
argument_list|(
name|reduceWork
operator|.
name|getAllOperators
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|ops
return|;
block|}
specifier|public
name|String
name|toXML
parameter_list|()
block|{
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|Utilities
operator|.
name|serializeObject
argument_list|(
name|this
argument_list|,
name|baos
argument_list|)
expr_stmt|;
return|return
operator|(
name|baos
operator|.
name|toString
argument_list|()
operator|)
return|;
block|}
block|}
end_class

end_unit

