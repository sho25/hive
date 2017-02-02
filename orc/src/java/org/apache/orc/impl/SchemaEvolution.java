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
operator|.
name|impl
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
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|TypeDescription
import|;
end_import

begin_comment
comment|/**  * Take the file types and the (optional) configuration column names/types and see if there  * has been schema evolution.  */
end_comment

begin_class
specifier|public
class|class
name|SchemaEvolution
block|{
comment|// indexed by reader column id
specifier|private
specifier|final
name|TypeDescription
index|[]
name|readerFileTypes
decl_stmt|;
comment|// indexed by reader column id
specifier|private
specifier|final
name|boolean
index|[]
name|readerIncluded
decl_stmt|;
comment|// the offset to the first column id ignoring any ACID columns
specifier|private
specifier|final
name|int
name|readerColumnOffset
decl_stmt|;
comment|// indexed by file column id
specifier|private
specifier|final
name|boolean
index|[]
name|fileIncluded
decl_stmt|;
specifier|private
specifier|final
name|TypeDescription
name|fileSchema
decl_stmt|;
specifier|private
specifier|final
name|TypeDescription
name|readerSchema
decl_stmt|;
specifier|private
name|boolean
name|hasConversion
decl_stmt|;
comment|// indexed by reader column id
specifier|private
specifier|final
name|boolean
index|[]
name|ppdSafeConversion
decl_stmt|;
specifier|public
name|SchemaEvolution
parameter_list|(
name|TypeDescription
name|fileSchema
parameter_list|,
name|boolean
index|[]
name|includedCols
parameter_list|)
block|{
name|this
argument_list|(
name|fileSchema
argument_list|,
literal|null
argument_list|,
name|includedCols
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SchemaEvolution
parameter_list|(
name|TypeDescription
name|fileSchema
parameter_list|,
name|TypeDescription
name|readerSchema
parameter_list|,
name|boolean
index|[]
name|includeCols
parameter_list|)
block|{
name|this
operator|.
name|readerIncluded
operator|=
name|includeCols
operator|==
literal|null
condition|?
literal|null
else|:
name|Arrays
operator|.
name|copyOf
argument_list|(
name|includeCols
argument_list|,
name|includeCols
operator|.
name|length
argument_list|)
expr_stmt|;
name|this
operator|.
name|hasConversion
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|fileSchema
operator|=
name|fileSchema
expr_stmt|;
name|boolean
name|isAcid
init|=
name|checkAcidSchema
argument_list|(
name|fileSchema
argument_list|)
decl_stmt|;
name|this
operator|.
name|readerColumnOffset
operator|=
name|isAcid
condition|?
name|acidEventFieldNames
operator|.
name|size
argument_list|()
else|:
literal|0
expr_stmt|;
if|if
condition|(
name|readerSchema
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|isAcid
condition|)
block|{
name|this
operator|.
name|readerSchema
operator|=
name|createEventSchema
argument_list|(
name|readerSchema
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|readerSchema
operator|=
name|readerSchema
expr_stmt|;
block|}
if|if
condition|(
name|readerIncluded
operator|!=
literal|null
operator|&&
name|readerIncluded
operator|.
name|length
operator|+
name|readerColumnOffset
operator|!=
name|this
operator|.
name|readerSchema
operator|.
name|getMaximumId
argument_list|()
operator|+
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Include vector the wrong length: "
operator|+
name|this
operator|.
name|readerSchema
operator|.
name|toJson
argument_list|()
operator|+
literal|" with include length "
operator|+
name|readerIncluded
operator|.
name|length
argument_list|)
throw|;
block|}
name|this
operator|.
name|readerFileTypes
operator|=
operator|new
name|TypeDescription
index|[
name|this
operator|.
name|readerSchema
operator|.
name|getMaximumId
argument_list|()
operator|+
literal|1
index|]
expr_stmt|;
name|this
operator|.
name|fileIncluded
operator|=
operator|new
name|boolean
index|[
name|fileSchema
operator|.
name|getMaximumId
argument_list|()
operator|+
literal|1
index|]
expr_stmt|;
name|buildConversionFileTypesArray
argument_list|(
name|fileSchema
argument_list|,
name|this
operator|.
name|readerSchema
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|readerSchema
operator|=
name|fileSchema
expr_stmt|;
name|this
operator|.
name|readerFileTypes
operator|=
operator|new
name|TypeDescription
index|[
name|this
operator|.
name|readerSchema
operator|.
name|getMaximumId
argument_list|()
operator|+
literal|1
index|]
expr_stmt|;
name|this
operator|.
name|fileIncluded
operator|=
name|readerIncluded
expr_stmt|;
if|if
condition|(
name|readerIncluded
operator|!=
literal|null
operator|&&
name|readerIncluded
operator|.
name|length
operator|+
name|readerColumnOffset
operator|!=
name|this
operator|.
name|readerSchema
operator|.
name|getMaximumId
argument_list|()
operator|+
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Include vector the wrong length: "
operator|+
name|this
operator|.
name|readerSchema
operator|.
name|toJson
argument_list|()
operator|+
literal|" with include length "
operator|+
name|readerIncluded
operator|.
name|length
argument_list|)
throw|;
block|}
name|buildSameSchemaFileTypesArray
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|ppdSafeConversion
operator|=
name|populatePpdSafeConversion
argument_list|()
expr_stmt|;
block|}
specifier|public
name|TypeDescription
name|getReaderSchema
parameter_list|()
block|{
return|return
name|readerSchema
return|;
block|}
comment|/**    * Returns the non-ACID (aka base) reader type description.    *    * @return the reader type ignoring the ACID rowid columns, if any    */
specifier|public
name|TypeDescription
name|getReaderBaseSchema
parameter_list|()
block|{
return|return
name|readerSchema
operator|.
name|findSubtype
argument_list|(
name|readerColumnOffset
argument_list|)
return|;
block|}
comment|/**    * Is there Schema Evolution data type conversion?    * @return    */
specifier|public
name|boolean
name|hasConversion
parameter_list|()
block|{
return|return
name|hasConversion
return|;
block|}
specifier|public
name|TypeDescription
name|getFileType
parameter_list|(
name|TypeDescription
name|readerType
parameter_list|)
block|{
return|return
name|getFileType
argument_list|(
name|readerType
operator|.
name|getId
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Get whether each column is included from the reader's point of view.    * @return a boolean array indexed by reader column id    */
specifier|public
name|boolean
index|[]
name|getReaderIncluded
parameter_list|()
block|{
return|return
name|readerIncluded
return|;
block|}
comment|/**    * Get whether each column is included from the file's point of view.    * @return a boolean array indexed by file column id    */
specifier|public
name|boolean
index|[]
name|getFileIncluded
parameter_list|()
block|{
return|return
name|fileIncluded
return|;
block|}
comment|/**    * Get the file type by reader type id.    * @param id reader column id    * @return    */
specifier|public
name|TypeDescription
name|getFileType
parameter_list|(
name|int
name|id
parameter_list|)
block|{
return|return
name|readerFileTypes
index|[
name|id
index|]
return|;
block|}
comment|/**    * Check if column is safe for ppd evaluation    * @param colId reader column id    * @return true if the specified column is safe for ppd evaluation else false    */
specifier|public
name|boolean
name|isPPDSafeConversion
parameter_list|(
specifier|final
name|int
name|colId
parameter_list|)
block|{
if|if
condition|(
name|hasConversion
argument_list|()
condition|)
block|{
if|if
condition|(
name|colId
operator|<
literal|0
operator|||
name|colId
operator|>=
name|ppdSafeConversion
operator|.
name|length
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|ppdSafeConversion
index|[
name|colId
index|]
return|;
block|}
comment|// when there is no schema evolution PPD is safe
return|return
literal|true
return|;
block|}
specifier|private
name|boolean
index|[]
name|populatePpdSafeConversion
parameter_list|()
block|{
if|if
condition|(
name|fileSchema
operator|==
literal|null
operator|||
name|readerSchema
operator|==
literal|null
operator|||
name|readerFileTypes
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|boolean
index|[]
name|result
init|=
operator|new
name|boolean
index|[
name|readerSchema
operator|.
name|getMaximumId
argument_list|()
operator|+
literal|1
index|]
decl_stmt|;
name|boolean
name|safePpd
init|=
name|validatePPDConversion
argument_list|(
name|fileSchema
argument_list|,
name|readerSchema
argument_list|)
decl_stmt|;
name|result
index|[
name|readerSchema
operator|.
name|getId
argument_list|()
index|]
operator|=
name|safePpd
expr_stmt|;
name|List
argument_list|<
name|TypeDescription
argument_list|>
name|children
init|=
name|readerSchema
operator|.
name|getChildren
argument_list|()
decl_stmt|;
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
name|TypeDescription
name|fileType
init|=
name|getFileType
argument_list|(
name|child
operator|.
name|getId
argument_list|()
argument_list|)
decl_stmt|;
name|safePpd
operator|=
name|validatePPDConversion
argument_list|(
name|fileType
argument_list|,
name|child
argument_list|)
expr_stmt|;
name|result
index|[
name|child
operator|.
name|getId
argument_list|()
index|]
operator|=
name|safePpd
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
specifier|private
name|boolean
name|validatePPDConversion
parameter_list|(
specifier|final
name|TypeDescription
name|fileType
parameter_list|,
specifier|final
name|TypeDescription
name|readerType
parameter_list|)
block|{
if|if
condition|(
name|fileType
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|fileType
operator|.
name|getCategory
argument_list|()
operator|.
name|isPrimitive
argument_list|()
condition|)
block|{
if|if
condition|(
name|fileType
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|readerType
operator|.
name|getCategory
argument_list|()
argument_list|)
condition|)
block|{
comment|// for decimals alone do equality check to not mess up with precision change
if|if
condition|(
name|fileType
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|TypeDescription
operator|.
name|Category
operator|.
name|DECIMAL
argument_list|)
operator|&&
operator|!
name|fileType
operator|.
name|equals
argument_list|(
name|readerType
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
comment|// only integer and string evolutions are safe
comment|// byte -> short -> int -> long
comment|// string<-> char<-> varchar
comment|// NOTE: Float to double evolution is not safe as floats are stored as doubles in ORC's
comment|// internal index, but when doing predicate evaluation for queries like "select * from
comment|// orc_float where f = 74.72" the constant on the filter is converted from string -> double
comment|// so the precisions will be different and the comparison will fail.
comment|// Soon, we should convert all sargs that compare equality between floats or
comment|// doubles to range predicates.
comment|// Similarly string -> char and varchar -> char and vice versa is not possible, as ORC stores
comment|// char with padded spaces in its internal index.
switch|switch
condition|(
name|fileType
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|BYTE
case|:
if|if
condition|(
name|readerType
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|TypeDescription
operator|.
name|Category
operator|.
name|SHORT
argument_list|)
operator|||
name|readerType
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|TypeDescription
operator|.
name|Category
operator|.
name|INT
argument_list|)
operator|||
name|readerType
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|TypeDescription
operator|.
name|Category
operator|.
name|LONG
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
break|break;
case|case
name|SHORT
case|:
if|if
condition|(
name|readerType
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|TypeDescription
operator|.
name|Category
operator|.
name|INT
argument_list|)
operator|||
name|readerType
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|TypeDescription
operator|.
name|Category
operator|.
name|LONG
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
break|break;
case|case
name|INT
case|:
if|if
condition|(
name|readerType
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|TypeDescription
operator|.
name|Category
operator|.
name|LONG
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
break|break;
case|case
name|STRING
case|:
if|if
condition|(
name|readerType
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|TypeDescription
operator|.
name|Category
operator|.
name|VARCHAR
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
break|break;
case|case
name|VARCHAR
case|:
if|if
condition|(
name|readerType
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|TypeDescription
operator|.
name|Category
operator|.
name|STRING
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
break|break;
default|default:
break|break;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Should we read the given reader column?    * @param readerId the id of column in the extended reader schema    * @return true if the column should be read    */
specifier|public
name|boolean
name|includeReaderColumn
parameter_list|(
name|int
name|readerId
parameter_list|)
block|{
return|return
name|readerIncluded
operator|==
literal|null
operator|||
name|readerId
operator|<=
name|readerColumnOffset
operator|||
name|readerIncluded
index|[
name|readerId
operator|-
name|readerColumnOffset
index|]
return|;
block|}
name|void
name|buildConversionFileTypesArray
parameter_list|(
name|TypeDescription
name|fileType
parameter_list|,
name|TypeDescription
name|readerType
parameter_list|)
block|{
comment|// if the column isn't included, don't map it
name|int
name|readerId
init|=
name|readerType
operator|.
name|getId
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|includeReaderColumn
argument_list|(
name|readerId
argument_list|)
condition|)
block|{
return|return;
block|}
name|boolean
name|isOk
init|=
literal|true
decl_stmt|;
comment|// check the easy case first
if|if
condition|(
name|fileType
operator|.
name|getCategory
argument_list|()
operator|==
name|readerType
operator|.
name|getCategory
argument_list|()
condition|)
block|{
switch|switch
condition|(
name|readerType
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
case|case
name|BYTE
case|:
case|case
name|SHORT
case|:
case|case
name|INT
case|:
case|case
name|LONG
case|:
case|case
name|DOUBLE
case|:
case|case
name|FLOAT
case|:
case|case
name|STRING
case|:
case|case
name|TIMESTAMP
case|:
case|case
name|BINARY
case|:
case|case
name|DATE
case|:
comment|// these are always a match
break|break;
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
comment|// We do conversion when same CHAR/VARCHAR type but different maxLength.
if|if
condition|(
name|fileType
operator|.
name|getMaxLength
argument_list|()
operator|!=
name|readerType
operator|.
name|getMaxLength
argument_list|()
condition|)
block|{
name|hasConversion
operator|=
literal|true
expr_stmt|;
block|}
break|break;
case|case
name|DECIMAL
case|:
comment|// We do conversion when same DECIMAL type but different precision/scale.
if|if
condition|(
name|fileType
operator|.
name|getPrecision
argument_list|()
operator|!=
name|readerType
operator|.
name|getPrecision
argument_list|()
operator|||
name|fileType
operator|.
name|getScale
argument_list|()
operator|!=
name|readerType
operator|.
name|getScale
argument_list|()
condition|)
block|{
name|hasConversion
operator|=
literal|true
expr_stmt|;
block|}
break|break;
case|case
name|UNION
case|:
case|case
name|MAP
case|:
case|case
name|LIST
case|:
block|{
comment|// these must be an exact match
name|List
argument_list|<
name|TypeDescription
argument_list|>
name|fileChildren
init|=
name|fileType
operator|.
name|getChildren
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TypeDescription
argument_list|>
name|readerChildren
init|=
name|readerType
operator|.
name|getChildren
argument_list|()
decl_stmt|;
if|if
condition|(
name|fileChildren
operator|.
name|size
argument_list|()
operator|==
name|readerChildren
operator|.
name|size
argument_list|()
condition|)
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
name|fileChildren
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|buildConversionFileTypesArray
argument_list|(
name|fileChildren
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|readerChildren
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|isOk
operator|=
literal|false
expr_stmt|;
block|}
break|break;
block|}
case|case
name|STRUCT
case|:
block|{
comment|// allow either side to have fewer fields than the other
name|List
argument_list|<
name|TypeDescription
argument_list|>
name|fileChildren
init|=
name|fileType
operator|.
name|getChildren
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TypeDescription
argument_list|>
name|readerChildren
init|=
name|readerType
operator|.
name|getChildren
argument_list|()
decl_stmt|;
if|if
condition|(
name|fileChildren
operator|.
name|size
argument_list|()
operator|!=
name|readerChildren
operator|.
name|size
argument_list|()
condition|)
block|{
name|hasConversion
operator|=
literal|true
expr_stmt|;
block|}
name|int
name|jointSize
init|=
name|Math
operator|.
name|min
argument_list|(
name|fileChildren
operator|.
name|size
argument_list|()
argument_list|,
name|readerChildren
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
name|jointSize
condition|;
operator|++
name|i
control|)
block|{
name|buildConversionFileTypesArray
argument_list|(
name|fileChildren
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|readerChildren
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown type "
operator|+
name|readerType
argument_list|)
throw|;
block|}
block|}
else|else
block|{
comment|/*        * Check for the few cases where will not convert....        */
name|isOk
operator|=
name|ConvertTreeReaderFactory
operator|.
name|canConvert
argument_list|(
name|fileType
argument_list|,
name|readerType
argument_list|)
expr_stmt|;
name|hasConversion
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|isOk
condition|)
block|{
if|if
condition|(
name|readerFileTypes
index|[
name|readerId
index|]
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"reader to file type entry already assigned"
argument_list|)
throw|;
block|}
name|readerFileTypes
index|[
name|readerId
index|]
operator|=
name|fileType
expr_stmt|;
name|fileIncluded
index|[
name|fileType
operator|.
name|getId
argument_list|()
index|]
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"ORC does not support type conversion from file type %s (%d) to reader type %s (%d)"
argument_list|,
name|fileType
operator|.
name|toString
argument_list|()
argument_list|,
name|fileType
operator|.
name|getId
argument_list|()
argument_list|,
name|readerType
operator|.
name|toString
argument_list|()
argument_list|,
name|readerId
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|/**    * Use to make a reader to file type array when the schema is the same.    * @return    */
specifier|private
name|void
name|buildSameSchemaFileTypesArray
parameter_list|()
block|{
name|buildSameSchemaFileTypesArrayRecurse
argument_list|(
name|readerSchema
argument_list|)
expr_stmt|;
block|}
name|void
name|buildSameSchemaFileTypesArrayRecurse
parameter_list|(
name|TypeDescription
name|readerType
parameter_list|)
block|{
name|int
name|id
init|=
name|readerType
operator|.
name|getId
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|includeReaderColumn
argument_list|(
name|id
argument_list|)
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|readerFileTypes
index|[
name|id
index|]
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"reader to file type entry already assigned"
argument_list|)
throw|;
block|}
name|readerFileTypes
index|[
name|id
index|]
operator|=
name|readerType
expr_stmt|;
name|List
argument_list|<
name|TypeDescription
argument_list|>
name|children
init|=
name|readerType
operator|.
name|getChildren
argument_list|()
decl_stmt|;
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
name|buildSameSchemaFileTypesArrayRecurse
argument_list|(
name|child
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
name|boolean
name|checkAcidSchema
parameter_list|(
name|TypeDescription
name|type
parameter_list|)
block|{
if|if
condition|(
name|type
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|TypeDescription
operator|.
name|Category
operator|.
name|STRUCT
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|rootFields
init|=
name|type
operator|.
name|getFieldNames
argument_list|()
decl_stmt|;
if|if
condition|(
name|acidEventFieldNames
operator|.
name|equals
argument_list|(
name|rootFields
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * @param typeDescr    * @return ORC types for the ACID event based on the row's type description    */
specifier|public
specifier|static
name|TypeDescription
name|createEventSchema
parameter_list|(
name|TypeDescription
name|typeDescr
parameter_list|)
block|{
name|TypeDescription
name|result
init|=
name|TypeDescription
operator|.
name|createStruct
argument_list|()
operator|.
name|addField
argument_list|(
literal|"operation"
argument_list|,
name|TypeDescription
operator|.
name|createInt
argument_list|()
argument_list|)
operator|.
name|addField
argument_list|(
literal|"originalTransaction"
argument_list|,
name|TypeDescription
operator|.
name|createLong
argument_list|()
argument_list|)
operator|.
name|addField
argument_list|(
literal|"bucket"
argument_list|,
name|TypeDescription
operator|.
name|createInt
argument_list|()
argument_list|)
operator|.
name|addField
argument_list|(
literal|"rowId"
argument_list|,
name|TypeDescription
operator|.
name|createLong
argument_list|()
argument_list|)
operator|.
name|addField
argument_list|(
literal|"currentTransaction"
argument_list|,
name|TypeDescription
operator|.
name|createLong
argument_list|()
argument_list|)
operator|.
name|addField
argument_list|(
literal|"row"
argument_list|,
name|typeDescr
operator|.
name|clone
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|result
return|;
block|}
specifier|public
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|acidEventFieldNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|acidEventFieldNames
operator|.
name|add
argument_list|(
literal|"operation"
argument_list|)
expr_stmt|;
name|acidEventFieldNames
operator|.
name|add
argument_list|(
literal|"originalTransaction"
argument_list|)
expr_stmt|;
name|acidEventFieldNames
operator|.
name|add
argument_list|(
literal|"bucket"
argument_list|)
expr_stmt|;
name|acidEventFieldNames
operator|.
name|add
argument_list|(
literal|"rowId"
argument_list|)
expr_stmt|;
name|acidEventFieldNames
operator|.
name|add
argument_list|(
literal|"currentTransaction"
argument_list|)
expr_stmt|;
name|acidEventFieldNames
operator|.
name|add
argument_list|(
literal|"row"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

