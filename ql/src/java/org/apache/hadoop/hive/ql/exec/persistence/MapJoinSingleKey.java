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
name|MapJoinMetaData
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

begin_class
specifier|public
class|class
name|MapJoinSingleKey
extends|extends
name|AbstractMapJoinKey
block|{
specifier|protected
specifier|transient
name|Object
name|obj
decl_stmt|;
specifier|public
name|MapJoinSingleKey
parameter_list|()
block|{   }
comment|/**    * @param obj    */
specifier|public
name|MapJoinSingleKey
parameter_list|(
name|Object
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
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|instanceof
name|MapJoinSingleKey
condition|)
block|{
name|MapJoinSingleKey
name|mObj
init|=
operator|(
name|MapJoinSingleKey
operator|)
name|o
decl_stmt|;
name|Object
name|key
init|=
name|mObj
operator|.
name|getObj
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|obj
operator|==
literal|null
operator|)
operator|&&
operator|(
name|key
operator|==
literal|null
operator|)
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|(
name|obj
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|key
operator|!=
literal|null
operator|)
condition|)
block|{
if|if
condition|(
name|obj
operator|.
name|equals
argument_list|(
name|key
argument_list|)
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
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|hashCode
decl_stmt|;
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
name|hashCode
operator|=
name|metadataTag
expr_stmt|;
block|}
else|else
block|{
name|hashCode
operator|=
literal|31
operator|+
name|obj
operator|.
name|hashCode
argument_list|()
expr_stmt|;
block|}
return|return
name|hashCode
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
name|MapJoinMetaData
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
literal|null
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"read empty back"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|obj
operator|=
name|list
operator|.
name|get
argument_list|(
literal|0
argument_list|)
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
comment|// out.writeInt(metadataTag);
comment|// get the tableDesc from the map stored in the mapjoin operator
name|HashTableSinkObjectCtx
name|ctx
init|=
name|MapJoinMetaData
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
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|list
init|=
name|MapJoinMetaData
operator|.
name|getList
argument_list|()
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|obj
argument_list|)
expr_stmt|;
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
name|list
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
literal|0
index|]
operator|)
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

