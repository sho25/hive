begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|mapreduce
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|JobContext
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
name|mapreduce
operator|.
name|OutputCommitter
import|;
end_import

begin_comment
comment|/**  *  This class will contain an implementation of an OutputCommitter.  *  See {@link OutputFormatContainer} for more information about containers.  * @deprecated Use/modify {@link org.apache.hive.hcatalog.mapreduce.OutputCommitterContainer} instead  */
end_comment

begin_class
specifier|abstract
class|class
name|OutputCommitterContainer
extends|extends
name|OutputCommitter
block|{
specifier|private
specifier|final
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|OutputCommitter
name|committer
decl_stmt|;
comment|/**      * @param context current JobContext      * @param committer OutputCommitter that this instance will contain      */
specifier|public
name|OutputCommitterContainer
parameter_list|(
name|JobContext
name|context
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|OutputCommitter
name|committer
parameter_list|)
block|{
name|this
operator|.
name|committer
operator|=
name|committer
expr_stmt|;
block|}
comment|/**      * @return underlying OutputCommitter      */
specifier|public
name|OutputCommitter
name|getBaseOutputCommitter
parameter_list|()
block|{
return|return
name|committer
return|;
block|}
block|}
end_class

end_unit

