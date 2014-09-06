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
name|ql
operator|.
name|optimizer
operator|.
name|optiq
operator|.
name|translator
package|;
end_package

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigDecimal
import|;
end_import

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
name|Calendar
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|GregorianCalendar
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|type
operator|.
name|Decimal128
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|FunctionRegistry
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
name|parse
operator|.
name|ParseUtils
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
name|parse
operator|.
name|RowResolver
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
name|parse
operator|.
name|SemanticException
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
name|plan
operator|.
name|ExprNodeColumnDesc
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
name|plan
operator|.
name|ExprNodeConstantDesc
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
name|plan
operator|.
name|ExprNodeDesc
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
name|plan
operator|.
name|ExprNodeFieldDesc
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
name|plan
operator|.
name|ExprNodeGenericFuncDesc
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
name|plan
operator|.
name|ExprNodeNullDesc
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDF
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFBaseCompare
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFBaseNumeric
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFBridge
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFTimestamp
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFToBinary
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFToChar
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFToDate
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFToDecimal
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFToUnixTimeStamp
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFToVarchar
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
name|ConstantObjectInspector
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
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
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
name|typeinfo
operator|.
name|PrimitiveTypeInfo
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
name|typeinfo
operator|.
name|TypeInfo
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
name|typeinfo
operator|.
name|TypeInfoUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|RelNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|relopt
operator|.
name|RelOptCluster
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|reltype
operator|.
name|RelDataType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|reltype
operator|.
name|RelDataTypeFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rex
operator|.
name|RexBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rex
operator|.
name|RexCall
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rex
operator|.
name|RexNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rex
operator|.
name|RexUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|sql
operator|.
name|SqlOperator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|sql
operator|.
name|fun
operator|.
name|SqlCastFunction
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
operator|.
name|Builder
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_class
specifier|public
class|class
name|RexNodeConverter
block|{
specifier|private
specifier|static
class|class
name|InputCtx
block|{
specifier|private
specifier|final
name|RelDataType
name|m_optiqInpDataType
decl_stmt|;
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|m_hiveNameToPosMap
decl_stmt|;
specifier|private
specifier|final
name|RowResolver
name|m_hiveRR
decl_stmt|;
specifier|private
specifier|final
name|int
name|m_offsetInOptiqSchema
decl_stmt|;
specifier|private
name|InputCtx
parameter_list|(
name|RelDataType
name|optiqInpDataType
parameter_list|,
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|hiveNameToPosMap
parameter_list|,
name|RowResolver
name|hiveRR
parameter_list|,
name|int
name|offsetInOptiqSchema
parameter_list|)
block|{
name|m_optiqInpDataType
operator|=
name|optiqInpDataType
expr_stmt|;
name|m_hiveNameToPosMap
operator|=
name|hiveNameToPosMap
expr_stmt|;
name|m_hiveRR
operator|=
name|hiveRR
expr_stmt|;
name|m_offsetInOptiqSchema
operator|=
name|offsetInOptiqSchema
expr_stmt|;
block|}
block|}
empty_stmt|;
specifier|private
specifier|final
name|RelOptCluster
name|m_cluster
decl_stmt|;
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|InputCtx
argument_list|>
name|m_inputCtxs
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|m_flattenExpr
decl_stmt|;
specifier|public
name|RexNodeConverter
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|RelDataType
name|inpDataType
parameter_list|,
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|nameToPosMap
parameter_list|,
name|int
name|offset
parameter_list|,
name|boolean
name|flattenExpr
parameter_list|)
block|{
name|this
operator|.
name|m_cluster
operator|=
name|cluster
expr_stmt|;
name|m_inputCtxs
operator|=
name|ImmutableList
operator|.
name|of
argument_list|(
operator|new
name|InputCtx
argument_list|(
name|inpDataType
argument_list|,
name|nameToPosMap
argument_list|,
literal|null
argument_list|,
name|offset
argument_list|)
argument_list|)
expr_stmt|;
name|m_flattenExpr
operator|=
name|flattenExpr
expr_stmt|;
block|}
specifier|public
name|RexNodeConverter
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|List
argument_list|<
name|InputCtx
argument_list|>
name|inpCtxLst
parameter_list|,
name|boolean
name|flattenExpr
parameter_list|)
block|{
name|this
operator|.
name|m_cluster
operator|=
name|cluster
expr_stmt|;
name|m_inputCtxs
operator|=
name|ImmutableList
operator|.
expr|<
name|InputCtx
operator|>
name|builder
argument_list|()
operator|.
name|addAll
argument_list|(
name|inpCtxLst
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|m_flattenExpr
operator|=
name|flattenExpr
expr_stmt|;
block|}
specifier|public
name|RexNode
name|convert
parameter_list|(
name|ExprNodeDesc
name|expr
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|expr
operator|instanceof
name|ExprNodeNullDesc
condition|)
block|{
return|return
name|m_cluster
operator|.
name|getRexBuilder
argument_list|()
operator|.
name|makeNullLiteral
argument_list|(
name|TypeConverter
operator|.
name|convert
argument_list|(
name|expr
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
name|m_cluster
operator|.
name|getRexBuilder
argument_list|()
operator|.
name|getTypeFactory
argument_list|()
argument_list|)
operator|.
name|getSqlTypeName
argument_list|()
argument_list|)
return|;
block|}
if|if
condition|(
name|expr
operator|instanceof
name|ExprNodeGenericFuncDesc
condition|)
block|{
return|return
name|convert
argument_list|(
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|expr
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|expr
operator|instanceof
name|ExprNodeConstantDesc
condition|)
block|{
return|return
name|convert
argument_list|(
operator|(
name|ExprNodeConstantDesc
operator|)
name|expr
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|expr
operator|instanceof
name|ExprNodeColumnDesc
condition|)
block|{
return|return
name|convert
argument_list|(
operator|(
name|ExprNodeColumnDesc
operator|)
name|expr
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|expr
operator|instanceof
name|ExprNodeFieldDesc
condition|)
block|{
return|return
name|convert
argument_list|(
operator|(
name|ExprNodeFieldDesc
operator|)
name|expr
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unsupported Expression"
argument_list|)
throw|;
block|}
comment|// TODO: handle ExprNodeColumnListDesc
block|}
specifier|private
name|RexNode
name|convert
parameter_list|(
specifier|final
name|ExprNodeFieldDesc
name|fieldDesc
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
name|m_cluster
operator|.
name|getRexBuilder
argument_list|()
operator|.
name|makeFieldAccess
argument_list|(
name|convert
argument_list|(
name|fieldDesc
operator|.
name|getDesc
argument_list|()
argument_list|)
argument_list|,
name|fieldDesc
operator|.
name|getFieldName
argument_list|()
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|private
name|RexNode
name|convert
parameter_list|(
specifier|final
name|ExprNodeGenericFuncDesc
name|func
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ExprNodeDesc
name|tmpExprNode
decl_stmt|;
name|RexNode
name|tmpRN
decl_stmt|;
name|TypeInfo
name|tgtDT
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|RexNode
argument_list|>
name|childRexNodeLst
init|=
operator|new
name|LinkedList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
decl_stmt|;
name|Builder
argument_list|<
name|RelDataType
argument_list|>
name|argTypeBldr
init|=
name|ImmutableList
operator|.
expr|<
name|RelDataType
operator|>
name|builder
argument_list|()
decl_stmt|;
comment|// TODO: 1) Expand to other functions as needed 2) What about types
comment|// other
comment|// than primitive
if|if
condition|(
name|func
operator|.
name|getGenericUDF
argument_list|()
operator|instanceof
name|GenericUDFBaseNumeric
condition|)
block|{
name|tgtDT
operator|=
name|func
operator|.
name|getTypeInfo
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|func
operator|.
name|getGenericUDF
argument_list|()
operator|instanceof
name|GenericUDFBaseCompare
condition|)
block|{
if|if
condition|(
name|func
operator|.
name|getChildren
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|2
condition|)
block|{
name|tgtDT
operator|=
name|FunctionRegistry
operator|.
name|getCommonClassForComparison
argument_list|(
name|func
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
name|func
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|ExprNodeDesc
name|childExpr
range|:
name|func
operator|.
name|getChildren
argument_list|()
control|)
block|{
name|tmpExprNode
operator|=
name|childExpr
expr_stmt|;
if|if
condition|(
name|tgtDT
operator|!=
literal|null
operator|&&
name|TypeInfoUtils
operator|.
name|isConversionRequiredForComparison
argument_list|(
name|tgtDT
argument_list|,
name|childExpr
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
condition|)
block|{
name|tmpExprNode
operator|=
name|ParseUtils
operator|.
name|createConversionCast
argument_list|(
name|childExpr
argument_list|,
operator|(
name|PrimitiveTypeInfo
operator|)
name|tgtDT
argument_list|)
expr_stmt|;
block|}
name|argTypeBldr
operator|.
name|add
argument_list|(
name|TypeConverter
operator|.
name|convert
argument_list|(
name|tmpExprNode
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
name|m_cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|tmpRN
operator|=
name|convert
argument_list|(
name|tmpExprNode
argument_list|)
expr_stmt|;
name|childRexNodeLst
operator|.
name|add
argument_list|(
name|tmpRN
argument_list|)
expr_stmt|;
block|}
comment|// This is an explicit cast
name|RexNode
name|expr
init|=
literal|null
decl_stmt|;
name|RelDataType
name|retType
init|=
literal|null
decl_stmt|;
name|expr
operator|=
name|handleExplicitCast
argument_list|(
name|func
argument_list|,
name|childRexNodeLst
argument_list|)
expr_stmt|;
if|if
condition|(
name|expr
operator|==
literal|null
condition|)
block|{
name|retType
operator|=
operator|(
name|expr
operator|!=
literal|null
operator|)
condition|?
name|expr
operator|.
name|getType
argument_list|()
else|:
name|TypeConverter
operator|.
name|convert
argument_list|(
name|func
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
name|m_cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|)
expr_stmt|;
name|SqlOperator
name|optiqOp
init|=
name|SqlFunctionConverter
operator|.
name|getOptiqOperator
argument_list|(
name|func
operator|.
name|getGenericUDF
argument_list|()
argument_list|,
name|argTypeBldr
operator|.
name|build
argument_list|()
argument_list|,
name|retType
argument_list|)
decl_stmt|;
name|expr
operator|=
name|m_cluster
operator|.
name|getRexBuilder
argument_list|()
operator|.
name|makeCall
argument_list|(
name|optiqOp
argument_list|,
name|childRexNodeLst
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|retType
operator|=
name|expr
operator|.
name|getType
argument_list|()
expr_stmt|;
block|}
comment|// TODO: Cast Function in Optiq have a bug where it infertype on cast throws
comment|// an exception
if|if
condition|(
name|m_flattenExpr
operator|&&
operator|(
name|expr
operator|instanceof
name|RexCall
operator|)
operator|&&
operator|!
operator|(
operator|(
operator|(
name|RexCall
operator|)
name|expr
operator|)
operator|.
name|getOperator
argument_list|()
operator|instanceof
name|SqlCastFunction
operator|)
condition|)
block|{
name|RexCall
name|call
init|=
operator|(
name|RexCall
operator|)
name|expr
decl_stmt|;
name|expr
operator|=
name|m_cluster
operator|.
name|getRexBuilder
argument_list|()
operator|.
name|makeCall
argument_list|(
name|retType
argument_list|,
name|call
operator|.
name|getOperator
argument_list|()
argument_list|,
name|RexUtil
operator|.
name|flatten
argument_list|(
name|call
operator|.
name|getOperands
argument_list|()
argument_list|,
name|call
operator|.
name|getOperator
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|expr
return|;
block|}
specifier|private
name|boolean
name|castExprUsingUDFBridge
parameter_list|(
name|GenericUDF
name|gUDF
parameter_list|)
block|{
name|boolean
name|castExpr
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|gUDF
operator|!=
literal|null
operator|&&
name|gUDF
operator|instanceof
name|GenericUDFBridge
condition|)
block|{
name|String
name|udfClassName
init|=
operator|(
operator|(
name|GenericUDFBridge
operator|)
name|gUDF
operator|)
operator|.
name|getUdfClassName
argument_list|()
decl_stmt|;
if|if
condition|(
name|udfClassName
operator|!=
literal|null
condition|)
block|{
name|int
name|sp
init|=
name|udfClassName
operator|.
name|lastIndexOf
argument_list|(
literal|'.'
argument_list|)
decl_stmt|;
comment|// TODO: add method to UDFBridge to say if it is a cast func
if|if
condition|(
name|sp
operator|>=
literal|0
operator|&
operator|(
name|sp
operator|+
literal|1
operator|)
operator|<
name|udfClassName
operator|.
name|length
argument_list|()
condition|)
block|{
name|udfClassName
operator|=
name|udfClassName
operator|.
name|substring
argument_list|(
name|sp
operator|+
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|udfClassName
operator|.
name|equals
argument_list|(
literal|"UDFToBoolean"
argument_list|)
operator|||
name|udfClassName
operator|.
name|equals
argument_list|(
literal|"UDFToByte"
argument_list|)
operator|||
name|udfClassName
operator|.
name|equals
argument_list|(
literal|"UDFToDouble"
argument_list|)
operator|||
name|udfClassName
operator|.
name|equals
argument_list|(
literal|"UDFToInteger"
argument_list|)
operator|||
name|udfClassName
operator|.
name|equals
argument_list|(
literal|"UDFToLong"
argument_list|)
operator|||
name|udfClassName
operator|.
name|equals
argument_list|(
literal|"UDFToShort"
argument_list|)
operator|||
name|udfClassName
operator|.
name|equals
argument_list|(
literal|"UDFToFloat"
argument_list|)
operator|||
name|udfClassName
operator|.
name|equals
argument_list|(
literal|"UDFToString"
argument_list|)
condition|)
name|castExpr
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
return|return
name|castExpr
return|;
block|}
specifier|private
name|RexNode
name|handleExplicitCast
parameter_list|(
name|ExprNodeGenericFuncDesc
name|func
parameter_list|,
name|List
argument_list|<
name|RexNode
argument_list|>
name|childRexNodeLst
parameter_list|)
block|{
name|RexNode
name|castExpr
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|childRexNodeLst
operator|!=
literal|null
operator|&&
name|childRexNodeLst
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|GenericUDF
name|udf
init|=
name|func
operator|.
name|getGenericUDF
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|udf
operator|instanceof
name|GenericUDFToChar
operator|)
operator|||
operator|(
name|udf
operator|instanceof
name|GenericUDFToVarchar
operator|)
operator|||
operator|(
name|udf
operator|instanceof
name|GenericUDFToDecimal
operator|)
operator|||
operator|(
name|udf
operator|instanceof
name|GenericUDFToDate
operator|)
operator|||
operator|(
name|udf
operator|instanceof
name|GenericUDFToBinary
operator|)
operator|||
name|castExprUsingUDFBridge
argument_list|(
name|udf
argument_list|)
condition|)
block|{
comment|// || (udf instanceof GenericUDFToUnixTimeStamp) || (udf instanceof
comment|// GenericUDFTimestamp) || castExprUsingUDFBridge(udf)) {
name|castExpr
operator|=
name|m_cluster
operator|.
name|getRexBuilder
argument_list|()
operator|.
name|makeCast
argument_list|(
name|TypeConverter
operator|.
name|convert
argument_list|(
name|func
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
name|m_cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|)
argument_list|,
name|childRexNodeLst
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|castExpr
return|;
block|}
specifier|private
name|InputCtx
name|getInputCtx
parameter_list|(
name|ExprNodeColumnDesc
name|col
parameter_list|)
throws|throws
name|SemanticException
block|{
name|InputCtx
name|ctxLookingFor
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|m_inputCtxs
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|ctxLookingFor
operator|=
name|m_inputCtxs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|tableAlias
init|=
name|col
operator|.
name|getTabAlias
argument_list|()
decl_stmt|;
name|String
name|colAlias
init|=
name|col
operator|.
name|getColumn
argument_list|()
decl_stmt|;
name|int
name|noInp
init|=
literal|0
decl_stmt|;
for|for
control|(
name|InputCtx
name|ic
range|:
name|m_inputCtxs
control|)
block|{
if|if
condition|(
name|tableAlias
operator|==
literal|null
operator|||
name|ic
operator|.
name|m_hiveRR
operator|.
name|hasTableAlias
argument_list|(
name|tableAlias
argument_list|)
condition|)
block|{
if|if
condition|(
name|ic
operator|.
name|m_hiveRR
operator|.
name|getPosition
argument_list|(
name|colAlias
argument_list|)
operator|>=
literal|0
condition|)
block|{
name|ctxLookingFor
operator|=
name|ic
expr_stmt|;
name|noInp
operator|++
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|noInp
operator|>
literal|1
condition|)
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Ambigous column mapping"
argument_list|)
throw|;
block|}
return|return
name|ctxLookingFor
return|;
block|}
specifier|protected
name|RexNode
name|convert
parameter_list|(
name|ExprNodeColumnDesc
name|col
parameter_list|)
throws|throws
name|SemanticException
block|{
name|InputCtx
name|ic
init|=
name|getInputCtx
argument_list|(
name|col
argument_list|)
decl_stmt|;
name|int
name|pos
init|=
name|ic
operator|.
name|m_hiveNameToPosMap
operator|.
name|get
argument_list|(
name|col
operator|.
name|getColumn
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|m_cluster
operator|.
name|getRexBuilder
argument_list|()
operator|.
name|makeInputRef
argument_list|(
name|ic
operator|.
name|m_optiqInpDataType
operator|.
name|getFieldList
argument_list|()
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|pos
operator|+
name|ic
operator|.
name|m_offsetInOptiqSchema
argument_list|)
return|;
block|}
specifier|protected
name|RexNode
name|convert
parameter_list|(
name|ExprNodeConstantDesc
name|literal
parameter_list|)
block|{
name|RexBuilder
name|rexBuilder
init|=
name|m_cluster
operator|.
name|getRexBuilder
argument_list|()
decl_stmt|;
name|RelDataTypeFactory
name|dtFactory
init|=
name|rexBuilder
operator|.
name|getTypeFactory
argument_list|()
decl_stmt|;
name|PrimitiveTypeInfo
name|hiveType
init|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|literal
operator|.
name|getTypeInfo
argument_list|()
decl_stmt|;
name|RelDataType
name|optiqDataType
init|=
name|TypeConverter
operator|.
name|convert
argument_list|(
name|hiveType
argument_list|,
name|dtFactory
argument_list|)
decl_stmt|;
name|PrimitiveCategory
name|hiveTypeCategory
init|=
name|hiveType
operator|.
name|getPrimitiveCategory
argument_list|()
decl_stmt|;
name|ConstantObjectInspector
name|coi
init|=
name|literal
operator|.
name|getWritableObjectInspector
argument_list|()
decl_stmt|;
name|Object
name|value
init|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardJavaObject
argument_list|(
name|literal
operator|.
name|getWritableObjectInspector
argument_list|()
operator|.
name|getWritableConstantValue
argument_list|()
argument_list|,
name|coi
argument_list|)
decl_stmt|;
name|RexNode
name|optiqLiteral
init|=
literal|null
decl_stmt|;
comment|// TODO: Verify if we need to use ConstantObjectInspector to unwrap data
switch|switch
condition|(
name|hiveTypeCategory
condition|)
block|{
case|case
name|BOOLEAN
case|:
name|optiqLiteral
operator|=
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
operator|(
operator|(
name|Boolean
operator|)
name|value
operator|)
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|BYTE
case|:
name|optiqLiteral
operator|=
name|rexBuilder
operator|.
name|makeExactLiteral
argument_list|(
operator|new
name|BigDecimal
argument_list|(
operator|(
name|Short
operator|)
name|value
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|SHORT
case|:
name|optiqLiteral
operator|=
name|rexBuilder
operator|.
name|makeExactLiteral
argument_list|(
operator|new
name|BigDecimal
argument_list|(
operator|(
name|Short
operator|)
name|value
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|INT
case|:
name|optiqLiteral
operator|=
name|rexBuilder
operator|.
name|makeExactLiteral
argument_list|(
operator|new
name|BigDecimal
argument_list|(
operator|(
name|Integer
operator|)
name|value
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|optiqLiteral
operator|=
name|rexBuilder
operator|.
name|makeBigintLiteral
argument_list|(
operator|new
name|BigDecimal
argument_list|(
operator|(
name|Long
operator|)
name|value
argument_list|)
argument_list|)
expr_stmt|;
break|break;
comment|// TODO: is Decimal an exact numeric or approximate numeric?
case|case
name|DECIMAL
case|:
if|if
condition|(
name|value
operator|instanceof
name|HiveDecimal
condition|)
name|value
operator|=
operator|(
operator|(
name|HiveDecimal
operator|)
name|value
operator|)
operator|.
name|bigDecimalValue
argument_list|()
expr_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|Decimal128
condition|)
name|value
operator|=
operator|(
operator|(
name|Decimal128
operator|)
name|value
operator|)
operator|.
name|toBigDecimal
argument_list|()
expr_stmt|;
name|optiqLiteral
operator|=
name|rexBuilder
operator|.
name|makeExactLiteral
argument_list|(
operator|(
name|BigDecimal
operator|)
name|value
argument_list|)
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
name|optiqLiteral
operator|=
name|rexBuilder
operator|.
name|makeApproxLiteral
argument_list|(
operator|new
name|BigDecimal
argument_list|(
operator|(
name|Float
operator|)
name|value
argument_list|)
argument_list|,
name|optiqDataType
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|optiqLiteral
operator|=
name|rexBuilder
operator|.
name|makeApproxLiteral
argument_list|(
operator|new
name|BigDecimal
argument_list|(
operator|(
name|Double
operator|)
name|value
argument_list|)
argument_list|,
name|optiqDataType
argument_list|)
expr_stmt|;
break|break;
case|case
name|CHAR
case|:
if|if
condition|(
name|value
operator|instanceof
name|HiveChar
condition|)
name|value
operator|=
operator|(
operator|(
name|HiveChar
operator|)
name|value
operator|)
operator|.
name|getValue
argument_list|()
expr_stmt|;
name|optiqLiteral
operator|=
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
operator|(
name|String
operator|)
name|value
argument_list|)
expr_stmt|;
break|break;
case|case
name|VARCHAR
case|:
if|if
condition|(
name|value
operator|instanceof
name|HiveVarchar
condition|)
name|value
operator|=
operator|(
operator|(
name|HiveVarchar
operator|)
name|value
operator|)
operator|.
name|getValue
argument_list|()
expr_stmt|;
name|optiqLiteral
operator|=
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
operator|(
name|String
operator|)
name|value
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRING
case|:
name|optiqLiteral
operator|=
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
operator|(
name|String
operator|)
name|value
argument_list|)
expr_stmt|;
break|break;
case|case
name|DATE
case|:
name|Calendar
name|cal
init|=
operator|new
name|GregorianCalendar
argument_list|()
decl_stmt|;
name|cal
operator|.
name|setTime
argument_list|(
operator|(
name|Date
operator|)
name|value
argument_list|)
expr_stmt|;
name|optiqLiteral
operator|=
name|rexBuilder
operator|.
name|makeDateLiteral
argument_list|(
name|cal
argument_list|)
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|optiqLiteral
operator|=
name|rexBuilder
operator|.
name|makeTimestampLiteral
argument_list|(
operator|(
name|Calendar
operator|)
name|value
argument_list|,
name|RelDataType
operator|.
name|PRECISION_NOT_SPECIFIED
argument_list|)
expr_stmt|;
break|break;
case|case
name|BINARY
case|:
case|case
name|VOID
case|:
case|case
name|UNKNOWN
case|:
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"UnSupported Literal"
argument_list|)
throw|;
block|}
return|return
name|optiqLiteral
return|;
block|}
specifier|public
specifier|static
name|RexNode
name|convert
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|ExprNodeDesc
name|joinCondnExprNode
parameter_list|,
name|List
argument_list|<
name|RelNode
argument_list|>
name|inputRels
parameter_list|,
name|LinkedHashMap
argument_list|<
name|RelNode
argument_list|,
name|RowResolver
argument_list|>
name|relToHiveRR
parameter_list|,
name|Map
argument_list|<
name|RelNode
argument_list|,
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|relToHiveColNameOptiqPosMap
parameter_list|,
name|boolean
name|flattenExpr
parameter_list|)
throws|throws
name|SemanticException
block|{
name|List
argument_list|<
name|InputCtx
argument_list|>
name|inputCtxLst
init|=
operator|new
name|ArrayList
argument_list|<
name|InputCtx
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|offSet
init|=
literal|0
decl_stmt|;
for|for
control|(
name|RelNode
name|r
range|:
name|inputRels
control|)
block|{
name|inputCtxLst
operator|.
name|add
argument_list|(
operator|new
name|InputCtx
argument_list|(
name|r
operator|.
name|getRowType
argument_list|()
argument_list|,
name|relToHiveColNameOptiqPosMap
operator|.
name|get
argument_list|(
name|r
argument_list|)
argument_list|,
name|relToHiveRR
operator|.
name|get
argument_list|(
name|r
argument_list|)
argument_list|,
name|offSet
argument_list|)
argument_list|)
expr_stmt|;
name|offSet
operator|+=
name|r
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldCount
argument_list|()
expr_stmt|;
block|}
return|return
operator|(
operator|new
name|RexNodeConverter
argument_list|(
name|cluster
argument_list|,
name|inputCtxLst
argument_list|,
name|flattenExpr
argument_list|)
operator|)
operator|.
name|convert
argument_list|(
name|joinCondnExprNode
argument_list|)
return|;
block|}
block|}
end_class

end_unit

