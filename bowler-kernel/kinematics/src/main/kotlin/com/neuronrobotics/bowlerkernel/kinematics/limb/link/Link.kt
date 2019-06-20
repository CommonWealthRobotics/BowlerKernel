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
package com.neuronrobotics.bowlerkernel.kinematics.limb.link

import com.neuronrobotics.bowlerkernel.kinematics.limb.Limb
import com.neuronrobotics.bowlerkernel.kinematics.motion.InertialStateEstimator
import com.neuronrobotics.bowlerkernel.util.JointLimits

/**
 * A link which can form the chain of a [Limb].
 */
interface Link {

    /**
     * The type of this link.
     */
    val type: LinkType

    /**
     * The DH description of this link.
     */
    val dhParam: DhParam

    /**
     * The movement limits of this link. This can represent angle for a rotary link or length for
     * a prismatic link.
     */
    val jointLimits: JointLimits

    /**
     * The [InertialStateEstimator].
     */
    val inertialStateEstimator: InertialStateEstimator
}
