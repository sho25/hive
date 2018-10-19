begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|JdbcStorageConfigManager
import|;
end_import

begin_comment
comment|/**  * JethroData specific data accessor. This is needed because JethroData JDBC drivers do  * not support generic LIMIT and OFFSET escape functions, and has  some special optimization  * for getting the query metadata using limit 0.  */
end_comment

begin_class
specifier|public
class|class
name|JethroDatabaseAccessor
extends|extends
name|GenericJdbcDatabaseAccessor
block|{
annotation|@
name|Override
specifier|protected
name|String
name|addLimitAndOffsetToQuery
parameter_list|(
name|String
name|sql
parameter_list|,
name|int
name|limit
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
if|if
condition|(
name|offset
operator|==
literal|0
condition|)
block|{
return|return
name|addLimitToQuery
argument_list|(
name|sql
argument_list|,
name|limit
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|sql
operator|+
literal|" LIMIT "
operator|+
name|offset
operator|+
literal|","
operator|+
name|limit
return|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|String
name|addLimitToQuery
parameter_list|(
name|String
name|sql
parameter_list|,
name|int
name|limit
parameter_list|)
block|{
return|return
literal|"Select * from ("
operator|+
name|sql
operator|+
literal|") as \"tmp\" limit "
operator|+
name|limit
return|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|getMetaDataQuery
parameter_list|(
name|String
name|sql
parameter_list|)
block|{
return|return
name|addLimitToQuery
argument_list|(
name|sql
argument_list|,
literal|0
argument_list|)
return|;
block|}
block|}
end_class

end_unit

