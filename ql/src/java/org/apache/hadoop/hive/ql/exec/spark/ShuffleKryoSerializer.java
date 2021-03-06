begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|exec
operator|.
name|spark
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|spark
operator|.
name|client
operator|.
name|SparkClientUtilities
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|SparkConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaSparkContext
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
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|MalformedURLException
import|;
end_import

begin_class
specifier|final
class|class
name|ShuffleKryoSerializer
block|{
specifier|private
specifier|static
specifier|final
name|String
name|HIVE_SHUFFLE_KRYO_SERIALIZER
init|=
literal|"org.apache.hive.spark.NoHashCodeKryoSerializer"
decl_stmt|;
specifier|private
specifier|static
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|serializer
operator|.
name|KryoSerializer
name|INSTANCE
decl_stmt|;
specifier|private
name|ShuffleKryoSerializer
parameter_list|()
block|{
comment|// Don't create me
block|}
specifier|static
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|serializer
operator|.
name|KryoSerializer
name|getInstance
parameter_list|(
name|JavaSparkContext
name|sc
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|INSTANCE
operator|==
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|ShuffleKryoSerializer
operator|.
name|class
init|)
block|{
if|if
condition|(
name|INSTANCE
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|INSTANCE
operator|=
operator|(
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|serializer
operator|.
name|KryoSerializer
operator|)
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getContextClassLoader
argument_list|()
operator|.
name|loadClass
argument_list|(
name|HIVE_SHUFFLE_KRYO_SERIALIZER
argument_list|)
operator|.
name|getConstructor
argument_list|(
name|SparkConf
operator|.
name|class
argument_list|)
operator|.
name|newInstance
argument_list|(
name|sc
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|INSTANCE
return|;
block|}
catch|catch
parameter_list|(
name|InstantiationException
decl||
name|IllegalAccessException
decl||
name|InvocationTargetException
decl||
name|NoSuchMethodException
decl||
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unable to create kryo serializer for shuffle RDDs using "
operator|+
literal|"class "
operator|+
name|HIVE_SHUFFLE_KRYO_SERIALIZER
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
else|else
block|{
return|return
name|INSTANCE
return|;
block|}
block|}
block|}
return|return
name|INSTANCE
return|;
block|}
block|}
end_class

end_unit

