/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.registry

import arrow.core.Either
import arrow.core.Option
import com.google.common.collect.ImmutableSet
import com.google.common.collect.ImmutableSetMultimap
import com.neuronrobotics.bowlerkernel.hardware.device.Device
import com.neuronrobotics.bowlerkernel.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.DeviceResource
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.resourceid.ResourceId
import com.neuronrobotics.bowlerkernel.hardware.deviceresource.unprovisioned.UnprovisionedDeviceResource

/**
 * A utility to keep track of what hardware is in use.
 */
internal interface HardwareRegistry {

    /**
     * The currently registered devices.
     */
    val registeredDevices: ImmutableSet<Device>

    /**
     * The currently registered device resources.
     */
    val registeredDeviceResources: ImmutableSetMultimap<Device, DeviceResource>

    /**
     * Registers a device id. Fails if the device is already registered.
     *
     * @param deviceId The device id to register.
     * @param makeDevice A lambda to construct the device.
     * @return An empty option on success, a [RegisterError] on failure.
     */
    fun <T : Device> registerDevice(
        deviceId: DeviceId,
        makeDevice: (DeviceId) -> T
    ): Either<RegisterError, T>

    /**
     * Registers a resource id attached (physically) to a device. Fails if the resource is
     * already registered or if the device is not registered.
     *
     * @param device The device the resource is attached to.
     * @param resourceId The resource id to register.
     * @param makeResource A lambda to construct the unprovisioned resource.
     * @return An empty option on success, a [RegisterError] on failure.
     */
    fun <D : Device, T : UnprovisionedDeviceResource> registerDeviceResource(
        device: D,
        resourceId: ResourceId,
        makeResource: (D, ResourceId) -> T
    ): Either<RegisterError, T>

    /**
     * Unregisters and disconnects a device. Fails if the device is not registered or if the
     * device still has registered resources.
     *
     * @param device The device to unregister.
     * @return An empty option on success, an [UnregisterError] on failure.
     */
    fun unregisterDevice(device: Device): Option<UnregisterError>

    /**
     * Unregisters a resource id attached (physically) to a device. Fails if the resource is
     * not registered or if the device is not registered.
     *
     * @param resource The resource to unregister.
     * @return An empty option on success, an [UnregisterError] on failure.
     */
    fun unregisterDeviceResource(
        resource: DeviceResource
    ): Option<UnregisterError>
}