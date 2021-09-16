/*
 * Copyright (C) 2021 Nick Iacullo
 *
 * This file is part of Hourglass.
 *
 * Hourglass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hourglass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Hourglass.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.lavabucket.hourglass.time;

import java.text.DecimalFormat;
import java.util.Comparator;

/**
 * A time or duration in Minecraft.
 *
 * <p>This class represents time as an integral component and a fractional component so that time
 * may be adjusted in increments smaller than 1 tick without losing the precision of a {@code long}.
 *
 * <p>The traditional integral component of a {@code Time} object compatible with Minecraft is
 * represented as a {@code long} and can be retrieved with {@link #longValue()}.
 * The fractional component is stored as a {@code double} with a value in the range [0,1) and can be
 * retrieved with {@link #fractionalValue()}.
 *
 * <p>For example, a {@code Time} object with a value of 6000.5 represents a time half way between
 * the ticks 6000 and 6001. The integral portion of this time is 6000 and the fractional portion is
 * 0.5.
 */
public class Time extends Number implements Comparable<Time> {

    /** The duration, in ticks, of a full Overworld day. */
    public static final int DAY_TICKS = 24000;

    /** The duration, in ticks, of a full Overworld lunar cycle. */
    public static final int LUNAR_CYCLE_TICKS = 192000;

    /** A {@link Time} representation of {@link #DAY_TICKS}. */
    public static final Time DAY_LENGTH = new Time(DAY_TICKS);

    /** A {@link Time} representation of {@link #LUNAR_CYCLE_TICKS}. */
    public static final Time LUNAR_CYCLE_LENGTH = new Time(LUNAR_CYCLE_TICKS);

    /** Time of day at noon in the Overworld. */
    public static final Time NOON = new Time(6000);

    /** Time of day at midnight in the Overworld. */
    public static final Time MIDNIGHT = new Time(18000);

    /** Time of day at morning in the Overworld. */
    public static final Time MORNING = new Time(0);

    private final long longPart;
    private final double fractionPart;

    /** Instantiates a new time object with a value of 0. */
    public Time() {
        this.longPart = 0;
        this.fractionPart = 0;
    }

    /**
     * Instantiates a new time object.
     * @param time  a time or duration
     */
    public Time(long time) {
        this.longPart = time;
        this.fractionPart = 0;
    }

    /**
     * Instantiates a new time object.
     * @param time  a time or duration represented as a double
     */
    public Time(double time) {
        this(0, time);
    }

    /** Initializes a time object with a long component and fraction component. */
    public Time(long longPart, double fractionPart) {
        // Constrain fractionPart to 0<= |fractionPart| < 1
        long overflow = (long) fractionPart;
        longPart = longPart + overflow;
        fractionPart = fractionPart - overflow;

        // Keep longPart and fractionPart the same sign
        if (longPart != 0 && fractionPart != 0 && longPart > 0 != fractionPart > 0) {
            if (longPart > 0) {
                longPart--;
                fractionPart++;
            } else {
                longPart++;
                fractionPart--;
            }
        }

        this.longPart = longPart;
        this.fractionPart = fractionPart;
    }

    /**
     * Returns the value of this {@code Time} as a {@code long} after dropping its fractional
     * component.
     *
     * @return the value of this {@code Time} as a {@code long}
     * @see #fractionPart
     */
    @Override
    public long longValue() {
        return longPart;
    }

    /**
     * Returns the fractional component of this {@code Time}. The value of this component is in the
     * range [0,1).
     *
     * <p>For example, a {@code Time} value of 500.75 has a fractional component value of 0.75.
     *
     * @return the fractional component of this {code Time}
     */
    public double fractionalValue() {
        return fractionPart;
    }

    /**
     * Returns the value of this {@code Time} as an {@code int} after a narrowing conversion of its
     * integral component.
     *
     * @return the value of this {@code Time} as an {@code int}
     */
    @Override
    public int intValue() {
        return (int) longValue();
    }

    /**
     * Returns the value of this {@code Time} as a {@code double} after a widening conversion.
     * This operation may reduce precision.
     *
     * @return the value of this {@code Time} as a {@code double}
     */
    @Override
    public double doubleValue() {
        return (double) longPart + fractionPart;
    }

    /**
     * Returns the value of this {@code Time} as a {@code float} after a widening conversion.
     * This operation may reduce precision.
     *
     * @return the value of this {@code Time} as a {@code float}
     */
    @Override
    public float floatValue() {
        return (float) doubleValue();
    }

    /**
     * {@return this time-of-day, between 0 (inclusive) and {@link #DAY_TICKS} (not inclusive)}
     */
    public Time timeOfDay() {
        return mod(DAY_TICKS);
    }

    /**
     * Returns the time-of-day of {@code time}, between 0 (inclusive) and
     * {@link #DAY_TICKS} (not inclusive).
     *
     * @param time  the time calculate
     * @return the time-of-day
     */
    public static long timeOfDay(long time) {
        return time % DAY_TICKS;
    }

    /**
     * {@return true if a new day has started between {@code a} and {@code b}}
     * @param a  the first time to check
     * @param b  the second time to check
     */
    public static boolean crossedMorning(Time a, Time b) {
        return a.getDay() != b.getDay();
    }

    /**
     * Returns {@code this} time's corresponding Overworld day, with the first day returning 1.
     * Days are counted every {@link #DAY_TICKS} ticks.
     *
     * @return {@code this} time's corresponding Overworld day
     */
    public long getDay() {
        return this.longPart / DAY_TICKS;
    }

    /**
     * {@return a new {@link Time} object with a value of {@code this + val}}
     * @param val  the value to be added to {@code this}
     */
    public Time add(Time val) {
        return new Time(this.longPart + val.longPart, this.fractionPart + val.fractionPart);
    }

    /**
     * {@return a new {@link Time} object with a value of {@code this + val}}
     * @param val  the value to be added to {@code this}
     */
    public Time add(long val) {
        return new Time(this.longPart + val, this.fractionPart);
    }

    /**
     * {@return a new {@link Time} object with a value of {@code this + val}}
     * @param val  the value to be added to {@code this}
     */
    public Time add(double val) {
        return add(new Time(val));
    }

    /**
     * {@return a new {@link Time} object with a value of {@code this - val}}
     * @param val  the value to be subtracted from {@code this}
     */
    public Time subtract(Time val) {
        return new Time(this.longPart - val.longPart, this.fractionPart - val.fractionPart);
    }

    /**
     * {@return a new {@link Time} object with a value of {@code this - val}}
     * @param val  the value to be subtracted from {@code this}
     */
    public Time subtract(long val) {
        return new Time(this.longPart - val, this.fractionPart);
    }

    /**
     * {@return a new {@link Time} object with a value of {@code this - val}}
     * @param val  the value to be subtracted from {@code this}
     */
    public Time subtract(double val) {
        return subtract(new Time(val));
    }

    /**
     * {@return a new {@link Time} object with a value value of {@code this / val}}
     * A loss of precision may occur do to conversion from {@code long} to {@code double}.
     *
     * @param val  the denominator
     */
    public double divide(Time val) {
        return this.doubleValue() / val.doubleValue();
    }

    /**
     * {@return a new {@link Time} object with a value value of {@code this / val}}
     * A loss of precision may occur do to conversion from {@code long} to {@code double}.
     *
     * @param val  the denominator
     */
    public double divide(double val) {
        return this.doubleValue() / val;
    }

    /**
     * {@return a new {@link Time} object with a value value of {@code this / val}}
     * A loss of precision may occur do to conversion from {@code long} to {@code double}.
     *
     * @param val  the denominator
     */
    public double divide(long val) {
        return this.doubleValue() / val;
    }

    /**
     * {@return a new {@link Time} object with a value of {@code -this}}
     */
    public Time negate() {
        return new Time(-longPart, -fractionPart);
    }

    /**
     * {@return a {@link Time} object whose value is the absolute value of {@code this}}
     */
    public Time abs() {
        return compareTo(new Time(0, 0)) < 0 ? negate() : this;
    }

    /**
     * {@return a {@link Time} whose value is {@code this modulo val}}
     * @param val  the divisor
     */
    public Time mod(long val) {
        return new Time(this.longPart % val, this.fractionPart);
    }

    /**
     * Checks if {@code this} time is between {@code a} and {@code b} in modular arithmetic.
     *
     * All three times <b>must</b> be reduced before using this method.
     *
     * @param a  the earlier time
     * @param b  the later time
     * @return true if {@code a < this < b} in modular arithmetic
     */
    public boolean betweenMod(Time a, Time b) {
        if (a.equals(b)) {
            return false;
        } else if (a.compareTo(b) < 0) {
            return this.compareTo(a) > 0 && this.compareTo(b) < 0;
        } else {
            return this.compareTo(a) > 0 || this.compareTo(b) < 0;
        }
    }

    /**
     * Compares {@code this} object with {@code other}.
     *
     * @param other  the object to compare
     * @return -1 if {@code this < other}, 0 if {@code this == other}, or 1 if {@code this > other}
     */
    @Override
    public int compareTo(Time other) {
        return Comparator
                .comparingLong((Time time) -> time.longPart)
                .thenComparingDouble((Time time) -> time.fractionPart)
                .compare(this, other);
    }

    /** {@return a hash code for this {@code Time}} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long fractionBits = Double.doubleToLongBits(fractionPart);
        result = prime * result + (int) (longPart ^ (longPart >>> 32));
        result = prime * result + (int) (fractionBits ^ (fractionBits >>> 32));
        return result;
    }

    /**
     * Compares {@code this} object to {@code obj}. Returns true if and only if {@code obj} is not
     * null and is a {@code Time} object with the same integral and fractional components as
     * {@code this}.
     *
     * @param obj  the object to compare
     * @return true if the objects are the same, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Time other = (Time) obj;
        if (Double.doubleToLongBits(fractionPart) != Double.doubleToLongBits(other.fractionPart))
            return false;
        if (longPart != other.longPart)
            return false;
        return true;
    }

    /**
     * Returns a string representation of this {@code Time} object, with a combined integral and
     * fractional components. This string will be rounded to have a maximum of 6 decimal digits.
     *
     * <p><b>Examples:</b>
     * <p>{@code new Time(24000).toString() --> "24000"}
     * <p>{@code new Time(.5D).toString() --> "0.5"}
     * <p>{@code new Time(500, 0.123D).toString() --> "500.123"}
     * <p>{@code new Time(-1000).toString() --> "-1000"}
     *
     * @return a string representation of this {@code Time}
    */
    @Override
    public String toString() {
        if (fractionPart == 0) {
            return Long.toString(longPart);
        } else if (longPart == 0) {
            DecimalFormat df = new DecimalFormat("#");
            df.setMaximumFractionDigits(6);
            df.setMinimumIntegerDigits(1);
            return df.format(fractionPart);
        } else {
            DecimalFormat df = new DecimalFormat("#");
            df.setMaximumFractionDigits(6);
            df.setMaximumIntegerDigits(0);
            return longPart + df.format(Math.abs(fractionPart));
        }
    }

}
