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
name|ByteWritable
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
name|AbstractPrimitiveJavaObjectInspector
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
name|objectinspector
operator|.
name|primitive
operator|.
name|SettableByteObjectInspector
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

begin_comment
comment|/**  * The ParquetByteInspector can inspect both ByteWritables and IntWritables into bytes.  *  */
end_comment

begin_class
specifier|public
class|class
name|ParquetByteInspector
extends|extends
name|AbstractPrimitiveJavaObjectInspector
implements|implements
name|SettableByteObjectInspector
block|{
name|ParquetByteInspector
parameter_list|()
block|{
name|super
argument_list|(
name|TypeInfoFactory
operator|.
name|byteTypeInfo
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getPrimitiveWritableObject
parameter_list|(
specifier|final
name|Object
name|o
parameter_list|)
block|{
return|return
name|o
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|ByteWritable
argument_list|(
name|get
argument_list|(
name|o
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getPrimitiveJavaObject
parameter_list|(
specifier|final
name|Object
name|o
parameter_list|)
block|{
return|return
name|o
operator|==
literal|null
condition|?
literal|null
else|:
name|get
argument_list|(
name|o
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|create
parameter_list|(
specifier|final
name|byte
name|val
parameter_list|)
block|{
return|return
operator|new
name|ByteWritable
argument_list|(
name|val
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
name|byte
name|val
parameter_list|)
block|{
operator|(
operator|(
name|ByteWritable
operator|)
name|o
operator|)
operator|.
name|set
argument_list|(
name|val
argument_list|)
expr_stmt|;
return|return
name|o
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|get
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
comment|// Accept int writables and convert them.
if|if
condition|(
name|o
operator|instanceof
name|IntWritable
condition|)
block|{
return|return
call|(
name|byte
call|)
argument_list|(
operator|(
name|IntWritable
operator|)
name|o
argument_list|)
operator|.
name|get
argument_list|()
return|;
block|}
return|return
operator|(
operator|(
name|ByteWritable
operator|)
name|o
operator|)
operator|.
name|get
argument_list|()
return|;
block|}
block|}
end_class

end_unit

