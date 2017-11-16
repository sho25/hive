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
name|exec
operator|.
name|tez
package|;
end_package

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
name|javax
operator|.
name|management
operator|.
name|MXBean
import|;
end_import

begin_comment
comment|/**  * MXbean to expose cache allocator related information through JMX.  */
end_comment

begin_interface
annotation|@
name|MXBean
specifier|public
interface|interface
name|WorkloadManagerMxBean
block|{
comment|/**    * @return The text-based description of current WM state.    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getWmStateDescription
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

