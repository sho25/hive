begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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
operator|.
name|ext
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentMap
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
name|protocol
operator|.
name|LlapTaskUmbilicalProtocol
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
name|LlapProtocolClientProxy
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
name|tezplugins
operator|.
name|helpers
operator|.
name|LlapTaskUmbilicalServer
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
name|ipc
operator|.
name|ProtocolSignature
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
name|service
operator|.
name|AbstractService
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
name|dag
operator|.
name|api
operator|.
name|TezException
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
name|records
operator|.
name|TezTaskAttemptID
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
name|impl
operator|.
name|TezEvent
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
name|TezHeartbeatRequest
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
name|TezHeartbeatResponse
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
name|LlapTaskUmbilicalExternalClient
extends|extends
name|AbstractService
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
name|LlapTaskUmbilicalExternalClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|LlapProtocolClientProxy
name|communicator
decl_stmt|;
specifier|private
specifier|volatile
name|LlapTaskUmbilicalServer
name|llapTaskUmbilicalServer
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|LlapTaskUmbilicalProtocol
name|umbilical
decl_stmt|;
specifier|protected
specifier|final
name|String
name|tokenIdentifier
decl_stmt|;
specifier|protected
specifier|final
name|Token
argument_list|<
name|JobTokenIdentifier
argument_list|>
name|sessionToken
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|TezEvent
argument_list|>
argument_list|>
name|pendingEvents
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// TODO KKK Work out the details of the tokenIdentifier, and the session token.
comment|// It may just be possible to create one here - since Shuffle is not involved, and this is only used
comment|// for communication from LLAP-Daemons to the server. It will need to be sent in as part
comment|// of the job submission request.
specifier|public
name|LlapTaskUmbilicalExternalClient
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|tokenIdentifier
parameter_list|,
name|Token
argument_list|<
name|JobTokenIdentifier
argument_list|>
name|sessionToken
parameter_list|)
block|{
name|super
argument_list|(
name|LlapTaskUmbilicalExternalClient
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|umbilical
operator|=
operator|new
name|LlapTaskUmbilicalExternalImpl
argument_list|()
expr_stmt|;
name|this
operator|.
name|tokenIdentifier
operator|=
name|tokenIdentifier
expr_stmt|;
name|this
operator|.
name|sessionToken
operator|=
name|sessionToken
expr_stmt|;
comment|// TODO. No support for the LLAP token yet. Add support for configurable threads, however 1 should always be enough.
name|this
operator|.
name|communicator
operator|=
operator|new
name|LlapProtocolClientProxy
argument_list|(
literal|1
argument_list|,
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serviceStart
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|numHandlers
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_TMP_EXT_CLIENT_NUM_SERVER_HANDLERS
argument_list|)
decl_stmt|;
name|llapTaskUmbilicalServer
operator|=
operator|new
name|LlapTaskUmbilicalServer
argument_list|(
name|conf
argument_list|,
name|umbilical
argument_list|,
name|numHandlers
argument_list|,
name|tokenIdentifier
argument_list|,
name|sessionToken
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serviceStop
parameter_list|()
block|{
name|llapTaskUmbilicalServer
operator|.
name|shutdownServer
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|communicator
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|communicator
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Submit the work for actual execution. This should always have the usingTezAm flag disabled    * @param submitWorkRequestProto    */
specifier|public
name|void
name|submitWork
parameter_list|(
specifier|final
name|SubmitWorkRequestProto
name|submitWorkRequestProto
parameter_list|,
name|String
name|llapHost
parameter_list|,
name|int
name|llapPort
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|submitWorkRequestProto
operator|.
name|getUsingTezAm
argument_list|()
operator|==
literal|false
argument_list|)
expr_stmt|;
comment|// Store the actual event first. To be returned on the first heartbeat.
name|Event
name|mrInputEvent
init|=
literal|null
decl_stmt|;
comment|// Construct a TezEvent out of this, to send it out on the next heaertbeat
comment|//    submitWorkRequestProto.getFragmentSpec().getFragmentIdentifierString()
comment|// Send out the actual SubmitWorkRequest
name|communicator
operator|.
name|sendSubmitWork
argument_list|(
name|submitWorkRequestProto
argument_list|,
name|llapHost
argument_list|,
name|llapPort
argument_list|,
operator|new
name|LlapProtocolClientProxy
operator|.
name|ExecuteRequestCallback
argument_list|<
name|LlapDaemonProtocolProtos
operator|.
name|SubmitWorkResponseProto
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|setResponse
parameter_list|(
name|LlapDaemonProtocolProtos
operator|.
name|SubmitWorkResponseProto
name|response
parameter_list|)
block|{
if|if
condition|(
name|response
operator|.
name|hasSubmissionState
argument_list|()
condition|)
block|{
if|if
condition|(
name|response
operator|.
name|getSubmissionState
argument_list|()
operator|.
name|equals
argument_list|(
name|LlapDaemonProtocolProtos
operator|.
name|SubmissionStateProto
operator|.
name|REJECTED
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Fragment: "
operator|+
name|submitWorkRequestProto
operator|.
name|getFragmentSpec
argument_list|()
operator|.
name|getFragmentIdentifierString
argument_list|()
operator|+
literal|" rejected. Server Busy."
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"DBG: Submitted "
operator|+
name|submitWorkRequestProto
operator|.
name|getFragmentSpec
argument_list|()
operator|.
name|getFragmentIdentifierString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|indicateError
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to submit: "
operator|+
name|submitWorkRequestProto
operator|.
name|getFragmentSpec
argument_list|()
operator|.
name|getFragmentIdentifierString
argument_list|()
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|//    // TODO Also send out information saying that the fragment is finishable - if that is not already included in the main fragment.
comment|//    // This entire call is only required if we're doing more than scans. MRInput has no dependencies and is always finishable
comment|//    QueryIdentifierProto queryIdentifier = QueryIdentifierProto
comment|//        .newBuilder()
comment|//        .setAppIdentifier(submitWorkRequestProto.getApplicationIdString()).setDagIdentifier(submitWorkRequestProto.getFragmentSpec().getDagId())
comment|//        .build();
comment|//    LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto sourceStateUpdatedRequest =
comment|//        LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto.newBuilder().setQueryIdentifier(queryIdentifier).setState(
comment|//            LlapDaemonProtocolProtos.SourceStateProto.S_SUCCEEDED).
comment|//            setSrcName(TODO)
comment|//    communicator.sendSourceStateUpdate(LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto.newBuilder().setQueryIdentifier(submitWorkRequestProto.getFragmentSpec().getFragmentIdentifierString()).set);
block|}
comment|// TODO Ideally, the server should be shared across all client sessions running on the same node.
specifier|private
class|class
name|LlapTaskUmbilicalExternalImpl
implements|implements
name|LlapTaskUmbilicalProtocol
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|canCommit
parameter_list|(
name|TezTaskAttemptID
name|taskid
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Expecting only a single instance of a task to be running.
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|TezHeartbeatResponse
name|heartbeat
parameter_list|(
name|TezHeartbeatRequest
name|request
parameter_list|)
throws|throws
name|IOException
throws|,
name|TezException
block|{
comment|// Keep-alive information. The client should be informed and will have to take care of re-submitting the work.
comment|// Some parts of fault tolerance go here.
comment|// This also provides completion information, and a possible notification when task actually starts running (first heartbeat)
comment|// Incoming events can be ignored until the point when shuffle needs to be handled, instead of just scans.
name|TezHeartbeatResponse
name|response
init|=
operator|new
name|TezHeartbeatResponse
argument_list|()
decl_stmt|;
comment|// Assuming TaskAttemptId and FragmentIdentifierString are the same. Verify this.
name|TezTaskAttemptID
name|taskAttemptId
init|=
name|request
operator|.
name|getCurrentTaskAttemptID
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"ZZZ: DBG: Received heartbeat from taskAttemptId: "
operator|+
name|taskAttemptId
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TezEvent
argument_list|>
name|tezEvents
init|=
name|pendingEvents
operator|.
name|remove
argument_list|(
name|taskAttemptId
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|response
operator|.
name|setLastRequestId
argument_list|(
name|request
operator|.
name|getRequestId
argument_list|()
argument_list|)
expr_stmt|;
comment|// Irrelevant from eventIds. This can be tracked in the AM itself, instead of polluting the task.
comment|// Also since we have all the MRInput events here - they'll all be sent in together.
name|response
operator|.
name|setNextFromEventId
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// Irrelevant. See comment above.
name|response
operator|.
name|setNextPreRoutedEventId
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|//Irrelevant. See comment above.
name|response
operator|.
name|setEvents
argument_list|(
name|tezEvents
argument_list|)
expr_stmt|;
comment|// TODO KKK: Should ideally handle things like Task success notifications.
comment|// Can this somehow be hooked into the LlapTaskCommunicator to make testing easy
return|return
name|response
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|nodeHeartbeat
parameter_list|(
name|Text
name|hostname
parameter_list|,
name|int
name|port
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO Eventually implement - to handle keep-alive messages from pending work.
block|}
annotation|@
name|Override
specifier|public
name|void
name|taskKilled
parameter_list|(
name|TezTaskAttemptID
name|taskAttemptId
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO Eventually implement - to handle preemptions within LLAP daemons.
block|}
annotation|@
name|Override
specifier|public
name|long
name|getProtocolVersion
parameter_list|(
name|String
name|protocol
parameter_list|,
name|long
name|clientVersion
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|ProtocolSignature
name|getProtocolSignature
parameter_list|(
name|String
name|protocol
parameter_list|,
name|long
name|clientVersion
parameter_list|,
name|int
name|clientMethodsHash
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ProtocolSignature
operator|.
name|getProtocolSignature
argument_list|(
name|this
argument_list|,
name|protocol
argument_list|,
name|clientVersion
argument_list|,
name|clientMethodsHash
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

