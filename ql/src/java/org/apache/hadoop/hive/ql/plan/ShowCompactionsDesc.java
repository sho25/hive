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
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_comment
comment|/**  * Descriptor for showing compactions.  */
end_comment

begin_class
specifier|public
class|class
name|ShowCompactionsDesc
extends|extends
name|DDLDesc
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
specifier|static
specifier|final
name|String
name|schema
init|=
literal|"dbname,tabname,partname,type,state,workerid,"
operator|+
literal|"starttime#string:string:string:string:string:string:string"
decl_stmt|;
specifier|private
name|String
name|resFile
decl_stmt|;
comment|/**    *    * @param resFile File that results of show will be written to.    */
specifier|public
name|ShowCompactionsDesc
parameter_list|(
name|Path
name|resFile
parameter_list|)
block|{
name|this
operator|.
name|resFile
operator|=
name|resFile
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
comment|/**    *  No arg constructor for serialization.    */
specifier|public
name|ShowCompactionsDesc
parameter_list|()
block|{   }
specifier|public
name|String
name|getSchema
parameter_list|()
block|{
return|return
name|schema
return|;
block|}
specifier|public
name|String
name|getResFile
parameter_list|()
block|{
return|return
name|resFile
return|;
block|}
block|}
end_class

end_unit

