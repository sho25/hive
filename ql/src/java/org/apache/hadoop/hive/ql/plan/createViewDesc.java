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
name|ql
operator|.
name|exec
operator|.
name|Utilities
import|;
end_import

begin_class
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"Create View"
argument_list|)
specifier|public
class|class
name|createViewDesc
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
name|viewName
decl_stmt|;
specifier|private
name|String
name|originalText
decl_stmt|;
specifier|private
name|String
name|expandedText
decl_stmt|;
specifier|private
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|schema
decl_stmt|;
specifier|private
name|String
name|comment
decl_stmt|;
specifier|private
name|boolean
name|ifNotExists
decl_stmt|;
specifier|public
name|createViewDesc
parameter_list|(
name|String
name|viewName
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|schema
parameter_list|,
name|String
name|comment
parameter_list|,
name|boolean
name|ifNotExists
parameter_list|)
block|{
name|this
operator|.
name|viewName
operator|=
name|viewName
expr_stmt|;
name|this
operator|.
name|schema
operator|=
name|schema
expr_stmt|;
name|this
operator|.
name|comment
operator|=
name|comment
expr_stmt|;
name|this
operator|.
name|ifNotExists
operator|=
name|ifNotExists
expr_stmt|;
block|}
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"name"
argument_list|)
specifier|public
name|String
name|getViewName
parameter_list|()
block|{
return|return
name|viewName
return|;
block|}
specifier|public
name|void
name|setViewName
parameter_list|(
name|String
name|viewName
parameter_list|)
block|{
name|this
operator|.
name|viewName
operator|=
name|viewName
expr_stmt|;
block|}
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"original text"
argument_list|)
specifier|public
name|String
name|getViewOriginalText
parameter_list|()
block|{
return|return
name|originalText
return|;
block|}
specifier|public
name|void
name|setViewOriginalText
parameter_list|(
name|String
name|originalText
parameter_list|)
block|{
name|this
operator|.
name|originalText
operator|=
name|originalText
expr_stmt|;
block|}
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"expanded text"
argument_list|)
specifier|public
name|String
name|getViewExpandedText
parameter_list|()
block|{
return|return
name|expandedText
return|;
block|}
specifier|public
name|void
name|setViewExpandedText
parameter_list|(
name|String
name|expandedText
parameter_list|)
block|{
name|this
operator|.
name|expandedText
operator|=
name|expandedText
expr_stmt|;
block|}
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"columns"
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getSchemaString
parameter_list|()
block|{
return|return
name|Utilities
operator|.
name|getFieldSchemaString
argument_list|(
name|schema
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getSchema
parameter_list|()
block|{
return|return
name|schema
return|;
block|}
specifier|public
name|void
name|setSchema
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|schema
parameter_list|)
block|{
name|this
operator|.
name|schema
operator|=
name|schema
expr_stmt|;
block|}
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"comment"
argument_list|)
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
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"if not exists"
argument_list|)
specifier|public
name|boolean
name|getIfNotExists
parameter_list|()
block|{
return|return
name|ifNotExists
return|;
block|}
specifier|public
name|void
name|setIfNotExists
parameter_list|(
name|boolean
name|ifNotExists
parameter_list|)
block|{
name|this
operator|.
name|ifNotExists
operator|=
name|ifNotExists
expr_stmt|;
block|}
block|}
end_class

end_unit

