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
name|serde
operator|.
name|simple_meta
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
name|hive
operator|.
name|serde
operator|.
name|*
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
name|serde
operator|.
name|thrift
operator|.
name|*
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
name|*
import|;
end_import

begin_comment
comment|/**  * The default implementation of Hive Field based on type info from the metastore  */
end_comment

begin_class
specifier|public
class|class
name|MetadataTypedSerDeField
implements|implements
name|SerDeField
block|{
specifier|protected
name|String
name|_fieldName
decl_stmt|;
specifier|protected
name|int
name|_position
decl_stmt|;
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"[fieldName="
operator|+
name|_fieldName
operator|+
literal|",position="
operator|+
name|_position
operator|+
literal|"]"
return|;
block|}
specifier|public
name|MetadataTypedSerDeField
parameter_list|()
throws|throws
name|SerDeException
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"MetaDataTypedSerDeField::empty constructor not implemented!"
argument_list|)
throw|;
block|}
specifier|public
name|MetadataTypedSerDeField
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Class
name|myClass
parameter_list|,
name|int
name|position
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// for now just support String so don't bother with the Class
name|_fieldName
operator|=
name|fieldName
expr_stmt|;
name|_position
operator|=
name|position
expr_stmt|;
block|}
specifier|public
name|Object
name|get
parameter_list|(
name|Object
name|obj
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|/**        if we had the runtime thrift, we'd call it to get this field's value:         ret = runtime_thrift.get(this.serde.ddl, _fieldName, obj);        // this assumes we have the fully qualified fieldName here (which we should)        // and that runtime_thrift can understand that.         // It would suck to have to parse the obj everytime so we should have it        // somehow pre-parsed and cached somewhere as an opaque object we pass into the        // runtime thrift.         pw 2/5/08      */
name|ColumnSet
name|temp
init|=
operator|(
name|ColumnSet
operator|)
name|obj
decl_stmt|;
if|if
condition|(
name|temp
operator|.
name|col
operator|.
name|size
argument_list|()
operator|<=
name|_position
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"get "
operator|+
name|temp
operator|.
name|col
operator|.
name|size
argument_list|()
operator|+
literal|"<="
operator|+
name|_position
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
try|try
block|{
specifier|final
name|String
name|ret
init|=
name|temp
operator|.
name|col
operator|.
name|get
argument_list|(
name|_position
argument_list|)
decl_stmt|;
return|return
name|ret
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"ERR: MetaDataTypedSerDeField::get cannot access field - "
operator|+
literal|"fieldName="
operator|+
name|_fieldName
operator|+
literal|",obj="
operator|+
name|obj
operator|+
literal|",length="
operator|+
name|temp
operator|.
name|col
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Illegal object or access error: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|public
name|boolean
name|isList
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|isMap
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|isPrimitive
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
specifier|public
name|Class
name|getType
parameter_list|()
block|{
comment|// total hack. Since fieldName is a String, this does the right thing :) pw 2/5/08
return|return
name|this
operator|.
name|_fieldName
operator|.
name|getClass
argument_list|()
return|;
block|}
specifier|public
name|Class
name|getListElementType
parameter_list|()
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not a list field "
argument_list|)
throw|;
block|}
specifier|public
name|Class
name|getMapKeyType
parameter_list|()
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not a map field "
argument_list|)
throw|;
block|}
specifier|public
name|Class
name|getMapValueType
parameter_list|()
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not a map field "
argument_list|)
throw|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|_fieldName
return|;
block|}
specifier|public
specifier|static
name|String
name|fieldToString
parameter_list|(
name|SerDeField
name|hf
parameter_list|)
block|{
return|return
operator|(
literal|"Field= "
operator|+
name|hf
operator|.
name|getName
argument_list|()
operator|+
literal|":String"
operator|)
return|;
block|}
block|}
end_class

end_unit

