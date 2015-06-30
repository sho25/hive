begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|streaming
operator|.
name|mutate
operator|.
name|client
operator|.
name|lock
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Timer
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
name|TimeUnit
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
name|IMetaStoreClient
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

begin_comment
comment|/** Creates a default {@link HeartbeatTimerTask} for {@link Lock Locks}. */
end_comment

begin_class
class|class
name|HeartbeatFactory
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
name|HeartbeatFactory
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Creates a new {@link HeartbeatTimerTask} instance for the {@link Lock} and schedules it. */
name|Timer
name|newInstance
parameter_list|(
name|IMetaStoreClient
name|metaStoreClient
parameter_list|,
name|LockFailureListener
name|listener
parameter_list|,
name|Long
name|transactionId
parameter_list|,
name|Collection
argument_list|<
name|Table
argument_list|>
name|tableDescriptors
parameter_list|,
name|long
name|lockId
parameter_list|,
name|int
name|heartbeatPeriod
parameter_list|)
block|{
name|Timer
name|heartbeatTimer
init|=
operator|new
name|Timer
argument_list|(
literal|"hive-lock-heartbeat[lockId="
operator|+
name|lockId
operator|+
literal|", transactionId="
operator|+
name|transactionId
operator|+
literal|"]"
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|HeartbeatTimerTask
name|task
init|=
operator|new
name|HeartbeatTimerTask
argument_list|(
name|metaStoreClient
argument_list|,
name|listener
argument_list|,
name|transactionId
argument_list|,
name|tableDescriptors
argument_list|,
name|lockId
argument_list|)
decl_stmt|;
name|heartbeatTimer
operator|.
name|schedule
argument_list|(
name|task
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
operator|.
name|toMillis
argument_list|(
name|heartbeatPeriod
argument_list|)
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
operator|.
name|toMillis
argument_list|(
name|heartbeatPeriod
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Scheduled heartbeat timer task: {}"
argument_list|,
name|heartbeatTimer
argument_list|)
expr_stmt|;
return|return
name|heartbeatTimer
return|;
block|}
block|}
end_class

end_unit

