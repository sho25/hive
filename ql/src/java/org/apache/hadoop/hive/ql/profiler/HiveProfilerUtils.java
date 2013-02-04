begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/** * Licensed to the Apache Software Foundation (ASF) under one * or more contributor license agreements.  See the NOTICE file * distributed with this work for additional information * regarding copyright ownership.  The ASF licenses this file * to you under the Apache License, Version 2.0 (the * "License"); you may not use this file except in compliance * with the License.  You may obtain a copy of the License at * *     http://www.apache.org/licenses/LICENSE-2.0 * * Unless required by applicable law or agreed to in writing, software * distributed under the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. * See the License for the specific language governing permissions and * limitations under the License. */
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
name|profiler
package|;
end_package

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
name|Statement
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|DatabaseMetaData
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|DriverManager
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|ResultSet
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
name|LogFactory
import|;
end_import

begin_class
specifier|public
class|class
name|HiveProfilerUtils
block|{
specifier|public
specifier|static
name|void
name|createTableIfNonExistent
parameter_list|(
name|HiveProfilerConnectionInfo
name|info
parameter_list|,
name|String
name|createTable
parameter_list|)
throws|throws
name|Exception
block|{
name|Connection
name|conn
init|=
name|info
operator|.
name|getConnection
argument_list|()
decl_stmt|;
name|Statement
name|stmt
init|=
name|conn
operator|.
name|createStatement
argument_list|()
decl_stmt|;
name|stmt
operator|.
name|setQueryTimeout
argument_list|(
name|info
operator|.
name|getTimeout
argument_list|()
argument_list|)
expr_stmt|;
name|DatabaseMetaData
name|dbm
init|=
name|conn
operator|.
name|getMetaData
argument_list|()
decl_stmt|;
name|ResultSet
name|rs
init|=
name|dbm
operator|.
name|getTables
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|info
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|boolean
name|tblExists
init|=
name|rs
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|tblExists
condition|)
block|{
name|stmt
operator|.
name|executeUpdate
argument_list|(
name|createTable
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|boolean
name|closeConnection
parameter_list|(
name|HiveProfilerConnectionInfo
name|info
parameter_list|)
throws|throws
name|SQLException
block|{
name|info
operator|.
name|getConnection
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// In case of derby, explicitly shutdown the database otherwise it reports error when
comment|// trying to connect to the same JDBC connection string again.
if|if
condition|(
name|info
operator|.
name|getDbClass
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"jdbc:derby"
argument_list|)
condition|)
block|{
try|try
block|{
comment|// The following closes the derby connection. It throws an exception that has to be caught
comment|// and ignored.
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|info
operator|.
name|getConnectionString
argument_list|()
operator|+
literal|";shutdown=true"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Do nothing because we know that an exception is thrown anyway.
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

