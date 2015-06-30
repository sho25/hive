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
name|streaming
operator|.
name|mutate
operator|.
name|worker
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
name|hive
operator|.
name|ql
operator|.
name|io
operator|.
name|RecordIdentifier
import|;
end_import

begin_comment
comment|/** Provide a means to extract {@link RecordIdentifier} from record objects. */
end_comment

begin_interface
specifier|public
interface|interface
name|RecordInspector
block|{
comment|/** Get the {@link RecordIdentifier} from the record - to be used for updates and deletes only. */
name|RecordIdentifier
name|extractRecordIdentifier
parameter_list|(
name|Object
name|record
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

