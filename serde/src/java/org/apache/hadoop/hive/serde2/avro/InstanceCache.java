begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|serde2
operator|.
name|avro
package|;
end_package

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
name|Set
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
comment|/**  * Cache for objects whose creation only depends on some other set of objects and therefore can be  * used against other equivalent versions of those objects. Essentially memoizes instance creation.  *  * @param<SeedObject> Object that determines the instance. The cache uses this object as a key for  *          its hash which is why it is imperative to have appropriate equals and hashcode  *          implementation for this object for the cache to work properly  * @param<Instance> Instance that will be created from SeedObject.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|InstanceCache
parameter_list|<
name|SeedObject
parameter_list|,
name|Instance
parameter_list|>
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
name|InstanceCache
operator|.
name|class
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|SeedObject
argument_list|,
name|Instance
argument_list|>
name|cache
init|=
operator|new
name|HashMap
argument_list|<
name|SeedObject
argument_list|,
name|Instance
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|InstanceCache
parameter_list|()
block|{}
comment|/**    * Retrieve (or create if it doesn't exist) the correct Instance for this    * SeedObject    */
specifier|public
name|Instance
name|retrieve
parameter_list|(
name|SeedObject
name|hv
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
return|return
name|retrieve
argument_list|(
name|hv
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Retrieve (or create if it doesn't exist) the correct Instance for this    * SeedObject using 'seenSchemas' to resolve circular references    */
specifier|public
name|Instance
name|retrieve
parameter_list|(
name|SeedObject
name|hv
parameter_list|,
name|Set
argument_list|<
name|SeedObject
argument_list|>
name|seenSchemas
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|debug
argument_list|(
literal|"Checking for hv: "
operator|+
name|hv
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|cache
operator|.
name|containsKey
argument_list|(
name|hv
argument_list|)
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|debug
argument_list|(
literal|"Returning cache result."
argument_list|)
expr_stmt|;
return|return
name|cache
operator|.
name|get
argument_list|(
name|hv
argument_list|)
return|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating new instance and storing in cache"
argument_list|)
expr_stmt|;
name|Instance
name|instance
init|=
name|makeInstance
argument_list|(
name|hv
argument_list|,
name|seenSchemas
argument_list|)
decl_stmt|;
name|cache
operator|.
name|put
argument_list|(
name|hv
argument_list|,
name|instance
argument_list|)
expr_stmt|;
return|return
name|instance
return|;
block|}
specifier|protected
specifier|abstract
name|Instance
name|makeInstance
parameter_list|(
name|SeedObject
name|hv
parameter_list|,
name|Set
argument_list|<
name|SeedObject
argument_list|>
name|seenSchemas
parameter_list|)
throws|throws
name|AvroSerdeException
function_decl|;
block|}
end_class

end_unit

