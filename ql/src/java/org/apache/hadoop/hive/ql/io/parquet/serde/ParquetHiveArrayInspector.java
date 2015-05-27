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
name|ArrayList
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
name|listContainer
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
name|listContainer
operator|==
literal|null
operator|||
name|listContainer
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
name|Writable
name|subObj
init|=
name|listContainer
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|subObj
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
name|index
operator|>=
literal|0
operator|&&
name|index
operator|<
operator|(
operator|(
name|ArrayWritable
operator|)
name|subObj
operator|)
operator|.
name|get
argument_list|()
operator|.
name|length
condition|)
block|{
return|return
operator|(
operator|(
name|ArrayWritable
operator|)
name|subObj
operator|)
operator|.
name|get
argument_list|()
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
name|listContainer
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
name|listContainer
operator|==
literal|null
operator|||
name|listContainer
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
specifier|final
name|Writable
name|subObj
init|=
name|listContainer
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|subObj
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
operator|(
operator|(
name|ArrayWritable
operator|)
name|subObj
operator|)
operator|.
name|get
argument_list|()
operator|.
name|length
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
name|listContainer
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
name|listContainer
operator|==
literal|null
operator|||
name|listContainer
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
name|Writable
name|subObj
init|=
name|listContainer
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|subObj
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
specifier|final
name|Writable
index|[]
name|array
init|=
operator|(
operator|(
name|ArrayWritable
operator|)
name|subObj
operator|)
operator|.
name|get
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Writable
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|Writable
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|Writable
name|obj
range|:
name|array
control|)
block|{
name|list
operator|.
name|add
argument_list|(
name|obj
argument_list|)
expr_stmt|;
block|}
return|return
name|list
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
specifier|final
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|size
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
name|size
condition|;
operator|++
name|i
control|)
block|{
name|result
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
return|return
name|result
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
name|ArrayList
name|l
init|=
operator|(
name|ArrayList
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
name|ArrayList
name|l
init|=
operator|(
name|ArrayList
operator|)
name|list
decl_stmt|;
name|l
operator|.
name|ensureCapacity
argument_list|(
name|newSize
argument_list|)
expr_stmt|;
while|while
condition|(
name|l
operator|.
name|size
argument_list|()
operator|<
name|newSize
condition|)
block|{
name|l
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
while|while
condition|(
name|l
operator|.
name|size
argument_list|()
operator|>
name|newSize
condition|)
block|{
name|l
operator|.
name|remove
argument_list|(
name|l
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
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
name|int
name|hash
init|=
literal|3
decl_stmt|;
name|hash
operator|=
literal|29
operator|*
name|hash
operator|+
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
expr_stmt|;
return|return
name|hash
return|;
block|}
block|}
end_class

end_unit

