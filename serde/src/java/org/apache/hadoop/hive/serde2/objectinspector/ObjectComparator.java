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
name|Comparator
import|;
end_import

begin_import
import|import static
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
name|NullValueOption
import|;
end_import

begin_comment
comment|/**  * This class wraps the ObjectInspectorUtils.compare method and implements java.util.Comparator.  */
end_comment

begin_class
specifier|public
class|class
name|ObjectComparator
implements|implements
name|Comparator
argument_list|<
name|Object
argument_list|>
block|{
specifier|private
specifier|final
name|ObjectInspector
name|objectInspector1
decl_stmt|;
specifier|private
specifier|final
name|ObjectInspector
name|objectInspector2
decl_stmt|;
specifier|private
specifier|final
name|NullValueOption
name|nullSortOrder
decl_stmt|;
specifier|private
specifier|final
name|MapEqualComparer
name|mapEqualComparer
init|=
operator|new
name|FullMapEqualComparer
argument_list|()
decl_stmt|;
specifier|public
name|ObjectComparator
parameter_list|(
name|ObjectInspector
name|objectInspector1
parameter_list|,
name|ObjectInspector
name|objectInspector2
parameter_list|,
name|NullValueOption
name|nullSortOrder
parameter_list|)
block|{
name|this
operator|.
name|objectInspector1
operator|=
name|objectInspector1
expr_stmt|;
name|this
operator|.
name|objectInspector2
operator|=
name|objectInspector2
expr_stmt|;
name|this
operator|.
name|nullSortOrder
operator|=
name|nullSortOrder
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Object
name|o1
parameter_list|,
name|Object
name|o2
parameter_list|)
block|{
return|return
name|ObjectInspectorUtils
operator|.
name|compare
argument_list|(
name|o1
argument_list|,
name|objectInspector1
argument_list|,
name|o2
argument_list|,
name|objectInspector2
argument_list|,
name|mapEqualComparer
argument_list|,
name|nullSortOrder
argument_list|)
return|;
block|}
block|}
end_class

end_unit

