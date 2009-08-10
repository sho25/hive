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
name|jdbc
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Connection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|sql
operator|.
name|DataSource
import|;
end_import

begin_class
specifier|public
class|class
name|HiveDataSource
implements|implements
name|DataSource
block|{
comment|/**    *    */
specifier|public
name|HiveDataSource
parameter_list|()
block|{
comment|// TODO Auto-generated constructor stub
block|}
comment|/* (non-Javadoc)    * @see javax.sql.DataSource#getConnection()    */
specifier|public
name|Connection
name|getConnection
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)    */
specifier|public
name|Connection
name|getConnection
parameter_list|(
name|String
name|username
parameter_list|,
name|String
name|password
parameter_list|)
throws|throws
name|SQLException
block|{
try|try
block|{
return|return
operator|new
name|HiveConnection
argument_list|(
literal|""
argument_list|,
literal|null
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|()
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see javax.sql.CommonDataSource#getLogWriter()    */
specifier|public
name|PrintWriter
name|getLogWriter
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see javax.sql.CommonDataSource#getLoginTimeout()    */
specifier|public
name|int
name|getLoginTimeout
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)    */
specifier|public
name|void
name|setLogWriter
parameter_list|(
name|PrintWriter
name|arg0
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see javax.sql.CommonDataSource#setLoginTimeout(int)    */
specifier|public
name|void
name|setLoginTimeout
parameter_list|(
name|int
name|arg0
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)    */
specifier|public
name|boolean
name|isWrapperFor
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|arg0
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.Wrapper#unwrap(java.lang.Class)    */
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|unwrap
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|arg0
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

