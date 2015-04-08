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
name|parse
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
name|plan
operator|.
name|Explain
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
name|plan
operator|.
name|ListBucketingCtx
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
name|mapred
operator|.
name|InputFormat
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
name|plan
operator|.
name|Explain
operator|.
name|Level
import|;
end_import

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Alter Table Partition Merge Files"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|AlterTablePartMergeFilesDesc
block|{
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
decl_stmt|;
specifier|private
name|ListBucketingCtx
name|lbCtx
decl_stmt|;
comment|// context for list bucketing.
specifier|private
name|List
argument_list|<
name|Path
argument_list|>
name|inputDir
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Path
name|outputDir
init|=
literal|null
decl_stmt|;
specifier|private
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|inputFormatClass
decl_stmt|;
specifier|public
name|AlterTablePartMergeFilesDesc
parameter_list|(
name|String
name|tableName
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
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
name|partSpec
operator|=
name|partSpec
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"table name"
argument_list|)
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
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"partition desc"
argument_list|)
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getPartSpec
parameter_list|()
block|{
return|return
name|partSpec
return|;
block|}
specifier|public
name|void
name|setPartSpec
parameter_list|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
block|{
name|this
operator|.
name|partSpec
operator|=
name|partSpec
expr_stmt|;
block|}
specifier|public
name|Path
name|getOutputDir
parameter_list|()
block|{
return|return
name|outputDir
return|;
block|}
specifier|public
name|void
name|setOutputDir
parameter_list|(
name|Path
name|outputDir
parameter_list|)
block|{
name|this
operator|.
name|outputDir
operator|=
name|outputDir
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|getInputDir
parameter_list|()
block|{
return|return
name|inputDir
return|;
block|}
specifier|public
name|void
name|setInputDir
parameter_list|(
name|List
argument_list|<
name|Path
argument_list|>
name|inputDir
parameter_list|)
block|{
name|this
operator|.
name|inputDir
operator|=
name|inputDir
expr_stmt|;
block|}
comment|/**    * @return the lbCtx    */
specifier|public
name|ListBucketingCtx
name|getLbCtx
parameter_list|()
block|{
return|return
name|lbCtx
return|;
block|}
comment|/**    * @param lbCtx the lbCtx to set    */
specifier|public
name|void
name|setLbCtx
parameter_list|(
name|ListBucketingCtx
name|lbCtx
parameter_list|)
block|{
name|this
operator|.
name|lbCtx
operator|=
name|lbCtx
expr_stmt|;
block|}
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|getInputFormatClass
parameter_list|()
block|{
return|return
name|inputFormatClass
return|;
block|}
specifier|public
name|void
name|setInputFormatClass
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|inputFormatClass
parameter_list|)
block|{
name|this
operator|.
name|inputFormatClass
operator|=
name|inputFormatClass
expr_stmt|;
block|}
block|}
end_class

end_unit

