import com.github.gjum.minecraft.jmcdata.math.euclideanDiv
import com.github.gjum.minecraft.jmcdata.math.euclideanMod
import org.junit.Assert
import org.junit.Test

class MathTest {
	@Test
	fun euclideanDiv() {
		Assert.assertEquals(2, 32.euclideanDiv(16))
		Assert.assertEquals(1, 16.euclideanDiv(16))
		Assert.assertEquals(0, 15.euclideanDiv(16))
		Assert.assertEquals(0, 0.euclideanDiv(16))
		Assert.assertEquals(-1, (-1).euclideanDiv(16))
		Assert.assertEquals(-1, (-15).euclideanDiv(16))
		Assert.assertEquals(-1, (-16).euclideanDiv(16))
		Assert.assertEquals(-2, (-17).euclideanDiv(16))
	}

	@Test
	fun `Int euclideanMod`() {
		Assert.assertEquals(0, 32.euclideanMod(16))
		Assert.assertEquals(0, 16.euclideanMod(16))
		Assert.assertEquals(15, 15.euclideanMod(16))
		Assert.assertEquals(0, 0.euclideanMod(16))
		Assert.assertEquals(15, (-1).euclideanMod(16))
		Assert.assertEquals(1, (-15).euclideanMod(16))
		Assert.assertEquals(0, (-16).euclideanMod(16))
		Assert.assertEquals(15, (-17).euclideanMod(16))
		Assert.assertEquals(0, (-32).euclideanMod(16))
	}

	@Test
	fun `Double euclideanMod`() {
		val epsilon = 0.000000001
		Assert.assertEquals(0.0, 32.0.euclideanMod(16.0), epsilon)
		Assert.assertEquals(0.0, 16.0.euclideanMod(16.0), epsilon)
		Assert.assertEquals(15.0, 15.0.euclideanMod(16.0), epsilon)
		Assert.assertEquals(0.0, 0.0.euclideanMod(16.0), epsilon)
		Assert.assertEquals(15.0, (-1.0).euclideanMod(16.0), epsilon)
		Assert.assertEquals(1.0, (-15.0).euclideanMod(16.0), epsilon)
		Assert.assertEquals(0.0, (-16.0).euclideanMod(16.0), epsilon)
		Assert.assertEquals(15.0, (-17.0).euclideanMod(16.0), epsilon)
		Assert.assertEquals(0.0, (-32.0).euclideanMod(16.0), epsilon)
	}
}
