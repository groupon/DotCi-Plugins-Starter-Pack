package com.groupon.jenkins.dotci.plugins;

import com.google.common.collect.ImmutableList;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  JunitPluginAdapterTest.getPluginInputFiles.class
})
public class JunitPluginAdapterTest {
  public static class getPluginInputFiles {
    @Test
    public void should_returnfiles_with_string() {
      JunitPluginAdapter subject = new JunitPluginAdapter();
      assertEquals(subject.getPluginInputFiles(), "**/surefire-reports/*.xml");
    }

    @Test
    public void should_return_files_with_string() {
      JunitPluginAdapter subject = new JunitPluginAdapter();
      subject.setOptions("file1,file2,file3");
      assertEquals(subject.getPluginInputFiles(), "file1,file2,file3");
    }

    @Test
    public void should_return_files_with_list() {
      JunitPluginAdapter subject = new JunitPluginAdapter();
      subject.setOptions(ImmutableList.of("file1", "file2", "file3"));
      assertEquals(subject.getPluginInputFiles(), "file1,file2,file3");
    }
  }
}

