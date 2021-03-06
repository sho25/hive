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
name|security
operator|.
name|authorization
operator|.
name|plugin
package|;
end_package

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
name|java
operator|.
name|util
operator|.
name|Locale
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
name|classification
operator|.
name|InterfaceAudience
operator|.
name|LimitedPrivate
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
name|classification
operator|.
name|InterfaceStability
operator|.
name|Evolving
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
name|security
operator|.
name|authorization
operator|.
name|PrivilegeScope
import|;
end_import

begin_comment
comment|/**  * Represents the hive privilege being granted/revoked  */
end_comment

begin_class
annotation|@
name|LimitedPrivate
argument_list|(
name|value
operator|=
block|{
literal|"Apache Argus (incubating)"
block|}
argument_list|)
annotation|@
name|Evolving
specifier|public
class|class
name|HivePrivilege
implements|implements
name|Comparable
argument_list|<
name|HivePrivilege
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"Privilege [name="
operator|+
name|name
operator|+
literal|", columns="
operator|+
name|columns
operator|+
literal|"]"
return|;
block|}
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|columns
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|supportedScope
decl_stmt|;
specifier|public
name|HivePrivilege
parameter_list|(
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columns
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|columns
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HivePrivilege
parameter_list|(
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columns
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|supportedScope
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|US
argument_list|)
expr_stmt|;
name|this
operator|.
name|columns
operator|=
name|columns
expr_stmt|;
name|this
operator|.
name|supportedScope
operator|=
name|supportedScope
expr_stmt|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getColumns
parameter_list|()
block|{
return|return
name|columns
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getSupportedScope
parameter_list|()
block|{
return|return
name|supportedScope
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
specifier|final
name|int
name|prime
init|=
literal|31
decl_stmt|;
name|int
name|result
init|=
literal|1
decl_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
operator|(
operator|(
name|columns
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|columns
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
operator|(
operator|(
name|name
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|name
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|HivePrivilege
name|other
init|=
operator|(
name|HivePrivilege
operator|)
name|obj
decl_stmt|;
if|if
condition|(
name|columns
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|other
operator|.
name|columns
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
block|}
elseif|else
if|if
condition|(
operator|!
name|columns
operator|.
name|equals
argument_list|(
name|other
operator|.
name|columns
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|name
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|other
operator|.
name|name
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
block|}
elseif|else
if|if
condition|(
operator|!
name|name
operator|.
name|equals
argument_list|(
name|other
operator|.
name|name
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
specifier|public
name|boolean
name|supportsScope
parameter_list|(
name|PrivilegeScope
name|scope
parameter_list|)
block|{
return|return
name|supportedScope
operator|!=
literal|null
operator|&&
name|supportedScope
operator|.
name|contains
argument_list|(
name|scope
operator|.
name|name
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|HivePrivilege
name|privilege
parameter_list|)
block|{
name|int
name|compare
init|=
name|columns
operator|!=
literal|null
condition|?
operator|(
name|privilege
operator|.
name|columns
operator|!=
literal|null
condition|?
name|compare
argument_list|(
name|columns
argument_list|,
name|privilege
operator|.
name|columns
argument_list|)
else|:
literal|1
operator|)
else|:
operator|(
name|privilege
operator|.
name|columns
operator|!=
literal|null
condition|?
operator|-
literal|1
else|:
literal|0
operator|)
decl_stmt|;
if|if
condition|(
name|compare
operator|==
literal|0
condition|)
block|{
name|compare
operator|=
name|name
operator|.
name|compareTo
argument_list|(
name|privilege
operator|.
name|name
argument_list|)
expr_stmt|;
block|}
return|return
name|compare
return|;
block|}
specifier|private
name|int
name|compare
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|o1
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|o2
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|Math
operator|.
name|min
argument_list|(
name|o1
operator|.
name|size
argument_list|()
argument_list|,
name|o2
operator|.
name|size
argument_list|()
argument_list|)
condition|;
name|i
operator|++
control|)
block|{
name|int
name|compare
init|=
name|o1
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|compare
operator|!=
literal|0
condition|)
block|{
return|return
name|compare
return|;
block|}
block|}
return|return
name|o1
operator|.
name|size
argument_list|()
operator|>
name|o2
operator|.
name|size
argument_list|()
condition|?
literal|1
else|:
operator|(
name|o1
operator|.
name|size
argument_list|()
operator|<
name|o2
operator|.
name|size
argument_list|()
condition|?
operator|-
literal|1
else|:
literal|0
operator|)
return|;
block|}
block|}
end_class

end_unit

