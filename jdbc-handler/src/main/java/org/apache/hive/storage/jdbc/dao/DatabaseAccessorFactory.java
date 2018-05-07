begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|storage
operator|.
name|jdbc
operator|.
name|dao
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
name|conf
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|storage
operator|.
name|jdbc
operator|.
name|conf
operator|.
name|DatabaseType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|storage
operator|.
name|jdbc
operator|.
name|conf
operator|.
name|JdbcStorageConfig
import|;
end_import

begin_comment
comment|/**  * Factory for creating the correct DatabaseAccessor class for the job  */
end_comment

begin_class
specifier|public
class|class
name|DatabaseAccessorFactory
block|{
specifier|private
name|DatabaseAccessorFactory
parameter_list|()
block|{   }
specifier|public
specifier|static
name|DatabaseAccessor
name|getAccessor
parameter_list|(
name|DatabaseType
name|dbType
parameter_list|)
block|{
name|DatabaseAccessor
name|accessor
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|dbType
condition|)
block|{
case|case
name|MYSQL
case|:
name|accessor
operator|=
operator|new
name|MySqlDatabaseAccessor
argument_list|()
expr_stmt|;
break|break;
case|case
name|JETHRO_DATA
case|:
name|accessor
operator|=
operator|new
name|JethroDatabaseAccessor
argument_list|()
expr_stmt|;
break|break;
case|case
name|POSTGRES
case|:
name|accessor
operator|=
operator|new
name|PostgresDatabaseAccessor
argument_list|()
expr_stmt|;
break|break;
case|case
name|ORACLE
case|:
name|accessor
operator|=
operator|new
name|OracleDatabaseAccessor
argument_list|()
expr_stmt|;
break|break;
case|case
name|MSSQL
case|:
name|accessor
operator|=
operator|new
name|MsSqlDatabaseAccessor
argument_list|()
expr_stmt|;
break|break;
default|default:
name|accessor
operator|=
operator|new
name|GenericJdbcDatabaseAccessor
argument_list|()
expr_stmt|;
break|break;
block|}
return|return
name|accessor
return|;
block|}
specifier|public
specifier|static
name|DatabaseAccessor
name|getAccessor
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|DatabaseType
name|dbType
init|=
name|DatabaseType
operator|.
name|valueOf
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|JdbcStorageConfig
operator|.
name|DATABASE_TYPE
operator|.
name|getPropertyName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|getAccessor
argument_list|(
name|dbType
argument_list|)
return|;
block|}
block|}
end_class

end_unit

