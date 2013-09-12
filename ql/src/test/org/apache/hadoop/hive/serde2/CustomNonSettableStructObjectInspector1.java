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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorUtils
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
name|serde2
operator|.
name|objectinspector
operator|.
name|StructField
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
name|serde2
operator|.
name|objectinspector
operator|.
name|StructObjectInspector
import|;
end_import

begin_class
specifier|public
class|class
name|CustomNonSettableStructObjectInspector1
extends|extends
name|StructObjectInspector
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|CustomNonSettableStructObjectInspector1
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
class|class
name|MyField
implements|implements
name|StructField
block|{
specifier|protected
name|int
name|fieldID
decl_stmt|;
specifier|protected
name|String
name|fieldName
decl_stmt|;
specifier|protected
name|ObjectInspector
name|fieldObjectInspector
decl_stmt|;
specifier|protected
name|String
name|fieldComment
decl_stmt|;
specifier|public
name|MyField
parameter_list|(
name|int
name|fieldID
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|ObjectInspector
name|fieldObjectInspector
parameter_list|)
block|{
name|this
operator|.
name|fieldID
operator|=
name|fieldID
expr_stmt|;
name|this
operator|.
name|fieldName
operator|=
name|fieldName
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
name|this
operator|.
name|fieldObjectInspector
operator|=
name|fieldObjectInspector
expr_stmt|;
block|}
specifier|public
name|MyField
parameter_list|(
name|int
name|fieldID
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|ObjectInspector
name|fieldObjectInspector
parameter_list|,
name|String
name|fieldComment
parameter_list|)
block|{
name|this
argument_list|(
name|fieldID
argument_list|,
name|fieldName
argument_list|,
name|fieldObjectInspector
argument_list|)
expr_stmt|;
name|this
operator|.
name|fieldComment
operator|=
name|fieldComment
expr_stmt|;
block|}
specifier|public
name|int
name|getFieldID
parameter_list|()
block|{
return|return
name|fieldID
return|;
block|}
specifier|public
name|String
name|getFieldName
parameter_list|()
block|{
return|return
name|fieldName
return|;
block|}
specifier|public
name|ObjectInspector
name|getFieldObjectInspector
parameter_list|()
block|{
return|return
name|fieldObjectInspector
return|;
block|}
specifier|public
name|String
name|getFieldComment
parameter_list|()
block|{
return|return
name|fieldComment
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
literal|""
operator|+
name|fieldID
operator|+
literal|":"
operator|+
name|fieldName
return|;
block|}
block|}
specifier|protected
name|List
argument_list|<
name|MyField
argument_list|>
name|fields
decl_stmt|;
specifier|public
name|String
name|getTypeName
parameter_list|()
block|{
return|return
name|ObjectInspectorUtils
operator|.
name|getStandardStructTypeName
argument_list|(
name|this
argument_list|)
return|;
block|}
comment|/**    * Call ObjectInspectorFactory.getNonSettableStructObjectInspector instead.    */
specifier|protected
name|CustomNonSettableStructObjectInspector1
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|structFieldNames
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|structFieldObjectInspectors
parameter_list|)
block|{
name|init
argument_list|(
name|structFieldNames
argument_list|,
name|structFieldObjectInspectors
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|init
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|structFieldNames
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|structFieldObjectInspectors
parameter_list|)
block|{
assert|assert
operator|(
name|structFieldNames
operator|.
name|size
argument_list|()
operator|==
name|structFieldObjectInspectors
operator|.
name|size
argument_list|()
operator|)
assert|;
name|fields
operator|=
operator|new
name|ArrayList
argument_list|<
name|MyField
argument_list|>
argument_list|(
name|structFieldNames
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|structFieldNames
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|fields
operator|.
name|add
argument_list|(
operator|new
name|MyField
argument_list|(
name|i
argument_list|,
name|structFieldNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|structFieldObjectInspectors
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|final
name|Category
name|getCategory
parameter_list|()
block|{
return|return
name|Category
operator|.
name|STRUCT
return|;
block|}
comment|// Without Data
annotation|@
name|Override
specifier|public
name|StructField
name|getStructFieldRef
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
return|return
name|ObjectInspectorUtils
operator|.
name|getStandardStructFieldRef
argument_list|(
name|fieldName
argument_list|,
name|fields
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|getAllStructFieldRefs
parameter_list|()
block|{
return|return
name|fields
return|;
block|}
comment|// With Data - Unsupported for the test case
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|Object
name|getStructFieldData
parameter_list|(
name|Object
name|data
parameter_list|,
name|StructField
name|fieldRef
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
comment|// Unsupported for the test case
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|List
argument_list|<
name|Object
argument_list|>
name|getStructFieldsDataAsList
parameter_list|(
name|Object
name|data
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

