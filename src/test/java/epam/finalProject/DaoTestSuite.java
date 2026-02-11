// DaoTestSuite.java
package epam.finalProject;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        AuthorDaoImplTest.class,
        BasketDaoImplTest.class
})
public class DaoTestSuite {
}
