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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|ddl
operator|.
name|table
operator|.
name|lock
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|ql
operator|.
name|ddl
operator|.
name|DDLDesc
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
name|plan
operator|.
name|Explain
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
name|plan
operator|.
name|Explain
operator|.
name|Level
import|;
end_import

begin_comment
comment|/**  * DDL task description for UNLOCK TABLE commands.  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Unlock Table"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|UnlockTableDesc
implements|implements
name|DDLDesc
implements|,
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
specifier|final
name|String
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
decl_stmt|;
specifier|public
name|UnlockTableDesc
parameter_list|(
name|String
name|tableName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|partSpec
operator|=
name|partSpec
expr_stmt|;
block|}
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getPartSpec
parameter_list|()
block|{
return|return
name|partSpec
return|;
block|}
block|}
end_class

end_unit

