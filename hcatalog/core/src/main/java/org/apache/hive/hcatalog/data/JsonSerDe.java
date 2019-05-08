begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|Arrays
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
name|Map
operator|.
name|Entry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|lang3
operator|.
name|ArrayUtils
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
name|conf
operator|.
name|Configuration
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
name|conf
operator|.
name|HiveConf
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
name|AbstractSerDe
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
name|SerDeSpec
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
name|SerDeStats
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
name|io
operator|.
name|Text
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
name|Writable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatSchemaUtils
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

begin_class
annotation|@
name|SerDeSpec
argument_list|(
name|schemaProps
operator|=
block|{
name|serdeConstants
operator|.
name|LIST_COLUMNS
block|,
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
block|,
name|serdeConstants
operator|.
name|TIMESTAMP_FORMATS
block|}
argument_list|)
specifier|public
class|class
name|JsonSerDe
extends|extends
name|AbstractSerDe
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|JsonSerDe
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|HCatSchema
name|schema
decl_stmt|;
specifier|private
name|HCatRecordObjectInspector
name|cachedObjectInspector
decl_stmt|;
specifier|private
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
name|JsonSerDe
name|jsonSerde
init|=
operator|new
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
name|JsonSerDe
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Properties
name|tbl
parameter_list|)
throws|throws
name|SerDeException
block|{
name|jsonSerde
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|tbl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|StructTypeInfo
name|rowTypeInfo
init|=
name|jsonSerde
operator|.
name|getTypeInfo
argument_list|()
decl_stmt|;
name|cachedObjectInspector
operator|=
name|HCatRecordObjectInspectorFactory
operator|.
name|getHCatRecordObjectInspector
argument_list|(
name|rowTypeInfo
argument_list|)
expr_stmt|;
try|try
block|{
name|schema
operator|=
name|HCatSchemaUtils
operator|.
name|getHCatSchema
argument_list|(
name|rowTypeInfo
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getStructSubSchema
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"schema : {}"
argument_list|,
name|schema
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"fields : {}"
argument_list|,
name|schema
operator|.
name|getFieldNames
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HCatException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Takes JSON string in Text form, and has to return an object representation above    * it that's readable by the corresponding object inspector.    *    * For this implementation, since we're using the jackson parser, we can construct    * our own object implementation, and we use HCatRecord for it    */
annotation|@
name|Override
specifier|public
name|Object
name|deserialize
parameter_list|(
name|Writable
name|blob
parameter_list|)
throws|throws
name|SerDeException
block|{
try|try
block|{
name|List
argument_list|<
name|?
argument_list|>
name|row
init|=
operator|(
name|List
argument_list|<
name|?
argument_list|>
operator|)
name|jsonSerde
operator|.
name|deserialize
argument_list|(
name|blob
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|fatRow
init|=
name|fatLand
argument_list|(
name|row
argument_list|)
decl_stmt|;
return|return
operator|new
name|DefaultHCatRecord
argument_list|(
name|fatRow
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"rawtypes"
block|,
literal|"unchecked"
block|}
argument_list|)
specifier|private
specifier|static
name|List
argument_list|<
name|Object
argument_list|>
name|fatLand
parameter_list|(
specifier|final
name|List
argument_list|<
name|?
argument_list|>
name|arr
parameter_list|)
block|{
specifier|final
name|List
name|ret
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|Object
name|o
range|:
name|arr
control|)
block|{
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
name|ret
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
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
name|ret
operator|.
name|add
argument_list|(
name|fatMap
argument_list|(
operator|(
operator|(
name|Map
operator|)
name|o
operator|)
argument_list|)
argument_list|)
expr_stmt|;
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
name|ret
operator|.
name|add
argument_list|(
name|fatLand
argument_list|(
operator|(
name|List
operator|)
name|o
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|o
operator|.
name|getClass
argument_list|()
operator|.
name|isArray
argument_list|()
operator|&&
name|o
operator|.
name|getClass
argument_list|()
operator|.
name|getComponentType
argument_list|()
operator|!=
name|byte
operator|.
name|class
condition|)
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|ct
init|=
name|o
operator|.
name|getClass
argument_list|()
operator|.
name|getComponentType
argument_list|()
decl_stmt|;
if|if
condition|(
name|ct
operator|.
name|isPrimitive
argument_list|()
condition|)
block|{
name|ret
operator|.
name|add
argument_list|(
name|primitiveArrayToList
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ret
operator|.
name|add
argument_list|(
name|fatLand
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|(
name|Object
index|[]
operator|)
name|o
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|ret
operator|.
name|add
argument_list|(
name|o
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ret
return|;
block|}
specifier|private
specifier|static
name|Object
name|fatMap
parameter_list|(
name|Map
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|map
parameter_list|)
block|{
name|Map
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|ret
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Entry
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
argument_list|>
name|es
init|=
name|map
operator|.
name|entrySet
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|e
range|:
name|es
control|)
block|{
specifier|final
name|Object
name|oldV
init|=
name|e
operator|.
name|getValue
argument_list|()
decl_stmt|;
specifier|final
name|Object
name|newV
decl_stmt|;
if|if
condition|(
name|oldV
operator|!=
literal|null
operator|&&
name|oldV
operator|.
name|getClass
argument_list|()
operator|.
name|isArray
argument_list|()
condition|)
block|{
name|newV
operator|=
name|fatLand
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|(
name|Object
index|[]
operator|)
name|oldV
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|newV
operator|=
name|oldV
expr_stmt|;
block|}
name|ret
operator|.
name|put
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|newV
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
specifier|private
specifier|static
name|Object
name|primitiveArrayToList
parameter_list|(
name|Object
name|arr
parameter_list|)
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|ct
init|=
name|arr
operator|.
name|getClass
argument_list|()
operator|.
name|getComponentType
argument_list|()
decl_stmt|;
if|if
condition|(
name|int
operator|.
name|class
operator|.
name|equals
argument_list|(
name|ct
argument_list|)
condition|)
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|ArrayUtils
operator|.
name|toObject
argument_list|(
operator|(
name|int
index|[]
operator|)
name|arr
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|long
operator|.
name|class
operator|.
name|equals
argument_list|(
name|ct
argument_list|)
condition|)
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|ArrayUtils
operator|.
name|toObject
argument_list|(
operator|(
name|long
index|[]
operator|)
name|arr
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|char
operator|.
name|class
operator|.
name|equals
argument_list|(
name|ct
argument_list|)
condition|)
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|ArrayUtils
operator|.
name|toObject
argument_list|(
operator|(
name|char
index|[]
operator|)
name|arr
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|byte
operator|.
name|class
operator|.
name|equals
argument_list|(
name|ct
argument_list|)
condition|)
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|ArrayUtils
operator|.
name|toObject
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|arr
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|short
operator|.
name|class
operator|.
name|equals
argument_list|(
name|ct
argument_list|)
condition|)
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|ArrayUtils
operator|.
name|toObject
argument_list|(
operator|(
name|short
index|[]
operator|)
name|arr
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|float
operator|.
name|class
operator|.
name|equals
argument_list|(
name|ct
argument_list|)
condition|)
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|ArrayUtils
operator|.
name|toObject
argument_list|(
operator|(
name|float
index|[]
operator|)
name|arr
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|double
operator|.
name|class
operator|.
name|equals
argument_list|(
name|ct
argument_list|)
condition|)
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|ArrayUtils
operator|.
name|toObject
argument_list|(
operator|(
name|double
index|[]
operator|)
name|arr
argument_list|)
argument_list|)
return|;
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unhandled primitiveArrayToList for type: "
operator|+
name|ct
argument_list|)
throw|;
block|}
specifier|public
name|String
name|getHiveInternalColumnName
parameter_list|(
name|int
name|fpos
parameter_list|)
block|{
return|return
name|HiveConf
operator|.
name|getColumnInternalName
argument_list|(
name|fpos
argument_list|)
return|;
block|}
comment|/**    * Given an object and object inspector pair, traverse the object    * and generate a Text representation of the object.    */
annotation|@
name|Override
specifier|public
name|Writable
name|serialize
parameter_list|(
name|Object
name|obj
parameter_list|,
name|ObjectInspector
name|objInspector
parameter_list|)
throws|throws
name|SerDeException
block|{
return|return
name|jsonSerde
operator|.
name|serialize
argument_list|(
name|obj
argument_list|,
name|objInspector
argument_list|)
return|;
block|}
comment|/**    *  Returns an object inspector for the specified schema that    *  is capable of reading in the object representation of the JSON string    */
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|getObjectInspector
parameter_list|()
throws|throws
name|SerDeException
block|{
return|return
name|cachedObjectInspector
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|getSerializedClass
parameter_list|()
block|{
return|return
name|Text
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|SerDeStats
name|getSerDeStats
parameter_list|()
block|{
comment|// no support for statistics yet
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

