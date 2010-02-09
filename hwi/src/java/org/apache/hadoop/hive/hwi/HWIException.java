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
name|hwi
package|;
end_package

begin_comment
comment|/**  * HWIException.  *  */
end_comment

begin_class
specifier|public
class|class
name|HWIException
extends|extends
name|Exception
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
name|HWIException
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/** Specify an error String with the Exception. */
specifier|public
name|HWIException
parameter_list|(
name|String
name|arg0
parameter_list|)
block|{
name|super
argument_list|(
name|arg0
argument_list|)
expr_stmt|;
block|}
comment|/** Wrap an Exception in HWIException. */
specifier|public
name|HWIException
parameter_list|(
name|Throwable
name|arg0
parameter_list|)
block|{
name|super
argument_list|(
name|arg0
argument_list|)
expr_stmt|;
block|}
comment|/** Specify an error String and wrap an Exception in HWIException. */
specifier|public
name|HWIException
parameter_list|(
name|String
name|arg0
parameter_list|,
name|Throwable
name|arg1
parameter_list|)
block|{
name|super
argument_list|(
name|arg0
argument_list|,
name|arg1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

