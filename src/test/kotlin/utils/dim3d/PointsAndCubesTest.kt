package utils.dim3d

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PointsAndCubesTest:StringSpec({

    "rotation around Z axis" {
        unitVecX.rotateZ(1) shouldBe unitVecY
        unitVecX.rotateZ(2) shouldBe -unitVecX
        unitVecX.rotateZ(3) shouldBe -unitVecY
        unitVecX.rotateZ(4) shouldBe unitVecX
    }

    "rotation around Y axis" {
        unitVecX.rotateY(1) shouldBe -unitVecZ
        unitVecX.rotateY(2) shouldBe -unitVecX
        unitVecX.rotateY(3) shouldBe unitVecZ
        unitVecX.rotateY(4) shouldBe unitVecX
    }

    "rotation around X axis" {
        unitVecZ.rotateY(1) shouldBe unitVecY
       // unitVecX.rotateY(2) shouldBe -unitVecX
       // unitVecX.rotateY(3) shouldBe unitVecZ
       // unitVecX.rotateY(4) shouldBe unitVecX
    }

})