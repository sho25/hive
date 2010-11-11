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
name|ObjectInspectorConverters
operator|.
name|Converter
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
name|BooleanObjectInspector
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
name|ByteObjectInspector
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
name|IntObjectInspector
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
name|LongObjectInspector
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
name|TypeInfo
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

begin_comment
comment|/**  * Compare two list of objects.  * Two lists are expected to have same types. Type information for every object is  * passed when calling Constructor to avoid the step of figuring out types from  * ObjectInspetor and determine how to compare the types when comparing.  * Also, for string and text elements, it performs slightly better than  * using ObjectInspectorUtils.compare() == 0, which instead of calling .compare()  * calls .equalTo(), which compares size before byte by byte comparison.  *  */
end_comment

begin_class
specifier|public
class|class
name|ListObjectsEqualComparer
block|{
enum|enum
name|CompareType
block|{
comment|// Now only string, text, int, long, byte and boolean comparisons
comment|// are treated as special cases.
comment|// For other types, we reuse ObjectInspectorUtils.compare()
name|COMPARE_STRING
block|,
name|COMPARE_TEXT
block|,
name|COMPARE_INT
block|,
name|COMPARE_LONG
block|,
name|COMPARE_BYTE
block|,
name|COMPARE_BOOL
block|,
name|OTHER
block|}
class|class
name|FieldComparer
block|{
specifier|protected
name|ObjectInspector
name|oi0
decl_stmt|,
name|oi1
decl_stmt|;
specifier|protected
name|ObjectInspector
name|compareOI
decl_stmt|;
specifier|protected
name|CompareType
name|compareType
decl_stmt|;
specifier|protected
name|Converter
name|converter0
decl_stmt|,
name|converter1
decl_stmt|;
specifier|protected
name|StringObjectInspector
name|soi0
decl_stmt|,
name|soi1
decl_stmt|;
specifier|protected
name|IntObjectInspector
name|ioi0
decl_stmt|,
name|ioi1
decl_stmt|;
specifier|protected
name|LongObjectInspector
name|loi0
decl_stmt|,
name|loi1
decl_stmt|;
specifier|protected
name|ByteObjectInspector
name|byoi0
decl_stmt|,
name|byoi1
decl_stmt|;
specifier|protected
name|BooleanObjectInspector
name|boi0
decl_stmt|,
name|boi1
decl_stmt|;
specifier|public
name|FieldComparer
parameter_list|(
name|ObjectInspector
name|oi0
parameter_list|,
name|ObjectInspector
name|oi1
parameter_list|)
block|{
name|this
operator|.
name|oi0
operator|=
name|oi0
expr_stmt|;
name|this
operator|.
name|oi1
operator|=
name|oi1
expr_stmt|;
name|TypeInfo
name|type0
init|=
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|oi0
argument_list|)
decl_stmt|;
name|TypeInfo
name|type1
init|=
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|oi1
argument_list|)
decl_stmt|;
if|if
condition|(
name|type0
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
operator|&&
name|type1
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
condition|)
block|{
name|soi0
operator|=
operator|(
name|StringObjectInspector
operator|)
name|oi0
expr_stmt|;
name|soi1
operator|=
operator|(
name|StringObjectInspector
operator|)
name|oi1
expr_stmt|;
if|if
condition|(
name|soi0
operator|.
name|preferWritable
argument_list|()
operator|||
name|soi1
operator|.
name|preferWritable
argument_list|()
condition|)
block|{
name|compareType
operator|=
name|CompareType
operator|.
name|COMPARE_TEXT
expr_stmt|;
block|}
else|else
block|{
name|compareType
operator|=
name|CompareType
operator|.
name|COMPARE_STRING
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|type0
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|)
operator|&&
name|type1
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|)
condition|)
block|{
name|compareType
operator|=
name|CompareType
operator|.
name|COMPARE_INT
expr_stmt|;
name|ioi0
operator|=
operator|(
name|IntObjectInspector
operator|)
name|oi0
expr_stmt|;
name|ioi1
operator|=
operator|(
name|IntObjectInspector
operator|)
name|oi1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type0
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|)
operator|&&
name|type1
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|)
condition|)
block|{
name|compareType
operator|=
name|CompareType
operator|.
name|COMPARE_LONG
expr_stmt|;
name|loi0
operator|=
operator|(
name|LongObjectInspector
operator|)
name|oi0
expr_stmt|;
name|loi1
operator|=
operator|(
name|LongObjectInspector
operator|)
name|oi1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type0
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|byteTypeInfo
argument_list|)
operator|&&
name|type1
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|byteTypeInfo
argument_list|)
condition|)
block|{
name|compareType
operator|=
name|CompareType
operator|.
name|COMPARE_BYTE
expr_stmt|;
name|byoi0
operator|=
operator|(
name|ByteObjectInspector
operator|)
name|oi0
expr_stmt|;
name|byoi1
operator|=
operator|(
name|ByteObjectInspector
operator|)
name|oi1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type0
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|)
operator|&&
name|type1
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|)
condition|)
block|{
name|compareType
operator|=
name|CompareType
operator|.
name|COMPARE_BOOL
expr_stmt|;
name|boi0
operator|=
operator|(
name|BooleanObjectInspector
operator|)
name|oi0
expr_stmt|;
name|boi1
operator|=
operator|(
name|BooleanObjectInspector
operator|)
name|oi1
expr_stmt|;
block|}
else|else
block|{
comment|// We don't check compatibility of two object inspectors, but directly
comment|// pass them into ObjectInspectorUtils.compare(), users of this class
comment|// should make sure ObjectInspectorUtils.compare() doesn't throw exceptions
comment|// and returns correct results.
name|compareType
operator|=
name|CompareType
operator|.
name|OTHER
expr_stmt|;
block|}
block|}
specifier|public
name|boolean
name|areEqual
parameter_list|(
name|Object
name|o0
parameter_list|,
name|Object
name|o1
parameter_list|)
block|{
if|if
condition|(
name|o0
operator|==
literal|null
operator|&&
name|o1
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
name|o0
operator|==
literal|null
operator|||
name|o1
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
switch|switch
condition|(
name|compareType
condition|)
block|{
case|case
name|COMPARE_TEXT
case|:
return|return
operator|(
name|soi0
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|o0
argument_list|)
operator|.
name|equals
argument_list|(
name|soi1
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|o1
argument_list|)
argument_list|)
operator|)
return|;
case|case
name|COMPARE_INT
case|:
return|return
operator|(
name|ioi0
operator|.
name|get
argument_list|(
name|o0
argument_list|)
operator|==
name|ioi1
operator|.
name|get
argument_list|(
name|o1
argument_list|)
operator|)
return|;
case|case
name|COMPARE_LONG
case|:
return|return
operator|(
name|loi0
operator|.
name|get
argument_list|(
name|o0
argument_list|)
operator|==
name|loi1
operator|.
name|get
argument_list|(
name|o1
argument_list|)
operator|)
return|;
case|case
name|COMPARE_BYTE
case|:
return|return
operator|(
name|byoi0
operator|.
name|get
argument_list|(
name|o0
argument_list|)
operator|==
name|byoi1
operator|.
name|get
argument_list|(
name|o1
argument_list|)
operator|)
return|;
case|case
name|COMPARE_BOOL
case|:
return|return
operator|(
name|boi0
operator|.
name|get
argument_list|(
name|o0
argument_list|)
operator|==
name|boi1
operator|.
name|get
argument_list|(
name|o1
argument_list|)
operator|)
return|;
case|case
name|COMPARE_STRING
case|:
return|return
operator|(
name|soi0
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|o0
argument_list|)
operator|.
name|equals
argument_list|(
name|soi1
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|o1
argument_list|)
argument_list|)
operator|)
return|;
default|default:
return|return
operator|(
name|ObjectInspectorUtils
operator|.
name|compare
argument_list|(
name|o0
argument_list|,
name|oi0
argument_list|,
name|o1
argument_list|,
name|oi1
argument_list|)
operator|==
literal|0
operator|)
return|;
block|}
block|}
block|}
name|FieldComparer
index|[]
name|fieldComparers
decl_stmt|;
name|int
name|numFields
decl_stmt|;
specifier|public
name|ListObjectsEqualComparer
parameter_list|(
name|ObjectInspector
index|[]
name|oi0
parameter_list|,
name|ObjectInspector
index|[]
name|oi1
parameter_list|)
block|{
if|if
condition|(
name|oi0
operator|.
name|length
operator|!=
name|oi1
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Sizes of two lists of object inspectors don't match."
argument_list|)
throw|;
block|}
name|numFields
operator|=
name|oi0
operator|.
name|length
expr_stmt|;
name|fieldComparers
operator|=
operator|new
name|FieldComparer
index|[
name|numFields
index|]
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
name|oi0
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|fieldComparers
index|[
name|i
index|]
operator|=
operator|new
name|FieldComparer
argument_list|(
name|oi0
index|[
name|i
index|]
argument_list|,
name|oi1
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * ol0, ol1 should have equal or less number of elements than objectinspectors    * passed in constructor.    *    * @param ol0    * @param ol1    * @return True if object in ol0 and ol1 are all identical    */
specifier|public
name|boolean
name|areEqual
parameter_list|(
name|Object
index|[]
name|ol0
parameter_list|,
name|Object
index|[]
name|ol1
parameter_list|)
block|{
if|if
condition|(
name|ol0
operator|.
name|length
operator|!=
name|numFields
operator|||
name|ol1
operator|.
name|length
operator|!=
name|numFields
condition|)
block|{
if|if
condition|(
name|ol0
operator|.
name|length
operator|!=
name|ol1
operator|.
name|length
condition|)
block|{
return|return
literal|false
return|;
block|}
assert|assert
operator|(
name|ol0
operator|.
name|length
operator|<=
name|numFields
operator|)
assert|;
assert|assert
operator|(
name|ol1
operator|.
name|length
operator|<=
name|numFields
operator|)
assert|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|Math
operator|.
name|min
argument_list|(
name|ol0
operator|.
name|length
argument_list|,
name|ol1
operator|.
name|length
argument_list|)
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|fieldComparers
index|[
name|i
index|]
operator|.
name|areEqual
argument_list|(
name|ol0
index|[
name|i
index|]
argument_list|,
name|ol1
index|[
name|i
index|]
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
for|for
control|(
name|int
name|i
init|=
name|numFields
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|--
control|)
block|{
if|if
condition|(
operator|!
name|fieldComparers
index|[
name|i
index|]
operator|.
name|areEqual
argument_list|(
name|ol0
index|[
name|i
index|]
argument_list|,
name|ol1
index|[
name|i
index|]
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

