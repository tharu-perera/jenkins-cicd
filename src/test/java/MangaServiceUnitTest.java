import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;

public class MangaServiceUnitTest {

    @Test
    public void testGetMangasByTitle() throws IOException {
        User user = new User();
        user.setCount(1);
        assertEquals(1, user.getCount());

    }

}
