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
name|plan
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
name|HashSet
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
name|hooks
operator|.
name|ReadEntity
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
name|hooks
operator|.
name|WriteEntity
import|;
end_import

begin_comment
comment|/**  * DDLWork.  *  */
end_comment

begin_class
specifier|public
class|class
name|DDLWork
implements|implements
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
name|CreateIndexDesc
name|createIndexDesc
decl_stmt|;
specifier|private
name|AlterIndexDesc
name|alterIndexDesc
decl_stmt|;
specifier|private
name|DropIndexDesc
name|dropIdxDesc
decl_stmt|;
specifier|private
name|CreateDatabaseDesc
name|createDatabaseDesc
decl_stmt|;
specifier|private
name|SwitchDatabaseDesc
name|switchDatabaseDesc
decl_stmt|;
specifier|private
name|DropDatabaseDesc
name|dropDatabaseDesc
decl_stmt|;
specifier|private
name|CreateTableDesc
name|createTblDesc
decl_stmt|;
specifier|private
name|CreateTableLikeDesc
name|createTblLikeDesc
decl_stmt|;
specifier|private
name|CreateViewDesc
name|createVwDesc
decl_stmt|;
specifier|private
name|DropTableDesc
name|dropTblDesc
decl_stmt|;
specifier|private
name|AlterTableDesc
name|alterTblDesc
decl_stmt|;
specifier|private
name|AlterIndexDesc
name|alterIdxDesc
decl_stmt|;
specifier|private
name|ShowDatabasesDesc
name|showDatabasesDesc
decl_stmt|;
specifier|private
name|ShowTablesDesc
name|showTblsDesc
decl_stmt|;
specifier|private
name|LockTableDesc
name|lockTblDesc
decl_stmt|;
specifier|private
name|UnlockTableDesc
name|unlockTblDesc
decl_stmt|;
specifier|private
name|ShowFunctionsDesc
name|showFuncsDesc
decl_stmt|;
specifier|private
name|ShowLocksDesc
name|showLocksDesc
decl_stmt|;
specifier|private
name|DescFunctionDesc
name|descFunctionDesc
decl_stmt|;
specifier|private
name|ShowPartitionsDesc
name|showPartsDesc
decl_stmt|;
specifier|private
name|DescTableDesc
name|descTblDesc
decl_stmt|;
specifier|private
name|AddPartitionDesc
name|addPartitionDesc
decl_stmt|;
specifier|private
name|AlterTableSimpleDesc
name|alterTblSimpleDesc
decl_stmt|;
specifier|private
name|MsckDesc
name|msckDesc
decl_stmt|;
specifier|private
name|ShowTableStatusDesc
name|showTblStatusDesc
decl_stmt|;
specifier|private
name|ShowIndexesDesc
name|showIndexesDesc
decl_stmt|;
specifier|private
name|DescDatabaseDesc
name|descDbDesc
decl_stmt|;
comment|/**    * ReadEntitites that are passed to the hooks.    */
specifier|protected
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
decl_stmt|;
comment|/**    * List of WriteEntities that are passed to the hooks.    */
specifier|protected
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
decl_stmt|;
specifier|public
name|DDLWork
parameter_list|()
block|{   }
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
block|{
name|this
operator|.
name|inputs
operator|=
name|inputs
expr_stmt|;
name|this
operator|.
name|outputs
operator|=
name|outputs
expr_stmt|;
block|}
specifier|public
name|DDLWork
parameter_list|(
name|CreateIndexDesc
name|createIndex
parameter_list|)
block|{
name|this
operator|.
name|createIndexDesc
operator|=
name|createIndex
expr_stmt|;
block|}
specifier|public
name|DDLWork
parameter_list|(
name|AlterIndexDesc
name|alterIndex
parameter_list|)
block|{
name|this
operator|.
name|alterIndexDesc
operator|=
name|alterIndex
expr_stmt|;
block|}
comment|/**    * @param createDatabaseDesc    *          Create Database descriptor    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|CreateDatabaseDesc
name|createDatabaseDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|createDatabaseDesc
operator|=
name|createDatabaseDesc
expr_stmt|;
block|}
comment|/**    * @param dropDatabaseDesc    *          Drop Database descriptor    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|DescDatabaseDesc
name|descDatabaseDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|descDbDesc
operator|=
name|descDatabaseDesc
expr_stmt|;
block|}
specifier|public
name|DescDatabaseDesc
name|getDescDatabaseDesc
parameter_list|()
block|{
return|return
name|descDbDesc
return|;
block|}
comment|/**    * @param dropDatabaseDesc    *          Drop Database descriptor    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|DropDatabaseDesc
name|dropDatabaseDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|dropDatabaseDesc
operator|=
name|dropDatabaseDesc
expr_stmt|;
block|}
comment|/**    * @param switchDatabaseDesc    *          Switch Database descriptor    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|SwitchDatabaseDesc
name|switchDatabaseDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|switchDatabaseDesc
operator|=
name|switchDatabaseDesc
expr_stmt|;
block|}
comment|/**    * @param alterTblDesc    *          alter table descriptor    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|AlterTableDesc
name|alterTblDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|alterTblDesc
operator|=
name|alterTblDesc
expr_stmt|;
block|}
comment|/**    * @param alterIdxDesc    *          alter index descriptor    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|AlterIndexDesc
name|alterIdxDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|alterIdxDesc
operator|=
name|alterIdxDesc
expr_stmt|;
block|}
comment|/**    * @param createTblDesc    *          create table descriptor    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|CreateTableDesc
name|createTblDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|createTblDesc
operator|=
name|createTblDesc
expr_stmt|;
block|}
comment|/**    * @param createTblLikeDesc    *          create table like descriptor    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|CreateTableLikeDesc
name|createTblLikeDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|createTblLikeDesc
operator|=
name|createTblLikeDesc
expr_stmt|;
block|}
comment|/**    * @param createVwDesc    *          create view descriptor    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|CreateViewDesc
name|createVwDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|createVwDesc
operator|=
name|createVwDesc
expr_stmt|;
block|}
comment|/**    * @param dropTblDesc    *          drop table descriptor    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|DropTableDesc
name|dropTblDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|dropTblDesc
operator|=
name|dropTblDesc
expr_stmt|;
block|}
comment|/**    * @param descTblDesc    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|DescTableDesc
name|descTblDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|descTblDesc
operator|=
name|descTblDesc
expr_stmt|;
block|}
comment|/**    * @param showDatabasesDesc    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|ShowDatabasesDesc
name|showDatabasesDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|showDatabasesDesc
operator|=
name|showDatabasesDesc
expr_stmt|;
block|}
comment|/**    * @param showTblsDesc    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|ShowTablesDesc
name|showTblsDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|showTblsDesc
operator|=
name|showTblsDesc
expr_stmt|;
block|}
comment|/**    * @param lockTblDesc    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|LockTableDesc
name|lockTblDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|lockTblDesc
operator|=
name|lockTblDesc
expr_stmt|;
block|}
comment|/**    * @param unlockTblDesc    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|UnlockTableDesc
name|unlockTblDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|unlockTblDesc
operator|=
name|unlockTblDesc
expr_stmt|;
block|}
comment|/**    * @param showFuncsDesc    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|ShowFunctionsDesc
name|showFuncsDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|showFuncsDesc
operator|=
name|showFuncsDesc
expr_stmt|;
block|}
comment|/**    * @param showLocksDesc    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|ShowLocksDesc
name|showLocksDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|showLocksDesc
operator|=
name|showLocksDesc
expr_stmt|;
block|}
comment|/**    * @param descFuncDesc    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|DescFunctionDesc
name|descFuncDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|descFunctionDesc
operator|=
name|descFuncDesc
expr_stmt|;
block|}
comment|/**    * @param showPartsDesc    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|ShowPartitionsDesc
name|showPartsDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|showPartsDesc
operator|=
name|showPartsDesc
expr_stmt|;
block|}
comment|/**    * @param addPartitionDesc    *          information about the partitions we want to add.    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|AddPartitionDesc
name|addPartitionDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|addPartitionDesc
operator|=
name|addPartitionDesc
expr_stmt|;
block|}
comment|/**    * @param touchDesc    *          information about the table/partitions that we want to touch    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|AlterTableSimpleDesc
name|simpleDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|alterTblSimpleDesc
operator|=
name|simpleDesc
expr_stmt|;
block|}
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|MsckDesc
name|checkDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|msckDesc
operator|=
name|checkDesc
expr_stmt|;
block|}
comment|/**    * @param showTblStatusDesc    *          show table status descriptor    */
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|ShowTableStatusDesc
name|showTblStatusDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|showTblStatusDesc
operator|=
name|showTblStatusDesc
expr_stmt|;
block|}
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|DropIndexDesc
name|dropIndexDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|dropIdxDesc
operator|=
name|dropIndexDesc
expr_stmt|;
block|}
specifier|public
name|DDLWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|ShowIndexesDesc
name|showIndexesDesc
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|showIndexesDesc
operator|=
name|showIndexesDesc
expr_stmt|;
block|}
comment|/**    * @return Create Database descriptor    */
specifier|public
name|CreateDatabaseDesc
name|getCreateDatabaseDesc
parameter_list|()
block|{
return|return
name|createDatabaseDesc
return|;
block|}
comment|/**    * Set Create Database descriptor    * @param createDatabaseDesc    */
specifier|public
name|void
name|setCreateDatabaseDesc
parameter_list|(
name|CreateDatabaseDesc
name|createDatabaseDesc
parameter_list|)
block|{
name|this
operator|.
name|createDatabaseDesc
operator|=
name|createDatabaseDesc
expr_stmt|;
block|}
comment|/**    * @return Drop Database descriptor    */
specifier|public
name|DropDatabaseDesc
name|getDropDatabaseDesc
parameter_list|()
block|{
return|return
name|dropDatabaseDesc
return|;
block|}
comment|/**    * Set Drop Database descriptor    * @param dropDatabaseDesc    */
specifier|public
name|void
name|setDropDatabaseDesc
parameter_list|(
name|DropDatabaseDesc
name|dropDatabaseDesc
parameter_list|)
block|{
name|this
operator|.
name|dropDatabaseDesc
operator|=
name|dropDatabaseDesc
expr_stmt|;
block|}
comment|/**    * @return Switch Database descriptor    */
specifier|public
name|SwitchDatabaseDesc
name|getSwitchDatabaseDesc
parameter_list|()
block|{
return|return
name|switchDatabaseDesc
return|;
block|}
comment|/**    * Set Switch Database descriptor    * @param switchDatabaseDesc    */
specifier|public
name|void
name|setSwitchDatabaseDesc
parameter_list|(
name|SwitchDatabaseDesc
name|switchDatabaseDesc
parameter_list|)
block|{
name|this
operator|.
name|switchDatabaseDesc
operator|=
name|switchDatabaseDesc
expr_stmt|;
block|}
comment|/**    * @return the createTblDesc    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Create Table Operator"
argument_list|)
specifier|public
name|CreateTableDesc
name|getCreateTblDesc
parameter_list|()
block|{
return|return
name|createTblDesc
return|;
block|}
comment|/**    * @param createTblDesc    *          the createTblDesc to set    */
specifier|public
name|void
name|setCreateTblDesc
parameter_list|(
name|CreateTableDesc
name|createTblDesc
parameter_list|)
block|{
name|this
operator|.
name|createTblDesc
operator|=
name|createTblDesc
expr_stmt|;
block|}
comment|/**    * @return the createIndexDesc    */
specifier|public
name|CreateIndexDesc
name|getCreateIndexDesc
parameter_list|()
block|{
return|return
name|createIndexDesc
return|;
block|}
comment|/**    * @param createIndexDesc    *          the createIndexDesc to set    */
specifier|public
name|void
name|setCreateIndexDesc
parameter_list|(
name|CreateIndexDesc
name|createIndexDesc
parameter_list|)
block|{
name|this
operator|.
name|createIndexDesc
operator|=
name|createIndexDesc
expr_stmt|;
block|}
comment|/**    * @return the alterIndexDesc    */
specifier|public
name|AlterIndexDesc
name|getAlterIndexDesc
parameter_list|()
block|{
return|return
name|alterIndexDesc
return|;
block|}
comment|/**    * @param alterTblDesc    *          the alterTblDesc to set    */
specifier|public
name|void
name|setAlterIndexDesc
parameter_list|(
name|AlterIndexDesc
name|alterIndexDesc
parameter_list|)
block|{
name|this
operator|.
name|alterIndexDesc
operator|=
name|alterIndexDesc
expr_stmt|;
block|}
comment|/**    * @return the createTblDesc    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Create Table Operator"
argument_list|)
specifier|public
name|CreateTableLikeDesc
name|getCreateTblLikeDesc
parameter_list|()
block|{
return|return
name|createTblLikeDesc
return|;
block|}
comment|/**    * @param createTblLikeDesc    *          the createTblDesc to set    */
specifier|public
name|void
name|setCreateTblLikeDesc
parameter_list|(
name|CreateTableLikeDesc
name|createTblLikeDesc
parameter_list|)
block|{
name|this
operator|.
name|createTblLikeDesc
operator|=
name|createTblLikeDesc
expr_stmt|;
block|}
comment|/**    * @return the createTblDesc    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Create View Operator"
argument_list|)
specifier|public
name|CreateViewDesc
name|getCreateViewDesc
parameter_list|()
block|{
return|return
name|createVwDesc
return|;
block|}
comment|/**    * @param createVwDesc    *          the createViewDesc to set    */
specifier|public
name|void
name|setCreateViewDesc
parameter_list|(
name|CreateViewDesc
name|createVwDesc
parameter_list|)
block|{
name|this
operator|.
name|createVwDesc
operator|=
name|createVwDesc
expr_stmt|;
block|}
comment|/**    * @return the dropTblDesc    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Drop Table Operator"
argument_list|)
specifier|public
name|DropTableDesc
name|getDropTblDesc
parameter_list|()
block|{
return|return
name|dropTblDesc
return|;
block|}
comment|/**    * @param dropTblDesc    *          the dropTblDesc to set    */
specifier|public
name|void
name|setDropTblDesc
parameter_list|(
name|DropTableDesc
name|dropTblDesc
parameter_list|)
block|{
name|this
operator|.
name|dropTblDesc
operator|=
name|dropTblDesc
expr_stmt|;
block|}
comment|/**    * @return the alterTblDesc    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Alter Table Operator"
argument_list|)
specifier|public
name|AlterTableDesc
name|getAlterTblDesc
parameter_list|()
block|{
return|return
name|alterTblDesc
return|;
block|}
comment|/**    * @param alterTblDesc    *          the alterTblDesc to set    */
specifier|public
name|void
name|setAlterTblDesc
parameter_list|(
name|AlterTableDesc
name|alterTblDesc
parameter_list|)
block|{
name|this
operator|.
name|alterTblDesc
operator|=
name|alterTblDesc
expr_stmt|;
block|}
comment|/**    * @return the showDatabasesDesc    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Show Databases Operator"
argument_list|)
specifier|public
name|ShowDatabasesDesc
name|getShowDatabasesDesc
parameter_list|()
block|{
return|return
name|showDatabasesDesc
return|;
block|}
comment|/**    * @param showDatabasesDesc    *          the showDatabasesDesc to set    */
specifier|public
name|void
name|setShowDatabasesDesc
parameter_list|(
name|ShowDatabasesDesc
name|showDatabasesDesc
parameter_list|)
block|{
name|this
operator|.
name|showDatabasesDesc
operator|=
name|showDatabasesDesc
expr_stmt|;
block|}
comment|/**    * @return the showTblsDesc    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Show Table Operator"
argument_list|)
specifier|public
name|ShowTablesDesc
name|getShowTblsDesc
parameter_list|()
block|{
return|return
name|showTblsDesc
return|;
block|}
comment|/**    * @param showTblsDesc    *          the showTblsDesc to set    */
specifier|public
name|void
name|setShowTblsDesc
parameter_list|(
name|ShowTablesDesc
name|showTblsDesc
parameter_list|)
block|{
name|this
operator|.
name|showTblsDesc
operator|=
name|showTblsDesc
expr_stmt|;
block|}
comment|/**    * @return the showFuncsDesc    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Show Function Operator"
argument_list|)
specifier|public
name|ShowFunctionsDesc
name|getShowFuncsDesc
parameter_list|()
block|{
return|return
name|showFuncsDesc
return|;
block|}
comment|/**    * @return the showLocksDesc    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Show Lock Operator"
argument_list|)
specifier|public
name|ShowLocksDesc
name|getShowLocksDesc
parameter_list|()
block|{
return|return
name|showLocksDesc
return|;
block|}
comment|/**    * @return the lockTblDesc    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Lock Table Operator"
argument_list|)
specifier|public
name|LockTableDesc
name|getLockTblDesc
parameter_list|()
block|{
return|return
name|lockTblDesc
return|;
block|}
comment|/**    * @return the unlockTblDesc    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Unlock Table Operator"
argument_list|)
specifier|public
name|UnlockTableDesc
name|getUnlockTblDesc
parameter_list|()
block|{
return|return
name|unlockTblDesc
return|;
block|}
comment|/**    * @return the descFuncDesc    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Show Function Operator"
argument_list|)
specifier|public
name|DescFunctionDesc
name|getDescFunctionDesc
parameter_list|()
block|{
return|return
name|descFunctionDesc
return|;
block|}
comment|/**    * @param showFuncsDesc    *          the showFuncsDesc to set    */
specifier|public
name|void
name|setShowFuncsDesc
parameter_list|(
name|ShowFunctionsDesc
name|showFuncsDesc
parameter_list|)
block|{
name|this
operator|.
name|showFuncsDesc
operator|=
name|showFuncsDesc
expr_stmt|;
block|}
comment|/**    * @param showLocksDesc    *          the showLocksDesc to set    */
specifier|public
name|void
name|setShowLocksDesc
parameter_list|(
name|ShowLocksDesc
name|showLocksDesc
parameter_list|)
block|{
name|this
operator|.
name|showLocksDesc
operator|=
name|showLocksDesc
expr_stmt|;
block|}
comment|/**    * @param lockTblDesc    *          the lockTblDesc to set    */
specifier|public
name|void
name|setLockTblDesc
parameter_list|(
name|LockTableDesc
name|lockTblDesc
parameter_list|)
block|{
name|this
operator|.
name|lockTblDesc
operator|=
name|lockTblDesc
expr_stmt|;
block|}
comment|/**    * @param unlockTblDesc    *          the unlockTblDesc to set    */
specifier|public
name|void
name|setUnlockTblDesc
parameter_list|(
name|UnlockTableDesc
name|unlockTblDesc
parameter_list|)
block|{
name|this
operator|.
name|unlockTblDesc
operator|=
name|unlockTblDesc
expr_stmt|;
block|}
comment|/**    * @param descFuncDesc    *          the showFuncsDesc to set    */
specifier|public
name|void
name|setDescFuncDesc
parameter_list|(
name|DescFunctionDesc
name|descFuncDesc
parameter_list|)
block|{
name|descFunctionDesc
operator|=
name|descFuncDesc
expr_stmt|;
block|}
comment|/**    * @return the showPartsDesc    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Show Partitions Operator"
argument_list|)
specifier|public
name|ShowPartitionsDesc
name|getShowPartsDesc
parameter_list|()
block|{
return|return
name|showPartsDesc
return|;
block|}
comment|/**    * @param showPartsDesc    *          the showPartsDesc to set    */
specifier|public
name|void
name|setShowPartsDesc
parameter_list|(
name|ShowPartitionsDesc
name|showPartsDesc
parameter_list|)
block|{
name|this
operator|.
name|showPartsDesc
operator|=
name|showPartsDesc
expr_stmt|;
block|}
comment|/**    * @return the showIndexesDesc    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Show Index Operator"
argument_list|)
specifier|public
name|ShowIndexesDesc
name|getShowIndexesDesc
parameter_list|()
block|{
return|return
name|showIndexesDesc
return|;
block|}
specifier|public
name|void
name|setShowIndexesDesc
parameter_list|(
name|ShowIndexesDesc
name|showIndexesDesc
parameter_list|)
block|{
name|this
operator|.
name|showIndexesDesc
operator|=
name|showIndexesDesc
expr_stmt|;
block|}
comment|/**    * @return the descTblDesc    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Describe Table Operator"
argument_list|)
specifier|public
name|DescTableDesc
name|getDescTblDesc
parameter_list|()
block|{
return|return
name|descTblDesc
return|;
block|}
comment|/**    * @param descTblDesc    *          the descTblDesc to set    */
specifier|public
name|void
name|setDescTblDesc
parameter_list|(
name|DescTableDesc
name|descTblDesc
parameter_list|)
block|{
name|this
operator|.
name|descTblDesc
operator|=
name|descTblDesc
expr_stmt|;
block|}
comment|/**    * @return information about the partitions we want to add.    */
specifier|public
name|AddPartitionDesc
name|getAddPartitionDesc
parameter_list|()
block|{
return|return
name|addPartitionDesc
return|;
block|}
comment|/**    * @param addPartitionDesc    *          information about the partitions we want to add.    */
specifier|public
name|void
name|setAddPartitionDesc
parameter_list|(
name|AddPartitionDesc
name|addPartitionDesc
parameter_list|)
block|{
name|this
operator|.
name|addPartitionDesc
operator|=
name|addPartitionDesc
expr_stmt|;
block|}
comment|/**    * @return information about the table/partitions we want to alter.    */
specifier|public
name|AlterTableSimpleDesc
name|getAlterTblSimpleDesc
parameter_list|()
block|{
return|return
name|alterTblSimpleDesc
return|;
block|}
comment|/**    * @param desc    *          information about the table/partitions we want to alter.    */
specifier|public
name|void
name|setAlterTblSimpleDesc
parameter_list|(
name|AlterTableSimpleDesc
name|desc
parameter_list|)
block|{
name|this
operator|.
name|alterTblSimpleDesc
operator|=
name|desc
expr_stmt|;
block|}
comment|/**    * @return Metastore check description    */
specifier|public
name|MsckDesc
name|getMsckDesc
parameter_list|()
block|{
return|return
name|msckDesc
return|;
block|}
comment|/**    * @param msckDesc    *          metastore check description    */
specifier|public
name|void
name|setMsckDesc
parameter_list|(
name|MsckDesc
name|msckDesc
parameter_list|)
block|{
name|this
operator|.
name|msckDesc
operator|=
name|msckDesc
expr_stmt|;
block|}
comment|/**    * @return show table descriptor    */
specifier|public
name|ShowTableStatusDesc
name|getShowTblStatusDesc
parameter_list|()
block|{
return|return
name|showTblStatusDesc
return|;
block|}
comment|/**    * @param showTblStatusDesc    *          show table descriptor    */
specifier|public
name|void
name|setShowTblStatusDesc
parameter_list|(
name|ShowTableStatusDesc
name|showTblStatusDesc
parameter_list|)
block|{
name|this
operator|.
name|showTblStatusDesc
operator|=
name|showTblStatusDesc
expr_stmt|;
block|}
specifier|public
name|CreateViewDesc
name|getCreateVwDesc
parameter_list|()
block|{
return|return
name|createVwDesc
return|;
block|}
specifier|public
name|void
name|setCreateVwDesc
parameter_list|(
name|CreateViewDesc
name|createVwDesc
parameter_list|)
block|{
name|this
operator|.
name|createVwDesc
operator|=
name|createVwDesc
expr_stmt|;
block|}
specifier|public
name|void
name|setDescFunctionDesc
parameter_list|(
name|DescFunctionDesc
name|descFunctionDesc
parameter_list|)
block|{
name|this
operator|.
name|descFunctionDesc
operator|=
name|descFunctionDesc
expr_stmt|;
block|}
specifier|public
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|getInputs
parameter_list|()
block|{
return|return
name|inputs
return|;
block|}
specifier|public
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|getOutputs
parameter_list|()
block|{
return|return
name|outputs
return|;
block|}
specifier|public
name|void
name|setInputs
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|)
block|{
name|this
operator|.
name|inputs
operator|=
name|inputs
expr_stmt|;
block|}
specifier|public
name|void
name|setOutputs
parameter_list|(
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
block|{
name|this
operator|.
name|outputs
operator|=
name|outputs
expr_stmt|;
block|}
specifier|public
name|DropIndexDesc
name|getDropIdxDesc
parameter_list|()
block|{
return|return
name|dropIdxDesc
return|;
block|}
specifier|public
name|void
name|setDropIdxDesc
parameter_list|(
name|DropIndexDesc
name|dropIdxDesc
parameter_list|)
block|{
name|this
operator|.
name|dropIdxDesc
operator|=
name|dropIdxDesc
expr_stmt|;
block|}
block|}
end_class

end_unit

