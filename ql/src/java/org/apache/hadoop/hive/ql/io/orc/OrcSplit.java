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
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
name|fs
operator|.
name|Path
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
name|hive
operator|.
name|ql
operator|.
name|io
operator|.
name|orc
operator|.
name|Reader
operator|.
name|FileMetaInfo
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
name|Text
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
name|WritableUtils
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
name|mapred
operator|.
name|FileSplit
import|;
end_import

begin_comment
comment|/**  * OrcFileSplit. Holds file meta info  *  */
end_comment

begin_class
specifier|public
class|class
name|OrcSplit
extends|extends
name|FileSplit
block|{
specifier|private
name|Reader
operator|.
name|FileMetaInfo
name|fileMetaInfo
decl_stmt|;
specifier|private
name|boolean
name|hasFooter
decl_stmt|;
specifier|protected
name|OrcSplit
parameter_list|()
block|{
comment|//The FileSplit() constructor in hadoop 0.20 and 1.x is package private so can't use it.
comment|//This constructor is used to create the object and then call readFields()
comment|// so just pass nulls to this super constructor.
name|super
argument_list|(
literal|null
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|OrcSplit
parameter_list|(
name|Path
name|path
parameter_list|,
name|long
name|offset
parameter_list|,
name|long
name|length
parameter_list|,
name|String
index|[]
name|hosts
parameter_list|,
name|FileMetaInfo
name|fileMetaInfo
parameter_list|)
block|{
name|super
argument_list|(
name|path
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|hosts
argument_list|)
expr_stmt|;
name|this
operator|.
name|fileMetaInfo
operator|=
name|fileMetaInfo
expr_stmt|;
name|hasFooter
operator|=
name|this
operator|.
name|fileMetaInfo
operator|!=
literal|null
expr_stmt|;
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
comment|//serialize path, offset, length using FileSplit
name|super
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
comment|// Whether footer information follows.
name|out
operator|.
name|writeBoolean
argument_list|(
name|hasFooter
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasFooter
condition|)
block|{
comment|// serialize FileMetaInfo fields
name|Text
operator|.
name|writeString
argument_list|(
name|out
argument_list|,
name|fileMetaInfo
operator|.
name|compressionType
argument_list|)
expr_stmt|;
name|WritableUtils
operator|.
name|writeVInt
argument_list|(
name|out
argument_list|,
name|fileMetaInfo
operator|.
name|bufferSize
argument_list|)
expr_stmt|;
name|WritableUtils
operator|.
name|writeVInt
argument_list|(
name|out
argument_list|,
name|fileMetaInfo
operator|.
name|metadataSize
argument_list|)
expr_stmt|;
comment|// serialize FileMetaInfo field footer
name|ByteBuffer
name|footerBuff
init|=
name|fileMetaInfo
operator|.
name|footerBuffer
decl_stmt|;
name|footerBuff
operator|.
name|reset
argument_list|()
expr_stmt|;
comment|// write length of buffer
name|WritableUtils
operator|.
name|writeVInt
argument_list|(
name|out
argument_list|,
name|footerBuff
operator|.
name|limit
argument_list|()
operator|-
name|footerBuff
operator|.
name|position
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|footerBuff
operator|.
name|array
argument_list|()
argument_list|,
name|footerBuff
operator|.
name|position
argument_list|()
argument_list|,
name|footerBuff
operator|.
name|limit
argument_list|()
operator|-
name|footerBuff
operator|.
name|position
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
comment|//deserialize path, offset, length using FileSplit
name|super
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|hasFooter
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
if|if
condition|(
name|hasFooter
condition|)
block|{
comment|// deserialize FileMetaInfo fields
name|String
name|compressionType
init|=
name|Text
operator|.
name|readString
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|int
name|bufferSize
init|=
name|WritableUtils
operator|.
name|readVInt
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|int
name|metadataSize
init|=
name|WritableUtils
operator|.
name|readVInt
argument_list|(
name|in
argument_list|)
decl_stmt|;
comment|// deserialize FileMetaInfo field footer
name|int
name|footerBuffSize
init|=
name|WritableUtils
operator|.
name|readVInt
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|ByteBuffer
name|footerBuff
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|footerBuffSize
argument_list|)
decl_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|footerBuff
operator|.
name|array
argument_list|()
argument_list|,
literal|0
argument_list|,
name|footerBuffSize
argument_list|)
expr_stmt|;
name|fileMetaInfo
operator|=
operator|new
name|FileMetaInfo
argument_list|(
name|compressionType
argument_list|,
name|bufferSize
argument_list|,
name|metadataSize
argument_list|,
name|footerBuff
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|FileMetaInfo
name|getFileMetaInfo
parameter_list|()
block|{
return|return
name|fileMetaInfo
return|;
block|}
specifier|public
name|boolean
name|hasFooter
parameter_list|()
block|{
return|return
name|hasFooter
return|;
block|}
block|}
end_class

end_unit

