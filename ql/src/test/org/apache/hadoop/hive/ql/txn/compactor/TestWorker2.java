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
name|ql
operator|.
name|txn
operator|.
name|compactor
package|;
end_package

begin_comment
comment|/**  * Same as TestWorker but tests delta file names in Hive 1.3.0 format   */
end_comment

begin_class
specifier|public
class|class
name|TestWorker2
extends|extends
name|TestWorker
block|{
specifier|public
name|TestWorker2
parameter_list|()
throws|throws
name|Exception
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
name|boolean
name|useHive130DeltaDirName
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

