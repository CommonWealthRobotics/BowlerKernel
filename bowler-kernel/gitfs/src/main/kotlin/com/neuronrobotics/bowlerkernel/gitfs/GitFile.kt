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
package com.neuronrobotics.bowlerkernel.gitfs

/**
 * Represents a file in Git.
 *
 * @param gitUrl The `.git` URL the repository could be cloned from, i.e.
 * `https://github.com/CommonWealthRobotics/BowlerBuilder.git` or
 * `https://gist.github.com/5681d11165708c3aec1ed5cf8cf38238.git`.
 * @param filename The name of the file in the repo (including extension).
 */
data class GitFile(
    val gitUrl: String,
    val filename: String
)