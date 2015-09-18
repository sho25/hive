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
name|serde2
package|;
end_package

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
name|List
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Joiner
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * ColumnProjectionUtils.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|ColumnProjectionUtils
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ColumnProjectionUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|READ_COLUMN_IDS_CONF_STR
init|=
literal|"hive.io.file.readcolumn.ids"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|READ_ALL_COLUMNS
init|=
literal|"hive.io.file.read.all.columns"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|READ_COLUMN_NAMES_CONF_STR
init|=
literal|"hive.io.file.readcolumn.names"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|READ_COLUMN_IDS_CONF_STR_DEFAULT
init|=
literal|""
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|boolean
name|READ_ALL_COLUMNS_DEFAULT
init|=
literal|true
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Joiner
name|CSV_JOINER
init|=
name|Joiner
operator|.
name|on
argument_list|(
literal|","
argument_list|)
operator|.
name|skipNulls
argument_list|()
decl_stmt|;
comment|/**    * @deprecated for backwards compatibility with<= 0.12, use setReadAllColumns    */
annotation|@
name|Deprecated
specifier|public
specifier|static
name|void
name|setFullyReadColumns
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|setReadAllColumns
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * @deprecated for backwards compatibility with<= 0.12, use setReadAllColumns    * and appendReadColumns    */
annotation|@
name|Deprecated
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|void
name|setReadColumnIDs
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|List
argument_list|<
name|Integer
argument_list|>
name|ids
parameter_list|)
block|{
name|setReadColumnIDConf
argument_list|(
name|conf
argument_list|,
name|READ_COLUMN_IDS_CONF_STR_DEFAULT
argument_list|)
expr_stmt|;
name|appendReadColumns
argument_list|(
name|conf
argument_list|,
name|ids
argument_list|)
expr_stmt|;
block|}
comment|/**    * @deprecated for backwards compatibility with<= 0.12, use appendReadColumns    */
annotation|@
name|Deprecated
specifier|public
specifier|static
name|void
name|appendReadColumnIDs
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|List
argument_list|<
name|Integer
argument_list|>
name|ids
parameter_list|)
block|{
name|appendReadColumns
argument_list|(
name|conf
argument_list|,
name|ids
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sets the<em>READ_ALL_COLUMNS</em> flag and removes any previously    * set column ids.    */
specifier|public
specifier|static
name|void
name|setReadAllColumns
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|conf
operator|.
name|setBoolean
argument_list|(
name|READ_ALL_COLUMNS
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|setReadColumnIDConf
argument_list|(
name|conf
argument_list|,
name|READ_COLUMN_IDS_CONF_STR_DEFAULT
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns the<em>READ_ALL_COLUMNS</em> columns flag.    */
specifier|public
specifier|static
name|boolean
name|isReadAllColumns
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
name|READ_ALL_COLUMNS
argument_list|,
name|READ_ALL_COLUMNS_DEFAULT
argument_list|)
return|;
block|}
comment|/**    * Appends read columns' ids (start from zero). Once a column    * is included in the list, a underlying record reader of a columnar file format    * (e.g. RCFile and ORC) can know what columns are needed.    */
specifier|public
specifier|static
name|void
name|appendReadColumns
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|List
argument_list|<
name|Integer
argument_list|>
name|ids
parameter_list|)
block|{
name|String
name|id
init|=
name|toReadColumnIDString
argument_list|(
name|ids
argument_list|)
decl_stmt|;
name|String
name|old
init|=
name|conf
operator|.
name|get
argument_list|(
name|READ_COLUMN_IDS_CONF_STR
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|String
name|newConfStr
init|=
name|id
decl_stmt|;
if|if
condition|(
name|old
operator|!=
literal|null
condition|)
block|{
name|newConfStr
operator|=
name|newConfStr
operator|+
name|StringUtils
operator|.
name|COMMA_STR
operator|+
name|old
expr_stmt|;
block|}
name|setReadColumnIDConf
argument_list|(
name|conf
argument_list|,
name|newConfStr
argument_list|)
expr_stmt|;
comment|// Set READ_ALL_COLUMNS to false
name|conf
operator|.
name|setBoolean
argument_list|(
name|READ_ALL_COLUMNS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * This method appends read column information to configuration to use for PPD. It is    * currently called with information from TSOP. Names come from TSOP input RowSchema, and    * IDs are the indexes inside the schema (which PPD assumes correspond to indexes inside the    * files to PPD in; something that would be invalid in many cases of schema evolution).    * @param conf Config to set values to.    * @param ids Column ids.    * @param names Column names.    */
specifier|public
specifier|static
name|void
name|appendReadColumns
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|List
argument_list|<
name|Integer
argument_list|>
name|ids
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|names
parameter_list|)
block|{
if|if
condition|(
name|ids
operator|.
name|size
argument_list|()
operator|!=
name|names
operator|.
name|size
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Read column counts do not match: "
operator|+
name|ids
operator|.
name|size
argument_list|()
operator|+
literal|" ids, "
operator|+
name|names
operator|.
name|size
argument_list|()
operator|+
literal|" names"
argument_list|)
expr_stmt|;
block|}
name|appendReadColumns
argument_list|(
name|conf
argument_list|,
name|ids
argument_list|)
expr_stmt|;
name|appendReadColumnNames
argument_list|(
name|conf
argument_list|,
name|names
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|appendReadColumns
parameter_list|(
name|StringBuilder
name|readColumnsBuffer
parameter_list|,
name|StringBuilder
name|readColumnNamesBuffer
parameter_list|,
name|List
argument_list|<
name|Integer
argument_list|>
name|ids
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|names
parameter_list|)
block|{
name|CSV_JOINER
operator|.
name|appendTo
argument_list|(
name|readColumnsBuffer
argument_list|,
name|ids
argument_list|)
expr_stmt|;
name|CSV_JOINER
operator|.
name|appendTo
argument_list|(
name|readColumnNamesBuffer
argument_list|,
name|names
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns an array of column ids(start from zero) which is set in the given    * parameter<tt>conf</tt>.    */
specifier|public
specifier|static
name|List
argument_list|<
name|Integer
argument_list|>
name|getReadColumnIDs
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|skips
init|=
name|conf
operator|.
name|get
argument_list|(
name|READ_COLUMN_IDS_CONF_STR
argument_list|,
name|READ_COLUMN_IDS_CONF_STR_DEFAULT
argument_list|)
decl_stmt|;
name|String
index|[]
name|list
init|=
name|StringUtils
operator|.
name|split
argument_list|(
name|skips
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|list
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|element
range|:
name|list
control|)
block|{
comment|// it may contain duplicates, remove duplicates
comment|// TODO: WTF? This would break many assumptions elsewhere if it did.
comment|//       Column names' and column ids' lists are supposed to be correlated.
name|Integer
name|toAdd
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|element
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|result
operator|.
name|contains
argument_list|(
name|toAdd
argument_list|)
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|toAdd
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Duplicate ID "
operator|+
name|toAdd
operator|+
literal|" in column ID list"
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
specifier|public
specifier|static
name|String
index|[]
name|getReadColumnNames
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|colNames
init|=
name|conf
operator|.
name|get
argument_list|(
name|READ_COLUMN_NAMES_CONF_STR
argument_list|,
name|READ_COLUMN_IDS_CONF_STR_DEFAULT
argument_list|)
decl_stmt|;
if|if
condition|(
name|colNames
operator|!=
literal|null
operator|&&
operator|!
name|colNames
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|colNames
operator|.
name|split
argument_list|(
literal|","
argument_list|)
return|;
block|}
return|return
operator|new
name|String
index|[]
block|{}
return|;
block|}
specifier|private
specifier|static
name|void
name|setReadColumnIDConf
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|id
parameter_list|)
block|{
if|if
condition|(
name|id
operator|.
name|trim
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|READ_COLUMN_IDS_CONF_STR
argument_list|,
name|READ_COLUMN_IDS_CONF_STR_DEFAULT
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|conf
operator|.
name|set
argument_list|(
name|READ_COLUMN_IDS_CONF_STR
argument_list|,
name|id
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|appendReadColumnNames
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|cols
parameter_list|)
block|{
name|String
name|old
init|=
name|conf
operator|.
name|get
argument_list|(
name|READ_COLUMN_NAMES_CONF_STR
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|StringBuilder
name|result
init|=
operator|new
name|StringBuilder
argument_list|(
name|old
argument_list|)
decl_stmt|;
name|boolean
name|first
init|=
name|old
operator|.
name|isEmpty
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|col
range|:
name|cols
control|)
block|{
if|if
condition|(
name|first
condition|)
block|{
name|first
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|result
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|append
argument_list|(
name|col
argument_list|)
expr_stmt|;
block|}
name|conf
operator|.
name|set
argument_list|(
name|READ_COLUMN_NAMES_CONF_STR
argument_list|,
name|result
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|String
name|toReadColumnIDString
parameter_list|(
name|List
argument_list|<
name|Integer
argument_list|>
name|ids
parameter_list|)
block|{
name|String
name|id
init|=
literal|""
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
name|ids
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|id
operator|=
name|id
operator|+
name|ids
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|id
operator|=
name|id
operator|+
name|StringUtils
operator|.
name|COMMA_STR
operator|+
name|ids
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|id
return|;
block|}
specifier|private
name|ColumnProjectionUtils
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit

