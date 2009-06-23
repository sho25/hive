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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Reader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigDecimal
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Array
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Blob
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Clob
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|NClob
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Ref
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|ResultSetMetaData
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|RowId
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
name|SQLWarning
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLXML
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
name|java
operator|.
name|sql
operator|.
name|Time
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
import|;
end_import

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
name|Calendar
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
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
name|serde2
operator|.
name|dynamic_type
operator|.
name|DynamicSerDe
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|serde
operator|.
name|Constants
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
name|conf
operator|.
name|Configuration
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
name|service
operator|.
name|HiveInterface
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
name|io
operator|.
name|BytesWritable
import|;
end_import

begin_class
specifier|public
class|class
name|HiveResultSet
implements|implements
name|java
operator|.
name|sql
operator|.
name|ResultSet
block|{
name|HiveInterface
name|client
decl_stmt|;
name|ArrayList
argument_list|<
name|?
argument_list|>
name|row
decl_stmt|;
name|DynamicSerDe
name|ds
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columnTypes
decl_stmt|;
comment|/**    *    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|HiveResultSet
parameter_list|(
name|HiveInterface
name|client
parameter_list|)
block|{
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|row
operator|=
operator|new
name|ArrayList
argument_list|()
expr_stmt|;
name|initDynamicSerde
argument_list|()
expr_stmt|;
block|}
comment|/**    * Instantiate the dynamic serde used to deserialize the result row    */
specifier|public
name|void
name|initDynamicSerde
parameter_list|()
block|{
try|try
block|{
name|String
name|fullSchema
init|=
name|client
operator|.
name|getSchema
argument_list|()
decl_stmt|;
name|String
index|[]
name|schemaParts
init|=
name|fullSchema
operator|.
name|split
argument_list|(
literal|"#"
argument_list|)
decl_stmt|;
if|if
condition|(
name|schemaParts
operator|.
name|length
operator|>
literal|2
condition|)
block|{
name|columnNames
operator|=
name|Arrays
operator|.
name|asList
argument_list|(
name|schemaParts
index|[
literal|1
index|]
operator|.
name|split
argument_list|(
literal|","
argument_list|)
argument_list|)
expr_stmt|;
name|columnTypes
operator|=
name|Arrays
operator|.
name|asList
argument_list|(
name|schemaParts
index|[
literal|2
index|]
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ds
operator|=
operator|new
name|DynamicSerDe
argument_list|()
expr_stmt|;
name|Properties
name|dsp
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|dsp
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|serde2
operator|.
name|thrift
operator|.
name|TCTLSeparatedProtocol
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|dsp
operator|.
name|setProperty
argument_list|(
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
name|api
operator|.
name|Constants
operator|.
name|META_TABLE_NAME
argument_list|,
literal|"result"
argument_list|)
expr_stmt|;
name|dsp
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_DDL
argument_list|,
name|schemaParts
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|dsp
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_LIB
argument_list|,
name|ds
operator|.
name|getClass
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|dsp
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|FIELD_DELIM
argument_list|,
literal|"9"
argument_list|)
expr_stmt|;
name|ds
operator|.
name|initialize
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|,
name|dsp
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|ex
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// TODO: Decide what to do here.
block|}
block|}
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#absolute(int)    */
specifier|public
name|boolean
name|absolute
parameter_list|(
name|int
name|row
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#afterLast()    */
specifier|public
name|void
name|afterLast
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#beforeFirst()    */
specifier|public
name|void
name|beforeFirst
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#cancelRowUpdates()    */
specifier|public
name|void
name|cancelRowUpdates
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#clearWarnings()    */
specifier|public
name|void
name|clearWarnings
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#close()    */
specifier|public
name|void
name|close
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#deleteRow()    */
specifier|public
name|void
name|deleteRow
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#findColumn(java.lang.String)    */
specifier|public
name|int
name|findColumn
parameter_list|(
name|String
name|columnName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#first()    */
specifier|public
name|boolean
name|first
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getArray(int)    */
specifier|public
name|Array
name|getArray
parameter_list|(
name|int
name|i
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getArray(java.lang.String)    */
specifier|public
name|Array
name|getArray
parameter_list|(
name|String
name|colName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getAsciiStream(int)    */
specifier|public
name|InputStream
name|getAsciiStream
parameter_list|(
name|int
name|columnIndex
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getAsciiStream(java.lang.String)    */
specifier|public
name|InputStream
name|getAsciiStream
parameter_list|(
name|String
name|columnName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getBigDecimal(int)    */
specifier|public
name|BigDecimal
name|getBigDecimal
parameter_list|(
name|int
name|columnIndex
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getBigDecimal(java.lang.String)    */
specifier|public
name|BigDecimal
name|getBigDecimal
parameter_list|(
name|String
name|columnName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getBigDecimal(int, int)    */
specifier|public
name|BigDecimal
name|getBigDecimal
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|int
name|scale
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getBigDecimal(java.lang.String, int)    */
specifier|public
name|BigDecimal
name|getBigDecimal
parameter_list|(
name|String
name|columnName
parameter_list|,
name|int
name|scale
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getBinaryStream(int)    */
specifier|public
name|InputStream
name|getBinaryStream
parameter_list|(
name|int
name|columnIndex
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getBinaryStream(java.lang.String)    */
specifier|public
name|InputStream
name|getBinaryStream
parameter_list|(
name|String
name|columnName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getBlob(int)    */
specifier|public
name|Blob
name|getBlob
parameter_list|(
name|int
name|i
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getBlob(java.lang.String)    */
specifier|public
name|Blob
name|getBlob
parameter_list|(
name|String
name|colName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getBoolean(int)    */
specifier|public
name|boolean
name|getBoolean
parameter_list|(
name|int
name|columnIndex
parameter_list|)
throws|throws
name|SQLException
block|{
name|Object
name|obj
init|=
name|row
operator|.
name|get
argument_list|(
name|columnIndex
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|Number
operator|.
name|class
operator|.
name|isInstance
argument_list|(
name|obj
argument_list|)
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|obj
operator|)
operator|.
name|intValue
argument_list|()
operator|!=
literal|0
return|;
block|}
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Cannot convert column "
operator|+
name|columnIndex
operator|+
literal|" to boolean"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getBoolean(java.lang.String)    */
specifier|public
name|boolean
name|getBoolean
parameter_list|(
name|String
name|columnName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getByte(int)    */
specifier|public
name|byte
name|getByte
parameter_list|(
name|int
name|columnIndex
parameter_list|)
throws|throws
name|SQLException
block|{
name|Object
name|obj
init|=
name|row
operator|.
name|get
argument_list|(
name|columnIndex
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|Number
operator|.
name|class
operator|.
name|isInstance
argument_list|(
name|obj
argument_list|)
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|obj
operator|)
operator|.
name|byteValue
argument_list|()
return|;
block|}
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Cannot convert column "
operator|+
name|columnIndex
operator|+
literal|" to byte"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getByte(java.lang.String)    */
specifier|public
name|byte
name|getByte
parameter_list|(
name|String
name|columnName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getBytes(int)    */
specifier|public
name|byte
index|[]
name|getBytes
parameter_list|(
name|int
name|columnIndex
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getBytes(java.lang.String)    */
specifier|public
name|byte
index|[]
name|getBytes
parameter_list|(
name|String
name|columnName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getCharacterStream(int)    */
specifier|public
name|Reader
name|getCharacterStream
parameter_list|(
name|int
name|columnIndex
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getCharacterStream(java.lang.String)    */
specifier|public
name|Reader
name|getCharacterStream
parameter_list|(
name|String
name|columnName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getClob(int)    */
specifier|public
name|Clob
name|getClob
parameter_list|(
name|int
name|i
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getClob(java.lang.String)    */
specifier|public
name|Clob
name|getClob
parameter_list|(
name|String
name|colName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getConcurrency()    */
specifier|public
name|int
name|getConcurrency
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getCursorName()    */
specifier|public
name|String
name|getCursorName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getDate(int)    */
specifier|public
name|Date
name|getDate
parameter_list|(
name|int
name|columnIndex
parameter_list|)
throws|throws
name|SQLException
block|{
try|try
block|{
return|return
name|Date
operator|.
name|valueOf
argument_list|(
operator|(
name|String
operator|)
name|row
operator|.
name|get
argument_list|(
name|columnIndex
operator|-
literal|1
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Cannot convert column "
operator|+
name|columnIndex
operator|+
literal|" to date: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getDate(java.lang.String)    */
specifier|public
name|Date
name|getDate
parameter_list|(
name|String
name|columnName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getDate(int, java.util.Calendar)    */
specifier|public
name|Date
name|getDate
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Calendar
name|cal
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getDate(java.lang.String, java.util.Calendar)    */
specifier|public
name|Date
name|getDate
parameter_list|(
name|String
name|columnName
parameter_list|,
name|Calendar
name|cal
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getDouble(int)    */
specifier|public
name|double
name|getDouble
parameter_list|(
name|int
name|columnIndex
parameter_list|)
throws|throws
name|SQLException
block|{
try|try
block|{
name|Object
name|obj
init|=
name|row
operator|.
name|get
argument_list|(
name|columnIndex
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|Number
operator|.
name|class
operator|.
name|isInstance
argument_list|(
name|obj
argument_list|)
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|obj
operator|)
operator|.
name|doubleValue
argument_list|()
return|;
block|}
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Illegal conversion"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Cannot convert column "
operator|+
name|columnIndex
operator|+
literal|" to double: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getDouble(java.lang.String)    */
specifier|public
name|double
name|getDouble
parameter_list|(
name|String
name|columnName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getFetchDirection()    */
specifier|public
name|int
name|getFetchDirection
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getFetchSize()    */
specifier|public
name|int
name|getFetchSize
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getFloat(int)    */
specifier|public
name|float
name|getFloat
parameter_list|(
name|int
name|columnIndex
parameter_list|)
throws|throws
name|SQLException
block|{
try|try
block|{
name|Object
name|obj
init|=
name|row
operator|.
name|get
argument_list|(
name|columnIndex
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|Number
operator|.
name|class
operator|.
name|isInstance
argument_list|(
name|obj
argument_list|)
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|obj
operator|)
operator|.
name|floatValue
argument_list|()
return|;
block|}
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Illegal conversion"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Cannot convert column "
operator|+
name|columnIndex
operator|+
literal|" to float: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getFloat(java.lang.String)    */
specifier|public
name|float
name|getFloat
parameter_list|(
name|String
name|columnName
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getHoldability()    */
specifier|public
name|int
name|getHoldability
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getInt(int)    */
specifier|public
name|int
name|getInt
parameter_list|(
name|int
name|columnIndex
parameter_list|)
throws|throws
name|SQLException
block|{
try|try
block|{
name|Object
name|obj
init|=
name|row
operator|.
name|get
argument_list|(
name|columnIndex
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|Number
operator|.
name|class
operator|.
name|isInstance
argument_list|(
name|obj
argument_list|)
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|obj
operator|)
operator|.
name|intValue
argument_list|()
return|;
block|}
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Illegal conversion"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Cannot convert column "
operator|+
name|columnIndex
operator|+
literal|" to integer"
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getInt(java.lang.String)    */
specifier|public
name|int
name|getInt
parameter_list|(
name|String
name|columnName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getLong(int)    */
specifier|public
name|long
name|getLong
parameter_list|(
name|int
name|columnIndex
parameter_list|)
throws|throws
name|SQLException
block|{
try|try
block|{
name|Object
name|obj
init|=
name|row
operator|.
name|get
argument_list|(
name|columnIndex
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|Number
operator|.
name|class
operator|.
name|isInstance
argument_list|(
name|obj
argument_list|)
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|obj
operator|)
operator|.
name|longValue
argument_list|()
return|;
block|}
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Illegal conversion"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Cannot convert column "
operator|+
name|columnIndex
operator|+
literal|" to long: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getLong(java.lang.String)    */
specifier|public
name|long
name|getLong
parameter_list|(
name|String
name|columnName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getMetaData()    */
specifier|public
name|ResultSetMetaData
name|getMetaData
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
operator|new
name|HiveResultSetMetaData
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|)
return|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getNCharacterStream(int)    */
specifier|public
name|Reader
name|getNCharacterStream
parameter_list|(
name|int
name|arg0
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getNCharacterStream(java.lang.String)    */
specifier|public
name|Reader
name|getNCharacterStream
parameter_list|(
name|String
name|arg0
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getNClob(int)    */
specifier|public
name|NClob
name|getNClob
parameter_list|(
name|int
name|arg0
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getNClob(java.lang.String)    */
specifier|public
name|NClob
name|getNClob
parameter_list|(
name|String
name|columnLabel
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getNString(int)    */
specifier|public
name|String
name|getNString
parameter_list|(
name|int
name|columnIndex
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getNString(java.lang.String)    */
specifier|public
name|String
name|getNString
parameter_list|(
name|String
name|columnLabel
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getObject(int)    */
specifier|public
name|Object
name|getObject
parameter_list|(
name|int
name|columnIndex
parameter_list|)
throws|throws
name|SQLException
block|{
try|try
block|{
return|return
name|row
operator|.
name|get
argument_list|(
name|columnIndex
operator|-
literal|1
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getObject(java.lang.String)    */
specifier|public
name|Object
name|getObject
parameter_list|(
name|String
name|columnName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getObject(int, java.util.Map)    */
specifier|public
name|Object
name|getObject
parameter_list|(
name|int
name|i
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|map
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getObject(java.lang.String, java.util.Map)    */
specifier|public
name|Object
name|getObject
parameter_list|(
name|String
name|colName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|map
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getRef(int)    */
specifier|public
name|Ref
name|getRef
parameter_list|(
name|int
name|i
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getRef(java.lang.String)    */
specifier|public
name|Ref
name|getRef
parameter_list|(
name|String
name|colName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getRow()    */
specifier|public
name|int
name|getRow
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getRowId(int)    */
specifier|public
name|RowId
name|getRowId
parameter_list|(
name|int
name|columnIndex
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getRowId(java.lang.String)    */
specifier|public
name|RowId
name|getRowId
parameter_list|(
name|String
name|columnLabel
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getSQLXML(int)    */
specifier|public
name|SQLXML
name|getSQLXML
parameter_list|(
name|int
name|columnIndex
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getSQLXML(java.lang.String)    */
specifier|public
name|SQLXML
name|getSQLXML
parameter_list|(
name|String
name|columnLabel
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getShort(int)    */
specifier|public
name|short
name|getShort
parameter_list|(
name|int
name|columnIndex
parameter_list|)
throws|throws
name|SQLException
block|{
try|try
block|{
name|Object
name|obj
init|=
name|row
operator|.
name|get
argument_list|(
name|columnIndex
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|Number
operator|.
name|class
operator|.
name|isInstance
argument_list|(
name|obj
argument_list|)
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|obj
operator|)
operator|.
name|shortValue
argument_list|()
return|;
block|}
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Illegal conversion"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Cannot convert column "
operator|+
name|columnIndex
operator|+
literal|" to short: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getShort(java.lang.String)    */
specifier|public
name|short
name|getShort
parameter_list|(
name|String
name|columnName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getStatement()    */
specifier|public
name|Statement
name|getStatement
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
comment|/**    * @param columnIndex - the first column is 1, the second is 2, ...    * @see java.sql.ResultSet#getString(int)    */
specifier|public
name|String
name|getString
parameter_list|(
name|int
name|columnIndex
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// Column index starts from 1, not 0.
return|return
name|row
operator|.
name|get
argument_list|(
name|columnIndex
operator|-
literal|1
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getString(java.lang.String)    */
specifier|public
name|String
name|getString
parameter_list|(
name|String
name|columnName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getTime(int)    */
specifier|public
name|Time
name|getTime
parameter_list|(
name|int
name|columnIndex
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getTime(java.lang.String)    */
specifier|public
name|Time
name|getTime
parameter_list|(
name|String
name|columnName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getTime(int, java.util.Calendar)    */
specifier|public
name|Time
name|getTime
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Calendar
name|cal
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getTime(java.lang.String, java.util.Calendar)    */
specifier|public
name|Time
name|getTime
parameter_list|(
name|String
name|columnName
parameter_list|,
name|Calendar
name|cal
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getTimestamp(int)    */
specifier|public
name|Timestamp
name|getTimestamp
parameter_list|(
name|int
name|columnIndex
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getTimestamp(java.lang.String)    */
specifier|public
name|Timestamp
name|getTimestamp
parameter_list|(
name|String
name|columnName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getTimestamp(int, java.util.Calendar)    */
specifier|public
name|Timestamp
name|getTimestamp
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Calendar
name|cal
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getTimestamp(java.lang.String, java.util.Calendar)    */
specifier|public
name|Timestamp
name|getTimestamp
parameter_list|(
name|String
name|columnName
parameter_list|,
name|Calendar
name|cal
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getType()    */
specifier|public
name|int
name|getType
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getURL(int)    */
specifier|public
name|URL
name|getURL
parameter_list|(
name|int
name|columnIndex
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getURL(java.lang.String)    */
specifier|public
name|URL
name|getURL
parameter_list|(
name|String
name|columnName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getUnicodeStream(int)    */
specifier|public
name|InputStream
name|getUnicodeStream
parameter_list|(
name|int
name|columnIndex
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getUnicodeStream(java.lang.String)    */
specifier|public
name|InputStream
name|getUnicodeStream
parameter_list|(
name|String
name|columnName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#getWarnings()    */
specifier|public
name|SQLWarning
name|getWarnings
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#insertRow()    */
specifier|public
name|void
name|insertRow
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#isAfterLast()    */
specifier|public
name|boolean
name|isAfterLast
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#isBeforeFirst()    */
specifier|public
name|boolean
name|isBeforeFirst
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#isClosed()    */
specifier|public
name|boolean
name|isClosed
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#isFirst()    */
specifier|public
name|boolean
name|isFirst
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#isLast()    */
specifier|public
name|boolean
name|isLast
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#last()    */
specifier|public
name|boolean
name|last
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#moveToCurrentRow()    */
specifier|public
name|void
name|moveToCurrentRow
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#moveToInsertRow()    */
specifier|public
name|void
name|moveToInsertRow
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
comment|/**    * Moves the cursor down one row from its current position.    *    * @see java.sql.ResultSet#next()    * @throws SQLException if a database access error occurs.    */
specifier|public
name|boolean
name|next
parameter_list|()
throws|throws
name|SQLException
block|{
name|String
name|row_str
init|=
literal|""
decl_stmt|;
try|try
block|{
name|row_str
operator|=
operator|(
name|String
operator|)
name|client
operator|.
name|fetchOne
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|row_str
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|Object
name|o
init|=
name|ds
operator|.
name|deserialize
argument_list|(
operator|new
name|BytesWritable
argument_list|(
name|row_str
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|row
operator|=
operator|(
name|ArrayList
argument_list|<
name|?
argument_list|>
operator|)
name|o
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|ex
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Error retrieving next row"
argument_list|)
throw|;
block|}
comment|// NOTE: fetchOne dosn't throw new SQLException("Method not supported").
return|return
operator|!
name|row_str
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
return|;
block|}
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#previous()    */
specifier|public
name|boolean
name|previous
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#refreshRow()    */
specifier|public
name|void
name|refreshRow
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#relative(int)    */
specifier|public
name|boolean
name|relative
parameter_list|(
name|int
name|rows
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#rowDeleted()    */
specifier|public
name|boolean
name|rowDeleted
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#rowInserted()    */
specifier|public
name|boolean
name|rowInserted
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#rowUpdated()    */
specifier|public
name|boolean
name|rowUpdated
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#setFetchDirection(int)    */
specifier|public
name|void
name|setFetchDirection
parameter_list|(
name|int
name|direction
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#setFetchSize(int)    */
specifier|public
name|void
name|setFetchSize
parameter_list|(
name|int
name|rows
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateArray(int, java.sql.Array)    */
specifier|public
name|void
name|updateArray
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Array
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateArray(java.lang.String, java.sql.Array)    */
specifier|public
name|void
name|updateArray
parameter_list|(
name|String
name|columnName
parameter_list|,
name|Array
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream)    */
specifier|public
name|void
name|updateAsciiStream
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|InputStream
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateAsciiStream(java.lang.String, java.io.InputStream)    */
specifier|public
name|void
name|updateAsciiStream
parameter_list|(
name|String
name|columnLabel
parameter_list|,
name|InputStream
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream, int)    */
specifier|public
name|void
name|updateAsciiStream
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|InputStream
name|x
parameter_list|,
name|int
name|length
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateAsciiStream(java.lang.String, java.io.InputStream, int)    */
specifier|public
name|void
name|updateAsciiStream
parameter_list|(
name|String
name|columnName
parameter_list|,
name|InputStream
name|x
parameter_list|,
name|int
name|length
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream, long)    */
specifier|public
name|void
name|updateAsciiStream
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|InputStream
name|x
parameter_list|,
name|long
name|length
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateAsciiStream(java.lang.String, java.io.InputStream, long)    */
specifier|public
name|void
name|updateAsciiStream
parameter_list|(
name|String
name|columnLabel
parameter_list|,
name|InputStream
name|x
parameter_list|,
name|long
name|length
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateBigDecimal(int, java.math.BigDecimal)    */
specifier|public
name|void
name|updateBigDecimal
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|BigDecimal
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateBigDecimal(java.lang.String, java.math.BigDecimal)    */
specifier|public
name|void
name|updateBigDecimal
parameter_list|(
name|String
name|columnName
parameter_list|,
name|BigDecimal
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream)    */
specifier|public
name|void
name|updateBinaryStream
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|InputStream
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateBinaryStream(java.lang.String, java.io.InputStream)    */
specifier|public
name|void
name|updateBinaryStream
parameter_list|(
name|String
name|columnLabel
parameter_list|,
name|InputStream
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream, int)    */
specifier|public
name|void
name|updateBinaryStream
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|InputStream
name|x
parameter_list|,
name|int
name|length
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateBinaryStream(java.lang.String, java.io.InputStream, int)    */
specifier|public
name|void
name|updateBinaryStream
parameter_list|(
name|String
name|columnName
parameter_list|,
name|InputStream
name|x
parameter_list|,
name|int
name|length
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream, long)    */
specifier|public
name|void
name|updateBinaryStream
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|InputStream
name|x
parameter_list|,
name|long
name|length
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateBinaryStream(java.lang.String, java.io.InputStream, long)    */
specifier|public
name|void
name|updateBinaryStream
parameter_list|(
name|String
name|columnLabel
parameter_list|,
name|InputStream
name|x
parameter_list|,
name|long
name|length
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateBlob(int, java.sql.Blob)    */
specifier|public
name|void
name|updateBlob
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Blob
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateBlob(java.lang.String, java.sql.Blob)    */
specifier|public
name|void
name|updateBlob
parameter_list|(
name|String
name|columnName
parameter_list|,
name|Blob
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateBlob(int, java.io.InputStream)    */
specifier|public
name|void
name|updateBlob
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|InputStream
name|inputStream
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateBlob(java.lang.String, java.io.InputStream)    */
specifier|public
name|void
name|updateBlob
parameter_list|(
name|String
name|columnLabel
parameter_list|,
name|InputStream
name|inputStream
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateBlob(int, java.io.InputStream, long)    */
specifier|public
name|void
name|updateBlob
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|InputStream
name|inputStream
parameter_list|,
name|long
name|length
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateBlob(java.lang.String, java.io.InputStream, long)    */
specifier|public
name|void
name|updateBlob
parameter_list|(
name|String
name|columnLabel
parameter_list|,
name|InputStream
name|inputStream
parameter_list|,
name|long
name|length
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateBoolean(int, boolean)    */
specifier|public
name|void
name|updateBoolean
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|boolean
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateBoolean(java.lang.String, boolean)    */
specifier|public
name|void
name|updateBoolean
parameter_list|(
name|String
name|columnName
parameter_list|,
name|boolean
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateByte(int, byte)    */
specifier|public
name|void
name|updateByte
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|byte
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateByte(java.lang.String, byte)    */
specifier|public
name|void
name|updateByte
parameter_list|(
name|String
name|columnName
parameter_list|,
name|byte
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateBytes(int, byte[])    */
specifier|public
name|void
name|updateBytes
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|byte
index|[]
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateBytes(java.lang.String, byte[])    */
specifier|public
name|void
name|updateBytes
parameter_list|(
name|String
name|columnName
parameter_list|,
name|byte
index|[]
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader)    */
specifier|public
name|void
name|updateCharacterStream
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Reader
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateCharacterStream(java.lang.String, java.io.Reader)    */
specifier|public
name|void
name|updateCharacterStream
parameter_list|(
name|String
name|columnLabel
parameter_list|,
name|Reader
name|reader
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader, int)    */
specifier|public
name|void
name|updateCharacterStream
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Reader
name|x
parameter_list|,
name|int
name|length
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateCharacterStream(java.lang.String, java.io.Reader, int)    */
specifier|public
name|void
name|updateCharacterStream
parameter_list|(
name|String
name|columnName
parameter_list|,
name|Reader
name|reader
parameter_list|,
name|int
name|length
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader, long)    */
specifier|public
name|void
name|updateCharacterStream
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Reader
name|x
parameter_list|,
name|long
name|length
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateCharacterStream(java.lang.String, java.io.Reader, long)    */
specifier|public
name|void
name|updateCharacterStream
parameter_list|(
name|String
name|columnLabel
parameter_list|,
name|Reader
name|reader
parameter_list|,
name|long
name|length
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateClob(int, java.sql.Clob)    */
specifier|public
name|void
name|updateClob
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Clob
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateClob(java.lang.String, java.sql.Clob)    */
specifier|public
name|void
name|updateClob
parameter_list|(
name|String
name|columnName
parameter_list|,
name|Clob
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateClob(int, java.io.Reader)    */
specifier|public
name|void
name|updateClob
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Reader
name|reader
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateClob(java.lang.String, java.io.Reader)    */
specifier|public
name|void
name|updateClob
parameter_list|(
name|String
name|columnLabel
parameter_list|,
name|Reader
name|reader
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateClob(int, java.io.Reader, long)    */
specifier|public
name|void
name|updateClob
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Reader
name|reader
parameter_list|,
name|long
name|length
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateClob(java.lang.String, java.io.Reader, long)    */
specifier|public
name|void
name|updateClob
parameter_list|(
name|String
name|columnLabel
parameter_list|,
name|Reader
name|reader
parameter_list|,
name|long
name|length
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateDate(int, java.sql.Date)    */
specifier|public
name|void
name|updateDate
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Date
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateDate(java.lang.String, java.sql.Date)    */
specifier|public
name|void
name|updateDate
parameter_list|(
name|String
name|columnName
parameter_list|,
name|Date
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateDouble(int, double)    */
specifier|public
name|void
name|updateDouble
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|double
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateDouble(java.lang.String, double)    */
specifier|public
name|void
name|updateDouble
parameter_list|(
name|String
name|columnName
parameter_list|,
name|double
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateFloat(int, float)    */
specifier|public
name|void
name|updateFloat
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|float
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateFloat(java.lang.String, float)    */
specifier|public
name|void
name|updateFloat
parameter_list|(
name|String
name|columnName
parameter_list|,
name|float
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateInt(int, int)    */
specifier|public
name|void
name|updateInt
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|int
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateInt(java.lang.String, int)    */
specifier|public
name|void
name|updateInt
parameter_list|(
name|String
name|columnName
parameter_list|,
name|int
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateLong(int, long)    */
specifier|public
name|void
name|updateLong
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|long
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateLong(java.lang.String, long)    */
specifier|public
name|void
name|updateLong
parameter_list|(
name|String
name|columnName
parameter_list|,
name|long
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateNCharacterStream(int, java.io.Reader)    */
specifier|public
name|void
name|updateNCharacterStream
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Reader
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateNCharacterStream(java.lang.String, java.io.Reader)    */
specifier|public
name|void
name|updateNCharacterStream
parameter_list|(
name|String
name|columnLabel
parameter_list|,
name|Reader
name|reader
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateNCharacterStream(int, java.io.Reader, long)    */
specifier|public
name|void
name|updateNCharacterStream
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Reader
name|x
parameter_list|,
name|long
name|length
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateNCharacterStream(java.lang.String, java.io.Reader, long)    */
specifier|public
name|void
name|updateNCharacterStream
parameter_list|(
name|String
name|columnLabel
parameter_list|,
name|Reader
name|reader
parameter_list|,
name|long
name|length
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateNClob(int, java.sql.NClob)    */
specifier|public
name|void
name|updateNClob
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|NClob
name|clob
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateNClob(java.lang.String, java.sql.NClob)    */
specifier|public
name|void
name|updateNClob
parameter_list|(
name|String
name|columnLabel
parameter_list|,
name|NClob
name|clob
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateNClob(int, java.io.Reader)    */
specifier|public
name|void
name|updateNClob
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Reader
name|reader
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateNClob(java.lang.String, java.io.Reader)    */
specifier|public
name|void
name|updateNClob
parameter_list|(
name|String
name|columnLabel
parameter_list|,
name|Reader
name|reader
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateNClob(int, java.io.Reader, long)    */
specifier|public
name|void
name|updateNClob
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Reader
name|reader
parameter_list|,
name|long
name|length
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateNClob(java.lang.String, java.io.Reader, long)    */
specifier|public
name|void
name|updateNClob
parameter_list|(
name|String
name|columnLabel
parameter_list|,
name|Reader
name|reader
parameter_list|,
name|long
name|length
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateNString(int, java.lang.String)    */
specifier|public
name|void
name|updateNString
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|String
name|string
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateNString(java.lang.String, java.lang.String)    */
specifier|public
name|void
name|updateNString
parameter_list|(
name|String
name|columnLabel
parameter_list|,
name|String
name|string
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateNull(int)    */
specifier|public
name|void
name|updateNull
parameter_list|(
name|int
name|columnIndex
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateNull(java.lang.String)    */
specifier|public
name|void
name|updateNull
parameter_list|(
name|String
name|columnName
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateObject(int, java.lang.Object)    */
specifier|public
name|void
name|updateObject
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Object
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateObject(java.lang.String, java.lang.Object)    */
specifier|public
name|void
name|updateObject
parameter_list|(
name|String
name|columnName
parameter_list|,
name|Object
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateObject(int, java.lang.Object, int)    */
specifier|public
name|void
name|updateObject
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Object
name|x
parameter_list|,
name|int
name|scale
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateObject(java.lang.String, java.lang.Object, int)    */
specifier|public
name|void
name|updateObject
parameter_list|(
name|String
name|columnName
parameter_list|,
name|Object
name|x
parameter_list|,
name|int
name|scale
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateRef(int, java.sql.Ref)    */
specifier|public
name|void
name|updateRef
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Ref
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateRef(java.lang.String, java.sql.Ref)    */
specifier|public
name|void
name|updateRef
parameter_list|(
name|String
name|columnName
parameter_list|,
name|Ref
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateRow()    */
specifier|public
name|void
name|updateRow
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateRowId(int, java.sql.RowId)    */
specifier|public
name|void
name|updateRowId
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|RowId
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateRowId(java.lang.String, java.sql.RowId)    */
specifier|public
name|void
name|updateRowId
parameter_list|(
name|String
name|columnLabel
parameter_list|,
name|RowId
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateSQLXML(int, java.sql.SQLXML)    */
specifier|public
name|void
name|updateSQLXML
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|SQLXML
name|xmlObject
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateSQLXML(java.lang.String, java.sql.SQLXML)    */
specifier|public
name|void
name|updateSQLXML
parameter_list|(
name|String
name|columnLabel
parameter_list|,
name|SQLXML
name|xmlObject
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateShort(int, short)    */
specifier|public
name|void
name|updateShort
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|short
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateShort(java.lang.String, short)    */
specifier|public
name|void
name|updateShort
parameter_list|(
name|String
name|columnName
parameter_list|,
name|short
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateString(int, java.lang.String)    */
specifier|public
name|void
name|updateString
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|String
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateString(java.lang.String, java.lang.String)    */
specifier|public
name|void
name|updateString
parameter_list|(
name|String
name|columnName
parameter_list|,
name|String
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateTime(int, java.sql.Time)    */
specifier|public
name|void
name|updateTime
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Time
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateTime(java.lang.String, java.sql.Time)    */
specifier|public
name|void
name|updateTime
parameter_list|(
name|String
name|columnName
parameter_list|,
name|Time
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateTimestamp(int, java.sql.Timestamp)    */
specifier|public
name|void
name|updateTimestamp
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Timestamp
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#updateTimestamp(java.lang.String, java.sql.Timestamp)    */
specifier|public
name|void
name|updateTimestamp
parameter_list|(
name|String
name|columnName
parameter_list|,
name|Timestamp
name|x
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
comment|/* (non-Javadoc)    * @see java.sql.ResultSet#wasNull()    */
specifier|public
name|boolean
name|wasNull
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

