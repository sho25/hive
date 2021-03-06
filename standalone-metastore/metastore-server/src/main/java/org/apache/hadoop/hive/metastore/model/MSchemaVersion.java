begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metastore
operator|.
name|model
package|;
end_package

begin_class
specifier|public
class|class
name|MSchemaVersion
block|{
specifier|private
name|MISchema
name|iSchema
decl_stmt|;
specifier|private
name|int
name|version
decl_stmt|;
specifier|private
name|long
name|createdAt
decl_stmt|;
specifier|private
name|MColumnDescriptor
name|cols
decl_stmt|;
specifier|private
name|int
name|state
decl_stmt|;
specifier|private
name|String
name|description
decl_stmt|;
specifier|private
name|String
name|schemaText
decl_stmt|;
specifier|private
name|String
name|fingerprint
decl_stmt|;
specifier|private
name|String
name|name
decl_stmt|;
specifier|private
name|MSerDeInfo
name|serDe
decl_stmt|;
specifier|public
name|MSchemaVersion
parameter_list|(
name|MISchema
name|iSchema
parameter_list|,
name|int
name|version
parameter_list|,
name|long
name|createdAt
parameter_list|,
name|MColumnDescriptor
name|cols
parameter_list|,
name|int
name|state
parameter_list|,
name|String
name|description
parameter_list|,
name|String
name|schemaText
parameter_list|,
name|String
name|fingerprint
parameter_list|,
name|String
name|name
parameter_list|,
name|MSerDeInfo
name|serDe
parameter_list|)
block|{
name|this
operator|.
name|iSchema
operator|=
name|iSchema
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
name|this
operator|.
name|createdAt
operator|=
name|createdAt
expr_stmt|;
name|this
operator|.
name|cols
operator|=
name|cols
expr_stmt|;
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
name|this
operator|.
name|description
operator|=
name|description
expr_stmt|;
name|this
operator|.
name|schemaText
operator|=
name|schemaText
expr_stmt|;
name|this
operator|.
name|fingerprint
operator|=
name|fingerprint
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|serDe
operator|=
name|serDe
expr_stmt|;
block|}
specifier|public
name|MISchema
name|getiSchema
parameter_list|()
block|{
return|return
name|iSchema
return|;
block|}
specifier|public
name|void
name|setiSchema
parameter_list|(
name|MISchema
name|iSchema
parameter_list|)
block|{
name|this
operator|.
name|iSchema
operator|=
name|iSchema
expr_stmt|;
block|}
specifier|public
name|int
name|getVersion
parameter_list|()
block|{
return|return
name|version
return|;
block|}
specifier|public
name|void
name|setVersion
parameter_list|(
name|int
name|version
parameter_list|)
block|{
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
block|}
specifier|public
name|long
name|getCreatedAt
parameter_list|()
block|{
return|return
name|createdAt
return|;
block|}
specifier|public
name|void
name|setCreatedAt
parameter_list|(
name|long
name|createdAt
parameter_list|)
block|{
name|this
operator|.
name|createdAt
operator|=
name|createdAt
expr_stmt|;
block|}
specifier|public
name|MColumnDescriptor
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
name|MColumnDescriptor
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
name|int
name|getState
parameter_list|()
block|{
return|return
name|state
return|;
block|}
specifier|public
name|void
name|setState
parameter_list|(
name|int
name|state
parameter_list|)
block|{
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
block|}
specifier|public
name|String
name|getDescription
parameter_list|()
block|{
return|return
name|description
return|;
block|}
specifier|public
name|void
name|setDescription
parameter_list|(
name|String
name|description
parameter_list|)
block|{
name|this
operator|.
name|description
operator|=
name|description
expr_stmt|;
block|}
specifier|public
name|String
name|getSchemaText
parameter_list|()
block|{
return|return
name|schemaText
return|;
block|}
specifier|public
name|void
name|setSchemaText
parameter_list|(
name|String
name|schemaText
parameter_list|)
block|{
name|this
operator|.
name|schemaText
operator|=
name|schemaText
expr_stmt|;
block|}
specifier|public
name|String
name|getFingerprint
parameter_list|()
block|{
return|return
name|fingerprint
return|;
block|}
specifier|public
name|void
name|setFingerprint
parameter_list|(
name|String
name|fingerprint
parameter_list|)
block|{
name|this
operator|.
name|fingerprint
operator|=
name|fingerprint
expr_stmt|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
specifier|public
name|void
name|setName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
specifier|public
name|MSerDeInfo
name|getSerDe
parameter_list|()
block|{
return|return
name|serDe
return|;
block|}
specifier|public
name|void
name|setSerDe
parameter_list|(
name|MSerDeInfo
name|serDe
parameter_list|)
block|{
name|this
operator|.
name|serDe
operator|=
name|serDe
expr_stmt|;
block|}
block|}
end_class

end_unit

