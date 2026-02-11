// ServiceTestSuite.java
package epam.finalProject;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        BookServiceImplTest.class
})
public class ServiceTestSuite {
}
