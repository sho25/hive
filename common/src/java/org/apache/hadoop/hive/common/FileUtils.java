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
name|net
operator|.
name|URI
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
name|BitSet
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

begin_comment
comment|/**  * Collection of file manipulation utilities common across Hive.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|FileUtils
block|{
comment|/**    * Variant of Path.makeQualified that qualifies the input path against the    * default file system indicated by the configuration    *    * This does not require a FileSystem handle in most cases - only requires the    * Filesystem URI. This saves the cost of opening the Filesystem - which can    * involve RPCs - as well as cause errors    *    * @param path    *          path to be fully qualified    * @param conf    *          Configuration file    * @return path qualified relative to default file system    */
specifier|public
specifier|static
name|Path
name|makeQualified
parameter_list|(
name|Path
name|path
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|path
operator|.
name|isAbsolute
argument_list|()
condition|)
block|{
comment|// in this case we need to get the working directory
comment|// and this requires a FileSystem handle. So revert to
comment|// original method.
return|return
name|path
operator|.
name|makeQualified
argument_list|(
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
argument_list|)
return|;
block|}
name|URI
name|fsUri
init|=
name|FileSystem
operator|.
name|getDefaultUri
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|URI
name|pathUri
init|=
name|path
operator|.
name|toUri
argument_list|()
decl_stmt|;
name|String
name|scheme
init|=
name|pathUri
operator|.
name|getScheme
argument_list|()
decl_stmt|;
name|String
name|authority
init|=
name|pathUri
operator|.
name|getAuthority
argument_list|()
decl_stmt|;
comment|// validate/fill-in scheme and authority. this follows logic
comment|// identical to FileSystem.get(URI, conf) - but doesn't actually
comment|// obtain a file system handle
if|if
condition|(
name|scheme
operator|==
literal|null
condition|)
block|{
comment|// no scheme - use default file system uri
name|scheme
operator|=
name|fsUri
operator|.
name|getScheme
argument_list|()
expr_stmt|;
name|authority
operator|=
name|fsUri
operator|.
name|getAuthority
argument_list|()
expr_stmt|;
if|if
condition|(
name|authority
operator|==
literal|null
condition|)
block|{
name|authority
operator|=
literal|""
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|authority
operator|==
literal|null
condition|)
block|{
comment|// no authority - use default one if it applies
if|if
condition|(
name|scheme
operator|.
name|equals
argument_list|(
name|fsUri
operator|.
name|getScheme
argument_list|()
argument_list|)
operator|&&
name|fsUri
operator|.
name|getAuthority
argument_list|()
operator|!=
literal|null
condition|)
name|authority
operator|=
name|fsUri
operator|.
name|getAuthority
argument_list|()
expr_stmt|;
else|else
name|authority
operator|=
literal|""
expr_stmt|;
block|}
block|}
return|return
operator|new
name|Path
argument_list|(
name|scheme
argument_list|,
name|authority
argument_list|,
name|pathUri
operator|.
name|getPath
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|FileUtils
parameter_list|()
block|{
comment|// prevent instantiation
block|}
specifier|public
specifier|static
name|String
name|makePartName
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|partCols
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|vals
parameter_list|)
block|{
name|StringBuilder
name|name
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
name|partCols
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
operator|>
literal|0
condition|)
block|{
name|name
operator|.
name|append
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
expr_stmt|;
block|}
name|name
operator|.
name|append
argument_list|(
name|escapePathName
argument_list|(
operator|(
name|partCols
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|)
operator|.
name|toLowerCase
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|name
operator|.
name|append
argument_list|(
literal|'='
argument_list|)
expr_stmt|;
name|name
operator|.
name|append
argument_list|(
name|escapePathName
argument_list|(
name|vals
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|name
operator|.
name|toString
argument_list|()
return|;
block|}
comment|// NOTE: This is for generating the internal path name for partitions. Users
comment|// should always use the MetaStore API to get the path name for a partition.
comment|// Users should not directly take partition values and turn it into a path
comment|// name by themselves, because the logic below may change in the future.
comment|//
comment|// In the future, it's OK to add new chars to the escape list, and old data
comment|// won't be corrupt, because the full path name in metastore is stored.
comment|// In that case, Hive will continue to read the old data, but when it creates
comment|// new partitions, it will use new names.
specifier|static
name|BitSet
name|charToEscape
init|=
operator|new
name|BitSet
argument_list|(
literal|128
argument_list|)
decl_stmt|;
static|static
block|{
for|for
control|(
name|char
name|c
init|=
literal|0
init|;
name|c
operator|<
literal|' '
condition|;
name|c
operator|++
control|)
block|{
name|charToEscape
operator|.
name|set
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
name|char
index|[]
name|clist
init|=
operator|new
name|char
index|[]
block|{
literal|'"'
block|,
literal|'#'
block|,
literal|'%'
block|,
literal|'\''
block|,
literal|'*'
block|,
literal|'/'
block|,
literal|':'
block|,
literal|'='
block|,
literal|'?'
block|,
literal|'\\'
block|,
literal|'\u007F'
block|}
decl_stmt|;
for|for
control|(
name|char
name|c
range|:
name|clist
control|)
block|{
name|charToEscape
operator|.
name|set
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
name|boolean
name|needsEscaping
parameter_list|(
name|char
name|c
parameter_list|)
block|{
return|return
name|c
operator|>=
literal|0
operator|&&
name|c
operator|<
name|charToEscape
operator|.
name|size
argument_list|()
operator|&&
name|charToEscape
operator|.
name|get
argument_list|(
name|c
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|escapePathName
parameter_list|(
name|String
name|path
parameter_list|)
block|{
comment|// __HIVE_DEFAULT_NULL__ is the system default value for null and empty string. We should
comment|// TODO: we should allow user to specify default partition or HDFS file location.
if|if
condition|(
name|path
operator|==
literal|null
operator|||
name|path
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|"__HIVE_DEFAULT_PARTITION__"
return|;
block|}
name|StringBuilder
name|sb
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
name|path
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|char
name|c
init|=
name|path
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|needsEscaping
argument_list|(
name|c
argument_list|)
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|'%'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%1$02X"
argument_list|,
operator|(
name|int
operator|)
name|c
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|String
name|unescapePathName
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|StringBuilder
name|sb
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
name|path
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|char
name|c
init|=
name|path
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|==
literal|'%'
operator|&&
name|i
operator|+
literal|2
operator|<
name|path
operator|.
name|length
argument_list|()
condition|)
block|{
name|int
name|code
init|=
operator|-
literal|1
decl_stmt|;
try|try
block|{
name|code
operator|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|path
operator|.
name|substring
argument_list|(
name|i
operator|+
literal|1
argument_list|,
name|i
operator|+
literal|3
argument_list|)
argument_list|,
literal|16
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|code
operator|=
operator|-
literal|1
expr_stmt|;
block|}
if|if
condition|(
name|code
operator|>=
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
operator|(
name|char
operator|)
name|code
argument_list|)
expr_stmt|;
name|i
operator|+=
literal|2
expr_stmt|;
continue|continue;
block|}
block|}
name|sb
operator|.
name|append
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Recursively lists status for all files starting from a particular    * directory (or individual file as base case).    *    * @param fs file system    *    * @param fileStatus starting point in file system    *    * @param results receives enumeration of all files found    */
specifier|public
specifier|static
name|void
name|listStatusRecursively
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|FileStatus
name|fileStatus
parameter_list|,
name|List
argument_list|<
name|FileStatus
argument_list|>
name|results
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|fileStatus
operator|.
name|isDir
argument_list|()
condition|)
block|{
for|for
control|(
name|FileStatus
name|stat
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|fileStatus
operator|.
name|getPath
argument_list|()
argument_list|)
control|)
block|{
name|listStatusRecursively
argument_list|(
name|fs
argument_list|,
name|stat
argument_list|,
name|results
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|results
operator|.
name|add
argument_list|(
name|fileStatus
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

