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
name|hcatalog
operator|.
name|cli
operator|.
name|SemanticAnalysis
package|;
end_package

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
name|ql
operator|.
name|parse
operator|.
name|ASTNode
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
name|AbstractSemanticAnalyzerHook
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
name|HiveSemanticAnalyzerHookContext
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
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_class
specifier|public
class|class
name|AddPartitionHook
extends|extends
name|AbstractSemanticAnalyzerHook
block|{
specifier|private
name|String
name|tblName
decl_stmt|,
name|inDriver
decl_stmt|,
name|outDriver
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ASTNode
name|preAnalyze
parameter_list|(
name|HiveSemanticAnalyzerHookContext
name|context
parameter_list|,
name|ASTNode
name|ast
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tblProps
decl_stmt|;
name|tblName
operator|=
name|ast
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
expr_stmt|;
try|try
block|{
name|tblProps
operator|=
name|context
operator|.
name|getHive
argument_list|()
operator|.
name|getTable
argument_list|(
name|tblName
argument_list|)
operator|.
name|getParameters
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|he
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|he
argument_list|)
throw|;
block|}
name|inDriver
operator|=
name|tblProps
operator|.
name|get
argument_list|(
name|HCatConstants
operator|.
name|HCAT_ISD_CLASS
argument_list|)
expr_stmt|;
name|outDriver
operator|=
name|tblProps
operator|.
name|get
argument_list|(
name|HCatConstants
operator|.
name|HCAT_OSD_CLASS
argument_list|)
expr_stmt|;
if|if
condition|(
name|inDriver
operator|==
literal|null
operator|||
name|outDriver
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Operation not supported. Partitions can be added only in a table created through HCatalog. "
operator|+
literal|"It seems table "
operator|+
name|tblName
operator|+
literal|" was not created through HCatalog."
argument_list|)
throw|;
block|}
return|return
name|ast
return|;
block|}
comment|//  @Override
comment|//  public void postAnalyze(HiveSemanticAnalyzerHookContext context,
comment|//      List<Task<? extends Serializable>> rootTasks) throws SemanticException {
comment|//
comment|//    try {
comment|//      Hive db = context.getHive();
comment|//      Table tbl = db.getTable(MetaStoreUtils.DEFAULT_DATABASE_NAME, tblName);
comment|//      for(Task<? extends Serializable> task : rootTasks){
comment|//        System.err.println("PArt spec: "+((DDLWork)task.getWork()).getAddPartitionDesc().getPartSpec());
comment|//        Partition part = db.getPartition(tbl,((DDLWork)task.getWork()).getAddPartitionDesc().getPartSpec(),false);
comment|//        Map<String,String> partParams = part.getParameters();
comment|//        if(partParams == null){
comment|//          System.err.println("Part map null ");
comment|//          partParams = new HashMap<String, String>();
comment|//        }
comment|//        partParams.put(InitializeInput.HOWL_ISD_CLASS, inDriver);
comment|//        partParams.put(InitializeInput.HOWL_OSD_CLASS, outDriver);
comment|//        part.getTPartition().setParameters(partParams);
comment|//        db.alterPartition(tblName, part);
comment|//      }
comment|//    } catch (HiveException he) {
comment|//      throw new SemanticException(he);
comment|//    } catch (InvalidOperationException e) {
comment|//      throw new SemanticException(e);
comment|//    }
comment|//  }
block|}
end_class

end_unit

