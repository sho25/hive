begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations under  * the License.  */
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
name|accumulo
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLDecoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|MessageFormat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Enumeration
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
name|HashSet
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
name|java
operator|.
name|util
operator|.
name|jar
operator|.
name|JarFile
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|jar
operator|.
name|JarOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|jar
operator|.
name|Manifest
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|zip
operator|.
name|ZipEntry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|zip
operator|.
name|ZipFile
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|zip
operator|.
name|ZipOutputStream
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
name|util
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
name|log4j
operator|.
name|Logger
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
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * Accumulo doesn't have a TableMapReduceUtil.addDependencyJars method like HBase which is very  * helpful  */
end_comment

begin_class
specifier|public
class|class
name|Utils
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|log
init|=
name|Logger
operator|.
name|getLogger
argument_list|(
name|Utils
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Thanks, HBase
specifier|public
specifier|static
name|void
name|addDependencyJars
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
modifier|...
name|classes
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|localFs
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|jars
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// Add jars that are already in the tmpjars variable
name|jars
operator|.
name|addAll
argument_list|(
name|conf
operator|.
name|getStringCollection
argument_list|(
literal|"tmpjars"
argument_list|)
argument_list|)
expr_stmt|;
comment|// add jars as we find them to a map of contents jar name so that we can
comment|// avoid
comment|// creating new jars for classes that have already been packaged.
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|packagedClasses
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// Add jars containing the specified classes
for|for
control|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
range|:
name|classes
control|)
block|{
if|if
condition|(
name|clazz
operator|==
literal|null
condition|)
continue|continue;
name|Path
name|path
init|=
name|findOrCreateJar
argument_list|(
name|clazz
argument_list|,
name|localFs
argument_list|,
name|packagedClasses
argument_list|)
decl_stmt|;
if|if
condition|(
name|path
operator|==
literal|null
condition|)
block|{
name|log
operator|.
name|warn
argument_list|(
literal|"Could not find jar for class "
operator|+
name|clazz
operator|+
literal|" in order to ship it to the cluster."
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
operator|!
name|localFs
operator|.
name|exists
argument_list|(
name|path
argument_list|)
condition|)
block|{
name|log
operator|.
name|warn
argument_list|(
literal|"Could not validate jar file "
operator|+
name|path
operator|+
literal|" for class "
operator|+
name|clazz
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|jars
operator|.
name|add
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|jars
operator|.
name|isEmpty
argument_list|()
condition|)
return|return;
name|conf
operator|.
name|set
argument_list|(
literal|"tmpjars"
argument_list|,
name|StringUtils
operator|.
name|arrayToString
argument_list|(
name|jars
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|jars
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * If org.apache.hadoop.util.JarFinder is available (0.23+ hadoop), finds the Jar for a class or    * creates it if it doesn't exist. If the class is in a directory in the classpath, it creates a    * Jar on the fly with the contents of the directory and returns the path to that Jar. If a Jar is    * created, it is created in the system temporary directory. Otherwise, returns an existing jar    * that contains a class of the same name. Maintains a mapping from jar contents to the tmp jar    * created.    *    * @param my_class    *          the class to find.    * @param fs    *          the FileSystem with which to qualify the returned path.    * @param packagedClasses    *          a map of class name to path.    * @return a jar file that contains the class.    * @throws IOException    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|private
specifier|static
name|Path
name|findOrCreateJar
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|my_class
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|packagedClasses
parameter_list|)
throws|throws
name|IOException
block|{
comment|// attempt to locate an existing jar for the class.
name|String
name|jar
init|=
name|findContainingJar
argument_list|(
name|my_class
argument_list|,
name|packagedClasses
argument_list|)
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|jar
operator|||
name|jar
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|jar
operator|=
name|getJar
argument_list|(
name|my_class
argument_list|)
expr_stmt|;
name|updateMap
argument_list|(
name|jar
argument_list|,
name|packagedClasses
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
literal|null
operator|==
name|jar
operator|||
name|jar
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|log
operator|.
name|debug
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"For class %s, using jar %s"
argument_list|,
name|my_class
operator|.
name|getName
argument_list|()
argument_list|,
name|jar
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|Path
argument_list|(
name|jar
argument_list|)
operator|.
name|makeQualified
argument_list|(
name|fs
argument_list|)
return|;
block|}
comment|/**    * Add entries to<code>packagedClasses</code> corresponding to class files contained in    *<code>jar</code>.    *    * @param jar    *          The jar who's content to list.    * @param packagedClasses    *          map[class -> jar]    */
specifier|private
specifier|static
name|void
name|updateMap
parameter_list|(
name|String
name|jar
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|packagedClasses
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
literal|null
operator|==
name|jar
operator|||
name|jar
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
name|ZipFile
name|zip
init|=
literal|null
decl_stmt|;
try|try
block|{
name|zip
operator|=
operator|new
name|ZipFile
argument_list|(
name|jar
argument_list|)
expr_stmt|;
for|for
control|(
name|Enumeration
argument_list|<
name|?
extends|extends
name|ZipEntry
argument_list|>
name|iter
init|=
name|zip
operator|.
name|entries
argument_list|()
init|;
name|iter
operator|.
name|hasMoreElements
argument_list|()
condition|;
control|)
block|{
name|ZipEntry
name|entry
init|=
name|iter
operator|.
name|nextElement
argument_list|()
decl_stmt|;
if|if
condition|(
name|entry
operator|.
name|getName
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|"class"
argument_list|)
condition|)
block|{
name|packagedClasses
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getName
argument_list|()
argument_list|,
name|jar
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
if|if
condition|(
literal|null
operator|!=
name|zip
condition|)
name|zip
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Find a jar that contains a class of the same name, if any. It will return a jar file, even if    * that is not the first thing on the class path that has a class with the same name. Looks first    * on the classpath and then in the<code>packagedClasses</code> map.    *    * @param my_class    *          the class to find.    * @return a jar file that contains the class, or null.    * @throws IOException    */
specifier|private
specifier|static
name|String
name|findContainingJar
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|my_class
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|packagedClasses
parameter_list|)
throws|throws
name|IOException
block|{
name|ClassLoader
name|loader
init|=
name|my_class
operator|.
name|getClassLoader
argument_list|()
decl_stmt|;
name|String
name|class_file
init|=
name|my_class
operator|.
name|getName
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"\\."
argument_list|,
literal|"/"
argument_list|)
operator|+
literal|".class"
decl_stmt|;
comment|// first search the classpath
for|for
control|(
name|Enumeration
argument_list|<
name|URL
argument_list|>
name|itr
init|=
name|loader
operator|.
name|getResources
argument_list|(
name|class_file
argument_list|)
init|;
name|itr
operator|.
name|hasMoreElements
argument_list|()
condition|;
control|)
block|{
name|URL
name|url
init|=
name|itr
operator|.
name|nextElement
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"jar"
operator|.
name|equals
argument_list|(
name|url
operator|.
name|getProtocol
argument_list|()
argument_list|)
condition|)
block|{
name|String
name|toReturn
init|=
name|url
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|toReturn
operator|.
name|startsWith
argument_list|(
literal|"file:"
argument_list|)
condition|)
block|{
name|toReturn
operator|=
name|toReturn
operator|.
name|substring
argument_list|(
literal|"file:"
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// URLDecoder is a misnamed class, since it actually decodes
comment|// x-www-form-urlencoded MIME type rather than actual
comment|// URL encoding (which the file path has). Therefore it would
comment|// decode +s to ' 's which is incorrect (spaces are actually
comment|// either unencoded or encoded as "%20"). Replace +s first, so
comment|// that they are kept sacred during the decoding process.
name|toReturn
operator|=
name|toReturn
operator|.
name|replaceAll
argument_list|(
literal|"\\+"
argument_list|,
literal|"%2B"
argument_list|)
expr_stmt|;
name|toReturn
operator|=
name|URLDecoder
operator|.
name|decode
argument_list|(
name|toReturn
argument_list|,
literal|"UTF-8"
argument_list|)
expr_stmt|;
return|return
name|toReturn
operator|.
name|replaceAll
argument_list|(
literal|"!.*$"
argument_list|,
literal|""
argument_list|)
return|;
block|}
block|}
comment|// now look in any jars we've packaged using JarFinder. Returns null
comment|// when
comment|// no jar is found.
return|return
name|packagedClasses
operator|.
name|get
argument_list|(
name|class_file
argument_list|)
return|;
block|}
comment|/**    * Invoke 'getJar' on a JarFinder implementation. Useful for some job configuration contexts    * (HBASE-8140) and also for testing on MRv2. First check if we have HADOOP-9426. Lacking that,    * fall back to the backport.    *    * @param my_class    *          the class to find.    * @return a jar file that contains the class, or null.    */
specifier|private
specifier|static
name|String
name|getJar
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|my_class
parameter_list|)
block|{
name|String
name|ret
init|=
literal|null
decl_stmt|;
name|String
name|hadoopJarFinder
init|=
literal|"org.apache.hadoop.util.JarFinder"
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|jarFinder
init|=
literal|null
decl_stmt|;
try|try
block|{
name|log
operator|.
name|debug
argument_list|(
literal|"Looking for "
operator|+
name|hadoopJarFinder
operator|+
literal|"."
argument_list|)
expr_stmt|;
name|jarFinder
operator|=
name|Class
operator|.
name|forName
argument_list|(
name|hadoopJarFinder
argument_list|)
expr_stmt|;
name|log
operator|.
name|debug
argument_list|(
name|hadoopJarFinder
operator|+
literal|" found."
argument_list|)
expr_stmt|;
name|Method
name|getJar
init|=
name|jarFinder
operator|.
name|getMethod
argument_list|(
literal|"getJar"
argument_list|,
name|Class
operator|.
name|class
argument_list|)
decl_stmt|;
name|ret
operator|=
operator|(
name|String
operator|)
name|getJar
operator|.
name|invoke
argument_list|(
literal|null
argument_list|,
name|my_class
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
name|log
operator|.
name|debug
argument_list|(
literal|"Using backported JarFinder."
argument_list|)
expr_stmt|;
name|ret
operator|=
name|jarFinderGetJar
argument_list|(
name|my_class
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|e
parameter_list|)
block|{
comment|// function was properly called, but threw it's own exception.
comment|// Unwrap it
comment|// and pass it on.
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// toss all other exceptions, related to reflection failure
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"getJar invocation failed."
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|ret
return|;
block|}
comment|/**    * Returns the full path to the Jar containing the class. It always return a JAR.    *    * @param klass    *          class.    *    * @return path to the Jar containing the class.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
specifier|public
specifier|static
name|String
name|jarFinderGetJar
parameter_list|(
name|Class
name|klass
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|klass
argument_list|,
literal|"klass"
argument_list|)
expr_stmt|;
name|ClassLoader
name|loader
init|=
name|klass
operator|.
name|getClassLoader
argument_list|()
decl_stmt|;
if|if
condition|(
name|loader
operator|!=
literal|null
condition|)
block|{
name|String
name|class_file
init|=
name|klass
operator|.
name|getName
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"\\."
argument_list|,
literal|"/"
argument_list|)
operator|+
literal|".class"
decl_stmt|;
try|try
block|{
for|for
control|(
name|Enumeration
name|itr
init|=
name|loader
operator|.
name|getResources
argument_list|(
name|class_file
argument_list|)
init|;
name|itr
operator|.
name|hasMoreElements
argument_list|()
condition|;
control|)
block|{
name|URL
name|url
init|=
operator|(
name|URL
operator|)
name|itr
operator|.
name|nextElement
argument_list|()
decl_stmt|;
name|String
name|path
init|=
name|url
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|path
operator|.
name|startsWith
argument_list|(
literal|"file:"
argument_list|)
condition|)
block|{
name|path
operator|=
name|path
operator|.
name|substring
argument_list|(
literal|"file:"
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|path
operator|=
name|URLDecoder
operator|.
name|decode
argument_list|(
name|path
argument_list|,
literal|"UTF-8"
argument_list|)
expr_stmt|;
if|if
condition|(
literal|"jar"
operator|.
name|equals
argument_list|(
name|url
operator|.
name|getProtocol
argument_list|()
argument_list|)
condition|)
block|{
name|path
operator|=
name|URLDecoder
operator|.
name|decode
argument_list|(
name|path
argument_list|,
literal|"UTF-8"
argument_list|)
expr_stmt|;
return|return
name|path
operator|.
name|replaceAll
argument_list|(
literal|"!.*$"
argument_list|,
literal|""
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"file"
operator|.
name|equals
argument_list|(
name|url
operator|.
name|getProtocol
argument_list|()
argument_list|)
condition|)
block|{
name|String
name|klassName
init|=
name|klass
operator|.
name|getName
argument_list|()
decl_stmt|;
name|klassName
operator|=
name|klassName
operator|.
name|replace
argument_list|(
literal|"."
argument_list|,
literal|"/"
argument_list|)
operator|+
literal|".class"
expr_stmt|;
name|path
operator|=
name|path
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|path
operator|.
name|length
argument_list|()
operator|-
name|klassName
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
name|File
name|baseDir
init|=
operator|new
name|File
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|File
name|testDir
init|=
operator|new
name|File
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.build.dir"
argument_list|,
literal|"target/test-dir"
argument_list|)
argument_list|)
decl_stmt|;
name|testDir
operator|=
name|testDir
operator|.
name|getAbsoluteFile
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|testDir
operator|.
name|exists
argument_list|()
condition|)
block|{
name|testDir
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
block|}
name|File
name|tempJar
init|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"hadoop-"
argument_list|,
literal|""
argument_list|,
name|testDir
argument_list|)
decl_stmt|;
name|tempJar
operator|=
operator|new
name|File
argument_list|(
name|tempJar
operator|.
name|getAbsolutePath
argument_list|()
operator|+
literal|".jar"
argument_list|)
expr_stmt|;
name|createJar
argument_list|(
name|baseDir
argument_list|,
name|tempJar
argument_list|)
expr_stmt|;
return|return
name|tempJar
operator|.
name|getAbsolutePath
argument_list|()
return|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
specifier|static
name|void
name|copyToZipStream
parameter_list|(
name|InputStream
name|is
parameter_list|,
name|ZipEntry
name|entry
parameter_list|,
name|ZipOutputStream
name|zos
parameter_list|)
throws|throws
name|IOException
block|{
name|zos
operator|.
name|putNextEntry
argument_list|(
name|entry
argument_list|)
expr_stmt|;
name|byte
index|[]
name|arr
init|=
operator|new
name|byte
index|[
literal|4096
index|]
decl_stmt|;
name|int
name|read
init|=
name|is
operator|.
name|read
argument_list|(
name|arr
argument_list|)
decl_stmt|;
while|while
condition|(
name|read
operator|>
operator|-
literal|1
condition|)
block|{
name|zos
operator|.
name|write
argument_list|(
name|arr
argument_list|,
literal|0
argument_list|,
name|read
argument_list|)
expr_stmt|;
name|read
operator|=
name|is
operator|.
name|read
argument_list|(
name|arr
argument_list|)
expr_stmt|;
block|}
name|is
operator|.
name|close
argument_list|()
expr_stmt|;
name|zos
operator|.
name|closeEntry
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|jarDir
parameter_list|(
name|File
name|dir
parameter_list|,
name|String
name|relativePath
parameter_list|,
name|ZipOutputStream
name|zos
parameter_list|)
throws|throws
name|IOException
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|relativePath
argument_list|,
literal|"relativePath"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|zos
argument_list|,
literal|"zos"
argument_list|)
expr_stmt|;
comment|// by JAR spec, if there is a manifest, it must be the first entry in
comment|// the
comment|// ZIP.
name|File
name|manifestFile
init|=
operator|new
name|File
argument_list|(
name|dir
argument_list|,
name|JarFile
operator|.
name|MANIFEST_NAME
argument_list|)
decl_stmt|;
name|ZipEntry
name|manifestEntry
init|=
operator|new
name|ZipEntry
argument_list|(
name|JarFile
operator|.
name|MANIFEST_NAME
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|manifestFile
operator|.
name|exists
argument_list|()
condition|)
block|{
name|zos
operator|.
name|putNextEntry
argument_list|(
name|manifestEntry
argument_list|)
expr_stmt|;
operator|new
name|Manifest
argument_list|()
operator|.
name|write
argument_list|(
operator|new
name|BufferedOutputStream
argument_list|(
name|zos
argument_list|)
argument_list|)
expr_stmt|;
name|zos
operator|.
name|closeEntry
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|InputStream
name|is
init|=
operator|new
name|FileInputStream
argument_list|(
name|manifestFile
argument_list|)
decl_stmt|;
name|copyToZipStream
argument_list|(
name|is
argument_list|,
name|manifestEntry
argument_list|,
name|zos
argument_list|)
expr_stmt|;
block|}
name|zos
operator|.
name|closeEntry
argument_list|()
expr_stmt|;
name|zipDir
argument_list|(
name|dir
argument_list|,
name|relativePath
argument_list|,
name|zos
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|zos
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|zipDir
parameter_list|(
name|File
name|dir
parameter_list|,
name|String
name|relativePath
parameter_list|,
name|ZipOutputStream
name|zos
parameter_list|,
name|boolean
name|start
parameter_list|)
throws|throws
name|IOException
block|{
name|String
index|[]
name|dirList
init|=
name|dir
operator|.
name|list
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|aDirList
range|:
name|dirList
control|)
block|{
name|File
name|f
init|=
operator|new
name|File
argument_list|(
name|dir
argument_list|,
name|aDirList
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|f
operator|.
name|isHidden
argument_list|()
condition|)
block|{
if|if
condition|(
name|f
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|start
condition|)
block|{
name|ZipEntry
name|dirEntry
init|=
operator|new
name|ZipEntry
argument_list|(
name|relativePath
operator|+
name|f
operator|.
name|getName
argument_list|()
operator|+
literal|"/"
argument_list|)
decl_stmt|;
name|zos
operator|.
name|putNextEntry
argument_list|(
name|dirEntry
argument_list|)
expr_stmt|;
name|zos
operator|.
name|closeEntry
argument_list|()
expr_stmt|;
block|}
name|String
name|filePath
init|=
name|f
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|File
name|file
init|=
operator|new
name|File
argument_list|(
name|filePath
argument_list|)
decl_stmt|;
name|zipDir
argument_list|(
name|file
argument_list|,
name|relativePath
operator|+
name|f
operator|.
name|getName
argument_list|()
operator|+
literal|"/"
argument_list|,
name|zos
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|path
init|=
name|relativePath
operator|+
name|f
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|path
operator|.
name|equals
argument_list|(
name|JarFile
operator|.
name|MANIFEST_NAME
argument_list|)
condition|)
block|{
name|ZipEntry
name|anEntry
init|=
operator|new
name|ZipEntry
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|InputStream
name|is
init|=
operator|new
name|FileInputStream
argument_list|(
name|f
argument_list|)
decl_stmt|;
name|copyToZipStream
argument_list|(
name|is
argument_list|,
name|anEntry
argument_list|,
name|zos
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
specifier|private
specifier|static
name|void
name|createJar
parameter_list|(
name|File
name|dir
parameter_list|,
name|File
name|jarFile
parameter_list|)
throws|throws
name|IOException
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|dir
argument_list|,
literal|"dir"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|jarFile
argument_list|,
literal|"jarFile"
argument_list|)
expr_stmt|;
name|File
name|jarDir
init|=
name|jarFile
operator|.
name|getParentFile
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|jarDir
operator|.
name|exists
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|jarDir
operator|.
name|mkdirs
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|MessageFormat
operator|.
name|format
argument_list|(
literal|"could not create dir [{0}]"
argument_list|,
name|jarDir
argument_list|)
argument_list|)
throw|;
block|}
block|}
name|JarOutputStream
name|zos
init|=
operator|new
name|JarOutputStream
argument_list|(
operator|new
name|FileOutputStream
argument_list|(
name|jarFile
argument_list|)
argument_list|)
decl_stmt|;
name|jarDir
argument_list|(
name|dir
argument_list|,
literal|""
argument_list|,
name|zos
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

