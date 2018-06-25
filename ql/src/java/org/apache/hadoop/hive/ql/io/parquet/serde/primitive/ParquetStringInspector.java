begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|serde
operator|.
name|primitive
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|CharacterCodingException
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
name|objectinspector
operator|.
name|primitive
operator|.
name|JavaStringObjectInspector
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
name|SettableStringObjectInspector
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
name|BooleanWritable
import|;
end_import

begin_comment
comment|/**  * The ParquetStringInspector inspects a BytesWritable, TimestampWritable, HiveDecimalWritable,  * DoubleWritable, FloatWritable, LongWritable, IntWritable, and BooleanWritable to give a Text  * or String.  *  */
end_comment

begin_class
specifier|public
class|class
name|ParquetStringInspector
extends|extends
name|JavaStringObjectInspector
implements|implements
name|SettableStringObjectInspector
block|{
name|ParquetStringInspector
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Text
name|getPrimitiveWritableObject
parameter_list|(
specifier|final
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
literal|null
return|;
block|}
if|if
condition|(
name|o
operator|instanceof
name|BytesWritable
condition|)
block|{
return|return
operator|new
name|Text
argument_list|(
operator|(
operator|(
name|BytesWritable
operator|)
name|o
operator|)
operator|.
name|getBytes
argument_list|()
argument_list|)
return|;
block|}
if|if
condition|(
name|o
operator|instanceof
name|Text
condition|)
block|{
return|return
operator|(
name|Text
operator|)
name|o
return|;
block|}
if|if
condition|(
name|o
operator|instanceof
name|String
condition|)
block|{
return|return
operator|new
name|Text
argument_list|(
operator|(
name|String
operator|)
name|o
argument_list|)
return|;
block|}
if|if
condition|(
operator|(
name|o
operator|instanceof
name|TimestampWritable
operator|)
operator|||
operator|(
name|o
operator|instanceof
name|HiveDecimalWritable
operator|)
operator|||
operator|(
name|o
operator|instanceof
name|DoubleWritable
operator|)
operator|||
operator|(
name|o
operator|instanceof
name|FloatWritable
operator|)
operator|||
operator|(
name|o
operator|instanceof
name|LongWritable
operator|)
operator|||
operator|(
name|o
operator|instanceof
name|IntWritable
operator|)
operator|||
operator|(
name|o
operator|instanceof
name|BooleanWritable
operator|)
condition|)
block|{
return|return
operator|new
name|Text
argument_list|(
name|o
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Cannot inspect "
operator|+
name|o
operator|.
name|getClass
argument_list|()
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getPrimitiveJavaObject
parameter_list|(
specifier|final
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
literal|null
return|;
block|}
if|if
condition|(
name|o
operator|instanceof
name|BytesWritable
condition|)
block|{
try|try
block|{
return|return
name|Text
operator|.
name|decode
argument_list|(
operator|(
operator|(
name|BytesWritable
operator|)
name|o
operator|)
operator|.
name|getBytes
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|CharacterCodingException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to decode string"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|o
operator|instanceof
name|Text
condition|)
block|{
return|return
operator|(
operator|(
name|Text
operator|)
name|o
operator|)
operator|.
name|toString
argument_list|()
return|;
block|}
if|if
condition|(
name|o
operator|instanceof
name|String
condition|)
block|{
return|return
operator|(
name|String
operator|)
name|o
return|;
block|}
if|if
condition|(
operator|(
name|o
operator|instanceof
name|TimestampWritable
operator|)
operator|||
operator|(
name|o
operator|instanceof
name|HiveDecimalWritable
operator|)
operator|||
operator|(
name|o
operator|instanceof
name|DoubleWritable
operator|)
operator|||
operator|(
name|o
operator|instanceof
name|FloatWritable
operator|)
operator|||
operator|(
name|o
operator|instanceof
name|LongWritable
operator|)
operator|||
operator|(
name|o
operator|instanceof
name|IntWritable
operator|)
operator|||
operator|(
name|o
operator|instanceof
name|BooleanWritable
operator|)
condition|)
block|{
return|return
operator|(
name|String
operator|)
name|o
operator|.
name|toString
argument_list|()
return|;
block|}
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Cannot inspect "
operator|+
name|o
operator|.
name|getClass
argument_list|()
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|set
parameter_list|(
specifier|final
name|Object
name|o
parameter_list|,
specifier|final
name|Text
name|text
parameter_list|)
block|{
return|return
operator|new
name|BytesWritable
argument_list|(
name|text
operator|==
literal|null
condition|?
literal|null
else|:
name|text
operator|.
name|getBytes
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|set
parameter_list|(
specifier|final
name|Object
name|o
parameter_list|,
specifier|final
name|String
name|string
parameter_list|)
block|{
try|try
block|{
return|return
operator|new
name|BytesWritable
argument_list|(
name|string
operator|==
literal|null
condition|?
literal|null
else|:
name|string
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to encode string in UTF-8"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Object
name|create
parameter_list|(
specifier|final
name|Text
name|text
parameter_list|)
block|{
if|if
condition|(
name|text
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|text
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|create
parameter_list|(
specifier|final
name|String
name|string
parameter_list|)
block|{
return|return
name|string
return|;
block|}
block|}
end_class

end_unit

