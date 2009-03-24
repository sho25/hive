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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * LazySimpleStructObjectInspector works on struct data that is stored in LazyStruct.  *   * The names of the struct fields and the internal structure of the struct fields  * are specified in the ctor of the LazySimpleStructObjectInspector.  *   * Always use the ObjectInspectorFactory to create new ObjectInspector objects, instead  * of directly creating an instance of this class.  */
end_comment

begin_class
specifier|public
class|class
name|LazySimpleStructObjectInspector
implements|implements
name|StructObjectInspector
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
name|LazySimpleStructObjectInspector
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
class|class
name|MyField
implements|implements
name|StructField
block|{
specifier|protected
name|int
name|fieldID
decl_stmt|;
specifier|protected
name|String
name|fieldName
decl_stmt|;
specifier|protected
name|ObjectInspector
name|fieldObjectInspector
decl_stmt|;
specifier|public
name|MyField
parameter_list|(
name|int
name|fieldID
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|ObjectInspector
name|fieldObjectInspector
parameter_list|)
block|{
name|this
operator|.
name|fieldID
operator|=
name|fieldID
expr_stmt|;
name|this
operator|.
name|fieldName
operator|=
name|fieldName
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
name|this
operator|.
name|fieldObjectInspector
operator|=
name|fieldObjectInspector
expr_stmt|;
block|}
specifier|public
name|int
name|getFieldID
parameter_list|()
block|{
return|return
name|fieldID
return|;
block|}
specifier|public
name|String
name|getFieldName
parameter_list|()
block|{
return|return
name|fieldName
return|;
block|}
specifier|public
name|ObjectInspector
name|getFieldObjectInspector
parameter_list|()
block|{
return|return
name|fieldObjectInspector
return|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|""
operator|+
name|fieldID
operator|+
literal|":"
operator|+
name|fieldName
return|;
block|}
block|}
specifier|protected
name|List
argument_list|<
name|MyField
argument_list|>
name|fields
decl_stmt|;
annotation|@
name|Override
specifier|public
name|String
name|getTypeName
parameter_list|()
block|{
return|return
name|ObjectInspectorUtils
operator|.
name|getStandardStructTypeName
argument_list|(
name|this
argument_list|)
return|;
block|}
name|byte
name|separator
decl_stmt|;
name|Text
name|nullSequence
decl_stmt|;
name|boolean
name|lastColumnTakesRest
decl_stmt|;
comment|/** Call ObjectInspectorFactory.getLazySimpleStructObjectInspector instead.    */
specifier|protected
name|LazySimpleStructObjectInspector
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|structFieldNames
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|structFieldObjectInspectors
parameter_list|,
name|byte
name|separator
parameter_list|,
name|Text
name|nullSequence
parameter_list|,
name|boolean
name|lastColumnTakesRest
parameter_list|)
block|{
name|init
argument_list|(
name|structFieldNames
argument_list|,
name|structFieldObjectInspectors
argument_list|,
name|separator
argument_list|,
name|nullSequence
argument_list|,
name|lastColumnTakesRest
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|init
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|structFieldNames
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|structFieldObjectInspectors
parameter_list|,
name|byte
name|separator
parameter_list|,
name|Text
name|nullSequence
parameter_list|,
name|boolean
name|lastColumnTakesRest
parameter_list|)
block|{
assert|assert
operator|(
name|structFieldNames
operator|.
name|size
argument_list|()
operator|==
name|structFieldObjectInspectors
operator|.
name|size
argument_list|()
operator|)
assert|;
name|this
operator|.
name|separator
operator|=
name|separator
expr_stmt|;
name|this
operator|.
name|nullSequence
operator|=
name|nullSequence
expr_stmt|;
name|this
operator|.
name|lastColumnTakesRest
operator|=
name|lastColumnTakesRest
expr_stmt|;
name|fields
operator|=
operator|new
name|ArrayList
argument_list|<
name|MyField
argument_list|>
argument_list|(
name|structFieldNames
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|structFieldNames
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|fields
operator|.
name|add
argument_list|(
operator|new
name|MyField
argument_list|(
name|i
argument_list|,
name|structFieldNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|structFieldObjectInspectors
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|LazySimpleStructObjectInspector
parameter_list|(
name|List
argument_list|<
name|StructField
argument_list|>
name|fields
parameter_list|,
name|byte
name|separator
parameter_list|,
name|Text
name|nullSequence
parameter_list|)
block|{
name|init
argument_list|(
name|fields
argument_list|,
name|separator
argument_list|,
name|nullSequence
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|init
parameter_list|(
name|List
argument_list|<
name|StructField
argument_list|>
name|fields
parameter_list|,
name|byte
name|separator
parameter_list|,
name|Text
name|nullSequence
parameter_list|)
block|{
name|this
operator|.
name|separator
operator|=
name|separator
expr_stmt|;
name|this
operator|.
name|nullSequence
operator|=
name|nullSequence
expr_stmt|;
name|this
operator|.
name|fields
operator|=
operator|new
name|ArrayList
argument_list|<
name|MyField
argument_list|>
argument_list|(
name|fields
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fields
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|this
operator|.
name|fields
operator|.
name|add
argument_list|(
operator|new
name|MyField
argument_list|(
name|i
argument_list|,
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldName
argument_list|()
argument_list|,
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|STRUCT
return|;
block|}
comment|// Without Data
annotation|@
name|Override
specifier|public
name|StructField
name|getStructFieldRef
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
return|return
name|ObjectInspectorUtils
operator|.
name|getStandardStructFieldRef
argument_list|(
name|fieldName
argument_list|,
name|fields
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|getAllStructFieldRefs
parameter_list|()
block|{
return|return
name|fields
return|;
block|}
comment|// With Data
annotation|@
name|Override
specifier|public
name|Object
name|getStructFieldData
parameter_list|(
name|Object
name|data
parameter_list|,
name|StructField
name|fieldRef
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
name|LazyStruct
name|struct
init|=
operator|(
name|LazyStruct
operator|)
name|data
decl_stmt|;
name|MyField
name|f
init|=
operator|(
name|MyField
operator|)
name|fieldRef
decl_stmt|;
name|int
name|fieldID
init|=
name|f
operator|.
name|getFieldID
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|fieldID
operator|>=
literal|0
operator|&&
name|fieldID
operator|<
name|fields
operator|.
name|size
argument_list|()
operator|)
assert|;
return|return
name|struct
operator|.
name|getField
argument_list|(
name|fieldID
argument_list|,
name|separator
argument_list|,
name|nullSequence
argument_list|,
name|lastColumnTakesRest
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Object
argument_list|>
name|getStructFieldsDataAsList
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
name|LazyStruct
name|struct
init|=
operator|(
name|LazyStruct
operator|)
name|data
decl_stmt|;
return|return
name|struct
operator|.
name|getFieldsAsList
argument_list|(
name|separator
argument_list|,
name|nullSequence
argument_list|,
name|lastColumnTakesRest
argument_list|)
return|;
block|}
block|}
end_class

end_unit

