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
name|serde2
operator|.
name|lazy
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
name|Map
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|LazyMap
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
name|MapObjectInspector
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * LazyMapObjectInspector works on struct data that is stored in LazyStruct.  *  * Always use the ObjectInspectorFactory to create new ObjectInspector objects,  * instead of directly creating an instance of this class.  */
end_comment

begin_class
specifier|public
class|class
name|LazyMapObjectInspector
implements|implements
name|MapObjectInspector
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|LazyMapObjectInspector
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|ObjectInspector
name|mapKeyObjectInspector
decl_stmt|;
specifier|private
name|ObjectInspector
name|mapValueObjectInspector
decl_stmt|;
specifier|private
name|byte
name|itemSeparator
decl_stmt|;
specifier|private
name|byte
name|keyValueSeparator
decl_stmt|;
specifier|private
name|Text
name|nullSequence
decl_stmt|;
specifier|private
name|boolean
name|escaped
decl_stmt|;
specifier|private
name|byte
name|escapeChar
decl_stmt|;
specifier|protected
name|LazyMapObjectInspector
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Call ObjectInspectorFactory.getStandardListObjectInspector instead.    */
specifier|protected
name|LazyMapObjectInspector
parameter_list|(
name|ObjectInspector
name|mapKeyObjectInspector
parameter_list|,
name|ObjectInspector
name|mapValueObjectInspector
parameter_list|,
name|byte
name|itemSeparator
parameter_list|,
name|byte
name|keyValueSeparator
parameter_list|,
name|Text
name|nullSequence
parameter_list|,
name|boolean
name|escaped
parameter_list|,
name|byte
name|escapeChar
parameter_list|)
block|{
name|this
operator|.
name|mapKeyObjectInspector
operator|=
name|mapKeyObjectInspector
expr_stmt|;
name|this
operator|.
name|mapValueObjectInspector
operator|=
name|mapValueObjectInspector
expr_stmt|;
name|this
operator|.
name|itemSeparator
operator|=
name|itemSeparator
expr_stmt|;
name|this
operator|.
name|keyValueSeparator
operator|=
name|keyValueSeparator
expr_stmt|;
name|this
operator|.
name|nullSequence
operator|=
name|nullSequence
expr_stmt|;
name|this
operator|.
name|escaped
operator|=
name|escaped
expr_stmt|;
name|this
operator|.
name|escapeChar
operator|=
name|escapeChar
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|Category
name|getCategory
parameter_list|()
block|{
return|return
name|Category
operator|.
name|MAP
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getTypeName
parameter_list|()
block|{
return|return
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|serde
operator|.
name|serdeConstants
operator|.
name|MAP_TYPE_NAME
operator|+
literal|"<"
operator|+
name|mapKeyObjectInspector
operator|.
name|getTypeName
argument_list|()
operator|+
literal|","
operator|+
name|mapValueObjectInspector
operator|.
name|getTypeName
argument_list|()
operator|+
literal|">"
return|;
block|}
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|getMapKeyObjectInspector
parameter_list|()
block|{
return|return
name|mapKeyObjectInspector
return|;
block|}
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|getMapValueObjectInspector
parameter_list|()
block|{
return|return
name|mapValueObjectInspector
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getMapValueElement
parameter_list|(
name|Object
name|data
parameter_list|,
name|Object
name|key
parameter_list|)
block|{
if|if
condition|(
name|data
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
name|LazyMap
operator|)
name|data
operator|)
operator|.
name|getMapValueElement
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|getMap
parameter_list|(
name|Object
name|data
parameter_list|)
block|{
if|if
condition|(
name|data
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
name|LazyMap
operator|)
name|data
operator|)
operator|.
name|getMap
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getMapSize
parameter_list|(
name|Object
name|data
parameter_list|)
block|{
if|if
condition|(
name|data
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
name|LazyMap
operator|)
name|data
operator|)
operator|.
name|getMapSize
argument_list|()
return|;
block|}
comment|// Called by LazyMap
specifier|public
name|byte
name|getItemSeparator
parameter_list|()
block|{
return|return
name|itemSeparator
return|;
block|}
specifier|public
name|byte
name|getKeyValueSeparator
parameter_list|()
block|{
return|return
name|keyValueSeparator
return|;
block|}
specifier|public
name|Text
name|getNullSequence
parameter_list|()
block|{
return|return
name|nullSequence
return|;
block|}
specifier|public
name|boolean
name|isEscaped
parameter_list|()
block|{
return|return
name|escaped
return|;
block|}
specifier|public
name|byte
name|getEscapeChar
parameter_list|()
block|{
return|return
name|escapeChar
return|;
block|}
block|}
end_class

end_unit

