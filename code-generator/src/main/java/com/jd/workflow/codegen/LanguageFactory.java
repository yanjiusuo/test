package com.jd.workflow.codegen;

import com.jd.workflow.codegen.language.JavaLanguageRender;
import com.jd.workflow.codegen.language.Language;
import com.jd.workflow.codegen.language.TypescriptLanguageRender;
import com.jd.workflow.soap.common.exception.BizException;

public class LanguageFactory {
      public static ILanguageCodeRender getLanguageRender(Language language,CodeGenerator codeGenerator){
          switch (language){
              case JAVA:
                  JavaLanguageRender javaLanguageRender = new JavaLanguageRender();
                  javaLanguageRender.setCodeGenerator(codeGenerator);
                  return javaLanguageRender;
              case TS:
                  TypescriptLanguageRender render = new TypescriptLanguageRender();
                  render.setCodeGenerator(codeGenerator);
                  return render;
              default:
                  throw new BizException("不支持的语言").param("language",language);
          }
      }
}
