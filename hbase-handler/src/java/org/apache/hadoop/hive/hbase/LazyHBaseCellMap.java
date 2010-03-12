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
name|hbase
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
import|;
end_import

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
name|hadoop
operator|.
name|hbase
operator|.
name|io
operator|.
name|RowResult
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
name|ByteArrayRef
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
name|LazyFactory
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
name|LazyPrimitive
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
name|LazyUtils
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
name|objectinspector
operator|.
name|LazyMapObjectInspector
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
name|PrimitiveObjectInspector
import|;
end_import

begin_comment
comment|/**  * LazyHBaseCellMap refines LazyMap with HBase column mapping.  */
end_comment

begin_class
specifier|public
class|class
name|LazyHBaseCellMap
extends|extends
name|LazyMap
block|{
specifier|private
name|RowResult
name|rowResult
decl_stmt|;
specifier|private
name|String
name|hbaseColumnFamily
decl_stmt|;
comment|/**    * Construct a LazyCellMap object with the ObjectInspector.    * @param oi    */
specifier|public
name|LazyHBaseCellMap
parameter_list|(
name|LazyMapObjectInspector
name|oi
parameter_list|)
block|{
name|super
argument_list|(
name|oi
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|ByteArrayRef
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
block|{
comment|// do nothing
block|}
specifier|public
name|void
name|init
parameter_list|(
name|RowResult
name|rr
parameter_list|,
name|String
name|columnFamily
parameter_list|)
block|{
name|rowResult
operator|=
name|rr
expr_stmt|;
name|hbaseColumnFamily
operator|=
name|columnFamily
expr_stmt|;
name|setParsed
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|parse
parameter_list|()
block|{
if|if
condition|(
name|cachedMap
operator|==
literal|null
condition|)
block|{
name|cachedMap
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|cachedMap
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|Iterator
argument_list|<
name|byte
index|[]
argument_list|>
name|iter
init|=
name|rowResult
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|byte
index|[]
name|columnFamily
init|=
name|hbaseColumnFamily
operator|.
name|getBytes
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|byte
index|[]
name|columnKey
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|columnFamily
operator|.
name|length
operator|>
name|columnKey
operator|.
name|length
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
literal|0
operator|==
name|LazyUtils
operator|.
name|compare
argument_list|(
name|columnFamily
argument_list|,
literal|0
argument_list|,
name|columnFamily
operator|.
name|length
argument_list|,
name|columnKey
argument_list|,
literal|0
argument_list|,
name|columnFamily
operator|.
name|length
argument_list|)
condition|)
block|{
name|byte
index|[]
name|columnValue
init|=
name|rowResult
operator|.
name|get
argument_list|(
name|columnKey
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|columnValue
operator|==
literal|null
operator|||
name|columnValue
operator|.
name|length
operator|==
literal|0
condition|)
block|{
comment|// an empty object
continue|continue;
block|}
comment|// Keys are always primitive
name|LazyPrimitive
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|key
init|=
name|LazyFactory
operator|.
name|createLazyPrimitiveClass
argument_list|(
call|(
name|PrimitiveObjectInspector
call|)
argument_list|(
operator|(
name|MapObjectInspector
operator|)
name|getInspector
argument_list|()
argument_list|)
operator|.
name|getMapKeyObjectInspector
argument_list|()
argument_list|)
decl_stmt|;
name|ByteArrayRef
name|keyRef
init|=
operator|new
name|ByteArrayRef
argument_list|()
decl_stmt|;
name|keyRef
operator|.
name|setData
argument_list|(
name|columnKey
argument_list|)
expr_stmt|;
name|key
operator|.
name|init
argument_list|(
name|keyRef
argument_list|,
name|columnFamily
operator|.
name|length
argument_list|,
name|columnKey
operator|.
name|length
operator|-
name|columnFamily
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Value
name|LazyObject
name|value
init|=
name|LazyFactory
operator|.
name|createLazyObject
argument_list|(
operator|(
operator|(
name|MapObjectInspector
operator|)
name|getInspector
argument_list|()
operator|)
operator|.
name|getMapValueObjectInspector
argument_list|()
argument_list|)
decl_stmt|;
name|ByteArrayRef
name|valueRef
init|=
operator|new
name|ByteArrayRef
argument_list|()
decl_stmt|;
name|valueRef
operator|.
name|setData
argument_list|(
name|columnValue
argument_list|)
expr_stmt|;
name|value
operator|.
name|init
argument_list|(
name|valueRef
argument_list|,
literal|0
argument_list|,
name|columnValue
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Put it into the map
name|cachedMap
operator|.
name|put
argument_list|(
name|key
operator|.
name|getObject
argument_list|()
argument_list|,
name|value
operator|.
name|getObject
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Get the value in the map for the given key.    *     * @param key    * @return    */
specifier|public
name|Object
name|getMapValueElement
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
if|if
condition|(
operator|!
name|getParsed
argument_list|()
condition|)
block|{
name|parse
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|entry
range|:
name|cachedMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|LazyPrimitive
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|lazyKeyI
init|=
operator|(
name|LazyPrimitive
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
comment|// getWritableObject() will convert LazyPrimitive to actual primitive
comment|// writable objects.
name|Object
name|keyI
init|=
name|lazyKeyI
operator|.
name|getWritableObject
argument_list|()
decl_stmt|;
if|if
condition|(
name|keyI
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|keyI
operator|.
name|equals
argument_list|(
name|key
argument_list|)
condition|)
block|{
comment|// Got a match, return the value
name|LazyObject
name|v
init|=
operator|(
name|LazyObject
operator|)
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
return|return
name|v
operator|==
literal|null
condition|?
name|v
else|:
name|v
operator|.
name|getObject
argument_list|()
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|Map
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|getMap
parameter_list|()
block|{
if|if
condition|(
operator|!
name|getParsed
argument_list|()
condition|)
block|{
name|parse
argument_list|()
expr_stmt|;
block|}
return|return
name|cachedMap
return|;
block|}
specifier|public
name|int
name|getMapSize
parameter_list|()
block|{
if|if
condition|(
operator|!
name|getParsed
argument_list|()
condition|)
block|{
name|parse
argument_list|()
expr_stmt|;
block|}
return|return
name|cachedMap
operator|.
name|size
argument_list|()
return|;
block|}
block|}
end_class

end_unit

