/*
 * Copyright 2014-2023 Fraunhofer ISE
 *
 * This file is part of j60870.
 * For more information visit http://www.openmuc.org
 *
 * j60870 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * j60870 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with j60870.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.j60870;

import org.junit.Test;
import org.openmuc.j60870.ie.IeQuality;
import org.openmuc.j60870.ie.IeValueWithTransientState;
import org.openmuc.j60870.ie.InformationElement;

import static org.junit.Assert.assertEquals;

public class UtilTest {

    @Test
    public void testSetGetValue() {
        assertEquals(327832, Util.convertToInformationObjectAddress(152, 0, 5));
    }

    @Test
    public void test111() {
        InformationElement[][] informationElements = {
                {new IeValueWithTransientState(-5, true),
                        new IeQuality(true, true, true, true, true)},
                {new IeValueWithTransientState(-5, false),
                        new IeQuality(true, true, true, true, true)},
                {new IeValueWithTransientState(-64, true),
                        new IeQuality(true, true, true, true, true)},
                {new IeValueWithTransientState(10, false),
                        new IeQuality(true, true, true, true, true)}};

        for (int i = 0; i < informationElements.length; i++) {
            System.out.println(informationElements[i]);
        }
    }

}
