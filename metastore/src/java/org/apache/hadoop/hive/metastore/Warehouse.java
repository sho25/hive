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
name|metastore
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
name|metastore
operator|.
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
name|ArrayList
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|LoginException
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
name|ArrayUtils
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
name|fs
operator|.
name|permission
operator|.
name|FsAction
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
name|common
operator|.
name|FileUtils
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
name|common
operator|.
name|JavaUtils
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
name|metastore
operator|.
name|api
operator|.
name|Database
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|shims
operator|.
name|ShimLoader
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
name|security
operator|.
name|UserGroupInformation
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
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * This class represents a warehouse where data of Hive tables is stored  */
end_comment

begin_class
specifier|public
class|class
name|Warehouse
block|{
specifier|private
name|Path
name|whRoot
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|String
name|whRootString
decl_stmt|;
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
literal|"hive.metastore.warehouse"
argument_list|)
decl_stmt|;
specifier|private
name|MetaStoreFS
name|fsHandler
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|storageAuthCheck
init|=
literal|false
decl_stmt|;
specifier|public
name|Warehouse
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|MetaException
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|whRootString
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
name|METASTOREWAREHOUSE
argument_list|)
expr_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|whRootString
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREWAREHOUSE
operator|.
name|varname
operator|+
literal|" is not set in the config or blank"
argument_list|)
throw|;
block|}
name|fsHandler
operator|=
name|getMetaStoreFsHandler
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|storageAuthCheck
operator|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AUTHORIZATION_STORAGE_AUTH_CHECKS
argument_list|)
expr_stmt|;
block|}
specifier|private
name|MetaStoreFS
name|getMetaStoreFsHandler
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|MetaException
block|{
name|String
name|handlerClassStr
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
name|HIVE_METASTORE_FS_HANDLER_CLS
argument_list|)
decl_stmt|;
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|MetaStoreFS
argument_list|>
name|handlerClass
init|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|MetaStoreFS
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|handlerClassStr
argument_list|,
literal|true
argument_list|,
name|JavaUtils
operator|.
name|getClassLoader
argument_list|()
argument_list|)
decl_stmt|;
name|MetaStoreFS
name|handler
init|=
operator|(
name|MetaStoreFS
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|handlerClass
argument_list|,
name|conf
argument_list|)
decl_stmt|;
return|return
name|handler
return|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Error in loading MetaStoreFS handler."
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/**    * Helper functions to convert IOException to MetaException    */
specifier|public
name|FileSystem
name|getFs
parameter_list|(
name|Path
name|f
parameter_list|)
throws|throws
name|MetaException
block|{
try|try
block|{
return|return
name|f
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|MetaStoreUtils
operator|.
name|logAndThrowMetaException
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
specifier|static
name|void
name|closeFs
parameter_list|(
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|MetaException
block|{
try|try
block|{
if|if
condition|(
name|fs
operator|!=
literal|null
condition|)
block|{
name|fs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|MetaStoreUtils
operator|.
name|logAndThrowMetaException
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Hadoop File System reverse lookups paths with raw ip addresses The File    * System URI always contains the canonical DNS name of the Namenode.    * Subsequently, operations on paths with raw ip addresses cause an exception    * since they don't match the file system URI.    *    * This routine solves this problem by replacing the scheme and authority of a    * path with the scheme and authority of the FileSystem that it maps to.    *    * @param path    *          Path to be canonicalized    * @return Path with canonical scheme and authority    */
specifier|public
name|Path
name|getDnsPath
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|MetaException
block|{
name|FileSystem
name|fs
init|=
name|getFs
argument_list|(
name|path
argument_list|)
decl_stmt|;
return|return
operator|(
operator|new
name|Path
argument_list|(
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|getScheme
argument_list|()
argument_list|,
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|getAuthority
argument_list|()
argument_list|,
name|path
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
operator|)
return|;
block|}
comment|/**    * Resolve the configured warehouse root dir with respect to the configuration    * This involves opening the FileSystem corresponding to the warehouse root    * dir (but that should be ok given that this is only called during DDL    * statements for non-external tables).    */
specifier|public
name|Path
name|getWhRoot
parameter_list|()
throws|throws
name|MetaException
block|{
if|if
condition|(
name|whRoot
operator|!=
literal|null
condition|)
block|{
return|return
name|whRoot
return|;
block|}
name|whRoot
operator|=
name|getDnsPath
argument_list|(
operator|new
name|Path
argument_list|(
name|whRootString
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|whRoot
return|;
block|}
specifier|public
name|Path
name|getTablePath
parameter_list|(
name|String
name|whRootString
parameter_list|,
name|String
name|tableName
parameter_list|)
throws|throws
name|MetaException
block|{
name|Path
name|whRoot
init|=
name|getDnsPath
argument_list|(
operator|new
name|Path
argument_list|(
name|whRootString
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|Path
argument_list|(
name|whRoot
argument_list|,
name|tableName
operator|.
name|toLowerCase
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|Path
name|getDatabasePath
parameter_list|(
name|Database
name|db
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|db
operator|.
name|getName
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|DEFAULT_DATABASE_NAME
argument_list|)
condition|)
block|{
return|return
name|getWhRoot
argument_list|()
return|;
block|}
return|return
operator|new
name|Path
argument_list|(
name|db
operator|.
name|getLocationUri
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|Path
name|getTablePath
parameter_list|(
name|Database
name|db
parameter_list|,
name|String
name|tableName
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
name|getDnsPath
argument_list|(
operator|new
name|Path
argument_list|(
name|getDatabasePath
argument_list|(
name|db
argument_list|)
argument_list|,
name|tableName
operator|.
name|toLowerCase
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|mkdirs
parameter_list|(
name|Path
name|f
parameter_list|)
throws|throws
name|MetaException
block|{
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
try|try
block|{
name|fs
operator|=
name|getFs
argument_list|(
name|f
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating directory if it doesn't exist: "
operator|+
name|f
argument_list|)
expr_stmt|;
return|return
operator|(
name|fs
operator|.
name|mkdirs
argument_list|(
name|f
argument_list|)
operator|||
name|fs
operator|.
name|getFileStatus
argument_list|(
name|f
argument_list|)
operator|.
name|isDir
argument_list|()
operator|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|closeFs
argument_list|(
name|fs
argument_list|)
expr_stmt|;
name|MetaStoreUtils
operator|.
name|logAndThrowMetaException
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|deleteDir
parameter_list|(
name|Path
name|f
parameter_list|,
name|boolean
name|recursive
parameter_list|)
throws|throws
name|MetaException
block|{
name|FileSystem
name|fs
init|=
name|getFs
argument_list|(
name|f
argument_list|)
decl_stmt|;
return|return
name|fsHandler
operator|.
name|deleteDir
argument_list|(
name|fs
argument_list|,
name|f
argument_list|,
name|recursive
argument_list|,
name|conf
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isWritable
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|storageAuthCheck
condition|)
block|{
comment|// no checks for non-secure hadoop installations
return|return
literal|true
return|;
block|}
if|if
condition|(
name|path
operator|==
literal|null
condition|)
block|{
comment|//what??!!
return|return
literal|false
return|;
block|}
specifier|final
name|FileStatus
name|stat
decl_stmt|;
try|try
block|{
name|stat
operator|=
name|getFs
argument_list|(
name|path
argument_list|)
operator|.
name|getFileStatus
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|fnfe
parameter_list|)
block|{
comment|// File named by path doesn't exist; nothing to validate.
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// all other exceptions are considered as emanating from
comment|// unauthorized accesses
return|return
literal|false
return|;
block|}
specifier|final
name|UserGroupInformation
name|ugi
decl_stmt|;
try|try
block|{
name|ugi
operator|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getUGIForConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LoginException
name|le
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|le
argument_list|)
throw|;
block|}
name|String
name|user
init|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getShortUserName
argument_list|(
name|ugi
argument_list|)
decl_stmt|;
comment|//check whether owner can delete
if|if
condition|(
name|stat
operator|.
name|getOwner
argument_list|()
operator|.
name|equals
argument_list|(
name|user
argument_list|)
operator|&&
name|stat
operator|.
name|getPermission
argument_list|()
operator|.
name|getUserAction
argument_list|()
operator|.
name|implies
argument_list|(
name|FsAction
operator|.
name|WRITE
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
comment|//check whether group of the user can delete
if|if
condition|(
name|stat
operator|.
name|getPermission
argument_list|()
operator|.
name|getGroupAction
argument_list|()
operator|.
name|implies
argument_list|(
name|FsAction
operator|.
name|WRITE
argument_list|)
condition|)
block|{
name|String
index|[]
name|groups
init|=
name|ugi
operator|.
name|getGroupNames
argument_list|()
decl_stmt|;
if|if
condition|(
name|ArrayUtils
operator|.
name|contains
argument_list|(
name|groups
argument_list|,
name|stat
operator|.
name|getGroup
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
comment|//check whether others can delete (uncommon case!!)
if|if
condition|(
name|stat
operator|.
name|getPermission
argument_list|()
operator|.
name|getOtherAction
argument_list|()
operator|.
name|implies
argument_list|(
name|FsAction
operator|.
name|WRITE
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/*   // NOTE: This is for generating the internal path name for partitions. Users   // should always use the MetaStore API to get the path name for a partition.   // Users should not directly take partition values and turn it into a path   // name by themselves, because the logic below may change in the future.   //   // In the future, it's OK to add new chars to the escape list, and old data   // won't be corrupt, because the full path name in metastore is stored.   // In that case, Hive will continue to read the old data, but when it creates   // new partitions, it will use new names.   static BitSet charToEscape = new BitSet(128);   static {     for (char c = 0; c< ' '; c++) {       charToEscape.set(c);     }     char[] clist = new char[] { '"', '#', '%', '\'', '*', '/', ':', '=', '?',         '\\', '\u00FF' };     for (char c : clist) {       charToEscape.set(c);     }   }    static boolean needsEscaping(char c) {     return c>= 0&& c< charToEscape.size()&& charToEscape.get(c);   }   */
specifier|static
name|String
name|escapePathName
parameter_list|(
name|String
name|path
parameter_list|)
block|{
return|return
name|FileUtils
operator|.
name|escapePathName
argument_list|(
name|path
argument_list|)
return|;
block|}
specifier|static
name|String
name|unescapePathName
parameter_list|(
name|String
name|path
parameter_list|)
block|{
return|return
name|FileUtils
operator|.
name|unescapePathName
argument_list|(
name|path
argument_list|)
return|;
block|}
comment|/**    * Given a partition specification, return the path corresponding to the    * partition spec. By default, the specification does not include dynamic partitions.    * @param spec    * @return string representation of the partition specification.    * @throws MetaException    */
specifier|public
specifier|static
name|String
name|makePartPath
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|spec
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
name|makePartName
argument_list|(
name|spec
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**    * A common function for constructing a partition name    * @param spec the partition spec    * @param addTrailingSeparator whether to add a trailing separator at the end    * @param isSorted whether the partition name should be sorted by key    * @return a partition name    * @throws MetaException    */
specifier|private
specifier|static
name|String
name|makePartNameCommon
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|spec
parameter_list|,
name|boolean
name|addTrailingSeparator
parameter_list|,
name|boolean
name|isSorted
parameter_list|)
throws|throws
name|MetaException
block|{
name|StringBuilder
name|suffixBuf
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|isSorted
condition|)
block|{
name|spec
operator|=
operator|new
name|TreeMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|spec
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|e
range|:
name|spec
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|e
operator|.
name|getValue
argument_list|()
operator|==
literal|null
operator|||
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Partition spec is incorrect. "
operator|+
name|spec
argument_list|)
throw|;
block|}
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|suffixBuf
operator|.
name|append
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
expr_stmt|;
block|}
name|suffixBuf
operator|.
name|append
argument_list|(
name|escapePathName
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|suffixBuf
operator|.
name|append
argument_list|(
literal|'='
argument_list|)
expr_stmt|;
name|suffixBuf
operator|.
name|append
argument_list|(
name|escapePathName
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|addTrailingSeparator
condition|)
block|{
name|suffixBuf
operator|.
name|append
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
expr_stmt|;
block|}
return|return
name|suffixBuf
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Makes a partition name from a specification    * @param spec    * @param addTrailingSeperator if true, adds a trailing separator e.g. 'ds=1/'    * @return    * @throws MetaException    */
specifier|public
specifier|static
name|String
name|makePartName
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|spec
parameter_list|,
name|boolean
name|addTrailingSeperator
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
name|makePartNameCommon
argument_list|(
name|spec
argument_list|,
name|addTrailingSeperator
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Makes a partition name from a specification.  The keys/value pairs in the partition    * name are sorted by key name.  E.g., created=4/ds=1/hr=3.  Sorting the keys is useful    * for comparison of partition names.    * @param spec the partition spec    * @param addTrailingSeparator whether to add a trailing separator at the end of the name    * @return a partition name in which the keys are sorted    * @throws MetaException    */
specifier|public
specifier|static
name|String
name|makeSortedPartName
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|spec
parameter_list|,
name|boolean
name|addTrailingSeparator
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
name|makePartNameCommon
argument_list|(
name|spec
argument_list|,
name|addTrailingSeparator
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**    * Given a dynamic partition specification, return the path corresponding to the    * static part of partition specification. This is basically a copy of makePartName    * but we get rid of MetaException since it is not serializable.    * @param spec    * @return string representation of the static part of the partition specification.    */
specifier|public
specifier|static
name|String
name|makeDynamicPartName
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|spec
parameter_list|)
block|{
name|StringBuilder
name|suffixBuf
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|e
range|:
name|spec
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|e
operator|.
name|getValue
argument_list|()
operator|!=
literal|null
operator|&&
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|suffixBuf
operator|.
name|append
argument_list|(
name|escapePathName
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|suffixBuf
operator|.
name|append
argument_list|(
literal|'='
argument_list|)
expr_stmt|;
name|suffixBuf
operator|.
name|append
argument_list|(
name|escapePathName
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|suffixBuf
operator|.
name|append
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// stop once we see a dynamic partition
break|break;
block|}
block|}
return|return
name|suffixBuf
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|static
specifier|final
name|Pattern
name|pat
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"([^/]+)=([^/]+)"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|makeSpecFromName
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|name
operator|==
literal|null
operator|||
name|name
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Partition name is invalid. "
operator|+
name|name
argument_list|)
throw|;
block|}
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|makeSpecFromName
argument_list|(
name|partSpec
argument_list|,
operator|new
name|Path
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|partSpec
return|;
block|}
specifier|public
specifier|static
name|void
name|makeSpecFromName
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|,
name|Path
name|currPath
parameter_list|)
block|{
name|List
argument_list|<
name|String
index|[]
argument_list|>
name|kvs
init|=
operator|new
name|ArrayList
argument_list|<
name|String
index|[]
argument_list|>
argument_list|()
decl_stmt|;
do|do
block|{
name|String
name|component
init|=
name|currPath
operator|.
name|getName
argument_list|()
decl_stmt|;
name|Matcher
name|m
init|=
name|pat
operator|.
name|matcher
argument_list|(
name|component
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|.
name|matches
argument_list|()
condition|)
block|{
name|String
name|k
init|=
name|unescapePathName
argument_list|(
name|m
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|v
init|=
name|unescapePathName
argument_list|(
name|m
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|)
decl_stmt|;
name|String
index|[]
name|kv
init|=
operator|new
name|String
index|[
literal|2
index|]
decl_stmt|;
name|kv
index|[
literal|0
index|]
operator|=
name|k
expr_stmt|;
name|kv
index|[
literal|1
index|]
operator|=
name|v
expr_stmt|;
name|kvs
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
name|currPath
operator|=
name|currPath
operator|.
name|getParent
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|currPath
operator|!=
literal|null
operator|&&
operator|!
name|currPath
operator|.
name|getName
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
do|;
comment|// reverse the list since we checked the part from leaf dir to table's base dir
for|for
control|(
name|int
name|i
init|=
name|kvs
operator|.
name|size
argument_list|()
init|;
name|i
operator|>
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|partSpec
operator|.
name|put
argument_list|(
name|kvs
operator|.
name|get
argument_list|(
name|i
operator|-
literal|1
argument_list|)
index|[
literal|0
index|]
argument_list|,
name|kvs
operator|.
name|get
argument_list|(
name|i
operator|-
literal|1
argument_list|)
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Path
name|getPartitionPath
parameter_list|(
name|Database
name|db
parameter_list|,
name|String
name|tableName
parameter_list|,
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|pm
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
operator|new
name|Path
argument_list|(
name|getTablePath
argument_list|(
name|db
argument_list|,
name|tableName
argument_list|)
argument_list|,
name|makePartPath
argument_list|(
name|pm
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|Path
name|getPartitionPath
parameter_list|(
name|Path
name|tblPath
parameter_list|,
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|pm
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
operator|new
name|Path
argument_list|(
name|tblPath
argument_list|,
name|makePartPath
argument_list|(
name|pm
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isDir
parameter_list|(
name|Path
name|f
parameter_list|)
throws|throws
name|MetaException
block|{
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
try|try
block|{
name|fs
operator|=
name|getFs
argument_list|(
name|f
argument_list|)
expr_stmt|;
name|FileStatus
name|fstatus
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|f
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fstatus
operator|.
name|isDir
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|closeFs
argument_list|(
name|fs
argument_list|)
expr_stmt|;
name|MetaStoreUtils
operator|.
name|logAndThrowMetaException
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
specifier|public
specifier|static
name|String
name|makePartName
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|vals
parameter_list|)
throws|throws
name|MetaException
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
comment|/**    * Makes a valid partition name.    * @param partCols The partition columns    * @param vals The partition values    * @param defaultStr    *    The default name given to a partition value if the respective value is empty or null.    * @return An escaped, valid partition name.    * @throws MetaException    */
specifier|public
specifier|static
name|String
name|makePartName
parameter_list|(
name|List
argument_list|<
name|FieldSchema
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
throws|throws
name|MetaException
block|{
if|if
condition|(
operator|(
name|partCols
operator|.
name|size
argument_list|()
operator|!=
name|vals
operator|.
name|size
argument_list|()
operator|)
operator|||
operator|(
name|partCols
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|)
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Invalid partition key& values"
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|colNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FieldSchema
name|col
range|:
name|partCols
control|)
block|{
name|colNames
operator|.
name|add
argument_list|(
name|col
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|FileUtils
operator|.
name|makePartName
argument_list|(
name|colNames
argument_list|,
name|vals
argument_list|,
name|defaultStr
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|getPartValuesFromPartName
parameter_list|(
name|String
name|partName
parameter_list|)
throws|throws
name|MetaException
block|{
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
name|Warehouse
operator|.
name|makeSpecFromName
argument_list|(
name|partName
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|values
operator|.
name|addAll
argument_list|(
name|partSpec
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|values
return|;
block|}
block|}
end_class

end_unit

