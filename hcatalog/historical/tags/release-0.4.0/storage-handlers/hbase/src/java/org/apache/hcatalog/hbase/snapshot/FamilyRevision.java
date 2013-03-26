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
name|hcatalog
operator|.
name|hbase
operator|.
name|snapshot
package|;
end_package

begin_comment
comment|/**  * A FamiliyRevision class consists of a revision number and a expiration  * timestamp. When a write transaction starts, the transaction  * object is appended to the transaction list of the each column  * family and stored in the corresponding znode. When a write transaction is  * committed, the transaction object is removed from the list.  */
end_comment

begin_class
specifier|public
class|class
name|FamilyRevision
implements|implements
name|Comparable
argument_list|<
name|FamilyRevision
argument_list|>
block|{
specifier|private
name|long
name|revision
decl_stmt|;
specifier|private
name|long
name|timestamp
decl_stmt|;
comment|/**      * Create a FamilyRevision object      * @param rev revision number      * @param ts expiration timestamp      */
name|FamilyRevision
parameter_list|(
name|long
name|rev
parameter_list|,
name|long
name|ts
parameter_list|)
block|{
name|this
operator|.
name|revision
operator|=
name|rev
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|ts
expr_stmt|;
block|}
specifier|public
name|long
name|getRevision
parameter_list|()
block|{
return|return
name|revision
return|;
block|}
specifier|public
name|long
name|getExpireTimestamp
parameter_list|()
block|{
return|return
name|timestamp
return|;
block|}
name|void
name|setExpireTimestamp
parameter_list|(
name|long
name|ts
parameter_list|)
block|{
name|timestamp
operator|=
name|ts
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|description
init|=
literal|"revision: "
operator|+
name|revision
operator|+
literal|" ts: "
operator|+
name|timestamp
decl_stmt|;
return|return
name|description
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|FamilyRevision
name|o
parameter_list|)
block|{
name|long
name|d
init|=
name|revision
operator|-
name|o
operator|.
name|getRevision
argument_list|()
decl_stmt|;
return|return
operator|(
name|d
operator|<
literal|0
operator|)
condition|?
operator|-
literal|1
else|:
operator|(
name|d
operator|>
literal|0
operator|)
condition|?
literal|1
else|:
literal|0
return|;
block|}
block|}
end_class

end_unit

