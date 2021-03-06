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
name|Map
import|;
end_import

begin_comment
comment|/*  * Assumes that a getMapValueElement on object2 will work with a key from  * object1. The equality is implemented fully, the greater-than/less-than  * values do not implement a transitive relation.   */
end_comment

begin_class
specifier|public
class|class
name|SimpleMapEqualComparer
implements|implements
name|MapEqualComparer
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Object
name|o1
parameter_list|,
name|MapObjectInspector
name|moi1
parameter_list|,
name|Object
name|o2
parameter_list|,
name|MapObjectInspector
name|moi2
parameter_list|)
block|{
name|int
name|mapsize1
init|=
name|moi1
operator|.
name|getMapSize
argument_list|(
name|o1
argument_list|)
decl_stmt|;
name|int
name|mapsize2
init|=
name|moi2
operator|.
name|getMapSize
argument_list|(
name|o2
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapsize1
operator|!=
name|mapsize2
condition|)
block|{
return|return
name|mapsize1
operator|-
name|mapsize2
return|;
block|}
name|ObjectInspector
name|mvoi1
init|=
name|moi1
operator|.
name|getMapValueObjectInspector
argument_list|()
decl_stmt|;
name|ObjectInspector
name|mvoi2
init|=
name|moi2
operator|.
name|getMapValueObjectInspector
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|map1
init|=
name|moi1
operator|.
name|getMap
argument_list|(
name|o1
argument_list|)
decl_stmt|;
for|for
control|(
name|Object
name|mk1
range|:
name|map1
operator|.
name|keySet
argument_list|()
control|)
block|{
name|int
name|rc
init|=
name|ObjectInspectorUtils
operator|.
name|compare
argument_list|(
name|moi1
operator|.
name|getMapValueElement
argument_list|(
name|o1
argument_list|,
name|mk1
argument_list|)
argument_list|,
name|mvoi1
argument_list|,
name|moi2
operator|.
name|getMapValueElement
argument_list|(
name|o2
argument_list|,
name|mk1
argument_list|)
argument_list|,
name|mvoi2
argument_list|,
name|this
argument_list|)
decl_stmt|;
if|if
condition|(
name|rc
operator|!=
literal|0
condition|)
block|{
return|return
name|rc
return|;
block|}
block|}
return|return
literal|0
return|;
block|}
block|}
end_class

end_unit

