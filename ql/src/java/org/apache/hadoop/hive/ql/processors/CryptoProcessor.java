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
name|processors
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|Options
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|OptionBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLine
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|ParseException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLineParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|GnuParser
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
name|conf
operator|.
name|HiveConf
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
name|session
operator|.
name|SessionState
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
name|shims
operator|.
name|HadoopShims
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
name|util
operator|.
name|Arrays
import|;
end_import

begin_comment
comment|/**  * This class processes HADOOP commands used for HDFS encryption. It is meant to be run  * only by Hive unit& queries tests.  */
end_comment

begin_class
specifier|public
class|class
name|CryptoProcessor
implements|implements
name|CommandProcessor
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|CryptoProcessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|HadoopShims
operator|.
name|HdfsEncryptionShim
name|encryptionShim
decl_stmt|;
specifier|private
name|Options
name|CREATE_KEY_OPTIONS
decl_stmt|;
specifier|private
name|Options
name|DELETE_KEY_OPTIONS
decl_stmt|;
specifier|private
name|Options
name|CREATE_ZONE_OPTIONS
decl_stmt|;
specifier|private
name|int
name|DEFAULT_BIT_LENGTH
init|=
literal|128
decl_stmt|;
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|public
name|CryptoProcessor
parameter_list|(
name|HadoopShims
operator|.
name|HdfsEncryptionShim
name|encryptionShim
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
block|{
name|this
operator|.
name|encryptionShim
operator|=
name|encryptionShim
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|CREATE_KEY_OPTIONS
operator|=
operator|new
name|Options
argument_list|()
expr_stmt|;
name|CREATE_KEY_OPTIONS
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withLongOpt
argument_list|(
literal|"keyName"
argument_list|)
operator|.
name|isRequired
argument_list|()
operator|.
name|create
argument_list|()
argument_list|)
expr_stmt|;
name|CREATE_KEY_OPTIONS
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withLongOpt
argument_list|(
literal|"bitLength"
argument_list|)
operator|.
name|create
argument_list|()
argument_list|)
expr_stmt|;
comment|// optional
name|DELETE_KEY_OPTIONS
operator|=
operator|new
name|Options
argument_list|()
expr_stmt|;
name|DELETE_KEY_OPTIONS
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withLongOpt
argument_list|(
literal|"keyName"
argument_list|)
operator|.
name|isRequired
argument_list|()
operator|.
name|create
argument_list|()
argument_list|)
expr_stmt|;
name|CREATE_ZONE_OPTIONS
operator|=
operator|new
name|Options
argument_list|()
expr_stmt|;
name|CREATE_ZONE_OPTIONS
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withLongOpt
argument_list|(
literal|"keyName"
argument_list|)
operator|.
name|isRequired
argument_list|()
operator|.
name|create
argument_list|()
argument_list|)
expr_stmt|;
name|CREATE_ZONE_OPTIONS
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withLongOpt
argument_list|(
literal|"path"
argument_list|)
operator|.
name|isRequired
argument_list|()
operator|.
name|create
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|CommandLine
name|parseCommandArgs
parameter_list|(
specifier|final
name|Options
name|opts
parameter_list|,
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|ParseException
block|{
name|CommandLineParser
name|parser
init|=
operator|new
name|GnuParser
argument_list|()
decl_stmt|;
return|return
name|parser
operator|.
name|parse
argument_list|(
name|opts
argument_list|,
name|args
argument_list|)
return|;
block|}
specifier|private
name|CommandProcessorResponse
name|returnErrorResponse
parameter_list|(
specifier|final
name|String
name|errmsg
parameter_list|)
block|{
return|return
operator|new
name|CommandProcessorResponse
argument_list|(
literal|1
argument_list|,
literal|"Encryption Processor Helper Failed:"
operator|+
name|errmsg
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|private
name|void
name|writeTestOutput
parameter_list|(
specifier|final
name|String
name|msg
parameter_list|)
block|{
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|out
operator|.
name|println
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|CommandProcessorResponse
name|run
parameter_list|(
name|String
name|command
parameter_list|)
block|{
name|String
index|[]
name|args
init|=
name|command
operator|.
name|split
argument_list|(
literal|"\\s+"
argument_list|)
decl_stmt|;
if|if
condition|(
name|args
operator|.
name|length
operator|<
literal|1
condition|)
block|{
return|return
name|returnErrorResponse
argument_list|(
literal|"Command arguments are empty."
argument_list|)
return|;
block|}
if|if
condition|(
name|encryptionShim
operator|==
literal|null
condition|)
block|{
return|return
name|returnErrorResponse
argument_list|(
literal|"Hadoop encryption shim is not initialized."
argument_list|)
return|;
block|}
name|String
name|action
init|=
name|args
index|[
literal|0
index|]
decl_stmt|;
name|String
name|params
index|[]
init|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|args
argument_list|,
literal|1
argument_list|,
name|args
operator|.
name|length
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|action
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"create_key"
argument_list|)
condition|)
block|{
name|createEncryptionKey
argument_list|(
name|params
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|action
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"create_zone"
argument_list|)
condition|)
block|{
name|createEncryptionZone
argument_list|(
name|params
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|action
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"delete_key"
argument_list|)
condition|)
block|{
name|deleteEncryptionKey
argument_list|(
name|params
argument_list|)
expr_stmt|;
block|}
else|else
block|{
return|return
name|returnErrorResponse
argument_list|(
literal|"Unknown command action: "
operator|+
name|action
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
name|returnErrorResponse
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
return|;
block|}
return|return
operator|new
name|CommandProcessorResponse
argument_list|(
literal|0
argument_list|)
return|;
block|}
comment|/**    * Creates an encryption key using the parameters passed through the 'create_key' action.    *    * @param params Parameters passed to the 'create_key' command action.    * @throws Exception If key creation failed.    */
specifier|private
name|void
name|createEncryptionKey
parameter_list|(
name|String
index|[]
name|params
parameter_list|)
throws|throws
name|Exception
block|{
name|CommandLine
name|args
init|=
name|parseCommandArgs
argument_list|(
name|CREATE_KEY_OPTIONS
argument_list|,
name|params
argument_list|)
decl_stmt|;
name|String
name|keyName
init|=
name|args
operator|.
name|getOptionValue
argument_list|(
literal|"keyName"
argument_list|)
decl_stmt|;
name|String
name|bitLength
init|=
name|args
operator|.
name|getOptionValue
argument_list|(
literal|"bitLength"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|DEFAULT_BIT_LENGTH
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|encryptionShim
operator|.
name|createKey
argument_list|(
name|keyName
argument_list|,
operator|new
name|Integer
argument_list|(
name|bitLength
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Cannot create encryption key: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
name|writeTestOutput
argument_list|(
literal|"Encryption key created: '"
operator|+
name|keyName
operator|+
literal|"'"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Creates an encryption zone using the parameters passed through the 'create_zone' action.    *    * @param params Parameters passed to the 'create_zone' command action.    * @throws Exception If zone creation failed.    */
specifier|private
name|void
name|createEncryptionZone
parameter_list|(
name|String
index|[]
name|params
parameter_list|)
throws|throws
name|Exception
block|{
name|CommandLine
name|args
init|=
name|parseCommandArgs
argument_list|(
name|CREATE_ZONE_OPTIONS
argument_list|,
name|params
argument_list|)
decl_stmt|;
name|String
name|keyName
init|=
name|args
operator|.
name|getOptionValue
argument_list|(
literal|"keyName"
argument_list|)
decl_stmt|;
name|Path
name|cryptoZone
init|=
operator|new
name|Path
argument_list|(
name|args
operator|.
name|getOptionValue
argument_list|(
literal|"path"
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|cryptoZone
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Cannot create encryption zone: Invalid path '"
operator|+
name|args
operator|.
name|getOptionValue
argument_list|(
literal|"path"
argument_list|)
operator|+
literal|"'"
argument_list|)
throw|;
block|}
try|try
block|{
name|encryptionShim
operator|.
name|createEncryptionZone
argument_list|(
name|cryptoZone
argument_list|,
name|keyName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Cannot create encryption zone: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
name|writeTestOutput
argument_list|(
literal|"Encryption zone created: '"
operator|+
name|cryptoZone
operator|+
literal|"' using key: '"
operator|+
name|keyName
operator|+
literal|"'"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Deletes an encryption key using the parameters passed through the 'delete_key' action.    *    * @param params Parameters passed to the 'delete_key' command action.    * @throws Exception If key deletion failed.    */
specifier|private
name|void
name|deleteEncryptionKey
parameter_list|(
name|String
index|[]
name|params
parameter_list|)
throws|throws
name|Exception
block|{
name|CommandLine
name|args
init|=
name|parseCommandArgs
argument_list|(
name|DELETE_KEY_OPTIONS
argument_list|,
name|params
argument_list|)
decl_stmt|;
name|String
name|keyName
init|=
name|args
operator|.
name|getOptionValue
argument_list|(
literal|"keyName"
argument_list|)
decl_stmt|;
try|try
block|{
name|encryptionShim
operator|.
name|deleteKey
argument_list|(
name|keyName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Cannot delete encryption key: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
name|writeTestOutput
argument_list|(
literal|"Encryption key deleted: '"
operator|+
name|keyName
operator|+
literal|"'"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

