begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional debugrmation  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|llap
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_class
specifier|public
class|class
name|LogLevels
block|{
specifier|private
specifier|final
name|boolean
name|isT
decl_stmt|,
name|isD
decl_stmt|,
name|isI
decl_stmt|,
name|isW
decl_stmt|,
name|isE
decl_stmt|;
specifier|public
name|LogLevels
parameter_list|(
name|Log
name|log
parameter_list|)
block|{
name|isT
operator|=
name|log
operator|.
name|isTraceEnabled
argument_list|()
expr_stmt|;
name|isD
operator|=
name|log
operator|.
name|isDebugEnabled
argument_list|()
expr_stmt|;
name|isI
operator|=
name|log
operator|.
name|isInfoEnabled
argument_list|()
expr_stmt|;
name|isW
operator|=
name|log
operator|.
name|isWarnEnabled
argument_list|()
expr_stmt|;
name|isE
operator|=
name|log
operator|.
name|isErrorEnabled
argument_list|()
expr_stmt|;
block|}
specifier|public
name|boolean
name|isTraceEnabled
parameter_list|()
block|{
return|return
name|isT
return|;
block|}
specifier|public
name|boolean
name|isDebugEnabled
parameter_list|()
block|{
return|return
name|isD
return|;
block|}
specifier|public
name|boolean
name|isInfoEnabled
parameter_list|()
block|{
return|return
name|isI
return|;
block|}
specifier|public
name|boolean
name|isWarnEnabled
parameter_list|()
block|{
return|return
name|isW
return|;
block|}
specifier|public
name|boolean
name|isErrorEnabled
parameter_list|()
block|{
return|return
name|isE
return|;
block|}
block|}
end_class

end_unit

