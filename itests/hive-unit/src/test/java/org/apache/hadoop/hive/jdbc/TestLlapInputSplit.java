begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|jdbc
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|HashMap
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
name|io
operator|.
name|Text
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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|metastore
operator|.
name|api
operator|.
name|Schema
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
name|mapred
operator|.
name|SplitLocationInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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
name|junit
operator|.
name|rules
operator|.
name|ExpectedException
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_class
specifier|public
class|class
name|TestLlapInputSplit
block|{
annotation|@
name|Test
specifier|public
name|void
name|testWritable
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|splitNum
init|=
literal|88
decl_stmt|;
name|byte
index|[]
name|planBytes
init|=
literal|"0123456789987654321"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|byte
index|[]
name|fragmentBytes
init|=
literal|"abcdefghijklmnopqrstuvwxyz"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|SplitLocationInfo
index|[]
name|locations
init|=
block|{
operator|new
name|SplitLocationInfo
argument_list|(
literal|"location1"
argument_list|,
literal|false
argument_list|)
block|,
operator|new
name|SplitLocationInfo
argument_list|(
literal|"location2"
argument_list|,
literal|false
argument_list|)
block|,     }
decl_stmt|;
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
name|fields
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
decl_stmt|;
name|fields
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"col1"
argument_list|,
literal|"string"
argument_list|,
literal|"comment1"
argument_list|)
argument_list|)
expr_stmt|;
name|fields
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"col2"
argument_list|,
literal|"int"
argument_list|,
literal|"comment2"
argument_list|)
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|properties
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|properties
operator|.
name|put
argument_list|(
literal|"key1"
argument_list|,
literal|"val1"
argument_list|)
expr_stmt|;
name|Schema
name|schema
init|=
operator|new
name|Schema
argument_list|(
name|fields
argument_list|,
name|properties
argument_list|)
decl_stmt|;
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
name|LlapInputSplit
name|split1
init|=
operator|new
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
name|LlapInputSplit
argument_list|(
name|splitNum
argument_list|,
name|planBytes
argument_list|,
name|fragmentBytes
argument_list|,
name|locations
argument_list|,
name|schema
argument_list|)
decl_stmt|;
name|ByteArrayOutputStream
name|byteOutStream
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|dataOut
init|=
operator|new
name|DataOutputStream
argument_list|(
name|byteOutStream
argument_list|)
decl_stmt|;
name|split1
operator|.
name|write
argument_list|(
name|dataOut
argument_list|)
expr_stmt|;
name|ByteArrayInputStream
name|byteInStream
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|byteOutStream
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|DataInputStream
name|dataIn
init|=
operator|new
name|DataInputStream
argument_list|(
name|byteInStream
argument_list|)
decl_stmt|;
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
name|LlapInputSplit
name|split2
init|=
operator|new
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
name|LlapInputSplit
argument_list|()
decl_stmt|;
name|split2
operator|.
name|readFields
argument_list|(
name|dataIn
argument_list|)
expr_stmt|;
comment|// Did we read all the data?
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|byteInStream
operator|.
name|available
argument_list|()
argument_list|)
expr_stmt|;
name|checkLlapSplits
argument_list|(
name|split1
argument_list|,
name|split2
argument_list|)
expr_stmt|;
comment|// Try JDBC LlapInputSplits
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|jdbc
operator|.
name|LlapInputSplit
argument_list|<
name|Text
argument_list|>
name|jdbcSplit1
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|jdbc
operator|.
name|LlapInputSplit
argument_list|<
name|Text
argument_list|>
argument_list|(
name|split1
argument_list|,
literal|"org.apache.hadoop.hive.llap.LlapInputFormat"
argument_list|)
decl_stmt|;
name|byteOutStream
operator|.
name|reset
argument_list|()
expr_stmt|;
name|jdbcSplit1
operator|.
name|write
argument_list|(
name|dataOut
argument_list|)
expr_stmt|;
name|byteInStream
operator|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|byteOutStream
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|dataIn
operator|=
operator|new
name|DataInputStream
argument_list|(
name|byteInStream
argument_list|)
expr_stmt|;
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|jdbc
operator|.
name|LlapInputSplit
argument_list|<
name|Text
argument_list|>
name|jdbcSplit2
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|jdbc
operator|.
name|LlapInputSplit
argument_list|<
name|Text
argument_list|>
argument_list|()
decl_stmt|;
name|jdbcSplit2
operator|.
name|readFields
argument_list|(
name|dataIn
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|byteInStream
operator|.
name|available
argument_list|()
argument_list|)
expr_stmt|;
name|checkLlapSplits
argument_list|(
operator|(
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
name|LlapInputSplit
operator|)
name|jdbcSplit1
operator|.
name|getSplit
argument_list|()
argument_list|,
operator|(
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
name|LlapInputSplit
operator|)
name|jdbcSplit2
operator|.
name|getSplit
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|jdbcSplit1
operator|.
name|getInputFormat
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|,
name|jdbcSplit2
operator|.
name|getInputFormat
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|static
name|void
name|checkLlapSplits
parameter_list|(
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
name|LlapInputSplit
name|split1
parameter_list|,
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
name|LlapInputSplit
name|split2
parameter_list|)
throws|throws
name|Exception
block|{
name|assertEquals
argument_list|(
name|split1
operator|.
name|getSplitNum
argument_list|()
argument_list|,
name|split2
operator|.
name|getSplitNum
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|split1
operator|.
name|getPlanBytes
argument_list|()
argument_list|,
name|split2
operator|.
name|getPlanBytes
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|split1
operator|.
name|getFragmentBytes
argument_list|()
argument_list|,
name|split2
operator|.
name|getFragmentBytes
argument_list|()
argument_list|)
expr_stmt|;
name|SplitLocationInfo
index|[]
name|locationInfo1
init|=
name|split1
operator|.
name|getLocationInfo
argument_list|()
decl_stmt|;
name|SplitLocationInfo
index|[]
name|locationInfo2
init|=
name|split2
operator|.
name|getLocationInfo
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|idx
init|=
literal|0
init|;
name|idx
operator|<
name|locationInfo1
operator|.
name|length
condition|;
operator|++
name|idx
control|)
block|{
name|assertEquals
argument_list|(
name|locationInfo1
index|[
name|idx
index|]
operator|.
name|getLocation
argument_list|()
argument_list|,
name|locationInfo2
index|[
name|idx
index|]
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|locationInfo1
index|[
name|idx
index|]
operator|.
name|isInMemory
argument_list|()
argument_list|,
name|locationInfo2
index|[
name|idx
index|]
operator|.
name|isInMemory
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|locationInfo1
index|[
name|idx
index|]
operator|.
name|isOnDisk
argument_list|()
argument_list|,
name|locationInfo2
index|[
name|idx
index|]
operator|.
name|isOnDisk
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertArrayEquals
argument_list|(
name|split1
operator|.
name|getLocations
argument_list|()
argument_list|,
name|split2
operator|.
name|getLocations
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|split1
operator|.
name|getSchema
argument_list|()
argument_list|,
name|split2
operator|.
name|getSchema
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

