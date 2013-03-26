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
name|hcatalog
operator|.
name|utils
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileReader
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
name|io
operator|.
name|ObjectInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ObjectOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|imageio
operator|.
name|stream
operator|.
name|FileImageInputStream
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|transfer
operator|.
name|DataTransferFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|transfer
operator|.
name|HCatWriter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|transfer
operator|.
name|WriteEntity
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|transfer
operator|.
name|WriterContext
import|;
end_import

begin_class
specifier|public
class|class
name|DataWriterMaster
block|{
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|FileNotFoundException
throws|,
name|IOException
throws|,
name|ClassNotFoundException
block|{
comment|// This config contains all the configuration that master node wants to provide
comment|// to the HCatalog.
name|Properties
name|externalConfigs
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|externalConfigs
operator|.
name|load
argument_list|(
operator|new
name|FileReader
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|config
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|kv
range|:
name|externalConfigs
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"k: "
operator|+
name|kv
operator|.
name|getKey
argument_list|()
operator|+
literal|"\t v: "
operator|+
name|kv
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|config
operator|.
name|put
argument_list|(
operator|(
name|String
operator|)
name|kv
operator|.
name|getKey
argument_list|()
argument_list|,
operator|(
name|String
operator|)
name|kv
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|args
operator|.
name|length
operator|==
literal|3
operator|&&
literal|"commit"
operator|.
name|equalsIgnoreCase
argument_list|(
name|args
index|[
literal|2
index|]
argument_list|)
condition|)
block|{
comment|// Then, master commits if everything goes well.
name|ObjectInputStream
name|ois
init|=
operator|new
name|ObjectInputStream
argument_list|(
operator|new
name|FileInputStream
argument_list|(
operator|new
name|File
argument_list|(
name|args
index|[
literal|1
index|]
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|WriterContext
name|cntxt
init|=
operator|(
name|WriterContext
operator|)
name|ois
operator|.
name|readObject
argument_list|()
decl_stmt|;
name|commit
argument_list|(
name|config
argument_list|,
literal|true
argument_list|,
name|cntxt
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
comment|// This piece of code runs in master node and gets necessary context.
name|WriterContext
name|cntxt
init|=
name|runsInMaster
argument_list|(
name|config
argument_list|)
decl_stmt|;
comment|// Master node will serialize writercontext and will make it available at slaves.
name|File
name|f
init|=
operator|new
name|File
argument_list|(
name|args
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
name|f
operator|.
name|delete
argument_list|()
expr_stmt|;
name|ObjectOutputStream
name|oos
init|=
operator|new
name|ObjectOutputStream
argument_list|(
operator|new
name|FileOutputStream
argument_list|(
name|f
argument_list|)
argument_list|)
decl_stmt|;
name|oos
operator|.
name|writeObject
argument_list|(
name|cntxt
argument_list|)
expr_stmt|;
name|oos
operator|.
name|flush
argument_list|()
expr_stmt|;
name|oos
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|WriterContext
name|runsInMaster
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|config
parameter_list|)
throws|throws
name|HCatException
block|{
name|WriteEntity
operator|.
name|Builder
name|builder
init|=
operator|new
name|WriteEntity
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|WriteEntity
name|entity
init|=
name|builder
operator|.
name|withTable
argument_list|(
name|config
operator|.
name|get
argument_list|(
literal|"table"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HCatWriter
name|writer
init|=
name|DataTransferFactory
operator|.
name|getHCatWriter
argument_list|(
name|entity
argument_list|,
name|config
argument_list|)
decl_stmt|;
name|WriterContext
name|info
init|=
name|writer
operator|.
name|prepareWrite
argument_list|()
decl_stmt|;
return|return
name|info
return|;
block|}
specifier|private
specifier|static
name|void
name|commit
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|config
parameter_list|,
name|boolean
name|status
parameter_list|,
name|WriterContext
name|cntxt
parameter_list|)
throws|throws
name|HCatException
block|{
name|WriteEntity
operator|.
name|Builder
name|builder
init|=
operator|new
name|WriteEntity
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|WriteEntity
name|entity
init|=
name|builder
operator|.
name|withTable
argument_list|(
name|config
operator|.
name|get
argument_list|(
literal|"table"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HCatWriter
name|writer
init|=
name|DataTransferFactory
operator|.
name|getHCatWriter
argument_list|(
name|entity
argument_list|,
name|config
argument_list|)
decl_stmt|;
if|if
condition|(
name|status
condition|)
block|{
name|writer
operator|.
name|commit
argument_list|(
name|cntxt
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|writer
operator|.
name|abort
argument_list|(
name|cntxt
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

