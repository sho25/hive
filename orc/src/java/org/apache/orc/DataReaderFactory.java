begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|orc
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|impl
operator|.
name|DataReaderProperties
import|;
end_import

begin_interface
specifier|public
interface|interface
name|DataReaderFactory
block|{
name|DataReader
name|create
parameter_list|(
name|DataReaderProperties
name|properties
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

