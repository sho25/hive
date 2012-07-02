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
name|BufferedOutputStream
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
name|FileInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileOutputStream
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
name|commons
operator|.
name|compress
operator|.
name|archivers
operator|.
name|tar
operator|.
name|TarArchiveEntry
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
name|compress
operator|.
name|archivers
operator|.
name|tar
operator|.
name|TarArchiveOutputStream
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
name|compress
operator|.
name|compressors
operator|.
name|gzip
operator|.
name|GzipCompressorOutputStream
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
name|compress
operator|.
name|utils
operator|.
name|IOUtils
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
comment|/**    * Variant of Path.makeQualified that qualifies the input path against the default file system    * indicated by the configuration    *    * This does not require a FileSystem handle in most cases - only requires the Filesystem URI.    * This saves the cost of opening the Filesystem - which can involve RPCs - as well as cause    * errors    *    * @param path    *          path to be fully qualified    * @param conf    *          Configuration file    * @return path qualified relative to default file system    */
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
block|{
name|authority
operator|=
name|fsUri
operator|.
name|getAuthority
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|authority
operator|=
literal|""
expr_stmt|;
block|}
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
return|return
name|makePartName
argument_list|(
name|partCols
argument_list|,
name|vals
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Makes a valid partition name.    * @param partCols The partition keys' names    * @param vals The partition values    * @param defaultStr    *         The default name given to a partition value if the respective value is empty or null.    * @return An escaped, valid partition name.    */
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
parameter_list|,
name|String
name|defaultStr
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
argument_list|,
name|defaultStr
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
argument_list|,
name|defaultStr
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
comment|/**      * ASCII 01-1F are HTTP control characters that need to be escaped.      * \u000A and \u000D are \n and \r, respectively.      */
name|char
index|[]
name|clist
init|=
operator|new
name|char
index|[]
block|{
literal|'\u0001'
block|,
literal|'\u0002'
block|,
literal|'\u0003'
block|,
literal|'\u0004'
block|,
literal|'\u0005'
block|,
literal|'\u0006'
block|,
literal|'\u0007'
block|,
literal|'\u0008'
block|,
literal|'\u0009'
block|,
literal|'\n'
block|,
literal|'\u000B'
block|,
literal|'\u000C'
block|,
literal|'\r'
block|,
literal|'\u000E'
block|,
literal|'\u000F'
block|,
literal|'\u0010'
block|,
literal|'\u0011'
block|,
literal|'\u0012'
block|,
literal|'\u0013'
block|,
literal|'\u0014'
block|,
literal|'\u0015'
block|,
literal|'\u0016'
block|,
literal|'\u0017'
block|,
literal|'\u0018'
block|,
literal|'\u0019'
block|,
literal|'\u001A'
block|,
literal|'\u001B'
block|,
literal|'\u001C'
block|,
literal|'\u001D'
block|,
literal|'\u001E'
block|,
literal|'\u001F'
block|,
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
block|,
literal|'{'
block|,
literal|'['
block|,
literal|']'
block|,
literal|'^'
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
return|return
name|escapePathName
argument_list|(
name|path
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Escapes a path name.    * @param path The path to escape.    * @param defaultPath    *          The default name for the path, if the given path is empty or null.    * @return An escaped path name.    */
specifier|public
specifier|static
name|String
name|escapePathName
parameter_list|(
name|String
name|path
parameter_list|,
name|String
name|defaultPath
parameter_list|)
block|{
comment|// __HIVE_DEFAULT_NULL__ is the system default value for null and empty string.
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
if|if
condition|(
name|defaultPath
operator|==
literal|null
condition|)
block|{
comment|//previously, when path is empty or null and no default path is specified,
comment|// __HIVE_DEFAULT_PARTITION__ was the return value for escapePathName
return|return
literal|"__HIVE_DEFAULT_PARTITION__"
return|;
block|}
else|else
block|{
return|return
name|defaultPath
return|;
block|}
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
comment|/**    * Recursively lists status for all files starting from a particular directory (or individual file    * as base case).    *    * @param fs    *          file system    *    * @param fileStatus    *          starting point in file system    *    * @param results    *          receives enumeration of all files found    */
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
comment|/**    * Archive all the files in the inputFiles into outputFile    *    * @param inputFiles    * @param outputFile    * @throws IOException    */
specifier|public
specifier|static
name|void
name|tar
parameter_list|(
name|String
name|parentDir
parameter_list|,
name|String
index|[]
name|inputFiles
parameter_list|,
name|String
name|outputFile
parameter_list|)
throws|throws
name|IOException
block|{
name|FileOutputStream
name|out
init|=
literal|null
decl_stmt|;
try|try
block|{
name|out
operator|=
operator|new
name|FileOutputStream
argument_list|(
operator|new
name|File
argument_list|(
name|parentDir
argument_list|,
name|outputFile
argument_list|)
argument_list|)
expr_stmt|;
name|TarArchiveOutputStream
name|tOut
init|=
operator|new
name|TarArchiveOutputStream
argument_list|(
operator|new
name|GzipCompressorOutputStream
argument_list|(
operator|new
name|BufferedOutputStream
argument_list|(
name|out
argument_list|)
argument_list|)
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
name|inputFiles
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|File
name|f
init|=
operator|new
name|File
argument_list|(
name|parentDir
argument_list|,
name|inputFiles
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|TarArchiveEntry
name|tarEntry
init|=
operator|new
name|TarArchiveEntry
argument_list|(
name|f
argument_list|,
name|f
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|tOut
operator|.
name|setLongFileMode
argument_list|(
name|TarArchiveOutputStream
operator|.
name|LONGFILE_GNU
argument_list|)
expr_stmt|;
name|tOut
operator|.
name|putArchiveEntry
argument_list|(
name|tarEntry
argument_list|)
expr_stmt|;
name|FileInputStream
name|input
init|=
operator|new
name|FileInputStream
argument_list|(
name|f
argument_list|)
decl_stmt|;
try|try
block|{
name|IOUtils
operator|.
name|copy
argument_list|(
name|input
argument_list|,
name|tOut
argument_list|)
expr_stmt|;
comment|// copy with 8K buffer, not close
block|}
finally|finally
block|{
name|input
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|tOut
operator|.
name|closeArchiveEntry
argument_list|()
expr_stmt|;
block|}
name|tOut
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// finishes inside
block|}
finally|finally
block|{
comment|// TarArchiveOutputStream seemed not to close files properly in error situation
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|IOUtils
operator|.
name|closeStream
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

