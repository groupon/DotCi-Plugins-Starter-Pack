package com.groupon.jenkins.dotci.plugins;

import com.google.common.collect.ImmutableList;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  CoberturaPluginAdapterTest.getPluginInputFiles.class
})
public class CoberturaPluginAdapterTest {
  public static class getPluginInputFiles {
    @Test
    public void should_return_file_with_default() {
      CoberturaPluginAdapter subject = new CoberturaPluginAdapter();
      assertEquals(subject.getPluginInputFiles(), "target/site/cobertura/coverage.xml");
    }

    @Test
    public void should_return_files_with_string() {
      CoberturaPluginAdapter subject = new CoberturaPluginAdapter();
      subject.setOptions("file1,file2,file3");
      assertEquals(subject.getPluginInputFiles(), "file1,file2,file3");
    }

    @Test
    public void should_return_files_with_list() {
      CoberturaPluginAdapter subject = new CoberturaPluginAdapter();
      subject.setOptions(ImmutableList.of("file1", "file2", "file3"));
      assertEquals(subject.getPluginInputFiles(), "file1,file2,file3");
    }
  }
}

