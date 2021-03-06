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
name|hive
operator|.
name|hcatalog
operator|.
name|streaming
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
name|GnuParser
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
name|HelpFormatter
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
name|Parser
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
name|hive
operator|.
name|common
operator|.
name|LogUtils
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
name|StringUtils
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_comment
comment|/**  * A stand alone utility to write data into the streaming ingest interface.  */
end_comment

begin_class
specifier|public
class|class
name|StreamingIntegrationTester
block|{
specifier|static
specifier|final
specifier|private
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|StreamingIntegrationTester
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
try|try
block|{
name|LogUtils
operator|.
name|initHiveLog4j
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LogUtils
operator|.
name|LogInitializationException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Unable to initialize log4j "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|Options
name|options
init|=
operator|new
name|Options
argument_list|()
decl_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"abort-pct"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"Percentage of transactions to abort, defaults to 5"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"abortpct"
argument_list|)
operator|.
name|create
argument_list|(
literal|'a'
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArgs
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"column-names"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"column names of table to write to"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"columns"
argument_list|)
operator|.
name|withValueSeparator
argument_list|(
literal|','
argument_list|)
operator|.
name|isRequired
argument_list|()
operator|.
name|create
argument_list|(
literal|'c'
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"database"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"Database of table to write to"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"database"
argument_list|)
operator|.
name|isRequired
argument_list|()
operator|.
name|create
argument_list|(
literal|'d'
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"frequency"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"How often to commit a transaction, in seconds, defaults to 1"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"frequency"
argument_list|)
operator|.
name|create
argument_list|(
literal|'f'
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"iterations"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"Number of batches to write, defaults to 10"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"num-batches"
argument_list|)
operator|.
name|create
argument_list|(
literal|'i'
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"metastore-uri"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"URI of Hive metastore"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"metastore-uri"
argument_list|)
operator|.
name|isRequired
argument_list|()
operator|.
name|create
argument_list|(
literal|'m'
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"num_transactions"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"Number of transactions per batch, defaults to 100"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"num-txns"
argument_list|)
operator|.
name|create
argument_list|(
literal|'n'
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArgs
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"partition-values"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"partition values, must be provided in order of partition columns, "
operator|+
literal|"if not provided table is assumed to not be partitioned"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"partition"
argument_list|)
operator|.
name|withValueSeparator
argument_list|(
literal|','
argument_list|)
operator|.
name|create
argument_list|(
literal|'p'
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"records-per-transaction"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"records to write in each transaction, defaults to 100"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"records-per-txn"
argument_list|)
operator|.
name|withValueSeparator
argument_list|(
literal|','
argument_list|)
operator|.
name|create
argument_list|(
literal|'r'
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArgs
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"column-types"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"column types, valid values are string, int, float, decimal, date, "
operator|+
literal|"datetime"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"schema"
argument_list|)
operator|.
name|withValueSeparator
argument_list|(
literal|','
argument_list|)
operator|.
name|isRequired
argument_list|()
operator|.
name|create
argument_list|(
literal|'s'
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"table"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"Table to write to"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"table"
argument_list|)
operator|.
name|isRequired
argument_list|()
operator|.
name|create
argument_list|(
literal|'t'
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"num-writers"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"Number of writers to create, defaults to 2"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"writers"
argument_list|)
operator|.
name|create
argument_list|(
literal|'w'
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|(
literal|false
argument_list|)
operator|.
name|withArgName
argument_list|(
literal|"pause"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"Wait on keyboard input after commit& batch close. default: disabled"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"pause"
argument_list|)
operator|.
name|create
argument_list|(
literal|'x'
argument_list|)
argument_list|)
expr_stmt|;
name|Parser
name|parser
init|=
operator|new
name|GnuParser
argument_list|()
decl_stmt|;
name|CommandLine
name|cmdline
init|=
literal|null
decl_stmt|;
try|try
block|{
name|cmdline
operator|=
name|parser
operator|.
name|parse
argument_list|(
name|options
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|usage
argument_list|(
name|options
argument_list|)
expr_stmt|;
block|}
name|boolean
name|pause
init|=
name|cmdline
operator|.
name|hasOption
argument_list|(
literal|'x'
argument_list|)
decl_stmt|;
name|String
name|db
init|=
name|cmdline
operator|.
name|getOptionValue
argument_list|(
literal|'d'
argument_list|)
decl_stmt|;
name|String
name|table
init|=
name|cmdline
operator|.
name|getOptionValue
argument_list|(
literal|'t'
argument_list|)
decl_stmt|;
name|String
name|uri
init|=
name|cmdline
operator|.
name|getOptionValue
argument_list|(
literal|'m'
argument_list|)
decl_stmt|;
name|int
name|txnsPerBatch
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmdline
operator|.
name|getOptionValue
argument_list|(
literal|'n'
argument_list|,
literal|"100"
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|writers
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmdline
operator|.
name|getOptionValue
argument_list|(
literal|'w'
argument_list|,
literal|"2"
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|batches
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmdline
operator|.
name|getOptionValue
argument_list|(
literal|'i'
argument_list|,
literal|"10"
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|recordsPerTxn
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmdline
operator|.
name|getOptionValue
argument_list|(
literal|'r'
argument_list|,
literal|"100"
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|frequency
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmdline
operator|.
name|getOptionValue
argument_list|(
literal|'f'
argument_list|,
literal|"1"
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|ap
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmdline
operator|.
name|getOptionValue
argument_list|(
literal|'a'
argument_list|,
literal|"5"
argument_list|)
argument_list|)
decl_stmt|;
name|float
name|abortPct
init|=
operator|(
operator|(
name|float
operator|)
name|ap
operator|)
operator|/
literal|100.0f
decl_stmt|;
name|String
index|[]
name|partVals
init|=
name|cmdline
operator|.
name|getOptionValues
argument_list|(
literal|'p'
argument_list|)
decl_stmt|;
name|String
index|[]
name|cols
init|=
name|cmdline
operator|.
name|getOptionValues
argument_list|(
literal|'c'
argument_list|)
decl_stmt|;
name|String
index|[]
name|types
init|=
name|cmdline
operator|.
name|getOptionValues
argument_list|(
literal|'s'
argument_list|)
decl_stmt|;
name|StreamingIntegrationTester
name|sit
init|=
operator|new
name|StreamingIntegrationTester
argument_list|(
name|db
argument_list|,
name|table
argument_list|,
name|uri
argument_list|,
name|txnsPerBatch
argument_list|,
name|writers
argument_list|,
name|batches
argument_list|,
name|recordsPerTxn
argument_list|,
name|frequency
argument_list|,
name|abortPct
argument_list|,
name|partVals
argument_list|,
name|cols
argument_list|,
name|types
argument_list|,
name|pause
argument_list|)
decl_stmt|;
name|sit
operator|.
name|go
argument_list|()
expr_stmt|;
block|}
specifier|static
name|void
name|usage
parameter_list|(
name|Options
name|options
parameter_list|)
block|{
name|HelpFormatter
name|hf
init|=
operator|new
name|HelpFormatter
argument_list|()
decl_stmt|;
name|hf
operator|.
name|printHelp
argument_list|(
name|HelpFormatter
operator|.
name|DEFAULT_WIDTH
argument_list|,
literal|"sit [options]"
argument_list|,
literal|"Usage: "
argument_list|,
name|options
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|db
decl_stmt|;
specifier|private
name|String
name|table
decl_stmt|;
specifier|private
name|String
name|uri
decl_stmt|;
specifier|private
name|int
name|txnsPerBatch
decl_stmt|;
specifier|private
name|int
name|writers
decl_stmt|;
specifier|private
name|int
name|batches
decl_stmt|;
specifier|private
name|int
name|recordsPerTxn
decl_stmt|;
specifier|private
name|int
name|frequency
decl_stmt|;
specifier|private
name|float
name|abortPct
decl_stmt|;
specifier|private
name|String
index|[]
name|partVals
decl_stmt|;
specifier|private
name|String
index|[]
name|cols
decl_stmt|;
specifier|private
name|String
index|[]
name|types
decl_stmt|;
specifier|private
name|boolean
name|pause
decl_stmt|;
specifier|private
name|StreamingIntegrationTester
parameter_list|(
name|String
name|db
parameter_list|,
name|String
name|table
parameter_list|,
name|String
name|uri
parameter_list|,
name|int
name|txnsPerBatch
parameter_list|,
name|int
name|writers
parameter_list|,
name|int
name|batches
parameter_list|,
name|int
name|recordsPerTxn
parameter_list|,
name|int
name|frequency
parameter_list|,
name|float
name|abortPct
parameter_list|,
name|String
index|[]
name|partVals
parameter_list|,
name|String
index|[]
name|cols
parameter_list|,
name|String
index|[]
name|types
parameter_list|,
name|boolean
name|pause
parameter_list|)
block|{
name|this
operator|.
name|db
operator|=
name|db
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|uri
operator|=
name|uri
expr_stmt|;
name|this
operator|.
name|txnsPerBatch
operator|=
name|txnsPerBatch
expr_stmt|;
name|this
operator|.
name|writers
operator|=
name|writers
expr_stmt|;
name|this
operator|.
name|batches
operator|=
name|batches
expr_stmt|;
name|this
operator|.
name|recordsPerTxn
operator|=
name|recordsPerTxn
expr_stmt|;
name|this
operator|.
name|frequency
operator|=
name|frequency
expr_stmt|;
name|this
operator|.
name|abortPct
operator|=
name|abortPct
expr_stmt|;
name|this
operator|.
name|partVals
operator|=
name|partVals
expr_stmt|;
name|this
operator|.
name|cols
operator|=
name|cols
expr_stmt|;
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
name|this
operator|.
name|pause
operator|=
name|pause
expr_stmt|;
block|}
specifier|private
name|void
name|go
parameter_list|()
block|{
name|HiveEndPoint
name|endPoint
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
name|partVals
operator|==
literal|null
condition|)
block|{
name|endPoint
operator|=
operator|new
name|HiveEndPoint
argument_list|(
name|uri
argument_list|,
name|db
argument_list|,
name|table
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|endPoint
operator|=
operator|new
name|HiveEndPoint
argument_list|(
name|uri
argument_list|,
name|db
argument_list|,
name|table
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|partVals
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|writers
condition|;
name|i
operator|++
control|)
block|{
name|Writer
name|w
init|=
operator|new
name|Writer
argument_list|(
name|endPoint
argument_list|,
name|i
argument_list|,
name|txnsPerBatch
argument_list|,
name|batches
argument_list|,
name|recordsPerTxn
argument_list|,
name|frequency
argument_list|,
name|abortPct
argument_list|,
name|cols
argument_list|,
name|types
argument_list|,
name|pause
argument_list|)
decl_stmt|;
name|w
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Caught exception while testing: "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|Writer
extends|extends
name|Thread
block|{
specifier|private
name|HiveEndPoint
name|endPoint
decl_stmt|;
specifier|private
name|int
name|txnsPerBatch
decl_stmt|;
specifier|private
name|int
name|batches
decl_stmt|;
specifier|private
name|int
name|writerNumber
decl_stmt|;
specifier|private
name|int
name|recordsPerTxn
decl_stmt|;
specifier|private
name|int
name|frequency
decl_stmt|;
specifier|private
name|float
name|abortPct
decl_stmt|;
specifier|private
name|String
index|[]
name|cols
decl_stmt|;
specifier|private
name|String
index|[]
name|types
decl_stmt|;
specifier|private
name|boolean
name|pause
decl_stmt|;
specifier|private
name|Random
name|rand
decl_stmt|;
name|Writer
parameter_list|(
name|HiveEndPoint
name|endPoint
parameter_list|,
name|int
name|writerNumber
parameter_list|,
name|int
name|txnsPerBatch
parameter_list|,
name|int
name|batches
parameter_list|,
name|int
name|recordsPerTxn
parameter_list|,
name|int
name|frequency
parameter_list|,
name|float
name|abortPct
parameter_list|,
name|String
index|[]
name|cols
parameter_list|,
name|String
index|[]
name|types
parameter_list|,
name|boolean
name|pause
parameter_list|)
block|{
name|this
operator|.
name|endPoint
operator|=
name|endPoint
expr_stmt|;
name|this
operator|.
name|txnsPerBatch
operator|=
name|txnsPerBatch
expr_stmt|;
name|this
operator|.
name|batches
operator|=
name|batches
expr_stmt|;
name|this
operator|.
name|writerNumber
operator|=
name|writerNumber
expr_stmt|;
name|this
operator|.
name|recordsPerTxn
operator|=
name|recordsPerTxn
expr_stmt|;
name|this
operator|.
name|frequency
operator|=
name|frequency
operator|*
literal|1000
expr_stmt|;
name|this
operator|.
name|abortPct
operator|=
name|abortPct
expr_stmt|;
name|this
operator|.
name|cols
operator|=
name|cols
expr_stmt|;
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
name|this
operator|.
name|pause
operator|=
name|pause
expr_stmt|;
name|rand
operator|=
operator|new
name|Random
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|StreamingConnection
name|conn
init|=
literal|null
decl_stmt|;
try|try
block|{
name|conn
operator|=
name|endPoint
operator|.
name|newConnection
argument_list|(
literal|true
argument_list|,
literal|"UT_"
operator|+
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|RecordWriter
name|writer
init|=
operator|new
name|DelimitedInputWriter
argument_list|(
name|cols
argument_list|,
literal|","
argument_list|,
name|endPoint
argument_list|)
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
name|batches
condition|;
name|i
operator|++
control|)
block|{
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting batch "
operator|+
name|i
argument_list|)
expr_stmt|;
name|TransactionBatch
name|batch
init|=
name|conn
operator|.
name|fetchTransactionBatch
argument_list|(
name|txnsPerBatch
argument_list|,
name|writer
argument_list|)
decl_stmt|;
try|try
block|{
while|while
condition|(
name|batch
operator|.
name|remainingTransactions
argument_list|()
operator|>
literal|0
condition|)
block|{
name|batch
operator|.
name|beginNextTransaction
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|recordsPerTxn
condition|;
name|j
operator|++
control|)
block|{
name|batch
operator|.
name|write
argument_list|(
name|generateRecord
argument_list|(
name|cols
argument_list|,
name|types
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|rand
operator|.
name|nextFloat
argument_list|()
operator|<
name|abortPct
condition|)
name|batch
operator|.
name|abort
argument_list|()
expr_stmt|;
else|else
name|batch
operator|.
name|commit
argument_list|()
expr_stmt|;
if|if
condition|(
name|pause
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Writer "
operator|+
name|writerNumber
operator|+
literal|" committed... press Enter to continue. "
operator|+
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|in
operator|.
name|read
argument_list|()
expr_stmt|;
block|}
block|}
name|long
name|end
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
if|if
condition|(
name|end
operator|-
name|start
operator|<
name|frequency
condition|)
name|Thread
operator|.
name|sleep
argument_list|(
name|frequency
operator|-
operator|(
name|end
operator|-
name|start
operator|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|batch
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|pause
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Writer "
operator|+
name|writerNumber
operator|+
literal|" has closed a Batch.. press Enter to continue. "
operator|+
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|in
operator|.
name|read
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Writer number "
operator|+
name|writerNumber
operator|+
literal|" caught exception while testing: "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|conn
operator|!=
literal|null
condition|)
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|byte
index|[]
name|generateRecord
parameter_list|(
name|String
index|[]
name|cols
parameter_list|,
name|String
index|[]
name|types
parameter_list|)
block|{
comment|// TODO make it so I can randomize the column order
name|StringBuilder
name|buf
init|=
operator|new
name|StringBuilder
argument_list|()
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
name|types
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|buf
operator|.
name|append
argument_list|(
name|generateColumn
argument_list|(
name|types
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
return|return
name|buf
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|()
return|;
block|}
specifier|private
name|String
name|generateColumn
parameter_list|(
name|String
name|type
parameter_list|)
block|{
if|if
condition|(
literal|"string"
operator|.
name|equals
argument_list|(
name|type
operator|.
name|toLowerCase
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|"When that Aprilis with his showers swoot"
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|toLowerCase
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"int"
argument_list|)
condition|)
block|{
return|return
literal|"42"
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|toLowerCase
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"dec"
argument_list|)
operator|||
name|type
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
literal|"float"
argument_list|)
condition|)
block|{
return|return
literal|"3.141592654"
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
literal|"datetime"
argument_list|)
condition|)
block|{
return|return
literal|"2014-03-07 15:33:22"
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
literal|"date"
argument_list|)
condition|)
block|{
return|return
literal|"1955-11-12"
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Sorry, I don't know the type "
operator|+
name|type
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

