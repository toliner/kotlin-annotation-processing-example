package toliner.example.annotationprocess

import example.generated.message
import junit.framework.Assert.assertEquals
import org.junit.Test

class AnnotationTest {
    @Test
    fun testExample() {
        assertEquals("Hello, World!", message)
    }
}