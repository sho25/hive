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
name|hive
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|serde2
operator|.
name|SerDeException
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
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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
name|ListTypeInfo
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
name|MapTypeInfo
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
name|PrimitiveTypeInfo
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
name|StructTypeInfo
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * ObjectInspectorFactory for HCatRecordObjectInspectors (and associated helper inspectors)  */
end_comment

begin_class
specifier|public
class|class
name|HCatRecordObjectInspectorFactory
block|{
specifier|private
specifier|final
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HCatRecordObjectInspectorFactory
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
name|HashMap
argument_list|<
name|TypeInfo
argument_list|,
name|HCatRecordObjectInspector
argument_list|>
name|cachedHCatRecordObjectInspectors
init|=
operator|new
name|HashMap
argument_list|<
name|TypeInfo
argument_list|,
name|HCatRecordObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
specifier|static
name|HashMap
argument_list|<
name|TypeInfo
argument_list|,
name|ObjectInspector
argument_list|>
name|cachedObjectInspectors
init|=
operator|new
name|HashMap
argument_list|<
name|TypeInfo
argument_list|,
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Returns HCatRecordObjectInspector given a StructTypeInfo type definition for the record to look into    * @param typeInfo Type definition for the record to look into    * @return appropriate HCatRecordObjectInspector    * @throws SerDeException    */
specifier|public
specifier|static
name|HCatRecordObjectInspector
name|getHCatRecordObjectInspector
parameter_list|(
name|StructTypeInfo
name|typeInfo
parameter_list|)
throws|throws
name|SerDeException
block|{
name|HCatRecordObjectInspector
name|oi
init|=
name|cachedHCatRecordObjectInspectors
operator|.
name|get
argument_list|(
name|typeInfo
argument_list|)
decl_stmt|;
if|if
condition|(
name|oi
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Got asked for OI for {} [{} ]"
argument_list|,
name|typeInfo
operator|.
name|getCategory
argument_list|()
argument_list|,
name|typeInfo
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|typeInfo
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|STRUCT
case|:
name|StructTypeInfo
name|structTypeInfo
init|=
operator|(
name|StructTypeInfo
operator|)
name|typeInfo
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
name|structTypeInfo
operator|.
name|getAllStructFieldNames
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|fieldTypeInfos
init|=
name|structTypeInfo
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|fieldObjectInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|fieldTypeInfos
operator|.
name|size
argument_list|()
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
name|fieldTypeInfos
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|fieldObjectInspectors
operator|.
name|add
argument_list|(
name|getStandardObjectInspectorFromTypeInfo
argument_list|(
name|fieldTypeInfos
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|oi
operator|=
operator|new
name|HCatRecordObjectInspector
argument_list|(
name|fieldNames
argument_list|,
name|fieldObjectInspectors
argument_list|)
expr_stmt|;
break|break;
default|default:
comment|// Hmm.. not good,
comment|// the only type expected here is STRUCT, which maps to HCatRecord
comment|// - anything else is an error. Return null as the inspector.
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"TypeInfo ["
operator|+
name|typeInfo
operator|.
name|getTypeName
argument_list|()
operator|+
literal|"] was not of struct type - HCatRecord expected struct type, got ["
operator|+
name|typeInfo
operator|.
name|getCategory
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|cachedHCatRecordObjectInspectors
operator|.
name|put
argument_list|(
name|typeInfo
argument_list|,
name|oi
argument_list|)
expr_stmt|;
block|}
return|return
name|oi
return|;
block|}
specifier|public
specifier|static
name|ObjectInspector
name|getStandardObjectInspectorFromTypeInfo
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|)
block|{
name|ObjectInspector
name|oi
init|=
name|cachedObjectInspectors
operator|.
name|get
argument_list|(
name|typeInfo
argument_list|)
decl_stmt|;
if|if
condition|(
name|oi
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Got asked for OI for {}, [{}]"
argument_list|,
name|typeInfo
operator|.
name|getCategory
argument_list|()
argument_list|,
name|typeInfo
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|typeInfo
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|PRIMITIVE
case|:
name|oi
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
operator|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRUCT
case|:
name|StructTypeInfo
name|structTypeInfo
init|=
operator|(
name|StructTypeInfo
operator|)
name|typeInfo
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
name|structTypeInfo
operator|.
name|getAllStructFieldNames
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|fieldTypeInfos
init|=
name|structTypeInfo
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|fieldObjectInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|fieldTypeInfos
operator|.
name|size
argument_list|()
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
name|fieldTypeInfos
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|fieldObjectInspectors
operator|.
name|add
argument_list|(
name|getStandardObjectInspectorFromTypeInfo
argument_list|(
name|fieldTypeInfos
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|oi
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|fieldNames
argument_list|,
name|fieldObjectInspectors
argument_list|)
expr_stmt|;
break|break;
case|case
name|LIST
case|:
name|ObjectInspector
name|elementObjectInspector
init|=
name|getStandardObjectInspectorFromTypeInfo
argument_list|(
operator|(
operator|(
name|ListTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getListElementTypeInfo
argument_list|()
argument_list|)
decl_stmt|;
name|oi
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|elementObjectInspector
argument_list|)
expr_stmt|;
break|break;
case|case
name|MAP
case|:
name|ObjectInspector
name|keyObjectInspector
init|=
name|getStandardObjectInspectorFromTypeInfo
argument_list|(
operator|(
operator|(
name|MapTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getMapKeyTypeInfo
argument_list|()
argument_list|)
decl_stmt|;
name|ObjectInspector
name|valueObjectInspector
init|=
name|getStandardObjectInspectorFromTypeInfo
argument_list|(
operator|(
operator|(
name|MapTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getMapValueTypeInfo
argument_list|()
argument_list|)
decl_stmt|;
name|oi
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardMapObjectInspector
argument_list|(
name|keyObjectInspector
argument_list|,
name|valueObjectInspector
argument_list|)
expr_stmt|;
break|break;
default|default:
name|oi
operator|=
literal|null
expr_stmt|;
block|}
name|cachedObjectInspectors
operator|.
name|put
argument_list|(
name|typeInfo
argument_list|,
name|oi
argument_list|)
expr_stmt|;
block|}
return|return
name|oi
return|;
block|}
block|}
end_class

end_unit

