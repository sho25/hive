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
name|MetadataReader
import|;
end_import

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
name|MetadataReaderProperties
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_interface
specifier|public
interface|interface
name|MetadataReaderFactory
block|{
name|MetadataReader
name|create
parameter_list|(
name|MetadataReaderProperties
name|properties
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

