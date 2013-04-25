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
name|udf
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * Interface to support use of standard UDFs inside the vectorized execution code path.  */
end_comment

begin_interface
specifier|public
interface|interface
name|IUDFUnaryString
block|{
name|Text
name|evaluate
parameter_list|(
name|Text
name|s
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

