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
name|Vector
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
operator|.
name|MapJoinObjectCtx
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
name|Text
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
name|lazy
operator|.
name|LazyObject
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
name|lazy
operator|.
name|LazyStruct
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
name|StructField
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
name|StructObjectInspector
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

begin_comment
comment|/**  * Map Join Object used for both key and value  */
end_comment

begin_class
specifier|public
class|class
name|MapJoinObjectValue
implements|implements
name|Externalizable
block|{
specifier|transient
specifier|protected
name|int
name|metadataTag
decl_stmt|;
specifier|transient
specifier|protected
name|Vector
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|>
name|obj
decl_stmt|;
specifier|transient
name|Writable
name|val
decl_stmt|;
specifier|public
name|MapJoinObjectValue
parameter_list|()
block|{
name|val
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
block|}
comment|/**    * @param metadataTag    * @param objectTypeTag    * @param obj    */
specifier|public
name|MapJoinObjectValue
parameter_list|(
name|int
name|metadataTag
parameter_list|,
name|Vector
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|>
name|obj
parameter_list|)
block|{
name|val
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
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
return|return
literal|true
return|;
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
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
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
name|MapJoinObjectCtx
name|ctx
init|=
name|MapJoinOperator
operator|.
name|getMapMetadata
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
name|int
name|sz
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|Vector
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|>
name|res
init|=
operator|new
name|Vector
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
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
name|res
operator|.
name|add
argument_list|(
name|memObj
argument_list|)
expr_stmt|;
block|}
name|obj
operator|=
name|res
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
name|MapJoinObjectCtx
name|ctx
init|=
name|MapJoinOperator
operator|.
name|getMapMetadata
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
name|Vector
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|>
name|v
init|=
operator|(
name|Vector
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|>
operator|)
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
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|v
operator|.
name|size
argument_list|()
condition|;
name|pos
operator|++
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
name|v
operator|.
name|get
argument_list|(
name|pos
argument_list|)
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
comment|/**    * @param metadataTag the metadataTag to set    */
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
name|Vector
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|>
name|getObj
parameter_list|()
block|{
return|return
name|obj
return|;
block|}
comment|/**    * @param obj the obj to set    */
specifier|public
name|void
name|setObj
parameter_list|(
name|Vector
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
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

