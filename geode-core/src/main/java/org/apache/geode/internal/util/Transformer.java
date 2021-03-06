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
package org.apache.geode.internal.util;

/**
 * Represents a data transform between two types.
 *
 * @param <T1> The data type to be transformed from.
 * @param <T2> The data type to be transformed to.
 */
public interface Transformer<T1, T2> {
  /**
   * Transforms one data type into another.
   *
   * @param t the data to be transferred from.
   * @return the transformed data.
   */
  public T2 transform(T1 t);
}
