/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.scripting

import arrow.core.Either
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.hardware.Script
import com.neuronrobotics.bowlerkernel.hardware.device.BowlerDeviceFactory
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.SimpleDeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.LED
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.ProvisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.Servo
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedLEDFactory
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedServoFactory
import com.neuronrobotics.bowlerkernel.hardware.protocol.BowlerRPCProtocol
import com.neuronrobotics.bowlerkernel.util.emptyImmutableList
import org.jlleitschuh.guice.key
import org.junit.jupiter.api.Test
import javax.inject.Inject

class TestHardware
@Inject constructor(
    private val bowlerDeviceFactory: BowlerDeviceFactory,
    private val ledFactoryFactory: UnprovisionedLEDFactory.Factory,
    private val servoFactoryFactory: UnprovisionedServoFactory.Factory
) : Script() {

    override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
        val device = bowlerDeviceFactory.makeBowlerDevice(
            SimpleDeviceId("/dev/ttyACM0"),
            object : BowlerRPCProtocol {
                override fun write() {
                }

                override fun read() {
                }
            }
        ).fold({ throw IllegalStateException(it) }, { it })

        val ledFactory = ledFactoryFactory.create(device)
        val led1 = ledFactory.makeUnprovisionedLED(
            ResourceId(
                DefaultResourceTypes.DigitalOut,
                DefaultAttachmentPoints.Pin(1)
            )
        ).provisionOrFail() as LED

        val led2 = ledFactory.makeUnprovisionedLED(
            ResourceId(
                DefaultResourceTypes.DigitalOut,
                DefaultAttachmentPoints.Pin(2)
            )
        ).provisionOrFail() as LED

        val servoFactory = servoFactoryFactory.create(device)
        val servo = servoFactory.makeUnprovisionedServo(
            ResourceId(
                DefaultResourceTypes.Servo,
                DefaultAttachmentPoints.Pin(3)
            )
        ).provisionOrFail() as Servo

        return Either.right(null)
    }

    override fun stopScript() {
    }
}

internal class HardwareScriptIntegrationTest {

    @Test
    fun `test registering some hardware`() {
        val script = object : Script() {
            override fun runScript(args: ImmutableList<Any?>): Either<String, Any?> {
                injector.getInstance(key<TestHardware>()).let {
                    it.runScript(args)
                    it.stopAndCleanUp()
                }
                return Either.right(null)
            }

            override fun stopScript() {
            }
        }

        script.runScript(emptyImmutableList())
        script.stopAndCleanUp()
    }
}

private inline fun <reified A : UnprovisionedDeviceResource> Either<String, A>.provisionOrFail():
    ProvisionedDeviceResource {
    return fold({ throw IllegalStateException(it) }, { it }).provision()
        .fold({ throw IllegalStateException(it) }, { it })
}