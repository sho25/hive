begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  *  you may not use this file except in compliance with the License.  *  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  */
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
name|llap
operator|.
name|daemon
operator|.
name|impl
package|;
end_package

begin_comment
comment|/**  * An identifier for a query, which is unique.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|QueryIdentifier
block|{
specifier|private
specifier|final
name|String
name|appIdentifier
decl_stmt|;
specifier|private
specifier|final
name|int
name|dagIdentifier
decl_stmt|;
specifier|public
name|QueryIdentifier
parameter_list|(
name|String
name|appIdentifier
parameter_list|,
name|int
name|dagIdentifier
parameter_list|)
block|{
name|this
operator|.
name|appIdentifier
operator|=
name|appIdentifier
expr_stmt|;
name|this
operator|.
name|dagIdentifier
operator|=
name|dagIdentifier
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
operator|||
operator|!
name|getClass
argument_list|()
operator|.
name|isAssignableFrom
argument_list|(
name|o
operator|.
name|getClass
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|QueryIdentifier
name|that
init|=
operator|(
name|QueryIdentifier
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|dagIdentifier
operator|!=
name|that
operator|.
name|dagIdentifier
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|appIdentifier
operator|.
name|equals
argument_list|(
name|that
operator|.
name|appIdentifier
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|appIdentifier
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|dagIdentifier
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"QueryIdentifier{"
operator|+
literal|"appIdentifier='"
operator|+
name|appIdentifier
operator|+
literal|'\''
operator|+
literal|", dagIdentifier="
operator|+
name|dagIdentifier
operator|+
literal|'}'
return|;
block|}
block|}
end_class

end_unit

