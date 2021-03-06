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
name|metastore
operator|.
name|model
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
name|metastore
operator|.
name|api
operator|.
name|RuntimeStat
import|;
end_import

begin_comment
comment|/**  * Represents a runtime stat query entry.  *  * As a query may contain a large number of operatorstat entries; they are stored together in a single row in the metastore.  * The number of operator stat entries this entity has; is shown in the weight column.  */
end_comment

begin_class
specifier|public
class|class
name|MRuntimeStat
block|{
specifier|private
name|int
name|createTime
decl_stmt|;
specifier|private
name|int
name|weight
decl_stmt|;
specifier|private
name|byte
index|[]
name|payload
decl_stmt|;
specifier|public
specifier|static
name|MRuntimeStat
name|fromThrift
parameter_list|(
name|RuntimeStat
name|stat
parameter_list|)
block|{
name|MRuntimeStat
name|ret
init|=
operator|new
name|MRuntimeStat
argument_list|()
decl_stmt|;
name|ret
operator|.
name|weight
operator|=
name|stat
operator|.
name|getWeight
argument_list|()
expr_stmt|;
name|ret
operator|.
name|payload
operator|=
name|stat
operator|.
name|getPayload
argument_list|()
expr_stmt|;
name|ret
operator|.
name|createTime
operator|=
call|(
name|int
call|)
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|/
literal|1000
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
specifier|public
specifier|static
name|RuntimeStat
name|toThrift
parameter_list|(
name|MRuntimeStat
name|stat
parameter_list|)
block|{
name|RuntimeStat
name|ret
init|=
operator|new
name|RuntimeStat
argument_list|()
decl_stmt|;
name|ret
operator|.
name|setWeight
argument_list|(
name|stat
operator|.
name|weight
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setCreateTime
argument_list|(
name|stat
operator|.
name|createTime
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setPayload
argument_list|(
name|stat
operator|.
name|payload
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
specifier|public
name|int
name|getWeight
parameter_list|()
block|{
return|return
name|weight
return|;
block|}
specifier|public
name|int
name|getCreatedTime
parameter_list|()
block|{
return|return
name|createTime
return|;
block|}
block|}
end_class

end_unit

