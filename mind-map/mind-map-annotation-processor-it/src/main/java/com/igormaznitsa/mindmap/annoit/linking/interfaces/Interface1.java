package com.igormaznitsa.mindmap.annoit.linking.interfaces;

import com.igormaznitsa.mindmap.annotations.MmdTopic;

@MmdTopic
public interface Interface1 extends RootInterface {
  @MmdTopic(jumpTo = "$$$111")
  void method1();

  @MmdTopic(jumpTo = "RootInterface")
  void method2();
}
