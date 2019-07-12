/******************************************************************************
 * STMC - Statistical Model Checker                                           *
 *                                                                            *
 * Copyright (C) 2019                                                         *
 * Authors:                                                                   *
 *     Nima Roohi <nroohi@ucsd.edu> (University of California San Diego)      *
 *                                                                            *
 * This file is part of STMC.                                                 *
 *                                                                            *
 * STMC is free software: you can redistribute it and/or modify               *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 *                                                                            *
 * Foobar is distributed in the hope that it will be useful,                  *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with Foobar.  If not, see <https://www.gnu.org/licenses/>.           *
 ******************************************************************************/

package edu.stmc;

/** Result of comparing a probability with a constant (often called threshold) */
public class CompResult {

  /** Used when test has two possible outputs */
  public enum Binary {
    UNDECIDED,
    SMALLER,
    LARGER
  }

  /** Used when test has three possible outputs */
  public enum Ternary {
    UNDECIDED,
    TOO_CLOSE,
    SMALLER,
    LARGER,
  }

}

