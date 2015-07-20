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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|is
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertThat
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
name|RecordIdentifier
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestSequenceValidator
block|{
specifier|private
specifier|static
specifier|final
name|int
name|BUCKET_ID
init|=
literal|1
decl_stmt|;
specifier|private
name|SequenceValidator
name|validator
init|=
operator|new
name|SequenceValidator
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testSingleInSequence
parameter_list|()
block|{
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|0L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRowIdInSequence
parameter_list|()
block|{
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|0L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|0L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|0L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|4
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTxIdInSequence
parameter_list|()
block|{
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|0L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|1L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|4L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMixedInSequence
parameter_list|()
block|{
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|0L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|0L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|1L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|1L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNegativeTxId
parameter_list|()
block|{
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
operator|-
literal|1L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|0L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNegativeRowId
parameter_list|()
block|{
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|0L
argument_list|,
name|BUCKET_ID
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|0L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRowIdOutOfSequence
parameter_list|()
block|{
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|0L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|0L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|4
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|0L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReset
parameter_list|()
block|{
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|0L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|0L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|4
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
comment|// New partition for example
name|validator
operator|.
name|reset
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|0L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTxIdOutOfSequence
parameter_list|()
block|{
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|0L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|4L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|1L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMixedOutOfSequence
parameter_list|()
block|{
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|0L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|1L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|4
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|1L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|1L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|5
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|validator
operator|.
name|isInSequence
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
literal|0L
argument_list|,
name|BUCKET_ID
argument_list|,
literal|6
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|NullPointerException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testNullRecordIdentifier
parameter_list|()
block|{
name|validator
operator|.
name|isInSequence
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

