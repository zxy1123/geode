/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.management.internal.cli;

import static org.apache.geode.distributed.ConfigurationProperties.USER_COMMAND_PACKAGES;

import org.apache.geode.distributed.ConfigurationProperties;
import org.apache.geode.distributed.internal.DistributionConfig;
import org.apache.geode.internal.ClassPathLoader;
import org.apache.geode.management.internal.cli.util.ClasspathScanLoadHelper;
import org.springframework.context.ApplicationContextAware;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.Converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * this only takes care of loading all available command markers and converters from the application
 * @since GemFire 7.0
 */
public class CommandManager {
  public static final String USER_CMD_PACKAGES_PROPERTY =
      DistributionConfig.GEMFIRE_PREFIX + USER_COMMAND_PACKAGES;
  public static final String USER_CMD_PACKAGES_ENV_VARIABLE = "GEMFIRE_USER_COMMAND_PACKAGES";
  private static final Object INSTANCE_LOCK = new Object();
  private static CommandManager INSTANCE = null;


  /**
   * List of converters which should be populated first before any command can be added
   */
  private final List<Converter<?>> converters = new ArrayList<Converter<?>>();

  private final List<CommandMarker> commandMarkers = new ArrayList<>();
  private Properties cacheProperties;
  private LogWrapper logWrapper;

  private CommandManager(final boolean loadDefaultCommands, final Properties cacheProperties)
      throws ClassNotFoundException, IOException {
    if (cacheProperties != null) {
      this.cacheProperties = cacheProperties;
    }
    logWrapper = LogWrapper.getInstance();
    if (loadDefaultCommands) {
      loadCommands();
    }
  }

  private static void raiseExceptionIfEmpty(Set<Class<?>> foundClasses, String errorFor)
      throws IllegalStateException {
    if (foundClasses == null || foundClasses.isEmpty()) {
      throw new IllegalStateException(
          "Required " + errorFor + " classes were not loaded. Check logs for errors.");
    }
  }

  public static CommandManager getInstance() throws ClassNotFoundException, IOException {
    return getInstance(true);
  }

  public static CommandManager getInstance(Properties cacheProperties)
      throws ClassNotFoundException, IOException {
    return getInstance(true, cacheProperties);
  }

  // For testing.
  public static void clearInstance() {
    synchronized (INSTANCE_LOCK) {
      INSTANCE = null;
    }
  }

  // This method exists for test code use only ...
  /* package */
  static CommandManager getInstance(boolean loadDefaultCommands)
      throws ClassNotFoundException, IOException {
    return getInstance(loadDefaultCommands, null);
  }

  private static CommandManager getInstance(boolean loadDefaultCommands, Properties cacheProperties)
      throws ClassNotFoundException, IOException {
    synchronized (INSTANCE_LOCK) {
      if (INSTANCE == null) {
        INSTANCE = new CommandManager(loadDefaultCommands, cacheProperties);
      }
      return INSTANCE;
    }
  }

  public static CommandManager getExisting() {
    // if (INSTANCE == null) {
    // throw new IllegalStateException("CommandManager doesn't exist.");
    // }
    return INSTANCE;
  }

  private void loadUserCommands() throws ClassNotFoundException, IOException {
    final Set<String> userCommandPackages = new HashSet<String>();

    // Find by packages specified by the system property
    if (System.getProperty(USER_CMD_PACKAGES_PROPERTY) != null) {
      StringTokenizer tokenizer =
          new StringTokenizer(System.getProperty(USER_CMD_PACKAGES_PROPERTY), ",");
      while (tokenizer.hasMoreTokens()) {
        userCommandPackages.add(tokenizer.nextToken());
      }
    }

    // Find by packages specified by the environment variable
    if (System.getenv().containsKey(USER_CMD_PACKAGES_ENV_VARIABLE)) {
      StringTokenizer tokenizer =
          new StringTokenizer(System.getenv().get(USER_CMD_PACKAGES_ENV_VARIABLE), ",");
      while (tokenizer.hasMoreTokens()) {
        userCommandPackages.add(tokenizer.nextToken());
      }
    }

    // Find by packages specified in the distribution config
    if (this.cacheProperties != null) {
      String cacheUserCmdPackages =
          this.cacheProperties.getProperty(ConfigurationProperties.USER_COMMAND_PACKAGES);
      if (cacheUserCmdPackages != null && !cacheUserCmdPackages.isEmpty()) {
        StringTokenizer tokenizer = new StringTokenizer(cacheUserCmdPackages, ",");
        while (tokenizer.hasMoreTokens()) {
          userCommandPackages.add(tokenizer.nextToken());
        }
      }
    }

    // Load commands found in all of the packages
    for (String userCommandPackage : userCommandPackages) {
      try {
        Set<Class<?>> foundClasses =
            ClasspathScanLoadHelper.loadAndGet(userCommandPackage, CommandMarker.class, true);
        for (Class<?> klass : foundClasses) {
          try {
            add((CommandMarker) klass.newInstance());
          } catch (Exception e) {
            logWrapper.warning("Could not load User Commands from: " + klass + " due to "
                + e.getLocalizedMessage()); // continue
          }
        }
        raiseExceptionIfEmpty(foundClasses, "User Command");
      } catch (ClassNotFoundException e) {
        logWrapper.warning("Could not load User Commands due to " + e.getLocalizedMessage());
        throw e;
      } catch (IOException e) {
        logWrapper.warning("Could not load User Commands due to " + e.getLocalizedMessage());
        throw e;
      } catch (IllegalStateException e) {
        logWrapper.warning(e.getMessage(), e);
        throw e;
      }
    }
  }

  /**
   * Loads commands via {@link ServiceLoader} from {@link ClassPathLoader}.
   *
   * @since GemFire 8.1
   */
  private void loadPluginCommands() {
    final Iterator<CommandMarker> iterator = ServiceLoader
        .load(CommandMarker.class, ClassPathLoader.getLatest().asClassLoader()).iterator();
    while (iterator.hasNext()) {
      try {
        final CommandMarker commandMarker = iterator.next();
        try {
          add(commandMarker);
        } catch (Exception e) {
          logWrapper.warning("Could not load Command from: " + commandMarker.getClass() + " due to "
              + e.getLocalizedMessage(), e); // continue
        }
      } catch (ServiceConfigurationError e) {
        logWrapper.severe("Could not load Command: " + e.getLocalizedMessage(), e); // continue
      }
    }
  }

  private void loadCommands() throws ClassNotFoundException, IOException {
    loadUserCommands();

    loadPluginCommands();

    // CommandMarkers
    Set<Class<?>> foundClasses = null;
    try {
      // geode's commands
      foundClasses = ClasspathScanLoadHelper.loadAndGet(
          "org.apache.geode.management.internal.cli.commands", CommandMarker.class, true);
      for (Class<?> klass : foundClasses) {
        try {
          add((CommandMarker) klass.newInstance());
        } catch (Exception e) {
          logWrapper.warning(
              "Could not load Command from: " + klass + " due to " + e
                  .getLocalizedMessage()); // continue
        }
      }
      raiseExceptionIfEmpty(foundClasses, "Commands");

      // Spring shell's commands
      foundClasses = ClasspathScanLoadHelper.loadAndGet(
          "org.springframework.shell.commands", CommandMarker.class, true);
      for (Class<?> klass : foundClasses) {
        if (ApplicationContextAware.class.isAssignableFrom(klass)) {
          // skip the ApplicationContextAware commands. i.e. the HelpCommands
          continue;
        }
        try {
          add((CommandMarker) klass.newInstance());
        } catch (Exception e) {
          logWrapper.warning(
              "Could not load Command from: " + klass + " due to " + e.getLocalizedMessage()); // continue
        }
      }
      raiseExceptionIfEmpty(foundClasses, "Commands");
    } catch (ClassNotFoundException e) {
      logWrapper.warning("Could not load Commands due to " + e.getLocalizedMessage());
      throw e;
    } catch (IOException e) {
      logWrapper.warning("Could not load Commands due to " + e.getLocalizedMessage());
      throw e;
    } catch (IllegalStateException e) {
      logWrapper.warning(e.getMessage(), e);
      throw e;
    }

    // Converters
    try {
      foundClasses = ClasspathScanLoadHelper
          .loadAndGet("org.apache.geode.management.internal.cli.converters", Converter.class, true);
      for (Class<?> klass : foundClasses) {
        try {
          add((Converter<?>) klass.newInstance());
        } catch (Exception e) {
          logWrapper.warning(
              "Could not load Converter from: " + klass + " due to " + e.getLocalizedMessage()); // continue
        }
      }
      raiseExceptionIfEmpty(foundClasses, "Converters");

      // Spring shell's converters
      foundClasses = ClasspathScanLoadHelper.loadAndGet("org.springframework.shell.converters",
          Converter.class, true);
      for (Class<?> klass : foundClasses) {
        try {
          add((Converter<?>) klass.newInstance());
        } catch (Exception e) {
          logWrapper.warning(
              "Could not load Converter from: " + klass + " due to " + e
                  .getLocalizedMessage()); // continue
        }
      }
      raiseExceptionIfEmpty(foundClasses, "Basic Converters");
    } catch (ClassNotFoundException e) {
      logWrapper.warning("Could not load Converters due to " + e.getLocalizedMessage());
      throw e;
    } catch (IOException e) {
      logWrapper.warning("Could not load Converters due to " + e.getLocalizedMessage());
      throw e;
    } catch (IllegalStateException e) {
      logWrapper.warning(e.getMessage(), e);
      throw e;
    }
  }

  public List<Converter<?>> getConverters() {
    return converters;
  }

  public List<CommandMarker> getCommandMarkers(){
    return commandMarkers;
  }
  /**
   * Method to add new Converter
   * 
   * @param converter
   */
  public void add(Converter<?> converter) {
    converters.add(converter);
  }

  /**
   * Method to add new Commands to the parser
   * 
   * @param commandMarker
   */
  public void add(CommandMarker commandMarker) {
    commandMarkers.add(commandMarker);
  }
}
