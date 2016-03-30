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
name|exec
operator|.
name|vector
package|;
end_package

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
name|vector
operator|.
name|ColumnVector
operator|.
name|Type
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
name|TypeInfoUtils
import|;
end_import

begin_comment
comment|/**  * Class to keep information on a set of typed vector columns.  Used by  * other classes to efficiently access the set of columns.  */
end_comment

begin_class
specifier|public
class|class
name|VectorColumnSetInfo
block|{
comment|// For simpler access, we make these members protected instead of
comment|// providing get methods.
comment|/**    * indices of LONG primitive keys.    */
specifier|protected
name|int
index|[]
name|longIndices
decl_stmt|;
comment|/**    * indices of DOUBLE primitive keys.    */
specifier|protected
name|int
index|[]
name|doubleIndices
decl_stmt|;
comment|/**    * indices of string (byte[]) primitive keys.    */
specifier|protected
name|int
index|[]
name|stringIndices
decl_stmt|;
comment|/**    * indices of decimal primitive keys.    */
specifier|protected
name|int
index|[]
name|decimalIndices
decl_stmt|;
comment|/**    * indices of TIMESTAMP primitive keys.    */
specifier|protected
name|int
index|[]
name|timestampIndices
decl_stmt|;
comment|/**    * indices of INTERVAL_DAY_TIME primitive keys.    */
specifier|protected
name|int
index|[]
name|intervalDayTimeIndices
decl_stmt|;
comment|/**    * Helper class for looking up a key value based on key index.    */
specifier|public
class|class
name|KeyLookupHelper
block|{
specifier|public
name|int
name|longIndex
decl_stmt|;
specifier|public
name|int
name|doubleIndex
decl_stmt|;
specifier|public
name|int
name|stringIndex
decl_stmt|;
specifier|public
name|int
name|decimalIndex
decl_stmt|;
specifier|public
name|int
name|timestampIndex
decl_stmt|;
specifier|public
name|int
name|intervalDayTimeIndex
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|INDEX_UNUSED
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|void
name|resetIndices
parameter_list|()
block|{
name|this
operator|.
name|longIndex
operator|=
name|this
operator|.
name|doubleIndex
operator|=
name|this
operator|.
name|stringIndex
operator|=
name|this
operator|.
name|decimalIndex
operator|=
name|timestampIndex
operator|=
name|intervalDayTimeIndex
operator|=
name|INDEX_UNUSED
expr_stmt|;
block|}
specifier|public
name|void
name|setLong
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|resetIndices
argument_list|()
expr_stmt|;
name|this
operator|.
name|longIndex
operator|=
name|index
expr_stmt|;
block|}
specifier|public
name|void
name|setDouble
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|resetIndices
argument_list|()
expr_stmt|;
name|this
operator|.
name|doubleIndex
operator|=
name|index
expr_stmt|;
block|}
specifier|public
name|void
name|setString
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|resetIndices
argument_list|()
expr_stmt|;
name|this
operator|.
name|stringIndex
operator|=
name|index
expr_stmt|;
block|}
specifier|public
name|void
name|setDecimal
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|resetIndices
argument_list|()
expr_stmt|;
name|this
operator|.
name|decimalIndex
operator|=
name|index
expr_stmt|;
block|}
specifier|public
name|void
name|setTimestamp
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|resetIndices
argument_list|()
expr_stmt|;
name|this
operator|.
name|timestampIndex
operator|=
name|index
expr_stmt|;
block|}
specifier|public
name|void
name|setIntervalDayTime
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|resetIndices
argument_list|()
expr_stmt|;
name|this
operator|.
name|intervalDayTimeIndex
operator|=
name|index
expr_stmt|;
block|}
block|}
comment|/**    * Lookup vector to map from key index to primitive type index.    */
specifier|protected
name|KeyLookupHelper
index|[]
name|indexLookup
decl_stmt|;
specifier|private
name|int
name|keyCount
decl_stmt|;
specifier|private
name|int
name|addIndex
decl_stmt|;
specifier|protected
name|int
name|longIndicesIndex
decl_stmt|;
specifier|protected
name|int
name|doubleIndicesIndex
decl_stmt|;
specifier|protected
name|int
name|stringIndicesIndex
decl_stmt|;
specifier|protected
name|int
name|decimalIndicesIndex
decl_stmt|;
specifier|protected
name|int
name|timestampIndicesIndex
decl_stmt|;
specifier|protected
name|int
name|intervalDayTimeIndicesIndex
decl_stmt|;
specifier|protected
name|VectorColumnSetInfo
parameter_list|(
name|int
name|keyCount
parameter_list|)
block|{
name|this
operator|.
name|keyCount
operator|=
name|keyCount
expr_stmt|;
name|this
operator|.
name|addIndex
operator|=
literal|0
expr_stmt|;
comment|// We'll over allocate and then shrink the array for each type
name|longIndices
operator|=
operator|new
name|int
index|[
name|this
operator|.
name|keyCount
index|]
expr_stmt|;
name|longIndicesIndex
operator|=
literal|0
expr_stmt|;
name|doubleIndices
operator|=
operator|new
name|int
index|[
name|this
operator|.
name|keyCount
index|]
expr_stmt|;
name|doubleIndicesIndex
operator|=
literal|0
expr_stmt|;
name|stringIndices
operator|=
operator|new
name|int
index|[
name|this
operator|.
name|keyCount
index|]
expr_stmt|;
name|stringIndicesIndex
operator|=
literal|0
expr_stmt|;
name|decimalIndices
operator|=
operator|new
name|int
index|[
name|this
operator|.
name|keyCount
index|]
expr_stmt|;
name|decimalIndicesIndex
operator|=
literal|0
expr_stmt|;
name|timestampIndices
operator|=
operator|new
name|int
index|[
name|this
operator|.
name|keyCount
index|]
expr_stmt|;
name|timestampIndicesIndex
operator|=
literal|0
expr_stmt|;
name|intervalDayTimeIndices
operator|=
operator|new
name|int
index|[
name|this
operator|.
name|keyCount
index|]
expr_stmt|;
name|intervalDayTimeIndicesIndex
operator|=
literal|0
expr_stmt|;
name|indexLookup
operator|=
operator|new
name|KeyLookupHelper
index|[
name|this
operator|.
name|keyCount
index|]
expr_stmt|;
block|}
specifier|protected
name|void
name|addKey
parameter_list|(
name|String
name|outputType
parameter_list|)
throws|throws
name|HiveException
block|{
name|indexLookup
index|[
name|addIndex
index|]
operator|=
operator|new
name|KeyLookupHelper
argument_list|()
expr_stmt|;
name|String
name|typeName
init|=
name|VectorizationContext
operator|.
name|mapTypeNameSynonyms
argument_list|(
name|outputType
argument_list|)
decl_stmt|;
name|TypeInfo
name|typeInfo
init|=
name|TypeInfoUtils
operator|.
name|getTypeInfoFromTypeString
argument_list|(
name|typeName
argument_list|)
decl_stmt|;
name|Type
name|columnVectorType
init|=
name|VectorizationContext
operator|.
name|getColumnVectorTypeFromTypeInfo
argument_list|(
name|typeInfo
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|columnVectorType
condition|)
block|{
case|case
name|LONG
case|:
name|longIndices
index|[
name|longIndicesIndex
index|]
operator|=
name|addIndex
expr_stmt|;
name|indexLookup
index|[
name|addIndex
index|]
operator|.
name|setLong
argument_list|(
name|longIndicesIndex
argument_list|)
expr_stmt|;
operator|++
name|longIndicesIndex
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|doubleIndices
index|[
name|doubleIndicesIndex
index|]
operator|=
name|addIndex
expr_stmt|;
name|indexLookup
index|[
name|addIndex
index|]
operator|.
name|setDouble
argument_list|(
name|doubleIndicesIndex
argument_list|)
expr_stmt|;
operator|++
name|doubleIndicesIndex
expr_stmt|;
break|break;
case|case
name|BYTES
case|:
name|stringIndices
index|[
name|stringIndicesIndex
index|]
operator|=
name|addIndex
expr_stmt|;
name|indexLookup
index|[
name|addIndex
index|]
operator|.
name|setString
argument_list|(
name|stringIndicesIndex
argument_list|)
expr_stmt|;
operator|++
name|stringIndicesIndex
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|decimalIndices
index|[
name|decimalIndicesIndex
index|]
operator|=
name|addIndex
expr_stmt|;
name|indexLookup
index|[
name|addIndex
index|]
operator|.
name|setDecimal
argument_list|(
name|decimalIndicesIndex
argument_list|)
expr_stmt|;
operator|++
name|decimalIndicesIndex
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|timestampIndices
index|[
name|timestampIndicesIndex
index|]
operator|=
name|addIndex
expr_stmt|;
name|indexLookup
index|[
name|addIndex
index|]
operator|.
name|setTimestamp
argument_list|(
name|timestampIndicesIndex
argument_list|)
expr_stmt|;
operator|++
name|timestampIndicesIndex
expr_stmt|;
break|break;
case|case
name|INTERVAL_DAY_TIME
case|:
name|intervalDayTimeIndices
index|[
name|intervalDayTimeIndicesIndex
index|]
operator|=
name|addIndex
expr_stmt|;
name|indexLookup
index|[
name|addIndex
index|]
operator|.
name|setIntervalDayTime
argument_list|(
name|intervalDayTimeIndicesIndex
argument_list|)
expr_stmt|;
operator|++
name|intervalDayTimeIndicesIndex
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Unexpected column vector type "
operator|+
name|columnVectorType
argument_list|)
throw|;
block|}
name|addIndex
operator|++
expr_stmt|;
block|}
specifier|protected
name|void
name|finishAdding
parameter_list|()
block|{
name|longIndices
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|longIndices
argument_list|,
name|longIndicesIndex
argument_list|)
expr_stmt|;
name|doubleIndices
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|doubleIndices
argument_list|,
name|doubleIndicesIndex
argument_list|)
expr_stmt|;
name|stringIndices
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|stringIndices
argument_list|,
name|stringIndicesIndex
argument_list|)
expr_stmt|;
name|decimalIndices
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|decimalIndices
argument_list|,
name|decimalIndicesIndex
argument_list|)
expr_stmt|;
name|timestampIndices
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|timestampIndices
argument_list|,
name|timestampIndicesIndex
argument_list|)
expr_stmt|;
name|intervalDayTimeIndices
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|intervalDayTimeIndices
argument_list|,
name|intervalDayTimeIndicesIndex
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

