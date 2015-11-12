begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|parquet
operator|.
name|convert
package|;
end_package

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigDecimal
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
import|;
end_import

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
name|ql
operator|.
name|io
operator|.
name|parquet
operator|.
name|timestamp
operator|.
name|NanoTime
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
name|io
operator|.
name|parquet
operator|.
name|timestamp
operator|.
name|NanoTimeUtils
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
name|io
operator|.
name|DateWritable
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
name|io
operator|.
name|DoubleWritable
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
name|io
operator|.
name|HiveDecimalWritable
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
name|io
operator|.
name|TimestampWritable
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
name|io
operator|.
name|BooleanWritable
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
name|BytesWritable
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
name|FloatWritable
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
name|IntWritable
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
name|LongWritable
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
name|parquet
operator|.
name|column
operator|.
name|Dictionary
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|io
operator|.
name|api
operator|.
name|Binary
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|io
operator|.
name|api
operator|.
name|PrimitiveConverter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|OriginalType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|PrimitiveType
import|;
end_import

begin_comment
comment|/**  *  * ETypeConverter is an easy way to set the converter for the right type.  *  */
end_comment

begin_enum
specifier|public
enum|enum
name|ETypeConverter
block|{
name|EDOUBLE_CONVERTER
parameter_list|(
name|Double
operator|.
name|TYPE
parameter_list|)
block|{
annotation|@
name|Override
name|PrimitiveConverter
name|getConverter
parameter_list|(
specifier|final
name|PrimitiveType
name|type
parameter_list|,
specifier|final
name|int
name|index
parameter_list|,
specifier|final
name|ConverterParent
name|parent
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
return|return
operator|new
name|PrimitiveConverter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|addDouble
parameter_list|(
specifier|final
name|double
name|value
parameter_list|)
block|{
name|parent
operator|.
name|set
argument_list|(
name|index
argument_list|,
operator|new
name|DoubleWritable
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
block|}
block|,
name|EBOOLEAN_CONVERTER
parameter_list|(
name|Boolean
operator|.
name|TYPE
parameter_list|)
block|{
annotation|@
name|Override
name|PrimitiveConverter
name|getConverter
parameter_list|(
specifier|final
name|PrimitiveType
name|type
parameter_list|,
specifier|final
name|int
name|index
parameter_list|,
specifier|final
name|ConverterParent
name|parent
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
return|return
operator|new
name|PrimitiveConverter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|addBoolean
parameter_list|(
specifier|final
name|boolean
name|value
parameter_list|)
block|{
name|parent
operator|.
name|set
argument_list|(
name|index
argument_list|,
operator|new
name|BooleanWritable
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
block|}
block|,
name|EFLOAT_CONVERTER
parameter_list|(
name|Float
operator|.
name|TYPE
parameter_list|)
block|{
annotation|@
name|Override
name|PrimitiveConverter
name|getConverter
parameter_list|(
specifier|final
name|PrimitiveType
name|type
parameter_list|,
specifier|final
name|int
name|index
parameter_list|,
specifier|final
name|ConverterParent
name|parent
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
if|if
condition|(
name|hiveTypeInfo
operator|!=
literal|null
operator|&&
name|hiveTypeInfo
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
argument_list|)
condition|)
block|{
return|return
operator|new
name|PrimitiveConverter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|addFloat
parameter_list|(
specifier|final
name|float
name|value
parameter_list|)
block|{
name|parent
operator|.
name|set
argument_list|(
name|index
argument_list|,
operator|new
name|DoubleWritable
argument_list|(
operator|(
name|double
operator|)
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
else|else
block|{
return|return
operator|new
name|PrimitiveConverter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|addFloat
parameter_list|(
specifier|final
name|float
name|value
parameter_list|)
block|{
name|parent
operator|.
name|set
argument_list|(
name|index
argument_list|,
operator|new
name|FloatWritable
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
block|}
block|}
block|,
name|EINT32_CONVERTER
parameter_list|(
name|Integer
operator|.
name|TYPE
parameter_list|)
block|{
annotation|@
name|Override
name|PrimitiveConverter
name|getConverter
parameter_list|(
specifier|final
name|PrimitiveType
name|type
parameter_list|,
specifier|final
name|int
name|index
parameter_list|,
specifier|final
name|ConverterParent
name|parent
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
if|if
condition|(
name|hiveTypeInfo
operator|!=
literal|null
operator|&&
name|hiveTypeInfo
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|)
condition|)
block|{
return|return
operator|new
name|PrimitiveConverter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|addInt
parameter_list|(
specifier|final
name|int
name|value
parameter_list|)
block|{
name|parent
operator|.
name|set
argument_list|(
name|index
argument_list|,
operator|new
name|LongWritable
argument_list|(
operator|(
name|long
operator|)
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
else|else
block|{
return|return
operator|new
name|PrimitiveConverter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|addInt
parameter_list|(
specifier|final
name|int
name|value
parameter_list|)
block|{
name|parent
operator|.
name|set
argument_list|(
name|index
argument_list|,
operator|new
name|IntWritable
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
block|}
block|}
block|,
name|EINT64_CONVERTER
parameter_list|(
name|Long
operator|.
name|TYPE
parameter_list|)
block|{
annotation|@
name|Override
name|PrimitiveConverter
name|getConverter
parameter_list|(
specifier|final
name|PrimitiveType
name|type
parameter_list|,
specifier|final
name|int
name|index
parameter_list|,
specifier|final
name|ConverterParent
name|parent
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
return|return
operator|new
name|PrimitiveConverter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|addLong
parameter_list|(
specifier|final
name|long
name|value
parameter_list|)
block|{
name|parent
operator|.
name|set
argument_list|(
name|index
argument_list|,
operator|new
name|LongWritable
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
block|}
block|,
name|EBINARY_CONVERTER
parameter_list|(
name|Binary
operator|.
name|class
parameter_list|)
block|{
annotation|@
name|Override
name|PrimitiveConverter
name|getConverter
parameter_list|(
specifier|final
name|PrimitiveType
name|type
parameter_list|,
specifier|final
name|int
name|index
parameter_list|,
specifier|final
name|ConverterParent
name|parent
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
return|return
operator|new
name|BinaryConverter
argument_list|<
name|BytesWritable
argument_list|>
argument_list|(
name|type
argument_list|,
name|parent
argument_list|,
name|index
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|BytesWritable
name|convert
parameter_list|(
name|Binary
name|binary
parameter_list|)
block|{
return|return
operator|new
name|BytesWritable
argument_list|(
name|binary
operator|.
name|getBytes
argument_list|()
argument_list|)
return|;
block|}
block|}
return|;
block|}
block|}
block|,
name|ESTRING_CONVERTER
parameter_list|(
name|String
operator|.
name|class
parameter_list|)
block|{
annotation|@
name|Override
name|PrimitiveConverter
name|getConverter
parameter_list|(
specifier|final
name|PrimitiveType
name|type
parameter_list|,
specifier|final
name|int
name|index
parameter_list|,
specifier|final
name|ConverterParent
name|parent
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
return|return
operator|new
name|BinaryConverter
argument_list|<
name|Text
argument_list|>
argument_list|(
name|type
argument_list|,
name|parent
argument_list|,
name|index
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Text
name|convert
parameter_list|(
name|Binary
name|binary
parameter_list|)
block|{
return|return
operator|new
name|Text
argument_list|(
name|binary
operator|.
name|getBytes
argument_list|()
argument_list|)
return|;
block|}
block|}
return|;
block|}
block|}
block|,
name|EDECIMAL_CONVERTER
parameter_list|(
name|BigDecimal
operator|.
name|class
parameter_list|)
block|{
annotation|@
name|Override
name|PrimitiveConverter
name|getConverter
parameter_list|(
specifier|final
name|PrimitiveType
name|type
parameter_list|,
specifier|final
name|int
name|index
parameter_list|,
specifier|final
name|ConverterParent
name|parent
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
return|return
operator|new
name|BinaryConverter
argument_list|<
name|HiveDecimalWritable
argument_list|>
argument_list|(
name|type
argument_list|,
name|parent
argument_list|,
name|index
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|HiveDecimalWritable
name|convert
parameter_list|(
name|Binary
name|binary
parameter_list|)
block|{
return|return
operator|new
name|HiveDecimalWritable
argument_list|(
name|binary
operator|.
name|getBytes
argument_list|()
argument_list|,
name|type
operator|.
name|getDecimalMetadata
argument_list|()
operator|.
name|getScale
argument_list|()
argument_list|)
return|;
block|}
block|}
return|;
block|}
block|}
block|,
name|ETIMESTAMP_CONVERTER
parameter_list|(
name|TimestampWritable
operator|.
name|class
parameter_list|)
block|{
annotation|@
name|Override
name|PrimitiveConverter
name|getConverter
parameter_list|(
specifier|final
name|PrimitiveType
name|type
parameter_list|,
specifier|final
name|int
name|index
parameter_list|,
specifier|final
name|ConverterParent
name|parent
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
return|return
operator|new
name|BinaryConverter
argument_list|<
name|TimestampWritable
argument_list|>
argument_list|(
name|type
argument_list|,
name|parent
argument_list|,
name|index
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|TimestampWritable
name|convert
parameter_list|(
name|Binary
name|binary
parameter_list|)
block|{
name|NanoTime
name|nt
init|=
name|NanoTime
operator|.
name|fromBinary
argument_list|(
name|binary
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|metadata
init|=
name|parent
operator|.
name|getMetadata
argument_list|()
decl_stmt|;
comment|//Current Hive parquet timestamp implementation stores it in UTC, but other components do not do that.
comment|//If this file written by current Hive implementation itself, we need to do the reverse conversion, else skip the conversion.
name|boolean
name|skipConversion
init|=
name|Boolean
operator|.
name|valueOf
argument_list|(
name|metadata
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_PARQUET_TIMESTAMP_SKIP_CONVERSION
operator|.
name|varname
argument_list|)
argument_list|)
decl_stmt|;
name|Timestamp
name|ts
init|=
name|NanoTimeUtils
operator|.
name|getTimestamp
argument_list|(
name|nt
argument_list|,
name|skipConversion
argument_list|)
decl_stmt|;
return|return
operator|new
name|TimestampWritable
argument_list|(
name|ts
argument_list|)
return|;
block|}
block|}
return|;
block|}
block|}
block|,
name|EDATE_CONVERTER
parameter_list|(
name|DateWritable
operator|.
name|class
parameter_list|)
block|{
annotation|@
name|Override
name|PrimitiveConverter
name|getConverter
parameter_list|(
specifier|final
name|PrimitiveType
name|type
parameter_list|,
specifier|final
name|int
name|index
parameter_list|,
specifier|final
name|ConverterParent
name|parent
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
return|return
operator|new
name|PrimitiveConverter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|addInt
parameter_list|(
specifier|final
name|int
name|value
parameter_list|)
block|{
name|parent
operator|.
name|set
argument_list|(
name|index
argument_list|,
operator|new
name|DateWritable
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
block|}
block|;
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|_type
decl_stmt|;
specifier|private
name|ETypeConverter
parameter_list|(
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|type
parameter_list|)
block|{
name|this
operator|.
name|_type
operator|=
name|type
expr_stmt|;
block|}
specifier|private
name|Class
argument_list|<
name|?
argument_list|>
name|getType
parameter_list|()
block|{
return|return
name|_type
return|;
block|}
specifier|abstract
name|PrimitiveConverter
name|getConverter
parameter_list|(
specifier|final
name|PrimitiveType
name|type
parameter_list|,
specifier|final
name|int
name|index
parameter_list|,
specifier|final
name|ConverterParent
name|parent
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
function_decl|;
specifier|public
specifier|static
name|PrimitiveConverter
name|getNewConverter
parameter_list|(
specifier|final
name|PrimitiveType
name|type
parameter_list|,
specifier|final
name|int
name|index
parameter_list|,
specifier|final
name|ConverterParent
name|parent
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
if|if
condition|(
name|type
operator|.
name|isPrimitive
argument_list|()
operator|&&
operator|(
name|type
operator|.
name|asPrimitiveType
argument_list|()
operator|.
name|getPrimitiveTypeName
argument_list|()
operator|.
name|equals
argument_list|(
name|PrimitiveType
operator|.
name|PrimitiveTypeName
operator|.
name|INT96
argument_list|)
operator|)
condition|)
block|{
comment|//TODO- cleanup once parquet support Timestamp type annotation.
return|return
name|ETypeConverter
operator|.
name|ETIMESTAMP_CONVERTER
operator|.
name|getConverter
argument_list|(
name|type
argument_list|,
name|index
argument_list|,
name|parent
argument_list|,
name|hiveTypeInfo
argument_list|)
return|;
block|}
if|if
condition|(
name|OriginalType
operator|.
name|DECIMAL
operator|==
name|type
operator|.
name|getOriginalType
argument_list|()
condition|)
block|{
return|return
name|EDECIMAL_CONVERTER
operator|.
name|getConverter
argument_list|(
name|type
argument_list|,
name|index
argument_list|,
name|parent
argument_list|,
name|hiveTypeInfo
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|OriginalType
operator|.
name|UTF8
operator|==
name|type
operator|.
name|getOriginalType
argument_list|()
condition|)
block|{
return|return
name|ESTRING_CONVERTER
operator|.
name|getConverter
argument_list|(
name|type
argument_list|,
name|index
argument_list|,
name|parent
argument_list|,
name|hiveTypeInfo
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|OriginalType
operator|.
name|DATE
operator|==
name|type
operator|.
name|getOriginalType
argument_list|()
condition|)
block|{
return|return
name|EDATE_CONVERTER
operator|.
name|getConverter
argument_list|(
name|type
argument_list|,
name|index
argument_list|,
name|parent
argument_list|,
name|hiveTypeInfo
argument_list|)
return|;
block|}
name|Class
argument_list|<
name|?
argument_list|>
name|javaType
init|=
name|type
operator|.
name|getPrimitiveTypeName
argument_list|()
operator|.
name|javaType
decl_stmt|;
for|for
control|(
specifier|final
name|ETypeConverter
name|eConverter
range|:
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|eConverter
operator|.
name|getType
argument_list|()
operator|==
name|javaType
condition|)
block|{
return|return
name|eConverter
operator|.
name|getConverter
argument_list|(
name|type
argument_list|,
name|index
argument_list|,
name|parent
argument_list|,
name|hiveTypeInfo
argument_list|)
return|;
block|}
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Converter not found ... for type : "
operator|+
name|type
argument_list|)
throw|;
block|}
specifier|public
specifier|abstract
specifier|static
class|class
name|BinaryConverter
parameter_list|<
name|T
extends|extends
name|Writable
parameter_list|>
extends|extends
name|PrimitiveConverter
block|{
specifier|protected
specifier|final
name|PrimitiveType
name|type
decl_stmt|;
specifier|private
specifier|final
name|ConverterParent
name|parent
decl_stmt|;
specifier|private
specifier|final
name|int
name|index
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|T
argument_list|>
name|lookupTable
decl_stmt|;
specifier|public
name|BinaryConverter
parameter_list|(
name|PrimitiveType
name|type
parameter_list|,
name|ConverterParent
name|parent
parameter_list|,
name|int
name|index
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|parent
operator|=
name|parent
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
block|}
specifier|protected
specifier|abstract
name|T
name|convert
parameter_list|(
name|Binary
name|binary
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
name|boolean
name|hasDictionarySupport
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setDictionary
parameter_list|(
name|Dictionary
name|dictionary
parameter_list|)
block|{
name|int
name|length
init|=
name|dictionary
operator|.
name|getMaxId
argument_list|()
operator|+
literal|1
decl_stmt|;
name|lookupTable
operator|=
operator|new
name|ArrayList
argument_list|<
name|T
argument_list|>
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
name|length
condition|;
name|i
operator|++
control|)
block|{
name|lookupTable
operator|.
name|add
argument_list|(
name|convert
argument_list|(
name|dictionary
operator|.
name|decodeToBinary
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|addValueFromDictionary
parameter_list|(
name|int
name|dictionaryId
parameter_list|)
block|{
name|parent
operator|.
name|set
argument_list|(
name|index
argument_list|,
name|lookupTable
operator|.
name|get
argument_list|(
name|dictionaryId
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addBinary
parameter_list|(
name|Binary
name|value
parameter_list|)
block|{
name|parent
operator|.
name|set
argument_list|(
name|index
argument_list|,
name|convert
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_enum

end_unit

