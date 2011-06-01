package org.apache.ibatis.datasource.jndi;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.datasource.DataSourceException;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

public class JndiDataSourceFactoryTest extends BaseDataTest {

  private static final String TEST_INITIAL_CONTEXT_FACTORY = MockContextFactory.class.getName();
  private static final String TEST_INITIAL_CONTEXT = "/mypath/path/";
  private static final String TEST_DATA_SOURCE = "myDataSource";
  private UnpooledDataSource expectedDataSource;

  @Before
  public void setup() throws Exception {
    expectedDataSource = createUnpooledDataSource(BLOG_PROPERTIES);
  }

  @Test
  public void shouldRetrieveDataSourceFromJNDI() throws Exception {
    createJndiDataSource();
    JndiDataSourceFactory factory = new JndiDataSourceFactory();
    factory.setProperties(new Properties() {
      {
        setProperty(JndiDataSourceFactory.ENV_PREFIX + Context.INITIAL_CONTEXT_FACTORY, TEST_INITIAL_CONTEXT_FACTORY);
        setProperty(JndiDataSourceFactory.INITIAL_CONTEXT, TEST_INITIAL_CONTEXT);
        setProperty(JndiDataSourceFactory.DATA_SOURCE, TEST_DATA_SOURCE);
      }
    });
    DataSource actualDataSource = factory.getDataSource();
    assertEquals(expectedDataSource, actualDataSource);
  }

  private void createJndiDataSource() throws Exception {
    try {
      Hashtable env = new Hashtable();
      env.put(Context.INITIAL_CONTEXT_FACTORY, TEST_INITIAL_CONTEXT_FACTORY);

      MockContext ctx = new MockContext(false);
      ctx.bind(TEST_DATA_SOURCE, expectedDataSource);

      InitialContext initCtx = new InitialContext(env);
      initCtx.bind(TEST_INITIAL_CONTEXT, ctx);
    } catch (NamingException e) {
      throw new DataSourceException("There was an error configuring JndiDataSourceTransactionPool. Cause: " + e, e);
    }
  }

  public static class MockContextFactory implements InitialContextFactory {
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
      return new MockContext(false);
    }
  }

  public static class MockContext extends InitialContext {
    private static Map bindings = new HashMap();

    public MockContext(boolean lazy) throws NamingException {
      super(lazy);
    }

    public Object lookup(String name) throws NamingException {
      return bindings.get(name);
    }

    public void bind(String name, Object obj) throws NamingException {
      bindings.put(name, obj);
    }
  }


}