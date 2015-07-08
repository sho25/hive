begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_comment
comment|/**  * Define the configuration properties that Orc understands.  */
end_comment

begin_enum
specifier|public
enum|enum
name|OrcConf
block|{
name|STRIPE_SIZE
argument_list|(
literal|"hive.exec.orc.default.stripe.size"
argument_list|,
literal|64L
operator|*
literal|1024
operator|*
literal|1024
argument_list|,
literal|"Define the default ORC stripe size, in bytes."
argument_list|)
block|,
name|BLOCK_SIZE
argument_list|(
literal|"hive.exec.orc.default.block.size"
argument_list|,
literal|256L
operator|*
literal|1024
operator|*
literal|1024
argument_list|,
literal|"Define the default file system block size for ORC files."
argument_list|)
block|,
name|ROW_INDEX_STRIDE
argument_list|(
literal|"hive.exec.orc.default.row.index.stride"
argument_list|,
literal|10000
argument_list|,
literal|"Define the default ORC index stride in number of rows. (Stride is the\n"
operator|+
literal|" number of rows n index entry represents.)"
argument_list|)
block|,
name|BUFFER_SIZE
argument_list|(
literal|"hive.exec.orc.default.buffer.size"
argument_list|,
literal|256
operator|*
literal|1024
argument_list|,
literal|"Define the default ORC buffer size, in bytes."
argument_list|)
block|,
name|BLOCK_PADDING
argument_list|(
literal|"hive.exec.orc.default.block.padding"
argument_list|,
literal|true
argument_list|,
literal|"Define the default block padding, which pads stripes to the HDFS\n"
operator|+
literal|" block boundaries."
argument_list|)
block|,
name|COMPRESS
argument_list|(
literal|"hive.exec.orc.default.compress"
argument_list|,
literal|"ZLIB"
argument_list|,
literal|"Define the default compression codec for ORC file"
argument_list|)
block|,
name|WRITE_FORMAT
argument_list|(
literal|"hive.exec.orc.write.format"
argument_list|,
literal|null
argument_list|,
literal|"Define the version of the file to write. Possible values are 0.11 and\n"
operator|+
literal|" 0.12. If this parameter is not defined, ORC will use the run\n"
operator|+
literal|" length encoding (RLE) introduced in Hive 0.12. Any value other\n"
operator|+
literal|" than 0.11 results in the 0.12 encoding."
argument_list|)
block|,
name|ENCODING_STRATEGY
argument_list|(
literal|"hive.exec.orc.encoding.strategy"
argument_list|,
literal|"SPEED"
argument_list|,
literal|"Define the encoding strategy to use while writing data. Changing this\n"
operator|+
literal|"will only affect the light weight encoding for integers. This\n"
operator|+
literal|"flag will not change the compression level of higher level\n"
operator|+
literal|"compression codec (like ZLIB)."
argument_list|)
block|,
name|COMPRESSION_STRATEGY
argument_list|(
literal|"hive.exec.orc.compression.strategy"
argument_list|,
literal|"SPEED"
argument_list|,
literal|"Define the compression strategy to use while writing data.\n"
operator|+
literal|"This changes the compression level of higher level compression\n"
operator|+
literal|"codec (like ZLIB)."
argument_list|)
block|,
name|BLOCK_PADDING_TOLERANCE
argument_list|(
literal|"hive.exec.orc.block.padding.tolerance"
argument_list|,
literal|0.05
argument_list|,
literal|"Define the tolerance for block padding as a decimal fraction of\n"
operator|+
literal|"stripe size (for example, the default value 0.05 is 5% of the\n"
operator|+
literal|"stripe size). For the defaults of 64Mb ORC stripe and 256Mb HDFS\n"
operator|+
literal|"blocks, the default block padding tolerance of 5% will\n"
operator|+
literal|"reserve a maximum of 3.2Mb for padding within the 256Mb block.\n"
operator|+
literal|"In that case, if the available size within the block is more than\n"
operator|+
literal|"3.2Mb, a new smaller stripe will be inserted to fit within that\n"
operator|+
literal|"space. This will make sure that no stripe written will block\n"
operator|+
literal|" boundaries and cause remote reads within a node local task."
argument_list|)
block|,
name|BLOOM_FILTER_FPP
argument_list|(
literal|"orc.default.bloom.fpp"
argument_list|,
literal|0.05
argument_list|,
literal|"Define the default false positive probability for bloom filters."
argument_list|)
block|,
name|USE_ZEROCOPY
argument_list|(
literal|"hive.exec.orc.zerocopy"
argument_list|,
literal|false
argument_list|,
literal|"Use zerocopy reads with ORC. (This requires Hadoop 2.3 or later.)"
argument_list|)
block|,
name|SKIP_CORRUPT_DATA
argument_list|(
literal|"hive.exec.orc.skip.corrupt.data"
argument_list|,
literal|false
argument_list|,
literal|"If ORC reader encounters corrupt data, this value will be used to\n"
operator|+
literal|"determine whether to skip the corrupt data or throw exception.\n"
operator|+
literal|"The default behavior is to throw exception."
argument_list|)
block|,
name|MEMORY_POOL
argument_list|(
literal|"hive.exec.orc.memory.pool"
argument_list|,
literal|0.5
argument_list|,
literal|"Maximum fraction of heap that can be used by ORC file writers"
argument_list|)
block|,
name|DICTIONARY_KEY_SIZE_THRESHOLD
argument_list|(
literal|"hive.exec.orc.dictionary.key.size.threshold"
argument_list|,
literal|0.8
argument_list|,
literal|"If the number of keys in a dictionary is greater than this fraction\n"
operator|+
literal|"of the total number of non-null rows, turn off dictionary\n"
operator|+
literal|"encoding.  Use 1 to always use dictionary encoding."
argument_list|)
block|,
name|ROW_INDEX_STRIDE_DICTIONARY_CHECK
argument_list|(
literal|"hive.orc.row.index.stride.dictionary.check"
argument_list|,
literal|true
argument_list|,
literal|"If enabled dictionary check will happen after first row index stride\n"
operator|+
literal|"(default 10000 rows) else dictionary check will happen before\n"
operator|+
literal|"writing first stripe. In both cases, the decision to use\n"
operator|+
literal|"dictionary or not will be retained thereafter."
argument_list|)
block|,   ;
specifier|private
specifier|final
name|String
name|attribute
decl_stmt|;
specifier|private
specifier|final
name|Object
name|defaultValue
decl_stmt|;
specifier|private
specifier|final
name|String
name|description
decl_stmt|;
name|OrcConf
parameter_list|(
name|String
name|attribute
parameter_list|,
name|Object
name|defaultValue
parameter_list|,
name|String
name|description
parameter_list|)
block|{
name|this
operator|.
name|attribute
operator|=
name|attribute
expr_stmt|;
name|this
operator|.
name|defaultValue
operator|=
name|defaultValue
expr_stmt|;
name|this
operator|.
name|description
operator|=
name|description
expr_stmt|;
block|}
specifier|public
name|String
name|getAttribute
parameter_list|()
block|{
return|return
name|attribute
return|;
block|}
specifier|public
name|Object
name|getDefaultValue
parameter_list|()
block|{
return|return
name|defaultValue
return|;
block|}
specifier|public
name|String
name|getDescription
parameter_list|()
block|{
return|return
name|description
return|;
block|}
specifier|public
name|long
name|getLong
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getLong
argument_list|(
name|attribute
argument_list|,
operator|(
operator|(
name|Number
operator|)
name|defaultValue
operator|)
operator|.
name|longValue
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|String
name|getString
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|get
argument_list|(
name|attribute
argument_list|,
operator|(
name|String
operator|)
name|defaultValue
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|getBoolean
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
block|{
return|return
operator|(
name|Boolean
operator|)
name|defaultValue
return|;
block|}
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
name|attribute
argument_list|,
operator|(
name|Boolean
operator|)
name|defaultValue
argument_list|)
return|;
block|}
specifier|public
name|double
name|getDouble
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|str
init|=
name|conf
operator|.
name|get
argument_list|(
name|attribute
argument_list|)
decl_stmt|;
if|if
condition|(
name|str
operator|==
literal|null
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|defaultValue
operator|)
operator|.
name|doubleValue
argument_list|()
return|;
block|}
return|return
name|Double
operator|.
name|parseDouble
argument_list|(
name|str
argument_list|)
return|;
block|}
block|}
end_enum

end_unit

