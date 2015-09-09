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
name|Closeable
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
name|fs
operator|.
name|Path
import|;
end_import

begin_comment
comment|/** Implementations are responsible for creating and obtaining path information about partitions. */
end_comment

begin_interface
interface|interface
name|PartitionHelper
extends|extends
name|Closeable
block|{
comment|/** Return the location of the partition described by the provided values. */
name|Path
name|getPathForPartition
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|newPartitionValues
parameter_list|)
throws|throws
name|WorkerException
function_decl|;
comment|/** Create the partition described by the provided values if it does not exist already. */
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
function_decl|;
block|}
end_interface

end_unit

