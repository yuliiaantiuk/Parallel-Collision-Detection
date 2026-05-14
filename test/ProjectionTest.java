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
        assertTrue(p2.overlaps(p3), "Projections 5-15 and 11-20 must be overlapped");
    }

    @Test
    void testGetOverlap() {
        Projection p1 = new Projection(0, 10);
        Projection p2 = new Projection(7.3, 15);
        assertEquals(2.7, p1.getOverlap(p2), 1e-9);
    }

    @Test
    void testSpecialCases() {
        Projection p1 = new Projection(0, 10);
        Projection p2 = new Projection(10, 20);
        assertEquals(0.0, p1.getOverlap(p2), 1e-9, "The overlap should be 0.0. Touching projections should not overlap");

        Projection pLarge = new Projection(0, 100);
        Projection pSmall = new Projection(20, 50);
        assertTrue(pLarge.overlaps(pSmall));
        assertEquals(30.0, pLarge.getOverlap(pSmall), 1e-9,  "The overlap should be 30.0");

        Projection pNeg1 = new Projection(-50, -10);
        Projection pNeg2 = new Projection(-20, 0);
        assertEquals(10.0, pNeg1.getOverlap(pNeg2), 1e-9, "The overlap should be 10.0");
    }

    @Test
    void testPerfectOverlap() {
        Projection p1 = new Projection(10.03, 20.01);
        Projection p2 = new Projection(10.03, 20.01);
        assertTrue(p1.overlaps(p2));
        assertEquals(9.98, p1.getOverlap(p2), 1e-9);
    }

    @Test
    void testFloatingPointPrecision() {
        Projection p1 = new Projection(0, 1.0);
        Projection p2 = new Projection(1.0 + 1e-12, 2.0);
        assertFalse(p1.overlaps(p2), "Extremely close projections should not overlap");
    }
}