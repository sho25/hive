begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
package|;
end_package

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
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|annotation
operator|.
name|XmlRootElement
import|;
end_import

begin_comment
comment|/**  * A description of the table to create.  */
end_comment

begin_class
annotation|@
name|XmlRootElement
specifier|public
class|class
name|TableDesc
extends|extends
name|GroupPermissionsDesc
block|{
specifier|public
name|boolean
name|external
init|=
literal|false
decl_stmt|;
specifier|public
name|boolean
name|ifNotExists
init|=
literal|false
decl_stmt|;
specifier|public
name|String
name|table
decl_stmt|;
specifier|public
name|String
name|comment
decl_stmt|;
specifier|public
name|List
argument_list|<
name|ColumnDesc
argument_list|>
name|columns
decl_stmt|;
specifier|public
name|List
argument_list|<
name|ColumnDesc
argument_list|>
name|partitionedBy
decl_stmt|;
specifier|public
name|ClusteredByDesc
name|clusteredBy
decl_stmt|;
specifier|public
name|StorageFormatDesc
name|format
decl_stmt|;
specifier|public
name|String
name|location
decl_stmt|;
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tableProperties
decl_stmt|;
comment|/**    * Create a new TableDesc    */
specifier|public
name|TableDesc
parameter_list|()
block|{   }
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"TableDesc(table=%s, columns=%s)"
argument_list|,
name|table
argument_list|,
name|columns
argument_list|)
return|;
block|}
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
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|TableDesc
operator|)
condition|)
return|return
literal|false
return|;
name|TableDesc
name|that
init|=
operator|(
name|TableDesc
operator|)
name|o
decl_stmt|;
return|return
name|xequals
argument_list|(
name|this
operator|.
name|external
argument_list|,
name|that
operator|.
name|external
argument_list|)
operator|&&
name|xequals
argument_list|(
name|this
operator|.
name|ifNotExists
argument_list|,
name|that
operator|.
name|ifNotExists
argument_list|)
operator|&&
name|xequals
argument_list|(
name|this
operator|.
name|table
argument_list|,
name|that
operator|.
name|table
argument_list|)
operator|&&
name|xequals
argument_list|(
name|this
operator|.
name|comment
argument_list|,
name|that
operator|.
name|comment
argument_list|)
operator|&&
name|xequals
argument_list|(
name|this
operator|.
name|columns
argument_list|,
name|that
operator|.
name|columns
argument_list|)
operator|&&
name|xequals
argument_list|(
name|this
operator|.
name|partitionedBy
argument_list|,
name|that
operator|.
name|partitionedBy
argument_list|)
operator|&&
name|xequals
argument_list|(
name|this
operator|.
name|clusteredBy
argument_list|,
name|that
operator|.
name|clusteredBy
argument_list|)
operator|&&
name|xequals
argument_list|(
name|this
operator|.
name|format
argument_list|,
name|that
operator|.
name|format
argument_list|)
operator|&&
name|xequals
argument_list|(
name|this
operator|.
name|location
argument_list|,
name|that
operator|.
name|location
argument_list|)
operator|&&
name|xequals
argument_list|(
name|this
operator|.
name|tableProperties
argument_list|,
name|that
operator|.
name|tableProperties
argument_list|)
operator|&&
name|super
operator|.
name|equals
argument_list|(
name|that
argument_list|)
return|;
block|}
comment|/**    * How to cluster the table.    */
annotation|@
name|XmlRootElement
specifier|public
specifier|static
class|class
name|ClusteredByDesc
block|{
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
decl_stmt|;
specifier|public
name|List
argument_list|<
name|ClusterSortOrderDesc
argument_list|>
name|sortedBy
decl_stmt|;
specifier|public
name|int
name|numberOfBuckets
decl_stmt|;
specifier|public
name|ClusteredByDesc
parameter_list|()
block|{     }
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|fmt
init|=
literal|"ClusteredByDesc(columnNames=%s, sortedBy=%s, numberOfBuckets=%s)"
decl_stmt|;
return|return
name|String
operator|.
name|format
argument_list|(
name|fmt
argument_list|,
name|columnNames
argument_list|,
name|sortedBy
argument_list|,
name|numberOfBuckets
argument_list|)
return|;
block|}
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
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|ClusteredByDesc
operator|)
condition|)
return|return
literal|false
return|;
name|ClusteredByDesc
name|that
init|=
operator|(
name|ClusteredByDesc
operator|)
name|o
decl_stmt|;
return|return
name|xequals
argument_list|(
name|this
operator|.
name|columnNames
argument_list|,
name|that
operator|.
name|columnNames
argument_list|)
operator|&&
name|xequals
argument_list|(
name|this
operator|.
name|sortedBy
argument_list|,
name|that
operator|.
name|sortedBy
argument_list|)
operator|&&
name|xequals
argument_list|(
name|this
operator|.
name|numberOfBuckets
argument_list|,
name|that
operator|.
name|numberOfBuckets
argument_list|)
return|;
block|}
block|}
comment|/**    * The clustered sort order.    */
annotation|@
name|XmlRootElement
specifier|public
specifier|static
class|class
name|ClusterSortOrderDesc
block|{
specifier|public
name|String
name|columnName
decl_stmt|;
specifier|public
name|SortDirectionDesc
name|order
decl_stmt|;
specifier|public
name|ClusterSortOrderDesc
parameter_list|()
block|{     }
specifier|public
name|ClusterSortOrderDesc
parameter_list|(
name|String
name|columnName
parameter_list|,
name|SortDirectionDesc
name|order
parameter_list|)
block|{
name|this
operator|.
name|columnName
operator|=
name|columnName
expr_stmt|;
name|this
operator|.
name|order
operator|=
name|order
expr_stmt|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"ClusterSortOrderDesc(columnName=%s, order=%s)"
argument_list|,
name|columnName
argument_list|,
name|order
argument_list|)
return|;
block|}
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
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|ClusterSortOrderDesc
operator|)
condition|)
return|return
literal|false
return|;
name|ClusterSortOrderDesc
name|that
init|=
operator|(
name|ClusterSortOrderDesc
operator|)
name|o
decl_stmt|;
return|return
name|xequals
argument_list|(
name|this
operator|.
name|columnName
argument_list|,
name|that
operator|.
name|columnName
argument_list|)
operator|&&
name|xequals
argument_list|(
name|this
operator|.
name|order
argument_list|,
name|that
operator|.
name|order
argument_list|)
return|;
block|}
block|}
comment|/**    * Ther ASC or DESC sort order.    */
annotation|@
name|XmlRootElement
specifier|public
specifier|static
enum|enum
name|SortDirectionDesc
block|{
name|ASC
block|,
name|DESC
block|}
comment|/**    * The storage format.    */
annotation|@
name|XmlRootElement
specifier|public
specifier|static
class|class
name|StorageFormatDesc
block|{
specifier|public
name|RowFormatDesc
name|rowFormat
decl_stmt|;
specifier|public
name|String
name|storedAs
decl_stmt|;
specifier|public
name|StoredByDesc
name|storedBy
decl_stmt|;
specifier|public
name|StorageFormatDesc
parameter_list|()
block|{     }
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
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|StorageFormatDesc
operator|)
condition|)
return|return
literal|false
return|;
name|StorageFormatDesc
name|that
init|=
operator|(
name|StorageFormatDesc
operator|)
name|o
decl_stmt|;
return|return
name|xequals
argument_list|(
name|this
operator|.
name|rowFormat
argument_list|,
name|that
operator|.
name|rowFormat
argument_list|)
operator|&&
name|xequals
argument_list|(
name|this
operator|.
name|storedAs
argument_list|,
name|that
operator|.
name|storedAs
argument_list|)
operator|&&
name|xequals
argument_list|(
name|this
operator|.
name|storedBy
argument_list|,
name|that
operator|.
name|storedBy
argument_list|)
return|;
block|}
block|}
comment|/**    * The Row Format.    */
annotation|@
name|XmlRootElement
specifier|public
specifier|static
class|class
name|RowFormatDesc
block|{
specifier|public
name|String
name|fieldsTerminatedBy
decl_stmt|;
specifier|public
name|String
name|collectionItemsTerminatedBy
decl_stmt|;
specifier|public
name|String
name|mapKeysTerminatedBy
decl_stmt|;
specifier|public
name|String
name|linesTerminatedBy
decl_stmt|;
specifier|public
name|SerdeDesc
name|serde
decl_stmt|;
specifier|public
name|RowFormatDesc
parameter_list|()
block|{     }
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
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|RowFormatDesc
operator|)
condition|)
return|return
literal|false
return|;
name|RowFormatDesc
name|that
init|=
operator|(
name|RowFormatDesc
operator|)
name|o
decl_stmt|;
return|return
name|xequals
argument_list|(
name|this
operator|.
name|fieldsTerminatedBy
argument_list|,
name|that
operator|.
name|fieldsTerminatedBy
argument_list|)
operator|&&
name|xequals
argument_list|(
name|this
operator|.
name|collectionItemsTerminatedBy
argument_list|,
name|that
operator|.
name|collectionItemsTerminatedBy
argument_list|)
operator|&&
name|xequals
argument_list|(
name|this
operator|.
name|mapKeysTerminatedBy
argument_list|,
name|that
operator|.
name|mapKeysTerminatedBy
argument_list|)
operator|&&
name|xequals
argument_list|(
name|this
operator|.
name|linesTerminatedBy
argument_list|,
name|that
operator|.
name|linesTerminatedBy
argument_list|)
operator|&&
name|xequals
argument_list|(
name|this
operator|.
name|serde
argument_list|,
name|that
operator|.
name|serde
argument_list|)
return|;
block|}
block|}
comment|/**    * The SERDE Row Format.    */
annotation|@
name|XmlRootElement
specifier|public
specifier|static
class|class
name|SerdeDesc
block|{
specifier|public
name|String
name|name
decl_stmt|;
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|properties
decl_stmt|;
specifier|public
name|SerdeDesc
parameter_list|()
block|{     }
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
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|SerdeDesc
operator|)
condition|)
return|return
literal|false
return|;
name|SerdeDesc
name|that
init|=
operator|(
name|SerdeDesc
operator|)
name|o
decl_stmt|;
return|return
name|xequals
argument_list|(
name|this
operator|.
name|name
argument_list|,
name|that
operator|.
name|name
argument_list|)
operator|&&
name|xequals
argument_list|(
name|this
operator|.
name|properties
argument_list|,
name|that
operator|.
name|properties
argument_list|)
return|;
block|}
block|}
comment|/**    * How to store the table.    */
annotation|@
name|XmlRootElement
specifier|public
specifier|static
class|class
name|StoredByDesc
block|{
specifier|public
name|String
name|className
decl_stmt|;
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|properties
decl_stmt|;
specifier|public
name|StoredByDesc
parameter_list|()
block|{     }
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
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|StoredByDesc
operator|)
condition|)
return|return
literal|false
return|;
name|StoredByDesc
name|that
init|=
operator|(
name|StoredByDesc
operator|)
name|o
decl_stmt|;
return|return
name|xequals
argument_list|(
name|this
operator|.
name|className
argument_list|,
name|that
operator|.
name|className
argument_list|)
operator|&&
name|xequals
argument_list|(
name|this
operator|.
name|properties
argument_list|,
name|that
operator|.
name|properties
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

