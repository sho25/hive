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
name|dbinstall
operator|.
name|rules
package|;
end_package

begin_comment
comment|/**  * JUnit TestRule for Mssql.  */
end_comment

begin_class
specifier|public
class|class
name|Mssql
extends|extends
name|DatabaseRule
block|{
annotation|@
name|Override
specifier|public
name|String
name|getDockerImageName
parameter_list|()
block|{
return|return
literal|"microsoft/mssql-server-linux:2017-GA"
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getDockerAdditionalArgs
parameter_list|()
block|{
return|return
name|buildArray
argument_list|(
literal|"-p"
argument_list|,
literal|"1433:1433"
argument_list|,
literal|"-e"
argument_list|,
literal|"ACCEPT_EULA=Y"
argument_list|,
literal|"-e"
argument_list|,
literal|"SA_PASSWORD="
operator|+
name|getDbRootPassword
argument_list|()
argument_list|,
literal|"-d"
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDbType
parameter_list|()
block|{
return|return
literal|"mssql"
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDbRootUser
parameter_list|()
block|{
return|return
literal|"SA"
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDbRootPassword
parameter_list|()
block|{
return|return
literal|"Its-a-s3cret"
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getJdbcDriver
parameter_list|()
block|{
return|return
name|com
operator|.
name|microsoft
operator|.
name|sqlserver
operator|.
name|jdbc
operator|.
name|SQLServerDriver
operator|.
name|class
operator|.
name|getName
argument_list|()
return|;
comment|// return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
block|}
annotation|@
name|Override
specifier|public
name|String
name|getJdbcUrl
parameter_list|()
block|{
return|return
literal|"jdbc:sqlserver://localhost:1433;DatabaseName="
operator|+
name|HIVE_DB
operator|+
literal|";"
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getInitialJdbcUrl
parameter_list|()
block|{
return|return
literal|"jdbc:sqlserver://localhost:1433"
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isContainerReady
parameter_list|(
name|String
name|logOutput
parameter_list|)
block|{
return|return
name|logOutput
operator|.
name|contains
argument_list|(
literal|"Recovery is complete. This is an informational message only. No user action is required."
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getHivePassword
parameter_list|()
block|{
return|return
literal|"h1vePassword!"
return|;
block|}
block|}
end_class

end_unit

