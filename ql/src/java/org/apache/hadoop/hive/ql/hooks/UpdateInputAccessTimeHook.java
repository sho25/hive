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
name|hooks
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|conf
operator|.
name|HiveConf
import|;
end_import

begin_import
import|import
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
name|session
operator|.
name|SessionState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|security
operator|.
name|UserGroupInformation
import|;
end_import

begin_import
import|import
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
name|metadata
operator|.
name|Hive
import|;
end_import

begin_import
import|import
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
name|metadata
operator|.
name|HiveException
import|;
end_import

begin_import
import|import
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
name|metadata
operator|.
name|Partition
import|;
end_import

begin_import
import|import
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
name|metadata
operator|.
name|Table
import|;
end_import

begin_comment
comment|/**  * Implementation of a pre execute hook that updates the access  * times for all the inputs.  */
end_comment

begin_class
specifier|public
class|class
name|UpdateInputAccessTimeHook
block|{
specifier|private
specifier|static
specifier|final
name|String
name|LAST_ACCESS_TIME
init|=
literal|"lastAccessTime"
decl_stmt|;
specifier|public
specifier|static
class|class
name|PreExec
implements|implements
name|PreExecute
block|{
name|Hive
name|db
decl_stmt|;
specifier|public
name|void
name|run
parameter_list|(
name|SessionState
name|sess
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|UserGroupInformation
name|ugi
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|db
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|db
operator|=
name|Hive
operator|.
name|get
argument_list|(
name|sess
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
comment|// ignore
name|db
operator|=
literal|null
expr_stmt|;
return|return;
block|}
block|}
name|int
name|lastAccessTime
init|=
call|(
name|int
call|)
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|/
literal|1000
argument_list|)
decl_stmt|;
for|for
control|(
name|ReadEntity
name|re
range|:
name|inputs
control|)
block|{
comment|// Set the last query time
name|ReadEntity
operator|.
name|Type
name|typ
init|=
name|re
operator|.
name|getType
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|typ
condition|)
block|{
comment|// It is possible that read and write entities contain a old version
comment|// of the object, before it was modified by StatsTask.
comment|// Get the latest versions of the object
case|case
name|TABLE
case|:
block|{
name|Table
name|t
init|=
name|db
operator|.
name|getTable
argument_list|(
name|re
operator|.
name|getTable
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|t
operator|.
name|setLastAccessTime
argument_list|(
name|lastAccessTime
argument_list|)
expr_stmt|;
name|db
operator|.
name|alterTable
argument_list|(
name|t
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|t
operator|.
name|getTableName
argument_list|()
argument_list|,
name|t
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|PARTITION
case|:
block|{
name|Partition
name|p
init|=
name|re
operator|.
name|getPartition
argument_list|()
decl_stmt|;
name|Table
name|t
init|=
name|db
operator|.
name|getTable
argument_list|(
name|p
operator|.
name|getTable
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|p
operator|=
name|db
operator|.
name|getPartition
argument_list|(
name|t
argument_list|,
name|p
operator|.
name|getSpec
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|p
operator|.
name|setLastAccessTime
argument_list|(
name|lastAccessTime
argument_list|)
expr_stmt|;
name|db
operator|.
name|alterPartition
argument_list|(
name|t
operator|.
name|getTableName
argument_list|()
argument_list|,
name|p
argument_list|)
expr_stmt|;
name|t
operator|.
name|setLastAccessTime
argument_list|(
name|lastAccessTime
argument_list|)
expr_stmt|;
name|db
operator|.
name|alterTable
argument_list|(
name|t
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|t
operator|.
name|getTableName
argument_list|()
argument_list|,
name|t
argument_list|)
expr_stmt|;
break|break;
block|}
default|default:
comment|// ignore dummy inputs
break|break;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

