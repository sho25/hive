begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|orc
package|;
end_package

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
name|java
operator|.
name|util
operator|.
name|List
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
name|conf
operator|.
name|Configuration
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
name|FileSystem
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
name|metastore
operator|.
name|FileFormatProxy
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
name|metastore
operator|.
name|Metastore
operator|.
name|SplitInfo
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
name|metastore
operator|.
name|Metastore
operator|.
name|SplitInfos
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
name|sarg
operator|.
name|SearchArgument
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
name|OrcConf
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
name|OrcProto
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
name|StripeInformation
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
name|OrcTail
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/** File format proxy for ORC. */
end_comment

begin_class
specifier|public
class|class
name|OrcFileFormatProxy
implements|implements
name|FileFormatProxy
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|OrcFileFormatProxy
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|SplitInfos
name|applySargToMetadata
parameter_list|(
name|SearchArgument
name|sarg
parameter_list|,
name|ByteBuffer
name|fileMetadata
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO: ideally we should store shortened representation of only the necessary fields
comment|//       in HBase; it will probably require custom SARG application code.
name|OrcTail
name|orcTail
init|=
name|ReaderImpl
operator|.
name|extractFileTail
argument_list|(
name|fileMetadata
argument_list|)
decl_stmt|;
name|OrcProto
operator|.
name|Footer
name|footer
init|=
name|orcTail
operator|.
name|getFooter
argument_list|()
decl_stmt|;
name|int
name|stripeCount
init|=
name|footer
operator|.
name|getStripesCount
argument_list|()
decl_stmt|;
name|boolean
name|writerUsedProlepticGregorian
init|=
name|footer
operator|.
name|hasCalendar
argument_list|()
condition|?
name|footer
operator|.
name|getCalendar
argument_list|()
operator|==
name|OrcProto
operator|.
name|CalendarKind
operator|.
name|PROLEPTIC_GREGORIAN
else|:
name|OrcConf
operator|.
name|PROLEPTIC_GREGORIAN_DEFAULT
operator|.
name|getBoolean
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|boolean
index|[]
name|result
init|=
name|OrcInputFormat
operator|.
name|pickStripesViaTranslatedSarg
argument_list|(
name|sarg
argument_list|,
name|orcTail
operator|.
name|getWriterVersion
argument_list|()
argument_list|,
name|footer
operator|.
name|getTypesList
argument_list|()
argument_list|,
name|orcTail
operator|.
name|getStripeStatistics
argument_list|(
name|writerUsedProlepticGregorian
argument_list|,
literal|true
argument_list|)
argument_list|,
name|stripeCount
argument_list|)
decl_stmt|;
comment|// For ORC case, send the boundaries of the stripes so we don't have to send the footer.
name|SplitInfos
operator|.
name|Builder
name|sb
init|=
name|SplitInfos
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|StripeInformation
argument_list|>
name|stripes
init|=
name|orcTail
operator|.
name|getStripes
argument_list|()
decl_stmt|;
name|boolean
name|isEliminated
init|=
literal|true
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|result
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|result
operator|!=
literal|null
operator|&&
operator|!
name|result
index|[
name|i
index|]
condition|)
continue|continue;
name|isEliminated
operator|=
literal|false
expr_stmt|;
name|StripeInformation
name|si
init|=
name|stripes
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"PPD is adding a split "
operator|+
name|i
operator|+
literal|": "
operator|+
name|si
operator|.
name|getOffset
argument_list|()
operator|+
literal|", "
operator|+
name|si
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|addInfos
argument_list|(
name|SplitInfo
operator|.
name|newBuilder
argument_list|()
operator|.
name|setIndex
argument_list|(
name|i
argument_list|)
operator|.
name|setOffset
argument_list|(
name|si
operator|.
name|getOffset
argument_list|()
argument_list|)
operator|.
name|setLength
argument_list|(
name|si
operator|.
name|getLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|isEliminated
condition|?
literal|null
else|:
name|sb
operator|.
name|build
argument_list|()
return|;
block|}
specifier|public
name|ByteBuffer
index|[]
name|getAddedColumnsToCache
parameter_list|()
block|{
return|return
literal|null
return|;
comment|// Nothing so far.
block|}
specifier|public
name|ByteBuffer
index|[]
index|[]
name|getAddedValuesToCache
parameter_list|(
name|List
argument_list|<
name|ByteBuffer
argument_list|>
name|metadata
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
comment|// Nothing so far (and shouldn't be called).
block|}
specifier|public
name|ByteBuffer
name|getMetadataToCache
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|ByteBuffer
index|[]
name|addedVals
parameter_list|)
throws|throws
name|IOException
block|{
comment|// For now, there's nothing special to return in addedVals. Just return the footer.
return|return
name|OrcFile
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|path
argument_list|)
operator|.
name|getSerializedFileFooter
argument_list|()
return|;
block|}
block|}
end_class

end_unit

