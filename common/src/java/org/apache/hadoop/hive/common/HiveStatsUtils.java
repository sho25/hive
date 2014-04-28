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
name|common
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
name|List
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

begin_comment
comment|/**  * HiveStatsUtils.  * A collection of utilities used for hive statistics.  * Used by classes in both metastore and ql package  */
end_comment

begin_class
specifier|public
class|class
name|HiveStatsUtils
block|{
comment|/**    * Get all file status from a root path and recursively go deep into certain levels.    *    * @param path    *          the root path    * @param level    *          the depth of directory to explore    * @param fs    *          the file system    * @return array of FileStatus    * @throws IOException    */
specifier|public
specifier|static
name|FileStatus
index|[]
name|getFileStatusRecurse
parameter_list|(
name|Path
name|path
parameter_list|,
name|int
name|level
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
comment|// if level is<0, the return all files/directories under the specified path
if|if
condition|(
name|level
operator|<
literal|0
condition|)
block|{
name|List
argument_list|<
name|FileStatus
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|FileStatus
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
name|FileStatus
name|fileStatus
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|FileUtils
operator|.
name|listStatusRecursively
argument_list|(
name|fs
argument_list|,
name|fileStatus
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// globStatus() API returns empty FileStatus[] when the specified path
comment|// does not exist. But getFileStatus() throw IOException. To mimic the
comment|// similar behavior we will return empty array on exception. For external
comment|// tables, the path of the table will not exists during table creation
return|return
operator|new
name|FileStatus
index|[
literal|0
index|]
return|;
block|}
return|return
name|result
operator|.
name|toArray
argument_list|(
operator|new
name|FileStatus
index|[
name|result
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
comment|// construct a path pattern (e.g., /*/*) to find all dynamically generated paths
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
name|path
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
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
name|level
condition|;
name|i
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
operator|.
name|append
argument_list|(
literal|"*"
argument_list|)
expr_stmt|;
block|}
name|Path
name|pathPattern
init|=
operator|new
name|Path
argument_list|(
name|path
argument_list|,
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|fs
operator|.
name|globStatus
argument_list|(
name|pathPattern
argument_list|)
return|;
block|}
block|}
end_class

end_unit

