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
name|exec
operator|.
name|tez
package|;
end_package

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|io
operator|.
name|ReadaheadPool
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
name|tez
operator|.
name|common
operator|.
name|TezUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|UserPayload
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|AbstractLogicalIOProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|Event
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|LogicalInput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|LogicalOutput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|ProcessorContext
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
name|JarURLConnection
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
name|JarEntry
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|crypto
operator|.
name|Mac
import|;
end_import

begin_comment
comment|/**  * A simple sleep processor implementation that sleeps for the configured  * time in milliseconds.  *  * @see Config for configuring the HivePreWarmProcessor  */
end_comment

begin_class
specifier|public
class|class
name|HivePreWarmProcessor
extends|extends
name|AbstractLogicalIOProcessor
block|{
specifier|private
specifier|static
name|boolean
name|prewarmed
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HivePreWarmProcessor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|public
name|HivePreWarmProcessor
parameter_list|(
name|ProcessorContext
name|context
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|()
throws|throws
name|Exception
block|{
name|UserPayload
name|userPayload
init|=
name|getContext
argument_list|()
operator|.
name|getUserPayload
argument_list|()
decl_stmt|;
name|this
operator|.
name|conf
operator|=
name|TezUtils
operator|.
name|createConfFromUserPayload
argument_list|(
name|userPayload
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|LogicalInput
argument_list|>
name|inputs
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|LogicalOutput
argument_list|>
name|outputs
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|prewarmed
condition|)
block|{
comment|/* container reuse */
return|return;
block|}
for|for
control|(
name|LogicalInput
name|input
range|:
name|inputs
operator|.
name|values
argument_list|()
control|)
block|{
name|input
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|LogicalOutput
name|output
range|:
name|outputs
operator|.
name|values
argument_list|()
control|)
block|{
name|output
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
comment|/* these are things that goes through singleton initialization on most queries */
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Mac
name|mac
init|=
name|Mac
operator|.
name|getInstance
argument_list|(
literal|"HmacSHA1"
argument_list|)
decl_stmt|;
name|ReadaheadPool
name|rpool
init|=
name|ReadaheadPool
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
expr_stmt|;
name|URL
name|hiveurl
init|=
operator|new
name|URL
argument_list|(
literal|"jar:"
operator|+
name|DagUtils
operator|.
name|getInstance
argument_list|()
operator|.
name|getExecJarPathLocal
argument_list|()
operator|+
literal|"!/"
argument_list|)
decl_stmt|;
name|JarURLConnection
name|hiveconn
init|=
operator|(
name|JarURLConnection
operator|)
name|hiveurl
operator|.
name|openConnection
argument_list|()
decl_stmt|;
name|JarFile
name|hivejar
init|=
name|hiveconn
operator|.
name|getJarFile
argument_list|()
decl_stmt|;
try|try
block|{
name|Enumeration
argument_list|<
name|JarEntry
argument_list|>
name|classes
init|=
name|hivejar
operator|.
name|entries
argument_list|()
decl_stmt|;
while|while
condition|(
name|classes
operator|.
name|hasMoreElements
argument_list|()
condition|)
block|{
name|JarEntry
name|je
init|=
name|classes
operator|.
name|nextElement
argument_list|()
decl_stmt|;
if|if
condition|(
name|je
operator|.
name|getName
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|".class"
argument_list|)
condition|)
block|{
name|String
name|klass
init|=
name|je
operator|.
name|getName
argument_list|()
operator|.
name|replace
argument_list|(
literal|".class"
argument_list|,
literal|""
argument_list|)
operator|.
name|replaceAll
argument_list|(
literal|"/"
argument_list|,
literal|"\\."
argument_list|)
decl_stmt|;
if|if
condition|(
name|klass
operator|.
name|indexOf
argument_list|(
literal|"ql.exec"
argument_list|)
operator|!=
operator|-
literal|1
operator|||
name|klass
operator|.
name|indexOf
argument_list|(
literal|"ql.io"
argument_list|)
operator|!=
operator|-
literal|1
condition|)
block|{
comment|/* several hive classes depend on the metastore APIs, which is not included              * in hive-exec.jar. These are the relatively safe ones - operators& io classes.              */
if|if
condition|(
name|klass
operator|.
name|indexOf
argument_list|(
literal|"vector"
argument_list|)
operator|!=
operator|-
literal|1
operator|||
name|klass
operator|.
name|indexOf
argument_list|(
literal|"Operator"
argument_list|)
operator|!=
operator|-
literal|1
condition|)
block|{
name|JavaUtils
operator|.
name|loadClass
argument_list|(
name|klass
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
finally|finally
block|{
name|hivejar
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|prewarmed
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleEvents
parameter_list|(
name|List
argument_list|<
name|Event
argument_list|>
name|processorEvents
parameter_list|)
block|{
comment|// Nothing to do
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Nothing to cleanup
block|}
block|}
end_class

end_unit

