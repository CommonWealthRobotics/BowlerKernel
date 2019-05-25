/*
 * This file is part of bowler-kernel.
 *
 * bowler-kernel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bowler-kernel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bowler-kernel.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlerkernel.vitamins.vitamin

import org.octogonapus.ktunits.quantities.AngularVelocity
import org.octogonapus.ktunits.quantities.ElectricCurrent
import org.octogonapus.ktunits.quantities.ElectricPotential
import org.octogonapus.ktunits.quantities.Power
import org.octogonapus.ktunits.quantities.Torque

/**
 * A generic DC motor.
 *
 * Good things to put in [Vitamin.specs]:
 *  - Peak power RPM
 *  - Peak efficiency RPM
 *  - Mounting holes (Vitamin for the screws and a bolt circle diameter)
 */
interface DCMotor : Vitamin {

    /**
     * The operating voltage.
     */
    val voltage: ElectricPotential

    /**
     * The free speed.
     */
    val freeSpeed: AngularVelocity

    /**
     * The free current.
     */
    val freeCurrent: ElectricCurrent

    /**
     * The stall torque.
     */
    val stallTorque: Torque

    /**
     * The stall current.
     */
    val stallCurrent: ElectricCurrent

    /**
     * The maximum power output.
     */
    val power: Power

    /**
     * The shaft type.
     */
    val shaft: DefaultShaft
}
