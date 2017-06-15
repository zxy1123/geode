package org.apache.geode.protocol.operations.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.geode.protocol.operations.OperationHandler;
import org.apache.geode.protocol.operations.registry.exception.OperationHandlerAlreadyRegisteredException;
import org.apache.geode.protocol.operations.registry.exception.OperationHandlerNotRegisteredException;
import org.apache.geode.serialization.SerializationService;
import org.apache.geode.test.junit.categories.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class OperationsHandlerRegistryJUnitTest {
  private OperationsHandlerRegistry operationsHandlerRegistry;

  @Before
  public void setup() {
    operationsHandlerRegistry = new OperationsHandlerRegistry();
  }

  @Test
  public void testAddOperationsHandlerForOperationType()
      throws OperationHandlerAlreadyRegisteredException {
    operationsHandlerRegistry.registerOperationHandlerForOperationId(5,
        new DummyOperationHandler());
    assertEquals(1, operationsHandlerRegistry.getRegisteredOperationHandlersCount());
  }

  @Test
  public void testAddingDuplicateOperationsHandlerForOperationType_ThrowsException()
      throws OperationHandlerAlreadyRegisteredException, OperationHandlerNotRegisteredException {
    DummyOperationHandler expectedOperationHandler = new DummyOperationHandler();
    operationsHandlerRegistry.registerOperationHandlerForOperationId(5, expectedOperationHandler);
    assertEquals(1, operationsHandlerRegistry.getRegisteredOperationHandlersCount());
    boolean exceptionCaught = false;
    try {
      operationsHandlerRegistry.registerOperationHandlerForOperationId(5,
          new DummyOperationHandler());
    } catch (OperationHandlerAlreadyRegisteredException e) {
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
    assertEquals(1, operationsHandlerRegistry.getRegisteredOperationHandlersCount());
    assertSame(expectedOperationHandler,
        operationsHandlerRegistry.getOperationHandlerForOperationId(5));
  }

  @Test
  public void testGetOperationsHandlerForOperationType()
      throws OperationHandlerAlreadyRegisteredException, OperationHandlerNotRegisteredException {
    DummyOperationHandler expectedOperationHandler = new DummyOperationHandler();
    operationsHandlerRegistry.registerOperationHandlerForOperationId(5, expectedOperationHandler);
    OperationHandler operationHandler =
        operationsHandlerRegistry.getOperationHandlerForOperationId(5);
    assertSame(expectedOperationHandler, operationHandler);
  }

  @Test
  public void testGetOperationsHandlerForMissingOperationType_ThrowsException() {
    boolean exceptionCaught = false;
    try {
      operationsHandlerRegistry.getOperationHandlerForOperationId(5);
    } catch (OperationHandlerNotRegisteredException e) {
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
  }

  private class DummyOperationHandler implements OperationHandler {

    @Override
    public Object process(SerializationService serializationService, Object request) {
      return null;
    }
  }


}
