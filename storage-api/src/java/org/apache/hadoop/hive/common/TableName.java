begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|common
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
name|Objects
import|;
end_import

begin_comment
comment|/**  * A container for a fully qualified table name, i.e. catalogname.databasename.tablename.  Also  * includes utilities for string parsing.  */
end_comment

begin_class
specifier|public
class|class
name|TableName
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
comment|/** Exception message thrown. */
specifier|private
specifier|static
specifier|final
name|String
name|ILL_ARG_EXCEPTION_MSG
init|=
literal|"Table name must be either<tablename>,<dbname>.<tablename> "
operator|+
literal|"or<catname>.<dbname>.<tablename>"
decl_stmt|;
comment|/** Names of the related DB objects. */
specifier|private
specifier|final
name|String
name|cat
decl_stmt|;
specifier|private
specifier|final
name|String
name|db
decl_stmt|;
specifier|private
specifier|final
name|String
name|table
decl_stmt|;
comment|/**    *    * @param catName catalog name.  Cannot be null.  If you do not know it you can get it from    *            SessionState.getCurrentCatalog() if you want to use the catalog from the current    *            session, or from MetaStoreUtils.getDefaultCatalog() if you do not have a session    *            or want to use the default catalog for the Hive instance.    * @param dbName database name.  Cannot be null.  If you do not now it you can get it from    *           SessionState.getCurrentDatabase() or use Warehouse.DEFAULT_DATABASE_NAME.    * @param tableName  table name, cannot be null    */
specifier|public
name|TableName
parameter_list|(
specifier|final
name|String
name|catName
parameter_list|,
specifier|final
name|String
name|dbName
parameter_list|,
specifier|final
name|String
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|cat
operator|=
name|catName
expr_stmt|;
name|this
operator|.
name|db
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|tableName
expr_stmt|;
block|}
comment|/**    * Build a TableName from a string of the form [[catalog.]database.]table.    * @param name name in string form, not null    * @param defaultCatalog default catalog to use if catalog is not in the name.  If you do not    *                       know it you can get it from SessionState.getCurrentCatalog() if you    *                       want to use the catalog from the current session, or from    *                       MetaStoreUtils.getDefaultCatalog() if you do not have a session or    *                       want to use the default catalog for the Hive instance.    * @param defaultDatabase default database to use if database is not in the name.  If you do    *                        not now it you can get it from SessionState.getCurrentDatabase() or    *                        use Warehouse.DEFAULT_DATABASE_NAME.    * @return TableName    * @throws IllegalArgumentException if a non-null name is given    */
specifier|public
specifier|static
name|TableName
name|fromString
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|String
name|defaultCatalog
parameter_list|,
specifier|final
name|String
name|defaultDatabase
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
if|if
condition|(
name|name
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|String
operator|.
name|join
argument_list|(
literal|""
argument_list|,
literal|"Table value was null. "
argument_list|,
name|ILL_ARG_EXCEPTION_MSG
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|name
operator|.
name|contains
argument_list|(
name|DatabaseName
operator|.
name|CAT_DB_TABLE_SEPARATOR
argument_list|)
condition|)
block|{
name|String
index|[]
name|names
init|=
name|name
operator|.
name|split
argument_list|(
literal|"\\."
argument_list|)
decl_stmt|;
if|if
condition|(
name|names
operator|.
name|length
operator|==
literal|2
condition|)
block|{
return|return
operator|new
name|TableName
argument_list|(
name|defaultCatalog
argument_list|,
name|names
index|[
literal|0
index|]
argument_list|,
name|names
index|[
literal|1
index|]
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|names
operator|.
name|length
operator|==
literal|3
condition|)
block|{
return|return
operator|new
name|TableName
argument_list|(
name|names
index|[
literal|0
index|]
argument_list|,
name|names
index|[
literal|1
index|]
argument_list|,
name|names
index|[
literal|2
index|]
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|ILL_ARG_EXCEPTION_MSG
argument_list|)
throw|;
block|}
block|}
else|else
block|{
return|return
operator|new
name|TableName
argument_list|(
name|defaultCatalog
argument_list|,
name|defaultDatabase
argument_list|,
name|name
argument_list|)
return|;
block|}
block|}
specifier|public
name|String
name|getCat
parameter_list|()
block|{
return|return
name|cat
return|;
block|}
specifier|public
name|String
name|getDb
parameter_list|()
block|{
return|return
name|db
return|;
block|}
specifier|public
name|String
name|getTable
parameter_list|()
block|{
return|return
name|table
return|;
block|}
comment|/**    * Get the name in db.table format, for use with stuff not yet converted to use the catalog.    * Fair warning, that if the db is null, this will return null.tableName    * @deprecated use {@link #getNotEmptyDbTable()} instead.    */
comment|// to be @Deprecated
specifier|public
name|String
name|getDbTable
parameter_list|()
block|{
return|return
name|db
operator|+
name|DatabaseName
operator|.
name|CAT_DB_TABLE_SEPARATOR
operator|+
name|table
return|;
block|}
comment|/**    * Get the name in `db`.`table` escaped format, if db is not empty, otherwise pass only the table name.    */
specifier|public
name|String
name|getEscapedNotEmptyDbTable
parameter_list|()
block|{
return|return
name|db
operator|==
literal|null
operator|||
name|db
operator|.
name|trim
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|?
literal|"`"
operator|+
name|table
operator|+
literal|"`"
else|:
literal|"`"
operator|+
name|db
operator|+
literal|"`"
operator|+
name|DatabaseName
operator|.
name|CAT_DB_TABLE_SEPARATOR
operator|+
literal|"`"
operator|+
name|table
operator|+
literal|"`"
return|;
block|}
comment|/**    * Get the name in db.table format, if db is not empty, otherwise pass only the table name.    */
specifier|public
name|String
name|getNotEmptyDbTable
parameter_list|()
block|{
return|return
name|db
operator|==
literal|null
operator|||
name|db
operator|.
name|trim
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|?
name|table
else|:
name|db
operator|+
name|DatabaseName
operator|.
name|CAT_DB_TABLE_SEPARATOR
operator|+
name|table
return|;
block|}
comment|/**    * Get the name in db.table format, for use with stuff not yet converted to use the catalog.    */
specifier|public
specifier|static
name|String
name|getDbTable
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
return|return
name|dbName
operator|+
name|DatabaseName
operator|.
name|CAT_DB_TABLE_SEPARATOR
operator|+
name|tableName
return|;
block|}
specifier|public
specifier|static
name|String
name|getQualified
parameter_list|(
name|String
name|catName
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
return|return
name|catName
operator|+
name|DatabaseName
operator|.
name|CAT_DB_TABLE_SEPARATOR
operator|+
name|dbName
operator|+
name|DatabaseName
operator|.
name|CAT_DB_TABLE_SEPARATOR
operator|+
name|tableName
return|;
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
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|TableName
name|tableName
init|=
operator|(
name|TableName
operator|)
name|o
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|cat
argument_list|,
name|tableName
operator|.
name|cat
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|db
argument_list|,
name|tableName
operator|.
name|db
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|table
argument_list|,
name|tableName
operator|.
name|table
argument_list|)
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
name|cat
argument_list|,
name|db
argument_list|,
name|table
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|cat
operator|+
name|DatabaseName
operator|.
name|CAT_DB_TABLE_SEPARATOR
operator|+
name|db
operator|+
name|DatabaseName
operator|.
name|CAT_DB_TABLE_SEPARATOR
operator|+
name|table
return|;
block|}
block|}
end_class

end_unit

