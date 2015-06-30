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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|NoSuchObjectException
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
operator|.
name|Lock
import|;
end_import

begin_import
import|import
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
operator|.
name|LockFailureListener
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
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
comment|/**  * Responsible for orchestrating {@link Transaction Transactions} within which ACID table mutation events can occur.  * Typically this will be a large batch of delta operations.  */
end_comment

begin_class
specifier|public
class|class
name|MutatorClient
implements|implements
name|Closeable
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
name|MutatorClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TRANSACTIONAL_PARAM_KEY
init|=
literal|"transactional"
decl_stmt|;
specifier|private
specifier|final
name|IMetaStoreClient
name|metaStoreClient
decl_stmt|;
specifier|private
specifier|final
name|Lock
operator|.
name|Options
name|lockOptions
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|AcidTable
argument_list|>
name|tables
decl_stmt|;
specifier|private
name|boolean
name|connected
decl_stmt|;
name|MutatorClient
parameter_list|(
name|IMetaStoreClient
name|metaStoreClient
parameter_list|,
name|HiveConf
name|configuration
parameter_list|,
name|LockFailureListener
name|lockFailureListener
parameter_list|,
name|String
name|user
parameter_list|,
name|Collection
argument_list|<
name|AcidTable
argument_list|>
name|tables
parameter_list|)
block|{
name|this
operator|.
name|metaStoreClient
operator|=
name|metaStoreClient
expr_stmt|;
name|this
operator|.
name|tables
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|tables
argument_list|)
argument_list|)
expr_stmt|;
name|lockOptions
operator|=
operator|new
name|Lock
operator|.
name|Options
argument_list|()
operator|.
name|configuration
argument_list|(
name|configuration
argument_list|)
operator|.
name|lockFailureListener
argument_list|(
name|lockFailureListener
operator|==
literal|null
condition|?
name|LockFailureListener
operator|.
name|NULL_LISTENER
else|:
name|lockFailureListener
argument_list|)
operator|.
name|user
argument_list|(
name|user
argument_list|)
expr_stmt|;
for|for
control|(
name|AcidTable
name|table
range|:
name|tables
control|)
block|{
name|lockOptions
operator|.
name|addTable
argument_list|(
name|table
operator|.
name|getDatabaseName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Connects to the {@link IMetaStoreClient meta store} that will be used to manage {@link Transaction} life-cycles.    * Also checks that the tables destined to receive mutation events are able to do so. The client should only hold one    * open transaction at any given time (TODO: enforce this).    */
specifier|public
name|void
name|connect
parameter_list|()
throws|throws
name|ConnectionException
block|{
if|if
condition|(
name|connected
condition|)
block|{
throw|throw
operator|new
name|ConnectionException
argument_list|(
literal|"Already connected."
argument_list|)
throw|;
block|}
for|for
control|(
name|AcidTable
name|table
range|:
name|tables
control|)
block|{
name|checkTable
argument_list|(
name|metaStoreClient
argument_list|,
name|table
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Connected to end point {}"
argument_list|,
name|metaStoreClient
argument_list|)
expr_stmt|;
name|connected
operator|=
literal|true
expr_stmt|;
block|}
comment|/** Creates a new {@link Transaction} by opening a transaction with the {@link IMetaStoreClient meta store}. */
specifier|public
name|Transaction
name|newTransaction
parameter_list|()
throws|throws
name|TransactionException
block|{
if|if
condition|(
operator|!
name|connected
condition|)
block|{
throw|throw
operator|new
name|TransactionException
argument_list|(
literal|"Not connected - cannot create transaction."
argument_list|)
throw|;
block|}
name|Transaction
name|transaction
init|=
operator|new
name|Transaction
argument_list|(
name|metaStoreClient
argument_list|,
name|lockOptions
argument_list|)
decl_stmt|;
for|for
control|(
name|AcidTable
name|table
range|:
name|tables
control|)
block|{
name|table
operator|.
name|setTransactionId
argument_list|(
name|transaction
operator|.
name|getTransactionId
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Created transaction {}"
argument_list|,
name|transaction
argument_list|)
expr_stmt|;
return|return
name|transaction
return|;
block|}
comment|/** Did the client connect successfully. Note the the client may have since become disconnected. */
specifier|public
name|boolean
name|isConnected
parameter_list|()
block|{
return|return
name|connected
return|;
block|}
comment|/**    * Closes the client releasing any {@link IMetaStoreClient meta store} connections held. Does not notify any open    * transactions (TODO: perhaps it should?)    */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|metaStoreClient
operator|.
name|close
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Closed client."
argument_list|)
expr_stmt|;
name|connected
operator|=
literal|false
expr_stmt|;
block|}
comment|/**    * Returns the list of managed {@link AcidTable AcidTables} that can receive mutation events under the control of this    * client.    */
specifier|public
name|List
argument_list|<
name|AcidTable
argument_list|>
name|getTables
parameter_list|()
throws|throws
name|ConnectionException
block|{
if|if
condition|(
operator|!
name|connected
condition|)
block|{
throw|throw
operator|new
name|ConnectionException
argument_list|(
literal|"Not connected - cannot interrogate tables."
argument_list|)
throw|;
block|}
return|return
name|Collections
operator|.
expr|<
name|AcidTable
operator|>
name|unmodifiableList
argument_list|(
name|tables
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"MutatorClient [metaStoreClient="
operator|+
name|metaStoreClient
operator|+
literal|", connected="
operator|+
name|connected
operator|+
literal|"]"
return|;
block|}
specifier|private
name|void
name|checkTable
parameter_list|(
name|IMetaStoreClient
name|metaStoreClient
parameter_list|,
name|AcidTable
name|acidTable
parameter_list|)
throws|throws
name|ConnectionException
block|{
try|try
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Checking table {}."
argument_list|,
name|acidTable
operator|.
name|getQualifiedName
argument_list|()
argument_list|)
expr_stmt|;
name|Table
name|metaStoreTable
init|=
name|metaStoreClient
operator|.
name|getTable
argument_list|(
name|acidTable
operator|.
name|getDatabaseName
argument_list|()
argument_list|,
name|acidTable
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|acidTable
operator|.
name|getTableType
argument_list|()
operator|==
name|TableType
operator|.
name|SINK
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
init|=
name|metaStoreTable
operator|.
name|getParameters
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|parameters
operator|.
name|get
argument_list|(
name|TRANSACTIONAL_PARAM_KEY
argument_list|)
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ConnectionException
argument_list|(
literal|"Cannot stream to table that is not transactional: '"
operator|+
name|acidTable
operator|.
name|getQualifiedName
argument_list|()
operator|+
literal|"'."
argument_list|)
throw|;
block|}
name|int
name|totalBuckets
init|=
name|metaStoreTable
operator|.
name|getSd
argument_list|()
operator|.
name|getNumBuckets
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table {} has {} buckets."
argument_list|,
name|acidTable
operator|.
name|getQualifiedName
argument_list|()
argument_list|,
name|totalBuckets
argument_list|)
expr_stmt|;
if|if
condition|(
name|totalBuckets
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|ConnectionException
argument_list|(
literal|"Cannot stream to table that has not been bucketed: '"
operator|+
name|acidTable
operator|.
name|getQualifiedName
argument_list|()
operator|+
literal|"'."
argument_list|)
throw|;
block|}
name|String
name|outputFormat
init|=
name|metaStoreTable
operator|.
name|getSd
argument_list|()
operator|.
name|getOutputFormat
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table {} has {} OutputFormat."
argument_list|,
name|acidTable
operator|.
name|getQualifiedName
argument_list|()
argument_list|,
name|outputFormat
argument_list|)
expr_stmt|;
name|acidTable
operator|.
name|setTable
argument_list|(
name|metaStoreTable
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ConnectionException
argument_list|(
literal|"Invalid table '"
operator|+
name|acidTable
operator|.
name|getQualifiedName
argument_list|()
operator|+
literal|"'"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ConnectionException
argument_list|(
literal|"Error communicating with the meta store"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table {} OK."
argument_list|,
name|acidTable
operator|.
name|getQualifiedName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

