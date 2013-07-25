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
name|exec
operator|.
name|persistence
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ObjectInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ObjectOutput
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
name|Arrays
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
name|HashTableSinkOperator
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
name|HashTableSinkOperator
operator|.
name|HashTableSinkObjectCtx
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
name|MapJoinOperator
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
name|serde2
operator|.
name|SerDeException
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorUtils
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorUtils
operator|.
name|ObjectInspectorCopyOption
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
name|Writable
import|;
end_import

begin_comment
comment|/**  * Map Join Object used for both key.  */
end_comment

begin_class
specifier|public
class|class
name|MapJoinObjectKey
extends|extends
name|AbstractMapJoinKey
block|{
specifier|protected
specifier|transient
name|Object
index|[]
name|obj
decl_stmt|;
specifier|public
name|MapJoinObjectKey
parameter_list|()
block|{   }
comment|/**    * @param obj    */
specifier|public
name|MapJoinObjectKey
parameter_list|(
name|Object
index|[]
name|obj
parameter_list|)
block|{
name|this
operator|.
name|obj
operator|=
name|obj
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|hashCode
argument_list|(
name|obj
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|MapJoinObjectKey
name|other
init|=
operator|(
name|MapJoinObjectKey
operator|)
name|obj
decl_stmt|;
if|if
condition|(
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|this
operator|.
name|obj
argument_list|,
name|other
operator|.
name|obj
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readExternal
parameter_list|(
name|ObjectInput
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
try|try
block|{
comment|// get the tableDesc from the map stored in the mapjoin operator
name|HashTableSinkObjectCtx
name|ctx
init|=
name|MapJoinOperator
operator|.
name|getMetadata
argument_list|()
operator|.
name|get
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|metadataTag
argument_list|)
argument_list|)
decl_stmt|;
name|Writable
name|val
init|=
name|ctx
operator|.
name|getSerDe
argument_list|()
operator|.
name|getSerializedClass
argument_list|()
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|val
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|list
init|=
operator|(
name|ArrayList
argument_list|<
name|Object
argument_list|>
operator|)
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|ctx
operator|.
name|getSerDe
argument_list|()
operator|.
name|deserialize
argument_list|(
name|val
argument_list|)
argument_list|,
name|ctx
operator|.
name|getSerDe
argument_list|()
operator|.
name|getObjectInspector
argument_list|()
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
decl_stmt|;
if|if
condition|(
name|list
operator|==
literal|null
condition|)
block|{
name|obj
operator|=
operator|new
name|ArrayList
argument_list|(
literal|0
argument_list|)
operator|.
name|toArray
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|obj
operator|=
name|list
operator|.
name|toArray
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeExternal
parameter_list|(
name|ObjectOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
comment|// get the tableDesc from the map stored in the mapjoin operator
name|HashTableSinkObjectCtx
name|ctx
init|=
name|HashTableSinkOperator
operator|.
name|getMetadata
argument_list|()
operator|.
name|get
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|metadataTag
argument_list|)
argument_list|)
decl_stmt|;
comment|// Different processing for key and value
name|Writable
name|outVal
init|=
name|ctx
operator|.
name|getSerDe
argument_list|()
operator|.
name|serialize
argument_list|(
name|obj
argument_list|,
name|ctx
operator|.
name|getStandardOI
argument_list|()
argument_list|)
decl_stmt|;
name|outVal
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * @return the obj    */
specifier|public
name|Object
index|[]
name|getObj
parameter_list|()
block|{
return|return
name|obj
return|;
block|}
comment|/**    * @param obj    *          the obj to set    */
specifier|public
name|void
name|setObj
parameter_list|(
name|Object
index|[]
name|obj
parameter_list|)
block|{
name|this
operator|.
name|obj
operator|=
name|obj
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasAnyNulls
parameter_list|(
name|boolean
index|[]
name|nullsafes
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|!=
literal|null
operator|&&
name|obj
operator|.
name|length
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|obj
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|obj
index|[
name|i
index|]
operator|==
literal|null
operator|&&
operator|(
name|nullsafes
operator|==
literal|null
operator|||
operator|!
name|nullsafes
index|[
name|i
index|]
operator|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

