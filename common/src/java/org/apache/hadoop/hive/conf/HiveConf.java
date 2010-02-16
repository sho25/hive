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
name|security
operator|.
name|UserGroupInformation
import|;
end_import

begin_comment
comment|/**  * Hive Configuration.  */
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
comment|/**    * Metastore related options that the db is initialized against.    */
specifier|public
specifier|static
specifier|final
name|HiveConf
operator|.
name|ConfVars
index|[]
name|metaVars
init|=
block|{
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREDIRECTORY
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREWAREHOUSE
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
block|}
decl_stmt|;
comment|/**    * ConfVars.    *    */
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
literal|false
argument_list|)
block|,
name|SCRIPTERRORLIMIT
argument_list|(
literal|"hive.exec.script.maxerrsize"
argument_list|,
literal|100000
argument_list|)
block|,
name|ALLOWPARTIALCONSUMP
argument_list|(
literal|"hive.exec.script.allow.partial.consumption"
argument_list|,
literal|false
argument_list|)
block|,
name|COMPRESSRESULT
argument_list|(
literal|"hive.exec.compress.output"
argument_list|,
literal|false
argument_list|)
block|,
name|COMPRESSINTERMEDIATE
argument_list|(
literal|"hive.exec.compress.intermediate"
argument_list|,
literal|false
argument_list|)
block|,
name|COMPRESSINTERMEDIATECODEC
argument_list|(
literal|"hive.intermediate.compression.codec"
argument_list|,
literal|""
argument_list|)
block|,
name|COMPRESSINTERMEDIATETYPE
argument_list|(
literal|"hive.intermediate.compression.type"
argument_list|,
literal|""
argument_list|)
block|,
name|BYTESPERREDUCER
argument_list|(
literal|"hive.exec.reducers.bytes.per.reducer"
argument_list|,
call|(
name|long
call|)
argument_list|(
literal|1000
operator|*
literal|1000
operator|*
literal|1000
argument_list|)
argument_list|)
block|,
name|MAXREDUCERS
argument_list|(
literal|"hive.exec.reducers.max"
argument_list|,
literal|999
argument_list|)
block|,
name|PREEXECHOOKS
argument_list|(
literal|"hive.exec.pre.hooks"
argument_list|,
literal|""
argument_list|)
block|,
name|POSTEXECHOOKS
argument_list|(
literal|"hive.exec.post.hooks"
argument_list|,
literal|""
argument_list|)
block|,
name|EXECPARALLEL
argument_list|(
literal|"hive.exec.parallel"
argument_list|,
literal|false
argument_list|)
block|,
comment|// parallel query launching
name|EXECPARALLETHREADNUMBER
argument_list|(
literal|"hive.exec.parallel.thread.number"
argument_list|,
literal|8
argument_list|)
block|,
name|HIVESPECULATIVEEXECREDUCERS
argument_list|(
literal|"hive.mapred.reduce.tasks.speculative.execution"
argument_list|,
literal|true
argument_list|)
block|,
comment|// hadoop stuff
name|HADOOPBIN
argument_list|(
literal|"hadoop.bin.path"
argument_list|,
name|System
operator|.
name|getenv
argument_list|(
literal|"HADOOP_HOME"
argument_list|)
operator|+
literal|"/bin/hadoop"
argument_list|)
block|,
name|HADOOPCONF
argument_list|(
literal|"hadoop.config.dir"
argument_list|,
name|System
operator|.
name|getenv
argument_list|(
literal|"HADOOP_HOME"
argument_list|)
operator|+
literal|"/conf"
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
name|HADOOPMAPREDINPUTDIR
argument_list|(
literal|"mapred.input.dir"
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
literal|1
argument_list|)
block|,
name|HADOOPJOBNAME
argument_list|(
literal|"mapred.job.name"
argument_list|,
literal|null
argument_list|)
block|,
name|HADOOPSPECULATIVEEXECREDUCERS
argument_list|(
literal|"mapred.reduce.tasks.speculative.execution"
argument_list|,
literal|false
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
name|METASTOREPWD
argument_list|(
literal|"javax.jdo.option.ConnectionPassword"
argument_list|,
literal|""
argument_list|)
block|,
comment|// CLI
name|CLIIGNOREERRORS
argument_list|(
literal|"hive.cli.errors.ignore"
argument_list|,
literal|false
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
name|HIVEQUERYSTRING
argument_list|(
literal|"hive.query.string"
argument_list|,
literal|""
argument_list|)
block|,
comment|// id of query being executed (multiple per session)
name|HIVEQUERYID
argument_list|(
literal|"hive.query.id"
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
comment|// hive added files and jars
name|HIVEADDEDFILES
argument_list|(
literal|"hive.added.files.path"
argument_list|,
literal|""
argument_list|)
block|,
name|HIVEADDEDJARS
argument_list|(
literal|"hive.added.jars.path"
argument_list|,
literal|""
argument_list|)
block|,
name|HIVEADDEDARCHIVES
argument_list|(
literal|"hive.added.archives.path"
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
name|HIVESCRIPTAUTOPROGRESS
argument_list|(
literal|"hive.script.auto.progress"
argument_list|,
literal|false
argument_list|)
block|,
name|HIVESCRIPTIDENVVAR
argument_list|(
literal|"hive.script.operator.id.env.var"
argument_list|,
literal|"HIVE_SCRIPT_OPERATOR_ID"
argument_list|)
block|,
name|HIVEMAPREDMODE
argument_list|(
literal|"hive.mapred.mode"
argument_list|,
literal|"nonstrict"
argument_list|)
block|,
name|HIVEALIAS
argument_list|(
literal|"hive.alias"
argument_list|,
literal|""
argument_list|)
block|,
name|HIVEMAPSIDEAGGREGATE
argument_list|(
literal|"hive.map.aggr"
argument_list|,
literal|"true"
argument_list|)
block|,
name|HIVEGROUPBYSKEW
argument_list|(
literal|"hive.groupby.skewindata"
argument_list|,
literal|"false"
argument_list|)
block|,
name|HIVEJOINEMITINTERVAL
argument_list|(
literal|"hive.join.emit.interval"
argument_list|,
literal|1000
argument_list|)
block|,
name|HIVEJOINCACHESIZE
argument_list|(
literal|"hive.join.cache.size"
argument_list|,
literal|25000
argument_list|)
block|,
name|HIVEMAPJOINBUCKETCACHESIZE
argument_list|(
literal|"hive.mapjoin.bucket.cache.size"
argument_list|,
literal|100
argument_list|)
block|,
name|HIVEMAPJOINROWSIZE
argument_list|(
literal|"hive.mapjoin.size.key"
argument_list|,
literal|10000
argument_list|)
block|,
name|HIVEMAPJOINCACHEROWS
argument_list|(
literal|"hive.mapjoin.cache.numrows"
argument_list|,
literal|25000
argument_list|)
block|,
name|HIVEGROUPBYMAPINTERVAL
argument_list|(
literal|"hive.groupby.mapaggr.checkinterval"
argument_list|,
literal|100000
argument_list|)
block|,
name|HIVEMAPAGGRHASHMEMORY
argument_list|(
literal|"hive.map.aggr.hash.percentmemory"
argument_list|,
operator|(
name|float
operator|)
literal|0.5
argument_list|)
block|,
name|HIVEMAPAGGRHASHMINREDUCTION
argument_list|(
literal|"hive.map.aggr.hash.min.reduction"
argument_list|,
operator|(
name|float
operator|)
literal|0.5
argument_list|)
block|,
comment|// for hive udtf operator
name|HIVEUDTFAUTOPROGRESS
argument_list|(
literal|"hive.udtf.auto.progress"
argument_list|,
literal|false
argument_list|)
block|,
comment|// Default file format for CREATE TABLE statement
comment|// Options: TextFile, SequenceFile
name|HIVEDEFAULTFILEFORMAT
argument_list|(
literal|"hive.default.fileformat"
argument_list|,
literal|"TextFile"
argument_list|)
block|,
comment|//Location of Hive run time structured log file
name|HIVEHISTORYFILELOC
argument_list|(
literal|"hive.querylog.location"
argument_list|,
literal|"/tmp/"
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
argument_list|)
block|,
comment|// Default serde and record reader for user scripts
name|HIVESCRIPTSERDE
argument_list|(
literal|"hive.script.serde"
argument_list|,
literal|"org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe"
argument_list|)
block|,
name|HIVESCRIPTRECORDREADER
argument_list|(
literal|"hive.script.recordreader"
argument_list|,
literal|"org.apache.hadoop.hive.ql.exec.TextRecordReader"
argument_list|)
block|,
name|HIVESCRIPTRECORDWRITER
argument_list|(
literal|"hive.script.recordwriter"
argument_list|,
literal|"org.apache.hadoop.hive.ql.exec.TextRecordWriter"
argument_list|)
block|,
comment|// HWI
name|HIVEHWILISTENHOST
argument_list|(
literal|"hive.hwi.listen.host"
argument_list|,
literal|"0.0.0.0"
argument_list|)
block|,
name|HIVEHWILISTENPORT
argument_list|(
literal|"hive.hwi.listen.port"
argument_list|,
literal|"9999"
argument_list|)
block|,
name|HIVEHWIWARFILE
argument_list|(
literal|"hive.hwi.war.file"
argument_list|,
name|System
operator|.
name|getenv
argument_list|(
literal|"HWI_WAR_FILE"
argument_list|)
argument_list|)
block|,
comment|// mapper/reducer memory in local mode
name|HIVEHADOOPMAXMEM
argument_list|(
literal|"hive.mapred.local.mem"
argument_list|,
literal|0
argument_list|)
block|,
comment|// test mode in hive mode
name|HIVETESTMODE
argument_list|(
literal|"hive.test.mode"
argument_list|,
literal|false
argument_list|)
block|,
name|HIVETESTMODEPREFIX
argument_list|(
literal|"hive.test.mode.prefix"
argument_list|,
literal|"test_"
argument_list|)
block|,
name|HIVETESTMODESAMPLEFREQ
argument_list|(
literal|"hive.test.mode.samplefreq"
argument_list|,
literal|32
argument_list|)
block|,
name|HIVETESTMODENOSAMPLE
argument_list|(
literal|"hive.test.mode.nosamplelist"
argument_list|,
literal|""
argument_list|)
block|,
name|HIVEMERGEMAPFILES
argument_list|(
literal|"hive.merge.mapfiles"
argument_list|,
literal|true
argument_list|)
block|,
name|HIVEMERGEMAPREDFILES
argument_list|(
literal|"hive.merge.mapredfiles"
argument_list|,
literal|false
argument_list|)
block|,
name|HIVEMERGEMAPFILESSIZE
argument_list|(
literal|"hive.merge.size.per.task"
argument_list|,
call|(
name|long
call|)
argument_list|(
literal|256
operator|*
literal|1000
operator|*
literal|1000
argument_list|)
argument_list|)
block|,
name|HIVEMERGEMAPFILESAVGSIZE
argument_list|(
literal|"hive.merge.smallfiles.avgsize"
argument_list|,
call|(
name|long
call|)
argument_list|(
literal|16
operator|*
literal|1000
operator|*
literal|1000
argument_list|)
argument_list|)
block|,
name|HIVESKEWJOIN
argument_list|(
literal|"hive.optimize.skewjoin"
argument_list|,
literal|false
argument_list|)
block|,
name|HIVESKEWJOINKEY
argument_list|(
literal|"hive.skewjoin.key"
argument_list|,
literal|1000000
argument_list|)
block|,
name|HIVESKEWJOINMAPJOINNUMMAPTASK
argument_list|(
literal|"hive.skewjoin.mapjoin.map.tasks"
argument_list|,
literal|10000
argument_list|)
block|,
name|HIVESKEWJOINMAPJOINMINSPLIT
argument_list|(
literal|"hive.skewjoin.mapjoin.min.split"
argument_list|,
literal|33554432
argument_list|)
block|,
comment|//32M
name|MAPREDMINSPLITSIZE
argument_list|(
literal|"mapred.min.split.size"
argument_list|,
literal|1
argument_list|)
block|,
name|HIVESENDHEARTBEAT
argument_list|(
literal|"hive.heartbeat.interval"
argument_list|,
literal|1000
argument_list|)
block|,
name|HIVEMAXMAPJOINSIZE
argument_list|(
literal|"hive.mapjoin.maxsize"
argument_list|,
literal|100000
argument_list|)
block|,
name|HIVEJOBPROGRESS
argument_list|(
literal|"hive.task.progress"
argument_list|,
literal|false
argument_list|)
block|,
name|HIVEINPUTFORMAT
argument_list|(
literal|"hive.input.format"
argument_list|,
literal|""
argument_list|)
block|,
comment|// Optimizer
name|HIVEOPTCP
argument_list|(
literal|"hive.optimize.cp"
argument_list|,
literal|true
argument_list|)
block|,
comment|// column pruner
name|HIVEOPTPPD
argument_list|(
literal|"hive.optimize.ppd"
argument_list|,
literal|true
argument_list|)
block|,
comment|// predicate pushdown
name|HIVEOPTGROUPBY
argument_list|(
literal|"hive.optimize.groupby"
argument_list|,
literal|true
argument_list|)
block|,
comment|// optimize group by
name|HIVEOPTBUCKETMAPJOIN
argument_list|(
literal|"hive.optimize.bucketmapjoin"
argument_list|,
literal|false
argument_list|)
block|,
comment|// optimize bucket map join
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
name|long
name|defaultLongVal
decl_stmt|;
specifier|public
specifier|final
name|float
name|defaultFloatVal
decl_stmt|;
specifier|public
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|valClass
decl_stmt|;
specifier|public
specifier|final
name|boolean
name|defaultBoolVal
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
name|valClass
operator|=
name|String
operator|.
name|class
expr_stmt|;
name|this
operator|.
name|defaultVal
operator|=
name|defaultVal
expr_stmt|;
name|this
operator|.
name|defaultIntVal
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultLongVal
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultFloatVal
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultBoolVal
operator|=
literal|false
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
name|valClass
operator|=
name|Integer
operator|.
name|class
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
name|defaultLongVal
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultFloatVal
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultBoolVal
operator|=
literal|false
expr_stmt|;
block|}
name|ConfVars
parameter_list|(
name|String
name|varname
parameter_list|,
name|long
name|defaultLongVal
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
name|valClass
operator|=
name|Long
operator|.
name|class
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
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultLongVal
operator|=
name|defaultLongVal
expr_stmt|;
name|this
operator|.
name|defaultFloatVal
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultBoolVal
operator|=
literal|false
expr_stmt|;
block|}
name|ConfVars
parameter_list|(
name|String
name|varname
parameter_list|,
name|float
name|defaultFloatVal
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
name|valClass
operator|=
name|Float
operator|.
name|class
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
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultLongVal
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultFloatVal
operator|=
name|defaultFloatVal
expr_stmt|;
name|this
operator|.
name|defaultBoolVal
operator|=
literal|false
expr_stmt|;
block|}
name|ConfVars
parameter_list|(
name|String
name|varname
parameter_list|,
name|boolean
name|defaultBoolVal
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
name|valClass
operator|=
name|Boolean
operator|.
name|class
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
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultLongVal
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultFloatVal
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultBoolVal
operator|=
name|defaultBoolVal
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
name|long
name|getLongVar
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
name|Long
operator|.
name|class
operator|)
assert|;
return|return
name|conf
operator|.
name|getLong
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|var
operator|.
name|defaultLongVal
argument_list|)
return|;
block|}
specifier|public
name|long
name|getLongVar
parameter_list|(
name|ConfVars
name|var
parameter_list|)
block|{
return|return
name|getLongVar
argument_list|(
name|this
argument_list|,
name|var
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|float
name|getFloatVar
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
name|Float
operator|.
name|class
operator|)
assert|;
return|return
name|conf
operator|.
name|getFloat
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|var
operator|.
name|defaultFloatVal
argument_list|)
return|;
block|}
specifier|public
name|float
name|getFloatVar
parameter_list|(
name|ConfVars
name|var
parameter_list|)
block|{
return|return
name|getFloatVar
argument_list|(
name|this
argument_list|,
name|var
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|getBoolVar
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
name|Boolean
operator|.
name|class
operator|)
assert|;
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|var
operator|.
name|defaultBoolVal
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|getBoolVar
parameter_list|(
name|ConfVars
name|var
parameter_list|)
block|{
return|return
name|getBoolVar
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
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|HiveConf
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
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
argument_list|<
name|?
argument_list|>
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
argument_list|<
name|?
argument_list|>
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
name|debug
argument_list|(
literal|"hive-default.xml not found."
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
name|debug
argument_list|(
literal|"hive-site.xml not found."
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
block|{
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
block|}
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
block|{
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
name|setVar
argument_list|(
name|this
argument_list|,
name|ConfVars
operator|.
name|HIVEAUXJARS
argument_list|,
name|auxJars
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return the user name set in hadoop.job.ugi param or the current user from System    * @throws IOException    */
specifier|public
name|String
name|getUser
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|UserGroupInformation
name|ugi
init|=
name|UserGroupInformation
operator|.
name|readFrom
argument_list|(
name|this
argument_list|)
decl_stmt|;
if|if
condition|(
name|ugi
operator|==
literal|null
condition|)
block|{
name|ugi
operator|=
name|UserGroupInformation
operator|.
name|login
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
return|return
name|ugi
operator|.
name|getUserName
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|LoginException
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|IOException
operator|)
operator|new
name|IOException
argument_list|()
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|String
name|getColumnInternalName
parameter_list|(
name|int
name|pos
parameter_list|)
block|{
return|return
literal|"_col"
operator|+
name|pos
return|;
block|}
block|}
end_class

end_unit

