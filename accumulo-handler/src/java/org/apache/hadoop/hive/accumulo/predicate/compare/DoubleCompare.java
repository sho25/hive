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

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigDecimal
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

begin_comment
comment|/**  * Set of comparison operations over a double constant. Used for Hive predicates involving double  * comparison.  *  * Used by {@link org.apache.hadoop.hive.accumulo.predicate.PrimitiveComparisonFilter}  */
end_comment

begin_class
specifier|public
class|class
name|DoubleCompare
implements|implements
name|PrimitiveComparison
block|{
specifier|private
name|BigDecimal
name|constant
decl_stmt|;
comment|/**      *      */
specifier|public
name|void
name|init
parameter_list|(
name|byte
index|[]
name|constant
parameter_list|)
block|{
name|this
operator|.
name|constant
operator|=
name|serialize
argument_list|(
name|constant
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return BigDecimal holding double byte [] value    */
specifier|public
name|BigDecimal
name|serialize
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
try|try
block|{
return|return
operator|new
name|BigDecimal
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|value
argument_list|)
operator|.
name|asDoubleBuffer
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
operator|.
name|toString
argument_list|()
operator|+
literal|" occurred trying to build double value. "
operator|+
literal|"Make sure the value type for the byte[] is double."
argument_list|)
throw|;
block|}
block|}
comment|/**    * @return true if double value is equal to constant, false otherwise.    */
annotation|@
name|Override
specifier|public
name|boolean
name|isEqual
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|serialize
argument_list|(
name|value
argument_list|)
operator|.
name|compareTo
argument_list|(
name|constant
argument_list|)
operator|==
literal|0
return|;
block|}
comment|/**    * @return true if double value not equal to constant, false otherwise.    */
annotation|@
name|Override
specifier|public
name|boolean
name|isNotEqual
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|serialize
argument_list|(
name|value
argument_list|)
operator|.
name|compareTo
argument_list|(
name|constant
argument_list|)
operator|!=
literal|0
return|;
block|}
comment|/**    * @return true if value greater than or equal to constant, false otherwise.    */
annotation|@
name|Override
specifier|public
name|boolean
name|greaterThanOrEqual
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|serialize
argument_list|(
name|value
argument_list|)
operator|.
name|compareTo
argument_list|(
name|constant
argument_list|)
operator|>=
literal|0
return|;
block|}
comment|/**    * @return true if value greater than constant, false otherwise.    */
annotation|@
name|Override
specifier|public
name|boolean
name|greaterThan
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|serialize
argument_list|(
name|value
argument_list|)
operator|.
name|compareTo
argument_list|(
name|constant
argument_list|)
operator|>
literal|0
return|;
block|}
comment|/**    * @return true if value less than or equal than constant, false otherwise.    */
annotation|@
name|Override
specifier|public
name|boolean
name|lessThanOrEqual
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|serialize
argument_list|(
name|value
argument_list|)
operator|.
name|compareTo
argument_list|(
name|constant
argument_list|)
operator|<=
literal|0
return|;
block|}
comment|/**    * @return true if value less than constant, false otherwise.    */
annotation|@
name|Override
specifier|public
name|boolean
name|lessThan
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|serialize
argument_list|(
name|value
argument_list|)
operator|.
name|compareTo
argument_list|(
name|constant
argument_list|)
operator|<
literal|0
return|;
block|}
comment|/**    * not supported for this PrimitiveCompare implementation.    */
annotation|@
name|Override
specifier|public
name|boolean
name|like
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Like not supported for "
operator|+
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

