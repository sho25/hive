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
package|;
end_package

begin_comment
comment|/**  * @deprecated Use/modify {@link org.apache.hive.hcatalog.ExitException} instead  */
end_comment

begin_class
specifier|public
class|class
name|ExitException
extends|extends
name|SecurityException
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
operator|-
literal|1982617086752946683L
decl_stmt|;
specifier|private
specifier|final
name|int
name|status
decl_stmt|;
comment|/**    * @return the status    */
specifier|public
name|int
name|getStatus
parameter_list|()
block|{
return|return
name|status
return|;
block|}
specifier|public
name|ExitException
parameter_list|(
name|int
name|status
parameter_list|)
block|{
name|super
argument_list|(
literal|"Raising exception, instead of System.exit(). Return code was: "
operator|+
name|status
argument_list|)
expr_stmt|;
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
block|}
block|}
end_class

end_unit

