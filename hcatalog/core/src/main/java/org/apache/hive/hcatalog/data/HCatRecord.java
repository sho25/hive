begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|data
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
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
name|Map
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
name|type
operator|.
name|HiveChar
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
name|type
operator|.
name|HiveDecimal
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
name|type
operator|.
name|HiveVarchar
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
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatSchema
import|;
end_import

begin_comment
comment|/**  * Abstract class exposing get and set semantics for basic record usage.  * Note :  *   HCatRecord is designed only to be used as in-memory representation only.  *   Don't use it to store data on the physical device.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|abstract
class|class
name|HCatRecord
implements|implements
name|HCatRecordable
block|{
specifier|public
specifier|abstract
name|Object
name|get
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|)
throws|throws
name|HCatException
function_decl|;
specifier|public
specifier|abstract
name|void
name|set
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|,
name|Object
name|value
parameter_list|)
throws|throws
name|HCatException
function_decl|;
specifier|public
specifier|abstract
name|void
name|remove
parameter_list|(
name|int
name|idx
parameter_list|)
throws|throws
name|HCatException
function_decl|;
specifier|public
specifier|abstract
name|void
name|copy
parameter_list|(
name|HCatRecord
name|r
parameter_list|)
throws|throws
name|HCatException
function_decl|;
specifier|protected
name|Object
name|get
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|,
name|Class
name|clazz
parameter_list|)
throws|throws
name|HCatException
block|{
comment|// TODO : if needed, verify that recordschema entry for fieldname matches appropriate type.
return|return
name|get
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|)
return|;
block|}
specifier|public
name|Boolean
name|getBoolean
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|)
throws|throws
name|HCatException
block|{
return|return
operator|(
name|Boolean
operator|)
name|get
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|Boolean
operator|.
name|class
argument_list|)
return|;
block|}
specifier|public
name|void
name|setBoolean
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|,
name|Boolean
name|value
parameter_list|)
throws|throws
name|HCatException
block|{
name|set
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getByteArray
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|)
throws|throws
name|HCatException
block|{
return|return
operator|(
name|byte
index|[]
operator|)
name|get
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|byte
index|[]
operator|.
expr|class
argument_list|)
return|;
block|}
specifier|public
name|void
name|setByteArray
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|HCatException
block|{
name|set
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Byte
name|getByte
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|)
throws|throws
name|HCatException
block|{
comment|//TINYINT
return|return
operator|(
name|Byte
operator|)
name|get
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|Byte
operator|.
name|class
argument_list|)
return|;
block|}
specifier|public
name|void
name|setByte
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|,
name|Byte
name|value
parameter_list|)
throws|throws
name|HCatException
block|{
name|set
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Short
name|getShort
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|)
throws|throws
name|HCatException
block|{
comment|// SMALLINT
return|return
operator|(
name|Short
operator|)
name|get
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|Short
operator|.
name|class
argument_list|)
return|;
block|}
specifier|public
name|void
name|setShort
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|,
name|Short
name|value
parameter_list|)
throws|throws
name|HCatException
block|{
name|set
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Integer
name|getInteger
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|)
throws|throws
name|HCatException
block|{
return|return
operator|(
name|Integer
operator|)
name|get
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|Integer
operator|.
name|class
argument_list|)
return|;
block|}
specifier|public
name|void
name|setInteger
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|,
name|Integer
name|value
parameter_list|)
throws|throws
name|HCatException
block|{
name|set
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Long
name|getLong
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|)
throws|throws
name|HCatException
block|{
comment|// BIGINT
return|return
operator|(
name|Long
operator|)
name|get
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|Long
operator|.
name|class
argument_list|)
return|;
block|}
specifier|public
name|void
name|setLong
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|,
name|Long
name|value
parameter_list|)
throws|throws
name|HCatException
block|{
name|set
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Float
name|getFloat
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|)
throws|throws
name|HCatException
block|{
return|return
operator|(
name|Float
operator|)
name|get
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|Float
operator|.
name|class
argument_list|)
return|;
block|}
specifier|public
name|void
name|setFloat
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|,
name|Float
name|value
parameter_list|)
throws|throws
name|HCatException
block|{
name|set
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Double
name|getDouble
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|)
throws|throws
name|HCatException
block|{
return|return
operator|(
name|Double
operator|)
name|get
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|Double
operator|.
name|class
argument_list|)
return|;
block|}
specifier|public
name|void
name|setDouble
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|,
name|Double
name|value
parameter_list|)
throws|throws
name|HCatException
block|{
name|set
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getString
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|)
throws|throws
name|HCatException
block|{
return|return
operator|(
name|String
operator|)
name|get
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|String
operator|.
name|class
argument_list|)
return|;
block|}
specifier|public
name|void
name|setString
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|,
name|String
name|value
parameter_list|)
throws|throws
name|HCatException
block|{
name|set
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|List
argument_list|<
name|?
extends|extends
name|Object
argument_list|>
name|getStruct
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|)
throws|throws
name|HCatException
block|{
return|return
operator|(
name|List
argument_list|<
name|?
extends|extends
name|Object
argument_list|>
operator|)
name|get
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|List
operator|.
name|class
argument_list|)
return|;
block|}
specifier|public
name|void
name|setStruct
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|,
name|List
argument_list|<
name|?
extends|extends
name|Object
argument_list|>
name|value
parameter_list|)
throws|throws
name|HCatException
block|{
name|set
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|?
argument_list|>
name|getList
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|)
throws|throws
name|HCatException
block|{
return|return
operator|(
name|List
argument_list|<
name|?
argument_list|>
operator|)
name|get
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|List
operator|.
name|class
argument_list|)
return|;
block|}
specifier|public
name|void
name|setList
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|,
name|List
argument_list|<
name|?
argument_list|>
name|value
parameter_list|)
throws|throws
name|HCatException
block|{
name|set
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|getMap
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|)
throws|throws
name|HCatException
block|{
return|return
operator|(
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|get
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|Map
operator|.
name|class
argument_list|)
return|;
block|}
specifier|public
name|void
name|setMap
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|,
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|value
parameter_list|)
throws|throws
name|HCatException
block|{
name|set
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setChar
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|,
name|HiveChar
name|value
parameter_list|)
throws|throws
name|HCatException
block|{
name|set
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveChar
name|getChar
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|)
throws|throws
name|HCatException
block|{
return|return
operator|(
name|HiveChar
operator|)
name|get
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|HiveChar
operator|.
name|class
argument_list|)
return|;
block|}
specifier|public
name|void
name|setVarchar
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|,
name|HiveVarchar
name|value
parameter_list|)
throws|throws
name|HCatException
block|{
name|set
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveVarchar
name|getVarchar
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|)
throws|throws
name|HCatException
block|{
return|return
operator|(
name|HiveVarchar
operator|)
name|get
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|HiveVarchar
operator|.
name|class
argument_list|)
return|;
block|}
specifier|public
name|void
name|setDecimal
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|,
name|HiveDecimal
name|value
parameter_list|)
throws|throws
name|HCatException
block|{
name|set
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveDecimal
name|getDecimal
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|)
throws|throws
name|HCatException
block|{
return|return
operator|(
name|HiveDecimal
operator|)
name|get
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|HiveDecimal
operator|.
name|class
argument_list|)
return|;
block|}
comment|/**    * Note that the proper way to construct a java.sql.Date for use with this object is     * Date.valueOf("1999-12-31").      */
specifier|public
name|void
name|setDate
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|,
name|Date
name|value
parameter_list|)
throws|throws
name|HCatException
block|{
name|set
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Date
name|getDate
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|)
throws|throws
name|HCatException
block|{
return|return
operator|(
name|Date
operator|)
name|get
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|Date
operator|.
name|class
argument_list|)
return|;
block|}
specifier|public
name|void
name|setTimestamp
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|,
name|Timestamp
name|value
parameter_list|)
throws|throws
name|HCatException
block|{
name|set
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Timestamp
name|getTimestamp
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|)
throws|throws
name|HCatException
block|{
return|return
operator|(
name|Timestamp
operator|)
name|get
argument_list|(
name|fieldName
argument_list|,
name|recordSchema
argument_list|,
name|Timestamp
operator|.
name|class
argument_list|)
return|;
block|}
block|}
end_class

end_unit

