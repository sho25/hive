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
name|ql
operator|.
name|udf
operator|.
name|generic
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
name|Comparator
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|Description
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
name|ql
operator|.
name|exec
operator|.
name|UDFArgumentException
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
name|ql
operator|.
name|exec
operator|.
name|UDFArgumentLengthException
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
name|ql
operator|.
name|exec
operator|.
name|UDFArgumentTypeException
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
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
name|serde
operator|.
name|serdeConstants
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
name|ConstantObjectInspector
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
name|ObjectInspectorFactory
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
name|ObjectInspectorUtils
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
name|StructField
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
name|StructObjectInspector
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
name|PrimitiveObjectInspectorUtils
operator|.
name|PrimitiveGrouping
import|;
end_import

begin_comment
comment|/**  * Generic UDF for tuple array sort by desired field[s] with [ordering(ASC or DESC)]  *<code>SORT_ARRAY_BY(array(obj1, obj2, obj3...),'f1','f2',..,['ASC','DESC'])</code>.  *  * @see org.apache.hadoop.hive.ql.udf.generic.GenericUDF  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"sort_array_by"
argument_list|,
name|value
operator|=
literal|"_FUNC_(array(obj1, obj2,...),'f1','f2',...,['ASC','DESC']) - "
operator|+
literal|"Sorts the input tuple array in user specified order(ASC,DESC) by desired field[s] name"
operator|+
literal|" If sorting order is not mentioned by user then dafault sorting order is ascending"
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_(array(struct('g',100),struct('b',200)),'col1','ASC') FROM src LIMIT 1;\n"
operator|+
literal|" array(struct('b',200),struct('g',100)) "
argument_list|)
specifier|public
class|class
name|GenericUDFSortArrayByField
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|transient
name|Converter
index|[]
name|converters
decl_stmt|;
specifier|private
specifier|transient
name|PrimitiveCategory
index|[]
name|inputTypes
decl_stmt|;
comment|/**Output array results*/
specifier|private
specifier|final
name|List
argument_list|<
name|Object
argument_list|>
name|ret
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|transient
name|ListObjectInspector
name|listObjectInspector
decl_stmt|;
specifier|private
specifier|transient
name|StructObjectInspector
name|structObjectInspector
decl_stmt|;
comment|/**All sorting fields*/
specifier|private
specifier|transient
name|StructField
index|[]
name|fields
decl_stmt|;
comment|/**Number of fields based on sorting will take place*/
specifier|private
specifier|transient
name|int
name|noOfInputFields
decl_stmt|;
comment|/**All possible ordering constants*/
specifier|private
enum|enum
name|SORT_ORDER_TYPE
block|{
name|ASC
block|,
name|DESC
block|}
empty_stmt|;
comment|/**default sorting order*/
specifier|private
specifier|transient
name|SORT_ORDER_TYPE
name|sortOrder
init|=
name|SORT_ORDER_TYPE
operator|.
name|ASC
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|initialize
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
name|GenericUDFUtils
operator|.
name|ReturnObjectInspectorResolver
name|returnOIResolver
decl_stmt|;
name|returnOIResolver
operator|=
operator|new
name|GenericUDFUtils
operator|.
name|ReturnObjectInspectorResolver
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|/**This UDF requires minimum 2 arguments array_name,field name*/
if|if
condition|(
name|arguments
operator|.
name|length
operator|<
literal|2
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentLengthException
argument_list|(
literal|"SORT_ARRAY_BY requires minimum 2 arguments, got "
operator|+
name|arguments
operator|.
name|length
argument_list|)
throw|;
block|}
comment|/**First argument must be array*/
switch|switch
condition|(
name|arguments
index|[
literal|0
index|]
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|LIST
case|:
name|listObjectInspector
operator|=
operator|(
name|ListObjectInspector
operator|)
name|arguments
index|[
literal|0
index|]
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"Argument 1 of function SORT_ARRAY_BY must be "
operator|+
name|serdeConstants
operator|.
name|LIST_TYPE_NAME
operator|+
literal|", but "
operator|+
name|arguments
index|[
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was found."
argument_list|)
throw|;
block|}
comment|/**Elements inside first argument(array) must be tuple(s)*/
switch|switch
condition|(
name|listObjectInspector
operator|.
name|getListElementObjectInspector
argument_list|()
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|STRUCT
case|:
name|structObjectInspector
operator|=
operator|(
name|StructObjectInspector
operator|)
name|listObjectInspector
operator|.
name|getListElementObjectInspector
argument_list|()
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"Element[s] of first argument array in function SORT_ARRAY_BY must be "
operator|+
name|serdeConstants
operator|.
name|STRUCT_TYPE_NAME
operator|+
literal|", but "
operator|+
name|listObjectInspector
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was found."
argument_list|)
throw|;
block|}
comment|/**All sort fields argument name and sort order name must be in String type*/
name|converters
operator|=
operator|new
name|Converter
index|[
name|arguments
operator|.
name|length
index|]
expr_stmt|;
name|inputTypes
operator|=
operator|new
name|PrimitiveCategory
index|[
name|arguments
operator|.
name|length
index|]
expr_stmt|;
name|fields
operator|=
operator|new
name|StructField
index|[
name|arguments
operator|.
name|length
operator|-
literal|1
index|]
expr_stmt|;
name|noOfInputFields
operator|=
name|arguments
operator|.
name|length
operator|-
literal|1
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|arguments
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|checkArgPrimitive
argument_list|(
name|arguments
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|checkArgGroups
argument_list|(
name|arguments
argument_list|,
name|i
argument_list|,
name|inputTypes
argument_list|,
name|PrimitiveGrouping
operator|.
name|STRING_GROUP
argument_list|)
expr_stmt|;
if|if
condition|(
name|arguments
index|[
name|i
index|]
operator|instanceof
name|ConstantObjectInspector
condition|)
block|{
name|String
name|fieldName
init|=
name|getConstantStringValue
argument_list|(
name|arguments
argument_list|,
name|i
argument_list|)
decl_stmt|;
comment|/**checking whether any sorting order (ASC,DESC) has specified in last argument*/
if|if
condition|(
name|i
operator|!=
literal|1
operator|&&
operator|(
name|i
operator|==
name|arguments
operator|.
name|length
operator|-
literal|1
operator|)
operator|&&
operator|(
name|fieldName
operator|.
name|trim
argument_list|()
operator|.
name|toUpperCase
argument_list|()
operator|.
name|equals
argument_list|(
name|SORT_ORDER_TYPE
operator|.
name|ASC
operator|.
name|name
argument_list|()
argument_list|)
operator|||
name|fieldName
operator|.
name|trim
argument_list|()
operator|.
name|toUpperCase
argument_list|()
operator|.
name|equals
argument_list|(
name|SORT_ORDER_TYPE
operator|.
name|DESC
operator|.
name|name
argument_list|()
argument_list|)
operator|)
condition|)
block|{
name|sortOrder
operator|=
name|SORT_ORDER_TYPE
operator|.
name|valueOf
argument_list|(
name|fieldName
operator|.
name|trim
argument_list|()
operator|.
name|toUpperCase
argument_list|()
argument_list|)
expr_stmt|;
name|noOfInputFields
operator|-=
literal|1
expr_stmt|;
continue|continue;
block|}
name|fields
index|[
name|i
operator|-
literal|1
index|]
operator|=
name|structObjectInspector
operator|.
name|getStructFieldRef
argument_list|(
name|getConstantStringValue
argument_list|(
name|arguments
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|obtainStringConverter
argument_list|(
name|arguments
argument_list|,
name|i
argument_list|,
name|inputTypes
argument_list|,
name|converters
argument_list|)
expr_stmt|;
block|}
name|ObjectInspector
name|returnOI
init|=
name|returnOIResolver
operator|.
name|get
argument_list|(
name|structObjectInspector
argument_list|)
decl_stmt|;
name|converters
index|[
literal|0
index|]
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|structObjectInspector
argument_list|,
name|returnOI
argument_list|)
expr_stmt|;
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|structObjectInspector
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|evaluate
parameter_list|(
name|DeferredObject
index|[]
name|arguments
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|/**Except first argument all remaining are field names and [sorting order]*/
comment|/**Add all non constant string tuple fields based on which sorting will happen with sorting ordering information if any.*/
name|String
name|field
init|=
literal|null
decl_stmt|;
comment|/**If sorting order is set in initialize method then we are excluding last argument  */
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|noOfInputFields
operator|&&
name|fields
index|[
name|i
index|]
operator|==
literal|null
condition|;
name|i
operator|++
control|)
block|{
name|field
operator|=
name|getStringValue
argument_list|(
name|arguments
argument_list|,
name|i
operator|+
literal|1
argument_list|,
name|converters
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|!=
literal|0
operator|&&
operator|(
name|i
operator|==
name|arguments
operator|.
name|length
operator|-
literal|2
operator|)
operator|&&
operator|(
name|field
operator|.
name|trim
argument_list|()
operator|.
name|toUpperCase
argument_list|()
operator|.
name|equals
argument_list|(
name|SORT_ORDER_TYPE
operator|.
name|ASC
operator|.
name|name
argument_list|()
argument_list|)
operator|||
name|field
operator|.
name|trim
argument_list|()
operator|.
name|toUpperCase
argument_list|()
operator|.
name|equals
argument_list|(
name|SORT_ORDER_TYPE
operator|.
name|DESC
operator|.
name|name
argument_list|()
argument_list|)
operator|)
condition|)
block|{
name|noOfInputFields
operator|-=
literal|1
expr_stmt|;
name|sortOrder
operator|=
name|SORT_ORDER_TYPE
operator|.
name|valueOf
argument_list|(
name|field
operator|.
name|trim
argument_list|()
operator|.
name|toUpperCase
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|fields
index|[
name|i
index|]
operator|=
name|structObjectInspector
operator|.
name|getStructFieldRef
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
name|Object
name|array
init|=
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|retArray
init|=
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|listObjectInspector
operator|.
name|getList
argument_list|(
name|array
argument_list|)
decl_stmt|;
comment|/**Sort the tuple*/
name|Collections
operator|.
name|sort
argument_list|(
name|retArray
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Object
name|object1
parameter_list|,
name|Object
name|object2
parameter_list|)
block|{
name|int
name|result
init|=
literal|0
decl_stmt|;
comment|/**If multiple fields are mentioned for sorting a record then inside the loop we do will do sorting for each field*/
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|noOfInputFields
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|o1
init|=
name|structObjectInspector
operator|.
name|getStructFieldData
argument_list|(
name|object1
argument_list|,
name|fields
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|Object
name|o2
init|=
name|structObjectInspector
operator|.
name|getStructFieldData
argument_list|(
name|object2
argument_list|,
name|fields
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|result
operator|=
name|ObjectInspectorUtils
operator|.
name|compare
argument_list|(
name|o1
argument_list|,
name|fields
index|[
name|i
index|]
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|,
name|o2
argument_list|,
name|fields
index|[
name|i
index|]
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
block|{
comment|/**Ordering*/
if|if
condition|(
name|sortOrder
operator|==
name|SORT_ORDER_TYPE
operator|.
name|DESC
condition|)
block|{
name|result
operator|*=
operator|-
literal|1
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
return|return
name|result
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|ret
operator|.
name|clear
argument_list|()
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
name|retArray
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ret
operator|.
name|add
argument_list|(
name|converters
index|[
literal|0
index|]
operator|.
name|convert
argument_list|(
name|retArray
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDisplayString
parameter_list|(
name|String
index|[]
name|children
parameter_list|)
block|{
return|return
name|getStandardDisplayString
argument_list|(
literal|"sort_array_by"
argument_list|,
name|children
argument_list|)
return|;
block|}
block|}
end_class

end_unit

