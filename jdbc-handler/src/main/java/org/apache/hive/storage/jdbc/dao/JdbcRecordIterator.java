begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|storage
operator|.
name|jdbc
operator|.
name|dao
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
name|io
operator|.
name|NullWritable
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
name|PreparedStatement
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
name|ResultSetMetaData
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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

begin_comment
comment|/**  * An iterator that allows iterating through a SQL resultset. Includes methods to clear up resources.  */
end_comment

begin_class
specifier|public
class|class
name|JdbcRecordIterator
implements|implements
name|Iterator
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOGGER
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|JdbcRecordIterator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Connection
name|conn
decl_stmt|;
specifier|private
name|PreparedStatement
name|ps
decl_stmt|;
specifier|private
name|ResultSet
name|rs
decl_stmt|;
specifier|public
name|JdbcRecordIterator
parameter_list|(
name|Connection
name|conn
parameter_list|,
name|PreparedStatement
name|ps
parameter_list|,
name|ResultSet
name|rs
parameter_list|)
block|{
name|this
operator|.
name|conn
operator|=
name|conn
expr_stmt|;
name|this
operator|.
name|ps
operator|=
name|ps
expr_stmt|;
name|this
operator|.
name|rs
operator|=
name|rs
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
try|try
block|{
return|return
name|rs
operator|.
name|next
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|se
parameter_list|)
block|{
name|LOGGER
operator|.
name|warn
argument_list|(
literal|"hasNext() threw exception"
argument_list|,
name|se
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|next
parameter_list|()
block|{
try|try
block|{
name|ResultSetMetaData
name|metadata
init|=
name|rs
operator|.
name|getMetaData
argument_list|()
decl_stmt|;
name|int
name|numColumns
init|=
name|metadata
operator|.
name|getColumnCount
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|record
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|numColumns
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numColumns
condition|;
name|i
operator|++
control|)
block|{
name|String
name|key
init|=
name|metadata
operator|.
name|getColumnName
argument_list|(
name|i
operator|+
literal|1
argument_list|)
decl_stmt|;
name|String
name|value
init|=
name|rs
operator|.
name|getString
argument_list|(
name|i
operator|+
literal|1
argument_list|)
decl_stmt|;
name|record
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|record
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOGGER
operator|.
name|warn
argument_list|(
literal|"next() threw exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Remove is not supported"
argument_list|)
throw|;
block|}
comment|/**    * Release all DB resources    */
specifier|public
name|void
name|close
parameter_list|()
block|{
try|try
block|{
name|rs
operator|.
name|close
argument_list|()
expr_stmt|;
name|ps
operator|.
name|close
argument_list|()
expr_stmt|;
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOGGER
operator|.
name|warn
argument_list|(
literal|"Caught exception while trying to close database objects"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

