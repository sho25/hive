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
name|serde2
operator|.
name|io
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
name|io
operator|.
name|Writable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * This class wraps the object and object inspector that will be used later  * in DataWritableWriter class to get the object values.  */
end_comment

begin_class
specifier|public
class|class
name|ParquetHiveRecord
implements|implements
name|Writable
block|{
specifier|public
name|Object
name|value
decl_stmt|;
specifier|public
name|StructObjectInspector
name|inspector
decl_stmt|;
specifier|public
name|ParquetHiveRecord
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ParquetHiveRecord
parameter_list|(
specifier|final
name|Object
name|o
parameter_list|,
specifier|final
name|StructObjectInspector
name|oi
parameter_list|)
block|{
name|value
operator|=
name|o
expr_stmt|;
name|inspector
operator|=
name|oi
expr_stmt|;
block|}
specifier|public
name|StructObjectInspector
name|getObjectInspector
parameter_list|()
block|{
return|return
name|inspector
return|;
block|}
specifier|public
name|Object
name|getObject
parameter_list|()
block|{
return|return
name|value
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|dataOutput
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Unsupported method call."
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|dataInput
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Unsupported method call."
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

