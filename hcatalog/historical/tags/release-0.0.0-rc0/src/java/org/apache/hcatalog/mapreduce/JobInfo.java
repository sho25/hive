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
name|hcatalog
operator|.
name|mapreduce
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
name|List
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatSchema
import|;
end_import

begin_comment
comment|/** The class used to serialize and store the information read from the metadata server */
end_comment

begin_class
specifier|public
class|class
name|JobInfo
implements|implements
name|Serializable
block|{
comment|/** The serialization version */
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|/** The db and table names. */
specifier|private
specifier|final
name|String
name|dbName
decl_stmt|;
specifier|private
specifier|final
name|String
name|tableName
decl_stmt|;
comment|/** The table schema. */
specifier|private
specifier|final
name|HCatSchema
name|tableSchema
decl_stmt|;
comment|/** The list of partitions matching the filter. */
specifier|private
specifier|final
name|List
argument_list|<
name|PartInfo
argument_list|>
name|partitions
decl_stmt|;
comment|/**      * Instantiates a new howl job info.      * @param tableName the table name      * @param tableSchema the table schema      * @param partitions the partitions      */
specifier|public
name|JobInfo
parameter_list|(
name|HCatTableInfo
name|howlTableInfo
parameter_list|,
name|HCatSchema
name|tableSchema
parameter_list|,
name|List
argument_list|<
name|PartInfo
argument_list|>
name|partitions
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|howlTableInfo
operator|.
name|getTableName
argument_list|()
expr_stmt|;
name|this
operator|.
name|dbName
operator|=
name|howlTableInfo
operator|.
name|getDatabaseName
argument_list|()
expr_stmt|;
name|this
operator|.
name|tableSchema
operator|=
name|tableSchema
expr_stmt|;
name|this
operator|.
name|partitions
operator|=
name|partitions
expr_stmt|;
block|}
comment|/**      * Gets the value of dbName      * @return the dbName      */
specifier|public
name|String
name|getDatabaseName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
comment|/**      * Gets the value of tableName      * @return the tableName      */
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
comment|/**      * Gets the value of tableSchema      * @return the tableSchema      */
specifier|public
name|HCatSchema
name|getTableSchema
parameter_list|()
block|{
return|return
name|tableSchema
return|;
block|}
comment|/**      * Gets the value of partitions      * @return the partitions      */
specifier|public
name|List
argument_list|<
name|PartInfo
argument_list|>
name|getPartitions
parameter_list|()
block|{
return|return
name|partitions
return|;
block|}
block|}
end_class

end_unit

