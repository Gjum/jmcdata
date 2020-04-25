import com.github.gjum.minecraft.jmcdata.MinecraftData
import com.github.gjum.minecraft.jmcdata.preFlatteningVariantId
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test

class ResourcesTest {
	@Test
	fun `load resources 1_12_2`() {
		val mcd = MinecraftData("1.12.2")
		Assert.assertNotNull(mcd.items["stone"])
		Assert.assertEquals("Stone", mcd.items["stone"]!!.displayName)
		Assert.assertEquals(64, mcd.items["stone"]!!.maxStackSize)

		Assert.assertNotNull(mcd.blocks["dirt"])
		Assert.assertEquals("Coarse Dirt", mcd.blocks["dirt"]!!.variants[1].displayName)

		Assert.assertNotNull(mcd.blocks[preFlatteningVariantId(3, 1)])
		Assert.assertEquals("Coarse Dirt", mcd.blocks[preFlatteningVariantId(3, 1)]!!.displayName)
	}

	@Test
	@Ignore // XXX re-enable once 1.14 blocks+windows data become available
	fun `load resources 1_14_4`() {
		val mcd = MinecraftData("1.14.4")
		Assert.assertNotNull(mcd.items["stone"])
		Assert.assertEquals("Stone", mcd.items["stone"]!!.displayName)
		Assert.assertEquals(64, mcd.items["stone"]!!.maxStackSize)
		Assert.assertNotNull(mcd.blocks["stone_slab"])
		Assert.assertEquals("Stone Slab[type=BOTTOM, waterlogged=FALSE]", mcd.blocks["stone_slab"]!!.variants[1].displayName)
	}
}
