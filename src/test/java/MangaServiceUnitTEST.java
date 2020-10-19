import com.User;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class MangaServiceUnitTEST {

    @Test
    public void testGetMangasByTitle() throws IOException {
        User user = new User();
        user.setCount(1);
        assertEquals(1, user.getCount());
    }

}
