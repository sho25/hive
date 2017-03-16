begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hplsql
package|;
end_package

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
name|metadata
operator|.
name|HiveException
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
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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
name|primitive
operator|.
name|StringObjectInspector
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
operator|.
name|DeferredObject
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
operator|.
name|DeferredJavaObject
import|;
end_import

begin_class
specifier|public
class|class
name|TestHplsqlUdf
block|{
name|StringObjectInspector
name|queryOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
decl_stmt|;
name|ObjectInspector
name|argOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
decl_stmt|;
comment|/**    * test evaluate for exec init and setParameters    */
annotation|@
name|Test
specifier|public
name|void
name|testEvaluateWithoutRun
parameter_list|()
throws|throws
name|HiveException
block|{
comment|// init udf
name|Udf
name|udf
init|=
operator|new
name|Udf
argument_list|()
decl_stmt|;
name|ObjectInspector
index|[]
name|initArguments
init|=
block|{
name|queryOI
block|,
name|argOI
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|initArguments
argument_list|)
expr_stmt|;
comment|//set arguments
name|DeferredObject
name|queryObj
init|=
operator|new
name|DeferredJavaObject
argument_list|(
literal|"hello(:1)"
argument_list|)
decl_stmt|;
name|DeferredObject
name|argObj
init|=
operator|new
name|DeferredJavaObject
argument_list|(
literal|"name"
argument_list|)
decl_stmt|;
name|DeferredObject
index|[]
name|argumentsObj
init|=
block|{
name|queryObj
block|,
name|argObj
block|}
decl_stmt|;
comment|// init exec and set parameters, included
name|udf
operator|.
name|initExec
argument_list|(
name|argumentsObj
argument_list|)
expr_stmt|;
name|udf
operator|.
name|setParameters
argument_list|(
name|argumentsObj
argument_list|)
expr_stmt|;
comment|// checking var exists and its value is right
name|Var
name|var
init|=
name|udf
operator|.
name|exec
operator|.
name|findVariable
argument_list|(
literal|":1"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|var
argument_list|)
expr_stmt|;
name|String
name|val
init|=
operator|(
name|String
operator|)
name|var
operator|.
name|value
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|val
argument_list|,
literal|"name"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

