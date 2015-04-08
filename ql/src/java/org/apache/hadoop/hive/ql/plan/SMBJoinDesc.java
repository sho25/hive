begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HashMap
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
name|ql
operator|.
name|exec
operator|.
name|DummyStoreOperator
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
name|Explain
operator|.
name|Level
import|;
end_import

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Sorted Merge Bucket Map Join Operator"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|SMBJoinDesc
extends|extends
name|MapJoinDesc
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
name|MapredLocalWork
name|localWork
decl_stmt|;
comment|// keep a mapping from tag to the fetch operator alias
specifier|private
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
name|tagToAlias
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|DummyStoreOperator
argument_list|>
name|aliasToSink
decl_stmt|;
specifier|public
name|SMBJoinDesc
parameter_list|(
name|MapJoinDesc
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
name|SMBJoinDesc
parameter_list|()
block|{   }
specifier|public
name|MapredLocalWork
name|getLocalWork
parameter_list|()
block|{
return|return
name|localWork
return|;
block|}
specifier|public
name|void
name|setLocalWork
parameter_list|(
name|MapredLocalWork
name|localWork
parameter_list|)
block|{
name|this
operator|.
name|localWork
operator|=
name|localWork
expr_stmt|;
block|}
specifier|public
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
name|getTagToAlias
parameter_list|()
block|{
return|return
name|tagToAlias
return|;
block|}
specifier|public
name|void
name|setTagToAlias
parameter_list|(
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
name|tagToAlias
parameter_list|)
block|{
name|this
operator|.
name|tagToAlias
operator|=
name|tagToAlias
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|DummyStoreOperator
argument_list|>
name|getAliasToSink
parameter_list|()
block|{
return|return
name|aliasToSink
return|;
block|}
specifier|public
name|void
name|setAliasToSink
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|DummyStoreOperator
argument_list|>
name|aliasToSink
parameter_list|)
block|{
name|this
operator|.
name|aliasToSink
operator|=
name|aliasToSink
expr_stmt|;
block|}
block|}
end_class

end_unit

