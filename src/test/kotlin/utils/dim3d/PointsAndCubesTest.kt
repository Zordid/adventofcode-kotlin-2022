package utils.dim3d

import CubeFace
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import opposite

class PointsAndCubesTest:StringSpec({

    "rotation around Z axis" {
        unitVecX.rotateZ(1) shouldBe unitVecY
        unitVecX.rotateZ(2) shouldBe -unitVecX
        unitVecX.rotateZ(3) shouldBe -unitVecY
        unitVecX.rotateZ(4) shouldBe unitVecX
    }

    "rotation around Y axis" {
        unitVecZ.rotateY(1) shouldBe unitVecX
        unitVecZ.rotateY(2) shouldBe -unitVecZ
        unitVecZ.rotateY(3) shouldBe -unitVecX
        unitVecZ.rotateY(4) shouldBe unitVecZ
    }

    "rotation around X axis" {
        unitVecY.rotateX(1) shouldBe unitVecZ
        unitVecY.rotateX(2) shouldBe -unitVecY
        unitVecY.rotateX(3) shouldBe -unitVecZ
        unitVecY.rotateX(4) shouldBe unitVecY
    }

    "cubes" {
        // A unitX
        val a = CubeFace(unitVecZ, unitVecY)
        println(a.faceVector)

        val d = a.down()
        val e = d.down()
        (e  opposite  a).shouldBeTrue()

        val c = d.left()
        val b = c.left()
        (b opposite d).shouldBeTrue()

        val f = e.right()
        (f opposite c).shouldBeTrue()

    }

})