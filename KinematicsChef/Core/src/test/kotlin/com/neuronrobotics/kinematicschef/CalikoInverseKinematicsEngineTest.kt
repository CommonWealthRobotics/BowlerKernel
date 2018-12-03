/*
 * Copyright 2018 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.kinematicschef

import com.neuronrobotics.sdk.addons.kinematics.DHLink
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.lang.Math.toDegrees
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.test.assertEquals

internal class CalikoInverseKinematicsEngineTest {

    private val engine = CalikoInverseKinematicsEngine()

    private val mockChain = TestUtil.makeMockChain()

    private fun xyPlane1dofArm(): Unit =
        mockChain.addLink(DHLink(10.0, 0.0, 0.0, 0.0))

    private fun xyPlane2dofArm() {
        mockChain.addLink(DHLink(0.0, 0.0, 10.0, -180.0))
        mockChain.addLink(DHLink(0.0, 0.0, 10.0, 0.0))
    }

    @Test
    fun `test 1 dof arm on x`() {
        xyPlane1dofArm()

        val target = 10
        val (angles, error) = engine.inverseKinematicsWithError(
            TransformNR().setX(target.toDouble()),
            listOf(0.0).toDoubleArray(),
            mockChain
        )

        assertAll(
            {
                assertEquals(1, angles.size, "Must have one joint angle")
            },
            {
                assertEquals(0, angles[0].roundToInt(), "Joint angle must be 0")
            },
            {
                assertEquals(0, error.roundToInt(), "Solve error should equal 0")
            }
        )
    }

    @Test
    fun `test 1 dof arm on y`() {
        xyPlane1dofArm()

        val target = 10
        val (angles, error) = engine.inverseKinematicsWithError(
            TransformNR().setY(target.toDouble()),
            listOf(0.0).toDoubleArray(),
            mockChain
        )

        assertAll(
            {
                assertEquals(1, angles.size, "Must have one joint angle")
            },
            {
                assertEquals(90, angles[0].roundToInt(), "Joint angle must be 90")
            },
            {
                assertEquals(0, error.roundToInt(), "Solve error should equal 0")
            }
        )
    }

    @Test
    fun `test 1 dof arm on z`() {
        xyPlane1dofArm()

        val target = 10
        val (angles, error) = engine.inverseKinematicsWithError(
            TransformNR().setZ(target.toDouble()),
            listOf(0.0).toDoubleArray(),
            mockChain
        )

        assertAll(
            {
                assertEquals(1, angles.size, "Must have one joint angle")
            },
            {
                assertEquals(0, angles[0].roundToInt(), "Joint angle must be 0")
            },
            {
                assertEquals(target, error.roundToInt(), "Solve error should equal $target")
            }
        )
    }

    @Test
    @Disabled
    fun `test 2 dof arm on x`() {
        xyPlane2dofArm()

        val target = 12
        val (angles, error) = engine.inverseKinematicsWithError(
            TransformNR().setX(target.toDouble()),
            listOf(0.0, 0.0).toDoubleArray(),
            mockChain
        )

        assertAll(
            {
                assertEquals(2, angles.size, "Must have two joint angles")
            },
            {
                assertEquals(45, angles[0].roundToInt(), "First joint angle must be 45")
            },
            {
                assertEquals(
                    -45,
                    toDegrees(angles[1]).roundToInt(),
                    "Second joint angle must be -45"
                )
            },
            {
                assertEquals(0, error.roundToInt(), "Solve error should equal 0")
            }
        )
    }

    @Test
    @Disabled
    fun `basic baxter left arm test`() {
        mockChain.addLink(DHLink(270.35, 0.0, 0.0, 0.0))
        mockChain.addLink(DHLink(0.0, -31.0 + 90, 69.0, -90.0))
        mockChain.addLink(DHLink(364.35, 0.0, 0.0, 90.0))
        mockChain.addLink(DHLink(0.0, 43.0, 69.0, -90.0))
        mockChain.addLink(DHLink(368.30, 0.0, 0.0, 90.0))
        mockChain.addLink(DHLink(0.0, 72.0, 10.0, -90.0))
        mockChain.addLink(DHLink(0.0, 0.0, 0.0, 90.0))

        val baxterArmHypot = 69 + 364.35 + 374.29 + 368.3
        val triangeLegLength = baxterArmHypot / sqrt(2.0)
        val (result, error) = engine.inverseKinematicsWithError(
            TransformNR().setX(triangeLegLength).setY(triangeLegLength).setZ(270.35 - 69 - 10),
            listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0).toDoubleArray(),
            mockChain
        )
        result.forEach { println(it) }
        println("Error: $error")
    }
}
