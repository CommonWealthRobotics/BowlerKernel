/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.registry

import arrow.core.right
import com.neuronrobotics.bowlerkernel.hardware.device.Device
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.SimpleDeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned.ProvisionedDeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultAttachmentPoints
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.DefaultResourceTypes
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDeviceResource
import org.junit.jupiter.api.Assertions.fail

internal data class MockDevice(
    override val deviceId: DeviceId
) : Device {
    var connectWasCalled = false
    var disconnectWasCalled = false

    override fun connect() {
        connectWasCalled = true
    }

    override fun disconnect() {
        disconnectWasCalled = true
    }

    override fun isResourceInRange(resourceId: ResourceId) = true
}

internal data class MockUnprovisionedDeviceResource(
    override val device: Device,
    override val resourceId: ResourceId
) : UnprovisionedDeviceResource {
    override fun provision() =
        MockProvisionedDeviceResource(
            device,
            resourceId
        ).right()
}

internal class MockProvisionedDeviceResource(
    override val device: Device,
    override val resourceId: ResourceId
) : ProvisionedDeviceResource

internal fun HardwareRegistry.makeDeviceOrFail(id: String): MockDevice =
    registerDevice(SimpleDeviceId(id)) {
        MockDevice(it)
    }.fold(
        { fail<MockDevice> { it } },
        { it }
    )

internal fun HardwareRegistry.makeDeviceResourceOrFail(
    device: Device,
    attachmentPoint: Byte
): MockUnprovisionedDeviceResource =
    registerDeviceResource(
        device,
        ResourceId(DefaultResourceTypes.DigitalOut, DefaultAttachmentPoints.Pin(attachmentPoint))
    ) { device, resource ->
        MockUnprovisionedDeviceResource(device, resource)
    }.fold(
        { fail<MockUnprovisionedDeviceResource> { it } },
        { it }
    )