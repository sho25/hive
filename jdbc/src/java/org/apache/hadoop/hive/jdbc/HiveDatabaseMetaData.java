begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  */
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
name|jdbc
package|;
end_package

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
name|RowIdLifetime
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

begin_class
specifier|public
class|class
name|HiveDatabaseMetaData
implements|implements
name|java
operator|.
name|sql
operator|.
name|DatabaseMetaData
block|{
comment|/**    *    */
specifier|public
name|HiveDatabaseMetaData
parameter_list|()
block|{
comment|// TODO Auto-generated constructor stub
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#allProceduresAreCallable()    */
specifier|public
name|boolean
name|allProceduresAreCallable
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#allTablesAreSelectable()    */
specifier|public
name|boolean
name|allTablesAreSelectable
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#autoCommitFailureClosesAllResultSets()    */
specifier|public
name|boolean
name|autoCommitFailureClosesAllResultSets
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#dataDefinitionCausesTransactionCommit()    */
specifier|public
name|boolean
name|dataDefinitionCausesTransactionCommit
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#dataDefinitionIgnoredInTransactions()    */
specifier|public
name|boolean
name|dataDefinitionIgnoredInTransactions
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#deletesAreDetected(int)    */
specifier|public
name|boolean
name|deletesAreDetected
parameter_list|(
name|int
name|type
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#doesMaxRowSizeIncludeBlobs()    */
specifier|public
name|boolean
name|doesMaxRowSizeIncludeBlobs
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getAttributes(java.lang.String, java.lang.String, java.lang.String, java.lang.String)    */
specifier|public
name|ResultSet
name|getAttributes
parameter_list|(
name|String
name|catalog
parameter_list|,
name|String
name|schemaPattern
parameter_list|,
name|String
name|typeNamePattern
parameter_list|,
name|String
name|attributeNamePattern
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getBestRowIdentifier(java.lang.String, java.lang.String, java.lang.String, int, boolean)    */
specifier|public
name|ResultSet
name|getBestRowIdentifier
parameter_list|(
name|String
name|catalog
parameter_list|,
name|String
name|schema
parameter_list|,
name|String
name|table
parameter_list|,
name|int
name|scope
parameter_list|,
name|boolean
name|nullable
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getCatalogSeparator()    */
specifier|public
name|String
name|getCatalogSeparator
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getCatalogTerm()    */
specifier|public
name|String
name|getCatalogTerm
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getCatalogs()    */
specifier|public
name|ResultSet
name|getCatalogs
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getClientInfoProperties()    */
specifier|public
name|ResultSet
name|getClientInfoProperties
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getColumnPrivileges(java.lang.String, java.lang.String, java.lang.String, java.lang.String)    */
specifier|public
name|ResultSet
name|getColumnPrivileges
parameter_list|(
name|String
name|catalog
parameter_list|,
name|String
name|schema
parameter_list|,
name|String
name|table
parameter_list|,
name|String
name|columnNamePattern
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getColumns(java.lang.String, java.lang.String, java.lang.String, java.lang.String)    */
specifier|public
name|ResultSet
name|getColumns
parameter_list|(
name|String
name|catalog
parameter_list|,
name|String
name|schemaPattern
parameter_list|,
name|String
name|tableNamePattern
parameter_list|,
name|String
name|columnNamePattern
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getConnection()    */
specifier|public
name|Connection
name|getConnection
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getCrossReference(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)    */
specifier|public
name|ResultSet
name|getCrossReference
parameter_list|(
name|String
name|primaryCatalog
parameter_list|,
name|String
name|primarySchema
parameter_list|,
name|String
name|primaryTable
parameter_list|,
name|String
name|foreignCatalog
parameter_list|,
name|String
name|foreignSchema
parameter_list|,
name|String
name|foreignTable
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getDatabaseMajorVersion()    */
specifier|public
name|int
name|getDatabaseMajorVersion
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getDatabaseMinorVersion()    */
specifier|public
name|int
name|getDatabaseMinorVersion
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getDatabaseProductName()    */
specifier|public
name|String
name|getDatabaseProductName
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getDatabaseProductVersion()    */
specifier|public
name|String
name|getDatabaseProductVersion
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getDefaultTransactionIsolation()    */
specifier|public
name|int
name|getDefaultTransactionIsolation
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getDriverMajorVersion()    */
specifier|public
name|int
name|getDriverMajorVersion
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|0
return|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getDriverMinorVersion()    */
specifier|public
name|int
name|getDriverMinorVersion
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|0
return|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getDriverName()    */
specifier|public
name|String
name|getDriverName
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
operator|new
name|String
argument_list|(
literal|"hive"
argument_list|)
return|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getDriverVersion()    */
specifier|public
name|String
name|getDriverVersion
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
operator|new
name|String
argument_list|(
literal|"0"
argument_list|)
return|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getExportedKeys(java.lang.String, java.lang.String, java.lang.String)    */
specifier|public
name|ResultSet
name|getExportedKeys
parameter_list|(
name|String
name|catalog
parameter_list|,
name|String
name|schema
parameter_list|,
name|String
name|table
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getExtraNameCharacters()    */
specifier|public
name|String
name|getExtraNameCharacters
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getFunctionColumns(java.lang.String, java.lang.String, java.lang.String, java.lang.String)    */
specifier|public
name|ResultSet
name|getFunctionColumns
parameter_list|(
name|String
name|arg0
parameter_list|,
name|String
name|arg1
parameter_list|,
name|String
name|arg2
parameter_list|,
name|String
name|arg3
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getFunctions(java.lang.String, java.lang.String, java.lang.String)    */
specifier|public
name|ResultSet
name|getFunctions
parameter_list|(
name|String
name|arg0
parameter_list|,
name|String
name|arg1
parameter_list|,
name|String
name|arg2
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getIdentifierQuoteString()    */
specifier|public
name|String
name|getIdentifierQuoteString
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getImportedKeys(java.lang.String, java.lang.String, java.lang.String)    */
specifier|public
name|ResultSet
name|getImportedKeys
parameter_list|(
name|String
name|catalog
parameter_list|,
name|String
name|schema
parameter_list|,
name|String
name|table
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getIndexInfo(java.lang.String, java.lang.String, java.lang.String, boolean, boolean)    */
specifier|public
name|ResultSet
name|getIndexInfo
parameter_list|(
name|String
name|catalog
parameter_list|,
name|String
name|schema
parameter_list|,
name|String
name|table
parameter_list|,
name|boolean
name|unique
parameter_list|,
name|boolean
name|approximate
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getJDBCMajorVersion()    */
specifier|public
name|int
name|getJDBCMajorVersion
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
literal|3
return|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getJDBCMinorVersion()    */
specifier|public
name|int
name|getJDBCMinorVersion
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
literal|0
return|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getMaxBinaryLiteralLength()    */
specifier|public
name|int
name|getMaxBinaryLiteralLength
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getMaxCatalogNameLength()    */
specifier|public
name|int
name|getMaxCatalogNameLength
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getMaxCharLiteralLength()    */
specifier|public
name|int
name|getMaxCharLiteralLength
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getMaxColumnNameLength()    */
specifier|public
name|int
name|getMaxColumnNameLength
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getMaxColumnsInGroupBy()    */
specifier|public
name|int
name|getMaxColumnsInGroupBy
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getMaxColumnsInIndex()    */
specifier|public
name|int
name|getMaxColumnsInIndex
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getMaxColumnsInOrderBy()    */
specifier|public
name|int
name|getMaxColumnsInOrderBy
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getMaxColumnsInSelect()    */
specifier|public
name|int
name|getMaxColumnsInSelect
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getMaxColumnsInTable()    */
specifier|public
name|int
name|getMaxColumnsInTable
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getMaxConnections()    */
specifier|public
name|int
name|getMaxConnections
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getMaxCursorNameLength()    */
specifier|public
name|int
name|getMaxCursorNameLength
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getMaxIndexLength()    */
specifier|public
name|int
name|getMaxIndexLength
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getMaxProcedureNameLength()    */
specifier|public
name|int
name|getMaxProcedureNameLength
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getMaxRowSize()    */
specifier|public
name|int
name|getMaxRowSize
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getMaxSchemaNameLength()    */
specifier|public
name|int
name|getMaxSchemaNameLength
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getMaxStatementLength()    */
specifier|public
name|int
name|getMaxStatementLength
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getMaxStatements()    */
specifier|public
name|int
name|getMaxStatements
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getMaxTableNameLength()    */
specifier|public
name|int
name|getMaxTableNameLength
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getMaxTablesInSelect()    */
specifier|public
name|int
name|getMaxTablesInSelect
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getMaxUserNameLength()    */
specifier|public
name|int
name|getMaxUserNameLength
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getNumericFunctions()    */
specifier|public
name|String
name|getNumericFunctions
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getPrimaryKeys(java.lang.String, java.lang.String, java.lang.String)    */
specifier|public
name|ResultSet
name|getPrimaryKeys
parameter_list|(
name|String
name|catalog
parameter_list|,
name|String
name|schema
parameter_list|,
name|String
name|table
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getProcedureColumns(java.lang.String, java.lang.String, java.lang.String, java.lang.String)    */
specifier|public
name|ResultSet
name|getProcedureColumns
parameter_list|(
name|String
name|catalog
parameter_list|,
name|String
name|schemaPattern
parameter_list|,
name|String
name|procedureNamePattern
parameter_list|,
name|String
name|columnNamePattern
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getProcedureTerm()    */
specifier|public
name|String
name|getProcedureTerm
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getProcedures(java.lang.String, java.lang.String, java.lang.String)    */
specifier|public
name|ResultSet
name|getProcedures
parameter_list|(
name|String
name|catalog
parameter_list|,
name|String
name|schemaPattern
parameter_list|,
name|String
name|procedureNamePattern
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getResultSetHoldability()    */
specifier|public
name|int
name|getResultSetHoldability
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getRowIdLifetime()    */
specifier|public
name|RowIdLifetime
name|getRowIdLifetime
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getSQLKeywords()    */
specifier|public
name|String
name|getSQLKeywords
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getSQLStateType()    */
specifier|public
name|int
name|getSQLStateType
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getSchemaTerm()    */
specifier|public
name|String
name|getSchemaTerm
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getSchemas()    */
specifier|public
name|ResultSet
name|getSchemas
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getSchemas(java.lang.String, java.lang.String)    */
specifier|public
name|ResultSet
name|getSchemas
parameter_list|(
name|String
name|catalog
parameter_list|,
name|String
name|schemaPattern
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getSearchStringEscape()    */
specifier|public
name|String
name|getSearchStringEscape
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getStringFunctions()    */
specifier|public
name|String
name|getStringFunctions
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getSuperTables(java.lang.String, java.lang.String, java.lang.String)    */
specifier|public
name|ResultSet
name|getSuperTables
parameter_list|(
name|String
name|catalog
parameter_list|,
name|String
name|schemaPattern
parameter_list|,
name|String
name|tableNamePattern
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getSuperTypes(java.lang.String, java.lang.String, java.lang.String)    */
specifier|public
name|ResultSet
name|getSuperTypes
parameter_list|(
name|String
name|catalog
parameter_list|,
name|String
name|schemaPattern
parameter_list|,
name|String
name|typeNamePattern
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getSystemFunctions()    */
specifier|public
name|String
name|getSystemFunctions
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getTablePrivileges(java.lang.String, java.lang.String, java.lang.String)    */
specifier|public
name|ResultSet
name|getTablePrivileges
parameter_list|(
name|String
name|catalog
parameter_list|,
name|String
name|schemaPattern
parameter_list|,
name|String
name|tableNamePattern
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getTableTypes()    */
specifier|public
name|ResultSet
name|getTableTypes
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getTables(java.lang.String, java.lang.String, java.lang.String, java.lang.String[])    */
specifier|public
name|ResultSet
name|getTables
parameter_list|(
name|String
name|catalog
parameter_list|,
name|String
name|schemaPattern
parameter_list|,
name|String
name|tableNamePattern
parameter_list|,
name|String
index|[]
name|types
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getTimeDateFunctions()    */
specifier|public
name|String
name|getTimeDateFunctions
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getTypeInfo()    */
specifier|public
name|ResultSet
name|getTypeInfo
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getUDTs(java.lang.String, java.lang.String, java.lang.String, int[])    */
specifier|public
name|ResultSet
name|getUDTs
parameter_list|(
name|String
name|catalog
parameter_list|,
name|String
name|schemaPattern
parameter_list|,
name|String
name|typeNamePattern
parameter_list|,
name|int
index|[]
name|types
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getURL()    */
specifier|public
name|String
name|getURL
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getUserName()    */
specifier|public
name|String
name|getUserName
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#getVersionColumns(java.lang.String, java.lang.String, java.lang.String)    */
specifier|public
name|ResultSet
name|getVersionColumns
parameter_list|(
name|String
name|catalog
parameter_list|,
name|String
name|schema
parameter_list|,
name|String
name|table
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#insertsAreDetected(int)    */
specifier|public
name|boolean
name|insertsAreDetected
parameter_list|(
name|int
name|type
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#isCatalogAtStart()    */
specifier|public
name|boolean
name|isCatalogAtStart
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#isReadOnly()    */
specifier|public
name|boolean
name|isReadOnly
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#locatorsUpdateCopy()    */
specifier|public
name|boolean
name|locatorsUpdateCopy
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#nullPlusNonNullIsNull()    */
specifier|public
name|boolean
name|nullPlusNonNullIsNull
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#nullsAreSortedAtEnd()    */
specifier|public
name|boolean
name|nullsAreSortedAtEnd
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#nullsAreSortedAtStart()    */
specifier|public
name|boolean
name|nullsAreSortedAtStart
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#nullsAreSortedHigh()    */
specifier|public
name|boolean
name|nullsAreSortedHigh
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#nullsAreSortedLow()    */
specifier|public
name|boolean
name|nullsAreSortedLow
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#othersDeletesAreVisible(int)    */
specifier|public
name|boolean
name|othersDeletesAreVisible
parameter_list|(
name|int
name|type
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#othersInsertsAreVisible(int)    */
specifier|public
name|boolean
name|othersInsertsAreVisible
parameter_list|(
name|int
name|type
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#othersUpdatesAreVisible(int)    */
specifier|public
name|boolean
name|othersUpdatesAreVisible
parameter_list|(
name|int
name|type
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#ownDeletesAreVisible(int)    */
specifier|public
name|boolean
name|ownDeletesAreVisible
parameter_list|(
name|int
name|type
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#ownInsertsAreVisible(int)    */
specifier|public
name|boolean
name|ownInsertsAreVisible
parameter_list|(
name|int
name|type
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#ownUpdatesAreVisible(int)    */
specifier|public
name|boolean
name|ownUpdatesAreVisible
parameter_list|(
name|int
name|type
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#storesLowerCaseIdentifiers()    */
specifier|public
name|boolean
name|storesLowerCaseIdentifiers
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#storesLowerCaseQuotedIdentifiers()    */
specifier|public
name|boolean
name|storesLowerCaseQuotedIdentifiers
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#storesMixedCaseIdentifiers()    */
specifier|public
name|boolean
name|storesMixedCaseIdentifiers
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#storesMixedCaseQuotedIdentifiers()    */
specifier|public
name|boolean
name|storesMixedCaseQuotedIdentifiers
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#storesUpperCaseIdentifiers()    */
specifier|public
name|boolean
name|storesUpperCaseIdentifiers
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#storesUpperCaseQuotedIdentifiers()    */
specifier|public
name|boolean
name|storesUpperCaseQuotedIdentifiers
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsANSI92EntryLevelSQL()    */
specifier|public
name|boolean
name|supportsANSI92EntryLevelSQL
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsANSI92FullSQL()    */
specifier|public
name|boolean
name|supportsANSI92FullSQL
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsANSI92IntermediateSQL()    */
specifier|public
name|boolean
name|supportsANSI92IntermediateSQL
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsAlterTableWithAddColumn()    */
specifier|public
name|boolean
name|supportsAlterTableWithAddColumn
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsAlterTableWithDropColumn()    */
specifier|public
name|boolean
name|supportsAlterTableWithDropColumn
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsBatchUpdates()    */
specifier|public
name|boolean
name|supportsBatchUpdates
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsCatalogsInDataManipulation()    */
specifier|public
name|boolean
name|supportsCatalogsInDataManipulation
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsCatalogsInIndexDefinitions()    */
specifier|public
name|boolean
name|supportsCatalogsInIndexDefinitions
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsCatalogsInPrivilegeDefinitions()    */
specifier|public
name|boolean
name|supportsCatalogsInPrivilegeDefinitions
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsCatalogsInProcedureCalls()    */
specifier|public
name|boolean
name|supportsCatalogsInProcedureCalls
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsCatalogsInTableDefinitions()    */
specifier|public
name|boolean
name|supportsCatalogsInTableDefinitions
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsColumnAliasing()    */
specifier|public
name|boolean
name|supportsColumnAliasing
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsConvert()    */
specifier|public
name|boolean
name|supportsConvert
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsConvert(int, int)    */
specifier|public
name|boolean
name|supportsConvert
parameter_list|(
name|int
name|fromType
parameter_list|,
name|int
name|toType
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsCoreSQLGrammar()    */
specifier|public
name|boolean
name|supportsCoreSQLGrammar
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsCorrelatedSubqueries()    */
specifier|public
name|boolean
name|supportsCorrelatedSubqueries
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsDataDefinitionAndDataManipulationTransactions()    */
specifier|public
name|boolean
name|supportsDataDefinitionAndDataManipulationTransactions
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsDataManipulationTransactionsOnly()    */
specifier|public
name|boolean
name|supportsDataManipulationTransactionsOnly
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsDifferentTableCorrelationNames()    */
specifier|public
name|boolean
name|supportsDifferentTableCorrelationNames
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsExpressionsInOrderBy()    */
specifier|public
name|boolean
name|supportsExpressionsInOrderBy
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsExtendedSQLGrammar()    */
specifier|public
name|boolean
name|supportsExtendedSQLGrammar
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsFullOuterJoins()    */
specifier|public
name|boolean
name|supportsFullOuterJoins
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsGetGeneratedKeys()    */
specifier|public
name|boolean
name|supportsGetGeneratedKeys
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsGroupBy()    */
specifier|public
name|boolean
name|supportsGroupBy
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsGroupByBeyondSelect()    */
specifier|public
name|boolean
name|supportsGroupByBeyondSelect
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsGroupByUnrelated()    */
specifier|public
name|boolean
name|supportsGroupByUnrelated
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsIntegrityEnhancementFacility()    */
specifier|public
name|boolean
name|supportsIntegrityEnhancementFacility
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsLikeEscapeClause()    */
specifier|public
name|boolean
name|supportsLikeEscapeClause
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsLimitedOuterJoins()    */
specifier|public
name|boolean
name|supportsLimitedOuterJoins
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsMinimumSQLGrammar()    */
specifier|public
name|boolean
name|supportsMinimumSQLGrammar
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsMixedCaseIdentifiers()    */
specifier|public
name|boolean
name|supportsMixedCaseIdentifiers
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsMixedCaseQuotedIdentifiers()    */
specifier|public
name|boolean
name|supportsMixedCaseQuotedIdentifiers
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsMultipleOpenResults()    */
specifier|public
name|boolean
name|supportsMultipleOpenResults
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsMultipleResultSets()    */
specifier|public
name|boolean
name|supportsMultipleResultSets
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsMultipleTransactions()    */
specifier|public
name|boolean
name|supportsMultipleTransactions
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsNamedParameters()    */
specifier|public
name|boolean
name|supportsNamedParameters
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsNonNullableColumns()    */
specifier|public
name|boolean
name|supportsNonNullableColumns
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsOpenCursorsAcrossCommit()    */
specifier|public
name|boolean
name|supportsOpenCursorsAcrossCommit
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsOpenCursorsAcrossRollback()    */
specifier|public
name|boolean
name|supportsOpenCursorsAcrossRollback
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsOpenStatementsAcrossCommit()    */
specifier|public
name|boolean
name|supportsOpenStatementsAcrossCommit
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsOpenStatementsAcrossRollback()    */
specifier|public
name|boolean
name|supportsOpenStatementsAcrossRollback
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsOrderByUnrelated()    */
specifier|public
name|boolean
name|supportsOrderByUnrelated
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsOuterJoins()    */
specifier|public
name|boolean
name|supportsOuterJoins
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsPositionedDelete()    */
specifier|public
name|boolean
name|supportsPositionedDelete
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsPositionedUpdate()    */
specifier|public
name|boolean
name|supportsPositionedUpdate
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsResultSetConcurrency(int, int)    */
specifier|public
name|boolean
name|supportsResultSetConcurrency
parameter_list|(
name|int
name|type
parameter_list|,
name|int
name|concurrency
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsResultSetHoldability(int)    */
specifier|public
name|boolean
name|supportsResultSetHoldability
parameter_list|(
name|int
name|holdability
parameter_list|)
throws|throws
name|SQLException
block|{
return|return
literal|false
return|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsResultSetType(int)    */
specifier|public
name|boolean
name|supportsResultSetType
parameter_list|(
name|int
name|type
parameter_list|)
throws|throws
name|SQLException
block|{
return|return
literal|true
return|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsSavepoints()    */
specifier|public
name|boolean
name|supportsSavepoints
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsSchemasInDataManipulation()    */
specifier|public
name|boolean
name|supportsSchemasInDataManipulation
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsSchemasInIndexDefinitions()    */
specifier|public
name|boolean
name|supportsSchemasInIndexDefinitions
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsSchemasInPrivilegeDefinitions()    */
specifier|public
name|boolean
name|supportsSchemasInPrivilegeDefinitions
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsSchemasInProcedureCalls()    */
specifier|public
name|boolean
name|supportsSchemasInProcedureCalls
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsSchemasInTableDefinitions()    */
specifier|public
name|boolean
name|supportsSchemasInTableDefinitions
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsSelectForUpdate()    */
specifier|public
name|boolean
name|supportsSelectForUpdate
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsStatementPooling()    */
specifier|public
name|boolean
name|supportsStatementPooling
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsStoredFunctionsUsingCallSyntax()    */
specifier|public
name|boolean
name|supportsStoredFunctionsUsingCallSyntax
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsStoredProcedures()    */
specifier|public
name|boolean
name|supportsStoredProcedures
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsSubqueriesInComparisons()    */
specifier|public
name|boolean
name|supportsSubqueriesInComparisons
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsSubqueriesInExists()    */
specifier|public
name|boolean
name|supportsSubqueriesInExists
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsSubqueriesInIns()    */
specifier|public
name|boolean
name|supportsSubqueriesInIns
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsSubqueriesInQuantifieds()    */
specifier|public
name|boolean
name|supportsSubqueriesInQuantifieds
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsTableCorrelationNames()    */
specifier|public
name|boolean
name|supportsTableCorrelationNames
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsTransactionIsolationLevel(int)    */
specifier|public
name|boolean
name|supportsTransactionIsolationLevel
parameter_list|(
name|int
name|level
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsTransactions()    */
specifier|public
name|boolean
name|supportsTransactions
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsUnion()    */
specifier|public
name|boolean
name|supportsUnion
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#supportsUnionAll()    */
specifier|public
name|boolean
name|supportsUnionAll
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#updatesAreDetected(int)    */
specifier|public
name|boolean
name|updatesAreDetected
parameter_list|(
name|int
name|type
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#usesLocalFilePerTable()    */
specifier|public
name|boolean
name|usesLocalFilePerTable
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.DatabaseMetaData#usesLocalFiles()    */
specifier|public
name|boolean
name|usesLocalFiles
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)    */
specifier|public
name|boolean
name|isWrapperFor
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|iface
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.Wrapper#unwrap(java.lang.Class)    */
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|unwrap
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|iface
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

