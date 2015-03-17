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
name|parse
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
name|ql
operator|.
name|parse
operator|.
name|ParseUtils
operator|.
name|ensureClassExists
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
name|Map
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
name|lang
operator|.
name|StringUtils
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
name|conf
operator|.
name|HiveConf
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
name|IOConstants
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
name|StorageFormatDescriptor
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
name|StorageFormatFactory
import|;
end_import

begin_class
specifier|public
class|class
name|StorageFormat
block|{
specifier|private
specifier|static
specifier|final
name|StorageFormatFactory
name|storageFormatFactory
init|=
operator|new
name|StorageFormatFactory
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|String
name|inputFormat
decl_stmt|;
specifier|private
name|String
name|outputFormat
decl_stmt|;
specifier|private
name|String
name|storageHandler
decl_stmt|;
specifier|private
name|String
name|serde
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|serdeProps
decl_stmt|;
specifier|public
name|StorageFormat
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|serdeProps
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
comment|/**    * Returns true if the passed token was a storage format token    * and thus was processed accordingly.    */
specifier|public
name|boolean
name|fillStorageFormat
parameter_list|(
name|ASTNode
name|child
parameter_list|)
throws|throws
name|SemanticException
block|{
switch|switch
condition|(
name|child
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|HiveParser
operator|.
name|TOK_TABLEFILEFORMAT
case|:
if|if
condition|(
name|child
operator|.
name|getChildCount
argument_list|()
operator|<
literal|2
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Incomplete specification of File Format. "
operator|+
literal|"You must provide InputFormat, OutputFormat."
argument_list|)
throw|;
block|}
name|inputFormat
operator|=
name|ensureClassExists
argument_list|(
name|BaseSemanticAnalyzer
operator|.
name|unescapeSQLString
argument_list|(
name|child
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|outputFormat
operator|=
name|ensureClassExists
argument_list|(
name|BaseSemanticAnalyzer
operator|.
name|unescapeSQLString
argument_list|(
name|child
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|child
operator|.
name|getChildCount
argument_list|()
operator|==
literal|3
condition|)
block|{
name|serde
operator|=
name|ensureClassExists
argument_list|(
name|BaseSemanticAnalyzer
operator|.
name|unescapeSQLString
argument_list|(
name|child
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|HiveParser
operator|.
name|TOK_STORAGEHANDLER
case|:
name|storageHandler
operator|=
name|ensureClassExists
argument_list|(
name|BaseSemanticAnalyzer
operator|.
name|unescapeSQLString
argument_list|(
name|child
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|child
operator|.
name|getChildCount
argument_list|()
operator|==
literal|2
condition|)
block|{
name|BaseSemanticAnalyzer
operator|.
name|readProps
argument_list|(
call|(
name|ASTNode
call|)
argument_list|(
name|child
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|,
name|serdeProps
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|HiveParser
operator|.
name|TOK_FILEFORMAT_GENERIC
case|:
name|ASTNode
name|grandChild
init|=
operator|(
name|ASTNode
operator|)
name|child
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|name
init|=
operator|(
name|grandChild
operator|==
literal|null
condition|?
literal|""
else|:
name|grandChild
operator|.
name|getText
argument_list|()
operator|)
operator|.
name|trim
argument_list|()
operator|.
name|toUpperCase
argument_list|()
decl_stmt|;
name|processStorageFormat
argument_list|(
name|name
argument_list|)
expr_stmt|;
break|break;
default|default:
comment|// token was not a storage format token
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
specifier|protected
name|void
name|processStorageFormat
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|name
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"File format in STORED AS clause cannot be empty"
argument_list|)
throw|;
block|}
name|StorageFormatDescriptor
name|descriptor
init|=
name|storageFormatFactory
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|descriptor
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Unrecognized file format in STORED AS clause:"
operator|+
literal|" '"
operator|+
name|name
operator|+
literal|"'"
argument_list|)
throw|;
block|}
name|inputFormat
operator|=
name|ensureClassExists
argument_list|(
name|descriptor
operator|.
name|getInputFormat
argument_list|()
argument_list|)
expr_stmt|;
name|outputFormat
operator|=
name|ensureClassExists
argument_list|(
name|descriptor
operator|.
name|getOutputFormat
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|serde
operator|==
literal|null
condition|)
block|{
name|serde
operator|=
name|ensureClassExists
argument_list|(
name|descriptor
operator|.
name|getSerde
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|serde
operator|==
literal|null
condition|)
block|{
comment|// RCFile supports a configurable SerDe
if|if
condition|(
name|name
operator|.
name|equalsIgnoreCase
argument_list|(
name|IOConstants
operator|.
name|RCFILE
argument_list|)
condition|)
block|{
name|serde
operator|=
name|ensureClassExists
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEDEFAULTRCFILESERDE
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|serde
operator|=
name|ensureClassExists
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEDEFAULTSERDE
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|protected
name|void
name|fillDefaultStorageFormat
parameter_list|(
name|boolean
name|isExternal
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
operator|(
name|inputFormat
operator|==
literal|null
operator|)
operator|&&
operator|(
name|storageHandler
operator|==
literal|null
operator|)
condition|)
block|{
name|String
name|defaultFormat
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEDEFAULTFILEFORMAT
argument_list|)
decl_stmt|;
name|String
name|defaultManagedFormat
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEDEFAULTMANAGEDFILEFORMAT
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|isExternal
operator|&&
operator|!
literal|"none"
operator|.
name|equals
argument_list|(
name|defaultManagedFormat
argument_list|)
condition|)
block|{
name|defaultFormat
operator|=
name|defaultManagedFormat
expr_stmt|;
block|}
if|if
condition|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|defaultFormat
argument_list|)
condition|)
block|{
name|inputFormat
operator|=
name|IOConstants
operator|.
name|TEXTFILE_INPUT
expr_stmt|;
name|outputFormat
operator|=
name|IOConstants
operator|.
name|TEXTFILE_OUTPUT
expr_stmt|;
block|}
else|else
block|{
name|processStorageFormat
argument_list|(
name|defaultFormat
argument_list|)
expr_stmt|;
if|if
condition|(
name|defaultFormat
operator|.
name|equalsIgnoreCase
argument_list|(
name|IOConstants
operator|.
name|RCFILE
argument_list|)
condition|)
block|{
name|serde
operator|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEDEFAULTRCFILESERDE
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|public
name|void
name|setSerde
parameter_list|(
name|String
name|serde
parameter_list|)
block|{
name|this
operator|.
name|serde
operator|=
name|serde
expr_stmt|;
block|}
specifier|public
name|String
name|getInputFormat
parameter_list|()
block|{
return|return
name|inputFormat
return|;
block|}
specifier|public
name|String
name|getOutputFormat
parameter_list|()
block|{
return|return
name|outputFormat
return|;
block|}
specifier|public
name|String
name|getStorageHandler
parameter_list|()
block|{
return|return
name|storageHandler
return|;
block|}
specifier|public
name|String
name|getSerde
parameter_list|()
block|{
return|return
name|serde
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getSerdeProps
parameter_list|()
block|{
return|return
name|serdeProps
return|;
block|}
block|}
end_class

end_unit

