import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProjectionTest {
    @Test
    void testOverlaps() {
        Projection p1 = new Projection(0, 10);
        Projection p2 = new Projection(5, 15);
        Projection p3 = new Projection(11, 20);

        assertTrue(p1.overlaps(p2), "Projections 0-10 and 5-15 must be overlapped");
        assertFalse(p1.overlaps(p3), "Projections 0-10 and 11-20 must not be overlapped");
    }

    @Test
    void testGetOverlap() {
        Projection p1 = new Projection(0, 10);
        Projection p2 = new Projection(7, 15);
        assertEquals(3.0, p1.getOverlap(p2), 1e-9);
    }

    @Test
    void testSpecialCases() {
        // Дотик (Touching) - зазвичай вважається колізією або межею
        Projection p1 = new Projection(0, 10);
        Projection p2 = new Projection(10, 20);
        assertTrue(p1.overlaps(p2), "Touching projections should overlap");

        // Одна всередині іншої (Containment)
        Projection pLarge = new Projection(0, 100);
        Projection pSmall = new Projection(20, 50);
        assertTrue(pLarge.overlaps(pSmall));
        assertEquals(30.0, pLarge.getOverlap(pSmall), 1e-9);

        // Від'ємні координати
        Projection pNeg1 = new Projection(-50, -10);
        Projection pNeg2 = new Projection(-20, 0);
        assertTrue(pNeg1.overlaps(pNeg2));
    }
}