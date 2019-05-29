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

import com.google.common.collect.ImmutableMap
import org.octogonapus.ktguava.klaxon.ConvertImmutableMap
import org.octogonapus.ktunits.quantities.Length
import org.octogonapus.ktunits.quantities.Mass

sealed class DefaultShaft(
    override val width: Length,
    override val length: Length,
    override val height: Length,
    override val mass: Mass,
    override val centerOfMass: CenterOfMass,
    @ConvertImmutableMap
    override val specs: ImmutableMap<String, Any>
) : Shaft {

    data class SquareShaft(
        override val width: Length,
        override val length: Length,
        override val height: Length,
        override val mass: Mass,
        override val centerOfMass: CenterOfMass,
        @ConvertImmutableMap
        override val specs: ImmutableMap<String, Any>
    ) : DefaultShaft(width, length, height, mass, centerOfMass, specs)

    data class RoundShaft(
        override val width: Length,
        override val length: Length,
        override val height: Length,
        override val mass: Mass,
        override val centerOfMass: CenterOfMass,
        @ConvertImmutableMap
        override val specs: ImmutableMap<String, Any>
    ) : DefaultShaft(width, length, height, mass, centerOfMass, specs)

    data class DShaft(
        override val width: Length,
        override val length: Length,
        override val height: Length,
        override val mass: Mass,
        override val centerOfMass: CenterOfMass,
        @ConvertImmutableMap
        override val specs: ImmutableMap<String, Any>
    ) : DefaultShaft(width, length, height, mass, centerOfMass, specs)

    sealed class ServoHorn(
        override val width: Length,
        override val length: Length,
        override val height: Length,
        override val mass: Mass,
        override val centerOfMass: CenterOfMass,
        @ConvertImmutableMap
        override val specs: ImmutableMap<String, Any>
    ) : DefaultShaft(width, length, height, mass, centerOfMass, specs) {

        data class Arm(
            override val width: Length,
            override val length: Length,
            override val height: Length,
            override val mass: Mass,
            override val centerOfMass: CenterOfMass,
            @ConvertImmutableMap
            override val specs: ImmutableMap<String, Any>
        ) : ServoHorn(width, length, height, mass, centerOfMass, specs)

        data class DoubleArm(
            override val width: Length,
            override val length: Length,
            override val height: Length,
            override val mass: Mass,
            override val centerOfMass: CenterOfMass,
            @ConvertImmutableMap
            override val specs: ImmutableMap<String, Any>
        ) : ServoHorn(width, length, height, mass, centerOfMass, specs)

        data class XArm(
            override val width: Length,
            override val length: Length,
            override val height: Length,
            override val mass: Mass,
            override val centerOfMass: CenterOfMass,
            @ConvertImmutableMap
            override val specs: ImmutableMap<String, Any>
        ) : ServoHorn(width, length, height, mass, centerOfMass, specs)

        data class Wheel(
            override val width: Length,
            override val length: Length,
            override val height: Length,
            override val mass: Mass,
            override val centerOfMass: CenterOfMass,
            @ConvertImmutableMap
            override val specs: ImmutableMap<String, Any>
        ) : ServoHorn(width, length, height, mass, centerOfMass, specs)
    }
}