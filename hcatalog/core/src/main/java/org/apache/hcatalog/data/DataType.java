begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
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
name|List
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
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
import|;
end_import

begin_comment
comment|/**  * @deprecated Use/modify {@link org.apache.hive.hcatalog.data.DataType} instead  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|DataType
block|{
specifier|public
specifier|static
specifier|final
name|byte
name|NULL
init|=
literal|1
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|BOOLEAN
init|=
literal|5
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|BYTE
init|=
literal|6
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|INTEGER
init|=
literal|10
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|SHORT
init|=
literal|11
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|LONG
init|=
literal|15
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|FLOAT
init|=
literal|20
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|DOUBLE
init|=
literal|25
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|STRING
init|=
literal|55
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|BINARY
init|=
literal|60
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|MAP
init|=
literal|100
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|STRUCT
init|=
literal|110
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|LIST
init|=
literal|120
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|ERROR
init|=
operator|-
literal|1
decl_stmt|;
comment|/**      * Determine the datatype of an object.      * @param o Object to test.      * @return byte code of the type, or ERROR if we don't know.      */
specifier|public
specifier|static
name|byte
name|findType
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
name|NULL
return|;
block|}
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
init|=
name|o
operator|.
name|getClass
argument_list|()
decl_stmt|;
comment|// Try to put the most common first
if|if
condition|(
name|clazz
operator|==
name|String
operator|.
name|class
condition|)
block|{
return|return
name|STRING
return|;
block|}
elseif|else
if|if
condition|(
name|clazz
operator|==
name|Integer
operator|.
name|class
condition|)
block|{
return|return
name|INTEGER
return|;
block|}
elseif|else
if|if
condition|(
name|clazz
operator|==
name|Long
operator|.
name|class
condition|)
block|{
return|return
name|LONG
return|;
block|}
elseif|else
if|if
condition|(
name|clazz
operator|==
name|Float
operator|.
name|class
condition|)
block|{
return|return
name|FLOAT
return|;
block|}
elseif|else
if|if
condition|(
name|clazz
operator|==
name|Double
operator|.
name|class
condition|)
block|{
return|return
name|DOUBLE
return|;
block|}
elseif|else
if|if
condition|(
name|clazz
operator|==
name|Boolean
operator|.
name|class
condition|)
block|{
return|return
name|BOOLEAN
return|;
block|}
elseif|else
if|if
condition|(
name|clazz
operator|==
name|Byte
operator|.
name|class
condition|)
block|{
return|return
name|BYTE
return|;
block|}
elseif|else
if|if
condition|(
name|clazz
operator|==
name|Short
operator|.
name|class
condition|)
block|{
return|return
name|SHORT
return|;
block|}
elseif|else
if|if
condition|(
name|o
operator|instanceof
name|List
argument_list|<
name|?
argument_list|>
condition|)
block|{
return|return
name|LIST
return|;
block|}
elseif|else
if|if
condition|(
name|o
operator|instanceof
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
condition|)
block|{
return|return
name|MAP
return|;
block|}
elseif|else
if|if
condition|(
name|o
operator|instanceof
name|byte
index|[]
condition|)
block|{
return|return
name|BINARY
return|;
block|}
else|else
block|{
return|return
name|ERROR
return|;
block|}
block|}
specifier|public
specifier|static
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
name|compare
argument_list|(
name|o1
argument_list|,
name|o2
argument_list|,
name|findType
argument_list|(
name|o1
argument_list|)
argument_list|,
name|findType
argument_list|(
name|o2
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|int
name|compare
parameter_list|(
name|Object
name|o1
parameter_list|,
name|Object
name|o2
parameter_list|,
name|byte
name|dt1
parameter_list|,
name|byte
name|dt2
parameter_list|)
block|{
if|if
condition|(
name|dt1
operator|==
name|dt2
condition|)
block|{
switch|switch
condition|(
name|dt1
condition|)
block|{
case|case
name|NULL
case|:
return|return
literal|0
return|;
case|case
name|BOOLEAN
case|:
return|return
operator|(
operator|(
name|Boolean
operator|)
name|o1
operator|)
operator|.
name|compareTo
argument_list|(
operator|(
name|Boolean
operator|)
name|o2
argument_list|)
return|;
case|case
name|BYTE
case|:
return|return
operator|(
operator|(
name|Byte
operator|)
name|o1
operator|)
operator|.
name|compareTo
argument_list|(
operator|(
name|Byte
operator|)
name|o2
argument_list|)
return|;
case|case
name|INTEGER
case|:
return|return
operator|(
operator|(
name|Integer
operator|)
name|o1
operator|)
operator|.
name|compareTo
argument_list|(
operator|(
name|Integer
operator|)
name|o2
argument_list|)
return|;
case|case
name|LONG
case|:
return|return
operator|(
operator|(
name|Long
operator|)
name|o1
operator|)
operator|.
name|compareTo
argument_list|(
operator|(
name|Long
operator|)
name|o2
argument_list|)
return|;
case|case
name|FLOAT
case|:
return|return
operator|(
operator|(
name|Float
operator|)
name|o1
operator|)
operator|.
name|compareTo
argument_list|(
operator|(
name|Float
operator|)
name|o2
argument_list|)
return|;
case|case
name|DOUBLE
case|:
return|return
operator|(
operator|(
name|Double
operator|)
name|o1
operator|)
operator|.
name|compareTo
argument_list|(
operator|(
name|Double
operator|)
name|o2
argument_list|)
return|;
case|case
name|STRING
case|:
return|return
operator|(
operator|(
name|String
operator|)
name|o1
operator|)
operator|.
name|compareTo
argument_list|(
operator|(
name|String
operator|)
name|o2
argument_list|)
return|;
case|case
name|SHORT
case|:
return|return
operator|(
operator|(
name|Short
operator|)
name|o1
operator|)
operator|.
name|compareTo
argument_list|(
operator|(
name|Short
operator|)
name|o2
argument_list|)
return|;
case|case
name|BINARY
case|:
return|return
name|compareByteArray
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|o1
argument_list|,
operator|(
name|byte
index|[]
operator|)
name|o2
argument_list|)
return|;
case|case
name|LIST
case|:
name|List
argument_list|<
name|?
argument_list|>
name|l1
init|=
operator|(
name|List
argument_list|<
name|?
argument_list|>
operator|)
name|o1
decl_stmt|;
name|List
argument_list|<
name|?
argument_list|>
name|l2
init|=
operator|(
name|List
argument_list|<
name|?
argument_list|>
operator|)
name|o2
decl_stmt|;
name|int
name|len
init|=
name|l1
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|len
operator|!=
name|l2
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
name|len
operator|-
name|l2
operator|.
name|size
argument_list|()
return|;
block|}
else|else
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
name|len
condition|;
name|i
operator|++
control|)
block|{
name|int
name|cmpVal
init|=
name|compare
argument_list|(
name|l1
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|l2
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmpVal
operator|!=
literal|0
condition|)
block|{
return|return
name|cmpVal
return|;
block|}
block|}
return|return
literal|0
return|;
block|}
case|case
name|MAP
case|:
block|{
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|m1
init|=
operator|(
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|o1
decl_stmt|;
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|m2
init|=
operator|(
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|o2
decl_stmt|;
name|int
name|sz1
init|=
name|m1
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|sz2
init|=
name|m2
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|sz1
operator|<
name|sz2
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|sz1
operator|>
name|sz2
condition|)
block|{
return|return
literal|1
return|;
block|}
else|else
block|{
comment|// This is bad, but we have to sort the keys of the maps in order
comment|// to be commutative.
name|TreeMap
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|tm1
init|=
operator|new
name|TreeMap
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
argument_list|(
name|m1
argument_list|)
decl_stmt|;
name|TreeMap
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|tm2
init|=
operator|new
name|TreeMap
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
argument_list|(
name|m2
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
argument_list|>
name|i1
init|=
name|tm1
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
argument_list|>
name|i2
init|=
name|tm2
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|i1
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|entry1
init|=
name|i1
operator|.
name|next
argument_list|()
decl_stmt|;
name|Map
operator|.
name|Entry
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|entry2
init|=
name|i2
operator|.
name|next
argument_list|()
decl_stmt|;
name|int
name|c
init|=
name|compare
argument_list|(
name|entry1
operator|.
name|getValue
argument_list|()
argument_list|,
name|entry2
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|!=
literal|0
condition|)
block|{
return|return
name|c
return|;
block|}
else|else
block|{
name|c
operator|=
name|compare
argument_list|(
name|entry1
operator|.
name|getValue
argument_list|()
argument_list|,
name|entry2
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|c
operator|!=
literal|0
condition|)
block|{
return|return
name|c
return|;
block|}
block|}
block|}
return|return
literal|0
return|;
block|}
block|}
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unkown type "
operator|+
name|dt1
operator|+
literal|" in compare"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
return|return
name|dt1
operator|<
name|dt2
condition|?
operator|-
literal|1
else|:
literal|1
return|;
block|}
block|}
specifier|private
specifier|static
name|int
name|compareByteArray
parameter_list|(
name|byte
index|[]
name|o1
parameter_list|,
name|byte
index|[]
name|o2
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
name|o1
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|==
name|o2
operator|.
name|length
condition|)
block|{
return|return
literal|1
return|;
block|}
if|if
condition|(
name|o1
index|[
name|i
index|]
operator|==
name|o2
index|[
name|i
index|]
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|o1
index|[
name|i
index|]
operator|>
name|o1
index|[
name|i
index|]
condition|)
block|{
return|return
literal|1
return|;
block|}
else|else
block|{
return|return
operator|-
literal|1
return|;
block|}
block|}
comment|//bytes in o1 are same as o2
comment|//in case o2 was longer
if|if
condition|(
name|o2
operator|.
name|length
operator|>
name|o1
operator|.
name|length
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
literal|0
return|;
comment|//equals
block|}
block|}
end_class

end_unit

