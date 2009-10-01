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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|Class
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
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

begin_comment
comment|/**  * Implementation for ColumnInfo which contains the internal name for the   * column (the one that is used by the operator to access the column) and  * the type (identified by a java class).  **/
end_comment

begin_class
specifier|public
class|class
name|ColumnInfo
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
name|String
name|internalName
decl_stmt|;
specifier|private
name|String
name|alias
init|=
literal|null
decl_stmt|;
comment|// [optional] alias of the column (external name as seen by the users)
comment|/**    * Store the alias of the table where available.    */
specifier|private
name|String
name|tabAlias
decl_stmt|;
comment|/**    * Indicates whether the column is a partition column.    */
specifier|private
name|boolean
name|isPartitionCol
decl_stmt|;
specifier|transient
specifier|private
name|TypeInfo
name|type
decl_stmt|;
specifier|public
name|ColumnInfo
parameter_list|()
block|{   }
specifier|public
name|ColumnInfo
parameter_list|(
name|String
name|internalName
parameter_list|,
name|TypeInfo
name|type
parameter_list|,
name|String
name|tabAlias
parameter_list|,
name|boolean
name|isPartitionCol
parameter_list|)
block|{
name|this
operator|.
name|internalName
operator|=
name|internalName
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|tabAlias
operator|=
name|tabAlias
expr_stmt|;
name|this
operator|.
name|isPartitionCol
operator|=
name|isPartitionCol
expr_stmt|;
block|}
specifier|public
name|ColumnInfo
parameter_list|(
name|String
name|internalName
parameter_list|,
name|Class
name|type
parameter_list|,
name|String
name|tabAlias
parameter_list|,
name|boolean
name|isPartitionCol
parameter_list|)
block|{
name|this
operator|.
name|internalName
operator|=
name|internalName
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfoFromPrimitiveWritable
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|this
operator|.
name|tabAlias
operator|=
name|tabAlias
expr_stmt|;
name|this
operator|.
name|isPartitionCol
operator|=
name|isPartitionCol
expr_stmt|;
block|}
specifier|public
name|TypeInfo
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
specifier|public
name|String
name|getInternalName
parameter_list|()
block|{
return|return
name|internalName
return|;
block|}
specifier|public
name|void
name|setType
parameter_list|(
name|TypeInfo
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
specifier|public
name|void
name|setInternalName
parameter_list|(
name|String
name|internalName
parameter_list|)
block|{
name|this
operator|.
name|internalName
operator|=
name|internalName
expr_stmt|;
block|}
specifier|public
name|String
name|getTabAlias
parameter_list|()
block|{
return|return
name|this
operator|.
name|tabAlias
return|;
block|}
specifier|public
name|boolean
name|getIsPartitionCol
parameter_list|()
block|{
return|return
name|this
operator|.
name|isPartitionCol
return|;
block|}
comment|/**    * Returns the string representation of the ColumnInfo.    */
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|internalName
operator|+
literal|": "
operator|+
name|type
return|;
block|}
specifier|public
name|void
name|setAlias
parameter_list|(
name|String
name|col_alias
parameter_list|)
block|{
name|alias
operator|=
name|col_alias
expr_stmt|;
block|}
specifier|public
name|String
name|getAlias
parameter_list|()
block|{
return|return
name|alias
return|;
block|}
block|}
end_class

end_unit

