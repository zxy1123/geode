package org.apache.geode.e2e;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;

public class StoryRunner extends JUnitStories {

  // Here we specify the configuration, starting from default MostUsefulConfiguration, and changing only what is need ed
  @Override
  public Configuration configuration() {
    return new MostUsefulConfiguration()
      // where to find the stories
      .useStoryLoader(new LoadFromClasspath(this.getClass()))
      // CONSOLE and TXT reporting
      .useStoryReporterBuilder(new StoryReporterBuilder()
        .withDefaultFormats()
        .withFormats(Format.ANSI_CONSOLE, Format.TXT)
        .withFailureTrace(true)
      );
  }

  protected String storyGlob() {
    return "**/*.story";
  }

  // Here we specify the steps classes
  @Override
  public InjectableStepsFactory stepsFactory() {
    // varargs, can have more that one steps classes
    return new InstanceStepsFactory(configuration(), new GetPutSteps());
  }

  @Override
  protected List<String> storyPaths() {
    String codeLocation = codeLocationFromClass(this.getClass()).getFile() + "../../resources/test";
    List<String> stories = new ArrayList<>();
    stories.add(storyGlob());
    return new StoryFinder().findPaths(codeLocation, stories, Collections.EMPTY_LIST);
  }
}
