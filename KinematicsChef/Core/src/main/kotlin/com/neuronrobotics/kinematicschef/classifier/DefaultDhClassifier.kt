/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef.classifier

import arrow.core.Either
import com.neuronrobotics.kinematicschef.dhparam.DhParam
import com.neuronrobotics.kinematicschef.dhparam.SphericalWrist
import com.neuronrobotics.kinematicschef.util.getPointMatrix
import com.neuronrobotics.kinematicschef.util.getTranslation
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder
import org.ejml.simple.SimpleMatrix

internal class DefaultDhClassifier
internal constructor() : DhClassifier {

    override fun deriveEulerAngles(wrist: SphericalWrist) =
        deriveEulerAngles(wrist.params[0], wrist.params[1], wrist.params[2])

    /**
     * @param wrist The wrist.
     * @param target The target tip transform.
     * @param tipTransform The tip transform from the DhParams
     */
    fun deriveEulerAngles(
        wrist: SphericalWrist,
        target: SimpleMatrix,
        tipTransform: SimpleMatrix
    ): Either<ClassifierError, RotationOrder> {
        val center = wristCenter(target, wrist)
        val centerTransformed = center.mult(tipTransform.invert())
        val centerPosition = centerTransformed.getTranslation()
        return if (centerPosition[1] == 0.0 && centerPosition[2] == 0.0) {
            // Wrist lies on the x-axis, therefore its dh params are valid
            deriveEulerAngles(wrist)
        } else {
            Either.left(
                ClassifierError(
                    """
                    The wrist does not have valid DH Parameters. Transformed center position:
                    $centerPosition
                    """.trimIndent()
                )
            )
        }
    }

    private fun deriveEulerAngles(
        link1: DhParam,
        link2: DhParam,
        link3: DhParam
    ): Either<ClassifierError, RotationOrder> {
        return when {
            link1.alpha == 0.0 ->
                handleThreeLinkZeroAlpha(link2, link3, link1)
            link1.alpha == -90.0 ->
                handleThreeLinkMinusNinetyAlpha(link2, link3, link1)
            else ->
                Either.left(failDerivation(link1, link2, link3))
        }
    }

    private fun deriveEulerAngles(
        link1: DhParam,
        link2: DhParam,
        link3: DhParam,
        link4: DhParam
    ): Either<ClassifierError, RotationOrder> {
        return when {
            link1.alpha == 0.0 ->
                handleFourLinkZeroAlpha(link2, link3, link4, link1)
            link1.alpha == -90.0 ->
                handleFourLinkMinusNinetyAlpha(link2, link3, link4, link1)
            else ->
                Either.left(failDerivation(link1, link2, link3, link4))
        }
    }

    private fun handleThreeLinkZeroAlpha(
        link2: DhParam,
        link3: DhParam,
        link1: DhParam
    ): Either<ClassifierError, RotationOrder> {
        return if (
            link2.alpha == 90.0 &&
            link3.alpha == -90.0 &&
            link1.theta - link3.theta == 180.0
        ) {
            Either.right(RotationOrder.ZXZ)
        } else if (
            link2.alpha == -90.0 &&
            link3.alpha == 90.0
        ) {
            Either.right(RotationOrder.ZYZ)
        } else {
            Either.left(failDerivation(link1, link2, link3))
        }
    }

    @SuppressWarnings("ComplexCondition")
    private fun handleThreeLinkMinusNinetyAlpha(
        link2: DhParam,
        link3: DhParam,
        link1: DhParam
    ): Either<ClassifierError, RotationOrder> {
        return if (
            link2.alpha == 90.0 &&
            link3.alpha == -90.0 &&
            link1.theta - link3.theta == 180.0 &&
            link2.theta - link3.theta == 180.0
        ) {
            Either.right(RotationOrder.YXZ)
        } else {
            Either.left(failDerivation(link1, link2, link3))
        }
    }

    @SuppressWarnings("ComplexCondition")
    private fun handleFourLinkZeroAlpha(
        link2: DhParam,
        link3: DhParam,
        link4: DhParam,
        link1: DhParam
    ): Either<ClassifierError, RotationOrder> {
        return if (
            link2.alpha == 90.0 &&
            link3.alpha == -90.0 &&
            link4.alpha == 90.0 &&
            link1.theta - link2.theta == 180.0 &&
            link1.theta - link3.theta == 180.0 &&
            link4.theta == 0.0
        ) {
            Either.right(RotationOrder.ZXY)
        } else if (
            link2.alpha == -90.0 &&
            link3.alpha == 90.0 &&
            link4.alpha == -90.0 &&
            link4.theta == -90.0
        ) {
            Either.right(RotationOrder.ZYX)
        } else {
            Either.left(failDerivation(link1, link2, link3, link4))
        }
    }

    @SuppressWarnings("ComplexCondition")
    private fun handleFourLinkMinusNinetyAlpha(
        link2: DhParam,
        link3: DhParam,
        link4: DhParam,
        link1: DhParam
    ): Either<ClassifierError, RotationOrder> {
        return if (
            link2.alpha == 90.0 &&
            link3.alpha == -90.0 &&
            link4.alpha == 90.0 &&
            link1.theta - link3.theta == 180.0
        ) {
            Either.right(RotationOrder.YXY)
        } else if (
            link2.alpha == 90.0 &&
            link3.alpha == 90.0 &&
            link4.alpha == -90.0 &&
            link4.theta == -90.0
        ) {
            Either.right(RotationOrder.YZX)
        } else {
            Either.left(failDerivation(link1, link2, link3, link4))
        }
    }

    private fun failDerivation(vararg params: DhParam): ClassifierError {
        return ClassifierError(
            """
            The wrist does not have Euler angles:
            ${params.joinToString(separator = "\n")}
            """.trimIndent()
        )
    }

    /**
     * Calculate the position of a spherical wrist's center given a target point and a desired wrist orientation.
     *
     * @param target The target position of the end effector
     * @param wrist The DH parameter chain for the spherical wrist
     */
    private fun wristCenter(
        target: SimpleMatrix,
        wrist: SphericalWrist
    ): SimpleMatrix {
        // TODO: Make sure to not duplicate this with Jason's when the branches merge
        val boneLength = wrist.params[1].r + wrist.params[2].d

        val x = target[0, 3] - boneLength * target[0, 2]
        val y = target[1, 3] - boneLength * target[1, 2]
        val z = target[2, 3] - boneLength * target[2, 2]

        return getPointMatrix(x, y, z)
    }
}
