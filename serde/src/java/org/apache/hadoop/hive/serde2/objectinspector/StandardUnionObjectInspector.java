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
name|serde2
operator|.
name|objectinspector
package|;
end_package

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

begin_comment
comment|/**  * StandardUnionObjectInspector works on union data that is stored as  * UnionObject.  * It holds the list of the object inspectors corresponding to each type of the  * object the Union can hold. The UniobObject has tag followed by the object  * it is holding.  *  * Always use the {@link ObjectInspectorFactory} to create new ObjectInspector  * objects, instead of directly creating an instance of this class.  */
end_comment

begin_class
specifier|public
class|class
name|StandardUnionObjectInspector
extends|extends
name|SettableUnionObjectInspector
block|{
specifier|private
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|ois
decl_stmt|;
specifier|protected
name|StandardUnionObjectInspector
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|StandardUnionObjectInspector
parameter_list|(
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|ois
parameter_list|)
block|{
name|this
operator|.
name|ois
operator|=
name|ois
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|getObjectInspectors
parameter_list|()
block|{
return|return
name|ois
return|;
block|}
specifier|public
specifier|static
class|class
name|StandardUnion
implements|implements
name|UnionObject
block|{
specifier|protected
name|byte
name|tag
decl_stmt|;
specifier|protected
name|Object
name|object
decl_stmt|;
specifier|public
name|StandardUnion
parameter_list|()
block|{     }
specifier|public
name|StandardUnion
parameter_list|(
name|byte
name|tag
parameter_list|,
name|Object
name|object
parameter_list|)
block|{
name|this
operator|.
name|tag
operator|=
name|tag
expr_stmt|;
name|this
operator|.
name|object
operator|=
name|object
expr_stmt|;
block|}
specifier|public
name|void
name|setObject
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|this
operator|.
name|object
operator|=
name|o
expr_stmt|;
block|}
specifier|public
name|void
name|setTag
parameter_list|(
name|byte
name|tag
parameter_list|)
block|{
name|this
operator|.
name|tag
operator|=
name|tag
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getObject
parameter_list|()
block|{
return|return
name|object
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|getTag
parameter_list|()
block|{
return|return
name|tag
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|tag
operator|+
literal|":"
operator|+
name|object
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
if|if
condition|(
name|object
operator|==
literal|null
condition|)
block|{
return|return
name|tag
return|;
block|}
else|else
block|{
return|return
name|object
operator|.
name|hashCode
argument_list|()
operator|^
name|tag
return|;
block|}
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
operator|!
operator|(
name|obj
operator|instanceof
name|StandardUnion
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|StandardUnion
name|that
init|=
operator|(
name|StandardUnion
operator|)
name|obj
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|object
operator|==
literal|null
operator|||
name|that
operator|.
name|object
operator|==
literal|null
condition|)
block|{
return|return
name|this
operator|.
name|tag
operator|==
name|that
operator|.
name|tag
operator|&&
name|this
operator|.
name|object
operator|==
name|that
operator|.
name|object
return|;
block|}
else|else
block|{
return|return
name|this
operator|.
name|tag
operator|==
name|that
operator|.
name|tag
operator|&&
name|this
operator|.
name|object
operator|.
name|equals
argument_list|(
name|that
operator|.
name|object
argument_list|)
return|;
block|}
block|}
block|}
comment|/**    * Return the tag of the object.    */
specifier|public
name|byte
name|getTag
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
operator|(
operator|(
name|UnionObject
operator|)
name|o
operator|)
operator|.
name|getTag
argument_list|()
return|;
block|}
comment|/**    * Return the field based on the tag value associated with the Object.    */
specifier|public
name|Object
name|getField
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|(
operator|(
name|UnionObject
operator|)
name|o
operator|)
operator|.
name|getObject
argument_list|()
return|;
block|}
specifier|public
name|Category
name|getCategory
parameter_list|()
block|{
return|return
name|Category
operator|.
name|UNION
return|;
block|}
specifier|public
name|String
name|getTypeName
parameter_list|()
block|{
return|return
name|ObjectInspectorUtils
operator|.
name|getStandardUnionTypeName
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|create
parameter_list|()
block|{
return|return
operator|new
name|StandardUnion
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|setFieldAndTag
parameter_list|(
name|Object
name|union
parameter_list|,
name|Object
name|field
parameter_list|,
name|byte
name|tag
parameter_list|)
block|{
name|StandardUnion
name|unionObject
init|=
operator|(
name|StandardUnion
operator|)
name|union
decl_stmt|;
name|unionObject
operator|.
name|setObject
argument_list|(
name|field
argument_list|)
expr_stmt|;
if|if
condition|(
name|field
operator|==
literal|null
condition|)
block|{
name|unionObject
operator|.
name|setTag
argument_list|(
operator|(
name|byte
operator|)
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|unionObject
operator|.
name|setTag
argument_list|(
name|tag
argument_list|)
expr_stmt|;
block|}
return|return
name|unionObject
return|;
block|}
block|}
end_class

end_unit

