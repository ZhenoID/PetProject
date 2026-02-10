// ServiceTestSuite.java
package epam.finalProject;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        AuthorServiceImplTest.class,
        BasketServiceImplTest.class,
        BookServiceImplTest.class,
        GenreServiceImplTest.class,
        UserServiceImplTest.class
})
public class ServiceTestSuite {
}
