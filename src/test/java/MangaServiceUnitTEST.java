import com.User;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;

public class MangaServiceUnitTEST {

    @Test
    public void testGetMangasByTitle() throws IOException {
            User user = new User();
            user.setCount(41);
        assertEquals(1, user.getCount());
    }

}
