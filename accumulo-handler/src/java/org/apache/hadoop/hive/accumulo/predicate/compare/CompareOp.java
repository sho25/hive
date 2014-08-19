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
name|accumulo
operator|.
name|predicate
operator|.
name|compare
package|;
end_package

begin_comment
comment|/**  * Handles different types of comparisons in hive predicates. Filter iterator delegates value  * acceptance to the CompareOpt.  *  * Used by {@link org.apache.hadoop.hive.accumulo.predicate.PrimitiveComparisonFilter}. Works with  * {@link PrimitiveComparison}  */
end_comment

begin_interface
specifier|public
interface|interface
name|CompareOp
block|{
comment|/**    * Sets the PrimitiveComparison for this CompareOp    */
specifier|public
name|void
name|setPrimitiveCompare
parameter_list|(
name|PrimitiveComparison
name|comp
parameter_list|)
function_decl|;
comment|/**    * @return The PrimitiveComparison this CompareOp is a part of    */
specifier|public
name|PrimitiveComparison
name|getPrimitiveCompare
parameter_list|()
function_decl|;
comment|/**    * @param val The bytes from the Accumulo Value    * @return true if the value is accepted by this CompareOp    */
specifier|public
name|boolean
name|accept
parameter_list|(
name|byte
index|[]
name|val
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

