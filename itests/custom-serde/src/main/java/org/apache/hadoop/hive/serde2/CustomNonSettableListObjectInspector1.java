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
package|;
end_package

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
name|hadoop
operator|.
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|ListObjectInspector
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

begin_class
specifier|public
class|class
name|CustomNonSettableListObjectInspector1
implements|implements
name|ListObjectInspector
block|{
specifier|private
name|ObjectInspector
name|listElementObjectInspector
decl_stmt|;
specifier|protected
name|CustomNonSettableListObjectInspector1
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|CustomNonSettableListObjectInspector1
parameter_list|(
name|ObjectInspector
name|listElementObjectInspector
parameter_list|)
block|{
name|this
operator|.
name|listElementObjectInspector
operator|=
name|listElementObjectInspector
expr_stmt|;
block|}
specifier|public
specifier|final
name|Category
name|getCategory
parameter_list|()
block|{
return|return
name|Category
operator|.
name|LIST
return|;
block|}
comment|// without data
specifier|public
name|ObjectInspector
name|getListElementObjectInspector
parameter_list|()
block|{
return|return
name|listElementObjectInspector
return|;
block|}
comment|// Not supported for the test case
specifier|public
name|Object
name|getListElement
parameter_list|(
name|Object
name|data
parameter_list|,
name|int
name|index
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
comment|// Not supported for the test case
specifier|public
name|int
name|getListLength
parameter_list|(
name|Object
name|data
parameter_list|)
block|{
return|return
literal|0
return|;
block|}
comment|// Not supported for the test case
specifier|public
name|List
argument_list|<
name|?
argument_list|>
name|getList
parameter_list|(
name|Object
name|data
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
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
name|LIST_TYPE_NAME
operator|+
literal|"<"
operator|+
name|listElementObjectInspector
operator|.
name|getTypeName
argument_list|()
operator|+
literal|">"
return|;
block|}
block|}
end_class

end_unit

