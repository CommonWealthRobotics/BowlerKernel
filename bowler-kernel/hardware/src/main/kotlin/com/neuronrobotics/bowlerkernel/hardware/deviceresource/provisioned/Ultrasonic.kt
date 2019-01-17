/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.hardware.deviceresource.provisioned

interface Ultrasonic : ProvisionedDeviceResource {

    /**
     * Reads the current distance measurement. If no object was found, this returns 0.
     *
     * @return The current distance.
     */
    fun read(): Long
}
