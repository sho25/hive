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
name|llap
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|DataInputBuffer
import|;
end_import

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
name|DataOutputBuffer
import|;
end_import

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
name|Writable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|ApplicationId
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|impl
operator|.
name|TaskSpec
import|;
end_import

begin_class
specifier|public
class|class
name|SubmitWorkInfo
implements|implements
name|Writable
block|{
specifier|private
name|TaskSpec
name|taskSpec
decl_stmt|;
specifier|private
name|ApplicationId
name|fakeAppId
decl_stmt|;
specifier|public
name|SubmitWorkInfo
parameter_list|(
name|TaskSpec
name|taskSpec
parameter_list|,
name|ApplicationId
name|fakeAppId
parameter_list|)
block|{
name|this
operator|.
name|taskSpec
operator|=
name|taskSpec
expr_stmt|;
name|this
operator|.
name|fakeAppId
operator|=
name|fakeAppId
expr_stmt|;
block|}
comment|// Empty constructor for writable etc.
specifier|public
name|SubmitWorkInfo
parameter_list|()
block|{   }
specifier|public
name|TaskSpec
name|getTaskSpec
parameter_list|()
block|{
return|return
name|taskSpec
return|;
block|}
specifier|public
name|ApplicationId
name|getFakeAppId
parameter_list|()
block|{
return|return
name|fakeAppId
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|taskSpec
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|fakeAppId
operator|.
name|getClusterTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|fakeAppId
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|taskSpec
operator|=
operator|new
name|TaskSpec
argument_list|()
expr_stmt|;
name|taskSpec
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|long
name|appIdTs
init|=
name|in
operator|.
name|readLong
argument_list|()
decl_stmt|;
name|int
name|appIdId
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|fakeAppId
operator|=
name|ApplicationId
operator|.
name|newInstance
argument_list|(
name|appIdTs
argument_list|,
name|appIdId
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|byte
index|[]
name|toBytes
parameter_list|(
name|SubmitWorkInfo
name|submitWorkInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|DataOutputBuffer
name|dob
init|=
operator|new
name|DataOutputBuffer
argument_list|()
decl_stmt|;
name|submitWorkInfo
operator|.
name|write
argument_list|(
name|dob
argument_list|)
expr_stmt|;
return|return
name|dob
operator|.
name|getData
argument_list|()
return|;
block|}
specifier|public
name|SubmitWorkInfo
name|fromBytes
parameter_list|(
name|byte
index|[]
name|submitWorkInfoBytes
parameter_list|)
throws|throws
name|IOException
block|{
name|DataInputBuffer
name|dib
init|=
operator|new
name|DataInputBuffer
argument_list|()
decl_stmt|;
name|dib
operator|.
name|reset
argument_list|(
name|submitWorkInfoBytes
argument_list|,
literal|0
argument_list|,
name|submitWorkInfoBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|SubmitWorkInfo
name|submitWorkInfo
init|=
operator|new
name|SubmitWorkInfo
argument_list|()
decl_stmt|;
name|submitWorkInfo
operator|.
name|readFields
argument_list|(
name|dib
argument_list|)
expr_stmt|;
return|return
name|submitWorkInfo
return|;
block|}
block|}
end_class

end_unit

