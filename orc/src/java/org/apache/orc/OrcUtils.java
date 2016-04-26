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
name|orc
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
name|List
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_class
specifier|public
class|class
name|OrcUtils
block|{
comment|/**    * Returns selected columns as a boolean array with true value set for specified column names.    * The result will contain number of elements equal to flattened number of columns.    * For example:    * selectedColumns - a,b,c    * allColumns - a,b,c,d    * If column c is a complex type, say list<string> and other types are primitives then result will    * be [false, true, true, true, true, true, false]    * Index 0 is the root element of the struct which is set to false by default, index 1,2    * corresponds to columns a and b. Index 3,4 correspond to column c which is list<string> and    * index 5 correspond to column d. After flattening list<string> gets 2 columns.    *    * @param selectedColumns - comma separated list of selected column names    * @param schema       - object schema    * @return - boolean array with true value set for the specified column names    */
specifier|public
specifier|static
name|boolean
index|[]
name|includeColumns
parameter_list|(
name|String
name|selectedColumns
parameter_list|,
name|TypeDescription
name|schema
parameter_list|)
block|{
name|int
name|numFlattenedCols
init|=
name|schema
operator|.
name|getMaximumId
argument_list|()
decl_stmt|;
name|boolean
index|[]
name|results
init|=
operator|new
name|boolean
index|[
name|numFlattenedCols
operator|+
literal|1
index|]
decl_stmt|;
if|if
condition|(
literal|"*"
operator|.
name|equals
argument_list|(
name|selectedColumns
argument_list|)
condition|)
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|results
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
name|results
return|;
block|}
if|if
condition|(
name|selectedColumns
operator|!=
literal|null
operator|&&
name|schema
operator|.
name|getCategory
argument_list|()
operator|==
name|TypeDescription
operator|.
name|Category
operator|.
name|STRUCT
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
name|schema
operator|.
name|getFieldNames
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TypeDescription
argument_list|>
name|fields
init|=
name|schema
operator|.
name|getChildren
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|column
range|:
name|selectedColumns
operator|.
name|split
argument_list|(
operator|(
literal|","
operator|)
argument_list|)
control|)
block|{
name|TypeDescription
name|col
init|=
name|findColumn
argument_list|(
name|column
argument_list|,
name|fieldNames
argument_list|,
name|fields
argument_list|)
decl_stmt|;
if|if
condition|(
name|col
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
name|col
operator|.
name|getId
argument_list|()
init|;
name|i
operator|<=
name|col
operator|.
name|getMaximumId
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|results
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|results
return|;
block|}
specifier|private
specifier|static
name|TypeDescription
name|findColumn
parameter_list|(
name|String
name|columnName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
parameter_list|,
name|List
argument_list|<
name|TypeDescription
argument_list|>
name|fields
parameter_list|)
block|{
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|fieldName
range|:
name|fieldNames
control|)
block|{
if|if
condition|(
name|fieldName
operator|.
name|equalsIgnoreCase
argument_list|(
name|columnName
argument_list|)
condition|)
block|{
return|return
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
return|;
block|}
else|else
block|{
name|i
operator|+=
literal|1
expr_stmt|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|getOrcTypes
parameter_list|(
name|TypeDescription
name|typeDescr
parameter_list|)
block|{
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|result
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|appendOrcTypes
argument_list|(
name|result
argument_list|,
name|typeDescr
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|private
specifier|static
name|void
name|appendOrcTypes
parameter_list|(
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|result
parameter_list|,
name|TypeDescription
name|typeDescr
parameter_list|)
block|{
name|OrcProto
operator|.
name|Type
operator|.
name|Builder
name|type
init|=
name|OrcProto
operator|.
name|Type
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TypeDescription
argument_list|>
name|children
init|=
name|typeDescr
operator|.
name|getChildren
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|typeDescr
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|BOOLEAN
argument_list|)
expr_stmt|;
break|break;
case|case
name|BYTE
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|BYTE
argument_list|)
expr_stmt|;
break|break;
case|case
name|SHORT
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|SHORT
argument_list|)
expr_stmt|;
break|break;
case|case
name|INT
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|INT
argument_list|)
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|LONG
argument_list|)
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|FLOAT
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|DOUBLE
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRING
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|STRING
argument_list|)
expr_stmt|;
break|break;
case|case
name|CHAR
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|CHAR
argument_list|)
expr_stmt|;
name|type
operator|.
name|setMaximumLength
argument_list|(
name|typeDescr
operator|.
name|getMaxLength
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|VARCHAR
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|VARCHAR
argument_list|)
expr_stmt|;
name|type
operator|.
name|setMaximumLength
argument_list|(
name|typeDescr
operator|.
name|getMaxLength
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|BINARY
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|BINARY
argument_list|)
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|TIMESTAMP
argument_list|)
expr_stmt|;
break|break;
case|case
name|DATE
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|DATE
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|DECIMAL
argument_list|)
expr_stmt|;
name|type
operator|.
name|setPrecision
argument_list|(
name|typeDescr
operator|.
name|getPrecision
argument_list|()
argument_list|)
expr_stmt|;
name|type
operator|.
name|setScale
argument_list|(
name|typeDescr
operator|.
name|getScale
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|LIST
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|LIST
argument_list|)
expr_stmt|;
name|type
operator|.
name|addSubtypes
argument_list|(
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|MAP
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|MAP
argument_list|)
expr_stmt|;
for|for
control|(
name|TypeDescription
name|t
range|:
name|children
control|)
block|{
name|type
operator|.
name|addSubtypes
argument_list|(
name|t
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|STRUCT
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|STRUCT
argument_list|)
expr_stmt|;
for|for
control|(
name|TypeDescription
name|t
range|:
name|children
control|)
block|{
name|type
operator|.
name|addSubtypes
argument_list|(
name|t
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|field
range|:
name|typeDescr
operator|.
name|getFieldNames
argument_list|()
control|)
block|{
name|type
operator|.
name|addFieldNames
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|UNION
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|UNION
argument_list|)
expr_stmt|;
for|for
control|(
name|TypeDescription
name|t
range|:
name|children
control|)
block|{
name|type
operator|.
name|addSubtypes
argument_list|(
name|t
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown category: "
operator|+
name|typeDescr
operator|.
name|getCategory
argument_list|()
argument_list|)
throw|;
block|}
name|result
operator|.
name|add
argument_list|(
name|type
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|children
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|TypeDescription
name|child
range|:
name|children
control|)
block|{
name|appendOrcTypes
argument_list|(
name|result
argument_list|,
name|child
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * NOTE: This method ignores the subtype numbers in the TypeDescription rebuilds the subtype    * numbers based on the length of the result list being appended.    *    * @param result    * @param typeDescr    */
specifier|public
specifier|static
name|void
name|appendOrcTypesRebuildSubtypes
parameter_list|(
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|result
parameter_list|,
name|TypeDescription
name|typeDescr
parameter_list|)
block|{
name|int
name|subtype
init|=
name|result
operator|.
name|size
argument_list|()
decl_stmt|;
name|OrcProto
operator|.
name|Type
operator|.
name|Builder
name|type
init|=
name|OrcProto
operator|.
name|Type
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|boolean
name|needsAdd
init|=
literal|true
decl_stmt|;
name|List
argument_list|<
name|TypeDescription
argument_list|>
name|children
init|=
name|typeDescr
operator|.
name|getChildren
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|typeDescr
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|BOOLEAN
argument_list|)
expr_stmt|;
break|break;
case|case
name|BYTE
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|BYTE
argument_list|)
expr_stmt|;
break|break;
case|case
name|SHORT
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|SHORT
argument_list|)
expr_stmt|;
break|break;
case|case
name|INT
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|INT
argument_list|)
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|LONG
argument_list|)
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|FLOAT
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|DOUBLE
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRING
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|STRING
argument_list|)
expr_stmt|;
break|break;
case|case
name|CHAR
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|CHAR
argument_list|)
expr_stmt|;
name|type
operator|.
name|setMaximumLength
argument_list|(
name|typeDescr
operator|.
name|getMaxLength
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|VARCHAR
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|VARCHAR
argument_list|)
expr_stmt|;
name|type
operator|.
name|setMaximumLength
argument_list|(
name|typeDescr
operator|.
name|getMaxLength
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|BINARY
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|BINARY
argument_list|)
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|TIMESTAMP
argument_list|)
expr_stmt|;
break|break;
case|case
name|DATE
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|DATE
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|DECIMAL
argument_list|)
expr_stmt|;
name|type
operator|.
name|setPrecision
argument_list|(
name|typeDescr
operator|.
name|getPrecision
argument_list|()
argument_list|)
expr_stmt|;
name|type
operator|.
name|setScale
argument_list|(
name|typeDescr
operator|.
name|getScale
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|LIST
case|:
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|LIST
argument_list|)
expr_stmt|;
name|type
operator|.
name|addSubtypes
argument_list|(
operator|++
name|subtype
argument_list|)
expr_stmt|;
name|result
operator|.
name|add
argument_list|(
name|type
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|needsAdd
operator|=
literal|false
expr_stmt|;
name|appendOrcTypesRebuildSubtypes
argument_list|(
name|result
argument_list|,
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|MAP
case|:
block|{
comment|// Make room for MAP type.
name|result
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
comment|// Add MAP type pair in order to determine their subtype values.
name|appendOrcTypesRebuildSubtypes
argument_list|(
name|result
argument_list|,
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|subtype2
init|=
name|result
operator|.
name|size
argument_list|()
decl_stmt|;
name|appendOrcTypesRebuildSubtypes
argument_list|(
name|result
argument_list|,
name|children
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|MAP
argument_list|)
expr_stmt|;
name|type
operator|.
name|addSubtypes
argument_list|(
name|subtype
operator|+
literal|1
argument_list|)
expr_stmt|;
name|type
operator|.
name|addSubtypes
argument_list|(
name|subtype2
argument_list|)
expr_stmt|;
name|result
operator|.
name|set
argument_list|(
name|subtype
argument_list|,
name|type
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|needsAdd
operator|=
literal|false
expr_stmt|;
block|}
break|break;
case|case
name|STRUCT
case|:
block|{
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
name|typeDescr
operator|.
name|getFieldNames
argument_list|()
decl_stmt|;
comment|// Make room for STRUCT type.
name|result
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|fieldSubtypes
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|fieldNames
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|TypeDescription
name|child
range|:
name|children
control|)
block|{
name|int
name|fieldSubtype
init|=
name|result
operator|.
name|size
argument_list|()
decl_stmt|;
name|fieldSubtypes
operator|.
name|add
argument_list|(
name|fieldSubtype
argument_list|)
expr_stmt|;
name|appendOrcTypesRebuildSubtypes
argument_list|(
name|result
argument_list|,
name|child
argument_list|)
expr_stmt|;
block|}
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|STRUCT
argument_list|)
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
name|fieldNames
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|type
operator|.
name|addSubtypes
argument_list|(
name|fieldSubtypes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|type
operator|.
name|addFieldNames
argument_list|(
name|fieldNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|set
argument_list|(
name|subtype
argument_list|,
name|type
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|needsAdd
operator|=
literal|false
expr_stmt|;
block|}
break|break;
case|case
name|UNION
case|:
block|{
comment|// Make room for UNION type.
name|result
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|unionSubtypes
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|children
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|TypeDescription
name|child
range|:
name|children
control|)
block|{
name|int
name|unionSubtype
init|=
name|result
operator|.
name|size
argument_list|()
decl_stmt|;
name|unionSubtypes
operator|.
name|add
argument_list|(
name|unionSubtype
argument_list|)
expr_stmt|;
name|appendOrcTypesRebuildSubtypes
argument_list|(
name|result
argument_list|,
name|child
argument_list|)
expr_stmt|;
block|}
name|type
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|UNION
argument_list|)
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
name|children
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|type
operator|.
name|addSubtypes
argument_list|(
name|unionSubtypes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|set
argument_list|(
name|subtype
argument_list|,
name|type
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|needsAdd
operator|=
literal|false
expr_stmt|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown category: "
operator|+
name|typeDescr
operator|.
name|getCategory
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|needsAdd
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|type
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * NOTE: This method ignores the subtype numbers in the OrcProto.Type rebuilds the subtype    * numbers based on the length of the result list being appended.    *    * @param result    * @param types    * @param columnId    */
specifier|public
specifier|static
name|int
name|appendOrcTypesRebuildSubtypes
parameter_list|(
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|result
parameter_list|,
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|types
parameter_list|,
name|int
name|columnId
parameter_list|)
block|{
name|OrcProto
operator|.
name|Type
name|oldType
init|=
name|types
operator|.
name|get
argument_list|(
name|columnId
operator|++
argument_list|)
decl_stmt|;
name|int
name|subtype
init|=
name|result
operator|.
name|size
argument_list|()
decl_stmt|;
name|OrcProto
operator|.
name|Type
operator|.
name|Builder
name|builder
init|=
name|OrcProto
operator|.
name|Type
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|boolean
name|needsAdd
init|=
literal|true
decl_stmt|;
switch|switch
condition|(
name|oldType
operator|.
name|getKind
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
name|builder
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|BOOLEAN
argument_list|)
expr_stmt|;
break|break;
case|case
name|BYTE
case|:
name|builder
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|BYTE
argument_list|)
expr_stmt|;
break|break;
case|case
name|SHORT
case|:
name|builder
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|SHORT
argument_list|)
expr_stmt|;
break|break;
case|case
name|INT
case|:
name|builder
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|INT
argument_list|)
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|builder
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|LONG
argument_list|)
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
name|builder
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|FLOAT
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|builder
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|DOUBLE
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRING
case|:
name|builder
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|STRING
argument_list|)
expr_stmt|;
break|break;
case|case
name|CHAR
case|:
name|builder
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|CHAR
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setMaximumLength
argument_list|(
name|oldType
operator|.
name|getMaximumLength
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|VARCHAR
case|:
name|builder
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|VARCHAR
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setMaximumLength
argument_list|(
name|oldType
operator|.
name|getMaximumLength
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|BINARY
case|:
name|builder
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|BINARY
argument_list|)
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|builder
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|TIMESTAMP
argument_list|)
expr_stmt|;
break|break;
case|case
name|DATE
case|:
name|builder
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|DATE
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|builder
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|DECIMAL
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setPrecision
argument_list|(
name|oldType
operator|.
name|getPrecision
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setScale
argument_list|(
name|oldType
operator|.
name|getScale
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|LIST
case|:
name|builder
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|LIST
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addSubtypes
argument_list|(
operator|++
name|subtype
argument_list|)
expr_stmt|;
name|result
operator|.
name|add
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|needsAdd
operator|=
literal|false
expr_stmt|;
name|columnId
operator|=
name|appendOrcTypesRebuildSubtypes
argument_list|(
name|result
argument_list|,
name|types
argument_list|,
name|columnId
argument_list|)
expr_stmt|;
break|break;
case|case
name|MAP
case|:
block|{
comment|// Make room for MAP type.
name|result
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
comment|// Add MAP type pair in order to determine their subtype values.
name|columnId
operator|=
name|appendOrcTypesRebuildSubtypes
argument_list|(
name|result
argument_list|,
name|types
argument_list|,
name|columnId
argument_list|)
expr_stmt|;
name|int
name|subtype2
init|=
name|result
operator|.
name|size
argument_list|()
decl_stmt|;
name|columnId
operator|=
name|appendOrcTypesRebuildSubtypes
argument_list|(
name|result
argument_list|,
name|types
argument_list|,
name|columnId
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|MAP
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addSubtypes
argument_list|(
name|subtype
operator|+
literal|1
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addSubtypes
argument_list|(
name|subtype2
argument_list|)
expr_stmt|;
name|result
operator|.
name|set
argument_list|(
name|subtype
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|needsAdd
operator|=
literal|false
expr_stmt|;
block|}
break|break;
case|case
name|STRUCT
case|:
block|{
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
name|oldType
operator|.
name|getFieldNamesList
argument_list|()
decl_stmt|;
comment|// Make room for STRUCT type.
name|result
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|fieldSubtypes
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|fieldNames
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
name|fieldNames
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|int
name|fieldSubtype
init|=
name|result
operator|.
name|size
argument_list|()
decl_stmt|;
name|fieldSubtypes
operator|.
name|add
argument_list|(
name|fieldSubtype
argument_list|)
expr_stmt|;
name|columnId
operator|=
name|appendOrcTypesRebuildSubtypes
argument_list|(
name|result
argument_list|,
name|types
argument_list|,
name|columnId
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|STRUCT
argument_list|)
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
name|fieldNames
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|addSubtypes
argument_list|(
name|fieldSubtypes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addFieldNames
argument_list|(
name|fieldNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|set
argument_list|(
name|subtype
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|needsAdd
operator|=
literal|false
expr_stmt|;
block|}
break|break;
case|case
name|UNION
case|:
block|{
name|int
name|subtypeCount
init|=
name|oldType
operator|.
name|getSubtypesCount
argument_list|()
decl_stmt|;
comment|// Make room for UNION type.
name|result
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|unionSubtypes
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|subtypeCount
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
name|subtypeCount
condition|;
name|i
operator|++
control|)
block|{
name|int
name|unionSubtype
init|=
name|result
operator|.
name|size
argument_list|()
decl_stmt|;
name|unionSubtypes
operator|.
name|add
argument_list|(
name|unionSubtype
argument_list|)
expr_stmt|;
name|columnId
operator|=
name|appendOrcTypesRebuildSubtypes
argument_list|(
name|result
argument_list|,
name|types
argument_list|,
name|columnId
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|setKind
argument_list|(
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|UNION
argument_list|)
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
name|subtypeCount
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|addSubtypes
argument_list|(
name|unionSubtypes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|set
argument_list|(
name|subtype
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|needsAdd
operator|=
literal|false
expr_stmt|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown category: "
operator|+
name|oldType
operator|.
name|getKind
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|needsAdd
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|columnId
return|;
block|}
comment|/**    * Translate the given rootColumn from the list of types to a TypeDescription.    * @param types all of the types    * @param rootColumn translate this type    * @return a new TypeDescription that matches the given rootColumn    */
specifier|public
specifier|static
name|TypeDescription
name|convertTypeFromProtobuf
parameter_list|(
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|types
parameter_list|,
name|int
name|rootColumn
parameter_list|)
block|{
name|OrcProto
operator|.
name|Type
name|type
init|=
name|types
operator|.
name|get
argument_list|(
name|rootColumn
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|type
operator|.
name|getKind
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
return|return
name|TypeDescription
operator|.
name|createBoolean
argument_list|()
return|;
case|case
name|BYTE
case|:
return|return
name|TypeDescription
operator|.
name|createByte
argument_list|()
return|;
case|case
name|SHORT
case|:
return|return
name|TypeDescription
operator|.
name|createShort
argument_list|()
return|;
case|case
name|INT
case|:
return|return
name|TypeDescription
operator|.
name|createInt
argument_list|()
return|;
case|case
name|LONG
case|:
return|return
name|TypeDescription
operator|.
name|createLong
argument_list|()
return|;
case|case
name|FLOAT
case|:
return|return
name|TypeDescription
operator|.
name|createFloat
argument_list|()
return|;
case|case
name|DOUBLE
case|:
return|return
name|TypeDescription
operator|.
name|createDouble
argument_list|()
return|;
case|case
name|STRING
case|:
return|return
name|TypeDescription
operator|.
name|createString
argument_list|()
return|;
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
block|{
name|TypeDescription
name|result
init|=
name|type
operator|.
name|getKind
argument_list|()
operator|==
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|CHAR
condition|?
name|TypeDescription
operator|.
name|createChar
argument_list|()
else|:
name|TypeDescription
operator|.
name|createVarchar
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|.
name|hasMaximumLength
argument_list|()
condition|)
block|{
name|result
operator|.
name|withMaxLength
argument_list|(
name|type
operator|.
name|getMaximumLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
case|case
name|BINARY
case|:
return|return
name|TypeDescription
operator|.
name|createBinary
argument_list|()
return|;
case|case
name|TIMESTAMP
case|:
return|return
name|TypeDescription
operator|.
name|createTimestamp
argument_list|()
return|;
case|case
name|DATE
case|:
return|return
name|TypeDescription
operator|.
name|createDate
argument_list|()
return|;
case|case
name|DECIMAL
case|:
block|{
name|TypeDescription
name|result
init|=
name|TypeDescription
operator|.
name|createDecimal
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|.
name|hasScale
argument_list|()
condition|)
block|{
name|result
operator|.
name|withScale
argument_list|(
name|type
operator|.
name|getScale
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|type
operator|.
name|hasPrecision
argument_list|()
condition|)
block|{
name|result
operator|.
name|withPrecision
argument_list|(
name|type
operator|.
name|getPrecision
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
case|case
name|LIST
case|:
return|return
name|TypeDescription
operator|.
name|createList
argument_list|(
name|convertTypeFromProtobuf
argument_list|(
name|types
argument_list|,
name|type
operator|.
name|getSubtypes
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
return|;
case|case
name|MAP
case|:
return|return
name|TypeDescription
operator|.
name|createMap
argument_list|(
name|convertTypeFromProtobuf
argument_list|(
name|types
argument_list|,
name|type
operator|.
name|getSubtypes
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|,
name|convertTypeFromProtobuf
argument_list|(
name|types
argument_list|,
name|type
operator|.
name|getSubtypes
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
return|;
case|case
name|STRUCT
case|:
block|{
name|TypeDescription
name|result
init|=
name|TypeDescription
operator|.
name|createStruct
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|f
init|=
literal|0
init|;
name|f
operator|<
name|type
operator|.
name|getSubtypesCount
argument_list|()
condition|;
operator|++
name|f
control|)
block|{
name|result
operator|.
name|addField
argument_list|(
name|type
operator|.
name|getFieldNames
argument_list|(
name|f
argument_list|)
argument_list|,
name|convertTypeFromProtobuf
argument_list|(
name|types
argument_list|,
name|type
operator|.
name|getSubtypes
argument_list|(
name|f
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
case|case
name|UNION
case|:
block|{
name|TypeDescription
name|result
init|=
name|TypeDescription
operator|.
name|createUnion
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|f
init|=
literal|0
init|;
name|f
operator|<
name|type
operator|.
name|getSubtypesCount
argument_list|()
condition|;
operator|++
name|f
control|)
block|{
name|result
operator|.
name|addUnionChild
argument_list|(
name|convertTypeFromProtobuf
argument_list|(
name|types
argument_list|,
name|type
operator|.
name|getSubtypes
argument_list|(
name|f
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown ORC type "
operator|+
name|type
operator|.
name|getKind
argument_list|()
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

