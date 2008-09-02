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
name|plan
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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
import|;
end_import

begin_class
specifier|public
class|class
name|createTableDesc
extends|extends
name|ddlDesc
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
name|String
name|tableName
decl_stmt|;
name|boolean
name|isExternal
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|bucketCols
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|sortCols
decl_stmt|;
name|int
name|numBuckets
decl_stmt|;
name|String
name|fieldDelim
decl_stmt|;
name|String
name|collItemDelim
decl_stmt|;
name|String
name|mapKeyDelim
decl_stmt|;
name|String
name|lineDelim
decl_stmt|;
name|String
name|comment
decl_stmt|;
name|boolean
name|isCompressed
decl_stmt|;
name|String
name|location
decl_stmt|;
specifier|public
name|createTableDesc
parameter_list|(
name|String
name|tableName
parameter_list|,
name|boolean
name|isExternal
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|bucketCols
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|sortCols
parameter_list|,
name|int
name|numBuckets
parameter_list|,
name|String
name|fieldDelim
parameter_list|,
name|String
name|collItemDelim
parameter_list|,
name|String
name|mapKeyDelim
parameter_list|,
name|String
name|lineDelim
parameter_list|,
name|String
name|comment
parameter_list|,
name|boolean
name|isCompressed
parameter_list|,
name|String
name|location
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|isExternal
operator|=
name|isExternal
expr_stmt|;
name|this
operator|.
name|bucketCols
operator|=
name|bucketCols
expr_stmt|;
name|this
operator|.
name|sortCols
operator|=
name|sortCols
expr_stmt|;
name|this
operator|.
name|collItemDelim
operator|=
name|collItemDelim
expr_stmt|;
name|this
operator|.
name|cols
operator|=
name|cols
expr_stmt|;
name|this
operator|.
name|comment
operator|=
name|comment
expr_stmt|;
name|this
operator|.
name|fieldDelim
operator|=
name|fieldDelim
expr_stmt|;
name|this
operator|.
name|isCompressed
operator|=
name|isCompressed
expr_stmt|;
name|this
operator|.
name|lineDelim
operator|=
name|lineDelim
expr_stmt|;
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
name|this
operator|.
name|mapKeyDelim
operator|=
name|mapKeyDelim
expr_stmt|;
name|this
operator|.
name|numBuckets
operator|=
name|numBuckets
expr_stmt|;
name|this
operator|.
name|partCols
operator|=
name|partCols
expr_stmt|;
block|}
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
specifier|public
name|void
name|setTableName
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getCols
parameter_list|()
block|{
return|return
name|cols
return|;
block|}
specifier|public
name|void
name|setCols
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
parameter_list|)
block|{
name|this
operator|.
name|cols
operator|=
name|cols
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getPartCols
parameter_list|()
block|{
return|return
name|partCols
return|;
block|}
specifier|public
name|void
name|setPartCols
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
parameter_list|)
block|{
name|this
operator|.
name|partCols
operator|=
name|partCols
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getBucketCols
parameter_list|()
block|{
return|return
name|bucketCols
return|;
block|}
specifier|public
name|void
name|setBucketCols
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|bucketCols
parameter_list|)
block|{
name|this
operator|.
name|bucketCols
operator|=
name|bucketCols
expr_stmt|;
block|}
specifier|public
name|int
name|getNumBuckets
parameter_list|()
block|{
return|return
name|numBuckets
return|;
block|}
specifier|public
name|void
name|setNumBuckets
parameter_list|(
name|int
name|numBuckets
parameter_list|)
block|{
name|this
operator|.
name|numBuckets
operator|=
name|numBuckets
expr_stmt|;
block|}
specifier|public
name|String
name|getFieldDelim
parameter_list|()
block|{
return|return
name|fieldDelim
return|;
block|}
specifier|public
name|void
name|setFieldDelim
parameter_list|(
name|String
name|fieldDelim
parameter_list|)
block|{
name|this
operator|.
name|fieldDelim
operator|=
name|fieldDelim
expr_stmt|;
block|}
specifier|public
name|String
name|getCollItemDelim
parameter_list|()
block|{
return|return
name|collItemDelim
return|;
block|}
specifier|public
name|void
name|setCollItemDelim
parameter_list|(
name|String
name|collItemDelim
parameter_list|)
block|{
name|this
operator|.
name|collItemDelim
operator|=
name|collItemDelim
expr_stmt|;
block|}
specifier|public
name|String
name|getMapKeyDelim
parameter_list|()
block|{
return|return
name|mapKeyDelim
return|;
block|}
specifier|public
name|void
name|setMapKeyDelim
parameter_list|(
name|String
name|mapKeyDelim
parameter_list|)
block|{
name|this
operator|.
name|mapKeyDelim
operator|=
name|mapKeyDelim
expr_stmt|;
block|}
specifier|public
name|String
name|getLineDelim
parameter_list|()
block|{
return|return
name|lineDelim
return|;
block|}
specifier|public
name|void
name|setLineDelim
parameter_list|(
name|String
name|lineDelim
parameter_list|)
block|{
name|this
operator|.
name|lineDelim
operator|=
name|lineDelim
expr_stmt|;
block|}
specifier|public
name|String
name|getComment
parameter_list|()
block|{
return|return
name|comment
return|;
block|}
specifier|public
name|void
name|setComment
parameter_list|(
name|String
name|comment
parameter_list|)
block|{
name|this
operator|.
name|comment
operator|=
name|comment
expr_stmt|;
block|}
specifier|public
name|boolean
name|isCompressed
parameter_list|()
block|{
return|return
name|isCompressed
return|;
block|}
specifier|public
name|void
name|setCompressed
parameter_list|(
name|boolean
name|isCompressed
parameter_list|)
block|{
name|this
operator|.
name|isCompressed
operator|=
name|isCompressed
expr_stmt|;
block|}
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
return|return
name|location
return|;
block|}
specifier|public
name|void
name|setLocation
parameter_list|(
name|String
name|location
parameter_list|)
block|{
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
block|}
specifier|public
name|boolean
name|isExternal
parameter_list|()
block|{
return|return
name|isExternal
return|;
block|}
specifier|public
name|void
name|setExternal
parameter_list|(
name|boolean
name|isExternal
parameter_list|)
block|{
name|this
operator|.
name|isExternal
operator|=
name|isExternal
expr_stmt|;
block|}
comment|/**    * @return the sortCols    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getSortCols
parameter_list|()
block|{
return|return
name|sortCols
return|;
block|}
comment|/**    * @param sortCols the sortCols to set    */
specifier|public
name|void
name|setSortCols
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|sortCols
parameter_list|)
block|{
name|this
operator|.
name|sortCols
operator|=
name|sortCols
expr_stmt|;
block|}
block|}
end_class

end_unit

