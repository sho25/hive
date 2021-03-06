begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hbase
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
name|hbase
operator|.
name|mapreduce
operator|.
name|TableSnapshotInputFormatImpl
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
name|InputSplit
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
name|lang
operator|.
name|reflect
operator|.
name|Constructor
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

begin_comment
comment|/**  * A helper class to isolate newer HBase features from users running against older versions of  * HBase that don't provide those features.  *  * TODO: remove this class when it's okay to drop support for earlier version of HBase.  */
end_comment

begin_class
specifier|public
class|class
name|HBaseTableSnapshotInputFormatUtil
block|{
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
name|HBaseTableSnapshotInputFormatUtil
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** The class we look for to determine if hbase snapshots are supported. */
specifier|private
specifier|static
specifier|final
name|String
name|TABLESNAPSHOTINPUTFORMAT_CLASS
init|=
literal|"org.apache.hadoop.hbase.mapreduce.TableSnapshotInputFormatImpl"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TABLESNAPSHOTREGIONSPLIT_CLASS
init|=
literal|"org.apache.hadoop.hbase.mapred.TableSnapshotInputFormat$TableSnapshotRegionSplit"
decl_stmt|;
comment|/** True when {@link #TABLESNAPSHOTINPUTFORMAT_CLASS} is present. */
specifier|private
specifier|static
specifier|final
name|boolean
name|SUPPORTS_TABLE_SNAPSHOTS
decl_stmt|;
static|static
block|{
name|boolean
name|support
init|=
literal|false
decl_stmt|;
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
init|=
name|Class
operator|.
name|forName
argument_list|(
name|TABLESNAPSHOTINPUTFORMAT_CLASS
argument_list|)
decl_stmt|;
name|support
operator|=
name|clazz
operator|!=
literal|null
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
comment|// pass
block|}
name|SUPPORTS_TABLE_SNAPSHOTS
operator|=
name|support
expr_stmt|;
block|}
comment|/** Return true when the HBase runtime supports {@link HiveHBaseTableSnapshotInputFormat}. */
specifier|public
specifier|static
name|void
name|assertSupportsTableSnapshots
parameter_list|()
block|{
if|if
condition|(
operator|!
name|SUPPORTS_TABLE_SNAPSHOTS
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"This version of HBase does not support Hive over table "
operator|+
literal|"snapshots. Please upgrade to at least HBase 0.98.3 or later. See HIVE-6584 for details."
argument_list|)
throw|;
block|}
block|}
comment|/**    * Configures {@code conf} for the snapshot job. Call only when    * {@link #assertSupportsTableSnapshots()} returns true.    */
specifier|public
specifier|static
name|void
name|configureJob
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|snapshotName
parameter_list|,
name|Path
name|restoreDir
parameter_list|)
throws|throws
name|IOException
block|{
name|TableSnapshotInputFormatImpl
operator|.
name|setInput
argument_list|(
name|conf
argument_list|,
name|snapshotName
argument_list|,
name|restoreDir
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a bare TableSnapshotRegionSplit. Needed because Writables require a    * default-constructed instance to hydrate from the DataInput.    *    * TODO: remove once HBASE-11555 is fixed.    */
specifier|public
specifier|static
name|InputSplit
name|createTableSnapshotRegionSplit
parameter_list|()
block|{
try|try
block|{
name|assertSupportsTableSnapshots
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Probably don't support table snapshots. Returning null instance."
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|InputSplit
argument_list|>
name|resultType
init|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|InputSplit
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|TABLESNAPSHOTREGIONSPLIT_CLASS
argument_list|)
decl_stmt|;
name|Constructor
argument_list|<
name|?
extends|extends
name|InputSplit
argument_list|>
name|cxtor
init|=
name|resultType
operator|.
name|getDeclaredConstructor
argument_list|(
operator|new
name|Class
index|[]
block|{}
argument_list|)
decl_stmt|;
name|cxtor
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|cxtor
operator|.
name|newInstance
argument_list|(
operator|new
name|Object
index|[]
block|{}
argument_list|)
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
name|UnsupportedOperationException
argument_list|(
literal|"Unable to find "
operator|+
name|TABLESNAPSHOTREGIONSPLIT_CLASS
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Unable to access specified class "
operator|+
name|TABLESNAPSHOTREGIONSPLIT_CLASS
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InstantiationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Unable to instantiate specified class "
operator|+
name|TABLESNAPSHOTREGIONSPLIT_CLASS
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Constructor threw an exception for "
operator|+
name|TABLESNAPSHOTREGIONSPLIT_CLASS
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Unable to find suitable constructor for class "
operator|+
name|TABLESNAPSHOTREGIONSPLIT_CLASS
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

