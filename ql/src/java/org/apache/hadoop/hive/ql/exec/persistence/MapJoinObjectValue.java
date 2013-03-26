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
name|Externalizable
import|;
end_import

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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
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
name|io
operator|.
name|ShortWritable
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
comment|/**  * Map Join Object used for both key and value.  */
end_comment

begin_class
specifier|public
class|class
name|MapJoinObjectValue
implements|implements
name|Externalizable
block|{
specifier|protected
specifier|transient
name|int
name|metadataTag
decl_stmt|;
specifier|protected
specifier|transient
name|MapJoinRowContainer
argument_list|<
name|Object
index|[]
argument_list|>
name|obj
decl_stmt|;
specifier|protected
specifier|transient
name|byte
name|aliasFilter
init|=
operator|(
name|byte
operator|)
literal|0xff
decl_stmt|;
specifier|public
name|MapJoinObjectValue
parameter_list|()
block|{    }
comment|/**    * @param metadataTag    * @param obj    */
specifier|public
name|MapJoinObjectValue
parameter_list|(
name|int
name|metadataTag
parameter_list|,
name|MapJoinRowContainer
argument_list|<
name|Object
index|[]
argument_list|>
name|obj
parameter_list|)
block|{
name|this
operator|.
name|metadataTag
operator|=
name|metadataTag
expr_stmt|;
name|this
operator|.
name|obj
operator|=
name|obj
expr_stmt|;
block|}
specifier|public
name|byte
name|getAliasFilter
parameter_list|()
block|{
return|return
name|aliasFilter
return|;
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
name|MapJoinObjectValue
condition|)
block|{
name|MapJoinObjectValue
name|mObj
init|=
operator|(
name|MapJoinObjectValue
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|mObj
operator|.
name|getMetadataTag
argument_list|()
operator|==
name|metadataTag
condition|)
block|{
if|if
condition|(
operator|(
name|obj
operator|==
literal|null
operator|)
operator|&&
operator|(
name|mObj
operator|.
name|getObj
argument_list|()
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
name|mObj
operator|.
name|getObj
argument_list|()
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|mObj
operator|.
name|getObj
argument_list|()
operator|.
name|equals
argument_list|(
name|obj
argument_list|)
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
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
operator|(
name|obj
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|obj
operator|.
name|hashCode
argument_list|()
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
name|metadataTag
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
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
name|int
name|sz
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|MapJoinRowContainer
argument_list|<
name|Object
index|[]
argument_list|>
name|res
init|=
operator|new
name|MapJoinRowContainer
argument_list|<
name|Object
index|[]
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|sz
operator|>
literal|0
condition|)
block|{
name|int
name|numCols
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|numCols
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|sz
condition|;
name|pos
operator|++
control|)
block|{
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
name|memObj
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
name|memObj
operator|==
literal|null
condition|)
block|{
name|res
operator|.
name|add
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|0
argument_list|)
operator|.
name|toArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Object
index|[]
name|array
init|=
name|memObj
operator|.
name|toArray
argument_list|()
decl_stmt|;
name|res
operator|.
name|add
argument_list|(
name|array
argument_list|)
expr_stmt|;
if|if
condition|(
name|ctx
operator|.
name|hasFilterTag
argument_list|()
condition|)
block|{
name|aliasFilter
operator|&=
operator|(
operator|(
name|ShortWritable
operator|)
name|array
index|[
name|array
operator|.
name|length
operator|-
literal|1
index|]
operator|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
else|else
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
name|sz
condition|;
name|i
operator|++
control|)
block|{
name|res
operator|.
name|add
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|0
argument_list|)
operator|.
name|toArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|obj
operator|=
name|res
expr_stmt|;
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
name|out
operator|.
name|writeInt
argument_list|(
name|metadataTag
argument_list|)
expr_stmt|;
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
comment|// Different processing for key and value
name|MapJoinRowContainer
argument_list|<
name|Object
index|[]
argument_list|>
name|v
init|=
name|obj
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|v
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|v
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|Object
index|[]
name|row
init|=
name|v
operator|.
name|first
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|row
operator|.
name|length
argument_list|)
expr_stmt|;
if|if
condition|(
name|row
operator|.
name|length
operator|>
literal|0
condition|)
block|{
for|for
control|(
init|;
name|row
operator|!=
literal|null
condition|;
name|row
operator|=
name|v
operator|.
name|next
argument_list|()
control|)
block|{
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
name|row
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
block|}
block|}
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
catch|catch
parameter_list|(
name|HiveException
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
comment|/**    * @return the metadataTag    */
specifier|public
name|int
name|getMetadataTag
parameter_list|()
block|{
return|return
name|metadataTag
return|;
block|}
comment|/**    * @param metadataTag    *          the metadataTag to set    */
specifier|public
name|void
name|setMetadataTag
parameter_list|(
name|int
name|metadataTag
parameter_list|)
block|{
name|this
operator|.
name|metadataTag
operator|=
name|metadataTag
expr_stmt|;
block|}
comment|/**    * @return the obj    */
specifier|public
name|MapJoinRowContainer
argument_list|<
name|Object
index|[]
argument_list|>
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
name|MapJoinRowContainer
argument_list|<
name|Object
index|[]
argument_list|>
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
block|}
end_class

end_unit

