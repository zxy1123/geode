/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.gemstone.gemfire.modules.session.internal.filter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author jdeppe
 */
public interface Callback {
  void call(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
