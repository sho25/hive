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
name|conf
package|;
end_package

begin_enum
specifier|public
enum|enum
name|DatabaseType
block|{
name|MYSQL
block|,
name|H2
block|,
name|DB2
block|,
name|DERBY
block|,
name|ORACLE
block|,
name|POSTGRES
block|,
name|MSSQL
block|,
name|METASTORE
block|,
name|JETHRO_DATA
block|}
end_enum

end_unit

