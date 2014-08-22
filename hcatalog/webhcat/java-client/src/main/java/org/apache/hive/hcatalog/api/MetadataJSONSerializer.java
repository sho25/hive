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
name|api
package|;
end_package

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
name|Partition
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
name|common
operator|.
name|HCatException
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
name|TDeserializer
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
name|apache
operator|.
name|thrift
operator|.
name|TSerializer
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
name|protocol
operator|.
name|TJSONProtocol
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
comment|/**  * MetadataSerializer implementation, that serializes HCat API elements into JSON.  */
end_comment

begin_class
class|class
name|MetadataJSONSerializer
extends|extends
name|MetadataSerializer
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
name|MetadataJSONSerializer
operator|.
name|class
argument_list|)
decl_stmt|;
name|MetadataJSONSerializer
parameter_list|()
throws|throws
name|HCatException
block|{}
annotation|@
name|Override
specifier|public
name|String
name|serializeTable
parameter_list|(
name|HCatTable
name|hcatTable
parameter_list|)
throws|throws
name|HCatException
block|{
try|try
block|{
return|return
operator|new
name|TSerializer
argument_list|(
operator|new
name|TJSONProtocol
operator|.
name|Factory
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|(
name|hcatTable
operator|.
name|toHiveTable
argument_list|()
argument_list|,
literal|"UTF-8"
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|TException
name|exception
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"Could not serialize HCatTable: "
operator|+
name|hcatTable
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|HCatTable
name|deserializeTable
parameter_list|(
name|String
name|hcatTableStringRep
parameter_list|)
throws|throws
name|HCatException
block|{
try|try
block|{
name|Table
name|table
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
operator|new
name|TDeserializer
argument_list|(
operator|new
name|TJSONProtocol
operator|.
name|Factory
argument_list|()
argument_list|)
operator|.
name|deserialize
argument_list|(
name|table
argument_list|,
name|hcatTableStringRep
argument_list|,
literal|"UTF-8"
argument_list|)
expr_stmt|;
return|return
operator|new
name|HCatTable
argument_list|(
name|table
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|TException
name|exception
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|debug
argument_list|(
literal|"Could not de-serialize from: "
operator|+
name|hcatTableStringRep
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"Could not de-serialize HCatTable."
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|serializePartition
parameter_list|(
name|HCatPartition
name|hcatPartition
parameter_list|)
throws|throws
name|HCatException
block|{
try|try
block|{
return|return
operator|new
name|TSerializer
argument_list|(
operator|new
name|TJSONProtocol
operator|.
name|Factory
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|(
name|hcatPartition
operator|.
name|toHivePartition
argument_list|()
argument_list|,
literal|"UTF-8"
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|TException
name|exception
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"Could not serialize HCatPartition: "
operator|+
name|hcatPartition
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|HCatPartition
name|deserializePartition
parameter_list|(
name|String
name|hcatPartitionStringRep
parameter_list|)
throws|throws
name|HCatException
block|{
try|try
block|{
name|Partition
name|partition
init|=
operator|new
name|Partition
argument_list|()
decl_stmt|;
operator|new
name|TDeserializer
argument_list|(
operator|new
name|TJSONProtocol
operator|.
name|Factory
argument_list|()
argument_list|)
operator|.
name|deserialize
argument_list|(
name|partition
argument_list|,
name|hcatPartitionStringRep
argument_list|,
literal|"UTF-8"
argument_list|)
expr_stmt|;
return|return
operator|new
name|HCatPartition
argument_list|(
literal|null
argument_list|,
name|partition
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|TException
name|exception
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|debug
argument_list|(
literal|"Could not de-serialize partition from: "
operator|+
name|hcatPartitionStringRep
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"Could not de-serialize HCatPartition."
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

