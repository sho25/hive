begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|common
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|common
operator|.
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatException
import|;
end_import

begin_comment
comment|/**  * HCatSchema. This class is NOT thread-safe.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|HCatSchema
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|fieldSchemas
decl_stmt|;
comment|//HCatFieldSchema.getName()->position
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|fieldPositionMap
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
decl_stmt|;
comment|/**    *    * @param fieldSchemas is now owned by HCatSchema. Any subsequent modifications    * on fieldSchemas won't get reflected in HCatSchema.  Each fieldSchema's name    * in the list must be unique, otherwise throws IllegalArgumentException.    */
specifier|public
name|HCatSchema
parameter_list|(
specifier|final
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|fieldSchemas
parameter_list|)
block|{
name|this
operator|.
name|fieldSchemas
operator|=
operator|new
name|ArrayList
argument_list|<
name|HCatFieldSchema
argument_list|>
argument_list|(
name|fieldSchemas
argument_list|)
expr_stmt|;
name|int
name|idx
init|=
literal|0
decl_stmt|;
name|fieldPositionMap
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|()
expr_stmt|;
name|fieldNames
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|HCatFieldSchema
name|field
range|:
name|fieldSchemas
control|)
block|{
if|if
condition|(
name|field
operator|==
literal|null
condition|)
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Field cannot be null"
argument_list|)
throw|;
name|String
name|fieldName
init|=
name|normalizeName
argument_list|(
name|field
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldPositionMap
operator|.
name|containsKey
argument_list|(
name|fieldName
argument_list|)
condition|)
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Field named "
operator|+
name|field
operator|.
name|getName
argument_list|()
operator|+
literal|" already exists"
argument_list|)
throw|;
name|fieldPositionMap
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|idx
argument_list|)
expr_stmt|;
name|fieldNames
operator|.
name|add
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|idx
operator|++
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|append
parameter_list|(
specifier|final
name|HCatFieldSchema
name|hfs
parameter_list|)
throws|throws
name|HCatException
block|{
if|if
condition|(
name|hfs
operator|==
literal|null
condition|)
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"Attempt to append null HCatFieldSchema in HCatSchema."
argument_list|)
throw|;
name|String
name|fieldName
init|=
name|normalizeName
argument_list|(
name|hfs
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldPositionMap
operator|.
name|containsKey
argument_list|(
name|fieldName
argument_list|)
condition|)
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"Attempt to append HCatFieldSchema with already "
operator|+
literal|"existing name: "
operator|+
name|fieldName
operator|+
literal|"."
argument_list|)
throw|;
name|this
operator|.
name|fieldSchemas
operator|.
name|add
argument_list|(
name|hfs
argument_list|)
expr_stmt|;
name|this
operator|.
name|fieldNames
operator|.
name|add
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|this
operator|.
name|fieldPositionMap
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|this
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/**    *  Users are not allowed to modify the list directly, since HCatSchema    *  maintains internal state. Use append/remove to modify the schema.    */
specifier|public
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|getFields
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|this
operator|.
name|fieldSchemas
argument_list|)
return|;
block|}
comment|/**    * Note : The position will be re-numbered when one of the preceding columns are removed.    * Hence, the client should not cache this value and expect it to be always valid.    * @param fieldName    * @return the index of field named fieldName in Schema. If field is not    * present, returns null.    */
specifier|public
name|Integer
name|getPosition
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
return|return
name|fieldPositionMap
operator|.
name|get
argument_list|(
name|normalizeName
argument_list|(
name|fieldName
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|HCatFieldSchema
name|get
parameter_list|(
name|String
name|fieldName
parameter_list|)
throws|throws
name|HCatException
block|{
return|return
name|get
argument_list|(
name|getPosition
argument_list|(
name|fieldName
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getFieldNames
parameter_list|()
block|{
return|return
name|this
operator|.
name|fieldNames
return|;
block|}
specifier|public
name|HCatFieldSchema
name|get
parameter_list|(
name|int
name|position
parameter_list|)
block|{
return|return
name|fieldSchemas
operator|.
name|get
argument_list|(
name|position
argument_list|)
return|;
block|}
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|fieldSchemas
operator|.
name|size
argument_list|()
return|;
block|}
specifier|private
name|void
name|reAlignPositionMap
parameter_list|(
name|int
name|startPosition
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|entry
range|:
name|fieldPositionMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|// Re-align the columns appearing on or after startPostion(say, column 1) such that
comment|// column 2 becomes column (2+offset), column 3 becomes column (3+offset) and so on.
name|Integer
name|entryVal
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|entryVal
operator|>=
name|startPosition
condition|)
block|{
name|entry
operator|.
name|setValue
argument_list|(
name|entryVal
operator|+
name|offset
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|remove
parameter_list|(
specifier|final
name|HCatFieldSchema
name|hcatFieldSchema
parameter_list|)
throws|throws
name|HCatException
block|{
if|if
condition|(
operator|!
name|fieldSchemas
operator|.
name|contains
argument_list|(
name|hcatFieldSchema
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"Attempt to delete a non-existent column from HCat Schema: "
operator|+
name|hcatFieldSchema
argument_list|)
throw|;
block|}
name|fieldSchemas
operator|.
name|remove
argument_list|(
name|hcatFieldSchema
argument_list|)
expr_stmt|;
comment|// Re-align the positionMap by -1 for the columns appearing after hcatFieldSchema.
name|String
name|fieldName
init|=
name|normalizeName
argument_list|(
name|hcatFieldSchema
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|reAlignPositionMap
argument_list|(
name|fieldPositionMap
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
operator|+
literal|1
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|fieldPositionMap
operator|.
name|remove
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|fieldNames
operator|.
name|remove
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|normalizeName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|name
operator|==
literal|null
condition|?
literal|null
else|:
name|name
operator|.
name|toLowerCase
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|boolean
name|first
init|=
literal|true
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|HCatFieldSchema
name|hfs
range|:
name|fieldSchemas
control|)
block|{
if|if
condition|(
operator|!
name|first
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|first
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|hfs
operator|.
name|getName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|hfs
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|hfs
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|String
name|getSchemaAsTypeString
parameter_list|()
block|{
name|boolean
name|first
init|=
literal|true
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|HCatFieldSchema
name|hfs
range|:
name|fieldSchemas
control|)
block|{
if|if
condition|(
operator|!
name|first
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|first
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|hfs
operator|.
name|getName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|hfs
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|hfs
operator|.
name|getTypeString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|obj
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
operator|!
operator|(
name|obj
operator|instanceof
name|HCatSchema
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|HCatSchema
name|other
init|=
operator|(
name|HCatSchema
operator|)
name|obj
decl_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|getFields
argument_list|()
operator|.
name|equals
argument_list|(
name|other
operator|.
name|getFields
argument_list|()
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
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|toString
argument_list|()
operator|.
name|hashCode
argument_list|()
return|;
block|}
block|}
end_class

end_unit

