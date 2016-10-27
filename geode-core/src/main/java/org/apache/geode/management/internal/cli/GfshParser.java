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

import org.apache.commons.lang.StringUtils;
import org.springframework.shell.converters.ArrayConverter;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.Completion;
import org.springframework.shell.core.Converter;
import org.springframework.shell.core.Parser;
import org.springframework.shell.core.SimpleParser;
import org.springframework.shell.event.ParseResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of the {@link Parser} interface for GemFire SHell (gfsh) requirements.
 *
 * @since GemFire 7.0
 */
public class GfshParser extends SimpleParser {

  public static final String LINE_SEPARATOR = System.getProperty("line.separator");
  public static final String OPTION_VALUE_SPECIFIER = "=";
  public static final String VALUE_SEPARATOR = ",";
  public static final String ARGUMENT_SEPARATOR = "?";
  public static final String OPTION_SEPARATOR = " ";
  public static final String SHORT_OPTION_SPECIFIER = "-";
  public static final String LONG_OPTION_SPECIFIER = "--";
  public static final String COMMAND_DELIMITER = ";";
  public static final String CONTINUATION_CHARACTER = "\\";

  // Make use of LogWrapper
  private static final LogWrapper logWrapper = LogWrapper.getInstance();

  //pattern used to split the userinput with whitespaces except in qutoes (single or double)
  private static Pattern
      PATTERN =
      Pattern.compile("([^\\s\"]*)\"([^\"]*)\"|([^\\s']*)'([^']*)'|[^\\s]+");
  // private CliStringResourceBundle cliStringBundle;
  private CommandManager commandManager;
  /**
   * Used for warning messages
   */
  private Logger consoleLogger;

  public GfshParser(CommandManager commandManager) {
    this.commandManager = commandManager;

    if (CliUtil.isGfshVM()) {
      consoleLogger = Logger.getLogger(this.getClass().getCanonicalName());
    } else {
      consoleLogger = logWrapper.getLogger();
    }

    for (CommandMarker command : commandManager.getCommandMarkers()) {
      add(command);
    }

    List<Converter<?>> converters = commandManager.getConverters();
    for (Converter<?> converter : converters) {
      if (converter.getClass().isAssignableFrom(ArrayConverter.class)) {
        ArrayConverter arrayConverter = (ArrayConverter) converter;
        arrayConverter.setConverters(new HashSet<>(converters));
      }
      add(converter);
    }
  }

  public static String convertToSimpleParserInput(String userInput) {
    List<String> inputTokens = splitUserInput(userInput);
    return getSimpleParserInputFromTokens(inputTokens);
  }

  public static List<String> splitUserInput(String userInput) {
    // first split with whitespaces except in quotes
    List<String> splitWithWhiteSpaces = new ArrayList<>();
    Matcher m = PATTERN.matcher(userInput);
    while (m.find()) {
      splitWithWhiteSpaces.add(m.group());
    }

    List<String> furtherSplitWithEquals = new ArrayList<>();
    for (String token : splitWithWhiteSpaces) {
      // if this token has equal sign, split around the first occurrance of it
      int indexOfFirstEqual = token.indexOf('=');
      if (indexOfFirstEqual < 0) {
        furtherSplitWithEquals.add(token);
      } else {
        String left = token.substring(0, indexOfFirstEqual);
        String right = token.substring(indexOfFirstEqual + 1);
        if (left.length() > 0) {
          furtherSplitWithEquals.add(left);
        }
        if (right.length() > 0) {
          furtherSplitWithEquals.add(right);
        }
      }
    }
    return furtherSplitWithEquals;
  }

  private static String getSimpleParserInputFromTokens(List<String> tokens) {
    // make a copy of the input since we need to do add/remove
    List<String> inputTokens = new ArrayList<>();

    // get the --J arguments from the list of tokens
    int firstJIndex = -1;
    List<String> jArguments = new ArrayList<>();

    for (int i = 0; i < tokens.size(); i++) {
      String token = tokens.get(i);
      if ("--J".equals(token)) {
        if (firstJIndex < 1) {
          firstJIndex = i;
        }
        i++;

        if (i < tokens.size()) {
          String jArg = tokens.get(i);
          if (jArg.charAt(0) == '"' || jArg.charAt(0) == '\'') {
            jArg = jArg.substring(1, jArg.length() - 1);
          }
          if (jArg.length() > 0) {
            jArguments.add(jArg);
          }
        }
      } else {
        inputTokens.add(token);
      }
    }

    // concatenate the remaining tokens with space
    StringBuffer rawInput = new StringBuffer();
    // firstJIndex must be less than or equal to the length of the inputToken
    for (int i = 0; i <= inputTokens.size(); i++) {
      // stick the --J arguments in the orginal first --J position
      if (i == firstJIndex) {
        rawInput.append("--J ");
        if (jArguments.size() > 0) {
          rawInput.append("\"").append(StringUtils.join(jArguments, ",")).append("\" ");
        }
      }
      // then add the next inputToken
      if (i < inputTokens.size()) {
        rawInput.append(inputTokens.get(i)).append(" ");
      }
    }

    return rawInput.toString().trim();
  }

  @Override
  public ParseResult parse(String userInput) {
    String rawInput = convertToSimpleParserInput(userInput);

    // User SimpleParser to parse the input
    ParseResult result = super.parse(rawInput);

    if (result != null) {
      return new GfshParseResult(result.getMethod(), result.getInstance(), result.getArguments(),
          userInput);
    }

    return null;
  }

  @Override
  public int completeAdvanced(String userInput, int cursor, final List<Completion> candidates) {
    cursor = userInput.length();
    List<String> inputTokens = splitUserInput(userInput);
    String lastToken = inputTokens.get(inputTokens.size() - 1);

    // trying to get candidates using the converted input and the candidateBeginAt cursor position
    String buffer = getSimpleParserInputFromTokens(inputTokens);
    int candidateBeginAt = buffer.length() - lastToken.length();
    List<Completion> potentials = getCandidates(buffer, candidateBeginAt);
    if (potentials.size() == 0 && !lastToken.startsWith("--")) {
      // if last token is not an option, add "--" to it and retry
      candidateBeginAt = buffer.length() + 1;
      potentials = getCandidates(buffer + " --", candidateBeginAt);
    } else if (potentials.size() == 1 && potentials.get(0).getValue().equals(lastToken)) {
      // if the candidate is exactly the same as the last token, add a space to it and retry
      potentials = getCandidates(buffer + " ", candidateBeginAt);
    }

    // If we have a candidate, need to determine what's the returned cursor should be
    if (potentials.size() > 0) {
      // usually we want to begin the cursor at candidateBeginCursor, but since we consolidated
      // --J options into one, and added quotes around we need to consider the length difference
      // between userInput and the converted input
      cursor = candidateBeginAt + (userInput.trim().length() - buffer.length());
      // our cursor can't be further than whatever user has typed in
      if (cursor > userInput.length()) {
        cursor = userInput.length();
      }
    }

    candidates.addAll(potentials);
    return cursor;
  }

  /**
   * @param buffer use the buffer to find the completion candidates
   * @param candidateBeginAt strip the found candidates from this position
   *
   * Note the cursor maynot be the size the buffer
   */
  public List<Completion> getCandidates(String buffer, int candidateBeginAt) {
    List<Completion> candidates = new ArrayList<>();

    // always pass the buffer length as the cursor position for simplicity purpose
    super.completeAdvanced(buffer, buffer.length(), candidates);

    // stripp off the beginning part of the candidates from the cursor point
    // starting from the end, since we need to remove and add
    for (int i = candidates.size() - 1; i >= 0; i--) {
      Completion candidate = candidates.remove(i);
      // we only need the part after the current cursor
      String suggest = candidate.getValue().substring(candidateBeginAt);
      // this suggested value usually ends with a space because it's assuming it's using
      // "--option argment" format. trim the ending space so that we can easily add the "="sign
      if (suggest.endsWith(" ")) {
        suggest = suggest.substring(0, suggest.length() - 1);
      }
      candidates.add(new Completion(suggest));
    }
    return candidates;
  }

}
