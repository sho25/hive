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
comment|/**  * SettableUnionObjectInspector.  *  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|SettableUnionObjectInspector
implements|implements
name|UnionObjectInspector
block|{
comment|/* Create an empty object */
specifier|public
specifier|abstract
name|Object
name|create
parameter_list|()
function_decl|;
comment|/* Add fields to the object */
specifier|public
specifier|abstract
name|Object
name|addField
parameter_list|(
name|Object
name|union
parameter_list|,
name|ObjectInspector
name|oi
parameter_list|)
function_decl|;
block|}
end_class

end_unit

