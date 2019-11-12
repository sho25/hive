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
name|commons
operator|.
name|lang3
operator|.
name|tuple
operator|.
name|Pair
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
name|serde2
operator|.
name|typeinfo
operator|.
name|PrimitiveTypeInfo
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
name|mapreduce
operator|.
name|RecordWriter
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
name|mapreduce
operator|.
name|TaskAttemptContext
import|;
end_import

begin_import
import|import
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
name|exception
operator|.
name|HiveJdbcDatabaseAccessException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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

begin_interface
specifier|public
interface|interface
name|DatabaseAccessor
block|{
name|List
argument_list|<
name|String
argument_list|>
name|getColumnNames
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|HiveJdbcDatabaseAccessException
function_decl|;
name|int
name|getTotalNumberOfRecords
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|HiveJdbcDatabaseAccessException
function_decl|;
name|JdbcRecordIterator
name|getRecordIterator
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|partitionColumn
parameter_list|,
name|String
name|lowerBound
parameter_list|,
name|String
name|upperBound
parameter_list|,
name|int
name|limit
parameter_list|,
name|int
name|offset
parameter_list|)
throws|throws
name|HiveJdbcDatabaseAccessException
function_decl|;
name|RecordWriter
name|getRecordWriter
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|Pair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getBounds
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|partitionColumn
parameter_list|,
name|boolean
name|lower
parameter_list|,
name|boolean
name|upper
parameter_list|)
throws|throws
name|HiveJdbcDatabaseAccessException
function_decl|;
name|boolean
name|needColumnQuote
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

