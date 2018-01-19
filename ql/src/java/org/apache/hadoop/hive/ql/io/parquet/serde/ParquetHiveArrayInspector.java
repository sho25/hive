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
name|SettableListObjectInspector
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
name|ArrayWritable
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

begin_comment
comment|/**  * The ParquetHiveArrayInspector will inspect an ArrayWritable, considering it as an Hive array.<br />  * It can also inspect a List if Hive decides to inspect the result of an inspection.  *  */
end_comment

begin_class
specifier|public
class|class
name|ParquetHiveArrayInspector
implements|implements
name|SettableListObjectInspector
block|{
name|ObjectInspector
name|arrayElementInspector
decl_stmt|;
specifier|public
name|ParquetHiveArrayInspector
parameter_list|(
specifier|final
name|ObjectInspector
name|arrayElementInspector
parameter_list|)
block|{
name|this
operator|.
name|arrayElementInspector
operator|=
name|arrayElementInspector
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getTypeName
parameter_list|()
block|{
return|return
literal|"array<"
operator|+
name|arrayElementInspector
operator|.
name|getTypeName
argument_list|()
operator|+
literal|">"
return|;
block|}
annotation|@
name|Override
specifier|public
name|Category
name|getCategory
parameter_list|()
block|{
return|return
name|Category
operator|.
name|LIST
return|;
block|}
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|getListElementObjectInspector
parameter_list|()
block|{
return|return
name|arrayElementInspector
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getListElement
parameter_list|(
specifier|final
name|Object
name|data
parameter_list|,
specifier|final
name|int
name|index
parameter_list|)
block|{
if|if
condition|(
name|data
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
name|ArrayWritable
condition|)
block|{
specifier|final
name|Writable
index|[]
name|array
init|=
operator|(
operator|(
name|ArrayWritable
operator|)
name|data
operator|)
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|array
operator|==
literal|null
operator|||
name|array
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
if|if
condition|(
name|index
operator|>=
literal|0
operator|&&
name|index
operator|<
name|array
operator|.
name|length
condition|)
block|{
return|return
name|array
index|[
name|index
index|]
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
if|if
condition|(
name|data
operator|instanceof
name|List
condition|)
block|{
return|return
operator|(
operator|(
name|List
argument_list|<
name|?
argument_list|>
operator|)
name|data
operator|)
operator|.
name|get
argument_list|(
name|index
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
annotation|@
name|Override
specifier|public
name|int
name|getListLength
parameter_list|(
specifier|final
name|Object
name|data
parameter_list|)
block|{
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|data
operator|instanceof
name|ArrayWritable
condition|)
block|{
specifier|final
name|Writable
index|[]
name|array
init|=
operator|(
operator|(
name|ArrayWritable
operator|)
name|data
operator|)
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|array
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
name|array
operator|.
name|length
return|;
block|}
if|if
condition|(
name|data
operator|instanceof
name|List
condition|)
block|{
return|return
operator|(
operator|(
name|List
argument_list|<
name|?
argument_list|>
operator|)
name|data
operator|)
operator|.
name|size
argument_list|()
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
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|?
argument_list|>
name|getList
parameter_list|(
specifier|final
name|Object
name|data
parameter_list|)
block|{
if|if
condition|(
name|data
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
name|ArrayWritable
condition|)
block|{
specifier|final
name|Writable
index|[]
name|array
init|=
operator|(
operator|(
name|ArrayWritable
operator|)
name|data
operator|)
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|array
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|ArrayList
argument_list|<
name|Writable
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|array
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|data
operator|instanceof
name|List
condition|)
block|{
return|return
operator|(
name|List
argument_list|<
name|?
argument_list|>
operator|)
name|data
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
annotation|@
name|Override
specifier|public
name|Object
name|create
parameter_list|(
specifier|final
name|int
name|size
parameter_list|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Object
index|[
name|size
index|]
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|Object
name|set
parameter_list|(
specifier|final
name|Object
name|list
parameter_list|,
specifier|final
name|int
name|index
parameter_list|,
specifier|final
name|Object
name|element
parameter_list|)
block|{
specifier|final
name|List
argument_list|<
name|Object
argument_list|>
name|l
init|=
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|list
decl_stmt|;
name|l
operator|.
name|set
argument_list|(
name|index
argument_list|,
name|element
argument_list|)
expr_stmt|;
return|return
name|list
return|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|Object
name|resize
parameter_list|(
specifier|final
name|Object
name|list
parameter_list|,
specifier|final
name|int
name|newSize
parameter_list|)
block|{
specifier|final
name|List
argument_list|<
name|Object
argument_list|>
name|l
init|=
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|list
decl_stmt|;
specifier|final
name|int
name|deltaSize
init|=
name|newSize
operator|-
name|l
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|deltaSize
operator|>
literal|0
condition|)
block|{
name|l
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Object
index|[
name|deltaSize
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|size
init|=
name|l
operator|.
name|size
argument_list|()
decl_stmt|;
name|l
operator|.
name|subList
argument_list|(
name|size
operator|+
name|deltaSize
argument_list|,
name|size
argument_list|)
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
return|return
name|list
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
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
operator|||
name|o
operator|.
name|getClass
argument_list|()
operator|!=
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
elseif|else
if|if
condition|(
name|o
operator|==
name|this
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
specifier|final
name|ObjectInspector
name|other
init|=
operator|(
operator|(
name|ParquetHiveArrayInspector
operator|)
name|o
operator|)
operator|.
name|arrayElementInspector
decl_stmt|;
return|return
name|other
operator|.
name|equals
argument_list|(
name|arrayElementInspector
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
operator|(
name|this
operator|.
name|arrayElementInspector
operator|!=
literal|null
condition|?
name|this
operator|.
name|arrayElementInspector
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
return|;
block|}
block|}
end_class

end_unit

