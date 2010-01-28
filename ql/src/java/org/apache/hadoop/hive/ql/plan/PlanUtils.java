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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|Properties
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|MetaStoreUtils
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
name|api
operator|.
name|FieldSchema
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
name|exec
operator|.
name|ColumnInfo
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
name|exec
operator|.
name|Operator
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
name|exec
operator|.
name|RowSchema
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
name|exec
operator|.
name|Utilities
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
name|HiveOutputFormat
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
name|IgnoreKeyTextOutputFormat
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
name|TypeCheckProcFactory
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
name|hive
operator|.
name|serde2
operator|.
name|Deserializer
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
name|MetadataTypedColumnsetSerDe
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
name|binarysortable
operator|.
name|BinarySortableSerDe
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
name|lazy
operator|.
name|LazySimpleSerDe
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
name|lazybinary
operator|.
name|LazyBinarySerDe
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
name|mapred
operator|.
name|InputFormat
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
name|mapred
operator|.
name|SequenceFileInputFormat
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
name|mapred
operator|.
name|SequenceFileOutputFormat
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
name|mapred
operator|.
name|TextInputFormat
import|;
end_import

begin_class
specifier|public
class|class
name|PlanUtils
block|{
specifier|protected
specifier|final
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"org.apache.hadoop.hive.ql.plan.PlanUtils"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
enum|enum
name|ExpressionTypes
block|{
name|FIELD
block|,
name|JEXL
block|}
empty_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|public
specifier|static
name|MapredWork
name|getMapRedWork
parameter_list|()
block|{
return|return
operator|new
name|MapredWork
argument_list|(
literal|""
argument_list|,
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
argument_list|,
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
argument_list|()
argument_list|,
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
argument_list|,
operator|new
name|TableDesc
argument_list|()
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|TableDesc
argument_list|>
argument_list|()
argument_list|,
literal|null
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
literal|1
argument_list|)
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Generate the table descriptor of MetadataTypedColumnsetSerDe with the    * separatorCode and column names (comma separated string).    */
specifier|public
specifier|static
name|TableDesc
name|getDefaultTableDesc
parameter_list|(
name|String
name|separatorCode
parameter_list|,
name|String
name|columns
parameter_list|)
block|{
return|return
name|getDefaultTableDesc
argument_list|(
name|separatorCode
argument_list|,
name|columns
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Generate the table descriptor of given serde with the separatorCode and    * column names (comma separated string).    */
specifier|public
specifier|static
name|TableDesc
name|getTableDesc
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Deserializer
argument_list|>
name|serdeClass
parameter_list|,
name|String
name|separatorCode
parameter_list|,
name|String
name|columns
parameter_list|)
block|{
return|return
name|getTableDesc
argument_list|(
name|serdeClass
argument_list|,
name|separatorCode
argument_list|,
name|columns
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Generate the table descriptor of MetadataTypedColumnsetSerDe with the    * separatorCode and column names (comma separated string), and whether the    * last column should take the rest of the line.    */
specifier|public
specifier|static
name|TableDesc
name|getDefaultTableDesc
parameter_list|(
name|String
name|separatorCode
parameter_list|,
name|String
name|columns
parameter_list|,
name|boolean
name|lastColumnTakesRestOfTheLine
parameter_list|)
block|{
return|return
name|getDefaultTableDesc
argument_list|(
name|separatorCode
argument_list|,
name|columns
argument_list|,
literal|null
argument_list|,
name|lastColumnTakesRestOfTheLine
argument_list|)
return|;
block|}
comment|/**    * Generate the table descriptor of the serde specified with the separatorCode    * and column names (comma separated string), and whether the last column    * should take the rest of the line.    */
specifier|public
specifier|static
name|TableDesc
name|getTableDesc
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Deserializer
argument_list|>
name|serdeClass
parameter_list|,
name|String
name|separatorCode
parameter_list|,
name|String
name|columns
parameter_list|,
name|boolean
name|lastColumnTakesRestOfTheLine
parameter_list|)
block|{
return|return
name|getTableDesc
argument_list|(
name|serdeClass
argument_list|,
name|separatorCode
argument_list|,
name|columns
argument_list|,
literal|null
argument_list|,
name|lastColumnTakesRestOfTheLine
argument_list|)
return|;
block|}
comment|/**    * Generate the table descriptor of MetadataTypedColumnsetSerDe with the    * separatorCode and column names (comma separated string), and whether the    * last column should take the rest of the line.    */
specifier|public
specifier|static
name|TableDesc
name|getDefaultTableDesc
parameter_list|(
name|String
name|separatorCode
parameter_list|,
name|String
name|columns
parameter_list|,
name|String
name|columnTypes
parameter_list|,
name|boolean
name|lastColumnTakesRestOfTheLine
parameter_list|)
block|{
return|return
name|getTableDesc
argument_list|(
name|LazySimpleSerDe
operator|.
name|class
argument_list|,
name|separatorCode
argument_list|,
name|columns
argument_list|,
name|columnTypes
argument_list|,
name|lastColumnTakesRestOfTheLine
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TableDesc
name|getTableDesc
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Deserializer
argument_list|>
name|serdeClass
parameter_list|,
name|String
name|separatorCode
parameter_list|,
name|String
name|columns
parameter_list|,
name|String
name|columnTypes
parameter_list|,
name|boolean
name|lastColumnTakesRestOfTheLine
parameter_list|)
block|{
return|return
name|getTableDesc
argument_list|(
name|serdeClass
argument_list|,
name|separatorCode
argument_list|,
name|columns
argument_list|,
name|columnTypes
argument_list|,
name|lastColumnTakesRestOfTheLine
argument_list|,
literal|false
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TableDesc
name|getTableDesc
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Deserializer
argument_list|>
name|serdeClass
parameter_list|,
name|String
name|separatorCode
parameter_list|,
name|String
name|columns
parameter_list|,
name|String
name|columnTypes
parameter_list|,
name|boolean
name|lastColumnTakesRestOfTheLine
parameter_list|,
name|boolean
name|useJSONForLazy
parameter_list|)
block|{
name|Properties
name|properties
init|=
name|Utilities
operator|.
name|makeProperties
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
name|separatorCode
argument_list|,
name|Constants
operator|.
name|LIST_COLUMNS
argument_list|,
name|columns
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|separatorCode
operator|.
name|equals
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|Utilities
operator|.
name|ctrlaCode
argument_list|)
argument_list|)
condition|)
block|{
name|properties
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|FIELD_DELIM
argument_list|,
name|separatorCode
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|columnTypes
operator|!=
literal|null
condition|)
block|{
name|properties
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|columnTypes
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|lastColumnTakesRestOfTheLine
condition|)
block|{
name|properties
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_LAST_COLUMN_TAKES_REST
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
comment|// It is not a very clean way, and should be modified later - due to
comment|// compatiblity reasons,
comment|// user sees the results as json for custom scripts and has no way for
comment|// specifying that.
comment|// Right now, it is hard-coded in the code
if|if
condition|(
name|useJSONForLazy
condition|)
block|{
name|properties
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_USE_JSON_OBJECTS
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|TableDesc
argument_list|(
name|serdeClass
argument_list|,
name|TextInputFormat
operator|.
name|class
argument_list|,
name|IgnoreKeyTextOutputFormat
operator|.
name|class
argument_list|,
name|properties
argument_list|)
return|;
block|}
comment|/**    * Generate a table descriptor from a createTableDesc.    */
specifier|public
specifier|static
name|TableDesc
name|getTableDesc
parameter_list|(
name|CreateTableDesc
name|crtTblDesc
parameter_list|,
name|String
name|cols
parameter_list|,
name|String
name|colTypes
parameter_list|)
block|{
name|Class
argument_list|<
name|?
extends|extends
name|Deserializer
argument_list|>
name|serdeClass
init|=
name|LazySimpleSerDe
operator|.
name|class
decl_stmt|;
name|String
name|separatorCode
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|Utilities
operator|.
name|ctrlaCode
argument_list|)
decl_stmt|;
name|String
name|columns
init|=
name|cols
decl_stmt|;
name|String
name|columnTypes
init|=
name|colTypes
decl_stmt|;
name|boolean
name|lastColumnTakesRestOfTheLine
init|=
literal|false
decl_stmt|;
name|TableDesc
name|ret
decl_stmt|;
try|try
block|{
if|if
condition|(
name|crtTblDesc
operator|.
name|getSerName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Class
name|c
init|=
name|Class
operator|.
name|forName
argument_list|(
name|crtTblDesc
operator|.
name|getSerName
argument_list|()
argument_list|)
decl_stmt|;
name|serdeClass
operator|=
name|c
expr_stmt|;
block|}
if|if
condition|(
name|crtTblDesc
operator|.
name|getFieldDelim
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|separatorCode
operator|=
name|crtTblDesc
operator|.
name|getFieldDelim
argument_list|()
expr_stmt|;
block|}
name|ret
operator|=
name|getTableDesc
argument_list|(
name|serdeClass
argument_list|,
name|separatorCode
argument_list|,
name|columns
argument_list|,
name|columnTypes
argument_list|,
name|lastColumnTakesRestOfTheLine
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// set other table properties
name|Properties
name|properties
init|=
name|ret
operator|.
name|getProperties
argument_list|()
decl_stmt|;
if|if
condition|(
name|crtTblDesc
operator|.
name|getCollItemDelim
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|properties
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|COLLECTION_DELIM
argument_list|,
name|crtTblDesc
operator|.
name|getCollItemDelim
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|crtTblDesc
operator|.
name|getMapKeyDelim
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|properties
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|MAPKEY_DELIM
argument_list|,
name|crtTblDesc
operator|.
name|getMapKeyDelim
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|crtTblDesc
operator|.
name|getFieldEscape
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|properties
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|ESCAPE_CHAR
argument_list|,
name|crtTblDesc
operator|.
name|getFieldEscape
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|crtTblDesc
operator|.
name|getLineDelim
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|properties
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|LINE_DELIM
argument_list|,
name|crtTblDesc
operator|.
name|getLineDelim
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// replace the default input& output file format with those found in
comment|// crtTblDesc
name|Class
name|c1
init|=
name|Class
operator|.
name|forName
argument_list|(
name|crtTblDesc
operator|.
name|getInputFormat
argument_list|()
argument_list|)
decl_stmt|;
name|Class
name|c2
init|=
name|Class
operator|.
name|forName
argument_list|(
name|crtTblDesc
operator|.
name|getOutputFormat
argument_list|()
argument_list|)
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|in_class
init|=
name|c1
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|HiveOutputFormat
argument_list|>
name|out_class
init|=
name|c2
decl_stmt|;
name|ret
operator|.
name|setInputFileFormatClass
argument_list|(
name|in_class
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setOutputFileFormatClass
argument_list|(
name|out_class
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
name|ret
return|;
block|}
comment|/**    * Generate the table descriptor of MetadataTypedColumnsetSerDe with the    * separatorCode. MetaDataTypedColumnsetSerDe is used because LazySimpleSerDe    * does not support a table with a single column "col" with type    * "array<string>".    */
specifier|public
specifier|static
name|TableDesc
name|getDefaultTableDesc
parameter_list|(
name|String
name|separatorCode
parameter_list|)
block|{
return|return
operator|new
name|TableDesc
argument_list|(
name|MetadataTypedColumnsetSerDe
operator|.
name|class
argument_list|,
name|TextInputFormat
operator|.
name|class
argument_list|,
name|IgnoreKeyTextOutputFormat
operator|.
name|class
argument_list|,
name|Utilities
operator|.
name|makeProperties
argument_list|(
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
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
name|separatorCode
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Generate the table descriptor for reduce key.    */
specifier|public
specifier|static
name|TableDesc
name|getReduceKeyTableDesc
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fieldSchemas
parameter_list|,
name|String
name|order
parameter_list|)
block|{
return|return
operator|new
name|TableDesc
argument_list|(
name|BinarySortableSerDe
operator|.
name|class
argument_list|,
name|SequenceFileInputFormat
operator|.
name|class
argument_list|,
name|SequenceFileOutputFormat
operator|.
name|class
argument_list|,
name|Utilities
operator|.
name|makeProperties
argument_list|(
name|Constants
operator|.
name|LIST_COLUMNS
argument_list|,
name|MetaStoreUtils
operator|.
name|getColumnNamesFromFieldSchema
argument_list|(
name|fieldSchemas
argument_list|)
argument_list|,
name|Constants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|MetaStoreUtils
operator|.
name|getColumnTypesFromFieldSchema
argument_list|(
name|fieldSchemas
argument_list|)
argument_list|,
name|Constants
operator|.
name|SERIALIZATION_SORT_ORDER
argument_list|,
name|order
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Generate the table descriptor for Map-side join key.    */
specifier|public
specifier|static
name|TableDesc
name|getMapJoinKeyTableDesc
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fieldSchemas
parameter_list|)
block|{
return|return
operator|new
name|TableDesc
argument_list|(
name|LazyBinarySerDe
operator|.
name|class
argument_list|,
name|SequenceFileInputFormat
operator|.
name|class
argument_list|,
name|SequenceFileOutputFormat
operator|.
name|class
argument_list|,
name|Utilities
operator|.
name|makeProperties
argument_list|(
literal|"columns"
argument_list|,
name|MetaStoreUtils
operator|.
name|getColumnNamesFromFieldSchema
argument_list|(
name|fieldSchemas
argument_list|)
argument_list|,
literal|"columns.types"
argument_list|,
name|MetaStoreUtils
operator|.
name|getColumnTypesFromFieldSchema
argument_list|(
name|fieldSchemas
argument_list|)
argument_list|,
name|Constants
operator|.
name|ESCAPE_CHAR
argument_list|,
literal|"\\"
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Generate the table descriptor for Map-side join key.    */
specifier|public
specifier|static
name|TableDesc
name|getMapJoinValueTableDesc
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fieldSchemas
parameter_list|)
block|{
return|return
operator|new
name|TableDesc
argument_list|(
name|LazyBinarySerDe
operator|.
name|class
argument_list|,
name|SequenceFileInputFormat
operator|.
name|class
argument_list|,
name|SequenceFileOutputFormat
operator|.
name|class
argument_list|,
name|Utilities
operator|.
name|makeProperties
argument_list|(
literal|"columns"
argument_list|,
name|MetaStoreUtils
operator|.
name|getColumnNamesFromFieldSchema
argument_list|(
name|fieldSchemas
argument_list|)
argument_list|,
literal|"columns.types"
argument_list|,
name|MetaStoreUtils
operator|.
name|getColumnTypesFromFieldSchema
argument_list|(
name|fieldSchemas
argument_list|)
argument_list|,
name|Constants
operator|.
name|ESCAPE_CHAR
argument_list|,
literal|"\\"
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Generate the table descriptor for intermediate files.    */
specifier|public
specifier|static
name|TableDesc
name|getIntermediateFileTableDesc
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fieldSchemas
parameter_list|)
block|{
return|return
operator|new
name|TableDesc
argument_list|(
name|LazyBinarySerDe
operator|.
name|class
argument_list|,
name|SequenceFileInputFormat
operator|.
name|class
argument_list|,
name|SequenceFileOutputFormat
operator|.
name|class
argument_list|,
name|Utilities
operator|.
name|makeProperties
argument_list|(
name|Constants
operator|.
name|LIST_COLUMNS
argument_list|,
name|MetaStoreUtils
operator|.
name|getColumnNamesFromFieldSchema
argument_list|(
name|fieldSchemas
argument_list|)
argument_list|,
name|Constants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|MetaStoreUtils
operator|.
name|getColumnTypesFromFieldSchema
argument_list|(
name|fieldSchemas
argument_list|)
argument_list|,
name|Constants
operator|.
name|ESCAPE_CHAR
argument_list|,
literal|"\\"
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Generate the table descriptor for intermediate files.    */
specifier|public
specifier|static
name|TableDesc
name|getReduceValueTableDesc
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fieldSchemas
parameter_list|)
block|{
return|return
operator|new
name|TableDesc
argument_list|(
name|LazyBinarySerDe
operator|.
name|class
argument_list|,
name|SequenceFileInputFormat
operator|.
name|class
argument_list|,
name|SequenceFileOutputFormat
operator|.
name|class
argument_list|,
name|Utilities
operator|.
name|makeProperties
argument_list|(
name|Constants
operator|.
name|LIST_COLUMNS
argument_list|,
name|MetaStoreUtils
operator|.
name|getColumnNamesFromFieldSchema
argument_list|(
name|fieldSchemas
argument_list|)
argument_list|,
name|Constants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|MetaStoreUtils
operator|.
name|getColumnTypesFromFieldSchema
argument_list|(
name|fieldSchemas
argument_list|)
argument_list|,
name|Constants
operator|.
name|ESCAPE_CHAR
argument_list|,
literal|"\\"
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Convert the ColumnList to FieldSchema list.    */
specifier|public
specifier|static
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getFieldSchemasFromColumnList
parameter_list|(
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|cols
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|outputColumnNames
parameter_list|,
name|int
name|start
parameter_list|,
name|String
name|fieldPrefix
parameter_list|)
block|{
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|schemas
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|(
name|cols
operator|.
name|size
argument_list|()
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
name|cols
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|schemas
operator|.
name|add
argument_list|(
name|MetaStoreUtils
operator|.
name|getFieldSchemaFromTypeInfo
argument_list|(
name|fieldPrefix
operator|+
name|outputColumnNames
operator|.
name|get
argument_list|(
name|i
operator|+
name|start
argument_list|)
argument_list|,
name|cols
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|schemas
return|;
block|}
comment|/**    * Convert the ColumnList to FieldSchema list.    */
specifier|public
specifier|static
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getFieldSchemasFromColumnList
parameter_list|(
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|cols
parameter_list|,
name|String
name|fieldPrefix
parameter_list|)
block|{
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|schemas
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|(
name|cols
operator|.
name|size
argument_list|()
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
name|cols
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|schemas
operator|.
name|add
argument_list|(
name|MetaStoreUtils
operator|.
name|getFieldSchemaFromTypeInfo
argument_list|(
name|fieldPrefix
operator|+
name|i
argument_list|,
name|cols
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|schemas
return|;
block|}
comment|/**    * Convert the RowSchema to FieldSchema list.    */
specifier|public
specifier|static
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getFieldSchemasFromRowSchema
parameter_list|(
name|RowSchema
name|row
parameter_list|,
name|String
name|fieldPrefix
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
name|c
init|=
name|row
operator|.
name|getSignature
argument_list|()
decl_stmt|;
return|return
name|getFieldSchemasFromColumnInfo
argument_list|(
name|c
argument_list|,
name|fieldPrefix
argument_list|)
return|;
block|}
comment|/**    * Convert the ColumnInfo to FieldSchema.    */
specifier|public
specifier|static
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getFieldSchemasFromColumnInfo
parameter_list|(
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
name|cols
parameter_list|,
name|String
name|fieldPrefix
parameter_list|)
block|{
if|if
condition|(
operator|(
name|cols
operator|==
literal|null
operator|)
operator|||
operator|(
name|cols
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|)
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
return|;
block|}
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|schemas
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|(
name|cols
operator|.
name|size
argument_list|()
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
name|cols
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|name
init|=
name|cols
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getInternalName
argument_list|()
decl_stmt|;
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
name|name
operator|=
name|fieldPrefix
operator|+
name|name
expr_stmt|;
block|}
name|schemas
operator|.
name|add
argument_list|(
name|MetaStoreUtils
operator|.
name|getFieldSchemaFromTypeInfo
argument_list|(
name|name
argument_list|,
name|cols
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|schemas
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|sortFieldSchemas
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|schema
parameter_list|)
block|{
name|Collections
operator|.
name|sort
argument_list|(
name|schema
argument_list|,
operator|new
name|Comparator
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|FieldSchema
name|o1
parameter_list|,
name|FieldSchema
name|o2
parameter_list|)
block|{
return|return
name|o1
operator|.
name|getName
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|schema
return|;
block|}
comment|/**    * Create the reduce sink descriptor.    *     * @param keyCols    *          The columns to be stored in the key    * @param valueCols    *          The columns to be stored in the value    * @param outputColumnNames    *          The output columns names    * @param tag    *          The tag for this reducesink    * @param partitionCols    *          The columns for partitioning.    * @param numReducers    *          The number of reducers, set to -1 for automatic inference based on    *          input data size.    * @return The reduceSinkDesc object.    */
specifier|public
specifier|static
name|ReduceSinkDesc
name|getReduceSinkDesc
parameter_list|(
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keyCols
parameter_list|,
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|valueCols
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|outputColumnNames
parameter_list|,
name|boolean
name|includeKeyCols
parameter_list|,
name|int
name|tag
parameter_list|,
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|partitionCols
parameter_list|,
name|String
name|order
parameter_list|,
name|int
name|numReducers
parameter_list|)
block|{
name|TableDesc
name|keyTable
init|=
literal|null
decl_stmt|;
name|TableDesc
name|valueTable
init|=
literal|null
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|outputKeyCols
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|outputValCols
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|includeKeyCols
condition|)
block|{
name|keyTable
operator|=
name|getReduceKeyTableDesc
argument_list|(
name|getFieldSchemasFromColumnList
argument_list|(
name|keyCols
argument_list|,
name|outputColumnNames
argument_list|,
literal|0
argument_list|,
literal|""
argument_list|)
argument_list|,
name|order
argument_list|)
expr_stmt|;
name|outputKeyCols
operator|.
name|addAll
argument_list|(
name|outputColumnNames
operator|.
name|subList
argument_list|(
literal|0
argument_list|,
name|keyCols
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|valueTable
operator|=
name|getReduceValueTableDesc
argument_list|(
name|getFieldSchemasFromColumnList
argument_list|(
name|valueCols
argument_list|,
name|outputColumnNames
argument_list|,
name|keyCols
operator|.
name|size
argument_list|()
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|outputValCols
operator|.
name|addAll
argument_list|(
name|outputColumnNames
operator|.
name|subList
argument_list|(
name|keyCols
operator|.
name|size
argument_list|()
argument_list|,
name|outputColumnNames
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|keyTable
operator|=
name|getReduceKeyTableDesc
argument_list|(
name|getFieldSchemasFromColumnList
argument_list|(
name|keyCols
argument_list|,
literal|"reducesinkkey"
argument_list|)
argument_list|,
name|order
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|keyCols
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|outputKeyCols
operator|.
name|add
argument_list|(
literal|"reducesinkkey"
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|valueTable
operator|=
name|getReduceValueTableDesc
argument_list|(
name|getFieldSchemasFromColumnList
argument_list|(
name|valueCols
argument_list|,
name|outputColumnNames
argument_list|,
literal|0
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|outputValCols
operator|.
name|addAll
argument_list|(
name|outputColumnNames
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ReduceSinkDesc
argument_list|(
name|keyCols
argument_list|,
name|valueCols
argument_list|,
name|outputKeyCols
argument_list|,
name|outputValCols
argument_list|,
name|tag
argument_list|,
name|partitionCols
argument_list|,
name|numReducers
argument_list|,
name|keyTable
argument_list|,
comment|// Revert to DynamicSerDe:
comment|// getBinaryTableDesc(getFieldSchemasFromColumnList(valueCols,
comment|// "reducesinkvalue")));
name|valueTable
argument_list|)
return|;
block|}
comment|/**    * Create the reduce sink descriptor.    *     * @param keyCols    *          The columns to be stored in the key    * @param valueCols    *          The columns to be stored in the value    * @param outputColumnNames    *          The output columns names    * @param tag    *          The tag for this reducesink    * @param numPartitionFields    *          The first numPartitionFields of keyCols will be partition columns.    *          If numPartitionFields=-1, then partition randomly.    * @param numReducers    *          The number of reducers, set to -1 for automatic inference based on    *          input data size.    * @return The reduceSinkDesc object.    */
specifier|public
specifier|static
name|ReduceSinkDesc
name|getReduceSinkDesc
parameter_list|(
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keyCols
parameter_list|,
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|valueCols
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|outputColumnNames
parameter_list|,
name|boolean
name|includeKey
parameter_list|,
name|int
name|tag
parameter_list|,
name|int
name|numPartitionFields
parameter_list|,
name|int
name|numReducers
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|partitionCols
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|numPartitionFields
operator|>=
name|keyCols
operator|.
name|size
argument_list|()
condition|)
block|{
name|partitionCols
operator|=
name|keyCols
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|numPartitionFields
operator|>=
literal|0
condition|)
block|{
name|partitionCols
operator|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|(
name|numPartitionFields
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numPartitionFields
condition|;
name|i
operator|++
control|)
block|{
name|partitionCols
operator|.
name|add
argument_list|(
name|keyCols
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// numPartitionFields = -1 means random partitioning
name|partitionCols
operator|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|partitionCols
operator|.
name|add
argument_list|(
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"rand"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|StringBuilder
name|order
init|=
operator|new
name|StringBuilder
argument_list|()
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
name|keyCols
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|order
operator|.
name|append
argument_list|(
literal|"+"
argument_list|)
expr_stmt|;
block|}
return|return
name|getReduceSinkDesc
argument_list|(
name|keyCols
argument_list|,
name|valueCols
argument_list|,
name|outputColumnNames
argument_list|,
name|includeKey
argument_list|,
name|tag
argument_list|,
name|partitionCols
argument_list|,
name|order
operator|.
name|toString
argument_list|()
argument_list|,
name|numReducers
argument_list|)
return|;
block|}
block|}
end_class

end_unit

