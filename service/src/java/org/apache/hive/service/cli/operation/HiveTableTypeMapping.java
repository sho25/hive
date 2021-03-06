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
name|service
operator|.
name|cli
operator|.
name|operation
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

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
name|TableType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * HiveTableTypeMapping.  * Default table type mapping  *  */
end_comment

begin_class
specifier|public
class|class
name|HiveTableTypeMapping
implements|implements
name|TableTypeMapping
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HiveTableTypeMapping
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|String
index|[]
name|mapToHiveType
parameter_list|(
name|String
name|clientTypeName
parameter_list|)
block|{
return|return
operator|new
name|String
index|[]
block|{
name|mapToClientType
argument_list|(
name|clientTypeName
argument_list|)
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|mapToClientType
parameter_list|(
name|String
name|hiveTypeName
parameter_list|)
block|{
try|try
block|{
name|TableType
name|hiveType
init|=
name|TableType
operator|.
name|valueOf
argument_list|(
name|hiveTypeName
operator|.
name|toUpperCase
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|hiveType
operator|.
name|name
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Invalid hive table type "
operator|+
name|hiveTypeName
argument_list|)
expr_stmt|;
return|return
name|hiveTypeName
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getTableTypeNames
parameter_list|()
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|typeNameSet
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|TableType
name|typeNames
range|:
name|TableType
operator|.
name|values
argument_list|()
control|)
block|{
name|typeNameSet
operator|.
name|add
argument_list|(
name|typeNames
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|typeNameSet
return|;
block|}
block|}
end_class

end_unit

