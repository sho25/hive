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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|Unstable
import|;
end_import

begin_comment
comment|/**  * Represents the object on which privilege is being granted/revoked  */
end_comment

begin_class
annotation|@
name|LimitedPrivate
argument_list|(
name|value
operator|=
block|{
literal|""
block|}
argument_list|)
annotation|@
name|Unstable
specifier|public
class|class
name|HivePrivilegeObject
implements|implements
name|Comparable
argument_list|<
name|HivePrivilegeObject
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|HivePrivilegeObject
name|o
parameter_list|)
block|{
name|int
name|compare
init|=
name|type
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|type
argument_list|)
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
name|dbname
operator|!=
literal|null
condition|?
operator|(
name|o
operator|.
name|dbname
operator|!=
literal|null
condition|?
name|dbname
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|dbname
argument_list|)
else|:
literal|1
operator|)
else|:
operator|(
name|o
operator|.
name|dbname
operator|!=
literal|null
condition|?
operator|-
literal|1
else|:
literal|0
operator|)
expr_stmt|;
block|}
if|if
condition|(
name|compare
operator|==
literal|0
condition|)
block|{
name|compare
operator|=
name|objectName
operator|!=
literal|null
condition|?
operator|(
name|o
operator|.
name|objectName
operator|!=
literal|null
condition|?
name|objectName
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|objectName
argument_list|)
else|:
literal|1
operator|)
else|:
operator|(
name|o
operator|.
name|objectName
operator|!=
literal|null
condition|?
operator|-
literal|1
else|:
literal|0
operator|)
expr_stmt|;
block|}
if|if
condition|(
name|compare
operator|==
literal|0
condition|)
block|{
name|compare
operator|=
name|partKeys
operator|!=
literal|null
condition|?
operator|(
name|o
operator|.
name|partKeys
operator|!=
literal|null
condition|?
name|compare
argument_list|(
name|partKeys
argument_list|,
name|o
operator|.
name|partKeys
argument_list|)
else|:
literal|1
operator|)
else|:
operator|(
name|o
operator|.
name|partKeys
operator|!=
literal|null
condition|?
operator|-
literal|1
else|:
literal|0
operator|)
expr_stmt|;
block|}
if|if
condition|(
name|compare
operator|==
literal|0
condition|)
block|{
name|compare
operator|=
name|columns
operator|!=
literal|null
condition|?
operator|(
name|o
operator|.
name|columns
operator|!=
literal|null
condition|?
name|compare
argument_list|(
name|columns
argument_list|,
name|o
operator|.
name|columns
argument_list|)
else|:
literal|1
operator|)
else|:
operator|(
name|o
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
name|Collection
argument_list|<
name|String
argument_list|>
name|o1
parameter_list|,
name|Collection
argument_list|<
name|String
argument_list|>
name|o2
parameter_list|)
block|{
name|Iterator
argument_list|<
name|String
argument_list|>
name|it1
init|=
name|o1
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|String
argument_list|>
name|it2
init|=
name|o2
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|it1
operator|.
name|hasNext
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|it2
operator|.
name|hasNext
argument_list|()
condition|)
block|{
break|break;
block|}
name|String
name|s1
init|=
name|it1
operator|.
name|next
argument_list|()
decl_stmt|;
name|String
name|s2
init|=
name|it2
operator|.
name|next
argument_list|()
decl_stmt|;
name|int
name|compare
init|=
name|s1
operator|!=
literal|null
condition|?
operator|(
name|s2
operator|!=
literal|null
condition|?
name|s1
operator|.
name|compareTo
argument_list|(
name|s2
argument_list|)
else|:
literal|1
operator|)
else|:
operator|(
name|s2
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
specifier|public
enum|enum
name|HivePrivilegeObjectType
block|{
name|GLOBAL
block|,
name|DATABASE
block|,
name|TABLE_OR_VIEW
block|,
name|PARTITION
block|,
name|COLUMN
block|,
name|LOCAL_URI
block|,
name|DFS_URI
block|,
name|COMMAND_PARAMS
block|,
name|FUNCTION
block|}
empty_stmt|;
specifier|public
enum|enum
name|HivePrivObjectActionType
block|{
name|OTHER
block|,
name|INSERT
block|,
name|INSERT_OVERWRITE
block|}
empty_stmt|;
specifier|private
specifier|final
name|HivePrivilegeObjectType
name|type
decl_stmt|;
specifier|private
specifier|final
name|String
name|dbname
decl_stmt|;
specifier|private
specifier|final
name|String
name|objectName
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|commandParams
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|partKeys
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|columns
decl_stmt|;
specifier|private
specifier|final
name|HivePrivObjectActionType
name|actionType
decl_stmt|;
specifier|public
name|HivePrivilegeObject
parameter_list|(
name|HivePrivilegeObjectType
name|type
parameter_list|,
name|String
name|dbname
parameter_list|,
name|String
name|objectName
parameter_list|)
block|{
name|this
argument_list|(
name|type
argument_list|,
name|dbname
argument_list|,
name|objectName
argument_list|,
name|HivePrivObjectActionType
operator|.
name|OTHER
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HivePrivilegeObject
parameter_list|(
name|HivePrivilegeObjectType
name|type
parameter_list|,
name|String
name|dbname
parameter_list|,
name|String
name|objectName
parameter_list|,
name|HivePrivObjectActionType
name|actionType
parameter_list|)
block|{
name|this
argument_list|(
name|type
argument_list|,
name|dbname
argument_list|,
name|objectName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|actionType
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HivePrivilegeObject
parameter_list|(
name|HivePrivilegeObjectType
name|type
parameter_list|,
name|String
name|dbname
parameter_list|,
name|String
name|objectName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partKeys
parameter_list|,
name|String
name|column
parameter_list|)
block|{
name|this
argument_list|(
name|type
argument_list|,
name|dbname
argument_list|,
name|objectName
argument_list|,
name|partKeys
argument_list|,
name|column
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|column
argument_list|)
argument_list|)
argument_list|,
name|HivePrivObjectActionType
operator|.
name|OTHER
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create HivePrivilegeObject of type {@link HivePrivilegeObjectType.COMMAND_PARAMS}    * @param cmdParams    * @return    */
specifier|public
specifier|static
name|HivePrivilegeObject
name|createHivePrivilegeObject
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|cmdParams
parameter_list|)
block|{
return|return
operator|new
name|HivePrivilegeObject
argument_list|(
name|HivePrivilegeObjectType
operator|.
name|COMMAND_PARAMS
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|cmdParams
argument_list|)
return|;
block|}
specifier|public
name|HivePrivilegeObject
parameter_list|(
name|HivePrivilegeObjectType
name|type
parameter_list|,
name|String
name|dbname
parameter_list|,
name|String
name|objectName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partKeys
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|columns
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|commandParams
parameter_list|)
block|{
name|this
argument_list|(
name|type
argument_list|,
name|dbname
argument_list|,
name|objectName
argument_list|,
name|partKeys
argument_list|,
name|columns
argument_list|,
name|HivePrivObjectActionType
operator|.
name|OTHER
argument_list|,
name|commandParams
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HivePrivilegeObject
parameter_list|(
name|HivePrivilegeObjectType
name|type
parameter_list|,
name|String
name|dbname
parameter_list|,
name|String
name|objectName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partKeys
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|columns
parameter_list|,
name|HivePrivObjectActionType
name|actionType
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|commandParams
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|dbname
operator|=
name|dbname
expr_stmt|;
name|this
operator|.
name|objectName
operator|=
name|objectName
expr_stmt|;
name|this
operator|.
name|partKeys
operator|=
name|partKeys
expr_stmt|;
name|this
operator|.
name|columns
operator|=
name|columns
expr_stmt|;
name|this
operator|.
name|actionType
operator|=
name|actionType
expr_stmt|;
name|this
operator|.
name|commandParams
operator|=
name|commandParams
expr_stmt|;
block|}
specifier|public
name|HivePrivilegeObjectType
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
specifier|public
name|String
name|getDbname
parameter_list|()
block|{
return|return
name|dbname
return|;
block|}
comment|/**    * @return name of table/view/uri/function name    */
specifier|public
name|String
name|getObjectName
parameter_list|()
block|{
return|return
name|objectName
return|;
block|}
specifier|public
name|HivePrivObjectActionType
name|getActionType
parameter_list|()
block|{
return|return
name|actionType
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getCommandParams
parameter_list|()
block|{
return|return
name|commandParams
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getPartKeys
parameter_list|()
block|{
return|return
name|partKeys
return|;
block|}
comment|/**    * Applicable columns in this object    * In case of DML read operations, this is the set of columns being used.    * Column information is not set for DDL operations and for tables being written into    * @return list of applicable columns    */
specifier|public
name|Set
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
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|name
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|DATABASE
case|:
name|name
operator|=
name|dbname
expr_stmt|;
break|break;
case|case
name|TABLE_OR_VIEW
case|:
case|case
name|PARTITION
case|:
name|name
operator|=
name|getDbObjectName
argument_list|(
name|dbname
argument_list|,
name|objectName
argument_list|)
expr_stmt|;
if|if
condition|(
name|partKeys
operator|!=
literal|null
condition|)
block|{
name|name
operator|+=
name|partKeys
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
break|break;
case|case
name|FUNCTION
case|:
name|name
operator|=
name|getDbObjectName
argument_list|(
name|dbname
argument_list|,
name|objectName
argument_list|)
expr_stmt|;
break|break;
case|case
name|COLUMN
case|:
case|case
name|LOCAL_URI
case|:
case|case
name|DFS_URI
case|:
name|name
operator|=
name|objectName
expr_stmt|;
break|break;
case|case
name|COMMAND_PARAMS
case|:
name|name
operator|=
name|commandParams
operator|.
name|toString
argument_list|()
expr_stmt|;
break|break;
block|}
comment|// get the string representing action type if its non default action type
name|String
name|actionTypeStr
init|=
literal|""
decl_stmt|;
if|if
condition|(
name|actionType
operator|!=
literal|null
condition|)
block|{
switch|switch
condition|(
name|actionType
condition|)
block|{
case|case
name|INSERT
case|:
case|case
name|INSERT_OVERWRITE
case|:
name|actionTypeStr
operator|=
literal|", action="
operator|+
name|actionType
expr_stmt|;
default|default:
block|}
block|}
return|return
literal|"Object [type="
operator|+
name|type
operator|+
literal|", name="
operator|+
name|name
operator|+
name|actionTypeStr
operator|+
literal|"]"
return|;
block|}
specifier|private
name|String
name|getDbObjectName
parameter_list|(
name|String
name|dbname2
parameter_list|,
name|String
name|objectName2
parameter_list|)
block|{
return|return
operator|(
name|dbname
operator|==
literal|null
condition|?
literal|""
else|:
name|dbname
operator|+
literal|"."
operator|)
operator|+
name|objectName
return|;
block|}
specifier|public
name|void
name|setColumns
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|columnms
parameter_list|)
block|{
name|this
operator|.
name|columns
operator|=
name|columnms
expr_stmt|;
block|}
block|}
end_class

end_unit

