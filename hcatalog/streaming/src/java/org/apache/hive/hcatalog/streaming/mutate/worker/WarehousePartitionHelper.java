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
name|worker
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
name|LinkedHashMap
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
name|hive
operator|.
name|metastore
operator|.
name|Warehouse
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

begin_comment
comment|/**  * A {@link PartitionHelper} implementation that uses the {@link Warehouse} class to obtain partition path information.  * As this does not require a connection to the meta store database it is safe to use in workers that are distributed on  * a cluster. However, it does not support the creation of new partitions so you will need to provide a mechanism to  * collect affected partitions in your merge job and create them from your client.  */
end_comment

begin_class
class|class
name|WarehousePartitionHelper
implements|implements
name|PartitionHelper
block|{
specifier|private
specifier|final
name|Warehouse
name|warehouse
decl_stmt|;
specifier|private
specifier|final
name|Path
name|tablePath
decl_stmt|;
specifier|private
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitions
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|partitionColumns
decl_stmt|;
name|WarehousePartitionHelper
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|Path
name|tablePath
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partitionColumns
parameter_list|)
throws|throws
name|MetaException
block|{
name|this
operator|.
name|tablePath
operator|=
name|tablePath
expr_stmt|;
name|this
operator|.
name|partitionColumns
operator|=
name|partitionColumns
expr_stmt|;
name|this
operator|.
name|partitions
operator|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|(
name|partitionColumns
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|partitionColumn
range|:
name|partitionColumns
control|)
block|{
name|partitions
operator|.
name|put
argument_list|(
name|partitionColumn
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|warehouse
operator|=
operator|new
name|Warehouse
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Path
name|getPathForPartition
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|partitionValues
parameter_list|)
throws|throws
name|WorkerException
block|{
if|if
condition|(
name|partitionValues
operator|.
name|size
argument_list|()
operator|!=
name|partitionColumns
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Incorrect number of partition values. columns="
operator|+
name|partitionColumns
operator|+
literal|",values="
operator|+
name|partitionValues
argument_list|)
throw|;
block|}
if|if
condition|(
name|partitionColumns
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|tablePath
return|;
block|}
for|for
control|(
name|int
name|columnIndex
init|=
literal|0
init|;
name|columnIndex
operator|<
name|partitionValues
operator|.
name|size
argument_list|()
condition|;
name|columnIndex
operator|++
control|)
block|{
name|String
name|partitionColumn
init|=
name|partitionColumns
operator|.
name|get
argument_list|(
name|columnIndex
argument_list|)
decl_stmt|;
name|String
name|partitionValue
init|=
name|partitionValues
operator|.
name|get
argument_list|(
name|columnIndex
argument_list|)
decl_stmt|;
name|partitions
operator|.
name|put
argument_list|(
name|partitionColumn
argument_list|,
name|partitionValue
argument_list|)
expr_stmt|;
block|}
try|try
block|{
return|return
name|warehouse
operator|.
name|getPartitionPath
argument_list|(
name|tablePath
argument_list|,
name|partitions
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|WorkerException
argument_list|(
literal|"Unable to determine partition path. tablePath="
operator|+
name|tablePath
operator|+
literal|",partition="
operator|+
name|partitionValues
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/** Throws {@link UnsupportedOperationException}. */
annotation|@
name|Override
specifier|public
name|void
name|createPartitionIfNotExists
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|newPartitionValues
parameter_list|)
throws|throws
name|WorkerException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"You require a connection to the meta store to do this."
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Nothing to close here.
block|}
block|}
end_class

end_unit

