begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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
name|objectinspector
package|;
end_package

begin_comment
comment|/**  * The UnionObject.  *  * It has tag followed by the object it is holding.  *  */
end_comment

begin_interface
specifier|public
interface|interface
name|UnionObject
block|{
comment|/**    * Get the tag of the union.    *    * @return the tag byte    */
name|byte
name|getTag
parameter_list|()
function_decl|;
comment|/**    * Get the Object.    *    * @return The Object union is holding    */
name|Object
name|getObject
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

