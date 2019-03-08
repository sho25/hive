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
name|io
operator|.
name|AcidUtils
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
name|Table
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
name|parse
operator|.
name|BaseSemanticAnalyzer
operator|.
name|TableSpec
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
name|parse
operator|.
name|ReplicationSpec
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

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Export Work"
argument_list|,
name|explainLevels
operator|=
block|{
name|Explain
operator|.
name|Level
operator|.
name|USER
block|,
name|Explain
operator|.
name|Level
operator|.
name|DEFAULT
block|,
name|Explain
operator|.
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|ExportWork
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ExportWork
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
specifier|final
specifier|static
class|class
name|MmContext
block|{
specifier|private
specifier|final
name|String
name|fqTableName
decl_stmt|;
specifier|private
name|MmContext
parameter_list|(
name|String
name|fqTableName
parameter_list|)
block|{
name|this
operator|.
name|fqTableName
operator|=
name|fqTableName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"["
operator|+
name|fqTableName
operator|+
literal|"]"
return|;
block|}
specifier|public
specifier|static
name|MmContext
name|createIfNeeded
parameter_list|(
name|Table
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|==
literal|null
condition|)
return|return
literal|null
return|;
if|if
condition|(
operator|!
name|AcidUtils
operator|.
name|isInsertOnlyTable
argument_list|(
name|t
operator|.
name|getParameters
argument_list|()
argument_list|)
condition|)
return|return
literal|null
return|;
return|return
operator|new
name|MmContext
argument_list|(
name|AcidUtils
operator|.
name|getFullTableName
argument_list|(
name|t
operator|.
name|getDbName
argument_list|()
argument_list|,
name|t
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|String
name|getFqTableName
parameter_list|()
block|{
return|return
name|fqTableName
return|;
block|}
block|}
specifier|private
specifier|final
name|String
name|exportRootDirName
decl_stmt|;
specifier|private
name|TableSpec
name|tableSpec
decl_stmt|;
specifier|private
name|ReplicationSpec
name|replicationSpec
decl_stmt|;
specifier|private
name|String
name|astRepresentationForErrorMsg
decl_stmt|;
specifier|private
name|String
name|acidFqTableName
decl_stmt|;
specifier|private
specifier|final
name|MmContext
name|mmContext
decl_stmt|;
comment|/**    * @param acidFqTableName if exporting Acid table, this is temp table - null otherwise    */
specifier|public
name|ExportWork
parameter_list|(
name|String
name|exportRootDirName
parameter_list|,
name|TableSpec
name|tableSpec
parameter_list|,
name|ReplicationSpec
name|replicationSpec
parameter_list|,
name|String
name|astRepresentationForErrorMsg
parameter_list|,
name|String
name|acidFqTableName
parameter_list|,
name|MmContext
name|mmContext
parameter_list|)
block|{
name|this
operator|.
name|exportRootDirName
operator|=
name|exportRootDirName
expr_stmt|;
name|this
operator|.
name|tableSpec
operator|=
name|tableSpec
expr_stmt|;
name|this
operator|.
name|replicationSpec
operator|=
name|replicationSpec
expr_stmt|;
name|this
operator|.
name|astRepresentationForErrorMsg
operator|=
name|astRepresentationForErrorMsg
expr_stmt|;
name|this
operator|.
name|mmContext
operator|=
name|mmContext
expr_stmt|;
name|this
operator|.
name|acidFqTableName
operator|=
name|acidFqTableName
expr_stmt|;
block|}
specifier|public
name|String
name|getExportRootDir
parameter_list|()
block|{
return|return
name|exportRootDirName
return|;
block|}
specifier|public
name|TableSpec
name|getTableSpec
parameter_list|()
block|{
return|return
name|tableSpec
return|;
block|}
specifier|public
name|ReplicationSpec
name|getReplicationSpec
parameter_list|()
block|{
return|return
name|replicationSpec
return|;
block|}
specifier|public
name|String
name|getAstRepresentationForErrorMsg
parameter_list|()
block|{
return|return
name|astRepresentationForErrorMsg
return|;
block|}
specifier|public
name|MmContext
name|getMmContext
parameter_list|()
block|{
return|return
name|mmContext
return|;
block|}
comment|/**    * For exporting Acid table, change the "pointer" to the temp table.    * This has to be done after the temp table is populated and all necessary Partition objects    * exist in the metastore.    * See {@link org.apache.hadoop.hive.ql.parse.AcidExportSemanticAnalyzer#isAcidExport(ASTNode)}    * for more info.    */
specifier|public
name|void
name|acidPostProcess
parameter_list|(
name|Hive
name|db
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|acidFqTableName
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Swapping export of "
operator|+
name|tableSpec
operator|.
name|tableName
operator|+
literal|" to "
operator|+
name|acidFqTableName
operator|+
literal|" using partSpec="
operator|+
name|tableSpec
operator|.
name|partSpec
argument_list|)
expr_stmt|;
name|tableSpec
operator|=
operator|new
name|TableSpec
argument_list|(
name|db
argument_list|,
name|acidFqTableName
argument_list|,
name|tableSpec
operator|.
name|partSpec
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

