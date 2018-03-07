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
name|ql
operator|.
name|exec
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
name|objectinspector
operator|.
name|ListObjectsEqualComparer
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
name|ObjectInspector
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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|StringObjectInspector
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
name|typeinfo
operator|.
name|TypeInfoFactory
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
name|typeinfo
operator|.
name|TypeInfoUtils
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

begin_class
specifier|public
class|class
name|KeyWrapperFactory
block|{
specifier|public
name|KeyWrapperFactory
parameter_list|(
name|ExprNodeEvaluator
index|[]
name|keyFields
parameter_list|,
name|ObjectInspector
index|[]
name|keyObjectInspectors
parameter_list|,
name|ObjectInspector
index|[]
name|currentKeyObjectInspectors
parameter_list|)
block|{
name|this
operator|.
name|keyFields
operator|=
name|keyFields
expr_stmt|;
name|this
operator|.
name|keyObjectInspectors
operator|=
name|keyObjectInspectors
expr_stmt|;
name|this
operator|.
name|currentKeyObjectInspectors
operator|=
name|currentKeyObjectInspectors
expr_stmt|;
block|}
specifier|public
name|KeyWrapper
name|getKeyWrapper
parameter_list|()
block|{
if|if
condition|(
name|keyFields
operator|.
name|length
operator|==
literal|1
operator|&&
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|keyObjectInspectors
index|[
literal|0
index|]
argument_list|)
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
condition|)
block|{
assert|assert
operator|(
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|currentKeyObjectInspectors
index|[
literal|0
index|]
argument_list|)
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
operator|)
assert|;
name|soi_new
operator|=
operator|(
name|StringObjectInspector
operator|)
name|keyObjectInspectors
index|[
literal|0
index|]
expr_stmt|;
name|soi_copy
operator|=
operator|(
name|StringObjectInspector
operator|)
name|currentKeyObjectInspectors
index|[
literal|0
index|]
expr_stmt|;
return|return
operator|new
name|TextKeyWrapper
argument_list|(
literal|false
argument_list|)
return|;
block|}
else|else
block|{
name|currentStructEqualComparer
operator|=
operator|new
name|ListObjectsEqualComparer
argument_list|(
name|currentKeyObjectInspectors
argument_list|,
name|currentKeyObjectInspectors
argument_list|)
expr_stmt|;
name|newKeyStructEqualComparer
operator|=
operator|new
name|ListObjectsEqualComparer
argument_list|(
name|currentKeyObjectInspectors
argument_list|,
name|keyObjectInspectors
argument_list|)
expr_stmt|;
return|return
operator|new
name|ListKeyWrapper
argument_list|(
literal|false
argument_list|)
return|;
block|}
block|}
specifier|transient
name|ExprNodeEvaluator
index|[]
name|keyFields
decl_stmt|;
specifier|transient
name|ObjectInspector
index|[]
name|keyObjectInspectors
decl_stmt|;
specifier|transient
name|ObjectInspector
index|[]
name|currentKeyObjectInspectors
decl_stmt|;
specifier|transient
name|ListObjectsEqualComparer
name|currentStructEqualComparer
decl_stmt|;
specifier|transient
name|ListObjectsEqualComparer
name|newKeyStructEqualComparer
decl_stmt|;
class|class
name|ListKeyWrapper
extends|extends
name|KeyWrapper
block|{
name|int
name|hashcode
init|=
operator|-
literal|1
decl_stmt|;
name|Object
index|[]
name|keys
decl_stmt|;
comment|// decide whether this is already in hashmap (keys in hashmap are deepcopied
comment|// version, and we need to use 'currentKeyObjectInspector').
name|ListObjectsEqualComparer
name|equalComparer
decl_stmt|;
specifier|public
name|ListKeyWrapper
parameter_list|(
name|boolean
name|isCopy
parameter_list|)
block|{
name|this
argument_list|(
operator|-
literal|1
argument_list|,
operator|new
name|Object
index|[
name|keyFields
operator|.
name|length
index|]
argument_list|,
name|isCopy
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ListKeyWrapper
parameter_list|(
name|int
name|hashcode
parameter_list|,
name|Object
index|[]
name|copiedKeys
parameter_list|,
name|boolean
name|isCopy
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|hashcode
operator|=
name|hashcode
expr_stmt|;
name|keys
operator|=
name|copiedKeys
expr_stmt|;
name|setEqualComparer
argument_list|(
name|isCopy
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setEqualComparer
parameter_list|(
name|boolean
name|copy
parameter_list|)
block|{
if|if
condition|(
operator|!
name|copy
condition|)
block|{
name|equalComparer
operator|=
name|newKeyStructEqualComparer
expr_stmt|;
block|}
else|else
block|{
name|equalComparer
operator|=
name|currentStructEqualComparer
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|hashcode
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
operator|!
operator|(
name|obj
operator|instanceof
name|ListKeyWrapper
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ListKeyWrapper
name|other
init|=
operator|(
operator|(
name|ListKeyWrapper
operator|)
name|obj
operator|)
decl_stmt|;
if|if
condition|(
name|other
operator|.
name|hashcode
operator|!=
name|this
operator|.
name|hashcode
operator|&&
name|this
operator|.
name|hashcode
operator|!=
operator|-
literal|1
operator|&&
name|other
operator|.
name|hashcode
operator|!=
operator|-
literal|1
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Object
index|[]
name|copied_in_hashmap
init|=
name|other
operator|.
name|keys
decl_stmt|;
name|boolean
name|result
init|=
name|equalComparer
operator|.
name|areEqual
argument_list|(
name|copied_in_hashmap
argument_list|,
name|keys
argument_list|)
decl_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setHashKey
parameter_list|()
block|{
name|hashcode
operator|=
name|ObjectInspectorUtils
operator|.
name|writableArrayHashCode
argument_list|(
name|keys
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|getNewKey
parameter_list|(
name|Object
name|row
parameter_list|,
name|ObjectInspector
name|rowInspector
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Compute the keys
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|keyFields
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|keys
index|[
name|i
index|]
operator|=
name|keyFields
index|[
name|i
index|]
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
name|hashcode
operator|=
operator|-
literal|1
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|KeyWrapper
name|copyKey
parameter_list|()
block|{
name|Object
index|[]
name|newDefaultKeys
init|=
name|deepCopyElements
argument_list|(
name|keys
argument_list|,
name|keyObjectInspectors
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
decl_stmt|;
return|return
operator|new
name|ListKeyWrapper
argument_list|(
name|hashcode
argument_list|,
name|newDefaultKeys
argument_list|,
literal|true
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|copyKey
parameter_list|(
name|KeyWrapper
name|oldWrapper
parameter_list|)
block|{
name|ListKeyWrapper
name|listWrapper
init|=
operator|(
name|ListKeyWrapper
operator|)
name|oldWrapper
decl_stmt|;
name|hashcode
operator|=
name|listWrapper
operator|.
name|hashcode
expr_stmt|;
name|equalComparer
operator|=
name|currentStructEqualComparer
expr_stmt|;
name|deepCopyElements
argument_list|(
name|listWrapper
operator|.
name|keys
argument_list|,
name|keyObjectInspectors
argument_list|,
name|keys
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
index|[]
name|getKeyArray
parameter_list|()
block|{
return|return
name|keys
return|;
block|}
specifier|private
name|Object
index|[]
name|deepCopyElements
parameter_list|(
name|Object
index|[]
name|keys
parameter_list|,
name|ObjectInspector
index|[]
name|keyObjectInspectors
parameter_list|,
name|ObjectInspectorCopyOption
name|copyOption
parameter_list|)
block|{
name|Object
index|[]
name|result
init|=
operator|new
name|Object
index|[
name|keys
operator|.
name|length
index|]
decl_stmt|;
name|deepCopyElements
argument_list|(
name|keys
argument_list|,
name|keyObjectInspectors
argument_list|,
name|result
argument_list|,
name|copyOption
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|private
name|void
name|deepCopyElements
parameter_list|(
name|Object
index|[]
name|keys
parameter_list|,
name|ObjectInspector
index|[]
name|keyObjectInspectors
parameter_list|,
name|Object
index|[]
name|result
parameter_list|,
name|ObjectInspectorCopyOption
name|copyOption
parameter_list|)
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
name|keys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|result
index|[
name|i
index|]
operator|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|,
name|keyObjectInspectors
index|[
name|i
index|]
argument_list|,
name|copyOption
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|transient
name|Object
index|[]
name|singleEleArray
init|=
operator|new
name|Object
index|[
literal|1
index|]
decl_stmt|;
specifier|transient
name|StringObjectInspector
name|soi_new
decl_stmt|,
name|soi_copy
decl_stmt|;
class|class
name|TextKeyWrapper
extends|extends
name|KeyWrapper
block|{
name|int
name|hashcode
decl_stmt|;
name|Object
name|key
decl_stmt|;
name|boolean
name|isCopy
decl_stmt|;
specifier|public
name|TextKeyWrapper
parameter_list|(
name|boolean
name|isCopy
parameter_list|)
block|{
name|this
argument_list|(
operator|-
literal|1
argument_list|,
literal|null
argument_list|,
name|isCopy
argument_list|)
expr_stmt|;
block|}
specifier|private
name|TextKeyWrapper
parameter_list|(
name|int
name|hashcode
parameter_list|,
name|Object
name|key
parameter_list|,
name|boolean
name|isCopy
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|hashcode
operator|=
name|hashcode
expr_stmt|;
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
name|this
operator|.
name|isCopy
operator|=
name|isCopy
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
name|hashcode
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|other
operator|instanceof
name|TextKeyWrapper
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Object
name|obj
init|=
operator|(
operator|(
name|TextKeyWrapper
operator|)
name|other
operator|)
operator|.
name|key
decl_stmt|;
name|Text
name|t1
decl_stmt|;
name|Text
name|t2
decl_stmt|;
if|if
condition|(
name|isCopy
condition|)
block|{
name|t1
operator|=
name|soi_copy
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|t2
operator|=
name|soi_copy
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|obj
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|t1
operator|=
name|soi_new
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|t2
operator|=
name|soi_copy
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|obj
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|t1
operator|==
literal|null
operator|&&
name|t2
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
name|t1
operator|==
literal|null
operator|||
name|t2
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
else|else
block|{
return|return
name|t1
operator|.
name|equals
argument_list|(
name|t2
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setHashKey
parameter_list|()
block|{
if|if
condition|(
name|key
operator|==
literal|null
condition|)
block|{
name|hashcode
operator|=
literal|0
expr_stmt|;
block|}
else|else
block|{
name|hashcode
operator|=
name|key
operator|.
name|hashCode
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|getNewKey
parameter_list|(
name|Object
name|row
parameter_list|,
name|ObjectInspector
name|rowInspector
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Compute the keys
name|key
operator|=
name|keyFields
index|[
literal|0
index|]
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|KeyWrapper
name|copyKey
parameter_list|()
block|{
return|return
operator|new
name|TextKeyWrapper
argument_list|(
name|hashcode
argument_list|,
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|key
argument_list|,
name|soi_new
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
argument_list|,
literal|true
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|copyKey
parameter_list|(
name|KeyWrapper
name|oldWrapper
parameter_list|)
block|{
name|TextKeyWrapper
name|textWrapper
init|=
operator|(
name|TextKeyWrapper
operator|)
name|oldWrapper
decl_stmt|;
name|hashcode
operator|=
name|textWrapper
operator|.
name|hashcode
expr_stmt|;
name|isCopy
operator|=
literal|true
expr_stmt|;
name|key
operator|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|textWrapper
operator|.
name|key
argument_list|,
name|soi_new
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
index|[]
name|getKeyArray
parameter_list|()
block|{
name|singleEleArray
index|[
literal|0
index|]
operator|=
name|key
expr_stmt|;
return|return
name|singleEleArray
return|;
block|}
block|}
block|}
end_class

end_unit

