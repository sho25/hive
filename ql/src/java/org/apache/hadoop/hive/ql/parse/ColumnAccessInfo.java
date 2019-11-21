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
name|parse
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
name|hive
operator|.
name|ql
operator|.
name|metadata
operator|.
name|VirtualColumn
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
name|LinkedHashMultimap
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
name|SetMultimap
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
name|LinkedHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|Objects
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_class
specifier|public
class|class
name|ColumnAccessInfo
block|{
comment|/**    * Map of table name to names of accessed columns (directly and indirectly -through views-).    */
specifier|private
specifier|final
name|SetMultimap
argument_list|<
name|String
argument_list|,
name|ColumnAccess
argument_list|>
name|tableToColumnAccessMap
decl_stmt|;
specifier|public
name|ColumnAccessInfo
parameter_list|()
block|{
comment|// Must be deterministic order map for consistent q-test output across Java versions
name|tableToColumnAccessMap
operator|=
name|LinkedHashMultimap
operator|.
name|create
argument_list|()
expr_stmt|;
block|}
comment|/**    * Adds access to column.    */
specifier|public
name|void
name|add
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|col
parameter_list|)
block|{
name|tableToColumnAccessMap
operator|.
name|put
argument_list|(
name|table
argument_list|,
operator|new
name|ColumnAccess
argument_list|(
name|col
argument_list|,
name|Access
operator|.
name|DIRECT
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Adds indirect access to column (through view).    */
specifier|public
name|void
name|addIndirect
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|col
parameter_list|)
block|{
name|tableToColumnAccessMap
operator|.
name|put
argument_list|(
name|table
argument_list|,
operator|new
name|ColumnAccess
argument_list|(
name|col
argument_list|,
name|Access
operator|.
name|INDIRECT
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Includes direct access.    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|getTableToColumnAccessMap
parameter_list|()
block|{
comment|// Must be deterministic order map for consistent q-test output across Java versions
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|mapping
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Collection
argument_list|<
name|ColumnAccess
argument_list|>
argument_list|>
name|entry
range|:
name|tableToColumnAccessMap
operator|.
name|asMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|sortedCols
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|ca
lambda|->
name|ca
operator|.
name|access
operator|==
name|Access
operator|.
name|DIRECT
argument_list|)
operator|.
name|map
argument_list|(
name|ca
lambda|->
name|ca
operator|.
name|columnName
argument_list|)
operator|.
name|sorted
argument_list|()
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|sortedCols
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|mapping
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|sortedCols
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|mapping
return|;
block|}
comment|/**    * Includes direct and indirect access.    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|getTableToColumnAllAccessMap
parameter_list|()
block|{
comment|// Must be deterministic order map for consistent q-test output across Java versions
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|mapping
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Collection
argument_list|<
name|ColumnAccess
argument_list|>
argument_list|>
name|entry
range|:
name|tableToColumnAccessMap
operator|.
name|asMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|mapping
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|ca
lambda|->
name|ca
operator|.
name|columnName
argument_list|)
operator|.
name|distinct
argument_list|()
operator|.
name|sorted
argument_list|()
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|mapping
return|;
block|}
comment|/**    * Strip a virtual column out of the set of columns.  This is useful in cases where we do not    * want to be checking against the user reading virtual columns, namely update and delete.    * @param vc    */
specifier|public
name|void
name|stripVirtualColumn
parameter_list|(
name|VirtualColumn
name|vc
parameter_list|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Collection
argument_list|<
name|ColumnAccess
argument_list|>
argument_list|>
name|e
range|:
name|tableToColumnAccessMap
operator|.
name|asMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
for|for
control|(
name|ColumnAccess
name|columnAccess
range|:
name|e
operator|.
name|getValue
argument_list|()
control|)
block|{
if|if
condition|(
name|vc
operator|.
name|getName
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|columnAccess
operator|.
name|columnName
argument_list|)
condition|)
block|{
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|remove
argument_list|(
name|columnAccess
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
block|}
comment|/**    * Column access information.    */
specifier|private
specifier|static
class|class
name|ColumnAccess
block|{
specifier|private
specifier|final
name|String
name|columnName
decl_stmt|;
specifier|private
specifier|final
name|Access
name|access
decl_stmt|;
specifier|private
name|ColumnAccess
parameter_list|(
name|String
name|columnName
parameter_list|,
name|Access
name|access
parameter_list|)
block|{
name|this
operator|.
name|columnName
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|columnName
argument_list|)
expr_stmt|;
name|this
operator|.
name|access
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|access
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|instanceof
name|ColumnAccess
condition|)
block|{
name|ColumnAccess
name|other
init|=
operator|(
name|ColumnAccess
operator|)
name|o
decl_stmt|;
return|return
name|columnName
operator|.
name|equals
argument_list|(
name|other
operator|.
name|columnName
argument_list|)
operator|&&
name|access
operator|==
name|other
operator|.
name|access
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|columnName
argument_list|,
name|access
argument_list|)
return|;
block|}
block|}
specifier|private
enum|enum
name|Access
block|{
name|DIRECT
block|,
name|INDIRECT
block|}
block|}
end_class

end_unit

