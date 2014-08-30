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
name|io
operator|.
name|orc
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
import|;
end_import

begin_comment
comment|/**  * Statistics for Timestamp columns.  */
end_comment

begin_interface
specifier|public
interface|interface
name|TimestampColumnStatistics
extends|extends
name|ColumnStatistics
block|{
comment|/**    * Get the minimum value for the column.    * @return minimum value    */
name|Timestamp
name|getMinimum
parameter_list|()
function_decl|;
comment|/**    * Get the maximum value for the column.    * @return maximum value    */
name|Timestamp
name|getMaximum
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

