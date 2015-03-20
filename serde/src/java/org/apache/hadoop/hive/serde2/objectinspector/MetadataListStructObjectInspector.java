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
name|Collections
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
name|concurrent
operator|.
name|ConcurrentHashMap
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
name|ColumnSet
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
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
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
name|PrimitiveObjectInspectorFactory
import|;
end_import

begin_comment
comment|/**  * StructObjectInspector works on struct data that is stored as a Java List or  * Java Array object. Basically, the fields are stored sequentially in the List  * object.  *  * The names of the struct fields and the internal structure of the struct  * fields are specified in the ctor of the StructObjectInspector.  *  */
end_comment

begin_class
specifier|public
class|class
name|MetadataListStructObjectInspector
extends|extends
name|StandardStructObjectInspector
block|{
specifier|static
name|ConcurrentHashMap
argument_list|<
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|,
name|MetadataListStructObjectInspector
argument_list|>
name|cached
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|,
name|MetadataListStructObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
comment|// public static MetadataListStructObjectInspector getInstance(int fields) {
comment|// return getInstance(ObjectInspectorUtils.getIntegerArray(fields));
comment|// }
specifier|public
specifier|static
name|MetadataListStructObjectInspector
name|getInstance
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|key
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|key
operator|.
name|add
argument_list|(
name|columnNames
argument_list|)
expr_stmt|;
name|MetadataListStructObjectInspector
name|result
init|=
name|cached
operator|.
name|get
argument_list|(
name|columnNames
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|result
operator|=
operator|new
name|MetadataListStructObjectInspector
argument_list|(
name|columnNames
argument_list|)
expr_stmt|;
name|MetadataListStructObjectInspector
name|prev
init|=
name|cached
operator|.
name|putIfAbsent
argument_list|(
name|key
argument_list|,
name|result
argument_list|)
decl_stmt|;
if|if
condition|(
name|prev
operator|!=
literal|null
condition|)
block|{
name|result
operator|=
name|prev
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
specifier|public
specifier|static
name|MetadataListStructObjectInspector
name|getInstance
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columnComments
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|key
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|addAll
argument_list|(
name|key
argument_list|,
name|columnNames
argument_list|,
name|columnComments
argument_list|)
expr_stmt|;
name|MetadataListStructObjectInspector
name|result
init|=
name|cached
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|result
operator|=
operator|new
name|MetadataListStructObjectInspector
argument_list|(
name|columnNames
argument_list|,
name|columnComments
argument_list|)
expr_stmt|;
name|MetadataListStructObjectInspector
name|prev
init|=
name|cached
operator|.
name|putIfAbsent
argument_list|(
name|key
argument_list|,
name|result
argument_list|)
decl_stmt|;
if|if
condition|(
name|prev
operator|!=
literal|null
condition|)
block|{
name|result
operator|=
name|prev
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
specifier|static
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|getFieldObjectInspectors
parameter_list|(
name|int
name|fields
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|r
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|fields
argument_list|)
decl_stmt|;
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
condition|;
name|i
operator|++
control|)
block|{
name|r
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|STRING
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|r
return|;
block|}
specifier|protected
name|MetadataListStructObjectInspector
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
name|MetadataListStructObjectInspector
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
parameter_list|)
block|{
name|super
argument_list|(
name|columnNames
argument_list|,
name|getFieldObjectInspectors
argument_list|(
name|columnNames
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MetadataListStructObjectInspector
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columnComments
parameter_list|)
block|{
name|super
argument_list|(
name|columnNames
argument_list|,
name|getFieldObjectInspectors
argument_list|(
name|columnNames
operator|.
name|size
argument_list|()
argument_list|)
argument_list|,
name|columnComments
argument_list|)
expr_stmt|;
block|}
comment|// Get col object out
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
operator|instanceof
name|ColumnSet
condition|)
block|{
name|data
operator|=
operator|(
operator|(
name|ColumnSet
operator|)
name|data
operator|)
operator|.
name|col
expr_stmt|;
block|}
return|return
name|super
operator|.
name|getStructFieldData
argument_list|(
name|data
argument_list|,
name|fieldRef
argument_list|)
return|;
block|}
comment|// Get col object out
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
operator|instanceof
name|ColumnSet
condition|)
block|{
name|data
operator|=
operator|(
operator|(
name|ColumnSet
operator|)
name|data
operator|)
operator|.
name|col
expr_stmt|;
block|}
return|return
name|super
operator|.
name|getStructFieldsDataAsList
argument_list|(
name|data
argument_list|)
return|;
block|}
block|}
end_class

end_unit

