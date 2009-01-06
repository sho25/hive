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
name|SQLException
import|;
end_import

begin_class
specifier|public
class|class
name|HiveResultSetMetaData
implements|implements
name|java
operator|.
name|sql
operator|.
name|ResultSetMetaData
block|{
comment|/* (non-Javadoc)    * @see java.sql.ResultSetMetaData#getCatalogName(int)    */
specifier|public
name|String
name|getCatalogName
parameter_list|(
name|int
name|column
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSetMetaData#getColumnClassName(int)    */
specifier|public
name|String
name|getColumnClassName
parameter_list|(
name|int
name|column
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSetMetaData#getColumnCount()    */
specifier|public
name|int
name|getColumnCount
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSetMetaData#getColumnDisplaySize(int)    */
specifier|public
name|int
name|getColumnDisplaySize
parameter_list|(
name|int
name|column
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSetMetaData#getColumnLabel(int)    */
specifier|public
name|String
name|getColumnLabel
parameter_list|(
name|int
name|column
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSetMetaData#getColumnName(int)    */
specifier|public
name|String
name|getColumnName
parameter_list|(
name|int
name|column
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSetMetaData#getColumnType(int)    */
specifier|public
name|int
name|getColumnType
parameter_list|(
name|int
name|column
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSetMetaData#getColumnTypeName(int)    */
specifier|public
name|String
name|getColumnTypeName
parameter_list|(
name|int
name|column
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSetMetaData#getPrecision(int)    */
specifier|public
name|int
name|getPrecision
parameter_list|(
name|int
name|column
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSetMetaData#getScale(int)    */
specifier|public
name|int
name|getScale
parameter_list|(
name|int
name|column
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSetMetaData#getSchemaName(int)    */
specifier|public
name|String
name|getSchemaName
parameter_list|(
name|int
name|column
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSetMetaData#getTableName(int)    */
specifier|public
name|String
name|getTableName
parameter_list|(
name|int
name|column
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSetMetaData#isAutoIncrement(int)    */
specifier|public
name|boolean
name|isAutoIncrement
parameter_list|(
name|int
name|column
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSetMetaData#isCaseSensitive(int)    */
specifier|public
name|boolean
name|isCaseSensitive
parameter_list|(
name|int
name|column
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSetMetaData#isCurrency(int)    */
specifier|public
name|boolean
name|isCurrency
parameter_list|(
name|int
name|column
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSetMetaData#isDefinitelyWritable(int)    */
specifier|public
name|boolean
name|isDefinitelyWritable
parameter_list|(
name|int
name|column
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSetMetaData#isNullable(int)    */
specifier|public
name|int
name|isNullable
parameter_list|(
name|int
name|column
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSetMetaData#isReadOnly(int)    */
specifier|public
name|boolean
name|isReadOnly
parameter_list|(
name|int
name|column
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSetMetaData#isSearchable(int)    */
specifier|public
name|boolean
name|isSearchable
parameter_list|(
name|int
name|column
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSetMetaData#isSigned(int)    */
specifier|public
name|boolean
name|isSigned
parameter_list|(
name|int
name|column
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSetMetaData#isWritable(int)    */
specifier|public
name|boolean
name|isWritable
parameter_list|(
name|int
name|column
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

