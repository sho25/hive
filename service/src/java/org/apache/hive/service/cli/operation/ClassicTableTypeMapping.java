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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

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
name|Map
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ArrayListMultimap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterables
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Multimap
import|;
end_import

begin_comment
comment|/**  * ClassicTableTypeMapping.  * Classic table type mapping :  *  Managed Table ==> Table  *  External Table ==> Table  *  Virtual View ==> View  */
end_comment

begin_class
specifier|public
class|class
name|ClassicTableTypeMapping
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
name|ClassicTableTypeMapping
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
enum|enum
name|ClassicTableTypes
block|{
name|TABLE
block|,
name|VIEW
block|,
name|MATERIALIZED_VIEW
block|,   }
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|hiveToClientMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Multimap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|clientToHiveMap
init|=
name|ArrayListMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
specifier|public
name|ClassicTableTypeMapping
parameter_list|()
block|{
name|hiveToClientMap
operator|.
name|put
argument_list|(
name|TableType
operator|.
name|MANAGED_TABLE
operator|.
name|name
argument_list|()
argument_list|,
name|ClassicTableTypes
operator|.
name|TABLE
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|hiveToClientMap
operator|.
name|put
argument_list|(
name|TableType
operator|.
name|EXTERNAL_TABLE
operator|.
name|name
argument_list|()
argument_list|,
name|ClassicTableTypes
operator|.
name|TABLE
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|hiveToClientMap
operator|.
name|put
argument_list|(
name|TableType
operator|.
name|VIRTUAL_VIEW
operator|.
name|name
argument_list|()
argument_list|,
name|ClassicTableTypes
operator|.
name|VIEW
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|hiveToClientMap
operator|.
name|put
argument_list|(
name|TableType
operator|.
name|MATERIALIZED_VIEW
operator|.
name|toString
argument_list|()
argument_list|,
name|ClassicTableTypes
operator|.
name|MATERIALIZED_VIEW
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|clientToHiveMap
operator|.
name|putAll
argument_list|(
name|ClassicTableTypes
operator|.
name|TABLE
operator|.
name|name
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|TableType
operator|.
name|MANAGED_TABLE
operator|.
name|name
argument_list|()
argument_list|,
name|TableType
operator|.
name|EXTERNAL_TABLE
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|clientToHiveMap
operator|.
name|put
argument_list|(
name|ClassicTableTypes
operator|.
name|VIEW
operator|.
name|name
argument_list|()
argument_list|,
name|TableType
operator|.
name|VIRTUAL_VIEW
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|clientToHiveMap
operator|.
name|put
argument_list|(
name|ClassicTableTypes
operator|.
name|MATERIALIZED_VIEW
operator|.
name|toString
argument_list|()
argument_list|,
name|TableType
operator|.
name|MATERIALIZED_VIEW
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|Collection
argument_list|<
name|String
argument_list|>
name|hiveTableType
init|=
name|clientToHiveMap
operator|.
name|get
argument_list|(
name|clientTypeName
operator|.
name|toUpperCase
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|hiveTableType
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Not supported client table type "
operator|+
name|clientTypeName
argument_list|)
expr_stmt|;
return|return
operator|new
name|String
index|[]
block|{
name|clientTypeName
block|}
return|;
block|}
return|return
name|Iterables
operator|.
name|toArray
argument_list|(
name|hiveTableType
argument_list|,
name|String
operator|.
name|class
argument_list|)
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
name|String
name|clientTypeName
init|=
name|hiveToClientMap
operator|.
name|get
argument_list|(
name|hiveTypeName
argument_list|)
decl_stmt|;
if|if
condition|(
name|clientTypeName
operator|==
literal|null
condition|)
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
return|return
name|clientTypeName
return|;
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
name|ClassicTableTypes
name|typeNames
range|:
name|ClassicTableTypes
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

