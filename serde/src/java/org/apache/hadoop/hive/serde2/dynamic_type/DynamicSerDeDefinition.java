begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/* Generated By:JJTree: Do not edit this line. DynamicSerDeDefinition.java */
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
name|dynamic_type
package|;
end_package

begin_class
specifier|public
class|class
name|DynamicSerDeDefinition
extends|extends
name|SimpleNode
block|{
specifier|public
name|DynamicSerDeDefinition
parameter_list|(
name|int
name|id
parameter_list|)
block|{
name|super
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
specifier|public
name|DynamicSerDeDefinition
parameter_list|(
name|thrift_grammar
name|p
parameter_list|,
name|int
name|id
parameter_list|)
block|{
name|super
argument_list|(
name|p
argument_list|,
name|id
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

