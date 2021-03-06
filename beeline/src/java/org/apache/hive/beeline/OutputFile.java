begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/*  * This source file is based on code taken from SQLLine 1.0.2  * See SQLLine notice in LICENSE  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|beeline
package|;
end_package

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
name|java
operator|.
name|io
operator|.
name|File
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
name|io
operator|.
name|PrintStream
import|;
end_import

begin_class
specifier|public
class|class
name|OutputFile
block|{
specifier|private
specifier|final
name|PrintStream
name|out
decl_stmt|;
specifier|private
specifier|final
name|String
name|filename
decl_stmt|;
specifier|public
name|OutputFile
parameter_list|(
name|String
name|filename
parameter_list|)
throws|throws
name|IOException
block|{
name|File
name|file
init|=
operator|new
name|File
argument_list|(
name|filename
argument_list|)
decl_stmt|;
name|this
operator|.
name|filename
operator|=
name|file
operator|.
name|getAbsolutePath
argument_list|()
expr_stmt|;
name|this
operator|.
name|out
operator|=
operator|new
name|PrintStream
argument_list|(
name|file
argument_list|,
literal|"UTF-8"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|protected
name|PrintStream
name|getOut
parameter_list|()
block|{
return|return
name|out
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|protected
name|String
name|getFilename
parameter_list|()
block|{
return|return
name|filename
return|;
block|}
comment|/**    * Constructor used by the decorating classes in tests.    * @param out The output stream    * @param filename The filename, to use in the toString() method    */
annotation|@
name|VisibleForTesting
specifier|protected
name|OutputFile
parameter_list|(
name|PrintStream
name|out
parameter_list|,
name|String
name|filename
parameter_list|)
block|{
name|this
operator|.
name|out
operator|=
name|out
expr_stmt|;
name|this
operator|.
name|filename
operator|=
name|filename
expr_stmt|;
block|}
comment|/**    * Returns true if a FetchConverter is defined for writing the results. Should be used only for    * testing, otherwise returns false.    * @return True if a FetchConverter is active    */
name|boolean
name|isActiveConverter
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**    * Indicates that result fetching is started, and the converter should be activated. The    * Converter starts to collect the data when the fetch is started, and prints out the    * converted data when the fetch is finished. Converter will collect data only if    * fetchStarted, and foundQuery is true.    */
name|void
name|fetchStarted
parameter_list|()
block|{
comment|// no-op for default output file
block|}
comment|/**    * Indicates that the following data will be a query result, and the converter should be    * activated. Converter will collect the data only if fetchStarted, and foundQuery is true.    * @param foundQuery The following data will be a query result (true) or not (false)    */
name|void
name|foundQuery
parameter_list|(
name|boolean
name|foundQuery
parameter_list|)
block|{
comment|// no-op for default output file
block|}
comment|/**    * Indicates that the previously collected data should be converted and written. Converter    * starts to collect the data when the fetch is started, and prints out the converted data when    * the fetch is finished.    */
name|void
name|fetchFinished
parameter_list|()
block|{
comment|// no-op for default output file
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|filename
return|;
block|}
specifier|public
name|void
name|addLine
parameter_list|(
name|String
name|command
parameter_list|)
block|{
name|out
operator|.
name|println
argument_list|(
name|command
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|println
parameter_list|(
name|String
name|command
parameter_list|)
block|{
name|out
operator|.
name|println
argument_list|(
name|command
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|print
parameter_list|(
name|String
name|command
parameter_list|)
block|{
name|out
operator|.
name|print
argument_list|(
name|command
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

