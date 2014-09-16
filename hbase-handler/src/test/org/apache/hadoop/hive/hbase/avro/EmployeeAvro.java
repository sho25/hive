begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Autogenerated by Avro  *   * DO NOT EDIT DIRECTLY  */
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
name|hbase
operator|.
name|avro
package|;
end_package

begin_interface
annotation|@
name|SuppressWarnings
argument_list|(
literal|"all"
argument_list|)
annotation|@
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|specific
operator|.
name|AvroGenerated
specifier|public
interface|interface
name|EmployeeAvro
block|{
specifier|public
specifier|static
specifier|final
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|Protocol
name|PROTOCOL
init|=
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|Protocol
operator|.
name|parse
argument_list|(
literal|"{\"protocol\":\"EmployeeAvro\",\"namespace\":\"org.apache.hadoop.hive.hbase.avro\",\"types\":[{\"type\":\"enum\",\"name\":\"Gender\",\"symbols\":[\"MALE\",\"FEMALE\"]},{\"type\":\"record\",\"name\":\"HomePhone\",\"fields\":[{\"name\":\"areaCode\",\"type\":\"long\"},{\"name\":\"number\",\"type\":\"long\"}]},{\"type\":\"record\",\"name\":\"OfficePhone\",\"fields\":[{\"name\":\"areaCode\",\"type\":\"long\"},{\"name\":\"number\",\"type\":\"long\"}]},{\"type\":\"record\",\"name\":\"Address\",\"fields\":[{\"name\":\"address1\",\"type\":\"string\"},{\"name\":\"address2\",\"type\":\"string\"},{\"name\":\"city\",\"type\":\"string\"},{\"name\":\"zipcode\",\"type\":\"long\"},{\"name\":\"county\",\"type\":[\"HomePhone\",\"OfficePhone\",\"string\",\"null\"]},{\"name\":\"aliases\",\"type\":[{\"type\":\"array\",\"items\":\"string\"},\"null\"]},{\"name\":\"metadata\",\"type\":[\"null\",{\"type\":\"map\",\"values\":\"string\"}]}]},{\"type\":\"record\",\"name\":\"ContactInfo\",\"fields\":[{\"name\":\"address\",\"type\":[{\"type\":\"array\",\"items\":\"Address\"},\"null\"]},{\"name\":\"homePhone\",\"type\":\"HomePhone\"},{\"name\":\"officePhone\",\"type\":\"OfficePhone\"}]},{\"type\":\"record\",\"name\":\"Employee\",\"fields\":[{\"name\":\"employeeName\",\"type\":\"string\"},{\"name\":\"employeeID\",\"type\":\"long\"},{\"name\":\"age\",\"type\":\"long\"},{\"name\":\"gender\",\"type\":\"Gender\"},{\"name\":\"contactInfo\",\"type\":\"ContactInfo\"}]}],\"messages\":{}}"
argument_list|)
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"all"
argument_list|)
specifier|public
interface|interface
name|Callback
extends|extends
name|EmployeeAvro
block|{
specifier|public
specifier|static
specifier|final
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|Protocol
name|PROTOCOL
init|=
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|hbase
operator|.
name|avro
operator|.
name|EmployeeAvro
operator|.
name|PROTOCOL
decl_stmt|;
block|}
block|}
end_interface

end_unit

