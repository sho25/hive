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
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatException
import|;
end_import

begin_comment
comment|/**  * Interface to serialize HCat API elements.  */
end_comment

begin_class
specifier|abstract
class|class
name|MetadataSerializer
block|{
comment|// Prevent construction outside the get() method.
specifier|protected
name|MetadataSerializer
parameter_list|()
block|{}
comment|/**    * Static getter method for the appropriate MetadataSerializer implementation.    * @return MetadataSerializer sub-class.    * @throws HCatException On failure to construct a concrete MetadataSerializer.    */
specifier|public
specifier|static
name|MetadataSerializer
name|get
parameter_list|()
throws|throws
name|HCatException
block|{
return|return
operator|new
name|MetadataJSONSerializer
argument_list|()
return|;
block|}
comment|/**    * Serializer for HCatTable instances.    * @param hcatTable The HCatTable operand, to be serialized.    * @return Serialized (i.e. String-ified) HCatTable.    * @throws HCatException On failure to serialize.    */
specifier|public
specifier|abstract
name|String
name|serializeTable
parameter_list|(
name|HCatTable
name|hcatTable
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**    * Deserializer for HCatTable string-representations.    * @param hcatTableStringRep Serialized HCatTable String (gotten from serializeTable()).    * @return Deserialized HCatTable instance.    * @throws HCatException On failure to deserialize (e.g. incompatible serialization format, etc.)    */
specifier|public
specifier|abstract
name|HCatTable
name|deserializeTable
parameter_list|(
name|String
name|hcatTableStringRep
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**    * Serializer for HCatPartition instances.    * @param hcatPartition The HCatPartition operand, to be serialized.    * @return Serialized (i.e. String-ified) HCatPartition.    * @throws HCatException On failure to serialize.    */
specifier|public
specifier|abstract
name|String
name|serializePartition
parameter_list|(
name|HCatPartition
name|hcatPartition
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**    * Deserializer for HCatPartition string-representations.    * @param hcatPartitionStringRep Serialized HCatPartition String (gotten from serializePartition()).    * @return Deserialized HCatPartition instance.    * @throws HCatException On failure to deserialize (e.g. incompatible serialization format, etc.)    */
specifier|public
specifier|abstract
name|HCatPartition
name|deserializePartition
parameter_list|(
name|String
name|hcatPartitionStringRep
parameter_list|)
throws|throws
name|HCatException
function_decl|;
block|}
end_class

end_unit

