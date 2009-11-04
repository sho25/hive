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
name|io
package|;
end_package

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
name|ArrayList
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
name|java
operator|.
name|util
operator|.
name|Set
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
name|fs
operator|.
name|FileStatus
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|metadata
operator|.
name|HiveException
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
name|JobConf
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
name|OutputFormat
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

begin_comment
comment|/**  * An util class for various Hive file format tasks.  * registerOutputFormatSubstitute(Class, Class)   * getOutputFormatSubstitute(Class) are added for backward   * compatibility. They return the newly added HiveOutputFormat for the older   * ones.  *   */
end_comment

begin_class
specifier|public
class|class
name|HiveFileFormatUtils
block|{
static|static
block|{
name|outputFormatSubstituteMap
operator|=
operator|new
name|HashMap
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|HiveOutputFormat
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|HiveFileFormatUtils
operator|.
name|registerOutputFormatSubstitute
argument_list|(
name|IgnoreKeyTextOutputFormat
operator|.
name|class
argument_list|,
name|HiveIgnoreKeyTextOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|HiveFileFormatUtils
operator|.
name|registerOutputFormatSubstitute
argument_list|(
name|SequenceFileOutputFormat
operator|.
name|class
argument_list|,
name|HiveSequenceFileOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
specifier|static
name|Map
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|HiveOutputFormat
argument_list|>
argument_list|>
name|outputFormatSubstituteMap
decl_stmt|;
comment|/**    * register a substitute    *     * @param origin    *          the class that need to be substituted    * @param substitute    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|synchronized
specifier|static
name|void
name|registerOutputFormatSubstitute
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
name|origin
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|HiveOutputFormat
argument_list|>
name|substitute
parameter_list|)
block|{
name|outputFormatSubstituteMap
operator|.
name|put
argument_list|(
name|origin
argument_list|,
name|substitute
argument_list|)
expr_stmt|;
block|}
comment|/**    * get a OutputFormat's substitute HiveOutputFormat    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|synchronized
specifier|static
name|Class
argument_list|<
name|?
extends|extends
name|HiveOutputFormat
argument_list|>
name|getOutputFormatSubstitute
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|origin
parameter_list|)
block|{
if|if
condition|(
name|HiveOutputFormat
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|origin
argument_list|)
condition|)
return|return
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|HiveOutputFormat
argument_list|>
operator|)
name|origin
return|;
name|Class
argument_list|<
name|?
extends|extends
name|HiveOutputFormat
argument_list|>
name|result
init|=
name|outputFormatSubstituteMap
operator|.
name|get
argument_list|(
name|origin
argument_list|)
decl_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * get the final output path of a given FileOutputFormat.    *     * @param parent    *          parent dir of the expected final output path    * @param jc    *          job configuration    */
specifier|public
specifier|static
name|Path
name|getOutputFormatFinalPath
parameter_list|(
name|Path
name|parent
parameter_list|,
name|JobConf
name|jc
parameter_list|,
name|HiveOutputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|hiveOutputFormat
parameter_list|,
name|boolean
name|isCompressed
parameter_list|,
name|Path
name|defaultFinalPath
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|hiveOutputFormat
operator|instanceof
name|HiveIgnoreKeyTextOutputFormat
condition|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|parent
argument_list|,
name|Utilities
operator|.
name|getTaskId
argument_list|(
name|jc
argument_list|)
operator|+
name|Utilities
operator|.
name|getFileExtension
argument_list|(
name|jc
argument_list|,
name|isCompressed
argument_list|)
argument_list|)
return|;
block|}
return|return
name|defaultFinalPath
return|;
block|}
static|static
block|{
name|inputFormatCheckerMap
operator|=
operator|new
name|HashMap
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|InputFormatChecker
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|HiveFileFormatUtils
operator|.
name|registerInputFormatChecker
argument_list|(
name|SequenceFileInputFormat
operator|.
name|class
argument_list|,
name|SequenceFileInputFormatChecker
operator|.
name|class
argument_list|)
expr_stmt|;
name|HiveFileFormatUtils
operator|.
name|registerInputFormatChecker
argument_list|(
name|RCFileInputFormat
operator|.
name|class
argument_list|,
name|RCFileInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|inputFormatCheckerInstanceCache
operator|=
operator|new
name|HashMap
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|InputFormatChecker
argument_list|>
argument_list|,
name|InputFormatChecker
argument_list|>
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
specifier|static
name|Map
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|InputFormatChecker
argument_list|>
argument_list|>
name|inputFormatCheckerMap
decl_stmt|;
specifier|private
specifier|static
name|Map
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|InputFormatChecker
argument_list|>
argument_list|,
name|InputFormatChecker
argument_list|>
name|inputFormatCheckerInstanceCache
decl_stmt|;
comment|/**    * register an InputFormatChecker for a given InputFormat    *     * @param format    *          the class that need to be substituted    * @param checker    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|synchronized
specifier|static
name|void
name|registerInputFormatChecker
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|format
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|InputFormatChecker
argument_list|>
name|checker
parameter_list|)
block|{
name|inputFormatCheckerMap
operator|.
name|put
argument_list|(
name|format
argument_list|,
name|checker
argument_list|)
expr_stmt|;
block|}
comment|/**    * get an InputFormatChecker for a file format.    */
specifier|public
specifier|synchronized
specifier|static
name|Class
argument_list|<
name|?
extends|extends
name|InputFormatChecker
argument_list|>
name|getInputFormatChecker
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|inputFormat
parameter_list|)
block|{
name|Class
argument_list|<
name|?
extends|extends
name|InputFormatChecker
argument_list|>
name|result
init|=
name|inputFormatCheckerMap
operator|.
name|get
argument_list|(
name|inputFormat
argument_list|)
decl_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * checks if files are in same format as the given input format    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
name|boolean
name|checkInputFormat
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|inputFormatCls
parameter_list|,
name|ArrayList
argument_list|<
name|FileStatus
argument_list|>
name|files
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|files
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|Class
argument_list|<
name|?
extends|extends
name|InputFormatChecker
argument_list|>
name|checkerCls
init|=
name|getInputFormatChecker
argument_list|(
name|inputFormatCls
argument_list|)
decl_stmt|;
if|if
condition|(
name|checkerCls
operator|==
literal|null
operator|&&
name|inputFormatCls
operator|.
name|isAssignableFrom
argument_list|(
name|TextInputFormat
operator|.
name|class
argument_list|)
condition|)
block|{
comment|// we get a text input format here, we can not determine a file is text
comment|// according to its content, so we can do is to test if other file
comment|// format can accept it. If one other file format can accept this file,
comment|// we treat this file as text file, although it maybe not.
return|return
name|checkTextInputFormat
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|files
argument_list|)
return|;
block|}
if|if
condition|(
name|checkerCls
operator|!=
literal|null
condition|)
block|{
name|InputFormatChecker
name|checkerInstance
init|=
name|inputFormatCheckerInstanceCache
operator|.
name|get
argument_list|(
name|checkerCls
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|checkerInstance
operator|==
literal|null
condition|)
block|{
name|checkerInstance
operator|=
name|checkerCls
operator|.
name|newInstance
argument_list|()
expr_stmt|;
name|inputFormatCheckerInstanceCache
operator|.
name|put
argument_list|(
name|checkerCls
argument_list|,
name|checkerInstance
argument_list|)
expr_stmt|;
block|}
return|return
name|checkerInstance
operator|.
name|validateInput
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|files
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
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
specifier|static
name|boolean
name|checkTextInputFormat
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|ArrayList
argument_list|<
name|FileStatus
argument_list|>
name|files
parameter_list|)
throws|throws
name|HiveException
block|{
name|Set
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
argument_list|>
name|inputFormatter
init|=
name|inputFormatCheckerMap
operator|.
name|keySet
argument_list|()
decl_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|reg
range|:
name|inputFormatter
control|)
block|{
name|boolean
name|result
init|=
name|checkInputFormat
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|reg
argument_list|,
name|files
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
condition|)
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

