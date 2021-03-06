begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Annotation
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_class
specifier|public
class|class
name|AnnotationUtils
block|{
comment|// until JDK8, this had a lock around annotationClass to avoid
comment|// https://bugs.openjdk.java.net/browse/JDK-7122142
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Annotation
parameter_list|>
name|T
name|getAnnotation
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|annotationClass
parameter_list|)
block|{
return|return
name|clazz
operator|.
name|getAnnotation
argument_list|(
name|annotationClass
argument_list|)
return|;
block|}
comment|// until JDK8, this had a lock around annotationClass to avoid
comment|// https://bugs.openjdk.java.net/browse/JDK-7122142
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Annotation
parameter_list|>
name|T
name|getAnnotation
parameter_list|(
name|Method
name|method
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|annotationClass
parameter_list|)
block|{
return|return
name|method
operator|.
name|getAnnotation
argument_list|(
name|annotationClass
argument_list|)
return|;
block|}
block|}
end_class

end_unit

