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
name|fs
operator|.
name|Path
import|;
end_import

begin_class
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"Fetch Operator"
argument_list|)
specifier|public
class|class
name|fetchWork
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
name|tblDir
decl_stmt|;
specifier|private
name|tableDesc
name|tblDesc
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|partDir
decl_stmt|;
specifier|private
name|List
argument_list|<
name|partitionDesc
argument_list|>
name|partDesc
decl_stmt|;
specifier|private
name|int
name|limit
decl_stmt|;
comment|/**    * Serialization Null Format for the serde used to fetch data    */
specifier|private
name|String
name|serializationNullFormat
init|=
literal|"NULL"
decl_stmt|;
specifier|public
name|fetchWork
parameter_list|()
block|{   }
specifier|public
name|fetchWork
parameter_list|(
name|String
name|tblDir
parameter_list|,
name|tableDesc
name|tblDesc
parameter_list|)
block|{
name|this
argument_list|(
name|tblDir
argument_list|,
name|tblDesc
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|fetchWork
parameter_list|(
name|String
name|tblDir
parameter_list|,
name|tableDesc
name|tblDesc
parameter_list|,
name|int
name|limit
parameter_list|)
block|{
name|this
operator|.
name|tblDir
operator|=
name|tblDir
expr_stmt|;
name|this
operator|.
name|tblDesc
operator|=
name|tblDesc
expr_stmt|;
name|this
operator|.
name|limit
operator|=
name|limit
expr_stmt|;
block|}
specifier|public
name|fetchWork
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|partDir
parameter_list|,
name|List
argument_list|<
name|partitionDesc
argument_list|>
name|partDesc
parameter_list|)
block|{
name|this
argument_list|(
name|partDir
argument_list|,
name|partDesc
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|fetchWork
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|partDir
parameter_list|,
name|List
argument_list|<
name|partitionDesc
argument_list|>
name|partDesc
parameter_list|,
name|int
name|limit
parameter_list|)
block|{
name|this
operator|.
name|partDir
operator|=
name|partDir
expr_stmt|;
name|this
operator|.
name|partDesc
operator|=
name|partDesc
expr_stmt|;
name|this
operator|.
name|limit
operator|=
name|limit
expr_stmt|;
block|}
specifier|public
name|String
name|getSerializationNullFormat
parameter_list|()
block|{
return|return
name|serializationNullFormat
return|;
block|}
specifier|public
name|void
name|setSerializationNullFormat
parameter_list|(
name|String
name|format
parameter_list|)
block|{
name|serializationNullFormat
operator|=
name|format
expr_stmt|;
block|}
comment|/**    * @return the tblDir    */
specifier|public
name|String
name|getTblDir
parameter_list|()
block|{
return|return
name|tblDir
return|;
block|}
comment|/**    * @return the tblDir    */
specifier|public
name|Path
name|getTblDirPath
parameter_list|()
block|{
return|return
operator|new
name|Path
argument_list|(
name|tblDir
argument_list|)
return|;
block|}
comment|/**    * @param tblDir    *          the tblDir to set    */
specifier|public
name|void
name|setTblDir
parameter_list|(
name|String
name|tblDir
parameter_list|)
block|{
name|this
operator|.
name|tblDir
operator|=
name|tblDir
expr_stmt|;
block|}
comment|/**    * @return the tblDesc    */
specifier|public
name|tableDesc
name|getTblDesc
parameter_list|()
block|{
return|return
name|tblDesc
return|;
block|}
comment|/**    * @param tblDesc    *          the tblDesc to set    */
specifier|public
name|void
name|setTblDesc
parameter_list|(
name|tableDesc
name|tblDesc
parameter_list|)
block|{
name|this
operator|.
name|tblDesc
operator|=
name|tblDesc
expr_stmt|;
block|}
comment|/**    * @return the partDir    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getPartDir
parameter_list|()
block|{
return|return
name|partDir
return|;
block|}
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|getPartDirPath
parameter_list|()
block|{
return|return
name|fetchWork
operator|.
name|convertStringToPathArray
argument_list|(
name|partDir
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|convertPathToStringArray
parameter_list|(
name|List
argument_list|<
name|Path
argument_list|>
name|paths
parameter_list|)
block|{
if|if
condition|(
name|paths
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|pathsStr
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Path
name|path
range|:
name|paths
control|)
block|{
name|pathsStr
operator|.
name|add
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|pathsStr
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|Path
argument_list|>
name|convertStringToPathArray
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|paths
parameter_list|)
block|{
if|if
condition|(
name|paths
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|Path
argument_list|>
name|pathsStr
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|path
range|:
name|paths
control|)
block|{
name|pathsStr
operator|.
name|add
argument_list|(
operator|new
name|Path
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|pathsStr
return|;
block|}
comment|/**    * @param partDir    *          the partDir to set    */
specifier|public
name|void
name|setPartDir
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|partDir
parameter_list|)
block|{
name|this
operator|.
name|partDir
operator|=
name|partDir
expr_stmt|;
block|}
comment|/**    * @return the partDesc    */
specifier|public
name|List
argument_list|<
name|partitionDesc
argument_list|>
name|getPartDesc
parameter_list|()
block|{
return|return
name|partDesc
return|;
block|}
comment|/**    * @param partDesc    *          the partDesc to set    */
specifier|public
name|void
name|setPartDesc
parameter_list|(
name|List
argument_list|<
name|partitionDesc
argument_list|>
name|partDesc
parameter_list|)
block|{
name|this
operator|.
name|partDesc
operator|=
name|partDesc
expr_stmt|;
block|}
comment|/**    * @return the limit    */
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"limit"
argument_list|)
specifier|public
name|int
name|getLimit
parameter_list|()
block|{
return|return
name|limit
return|;
block|}
comment|/**    * @param limit    *          the limit to set    */
specifier|public
name|void
name|setLimit
parameter_list|(
name|int
name|limit
parameter_list|)
block|{
name|this
operator|.
name|limit
operator|=
name|limit
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
if|if
condition|(
name|tblDir
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|String
argument_list|(
literal|"table = "
operator|+
name|tblDir
argument_list|)
return|;
block|}
if|if
condition|(
name|partDir
operator|==
literal|null
condition|)
block|{
return|return
literal|"null fetchwork"
return|;
block|}
name|String
name|ret
init|=
operator|new
name|String
argument_list|(
literal|"partition = "
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|part
range|:
name|partDir
control|)
block|{
name|ret
operator|=
name|ret
operator|.
name|concat
argument_list|(
name|part
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
block|}
end_class

end_unit

