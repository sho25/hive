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
name|serde
package|;
end_package

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
name|serde2
operator|.
name|io
operator|.
name|ObjectArrayWritable
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

begin_comment
comment|/**  * The StandardParquetHiveMapInspector will inspect an ArrayWritable, considering it as a Hive map.<br />  * It can also inspect a Map if Hive decides to inspect the result of an inspection.  *  */
end_comment

begin_class
specifier|public
class|class
name|StandardParquetHiveMapInspector
extends|extends
name|AbstractParquetMapInspector
block|{
specifier|public
name|StandardParquetHiveMapInspector
parameter_list|(
specifier|final
name|ObjectInspector
name|keyInspector
parameter_list|,
specifier|final
name|ObjectInspector
name|valueInspector
parameter_list|)
block|{
name|super
argument_list|(
name|keyInspector
argument_list|,
name|valueInspector
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getMapValueElement
parameter_list|(
specifier|final
name|Object
name|data
parameter_list|,
specifier|final
name|Object
name|key
parameter_list|)
block|{
if|if
condition|(
name|data
operator|==
literal|null
operator|||
name|key
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
name|data
operator|instanceof
name|ObjectArrayWritable
condition|)
block|{
specifier|final
name|Object
index|[]
name|mapContainer
init|=
operator|(
operator|(
name|ObjectArrayWritable
operator|)
name|data
operator|)
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|mapContainer
operator|==
literal|null
operator|||
name|mapContainer
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
specifier|final
name|Object
index|[]
name|mapArray
init|=
operator|(
operator|(
name|ObjectArrayWritable
operator|)
name|mapContainer
index|[
literal|0
index|]
operator|)
operator|.
name|get
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|Object
name|obj
range|:
name|mapArray
control|)
block|{
specifier|final
name|ObjectArrayWritable
name|mapObj
init|=
operator|(
name|ObjectArrayWritable
operator|)
name|obj
decl_stmt|;
specifier|final
name|Object
index|[]
name|arr
init|=
name|mapObj
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|key
operator|.
name|equals
argument_list|(
name|arr
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
return|return
name|arr
index|[
literal|1
index|]
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
if|if
condition|(
name|data
operator|instanceof
name|Map
condition|)
block|{
return|return
operator|(
operator|(
name|Map
operator|)
name|data
operator|)
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Cannot inspect "
operator|+
name|data
operator|.
name|getClass
argument_list|()
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

