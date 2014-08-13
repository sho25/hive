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

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"show grant desc"
argument_list|)
specifier|public
class|class
name|ShowGrantDesc
block|{
specifier|private
name|PrincipalDesc
name|principalDesc
decl_stmt|;
specifier|private
name|PrivilegeObjectDesc
name|hiveObj
decl_stmt|;
specifier|private
name|String
name|resFile
decl_stmt|;
comment|/**    * thrift ddl for the result of show grant.    */
specifier|private
specifier|static
specifier|final
name|String
name|tabularSchema
init|=
literal|"database,table,partition,column,principal_name,principal_type,privilege,"
operator|+
literal|"grant_option,grant_time,grantor#"
operator|+
literal|"string:string:string:string:string:string:string:boolean:bigint:string"
decl_stmt|;
specifier|public
name|ShowGrantDesc
parameter_list|()
block|{   }
specifier|public
name|ShowGrantDesc
parameter_list|(
name|String
name|resFile
parameter_list|,
name|PrincipalDesc
name|principalDesc
parameter_list|,
name|PrivilegeObjectDesc
name|subjectObj
parameter_list|)
block|{
name|this
operator|.
name|resFile
operator|=
name|resFile
expr_stmt|;
name|this
operator|.
name|principalDesc
operator|=
name|principalDesc
expr_stmt|;
name|this
operator|.
name|hiveObj
operator|=
name|subjectObj
expr_stmt|;
block|}
specifier|public
specifier|static
name|String
name|getSchema
parameter_list|()
block|{
return|return
name|tabularSchema
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"principal desc"
argument_list|)
specifier|public
name|PrincipalDesc
name|getPrincipalDesc
parameter_list|()
block|{
return|return
name|principalDesc
return|;
block|}
specifier|public
name|void
name|setPrincipalDesc
parameter_list|(
name|PrincipalDesc
name|principalDesc
parameter_list|)
block|{
name|this
operator|.
name|principalDesc
operator|=
name|principalDesc
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"object"
argument_list|)
specifier|public
name|PrivilegeObjectDesc
name|getHiveObj
parameter_list|()
block|{
return|return
name|hiveObj
return|;
block|}
specifier|public
name|void
name|setHiveObj
parameter_list|(
name|PrivilegeObjectDesc
name|subjectObj
parameter_list|)
block|{
name|this
operator|.
name|hiveObj
operator|=
name|subjectObj
expr_stmt|;
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
specifier|public
name|void
name|setResFile
parameter_list|(
name|String
name|resFile
parameter_list|)
block|{
name|this
operator|.
name|resFile
operator|=
name|resFile
expr_stmt|;
block|}
block|}
end_class

end_unit

