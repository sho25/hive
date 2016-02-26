begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|llap
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
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|Socket
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
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
name|llap
operator|.
name|daemon
operator|.
name|rpc
operator|.
name|LlapDaemonProtocolProtos
operator|.
name|FragmentRuntimeInfo
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
name|llap
operator|.
name|daemon
operator|.
name|rpc
operator|.
name|LlapDaemonProtocolProtos
operator|.
name|SubmitWorkRequestProto
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
name|llap
operator|.
name|ext
operator|.
name|LlapTaskUmbilicalExternalClient
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
name|llap
operator|.
name|tez
operator|.
name|Converters
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
name|DataInputBuffer
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
name|DataOutputBuffer
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
name|NullWritable
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
name|Text
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
name|WritableComparable
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
name|InputSplit
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
name|mapred
operator|.
name|RecordReader
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
name|Reporter
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
name|Credentials
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
name|token
operator|.
name|Token
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
name|yarn
operator|.
name|api
operator|.
name|ApplicationConstants
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
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|ApplicationAttemptId
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
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|ApplicationId
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
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|ContainerId
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
name|security
operator|.
name|JobTokenIdentifier
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
name|security
operator|.
name|TokenCache
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
name|impl
operator|.
name|TaskSpec
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
name|impl
operator|.
name|TezEvent
import|;
end_import

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

begin_class
specifier|public
class|class
name|LlapInputFormat
parameter_list|<
name|V
extends|extends
name|WritableComparable
parameter_list|>
implements|implements
name|InputFormat
argument_list|<
name|NullWritable
argument_list|,
name|V
argument_list|>
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
name|LlapInputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|LlapInputFormat
parameter_list|()
block|{   }
comment|/*    * This proxy record reader has the duty of establishing a connected socket with LLAP, then fire    * off the work in the split to LLAP and finally return the connected socket back in an    * LlapRecordReader. The LlapRecordReader class reads the results from the socket.    */
annotation|@
name|Override
specifier|public
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|V
argument_list|>
name|getRecordReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|LlapInputSplit
name|llapSplit
init|=
operator|(
name|LlapInputSplit
operator|)
name|split
decl_stmt|;
name|SubmitWorkInfo
name|submitWorkInfo
init|=
name|SubmitWorkInfo
operator|.
name|fromBytes
argument_list|(
name|llapSplit
operator|.
name|getPlanBytes
argument_list|()
argument_list|)
decl_stmt|;
comment|// TODO HACK: Spark is built with Hive-1.2.1, does not have access to HiveConf.ConfVars.LLAP_DAEMON_RPC_PORT
name|int
name|llapSubmitPort
init|=
name|job
operator|.
name|getInt
argument_list|(
literal|"hive.llap.daemon.rpc.port"
argument_list|,
literal|15001
argument_list|)
decl_stmt|;
name|LlapTaskUmbilicalExternalClient
name|llapClient
init|=
operator|new
name|LlapTaskUmbilicalExternalClient
argument_list|(
name|job
argument_list|,
name|submitWorkInfo
operator|.
name|getTokenIdentifier
argument_list|()
argument_list|,
name|submitWorkInfo
operator|.
name|getToken
argument_list|()
argument_list|)
decl_stmt|;
name|llapClient
operator|.
name|init
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|llapClient
operator|.
name|start
argument_list|()
expr_stmt|;
name|SubmitWorkRequestProto
name|submitWorkRequestProto
init|=
name|constructSubmitWorkRequestProto
argument_list|(
name|submitWorkInfo
argument_list|,
name|llapSplit
operator|.
name|getSplitNum
argument_list|()
argument_list|,
name|llapClient
operator|.
name|getAddress
argument_list|()
argument_list|,
name|submitWorkInfo
operator|.
name|getToken
argument_list|()
argument_list|)
decl_stmt|;
name|TezEvent
name|tezEvent
init|=
operator|new
name|TezEvent
argument_list|()
decl_stmt|;
name|DataInputBuffer
name|dib
init|=
operator|new
name|DataInputBuffer
argument_list|()
decl_stmt|;
name|dib
operator|.
name|reset
argument_list|(
name|llapSplit
operator|.
name|getFragmentBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|llapSplit
operator|.
name|getFragmentBytes
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|tezEvent
operator|.
name|readFields
argument_list|(
name|dib
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TezEvent
argument_list|>
name|tezEventList
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|tezEventList
operator|.
name|add
argument_list|(
name|tezEvent
argument_list|)
expr_stmt|;
comment|// this is just the portion that sets up the io to receive data
name|String
name|host
init|=
name|split
operator|.
name|getLocations
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|llapClient
operator|.
name|submitWork
argument_list|(
name|submitWorkRequestProto
argument_list|,
name|host
argument_list|,
name|llapSubmitPort
argument_list|,
name|tezEventList
argument_list|)
expr_stmt|;
name|String
name|id
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|job
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYID
argument_list|)
operator|+
literal|"_"
operator|+
name|llapSplit
operator|.
name|getSplitNum
argument_list|()
decl_stmt|;
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|Socket
name|socket
init|=
operator|new
name|Socket
argument_list|(
name|host
argument_list|,
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_DAEMON_OUTPUT_SERVICE_PORT
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Socket connected"
argument_list|)
expr_stmt|;
name|socket
operator|.
name|getOutputStream
argument_list|()
operator|.
name|write
argument_list|(
name|id
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|socket
operator|.
name|getOutputStream
argument_list|()
operator|.
name|write
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|socket
operator|.
name|getOutputStream
argument_list|()
operator|.
name|flush
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Registered id: "
operator|+
name|id
argument_list|)
expr_stmt|;
return|return
operator|new
name|LlapRecordReader
argument_list|(
name|socket
operator|.
name|getInputStream
argument_list|()
argument_list|,
name|llapSplit
operator|.
name|getSchema
argument_list|()
argument_list|,
name|Text
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|InputSplit
index|[]
name|getSplits
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|int
name|numSplits
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"These are not the splits you are looking for."
argument_list|)
throw|;
block|}
specifier|private
name|SubmitWorkRequestProto
name|constructSubmitWorkRequestProto
parameter_list|(
name|SubmitWorkInfo
name|submitWorkInfo
parameter_list|,
name|int
name|taskNum
parameter_list|,
name|InetSocketAddress
name|address
parameter_list|,
name|Token
argument_list|<
name|JobTokenIdentifier
argument_list|>
name|token
parameter_list|)
throws|throws
name|IOException
block|{
name|TaskSpec
name|taskSpec
init|=
name|submitWorkInfo
operator|.
name|getTaskSpec
argument_list|()
decl_stmt|;
name|ApplicationId
name|appId
init|=
name|submitWorkInfo
operator|.
name|getFakeAppId
argument_list|()
decl_stmt|;
name|SubmitWorkRequestProto
operator|.
name|Builder
name|builder
init|=
name|SubmitWorkRequestProto
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
comment|// This works, assuming the executor is running within YARN.
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting user in submitWorkRequest to: "
operator|+
name|System
operator|.
name|getenv
argument_list|(
name|ApplicationConstants
operator|.
name|Environment
operator|.
name|USER
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setUser
argument_list|(
name|System
operator|.
name|getenv
argument_list|(
name|ApplicationConstants
operator|.
name|Environment
operator|.
name|USER
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setApplicationIdString
argument_list|(
name|appId
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setAppAttemptNumber
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setTokenIdentifier
argument_list|(
name|appId
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|ContainerId
name|containerId
init|=
name|ContainerId
operator|.
name|newInstance
argument_list|(
name|ApplicationAttemptId
operator|.
name|newInstance
argument_list|(
name|appId
argument_list|,
literal|0
argument_list|)
argument_list|,
name|taskNum
argument_list|)
decl_stmt|;
name|builder
operator|.
name|setContainerIdString
argument_list|(
name|containerId
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setAmHost
argument_list|(
name|address
operator|.
name|getHostName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setAmPort
argument_list|(
name|address
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|Credentials
name|taskCredentials
init|=
operator|new
name|Credentials
argument_list|()
decl_stmt|;
comment|// Credentials can change across DAGs. Ideally construct only once per DAG.
comment|// TODO Figure out where credentials will come from. Normally Hive sets up
comment|// URLs on the tez dag, for which Tez acquires credentials.
comment|//    taskCredentials.addAll(getContext().getCredentials());
comment|//    Preconditions.checkState(currentQueryIdentifierProto.getDagIdentifier() ==
comment|//        taskSpec.getTaskAttemptID().getTaskID().getVertexID().getDAGId().getId());
comment|//    ByteBuffer credentialsBinary = credentialMap.get(currentQueryIdentifierProto);
comment|//    if (credentialsBinary == null) {
comment|//      credentialsBinary = serializeCredentials(getContext().getCredentials());
comment|//      credentialMap.putIfAbsent(currentQueryIdentifierProto, credentialsBinary.duplicate());
comment|//    } else {
comment|//      credentialsBinary = credentialsBinary.duplicate();
comment|//    }
comment|//    builder.setCredentialsBinary(ByteString.copyFrom(credentialsBinary));
name|Credentials
name|credentials
init|=
operator|new
name|Credentials
argument_list|()
decl_stmt|;
name|TokenCache
operator|.
name|setSessionToken
argument_list|(
name|token
argument_list|,
name|credentials
argument_list|)
expr_stmt|;
name|ByteBuffer
name|credentialsBinary
init|=
name|serializeCredentials
argument_list|(
name|credentials
argument_list|)
decl_stmt|;
name|builder
operator|.
name|setCredentialsBinary
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|credentialsBinary
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setFragmentSpec
argument_list|(
name|Converters
operator|.
name|convertTaskSpecToProto
argument_list|(
name|taskSpec
argument_list|)
argument_list|)
expr_stmt|;
name|FragmentRuntimeInfo
operator|.
name|Builder
name|runtimeInfo
init|=
name|FragmentRuntimeInfo
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|runtimeInfo
operator|.
name|setCurrentAttemptStartTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|runtimeInfo
operator|.
name|setWithinDagPriority
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|runtimeInfo
operator|.
name|setDagStartTime
argument_list|(
name|submitWorkInfo
operator|.
name|getCreationTime
argument_list|()
argument_list|)
expr_stmt|;
name|runtimeInfo
operator|.
name|setFirstAttemptStartTime
argument_list|(
name|submitWorkInfo
operator|.
name|getCreationTime
argument_list|()
argument_list|)
expr_stmt|;
name|runtimeInfo
operator|.
name|setNumSelfAndUpstreamTasks
argument_list|(
name|taskSpec
operator|.
name|getVertexParallelism
argument_list|()
argument_list|)
expr_stmt|;
name|runtimeInfo
operator|.
name|setNumSelfAndUpstreamCompletedTasks
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setUsingTezAm
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setFragmentRuntimeInfo
argument_list|(
name|runtimeInfo
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
name|ByteBuffer
name|serializeCredentials
parameter_list|(
name|Credentials
name|credentials
parameter_list|)
throws|throws
name|IOException
block|{
name|Credentials
name|containerCredentials
init|=
operator|new
name|Credentials
argument_list|()
decl_stmt|;
name|containerCredentials
operator|.
name|addAll
argument_list|(
name|credentials
argument_list|)
expr_stmt|;
name|DataOutputBuffer
name|containerTokens_dob
init|=
operator|new
name|DataOutputBuffer
argument_list|()
decl_stmt|;
name|containerCredentials
operator|.
name|writeTokenStorageToStream
argument_list|(
name|containerTokens_dob
argument_list|)
expr_stmt|;
return|return
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|containerTokens_dob
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|containerTokens_dob
operator|.
name|getLength
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

