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
name|ql
operator|.
name|udf
operator|.
name|generic
package|;
end_package

begin_import
import|import
name|javaewah
operator|.
name|EWAHCompressedBitmap
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
name|Description
import|;
end_import

begin_comment
comment|/**  * GenericEWAHUDFBitmapAnd.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"ewah_bitmap_and"
argument_list|,
name|value
operator|=
literal|"_FUNC_(b1, b2) - Return an EWAH-compressed bitmap that is the bitwise AND of two bitmaps."
argument_list|)
specifier|public
class|class
name|GenericUDFEWAHBitmapAnd
extends|extends
name|AbstractGenericUDFEWAHBitmapBop
block|{
specifier|public
name|GenericUDFEWAHBitmapAnd
parameter_list|()
block|{
name|super
argument_list|(
literal|"EWAH_BITMAP_AND"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|EWAHCompressedBitmap
name|bitmapBop
parameter_list|(
name|EWAHCompressedBitmap
name|bitmap1
parameter_list|,
name|EWAHCompressedBitmap
name|bitmap2
parameter_list|)
block|{
return|return
name|bitmap1
operator|.
name|and
argument_list|(
name|bitmap2
argument_list|)
return|;
block|}
block|}
end_class

end_unit

