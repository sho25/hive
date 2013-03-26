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
name|hcatalog
operator|.
name|storagehandler
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
name|Properties
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|logging
operator|.
name|Logger
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
name|hive
operator|.
name|metastore
operator|.
name|HiveMetaHook
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
name|metastore
operator|.
name|api
operator|.
name|Table
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
name|ql
operator|.
name|io
operator|.
name|RCFileInputFormat
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
name|ql
operator|.
name|io
operator|.
name|RCFileOutputFormat
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
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
name|ql
operator|.
name|plan
operator|.
name|TableDesc
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
name|ql
operator|.
name|security
operator|.
name|authorization
operator|.
name|HiveAuthorizationProvider
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
name|serde2
operator|.
name|SerDe
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
name|serde2
operator|.
name|lazy
operator|.
name|LazySimpleSerDe
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
name|serde2
operator|.
name|lazybinary
operator|.
name|LazyBinarySerDe
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
name|InputFormat
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
name|OutputFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|HCatRecordSerDe
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|mapred
operator|.
name|HCatMapredInputFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|mapred
operator|.
name|HCatMapredOutputFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|mapreduce
operator|.
name|HCatInputStorageDriver
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|mapreduce
operator|.
name|HCatOutputStorageDriver
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|mapreduce
operator|.
name|HCatStorageHandler
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|mapreduce
operator|.
name|OutputJobInfo
import|;
end_import

begin_comment
comment|/**  * This  is a broken class and should be removed as  * part of HCATALOG-237  */
end_comment

begin_class
annotation|@
name|Deprecated
specifier|public
class|class
name|HCatStorageHandlerImpl
extends|extends
name|HCatStorageHandler
block|{
name|Class
name|isd
decl_stmt|;
name|Class
name|osd
decl_stmt|;
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HCatStorageHandlerImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|HCatInputStorageDriver
argument_list|>
name|getInputStorageDriver
parameter_list|()
block|{
return|return
name|isd
return|;
block|}
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|HCatOutputStorageDriver
argument_list|>
name|getOutputStorageDriver
parameter_list|()
block|{
return|return
name|osd
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|configureInputJobProperties
parameter_list|(
name|TableDesc
name|tableDesc
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
parameter_list|)
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|configureOutputJobProperties
parameter_list|(
name|TableDesc
name|tableDesc
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
parameter_list|)
block|{
comment|//To change body of implemented methods use File | Settings | File Templates.
block|}
annotation|@
name|Override
specifier|public
name|HiveAuthorizationProvider
name|getAuthorizationProvider
parameter_list|()
throws|throws
name|HiveException
block|{
return|return
operator|new
name|DummyHCatAuthProvider
argument_list|()
return|;
block|}
specifier|public
name|void
name|commitCreateTable
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|MetaException
block|{   }
annotation|@
name|Override
specifier|public
name|HiveMetaHook
name|getMetaHook
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
comment|//  public void configureTableJobProperties(TableDesc tableDesc,
comment|//      Map<String, String> jobProperties) {
comment|//    // Information about the table and the job to be performed
comment|//    // We pass them on into the mepredif / mapredof
comment|//
comment|//    Properties tprops = tableDesc.getProperties();
comment|//
comment|//    if(LOG.isDebugEnabled()){
comment|//      LOG.debug("HCatStorageHandlerImpl configureTableJobProperties:");
comment|//      HCatUtil.logStackTrace(LOG);
comment|//      HCatUtil.logMap(LOG, "jobProperties", jobProperties);
comment|//      if (tprops!= null){
comment|//        HCatUtil.logEntrySet(LOG, "tableprops", tprops.entrySet());
comment|//      }
comment|//      LOG.debug("tablename : "+tableDesc.getTableName());
comment|//    }
comment|//
comment|//    // copy existing table props first
comment|//    for (Entry e : tprops.entrySet()){
comment|//      jobProperties.put((String)e.getKey(), (String)e.getValue());
comment|//    }
comment|//
comment|//    // try to set input format related properties
comment|//    try {
comment|//      HCatMapredInputFormat.setTableDesc(tableDesc,jobProperties);
comment|//    } catch (IOException ioe){
comment|//      // ok, things are probably not going to work, but we
comment|//      // can't throw out exceptions per interface. So, we log.
comment|//      LOG.error("HCatInputFormat init fail " + ioe.getMessage());
comment|//      LOG.error(ioe.getStackTrace());
comment|//    }
comment|//
comment|//    // try to set output format related properties
comment|//    try {
comment|//      HCatMapredOutputFormat.setTableDesc(tableDesc,jobProperties);
comment|//    } catch (IOException ioe){
comment|//      // ok, things are probably not going to work, but we
comment|//      // can't throw out exceptions per interface. So, we log.
comment|//      LOG.error("HCatOutputFormat init fail " + ioe.getMessage());
comment|//      LOG.error(ioe.getStackTrace());
comment|//    }
comment|//  }
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|SerDe
argument_list|>
name|getSerDeClass
parameter_list|()
block|{
return|return
name|HCatRecordSerDe
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|getInputFormatClass
parameter_list|()
block|{
return|return
name|HCatMapredInputFormat
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
name|getOutputFormatClass
parameter_list|()
block|{
return|return
name|HCatMapredOutputFormat
operator|.
name|class
return|;
block|}
block|}
end_class

end_unit

