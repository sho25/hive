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
name|conf
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintStream
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
name|util
operator|.
name|Iterator
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
name|commons
operator|.
name|lang
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * Hive Configuration  */
end_comment

begin_class
specifier|public
class|class
name|HiveConf
extends|extends
name|Configuration
block|{
specifier|protected
name|String
name|hiveJar
decl_stmt|;
specifier|protected
name|Properties
name|origProp
decl_stmt|;
specifier|protected
name|String
name|auxJars
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Log
name|l4j
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HiveConf
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
enum|enum
name|ConfVars
block|{
comment|// QL execution stuff
name|SCRIPTWRAPPER
argument_list|(
literal|"hive.exec.script.wrapper"
argument_list|,
literal|null
argument_list|)
block|,
name|PLAN
argument_list|(
literal|"hive.exec.plan"
argument_list|,
literal|null
argument_list|)
block|,
name|SCRATCHDIR
argument_list|(
literal|"hive.exec.scratchdir"
argument_list|,
literal|"/tmp/"
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
operator|+
literal|"/hive"
argument_list|)
block|,
name|SUBMITVIACHILD
argument_list|(
literal|"hive.exec.submitviachild"
argument_list|,
literal|"false"
argument_list|)
block|,
comment|// hadoop stuff
name|HADOOPBIN
argument_list|(
literal|"hadoop.bin.path"
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.dir"
argument_list|)
operator|+
literal|"/../../../bin/hadoop"
argument_list|)
block|,
name|HADOOPCONF
argument_list|(
literal|"hadoop.config.dir"
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.dir"
argument_list|)
operator|+
literal|"/../../../conf"
argument_list|)
block|,
name|HADOOPFS
argument_list|(
literal|"fs.default.name"
argument_list|,
literal|"file:///"
argument_list|)
block|,
name|HADOOPMAPFILENAME
argument_list|(
literal|"map.input.file"
argument_list|,
literal|null
argument_list|)
block|,
name|HADOOPJT
argument_list|(
literal|"mapred.job.tracker"
argument_list|,
literal|"local"
argument_list|)
block|,
name|HADOOPNUMREDUCERS
argument_list|(
literal|"mapred.reduce.tasks"
argument_list|,
literal|"1"
argument_list|)
block|,
name|HADOOPJOBNAME
argument_list|(
literal|"mapred.job.name"
argument_list|,
literal|null
argument_list|)
block|,
comment|// MetaStore stuff.
name|METASTOREDIRECTORY
argument_list|(
literal|"hive.metastore.metadb.dir"
argument_list|,
literal|""
argument_list|)
block|,
name|METASTOREWAREHOUSE
argument_list|(
literal|"hive.metastore.warehouse.dir"
argument_list|,
literal|""
argument_list|)
block|,
name|METASTOREURIS
argument_list|(
literal|"hive.metastore.uris"
argument_list|,
literal|""
argument_list|)
block|,
comment|// Things we log in the jobconf
comment|// session identifier
name|HIVESESSIONID
argument_list|(
literal|"hive.session.id"
argument_list|,
literal|""
argument_list|)
block|,
comment|// query being executed (multiple per session)
name|HIVEQUERYID
argument_list|(
literal|"hive.query.string"
argument_list|,
literal|""
argument_list|)
block|,
comment|// id of the mapred plan being executed (multiple per query)
name|HIVEPLANID
argument_list|(
literal|"hive.query.planid"
argument_list|,
literal|""
argument_list|)
block|,
comment|// max jobname length
name|HIVEJOBNAMELENGTH
argument_list|(
literal|"hive.jobname.length"
argument_list|,
literal|50
argument_list|)
block|,
comment|// hive jar
name|HIVEJAR
argument_list|(
literal|"hive.jar.path"
argument_list|,
literal|""
argument_list|)
block|,
name|HIVEAUXJARS
argument_list|(
literal|"hive.aux.jars.path"
argument_list|,
literal|""
argument_list|)
block|,
comment|// for hive script operator
name|HIVETABLENAME
argument_list|(
literal|"hive.table.name"
argument_list|,
literal|""
argument_list|)
block|,
name|HIVEPARTITIONNAME
argument_list|(
literal|"hive.partition.name"
argument_list|,
literal|""
argument_list|)
block|,
name|HIVEALIAS
argument_list|(
literal|"hive.alias"
argument_list|,
literal|""
argument_list|)
block|;
specifier|public
specifier|final
name|String
name|varname
decl_stmt|;
specifier|public
specifier|final
name|String
name|defaultVal
decl_stmt|;
specifier|public
specifier|final
name|int
name|defaultIntVal
decl_stmt|;
specifier|public
specifier|final
name|Class
name|valClass
decl_stmt|;
name|ConfVars
parameter_list|(
name|String
name|varname
parameter_list|,
name|String
name|defaultVal
parameter_list|)
block|{
name|this
operator|.
name|varname
operator|=
name|varname
expr_stmt|;
name|this
operator|.
name|defaultVal
operator|=
name|defaultVal
expr_stmt|;
name|this
operator|.
name|valClass
operator|=
name|String
operator|.
name|class
expr_stmt|;
name|this
operator|.
name|defaultIntVal
operator|=
operator|-
literal|1
expr_stmt|;
block|}
name|ConfVars
parameter_list|(
name|String
name|varname
parameter_list|,
name|int
name|defaultIntVal
parameter_list|)
block|{
name|this
operator|.
name|varname
operator|=
name|varname
expr_stmt|;
name|this
operator|.
name|defaultVal
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|defaultIntVal
operator|=
name|defaultIntVal
expr_stmt|;
name|this
operator|.
name|valClass
operator|=
name|Integer
operator|.
name|class
expr_stmt|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|varname
return|;
block|}
block|}
specifier|public
specifier|static
name|int
name|getIntVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|)
block|{
assert|assert
operator|(
name|var
operator|.
name|valClass
operator|==
name|Integer
operator|.
name|class
operator|)
assert|;
return|return
name|conf
operator|.
name|getInt
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|var
operator|.
name|defaultIntVal
argument_list|)
return|;
block|}
specifier|public
name|int
name|getIntVar
parameter_list|(
name|ConfVars
name|var
parameter_list|)
block|{
return|return
name|getIntVar
argument_list|(
name|this
argument_list|,
name|var
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|getVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|)
block|{
assert|assert
operator|(
name|var
operator|.
name|valClass
operator|==
name|String
operator|.
name|class
operator|)
assert|;
return|return
name|conf
operator|.
name|get
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|var
operator|.
name|defaultVal
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|setVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|,
name|String
name|val
parameter_list|)
block|{
assert|assert
operator|(
name|var
operator|.
name|valClass
operator|==
name|String
operator|.
name|class
operator|)
assert|;
name|conf
operator|.
name|set
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getVar
parameter_list|(
name|ConfVars
name|var
parameter_list|)
block|{
return|return
name|getVar
argument_list|(
name|this
argument_list|,
name|var
argument_list|)
return|;
block|}
specifier|public
name|void
name|setVar
parameter_list|(
name|ConfVars
name|var
parameter_list|,
name|String
name|val
parameter_list|)
block|{
name|setVar
argument_list|(
name|this
argument_list|,
name|var
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|logVars
parameter_list|(
name|PrintStream
name|ps
parameter_list|)
block|{
for|for
control|(
name|ConfVars
name|one
range|:
name|ConfVars
operator|.
name|values
argument_list|()
control|)
block|{
name|ps
operator|.
name|println
argument_list|(
name|one
operator|.
name|varname
operator|+
literal|"="
operator|+
operator|(
operator|(
name|get
argument_list|(
name|one
operator|.
name|varname
argument_list|)
operator|!=
literal|null
operator|)
condition|?
name|get
argument_list|(
name|one
operator|.
name|varname
argument_list|)
else|:
literal|""
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|HiveConf
parameter_list|(
name|Class
name|cls
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|initialize
argument_list|(
name|cls
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveConf
parameter_list|(
name|Configuration
name|other
parameter_list|,
name|Class
name|cls
parameter_list|)
block|{
name|super
argument_list|(
name|other
argument_list|)
expr_stmt|;
name|initialize
argument_list|(
name|cls
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Properties
name|getUnderlyingProps
parameter_list|()
block|{
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|iter
init|=
name|this
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|Properties
name|p
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|e
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|p
operator|.
name|setProperty
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|p
return|;
block|}
specifier|private
name|void
name|initialize
parameter_list|(
name|Class
name|cls
parameter_list|)
block|{
name|hiveJar
operator|=
operator|(
operator|new
name|JobConf
argument_list|(
name|cls
argument_list|)
operator|)
operator|.
name|getJar
argument_list|()
expr_stmt|;
comment|// preserve the original configuration
name|origProp
operator|=
name|getUnderlyingProps
argument_list|()
expr_stmt|;
comment|// let's add the hive configuration
name|URL
name|hconfurl
init|=
name|getClassLoader
argument_list|()
operator|.
name|getResource
argument_list|(
literal|"hive-default.xml"
argument_list|)
decl_stmt|;
if|if
condition|(
name|hconfurl
operator|==
literal|null
condition|)
block|{
name|l4j
operator|.
name|warn
argument_list|(
literal|"Unable to locate default hive configuration"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|addResource
argument_list|(
name|hconfurl
argument_list|)
expr_stmt|;
block|}
name|URL
name|hsiteurl
init|=
name|getClassLoader
argument_list|()
operator|.
name|getResource
argument_list|(
literal|"hive-site.xml"
argument_list|)
decl_stmt|;
if|if
condition|(
name|hsiteurl
operator|==
literal|null
condition|)
block|{
name|l4j
operator|.
name|warn
argument_list|(
literal|"Unable to locate hive site configuration"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|addResource
argument_list|(
name|hsiteurl
argument_list|)
expr_stmt|;
block|}
comment|// if hadoop configuration files are already in our path - then define
comment|// the containing directory as the configuration directory
name|URL
name|hadoopconfurl
init|=
name|getClassLoader
argument_list|()
operator|.
name|getResource
argument_list|(
literal|"hadoop-default.xml"
argument_list|)
decl_stmt|;
if|if
condition|(
name|hadoopconfurl
operator|==
literal|null
condition|)
name|hadoopconfurl
operator|=
name|getClassLoader
argument_list|()
operator|.
name|getResource
argument_list|(
literal|"hadoop-site.xml"
argument_list|)
expr_stmt|;
if|if
condition|(
name|hadoopconfurl
operator|!=
literal|null
condition|)
block|{
name|String
name|conffile
init|=
name|hadoopconfurl
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|this
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HADOOPCONF
argument_list|,
name|conffile
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|conffile
operator|.
name|lastIndexOf
argument_list|(
literal|'/'
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|applySystemProperties
argument_list|()
expr_stmt|;
comment|// if the running class was loaded directly (through eclipse) rather than through a
comment|// jar then this would be needed
if|if
condition|(
name|hiveJar
operator|==
literal|null
condition|)
block|{
name|hiveJar
operator|=
name|this
operator|.
name|get
argument_list|(
name|ConfVars
operator|.
name|HIVEJAR
operator|.
name|varname
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|auxJars
operator|==
literal|null
condition|)
block|{
name|auxJars
operator|=
name|this
operator|.
name|get
argument_list|(
name|ConfVars
operator|.
name|HIVEAUXJARS
operator|.
name|varname
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|applySystemProperties
parameter_list|()
block|{
for|for
control|(
name|ConfVars
name|oneVar
range|:
name|ConfVars
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|System
operator|.
name|getProperty
argument_list|(
name|oneVar
operator|.
name|varname
argument_list|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|System
operator|.
name|getProperty
argument_list|(
name|oneVar
operator|.
name|varname
argument_list|)
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
name|this
operator|.
name|set
argument_list|(
name|oneVar
operator|.
name|varname
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
name|oneVar
operator|.
name|varname
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|Properties
name|getChangedProperties
parameter_list|()
block|{
name|Properties
name|ret
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|Properties
name|newProp
init|=
name|getUnderlyingProps
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
name|one
range|:
name|newProp
operator|.
name|keySet
argument_list|()
control|)
block|{
name|String
name|oneProp
init|=
operator|(
name|String
operator|)
name|one
decl_stmt|;
name|String
name|oldValue
init|=
name|origProp
operator|.
name|getProperty
argument_list|(
name|oneProp
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|StringUtils
operator|.
name|equals
argument_list|(
name|oldValue
argument_list|,
name|newProp
operator|.
name|getProperty
argument_list|(
name|oneProp
argument_list|)
argument_list|)
condition|)
block|{
name|ret
operator|.
name|setProperty
argument_list|(
name|oneProp
argument_list|,
name|newProp
operator|.
name|getProperty
argument_list|(
name|oneProp
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|(
name|ret
operator|)
return|;
block|}
specifier|public
name|Properties
name|getAllProperties
parameter_list|()
block|{
return|return
name|getUnderlyingProps
argument_list|()
return|;
block|}
specifier|public
name|String
name|getJar
parameter_list|()
block|{
return|return
name|hiveJar
return|;
block|}
comment|/**    * @return the auxJars    */
specifier|public
name|String
name|getAuxJars
parameter_list|()
block|{
return|return
name|auxJars
return|;
block|}
comment|/**    * @param auxJars the auxJars to set    */
specifier|public
name|void
name|setAuxJars
parameter_list|(
name|String
name|auxJars
parameter_list|)
block|{
name|this
operator|.
name|auxJars
operator|=
name|auxJars
expr_stmt|;
block|}
block|}
end_class

end_unit

