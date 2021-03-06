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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|udf
operator|.
name|xml
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Reader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringReader
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|namespace
operator|.
name|QName
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|parsers
operator|.
name|DocumentBuilder
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|parsers
operator|.
name|DocumentBuilderFactory
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|parsers
operator|.
name|ParserConfigurationException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|xpath
operator|.
name|XPath
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|xpath
operator|.
name|XPathConstants
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|xpath
operator|.
name|XPathExpression
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|xpath
operator|.
name|XPathExpressionException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|xpath
operator|.
name|XPathFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|w3c
operator|.
name|dom
operator|.
name|Node
import|;
end_import

begin_import
import|import
name|org
operator|.
name|w3c
operator|.
name|dom
operator|.
name|NodeList
import|;
end_import

begin_import
import|import
name|org
operator|.
name|xml
operator|.
name|sax
operator|.
name|InputSource
import|;
end_import

begin_comment
comment|/**  * Utility class for all XPath UDFs. Each UDF instance should keep an instance  * of this class.  */
end_comment

begin_class
specifier|public
class|class
name|UDFXPathUtil
block|{
specifier|public
specifier|static
specifier|final
name|String
name|SAX_FEATURE_PREFIX
init|=
literal|"http://xml.org/sax/features/"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXTERNAL_GENERAL_ENTITIES_FEATURE
init|=
literal|"external-general-entities"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXTERNAL_PARAMETER_ENTITIES_FEATURE
init|=
literal|"external-parameter-entities"
decl_stmt|;
specifier|private
name|DocumentBuilderFactory
name|dbf
init|=
name|DocumentBuilderFactory
operator|.
name|newInstance
argument_list|()
decl_stmt|;
specifier|private
name|DocumentBuilder
name|builder
init|=
literal|null
decl_stmt|;
specifier|private
name|XPath
name|xpath
init|=
name|XPathFactory
operator|.
name|newInstance
argument_list|()
operator|.
name|newXPath
argument_list|()
decl_stmt|;
specifier|private
name|ReusableStringReader
name|reader
init|=
operator|new
name|ReusableStringReader
argument_list|()
decl_stmt|;
specifier|private
name|InputSource
name|inputSource
init|=
operator|new
name|InputSource
argument_list|(
name|reader
argument_list|)
decl_stmt|;
specifier|private
name|XPathExpression
name|expression
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|oldPath
init|=
literal|null
decl_stmt|;
specifier|public
name|Object
name|eval
parameter_list|(
name|String
name|xml
parameter_list|,
name|String
name|path
parameter_list|,
name|QName
name|qname
parameter_list|)
block|{
if|if
condition|(
name|xml
operator|==
literal|null
operator|||
name|path
operator|==
literal|null
operator|||
name|qname
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|xml
operator|.
name|length
argument_list|()
operator|==
literal|0
operator|||
name|path
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
operator|!
name|path
operator|.
name|equals
argument_list|(
name|oldPath
argument_list|)
condition|)
block|{
try|try
block|{
name|expression
operator|=
name|xpath
operator|.
name|compile
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|XPathExpressionException
name|e
parameter_list|)
block|{
name|expression
operator|=
literal|null
expr_stmt|;
block|}
name|oldPath
operator|=
name|path
expr_stmt|;
block|}
if|if
condition|(
name|expression
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|builder
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|initializeDocumentBuilderFactory
argument_list|()
expr_stmt|;
name|builder
operator|=
name|dbf
operator|.
name|newDocumentBuilder
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParserConfigurationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Error instantiating DocumentBuilder, cannot build xml parser"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
name|reader
operator|.
name|set
argument_list|(
name|xml
argument_list|)
expr_stmt|;
try|try
block|{
return|return
name|expression
operator|.
name|evaluate
argument_list|(
name|builder
operator|.
name|parse
argument_list|(
name|inputSource
argument_list|)
argument_list|,
name|qname
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|XPathExpressionException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Invalid expression '"
operator|+
name|oldPath
operator|+
literal|"'"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Error loading expression '"
operator|+
name|oldPath
operator|+
literal|"'"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|initializeDocumentBuilderFactory
parameter_list|()
throws|throws
name|ParserConfigurationException
block|{
name|dbf
operator|.
name|setFeature
argument_list|(
name|SAX_FEATURE_PREFIX
operator|+
name|EXTERNAL_GENERAL_ENTITIES_FEATURE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|dbf
operator|.
name|setFeature
argument_list|(
name|SAX_FEATURE_PREFIX
operator|+
name|EXTERNAL_PARAMETER_ENTITIES_FEATURE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Boolean
name|evalBoolean
parameter_list|(
name|String
name|xml
parameter_list|,
name|String
name|path
parameter_list|)
block|{
return|return
operator|(
name|Boolean
operator|)
name|eval
argument_list|(
name|xml
argument_list|,
name|path
argument_list|,
name|XPathConstants
operator|.
name|BOOLEAN
argument_list|)
return|;
block|}
specifier|public
name|String
name|evalString
parameter_list|(
name|String
name|xml
parameter_list|,
name|String
name|path
parameter_list|)
block|{
return|return
operator|(
name|String
operator|)
name|eval
argument_list|(
name|xml
argument_list|,
name|path
argument_list|,
name|XPathConstants
operator|.
name|STRING
argument_list|)
return|;
block|}
specifier|public
name|Double
name|evalNumber
parameter_list|(
name|String
name|xml
parameter_list|,
name|String
name|path
parameter_list|)
block|{
return|return
operator|(
name|Double
operator|)
name|eval
argument_list|(
name|xml
argument_list|,
name|path
argument_list|,
name|XPathConstants
operator|.
name|NUMBER
argument_list|)
return|;
block|}
specifier|public
name|Node
name|evalNode
parameter_list|(
name|String
name|xml
parameter_list|,
name|String
name|path
parameter_list|)
block|{
return|return
operator|(
name|Node
operator|)
name|eval
argument_list|(
name|xml
argument_list|,
name|path
argument_list|,
name|XPathConstants
operator|.
name|NODE
argument_list|)
return|;
block|}
specifier|public
name|NodeList
name|evalNodeList
parameter_list|(
name|String
name|xml
parameter_list|,
name|String
name|path
parameter_list|)
block|{
return|return
operator|(
name|NodeList
operator|)
name|eval
argument_list|(
name|xml
argument_list|,
name|path
argument_list|,
name|XPathConstants
operator|.
name|NODESET
argument_list|)
return|;
block|}
comment|/**    * Reusable, non-threadsafe version of {@link StringReader}.    */
specifier|public
specifier|static
class|class
name|ReusableStringReader
extends|extends
name|Reader
block|{
specifier|private
name|String
name|str
init|=
literal|null
decl_stmt|;
specifier|private
name|int
name|length
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|int
name|next
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|mark
init|=
literal|0
decl_stmt|;
specifier|public
name|ReusableStringReader
parameter_list|()
block|{     }
specifier|public
name|void
name|set
parameter_list|(
name|String
name|s
parameter_list|)
block|{
name|this
operator|.
name|str
operator|=
name|s
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|s
operator|.
name|length
argument_list|()
expr_stmt|;
name|this
operator|.
name|mark
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|next
operator|=
literal|0
expr_stmt|;
block|}
comment|/** Check to make sure that the stream has not been closed */
specifier|private
name|void
name|ensureOpen
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|str
operator|==
literal|null
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Stream closed"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|()
throws|throws
name|IOException
block|{
name|ensureOpen
argument_list|()
expr_stmt|;
if|if
condition|(
name|next
operator|>=
name|length
condition|)
return|return
operator|-
literal|1
return|;
return|return
name|str
operator|.
name|charAt
argument_list|(
name|next
operator|++
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|(
name|char
name|cbuf
index|[]
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
name|ensureOpen
argument_list|()
expr_stmt|;
if|if
condition|(
operator|(
name|off
operator|<
literal|0
operator|)
operator|||
operator|(
name|off
operator|>
name|cbuf
operator|.
name|length
operator|)
operator|||
operator|(
name|len
operator|<
literal|0
operator|)
operator|||
operator|(
operator|(
name|off
operator|+
name|len
operator|)
operator|>
name|cbuf
operator|.
name|length
operator|)
operator|||
operator|(
operator|(
name|off
operator|+
name|len
operator|)
operator|<
literal|0
operator|)
condition|)
block|{
throw|throw
operator|new
name|IndexOutOfBoundsException
argument_list|()
throw|;
block|}
elseif|else
if|if
condition|(
name|len
operator|==
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
if|if
condition|(
name|next
operator|>=
name|length
condition|)
return|return
operator|-
literal|1
return|;
name|int
name|n
init|=
name|Math
operator|.
name|min
argument_list|(
name|length
operator|-
name|next
argument_list|,
name|len
argument_list|)
decl_stmt|;
name|str
operator|.
name|getChars
argument_list|(
name|next
argument_list|,
name|next
operator|+
name|n
argument_list|,
name|cbuf
argument_list|,
name|off
argument_list|)
expr_stmt|;
name|next
operator|+=
name|n
expr_stmt|;
return|return
name|n
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|skip
parameter_list|(
name|long
name|ns
parameter_list|)
throws|throws
name|IOException
block|{
name|ensureOpen
argument_list|()
expr_stmt|;
if|if
condition|(
name|next
operator|>=
name|length
condition|)
return|return
literal|0
return|;
comment|// Bound skip by beginning and end of the source
name|long
name|n
init|=
name|Math
operator|.
name|min
argument_list|(
name|length
operator|-
name|next
argument_list|,
name|ns
argument_list|)
decl_stmt|;
name|n
operator|=
name|Math
operator|.
name|max
argument_list|(
operator|-
name|next
argument_list|,
name|n
argument_list|)
expr_stmt|;
name|next
operator|+=
name|n
expr_stmt|;
return|return
name|n
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|ready
parameter_list|()
throws|throws
name|IOException
block|{
name|ensureOpen
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|markSupported
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|mark
parameter_list|(
name|int
name|readAheadLimit
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|readAheadLimit
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Read-ahead limit< 0"
argument_list|)
throw|;
block|}
name|ensureOpen
argument_list|()
expr_stmt|;
name|mark
operator|=
name|next
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
name|ensureOpen
argument_list|()
expr_stmt|;
name|next
operator|=
name|mark
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|str
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

