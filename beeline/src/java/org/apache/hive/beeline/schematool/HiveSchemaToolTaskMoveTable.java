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
name|beeline
operator|.
name|schematool
package|;
end_package

begin_import
import|import static
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
name|utils
operator|.
name|StringUtils
operator|.
name|normalizeIdentifier
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Connection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|ResultSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Statement
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
name|HiveMetaException
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
comment|/**  * Print Hive version and schema version.  */
end_comment

begin_class
class|class
name|HiveSchemaToolTaskMoveTable
extends|extends
name|HiveSchemaToolTask
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
name|HiveSchemaToolTaskMoveTable
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|String
name|fromCat
decl_stmt|;
specifier|private
name|String
name|toCat
decl_stmt|;
specifier|private
name|String
name|fromDb
decl_stmt|;
specifier|private
name|String
name|toDb
decl_stmt|;
specifier|private
name|String
name|tableName
decl_stmt|;
annotation|@
name|Override
name|void
name|setCommandLineArguments
parameter_list|(
name|HiveSchemaToolCommandLine
name|cl
parameter_list|)
block|{
name|fromCat
operator|=
name|normalizeIdentifier
argument_list|(
name|cl
operator|.
name|getOptionValue
argument_list|(
literal|"fromCatalog"
argument_list|)
argument_list|)
expr_stmt|;
name|toCat
operator|=
name|normalizeIdentifier
argument_list|(
name|cl
operator|.
name|getOptionValue
argument_list|(
literal|"toCatalog"
argument_list|)
argument_list|)
expr_stmt|;
name|fromDb
operator|=
name|normalizeIdentifier
argument_list|(
name|cl
operator|.
name|getOptionValue
argument_list|(
literal|"fromDatabase"
argument_list|)
argument_list|)
expr_stmt|;
name|toDb
operator|=
name|normalizeIdentifier
argument_list|(
name|cl
operator|.
name|getOptionValue
argument_list|(
literal|"toDatabase"
argument_list|)
argument_list|)
expr_stmt|;
name|tableName
operator|=
name|normalizeIdentifier
argument_list|(
name|cl
operator|.
name|getOptionValue
argument_list|(
literal|"moveTable"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|execute
parameter_list|()
throws|throws
name|HiveMetaException
block|{
name|Connection
name|conn
init|=
name|schemaTool
operator|.
name|getConnectionToMetastore
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
name|conn
operator|.
name|setAutoCommit
argument_list|(
literal|false
argument_list|)
expr_stmt|;
try|try
init|(
name|Statement
name|stmt
init|=
name|conn
operator|.
name|createStatement
argument_list|()
init|)
block|{
name|updateTableId
argument_list|(
name|stmt
argument_list|)
expr_stmt|;
name|updateDbNameForTable
argument_list|(
name|stmt
argument_list|,
literal|"TAB_COL_STATS"
argument_list|,
literal|"TABLE_NAME"
argument_list|,
name|fromCat
argument_list|,
name|toCat
argument_list|,
name|fromDb
argument_list|,
name|toDb
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|updateDbNameForTable
argument_list|(
name|stmt
argument_list|,
literal|"PART_COL_STATS"
argument_list|,
literal|"TABLE_NAME"
argument_list|,
name|fromCat
argument_list|,
name|toCat
argument_list|,
name|fromDb
argument_list|,
name|toDb
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|updateDbNameForTable
argument_list|(
name|stmt
argument_list|,
literal|"PARTITION_EVENTS"
argument_list|,
literal|"TBL_NAME"
argument_list|,
name|fromCat
argument_list|,
name|toCat
argument_list|,
name|fromDb
argument_list|,
name|toDb
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|updateDbNameForTable
argument_list|(
name|stmt
argument_list|,
literal|"NOTIFICATION_LOG"
argument_list|,
literal|"TBL_NAME"
argument_list|,
name|fromCat
argument_list|,
name|toCat
argument_list|,
name|fromDb
argument_list|,
name|toDb
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|conn
operator|.
name|commit
argument_list|()
expr_stmt|;
name|success
operator|=
literal|true
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|SQLException
name|se
parameter_list|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"Failed to move table"
argument_list|,
name|se
argument_list|)
throw|;
block|}
finally|finally
block|{
try|try
block|{
if|if
condition|(
operator|!
name|success
condition|)
block|{
name|conn
operator|.
name|rollback
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
comment|// Not really much we can do here.
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to rollback, everything will probably go bad from here."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
specifier|final
name|String
name|UPDATE_TABLE_ID_STMT
init|=
literal|"update<q>TBLS<q> "
operator|+
literal|"   set<q>DB_ID<q> = %d "
operator|+
literal|" where<q>DB_ID<q> = %d "
operator|+
literal|"   and<q>TBL_NAME<q> = '%s'"
decl_stmt|;
specifier|private
name|void
name|updateTableId
parameter_list|(
name|Statement
name|stmt
parameter_list|)
throws|throws
name|SQLException
throws|,
name|HiveMetaException
block|{
comment|// Find the old database id
name|long
name|oldDbId
init|=
name|getDbId
argument_list|(
name|stmt
argument_list|,
name|fromDb
argument_list|,
name|fromCat
argument_list|)
decl_stmt|;
comment|// Find the new database id
name|long
name|newDbId
init|=
name|getDbId
argument_list|(
name|stmt
argument_list|,
name|toDb
argument_list|,
name|toCat
argument_list|)
decl_stmt|;
name|String
name|update
init|=
name|String
operator|.
name|format
argument_list|(
name|schemaTool
operator|.
name|quote
argument_list|(
name|UPDATE_TABLE_ID_STMT
argument_list|)
argument_list|,
name|newDbId
argument_list|,
name|oldDbId
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Going to run "
operator|+
name|update
argument_list|)
expr_stmt|;
name|int
name|numUpdated
init|=
name|stmt
operator|.
name|executeUpdate
argument_list|(
name|update
argument_list|)
decl_stmt|;
if|if
condition|(
name|numUpdated
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"Failed to properly update TBLS table.  Expected to update "
operator|+
literal|"1 row but instead updated "
operator|+
name|numUpdated
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
specifier|final
name|String
name|DB_ID_QUERY
init|=
literal|"select<q>DB_ID<q> "
operator|+
literal|"  from<q>DBS<q> "
operator|+
literal|" where<q>NAME<q> = '%s' "
operator|+
literal|"   and<q>CTLG_NAME<q> = '%s'"
decl_stmt|;
specifier|private
name|long
name|getDbId
parameter_list|(
name|Statement
name|stmt
parameter_list|,
name|String
name|db
parameter_list|,
name|String
name|catalog
parameter_list|)
throws|throws
name|SQLException
throws|,
name|HiveMetaException
block|{
name|String
name|query
init|=
name|String
operator|.
name|format
argument_list|(
name|schemaTool
operator|.
name|quote
argument_list|(
name|DB_ID_QUERY
argument_list|)
argument_list|,
name|db
argument_list|,
name|catalog
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Going to run "
operator|+
name|query
argument_list|)
expr_stmt|;
try|try
init|(
name|ResultSet
name|rs
init|=
name|stmt
operator|.
name|executeQuery
argument_list|(
name|query
argument_list|)
init|)
block|{
if|if
condition|(
operator|!
name|rs
operator|.
name|next
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"Unable to find database "
operator|+
name|fromDb
argument_list|)
throw|;
block|}
return|return
name|rs
operator|.
name|getLong
argument_list|(
literal|1
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
specifier|final
name|String
name|UPDATE_DB_NAME_STMT
init|=
literal|"update<q>%s<q> "
operator|+
literal|"   set<q>CAT_NAME<q> = '%s', "
operator|+
literal|"<q>DB_NAME<q> = '%s' "
operator|+
literal|" where<q>CAT_NAME<q> = '%s' "
operator|+
literal|"   and<q>DB_NAME<q> = '%s' "
operator|+
literal|"   and<q>%s<q> = '%s'"
decl_stmt|;
specifier|private
name|void
name|updateDbNameForTable
parameter_list|(
name|Statement
name|stmt
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|tableColumnName
parameter_list|,
name|String
name|fromCat
parameter_list|,
name|String
name|toCat
parameter_list|,
name|String
name|fromDb
parameter_list|,
name|String
name|toDb
parameter_list|,
name|String
name|hiveTblName
parameter_list|)
throws|throws
name|HiveMetaException
throws|,
name|SQLException
block|{
name|String
name|update
init|=
name|String
operator|.
name|format
argument_list|(
name|schemaTool
operator|.
name|quote
argument_list|(
name|UPDATE_DB_NAME_STMT
argument_list|)
argument_list|,
name|tableName
argument_list|,
name|toCat
argument_list|,
name|toDb
argument_list|,
name|fromCat
argument_list|,
name|fromDb
argument_list|,
name|tableColumnName
argument_list|,
name|hiveTblName
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Going to run "
operator|+
name|update
argument_list|)
expr_stmt|;
name|int
name|numUpdated
init|=
name|stmt
operator|.
name|executeUpdate
argument_list|(
name|update
argument_list|)
decl_stmt|;
if|if
condition|(
name|numUpdated
operator|>
literal|1
operator|||
name|numUpdated
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"Failed to properly update the "
operator|+
name|tableName
operator|+
literal|" table.  Expected to update 1 row but instead updated "
operator|+
name|numUpdated
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

