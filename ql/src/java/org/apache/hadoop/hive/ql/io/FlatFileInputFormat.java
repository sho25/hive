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
name|io
operator|.
name|EOFException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
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
name|fs
operator|.
name|FSDataInputStream
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
name|mapred
operator|.
name|FileInputFormat
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
name|JobConf
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
name|InputSplit
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
name|Reporter
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
name|RecordReader
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
name|conf
operator|.
name|Configurable
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
name|serializer
operator|.
name|Serialization
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
name|serializer
operator|.
name|Serializer
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
name|serializer
operator|.
name|SerializationFactory
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
name|serializer
operator|.
name|Deserializer
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
name|compress
operator|.
name|CompressionCodecFactory
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
name|compress
operator|.
name|CompressionCodec
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/** An {@link org.apache.hadoop.mapred.InputFormat} for Plain files with {@link Deserializer} records */
end_comment

begin_class
specifier|public
class|class
name|FlatFileInputFormat
parameter_list|<
name|T
parameter_list|>
extends|extends
name|FileInputFormat
argument_list|<
name|Void
argument_list|,
name|FlatFileInputFormat
operator|.
name|RowContainer
argument_list|<
name|T
argument_list|>
argument_list|>
block|{
comment|/**    * A work-around until HADOOP-1230 is fixed.     *    * Allows boolean next(k,v) to be called by reference but still allow the deserializer to create a new    * object (i.e., row) on every call to next.    */
specifier|static
specifier|public
class|class
name|RowContainer
parameter_list|<
name|T
parameter_list|>
block|{
name|T
name|row
decl_stmt|;
block|}
comment|/**    * An implementation of SerializationContext is responsible for looking up the Serialization implementation    * for the given RecordReader. Potentially based on the Configuration or some other mechanism    *    * The SerializationFactory does not give this functionality since:    *  1. Requires Serialization implementations to be specified in the Configuration a-priori (although same as setting    *     a SerializationContext)    *  2. Does not lookup the actual subclass being deserialized. e.g., for Serializable does not have a way of  configuring    *      the actual Java class being serialized/deserialized.    */
specifier|static
specifier|public
interface|interface
name|SerializationContext
parameter_list|<
name|S
parameter_list|>
extends|extends
name|Configurable
block|{
comment|/**      *  An {@link Serialization} object for objects of type S      * @return a serialization object for this context      */
specifier|public
name|Serialization
argument_list|<
name|S
argument_list|>
name|getSerialization
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**      *  Produces the specific class to deserialize      */
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|S
argument_list|>
name|getRealClass
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    * The JobConf keys for the Serialization implementation    */
specifier|static
specifier|public
specifier|final
name|String
name|SerializationImplKey
init|=
literal|"mapred.input.serialization.implKey"
decl_stmt|;
comment|/**    *  An implementation of {@link SerializationContext} that reads the Serialization class and     *  specific subclass to be deserialized from the JobConf.    *    */
specifier|static
specifier|public
class|class
name|SerializationContextFromConf
parameter_list|<
name|S
parameter_list|>
implements|implements
name|FlatFileInputFormat
operator|.
name|SerializationContext
argument_list|<
name|S
argument_list|>
block|{
comment|/**      * The JobConf keys for the Class that is being deserialized.      */
specifier|static
specifier|public
specifier|final
name|String
name|SerializationSubclassKey
init|=
literal|"mapred.input.serialization.subclassKey"
decl_stmt|;
comment|/**      * Implements configurable so it can use the configuration to find the right classes      * Note: ReflectionUtils will automatigically call setConf with the right configuration.      */
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
comment|/**      * @return the actual class being deserialized      * @exception does not currently throw IOException      */
specifier|public
name|Class
argument_list|<
name|S
argument_list|>
name|getRealClass
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|(
name|Class
argument_list|<
name|S
argument_list|>
operator|)
name|conf
operator|.
name|getClass
argument_list|(
name|SerializationSubclassKey
argument_list|,
literal|null
argument_list|,
name|Object
operator|.
name|class
argument_list|)
return|;
block|}
comment|/**      * Looks up and instantiates the Serialization Object      *      * Important to note here that we are not relying on the Hadoop SerializationFactory part of the       * Serialization framework. This is because in the case of Non-Writable Objects, we cannot make any      * assumptions about the uniformity of the serialization class APIs - i.e., there may not be a "write"      * method call and a subclass may need to implement its own Serialization classes.       * The SerializationFactory currently returns the first (de)serializer that is compatible      * with the class to be deserialized;  in this context, that assumption isn't necessarily true.      *      * @return the serialization object for this context      * @exception does not currently throw any IOException      */
specifier|public
name|Serialization
argument_list|<
name|S
argument_list|>
name|getSerialization
parameter_list|()
throws|throws
name|IOException
block|{
name|Class
argument_list|<
name|Serialization
argument_list|<
name|S
argument_list|>
argument_list|>
name|tClass
init|=
operator|(
name|Class
argument_list|<
name|Serialization
argument_list|<
name|S
argument_list|>
argument_list|>
operator|)
name|conf
operator|.
name|getClass
argument_list|(
name|SerializationImplKey
argument_list|,
literal|null
argument_list|,
name|Serialization
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
name|tClass
operator|==
literal|null
condition|?
literal|null
else|:
operator|(
name|Serialization
argument_list|<
name|S
argument_list|>
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|tClass
argument_list|,
name|conf
argument_list|)
return|;
block|}
block|}
comment|/**     * An {@link RecordReader} for plain files with {@link Deserializer} records     *    * Reads one row at a time of type R.    * R is intended to be a base class of something such as: Record, Writable, Text, ...    *    */
specifier|public
class|class
name|FlatFileRecordReader
parameter_list|<
name|R
parameter_list|>
implements|implements
name|RecordReader
argument_list|<
name|Void
argument_list|,
name|FlatFileInputFormat
operator|.
name|RowContainer
argument_list|<
name|R
argument_list|>
argument_list|>
block|{
comment|/**      *  An interface for a helper class for instantiating {@link Serialization} classes.      */
comment|/**      * The stream in use - is fsin if not compressed, otherwise, it is dcin.      */
specifier|private
specifier|final
name|DataInputStream
name|in
decl_stmt|;
comment|/**      * The decompressed stream or null if the input is not decompressed.      */
specifier|private
specifier|final
name|InputStream
name|dcin
decl_stmt|;
comment|/**      * The underlying stream.      */
specifier|private
specifier|final
name|FSDataInputStream
name|fsin
decl_stmt|;
comment|/**      * For calculating progress      */
specifier|private
specifier|final
name|long
name|end
decl_stmt|;
comment|/**      * The constructed deserializer      */
specifier|private
specifier|final
name|Deserializer
argument_list|<
name|R
argument_list|>
name|deserializer
decl_stmt|;
comment|/**      * Once EOF is reached, stop calling the deserializer       */
specifier|private
name|boolean
name|isEOF
decl_stmt|;
comment|/**      * The JobConf which contains information needed to instantiate the correct Deserializer      */
specifier|private
name|Configuration
name|conf
decl_stmt|;
comment|/**      * The actual class of the row's we are deserializing, not just the base class      */
specifier|private
name|Class
argument_list|<
name|R
argument_list|>
name|realRowClass
decl_stmt|;
comment|/**      * FlatFileRecordReader constructor constructs the underlying stream (potentially decompressed) and       * creates the deserializer.      *      * @param conf the jobconf      * @param split the split for this file      */
specifier|public
name|FlatFileRecordReader
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSplit
name|split
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Path
name|path
init|=
name|split
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|FileSystem
name|fileSys
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|CompressionCodecFactory
name|compressionCodecs
init|=
operator|new
name|CompressionCodecFactory
argument_list|(
name|conf
argument_list|)
decl_stmt|;
specifier|final
name|CompressionCodec
name|codec
init|=
name|compressionCodecs
operator|.
name|getCodec
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|fsin
operator|=
name|fileSys
operator|.
name|open
argument_list|(
name|path
argument_list|)
expr_stmt|;
if|if
condition|(
name|codec
operator|!=
literal|null
condition|)
block|{
name|dcin
operator|=
name|codec
operator|.
name|createInputStream
argument_list|(
name|fsin
argument_list|)
expr_stmt|;
name|in
operator|=
operator|new
name|DataInputStream
argument_list|(
name|dcin
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|dcin
operator|=
literal|null
expr_stmt|;
name|in
operator|=
name|fsin
expr_stmt|;
block|}
name|isEOF
operator|=
literal|false
expr_stmt|;
name|end
operator|=
name|split
operator|.
name|getLength
argument_list|()
expr_stmt|;
comment|// Instantiate a SerializationContext which this will use to lookup the Serialization class and the
comment|// actual class being deserialized
name|SerializationContext
argument_list|<
name|R
argument_list|>
name|sinfo
decl_stmt|;
name|Class
argument_list|<
name|SerializationContext
argument_list|<
name|R
argument_list|>
argument_list|>
name|sinfoClass
init|=
operator|(
name|Class
argument_list|<
name|SerializationContext
argument_list|<
name|R
argument_list|>
argument_list|>
operator|)
name|conf
operator|.
name|getClass
argument_list|(
name|SerializationContextImplKey
argument_list|,
name|SerializationContextFromConf
operator|.
name|class
argument_list|)
decl_stmt|;
name|sinfo
operator|=
operator|(
name|SerializationContext
argument_list|<
name|R
argument_list|>
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|sinfoClass
argument_list|,
name|conf
argument_list|)
expr_stmt|;
comment|// Get the Serialization object and the class being deserialized
name|Serialization
argument_list|<
name|R
argument_list|>
name|serialization
init|=
name|sinfo
operator|.
name|getSerialization
argument_list|()
decl_stmt|;
name|realRowClass
operator|=
operator|(
name|Class
argument_list|<
name|R
argument_list|>
operator|)
name|sinfo
operator|.
name|getRealClass
argument_list|()
expr_stmt|;
name|deserializer
operator|=
operator|(
name|Deserializer
argument_list|<
name|R
argument_list|>
operator|)
name|serialization
operator|.
name|getDeserializer
argument_list|(
operator|(
name|Class
argument_list|<
name|R
argument_list|>
operator|)
name|realRowClass
argument_list|)
expr_stmt|;
name|deserializer
operator|.
name|open
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
comment|/**      * The actual class of the data being deserialized      */
specifier|private
name|Class
argument_list|<
name|R
argument_list|>
name|realRowclass
decl_stmt|;
comment|/**      * The JobConf key of the SerializationContext to use      */
specifier|static
specifier|public
specifier|final
name|String
name|SerializationContextImplKey
init|=
literal|"mapred.input.serialization.context_impl"
decl_stmt|;
comment|/**      * @return null      */
specifier|public
name|Void
name|createKey
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
comment|/**      * @return a new R instance.      */
specifier|public
name|RowContainer
argument_list|<
name|R
argument_list|>
name|createValue
parameter_list|()
block|{
name|RowContainer
argument_list|<
name|R
argument_list|>
name|r
init|=
operator|new
name|RowContainer
argument_list|<
name|R
argument_list|>
argument_list|()
decl_stmt|;
name|r
operator|.
name|row
operator|=
operator|(
name|R
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|realRowClass
argument_list|,
name|conf
argument_list|)
expr_stmt|;
return|return
name|r
return|;
block|}
comment|/**      * Returns the next row # and value      *      * @param key - void as these files have a value only      * @param value - the row container which is always re-used, but the internal value may be set to a new Object      * @return whether the key and value were read. True if they were and false if EOF      * @exception IOException from the deserializer      */
specifier|public
specifier|synchronized
name|boolean
name|next
parameter_list|(
name|Void
name|key
parameter_list|,
name|RowContainer
argument_list|<
name|R
argument_list|>
name|value
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|isEOF
operator|||
name|in
operator|.
name|available
argument_list|()
operator|==
literal|0
condition|)
block|{
name|isEOF
operator|=
literal|true
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// the deserializer is responsible for actually reading each record from the stream
try|try
block|{
name|value
operator|.
name|row
operator|=
name|deserializer
operator|.
name|deserialize
argument_list|(
name|value
operator|.
name|row
argument_list|)
expr_stmt|;
if|if
condition|(
name|value
operator|.
name|row
operator|==
literal|null
condition|)
block|{
name|isEOF
operator|=
literal|true
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|EOFException
name|e
parameter_list|)
block|{
name|isEOF
operator|=
literal|true
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
specifier|public
specifier|synchronized
name|float
name|getProgress
parameter_list|()
throws|throws
name|IOException
block|{
comment|// this assumes no splitting
if|if
condition|(
name|end
operator|==
literal|0
condition|)
block|{
return|return
literal|0.0f
return|;
block|}
else|else
block|{
comment|// gives progress over uncompressed stream
comment|// assumes deserializer is not buffering itself
return|return
name|Math
operator|.
name|min
argument_list|(
literal|1.0f
argument_list|,
name|fsin
operator|.
name|getPos
argument_list|()
operator|/
call|(
name|float
call|)
argument_list|(
name|end
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|synchronized
name|long
name|getPos
parameter_list|()
throws|throws
name|IOException
block|{
comment|// assumes deserializer is not buffering itself
comment|// position over uncompressed stream. not sure what
comment|// effect this has on stats about job
return|return
name|fsin
operator|.
name|getPos
argument_list|()
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
comment|// assuming that this closes the underlying streams
name|deserializer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
name|boolean
name|isSplittable
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|filename
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|RecordReader
argument_list|<
name|Void
argument_list|,
name|RowContainer
argument_list|<
name|T
argument_list|>
argument_list|>
name|getRecordReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|reporter
operator|.
name|setStatus
argument_list|(
name|split
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|FlatFileRecordReader
argument_list|<
name|T
argument_list|>
argument_list|(
name|job
argument_list|,
operator|(
name|FileSplit
operator|)
name|split
argument_list|)
return|;
block|}
block|}
end_class

end_unit

