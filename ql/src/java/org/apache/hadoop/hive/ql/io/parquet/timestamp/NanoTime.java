begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

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
name|parquet
operator|.
name|timestamp
package|;
end_package

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
name|java
operator|.
name|nio
operator|.
name|ByteOrder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|Preconditions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|io
operator|.
name|api
operator|.
name|Binary
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|io
operator|.
name|api
operator|.
name|RecordConsumer
import|;
end_import

begin_comment
comment|/**  * Provides a wrapper representing a parquet-timestamp, with methods to  * convert to and from binary.  */
end_comment

begin_class
specifier|public
class|class
name|NanoTime
block|{
specifier|private
specifier|final
name|int
name|julianDay
decl_stmt|;
specifier|private
specifier|final
name|long
name|timeOfDayNanos
decl_stmt|;
specifier|public
specifier|static
name|NanoTime
name|fromBinary
parameter_list|(
name|Binary
name|bytes
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|bytes
operator|.
name|length
argument_list|()
operator|==
literal|12
argument_list|,
literal|"Must be 12 bytes"
argument_list|)
expr_stmt|;
name|ByteBuffer
name|buf
init|=
name|bytes
operator|.
name|toByteBuffer
argument_list|()
decl_stmt|;
name|buf
operator|.
name|order
argument_list|(
name|ByteOrder
operator|.
name|LITTLE_ENDIAN
argument_list|)
expr_stmt|;
name|long
name|timeOfDayNanos
init|=
name|buf
operator|.
name|getLong
argument_list|()
decl_stmt|;
name|int
name|julianDay
init|=
name|buf
operator|.
name|getInt
argument_list|()
decl_stmt|;
return|return
operator|new
name|NanoTime
argument_list|(
name|julianDay
argument_list|,
name|timeOfDayNanos
argument_list|)
return|;
block|}
specifier|public
name|NanoTime
parameter_list|(
name|int
name|julianDay
parameter_list|,
name|long
name|timeOfDayNanos
parameter_list|)
block|{
name|this
operator|.
name|julianDay
operator|=
name|julianDay
expr_stmt|;
name|this
operator|.
name|timeOfDayNanos
operator|=
name|timeOfDayNanos
expr_stmt|;
block|}
specifier|public
name|int
name|getJulianDay
parameter_list|()
block|{
return|return
name|julianDay
return|;
block|}
specifier|public
name|long
name|getTimeOfDayNanos
parameter_list|()
block|{
return|return
name|timeOfDayNanos
return|;
block|}
specifier|public
name|Binary
name|toBinary
parameter_list|()
block|{
name|ByteBuffer
name|buf
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|12
argument_list|)
decl_stmt|;
name|buf
operator|.
name|order
argument_list|(
name|ByteOrder
operator|.
name|LITTLE_ENDIAN
argument_list|)
expr_stmt|;
name|buf
operator|.
name|putLong
argument_list|(
name|timeOfDayNanos
argument_list|)
expr_stmt|;
name|buf
operator|.
name|putInt
argument_list|(
name|julianDay
argument_list|)
expr_stmt|;
name|buf
operator|.
name|flip
argument_list|()
expr_stmt|;
return|return
name|Binary
operator|.
name|fromByteBuffer
argument_list|(
name|buf
argument_list|)
return|;
block|}
specifier|public
name|void
name|writeValue
parameter_list|(
name|RecordConsumer
name|recordConsumer
parameter_list|)
block|{
name|recordConsumer
operator|.
name|addBinary
argument_list|(
name|toBinary
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"NanoTime{julianDay="
operator|+
name|julianDay
operator|+
literal|", timeOfDayNanos="
operator|+
name|timeOfDayNanos
operator|+
literal|"}"
return|;
block|}
block|}
end_class

end_unit

